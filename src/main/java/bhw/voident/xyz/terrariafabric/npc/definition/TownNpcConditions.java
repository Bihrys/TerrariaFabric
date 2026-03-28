package bhw.voident.xyz.terrariafabric.npc.definition;

import bhw.voident.xyz.terrariafabric.npc.state.NpcWorldState;
import bhw.voident.xyz.terrariafabric.currency.CoinCurrencySystem;
import bhw.voident.xyz.terrariafabric.player.TerrariafabricMaxHearts;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.List;
import java.util.function.Predicate;

/** 类用途：封装常见城镇 NPC 解锁条件，避免每个定义重复扫玩家和实体。 */
public final class TownNpcConditions {

    private TownNpcConditions() {
    }

    public static boolean anyActivePlayerMatches(ServerLevel level, Predicate<ServerPlayer> predicate) {
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            if (predicate.test(player)) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyActivePlayerHasAtLeastHearts(ServerLevel level, int hearts) {
        return anyActivePlayerMatches(level, player ->
                player instanceof TerrariafabricMaxHearts maxHearts
                        && maxHearts.terrariafabric$getMaxHearts() >= hearts
        );
    }

    public static int highestActivePlayerHearts(ServerLevel level) {
        int highest = 0;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            if (player instanceof TerrariafabricMaxHearts maxHearts) {
                highest = Math.max(highest, maxHearts.terrariafabric$getMaxHearts());
            }
        }
        return highest;
    }

    public static long lowestActivePlayerCopperValue(ServerLevel level) {
        long lowest = Long.MAX_VALUE;
        boolean found = false;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            found = true;
            lowest = Math.min(lowest, CoinCurrencySystem.getInventoryCopperValue(player));
        }
        return found ? lowest : 0L;
    }

    public static int activePlayerCount(ServerLevel level) {
        int count = 0;
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator()) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasUnlocked(ServerLevel level, String npcId) {
        return NpcWorldState.get(level).getOrCreate(npcId).spawnedOnce();
    }

    public static boolean hasActiveNpc(ServerLevel level, Class<? extends PathfinderMob> npcClass) {
        List<? extends PathfinderMob> matches = level.getEntities(EntityTypeTest.forClass(npcClass), entity -> true);
        return !matches.isEmpty();
    }
}
