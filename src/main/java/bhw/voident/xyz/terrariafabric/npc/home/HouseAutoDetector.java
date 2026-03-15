package bhw.voident.xyz.terrariafabric.npc.home;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class HouseAutoDetector {

    private static final long SCAN_INTERVAL = 1200L;
    private static final int SEARCH_RADIUS = 8;

    private HouseAutoDetector() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(HouseAutoDetector::tickWorld);
    }

    private static void tickWorld(ServerLevel level) {
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return;
        }
        if (level.getGameTime() % SCAN_INTERVAL != 0) {
            return;
        }
        if (level.players().isEmpty()) {
            return;
        }

        HousingData data = HousingData.get(level);
        for (ServerPlayer player : level.players()) {
            BlockPos base = player.blockPosition();
            for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
                for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
                    for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                        BlockPos pos = base.offset(dx, dy, dz);
                        if (!level.getBlockState(pos).is(Blocks.CRAFTING_TABLE)) {
                            continue;
                        }
                        HouseCheckResult result = HouseDetector.check(level, pos);
                        if (result.isSuitable() && result.room() != null) {
                            data.getOrCreate(result.room());
                        }
                    }
                }
            }
        }
    }
}
