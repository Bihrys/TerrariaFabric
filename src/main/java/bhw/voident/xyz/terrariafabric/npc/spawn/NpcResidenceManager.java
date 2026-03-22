package bhw.voident.xyz.terrariafabric.npc.spawn;

import bhw.voident.xyz.terrariafabric.advancement.TerrariafabricAdvancements;
import bhw.voident.xyz.terrariafabric.npc.definition.NpcDefinition;
import bhw.voident.xyz.terrariafabric.npc.definition.NpcDefinitions;
import bhw.voident.xyz.terrariafabric.npc.home.HouseDetector;
import bhw.voident.xyz.terrariafabric.npc.home.HousingRegistry;
import bhw.voident.xyz.terrariafabric.npc.state.NpcWorldState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

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
                    handleNpcMovedIn(level, definition, npc);
                }
                continue;
            }

            NpcWorldState.NpcRecord record = worldState.getOrCreate(definition.id());
            if (!record.spawnedOnce() && definition.canSpawnNaturally(level)) {
                HousingRegistry.RoomRecord availableRoom = registry.findAvailableRoomFor(level, definition);
                if (availableRoom != null) {
                    registry.setOccupant(availableRoom, definition.id(), false);
                    if (ensurePresentInRoom(level, definition, availableRoom, true) == null) {
                        registry.setOccupant(availableRoom, null, false);
                    }
                }
                continue;
            }

            if (!record.pendingRespawn()) {
                continue;
            }
            if (definition.needsHousingForRespawn() && !level.isDay()) {
                continue;
            }
            if (!definition.canRespawn(level)) {
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
            handleNpcMovedIn(level, definition, npc);
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
        handleNpcMovedIn(level, definition, spawned);
        return spawned;
    }

    private static PathfinderMob spawn(ServerLevel level, NpcDefinition definition, BlockPos spawnPos) {
        PathfinderMob npc = (PathfinderMob) definition.entityType().spawn(level, spawnPos, MobSpawnType.EVENT);
        if (npc == null) {
            return null;
        }
        ensureDisplayName(level, definition, npc);
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
        if (record.entityUuid() != null) {
            Entity entity = level.getEntity(record.entityUuid());
            if (definition.entityClass().isInstance(entity)) {
                PathfinderMob npc = definition.entityClass().cast(entity);
                ensureDisplayName(level, definition, npc);
                return npc;
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
        ensureDisplayName(level, definition, found);
        NpcWorldState.get(level).setTrackedEntity(definition.id(), found.getUUID());
        return found;
    }

    private static void handleNpcMovedIn(ServerLevel level, NpcDefinition definition, PathfinderMob npc) {
        ensureDisplayName(level, definition, npc);
        TerrariafabricAdvancements.awardHasAHome(level);
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            player.sendSystemMessage(Component.translatable("message.terrariafabric.npc.moved_in", npc.getDisplayName()));
        }
    }

    private static void ensureDisplayName(ServerLevel level, NpcDefinition definition, PathfinderMob npc) {
        String suffix = "(" + definition.professionSuffix() + ")";
        Component customName = npc.getCustomName();
        if (customName != null) {
            String currentName = customName.getString();
            if (!currentName.endsWith(suffix)) {
                npc.setCustomName(Component.literal(currentName + suffix));
            }
        } else {
            npc.setCustomName(Component.literal(definition.formatDisplayName(level.getRandom())));
        }
        npc.setCustomNameVisible(true);
    }
}
