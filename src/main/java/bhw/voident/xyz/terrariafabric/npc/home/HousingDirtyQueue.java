package bhw.voident.xyz.terrariafabric.npc.home;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**

 * 类用途：房屋脏区队列与增量复检。

 */

public final class HousingDirtyQueue {

    private final Deque<BlockPos> anchors = new ArrayDeque<>();
    private final Set<BlockPos> queued = new HashSet<>();

    public void markAround(BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    mark(center.offset(x, y, z));
                }
            }
        }
    }

    public void mark(BlockPos anchor) {
        BlockPos immutable = anchor.immutable();
        if (!queued.add(immutable)) {
            return;
        }
        anchors.addLast(immutable);
    }

    public boolean isEmpty() {
        return anchors.isEmpty();
    }

    public void process(ServerLevel level, HousingRegistry registry, int budget) {
        for (int i = 0; i < budget && !anchors.isEmpty(); i++) {
            BlockPos anchor = anchors.removeFirst();
            queued.remove(anchor);
            registry.markDirtyAround(anchor);
            HouseCheckResult result = HouseDetector.check(level, anchor);
            registry.reconcileResult(anchor, result, level.getGameTime());
        }
    }
}

