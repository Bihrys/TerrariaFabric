package bhw.voident.xyz.terrariafabric.npc.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

/** 类用途：复用 Town NPC 的到达刷点，统一按向导那套地表空气位寻找逻辑刷入世界。 */
public final class TownNpcSpawnLocations {

    private TownNpcSpawnLocations() {
    }

    public static BlockPos findArrivalSpawnPosition(ServerLevel level, ServerPlayer player, int radius, int attempts) {
        RandomSource random = level.getRandom();
        BlockPos base = player.blockPosition();
        for (int i = 0; i < attempts; i++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            BlockPos candidate = base.offset(dx, 0, dz);
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
            BlockPos spawnPos = surface.above();
            if (level.getBlockState(spawnPos).isAir()) {
                return spawnPos;
            }
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base).above();
    }
}
