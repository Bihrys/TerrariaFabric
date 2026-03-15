package bhw.voident.xyz.terrariafabric.npc.spawn;

import bhw.voident.xyz.terrariafabric.entity.GuideEntity;
import bhw.voident.xyz.terrariafabric.entity.TerrariafabricEntities;
import bhw.voident.xyz.terrariafabric.npc.NpcNames;
import bhw.voident.xyz.terrariafabric.npc.home.HouseDetector;
import bhw.voident.xyz.terrariafabric.npc.home.HousingData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class GuideSpawner {

    private static final long SPAWN_INTERVAL = 200L;
    private static final String[] GUIDE_FALLBACK_NAMES = new String[]{
            "Andrew", "Asher", "Bradley", "Brandon", "Brett", "Brian", "Cody", "Cole",
            "Colin", "Connor", "Daniel", "Dylan", "Garrett", "Harley", "Jack", "Jacob",
            "Jake", "Jan", "Jeff", "Jeffrey", "Joe", "Kevin", "Kyle", "Levi", "Logan",
            "Luke", "Marty", "Maxwell", "Ryan", "Scott", "Seth", "Steve", "Tanner",
            "Trent", "Wyatt", "Zach"
    };

    private GuideSpawner() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(GuideSpawner::tickWorld);
    }

    private static void tickWorld(ServerLevel level) {
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return;
        }
        if (level.getGameTime() % SPAWN_INTERVAL != 0) {
            return;
        }

        HousingData data = HousingData.get(level);
        GuideEntity existing = findGuide(level);
        if (existing != null) {
            HousingData.RoomRecord assigned = data.findAssignedRoom(HousingData.NPC_GUIDE);
            if (assigned == null) {
                HousingData.RoomRecord empty = data.findRandomEmptyRoom(level.getRandom());
                if (empty != null) {
                    data.setOccupant(empty, HousingData.NPC_GUIDE, empty.isManual());
                    assignGuideToRoom(existing, empty);
                }
            } else {
                ensureGuideHome(existing, assigned);
            }
            return;
        }

        if (!level.isDay()) {
            return;
        }

        HousingData.RoomRecord target = data.findAssignedRoom(HousingData.NPC_GUIDE);
        if (target == null) {
            target = data.findRandomEmptyRoom(level.getRandom());
        }
        if (target == null) {
            return;
        }

        spawnGuideInRoom(level, target);
    }

    public static GuideEntity findGuide(ServerLevel level) {
        AABB bounds = new AABB(
                level.getWorldBorder().getMinX(),
                level.getMinBuildHeight(),
                level.getWorldBorder().getMinZ(),
                level.getWorldBorder().getMaxX(),
                level.getMaxBuildHeight(),
                level.getWorldBorder().getMaxZ()
        );
        List<GuideEntity> guides = level.getEntitiesOfClass(GuideEntity.class, bounds);
        if (guides.isEmpty()) {
            return null;
        }
        return guides.get(0);
    }

    public static GuideEntity spawnGuideInRoom(ServerLevel level, HousingData.RoomRecord room) {
        if (room == null) {
            return null;
        }
        if (!level.hasChunkAt(room.getCenter())) {
            return null;
        }

        BlockPos spawnPos = HouseDetector.findSpawnPositionInRoom(level, room.getMin(), room.getMax());
        if (spawnPos == null) {
            HousingData data = HousingData.get(level);
            if (HousingData.NPC_GUIDE.equals(room.getOccupantId())) {
                data.setOccupant(room, null, false);
            }
            data.removeRoom(room);
            return null;
        }

        GuideEntity guide = TerrariafabricEntities.GUIDE.spawn(level, spawnPos, MobSpawnType.EVENT);
        if (guide == null) {
            return null;
        }

        guide.setCustomName(Component.literal(pickGuideName(level.getRandom()))); 
        guide.setCustomNameVisible(true);
        guide.setPersistenceRequired();
        assignGuideToRoom(guide, room);

        HousingData data = HousingData.get(level);
        if (!HousingData.NPC_GUIDE.equals(room.getOccupantId())) {
            data.setOccupant(room, HousingData.NPC_GUIDE, room.isManual());
        }
        return guide;
    }

    public static void assignGuideToRoom(GuideEntity guide, HousingData.RoomRecord room) {
        BlockPos center = room.getCenter();
        int radius = room.getRadius();
        guide.restrictTo(center, radius);
        guide.getNavigation().moveTo(center.getX() + 0.5, center.getY() + 0.1, center.getZ() + 0.5, 1.0);
    }

    private static void ensureGuideHome(GuideEntity guide, HousingData.RoomRecord room) {
        BlockPos center = room.getCenter();
        int radius = room.getRadius();
        if (!guide.hasRestriction()
                || !center.equals(guide.getRestrictCenter())
                || guide.getRestrictRadius() < radius) {
            assignGuideToRoom(guide, room);
        }
    }

    private static String pickGuideName(RandomSource random) {
        return NpcNames.pick(HousingData.NPC_GUIDE, random, GUIDE_FALLBACK_NAMES);
    }
}
