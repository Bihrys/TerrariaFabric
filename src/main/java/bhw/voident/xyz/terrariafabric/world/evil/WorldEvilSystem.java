package bhw.voident.xyz.terrariafabric.world.evil;

import bhw.voident.xyz.terrariafabric.Terrariafabric;
import bhw.voident.xyz.terrariafabric.block.TerrariafabricBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/** 类用途：世界级邪恶系统，负责单世界邪恶类型选择、初始邪恶草地生成和低频蔓延。 */
public final class WorldEvilSystem {

    // 首次生成和后续蔓延都走世界级固定预算更新，不让邪恶方块自己高频 random tick。
    // 想临时提速调试蔓延就改这几组常量。
    private static final int INITIAL_PATCH_INTERVAL = 40;
    private static final int INITIAL_PATCH_ATTEMPTS = 24;
    private static final int INITIAL_PATCH_MIN_DISTANCE = 72;
    private static final int INITIAL_PATCH_MAX_DISTANCE = 128;
    private static final int INITIAL_PATCH_RADIUS = 7;
    private static final int SOURCE_SEARCH_ATTEMPTS = 10;
    private static final int SOURCE_SEARCH_RADIUS = 96;
    private static final int SOURCE_SEARCH_DEPTH = 6;
    private static final int SPREAD_ATTEMPTS_PER_CYCLE = 8;
    private static final int PRE_HARDMODE_SPREAD_INTERVAL = 40;
    private static final int HARDMODE_SPREAD_INTERVAL = 20;

