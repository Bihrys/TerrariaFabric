package bhw.voident.xyz.terrariafabric.npc.home;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public final class HousingRelevantBlocks {

    private HousingRelevantBlocks() {
    }

    public static boolean isRelevant(ServerLevel level, BlockPos pos, BlockState state) {
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
                || id.contains("sea_lantern")
                || (!state.isAir() && !state.getCollisionShape(level, pos).isEmpty());
    }

    public static int enqueueNearbyAnchors(ServerLevel level, BlockPos center, int radius, int limit, HousingDirtyQueue queue) {
        int found = 0;
        for (int x = -radius; x <= radius && found < limit; x++) {
            for (int y = -radius; y <= radius && found < limit; y++) {
                for (int z = -radius; z <= radius && found < limit; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!isRelevant(level, pos, level.getBlockState(pos))) {
                        continue;
                    }
                    queue.mark(pos);
                    found++;
                }
            }
        }
        return found;
    }
}

