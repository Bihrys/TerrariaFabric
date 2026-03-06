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
            source.sendFailure(Component.literal("Only players can use /checkhouse."));
            return 0;
        }

        checkHouseAndReport(player);
        return Command.SINGLE_SUCCESS;
    }

    private static void checkHouseAndReport(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos start = findStartingAir(player);

        if (start == null) {
            send(player, "No valid start block found nearby.");
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
        boolean hasWorkbench = false;
        boolean hasBed = false;
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
                if ("crafting_table".equals(idPath)) {
                    hasWorkbench = true;
                }
                if (idPath.endsWith("_bed")) {
                    hasBed = true;
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
        boolean isValid = !leaked && !tooLarge && totalAvailable >= 20 && hasDoor && hasWorkbench && hasBed && hasAnyLight;

        if (isValid) {
            send(player, "Room is valid.");
            return;
        }

        List<String> missing = new ArrayList<>();
        if (tooLarge) {
            missing.add("room too large");
        }
        if (leaked) {
            missing.add("room not enclosed");
        }
        if (totalAvailable < 20) {
            missing.add("room too small");
        }
        if (!hasDoor) {
            missing.add("missing door");
        }
        if (!hasWorkbench) {
            missing.add("missing crafting table");
        }
        if (!hasBed) {
            missing.add("missing bed");
        }
        if (!hasAnyLight) {
            missing.add("missing light source");
        }
        send(player, "Room is invalid: " + String.join(", ", missing));
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

    private static String getBlockIdPath(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
    }

    private static void send(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