    private WorldEvilSystem() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(WorldEvilSystem::tickWorld);
    }

    private static void tickWorld(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }

        WorldEvilState state = WorldEvilState.get(level);
        ensurePrimaryEvil(level, state);

        if (!state.initialPatchGenerated()) {
            tryGenerateInitialPatch(level, state);
            return;
        }

        if (!state.biomeSpreadEnabled()) {
            return;
        }

        // 预困难模式放慢一档，hardmode 再提速；两边都只按固定间隔抽样，不做每 tick 扫描。
        int interval = state.hardmodeUnlocked() ? HARDMODE_SPREAD_INTERVAL : PRE_HARDMODE_SPREAD_INTERVAL;
        if (level.getGameTime() % interval != 0L) {
            return;
        }

        spreadActiveEvil(level, EvilBiomeType.CORRUPTION);
        spreadActiveEvil(level, EvilBiomeType.CRIMSON);
    }

    private static void ensurePrimaryEvil(ServerLevel level, WorldEvilState state) {
        if (state.primaryEvilChosen()) {
            return;
        }

        EvilBiomeType chosen = level.getRandom().nextBoolean() ? EvilBiomeType.CRIMSON : EvilBiomeType.CORRUPTION;
        state.choosePrimaryEvil(chosen);
        Terrariafabric.LOGGER.info("TerrariaFabric overworld evil chosen: {}", chosen.id());
    }

    private static void tryGenerateInitialPatch(ServerLevel level, WorldEvilState state) {
        if (level.getGameTime() % INITIAL_PATCH_INTERVAL != 0L) {
            return;
        }

        List<ServerPlayer> players = activePlayers(level);
        if (players.isEmpty()) {
            return;
        }

        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < INITIAL_PATCH_ATTEMPTS; attempt++) {
            ServerPlayer player = players.get(random.nextInt(players.size()));
            BlockPos candidateCenter = randomInitialPatchCenter(level, player.blockPosition(), random);
            BlockPos surface = findSurfaceGrass(level, candidateCenter);
            if (surface == null) {
                continue;
            }
            if (convertPatch(level, surface, state.primaryEvil(), INITIAL_PATCH_RADIUS, 0.78F) > 0) {
                state.setInitialPatchGenerated(true);
                return;
            }
        }
    }

    private static BlockPos randomInitialPatchCenter(ServerLevel level, BlockPos center, RandomSource random) {
        int distance = Mth.nextInt(random, INITIAL_PATCH_MIN_DISTANCE, INITIAL_PATCH_MAX_DISTANCE);
        int angleSlice = random.nextInt(8);
        int offsetX = switch (angleSlice) {
            case 0 -> distance;
            case 1 -> distance;
            case 2 -> 0;
            case 3 -> -distance;
            case 4 -> -distance;
            case 5 -> -distance;
            case 6 -> 0;
            default -> distance;
        };
        int offsetZ = switch (angleSlice) {
            case 0 -> 0;
            case 1 -> distance;
            case 2 -> distance;
            case 3 -> distance;
            case 4 -> 0;
            case 5 -> -distance;
            case 6 -> -distance;
            default -> -distance;
        };
        BlockPos spawn = level.getSharedSpawnPos();
        int x = center.getX() + offsetX;
        int z = center.getZ() + offsetZ;
        if (Math.abs(x - spawn.getX()) < INITIAL_PATCH_MIN_DISTANCE / 2
                && Math.abs(z - spawn.getZ()) < INITIAL_PATCH_MIN_DISTANCE / 2) {
            x += offsetX >= 0 ? INITIAL_PATCH_MIN_DISTANCE : -INITIAL_PATCH_MIN_DISTANCE;
            z += offsetZ >= 0 ? INITIAL_PATCH_MIN_DISTANCE : -INITIAL_PATCH_MIN_DISTANCE;
        }
        return new BlockPos(x, center.getY(), z);
    }

    private static void spreadActiveEvil(ServerLevel level, EvilBiomeType type) {
        List<ServerPlayer> players = activePlayers(level);
        if (players.isEmpty()) {
            return;
        }

        // 每轮只挑少量已加载区域里的邪恶源块做 1 格扩散，避免退化成全图蔓延扫描。
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < SPREAD_ATTEMPTS_PER_CYCLE; attempt++) {
            ServerPlayer player = players.get(random.nextInt(players.size()));
            BlockPos source = findNearbyPrimaryGrass(level, player.blockPosition(), type, random);
            if (source == null) {
                continue;
            }
            trySpreadFromSource(level, source, type, random);
        }
    }

    private static BlockPos findNearbyPrimaryGrass(ServerLevel level, BlockPos center, EvilBiomeType type, RandomSource random) {
        for (int attempt = 0; attempt < SOURCE_SEARCH_ATTEMPTS; attempt++) {
            int x = center.getX() + random.nextInt(SOURCE_SEARCH_RADIUS * 2 + 1) - SOURCE_SEARCH_RADIUS;
            int z = center.getZ() + random.nextInt(SOURCE_SEARCH_RADIUS * 2 + 1) - SOURCE_SEARCH_RADIUS;
            BlockPos probe = new BlockPos(x, center.getY(), z);
            if (!level.hasChunkAt(probe)) {
                continue;
            }

            BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, probe).below();
            int minY = Math.max(level.getMinBuildHeight(), surface.getY() - SOURCE_SEARCH_DEPTH);
            for (int y = surface.getY() + 1; y >= minY; y--) {
                BlockPos candidate = new BlockPos(x, y, z);
                if (TerrariafabricBlocks.isPrimaryGrass(level.getBlockState(candidate).getBlock(), type)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static void trySpreadFromSource(ServerLevel level, BlockPos source, EvilBiomeType type, RandomSource random) {
        for (int attempt = 0; attempt < 4; attempt++) {
            BlockPos target = source.offset(
                    random.nextInt(3) - 1,
                    random.nextInt(3) - 1,
                    random.nextInt(3) - 1
            );
            if (!level.hasChunkAt(target) || !canConvertToEvilGrass(level, target)) {
                continue;
            }
            level.setBlock(target, TerrariafabricBlocks.primaryGrass(type).defaultBlockState(), 3);
            return;
        }
    }

    private static int convertPatch(ServerLevel level, BlockPos center, EvilBiomeType type, int radius, float density) {
        int converted = 0;
        RandomSource random = level.getRandom();
        Block block = TerrariafabricBlocks.primaryGrass(type);
        int radiusSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distanceSq = dx * dx + dz * dz;
                if (distanceSq > radiusSq || random.nextFloat() > density) {
                    continue;
                }

                BlockPos column = center.offset(dx, 0, dz);
                BlockPos target = findSurfaceGrass(level, column);
                if (target == null || !canConvertToEvilGrass(level, target)) {
                    continue;
                }

                level.setBlock(target, block.defaultBlockState(), 3);
                converted++;
            }
        }
        return converted;
    }

    private static BlockPos findSurfaceGrass(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return null;
        }

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).below();
        int minY = Math.max(level.getMinBuildHeight(), surface.getY() - SOURCE_SEARCH_DEPTH);
        for (int y = surface.getY() + 1; y >= minY; y--) {
            BlockPos candidate = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = level.getBlockState(candidate);
            if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean canConvertToEvilGrass(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.DIRT)) {
            return false;
        }

        BlockState aboveState = level.getBlockState(pos.above());
        if (!aboveState.getFluidState().isEmpty()) {
            return false;
        }
        return !aboveState.isFaceSturdy(level, pos.above(), Direction.DOWN);
    }

    private static List<ServerPlayer> activePlayers(ServerLevel level) {
        return level.players().stream()
                .filter(player -> !player.isSpectator())
                .toList();
    }
}
