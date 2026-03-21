package bhw.voident.xyz.terrariafabric.npc.spawn;

import bhw.voident.xyz.terrariafabric.npc.definition.NpcDefinition;
import bhw.voident.xyz.terrariafabric.npc.definition.NpcDefinitions;
import bhw.voident.xyz.terrariafabric.npc.home.HouseDetector;
import bhw.voident.xyz.terrariafabric.npc.home.HousingRegistry;
import bhw.voident.xyz.terrariafabric.npc.state.NpcWorldState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public final class NpcResidenceManager {

    private static final double SNAP_DISTANCE_SQR = 24.0 * 24.0;

    private NpcResidenceManager() {
    }

    public static void syncTrackedEntities(ServerLevel level) {
        for (NpcDefinition definition : NpcDefinitions.all()) {
            findTrackedEntity(level, definition, true);
        }
    }

    public static void tryWorldSpawn(ServerLevel level, ServerPlayer player) {
        NpcWorldState worldState = NpcWorldState.get(level);
        for (NpcDefinition definition : NpcDefinitions.all()) {
            if (!definition.spawnsWithWorld()) {
                continue;
            }
            NpcWorldState.NpcRecord record = worldState.getOrCreate(definition.id());
            if (record.spawnedOnce()) {
                continue;
            }
            PathfinderMob existing = findTrackedEntity(level, definition, true);
            if (existing != null) {
                worldState.setTrackedEntity(definition.id(), existing.getUUID());
                continue;
            }
            BlockPos spawnPos = definition.findWorldSpawnPosition(level, player);
            PathfinderMob spawned = spawn(level, definition, spawnPos);
            if (spawned != null) {
                worldState.setTrackedEntity(definition.id(), spawned.getUUID());
            }
        }
    }

    public static boolean assignRoom(ServerLevel level, NpcDefinition definition, HousingRegistry.RoomRecord room, boolean manual, boolean forceSpawn) {
        HousingRegistry registry = HousingRegistry.get(level);
        registry.clearOccupant(definition.id());
        registry.setOccupant(room, definition.id(), manual);
        PathfinderMob npc = ensurePresentInRoom(level, definition, room, forceSpawn);
        if (npc != null) {
            return true;
        }
        registry.setOccupant(room, null, false);
        return false;
    }

    public static boolean reconcileRoom(ServerLevel level, HousingRegistry.RoomRecord room) {
        String occupantId = room.occupantId();
        if (occupantId == null) {
            return false;
        }
        NpcDefinition definition = NpcDefinitions.byId(occupantId);
        if (definition == null) {
            return room.isOccupied();
        }
        PathfinderMob npc = findTrackedEntity(level, definition, true);
        if (npc != null) {
            moveIntoRoom(npc, room);
            return true;
        }
        if (room.isManual()) {
            return ensurePresentInRoom(level, definition, room, true) != null;
        }
        HousingRegistry.get(level).setOccupant(room, null, false);
        return false;
    }

    public static void tick(ServerLevel level) {
        HousingRegistry registry = HousingRegistry.get(level);
        NpcWorldState worldState = NpcWorldState.get(level);

        for (NpcDefinition definition : NpcDefinitions.all()) {
            PathfinderMob npc = findTrackedEntity(level, definition, true);
            HousingRegistry.RoomRecord assignedRoom = registry.findAssignedRoom(definition.id());

            // 先处理已经存在的 NPC，优先维持它和房间绑定关系，再考虑补空房。
            if (npc != null) {
                worldState.setTrackedEntity(definition.id(), npc.getUUID());
                if (assignedRoom != null) {
                    maintainRoomPosition(npc, assignedRoom);
                    continue;
                }

                HousingRegistry.RoomRecord availableRoom = registry.findAvailableRoomFor(level, definition);
                if (availableRoom != null) {
                    registry.setOccupant(availableRoom, definition.id(), false);
                    moveIntoRoom(npc, availableRoom);
                }
                continue;
            }

            NpcWorldState.NpcRecord record = worldState.getOrCreate(definition.id());
            // 不在待重生状态时直接跳过，避免每 tick 都尝试找房间和刷实体。
            if (!record.pendingRespawn()) {
                continue;
            }
            if (definition.needsHousingForRespawn() && !level.isDay()) {
                continue;
            }

            HousingRegistry.RoomRecord availableRoom = registry.findAvailableRoomFor(level, definition);
            if (availableRoom == null) {
                continue;
            }
            registry.setOccupant(availableRoom, definition.id(), false);
            if (ensurePresentInRoom(level, definition, availableRoom, true) == null) {
                registry.setOccupant(availableRoom, null, false);
            }
        }
    }

    public static void onNpcDeath(ServerLevel level, String npcId, UUID entityUuid) {
        NpcDefinition definition = NpcDefinitions.byId(npcId);
        if (definition == null) {
            return;
        }
        PathfinderMob tracked = findTrackedEntity(level, definition, false);
        if (tracked != null && !tracked.getUUID().equals(entityUuid)) {
            return;
        }
        HousingRegistry.get(level).clearOccupant(npcId);
        NpcWorldState.get(level).markPendingRespawn(npcId);
    }

    public static PathfinderMob ensurePresentInRoom(ServerLevel level, NpcDefinition definition, HousingRegistry.RoomRecord room, boolean forceSpawn) {
        PathfinderMob npc = findTrackedEntity(level, definition, true);
        if (npc != null) {
            moveIntoRoom(npc, room);
            return npc;
        }

        if (!forceSpawn && definition.needsHousingForRespawn() && !level.isDay()) {
            return null;
        }

        BlockPos spawnPos = HouseDetector.findSpawnPositionInRoom(level, room.min(), room.max());
        if (spawnPos == null || !level.hasChunkAt(room.center())) {
            return null;
        }

        PathfinderMob spawned = spawn(level, definition, spawnPos);
        if (spawned == null) {
            return null;
        }

        moveIntoRoom(spawned, room);
        NpcWorldState.get(level).setTrackedEntity(definition.id(), spawned.getUUID());
        return spawned;
    }

    private static PathfinderMob spawn(ServerLevel level, NpcDefinition definition, BlockPos spawnPos) {
        PathfinderMob npc = (PathfinderMob) definition.entityType().spawn(level, spawnPos, MobSpawnType.EVENT);
        if (npc == null) {
            return null;
        }
        npc.setCustomName(net.minecraft.network.chat.Component.literal(definition.pickName(level.getRandom())));
        definition.onSpawn(npc, level);
        return npc;
    }

    private static void maintainRoomPosition(PathfinderMob npc, HousingRegistry.RoomRecord room) {
        BlockPos center = room.center();
        if (!npc.hasRestriction()
                || !center.equals(npc.getRestrictCenter())
                || npc.getRestrictRadius() < room.radius()) {
            moveIntoRoom(npc, room);
            return;
        }
        if (!npc.isWithinRestriction()
                || npc.distanceToSqr(center.getX() + 0.5, center.getY(), center.getZ() + 0.5) > SNAP_DISTANCE_SQR) {
            moveIntoRoom(npc, room);
        }
    }

    private static void moveIntoRoom(PathfinderMob npc, HousingRegistry.RoomRecord room) {
        BlockPos center = room.center();
        npc.restrictTo(center, room.radius());

        BlockPos spawnPos = center;
        if (npc.level() instanceof ServerLevel level) {
            BlockPos found = HouseDetector.findSpawnPositionInRoom(level, room.min(), room.max());
            if (found != null) {
                spawnPos = found;
            }
        }

        npc.getNavigation().stop();
        npc.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        npc.setDeltaMovement(Vec3.ZERO);
    }

    private static PathfinderMob findTrackedEntity(ServerLevel level, NpcDefinition definition, boolean allowScan) {
        NpcWorldState.NpcRecord record = NpcWorldState.get(level).getOrCreate(definition.id());
        // 先走持久化 UUID；只有记录失效时才退回全图扫描，降低常驻开销。
        if (record.entityUuid() != null) {
            Entity entity = level.getEntity(record.entityUuid());
            if (definition.entityClass().isInstance(entity)) {
                return definition.entityClass().cast(entity);
            }
        }
        if (!allowScan) {
            return null;
        }

        List<? extends PathfinderMob> entities = level.getEntities(
                EntityTypeTest.forClass(definition.entityClass()),
                candidate -> true
        );
        if (entities.isEmpty()) {
            return null;
        }
        PathfinderMob found = entities.get(0);
        NpcWorldState.get(level).setTrackedEntity(definition.id(), found.getUUID());
        return found;
    }
}

