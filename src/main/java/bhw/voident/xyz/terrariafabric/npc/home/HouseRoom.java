package bhw.voident.xyz.terrariafabric.npc.home;

import net.minecraft.core.BlockPos;

/**

 * 类用途：房屋边界与中心数据对象。

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
