package bhw.voident.xyz.terrariafabric.command;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;

public final class HouseCommand {

    private static final int MAX_BLOCKS = 500;
    private static final int MAX_RADIUS = 12;

    private HouseCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("checkhouse")
                        .executes(context -> execute(context.getSource())))
        );
    }

    private static int execute(CommandSourceStack source) {
        Entity entity = source.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("只有玩家可以使用 /checkhouse。"));
            return 0;
        }

        checkHouseAndReport(player);
        return Command.SINGLE_SUCCESS;
    }

    private static void checkHouseAndReport(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos start = findStartingAir(player);

        if (start == null) {
            send(player, "这不是有效的房屋。");
            return;
        }

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
        boolean structuralInvalid = leaked || tooLarge || totalAvailable < 20;
        if (structuralInvalid) {
            send(player, "这不是有效的房屋。");
            return;
        }

        List<String> missing = new ArrayList<>();
        if (!hasDoor) {
            missing.add("门");
        }
        if (!hasTable) {
            missing.add("桌子");
        }
        if (!hasChair) {
            missing.add("床");
        }
        if (!hasAnyLight) {
            missing.add("光源");
        }

        if (missing.isEmpty()) {
            send(player, "此房屋很适合。");
            return;
        }

        send(player, buildMissingMessage(missing));
    }

    private static BlockPos findStartingAir(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos base = player.blockPosition();

        BlockPos body1 = base;
        BlockPos body2 = base.above();
        if (isCandidateStart(world.getBlockState(body1))) {
            return body1;
        }
        if (isCandidateStart(world.getBlockState(body2))) {
            return body2;
        }

        BlockPos down = base.below();
        if (isCandidateStart(world.getBlockState(down))) {
            return down;
        }

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos nearby = base.offset(x, y, z);
                    if (isCandidateStart(world.getBlockState(nearby))) {
                        return nearby;
                    }
                }
            }
        }

        return null;
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

    private static String buildMissingMessage(List<String> missing) {
        return switch (missing.size()) {
            case 1 -> "这个房屋缺少" + missing.get(0) + "。";
            case 2 -> "这个房屋缺少" + missing.get(0) + "和" + missing.get(1) + "。";
            case 3 -> "这个房屋缺少" + missing.get(0) + "、" + missing.get(1) + "和" + missing.get(2) + "。";
            case 4 -> "这个房屋缺少" + missing.get(0) + "、" + missing.get(1) + "、" + missing.get(2) + "和" + missing.get(3) + "。";
            default -> "这不是有效的房屋。";
        };
    }

    private static String getBlockIdPath(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
    }

    private static void send(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
