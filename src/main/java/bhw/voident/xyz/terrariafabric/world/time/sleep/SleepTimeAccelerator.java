package bhw.voident.xyz.terrariafabric.world.time.sleep;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

import java.util.List;

public final class SleepTimeAccelerator {

    private static final int SPEED_MULTIPLIER = 5;
    private static final int EXTRA_TICKS_PER_TICK = SPEED_MULTIPLIER - 1;

    private SleepTimeAccelerator() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(SleepTimeAccelerator::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        if (!areAllActivePlayersSleeping(server)) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (!level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                continue;
            }
            level.setDayTime(level.getDayTime() + EXTRA_TICKS_PER_TICK);
        }
    }

    private static boolean areAllActivePlayersSleeping(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return false;
        }

        boolean hasActivePlayer = false;
        for (ServerPlayer player : players) {
            if (player.isSpectator()) {
                continue;
            }
            hasActivePlayer = true;
            if (!player.isSleeping()) {
                return false;
            }
        }

        return hasActivePlayer;
    }
}
