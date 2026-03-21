package bhw.voident.xyz.terrariafabric.npc.home;

import net.minecraft.core.BlockPos;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public final class HouseRoom {
    private final BlockPos min;
    private final BlockPos max;

    public HouseRoom(BlockPos min, BlockPos max) {
        this.min = min;
        this.max = max;
    }

    public BlockPos min() {
        return min;
    }

    public BlockPos max() {
        return max;
    }
}
