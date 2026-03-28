package bhw.voident.xyz.terrariafabric.npc.home;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/** 类用途：区分房屋脏区触发方块和自动扫描时值得尝试的家具锚点。 */
public final class HousingRelevantBlocks {

    private HousingRelevantBlocks() {
    }

    public static boolean isRelevant(ServerLevel level, BlockPos pos, BlockState state) {
        return isAutoScanSource(state) || (!state.isAir() && !state.getCollisionShape(level, pos).isEmpty());
    }

    public static int enqueueNearbyRoomStarts(ServerLevel level, BlockPos center, int radius, int limit, HousingDirtyQueue queue) {
        int found = 0;
        Set<BlockPos> uniqueStarts = new HashSet<>();
        for (int x = -radius; x <= radius && found < limit; x++) {
            for (int y = -radius; y <= radius && found < limit; y++) {
                for (int z = -radius; z <= radius && found < limit; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!isAutoScanSource(state)) {
                        continue;
                    }

                    BlockPos start = HouseDetector.findStartingAir(level, pos);
                    if (start == null || !uniqueStarts.add(start.immutable())) {
                        continue;
                    }

                    queue.mark(start);
                    found++;
                }
            }
        }
        return found;
    }

    private static boolean isAutoScanSource(BlockState state) {
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return id.endsWith("_door")
                || "crafting_table".equals(id)
                || id.endsWith("_table")
                || id.contains("workbench")
                || id.endsWith("_bed")
                || id.contains("chair")
                || id.contains("stool")
                || id.contains("seat")
                || id.contains("bench")
                || id.contains("sofa")
                || id.contains("couch")
                || id.contains("torch")
                || id.contains("lantern")
                || id.contains("candle")
                || id.contains("glowstone")
                || id.contains("shroomlight")
                || id.contains("sea_lantern");
    }
}
