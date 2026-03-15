package bhw.voident.xyz.terrariafabric.npc.home;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;

public final class HouseDetector {

    private static final int MAX_BLOCKS = 500;
    private static final int MAX_RADIUS = 12;
    private static final int MIN_SPACE = 20;

    private HouseDetector() {
    }

    public static HouseCheckResult check(ServerPlayer player) {
        BlockPos start = findStartingAir(player.serverLevel(), player.blockPosition());
        if (start == null) {
            return HouseCheckResult.noStart();
        }
        return checkFromStart(player.serverLevel(), start);
    }

    public static HouseCheckResult check(ServerLevel level, BlockPos base) {
        BlockPos start = findStartingAir(level, base);
        if (start == null) {
            return HouseCheckResult.noStart();
        }
        return checkFromStart(level, start);
    }

    public static BlockPos findStartingAir(ServerLevel level, BlockPos base) {
        BlockPos body1 = base;
        BlockPos body2 = base.above();
        if (isCandidateStart(level.getBlockState(body1))) {
            return body1;
        }
        if (isCandidateStart(level.getBlockState(body2))) {
            return body2;
        }

        BlockPos down = base.below();
        if (isCandidateStart(level.getBlockState(down))) {
            return down;
        }

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos nearby = base.offset(x, y, z);
                    if (isCandidateStart(level.getBlockState(nearby))) {
                        return nearby;
                    }
                }
            }
        }

        return null;
    }

    public static BlockPos findSpawnPositionInRoom(ServerLevel level, BlockPos min, BlockPos max) {
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).isAir()) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private static HouseCheckResult checkFromStart(ServerLevel world, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);

        int airBlocks = 0;
        int nonFullLightBlocks = 0;
        int carpetBlocks = 0;
        int passThroughOther = 0;
        boolean hasDoor = false;
        boolean hasTable = false;
        boolean hasChair = false;
        boolean hasAnyLight = false;
        boolean leaked = false;
        boolean tooLarge = false;

        BlockPos origin = start;
        int maxRadiusSq = MAX_RADIUS * MAX_RADIUS;
        int[][] directions = new int[][]{
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };

        int minX = start.getX();
        int minY = start.getY();
        int minZ = start.getZ();
        int maxX = start.getX();
        int maxY = start.getY();
        int maxZ = start.getZ();

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            if (visited.size() > MAX_BLOCKS) {
                tooLarge = true;
                break;
            }

            int dx = current.getX() - origin.getX();
            int dy = current.getY() - origin.getY();
            int dz = current.getZ() - origin.getZ();
            if (dx * dx + dy * dy + dz * dz > maxRadiusSq) {
                leaked = true;
                break;
            }

            BlockState currentState = world.getBlockState(current);
            if (currentState.isAir()) {
                airBlocks++;
            } else if (isNonFullLightSource(currentState)) {
                nonFullLightBlocks++;
                hasAnyLight = true;
            } else if (isCarpet(currentState)) {
                carpetBlocks++;
            } else if (isPassThrough(currentState)) {
                passThroughOther++;
            }

            minX = Math.min(minX, current.getX());
            minY = Math.min(minY, current.getY());
            minZ = Math.min(minZ, current.getZ());
            maxX = Math.max(maxX, current.getX());
            maxY = Math.max(maxY, current.getY());
            maxZ = Math.max(maxZ, current.getZ());

            for (int[] dir : directions) {
                BlockPos neighbor = current.offset(dir[0], dir[1], dir[2]);
                BlockState neighborState = world.getBlockState(neighbor);
                String idPath = getBlockIdPath(neighborState);

                if (idPath.endsWith("_door")) {
                    hasDoor = true;
                }
                if (isTable(idPath)) {
                    hasTable = true;
                }
                if (isChair(idPath)) {
                    hasChair = true;
                }
                if (isLightSource(neighborState)) {
                    hasAnyLight = true;
                }

                if (isCandidateStart(neighborState) && visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        int totalAvailable = airBlocks + nonFullLightBlocks + carpetBlocks + passThroughOther;
        boolean structuralInvalid = leaked || tooLarge || totalAvailable < MIN_SPACE;
        if (structuralInvalid) {
            return HouseCheckResult.invalid();
        }

        EnumSet<HouseMissing> missing = EnumSet.noneOf(HouseMissing.class);
        if (!hasDoor) {
            missing.add(HouseMissing.DOOR);
        }
        if (!hasTable) {
            missing.add(HouseMissing.TABLE);
        }
        if (!hasChair) {
            missing.add(HouseMissing.CHAIR);
        }
        if (!hasAnyLight) {
            missing.add(HouseMissing.LIGHT);
        }

        HouseRoom room = new HouseRoom(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
        return HouseCheckResult.withRoom(true, new ArrayList<>(missing), room);
    }

    private static boolean isCandidateStart(BlockState state) {
        return state.isAir() || isNonFullLightSource(state) || isCarpet(state) || isPassThrough(state);
    }

    private static boolean isNonFullLightSource(BlockState state) {
        String name = getBlockIdPath(state);
        return name.contains("torch") || name.contains("candle");
    }

    private static boolean isCarpet(BlockState state) {
        return getBlockIdPath(state).contains("carpet");
    }

    private static boolean isPassThrough(BlockState state) {
        String name = getBlockIdPath(state);
        return name.contains("button")
                || name.contains("pressure_plate")
                || name.contains("flower")
                || name.contains("mushroom")
                || name.contains("lever")
                || name.contains("sapling")
                || name.contains("trapdoor")
                || name.contains("sign")
                || name.contains("bell")
                || name.contains("banner")
                || name.contains("lily_pad");
    }

    private static boolean isLightSource(BlockState state) {
        String name = getBlockIdPath(state);
        return name.contains("torch")
                || name.contains("lantern")
                || name.contains("candle")
                || name.contains("sea_lantern")
                || name.contains("glowstone")
                || name.contains("shroomlight");
    }

    private static boolean isTable(String idPath) {
        return "crafting_table".equals(idPath) || idPath.endsWith("_table") || idPath.contains("workbench");
    }

    private static boolean isChair(String idPath) {
        return idPath.endsWith("_bed")
                || idPath.contains("chair")
                || idPath.contains("stool")
                || idPath.contains("seat")
                || idPath.contains("sofa")
                || idPath.contains("couch")
                || idPath.contains("bench");
    }

    private static String getBlockIdPath(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
    }
}
