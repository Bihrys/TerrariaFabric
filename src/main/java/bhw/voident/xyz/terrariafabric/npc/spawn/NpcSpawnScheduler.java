package bhw.voident.xyz.terrariafabric.npc.spawn;

import bhw.voident.xyz.terrariafabric.npc.home.HousingDirtyQueue;
import bhw.voident.xyz.terrariafabric.npc.home.HousingRegistry;
import bhw.voident.xyz.terrariafabric.npc.home.HousingRelevantBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.IdentityHashMap;
import java.util.Map;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public final class NpcSpawnScheduler {

    private static final long NPC_TICK_INTERVAL = 100L;
    private static final long FALLBACK_SCAN_INTERVAL = 1200L;
    private static final int DIRTY_PROCESS_BUDGET = 6;
    private static final int FALLBACK_SCAN_RADIUS = 10;
    private static final int FALLBACK_SCAN_LIMIT = 32;

    private static final Map<ServerLevel, WorldRuntime> RUNTIMES = new IdentityHashMap<>();

    private NpcSpawnScheduler() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(NpcSpawnScheduler::tickWorld);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                server.execute(() -> onPlayerJoin(handler.player)));
    }

    public static void markHousingDirty(ServerLevel level, net.minecraft.core.BlockPos pos) {
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return;
        }
        runtime(level).dirtyQueue.markAround(pos);
    }

    private static void onPlayerJoin(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return;
        }

        WorldRuntime runtime = runtime(level);
        runtime.wasDay = level.isDay();
        HousingRelevantBlocks.enqueueNearbyAnchors(
                level,
                player.blockPosition(),
                FALLBACK_SCAN_RADIUS,
                FALLBACK_SCAN_LIMIT,
                runtime.dirtyQueue
        );
        NpcResidenceManager.syncTrackedEntities(level);
        NpcResidenceManager.tryWorldSpawn(level, player);
        NpcResidenceManager.tick(level);
    }

    private static void tickWorld(ServerLevel level) {
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return;
        }

        WorldRuntime runtime = runtime(level);
        boolean isDay = level.isDay();
        if (isDay && !runtime.wasDay) {
            NpcResidenceManager.tick(level);
        }
        runtime.wasDay = isDay;

        HousingRegistry registry = HousingRegistry.get(level);
        runtime.dirtyQueue.process(level, registry, DIRTY_PROCESS_BUDGET);

        if (level.getGameTime() % FALLBACK_SCAN_INTERVAL == 0) {
            for (ServerPlayer player : level.players()) {
                HousingRelevantBlocks.enqueueNearbyAnchors(
                        level,
                        player.blockPosition(),
                        FALLBACK_SCAN_RADIUS,
                        FALLBACK_SCAN_LIMIT,
                        runtime.dirtyQueue
                );
            }
        }

        if (level.getGameTime() % NPC_TICK_INTERVAL == 0) {
            NpcResidenceManager.tick(level);
        }
    }

    private static WorldRuntime runtime(ServerLevel level) {
        return RUNTIMES.computeIfAbsent(level, ignored -> new WorldRuntime(level.isDay()));
    }

    private static final class WorldRuntime {
        private final HousingDirtyQueue dirtyQueue = new HousingDirtyQueue();
        private boolean wasDay;

        private WorldRuntime(boolean wasDay) {
            this.wasDay = wasDay;
        }
    }
}

