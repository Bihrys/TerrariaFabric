package bhw.voident.xyz.terrariafabric.sit;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class SitUtil {
    private static final Map<ResourceKey<Level>, Map<BlockPos, SeatData>> OCCUPIED = new HashMap<>();

    private SitUtil() {
    }

    public static boolean addSitEntity(Level level, BlockPos blockPos, SitSeatEntity entity, Vec3 playerPos) {
        if (level.isClientSide()) {
            return false;
        }

        ResourceKey<Level> key = level.dimension();
        OCCUPIED.computeIfAbsent(key, unused -> new HashMap<>())
                .put(blockPos, new SeatData(entity, playerPos));
        return true;
    }

    public static boolean removeSitEntity(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false;
        }

        ResourceKey<Level> key = level.dimension();
        Map<BlockPos, SeatData> map = OCCUPIED.get(key);
        if (map == null) {
            return false;
        }

        map.remove(pos);
        return true;
    }

    public static SitSeatEntity getSitEntity(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return null;
        }

        ResourceKey<Level> key = level.dimension();
        Map<BlockPos, SeatData> map = OCCUPIED.get(key);
        if (map == null) {
            return null;
        }

        SeatData data = map.get(pos);
        if (data == null) {
            return null;
        }

        if (data.entity().isRemoved()) {
            map.remove(pos);
            return null;
        }

        return data.entity();
    }

    public static Vec3 getPreviousPlayerPosition(Player player, SitSeatEntity sitEntity) {
        if (player.level().isClientSide()) {
            return null;
        }

        ResourceKey<Level> key = player.level().dimension();
        Map<BlockPos, SeatData> map = OCCUPIED.get(key);
        if (map == null) {
            return null;
        }

        for (SeatData data : map.values()) {
            if (data.entity() == sitEntity) {
                return data.playerPos();
            }
        }

        return null;
    }

    public static boolean isPlayerSitting(Player player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof SitSeatEntity || vehicle instanceof Player) {
            return true;
        }

        if (player.level().isClientSide()) {
            return false;
        }

        ResourceKey<Level> key = player.level().dimension();
        Map<BlockPos, SeatData> map = OCCUPIED.get(key);
        if (map == null) {
            return false;
        }

        for (SeatData data : map.values()) {
            if (data.entity().hasPassenger(player)) {
                return true;
            }
        }

        return false;
    }

    private record SeatData(SitSeatEntity entity, Vec3 playerPos) {
    }
}
