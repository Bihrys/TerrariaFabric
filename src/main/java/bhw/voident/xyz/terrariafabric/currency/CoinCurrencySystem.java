package bhw.voident.xyz.terrariafabric.currency;

import bhw.voident.xyz.terrariafabric.item.TerrariafabricItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public final class CoinCurrencySystem {

    public static final int COPPER_PER_SILVER = 100;
    public static final int SILVER_PER_GOLD = 100;
    public static final int GOLD_PER_PLATINUM = 100;

    public static final int COPPER_PER_GOLD = COPPER_PER_SILVER * SILVER_PER_GOLD;
    public static final int COPPER_PER_PLATINUM = COPPER_PER_GOLD * GOLD_PER_PLATINUM;

    private static final long AUTO_CONVERT_INTERVAL = 20L;
    private static final long MANUAL_SPLIT_PAUSE_TICKS = 20L * 20L;
    private static final Map<UUID, Long> AUTO_CONVERT_PAUSE_UNTIL = new HashMap<>();
    private static final Set<UUID> CONVERTING_PLAYERS = new HashSet<>();

    private CoinCurrencySystem() {
    }

    public static void register() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(CoinCurrencySystem::onKilledOtherEntity);
        ServerTickEvents.END_SERVER_TICK.register(CoinCurrencySystem::onServerTick);
    }

    public static void pauseAutoConvertForManualSplit(ServerPlayer player) {
        AUTO_CONVERT_PAUSE_UNTIL.put(player.getUUID(), player.serverLevel().getGameTime() + MANUAL_SPLIT_PAUSE_TICKS);
    }

    public static void tryAutoConvertNow(ServerPlayer player) {
        if (player == null) {
            return;
        }
        if (isAutoConvertPaused(player)) {
            return;
        }

        UUID playerId = player.getUUID();
        if (!CONVERTING_PLAYERS.add(playerId)) {
            return;
        }
        try {
            autoConvertPlayerCoins(player);
        } finally {
            CONVERTING_PLAYERS.remove(playerId);
        }
    }

    private static void onServerTick(MinecraftServer server) {
        if (server.getTickCount() % AUTO_CONVERT_INTERVAL != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tryAutoConvertNow(player);
        }
    }

    private static void onKilledOtherEntity(ServerLevel level, Entity attacker, LivingEntity killedEntity) {
        if (!(killedEntity instanceof Enemy)) {
            return;
        }
        ServerPlayer killer = resolveKillerPlayer(attacker);
        if (killer == null) {
            return;
        }

        long copper = rollCoinRewardCopper(level, killer, killedEntity);
        if (copper <= 0L) {
            return;
        }

        CoinBreakdown breakdown = CoinBreakdown.fromCopper(copper);
        spawnCoinDrops(level, killedEntity.position(), TerrariafabricItems.PLATINUM_COIN, breakdown.platinum());
        spawnCoinDrops(level, killedEntity.position(), TerrariafabricItems.GOLD_COIN, breakdown.gold());
        spawnCoinDrops(level, killedEntity.position(), TerrariafabricItems.SILVER_COIN, breakdown.silver());
        spawnCoinDrops(level, killedEntity.position(), TerrariafabricItems.COPPER_COIN, breakdown.copper());
    }

    private static ServerPlayer resolveKillerPlayer(Entity attacker) {
        if (attacker instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        if (attacker instanceof net.minecraft.world.entity.projectile.Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        if (attacker instanceof net.minecraft.world.entity.TamableAnimal tamable
                && tamable.getOwner() instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        return null;
    }

    private static long rollCoinRewardCopper(ServerLevel level, ServerPlayer killer, LivingEntity killedEntity) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return 0L;
        }

        // Terraria enemy coins are based on a base value with random multipliers.
        // Here we map that feeling to Minecraft by using target max health + difficulty + looting bonus.
        double base = Math.max(1.0, killedEntity.getMaxHealth() * 0.25D);
        if (killedEntity.getType().getCategory() == MobCategory.MONSTER && killedEntity.getMaxHealth() >= 100.0F) {
            base *= 1.6D;
        }

        double difficultyMultiplier = switch (level.getDifficulty()) {
            case EASY -> 0.85D;
            case NORMAL -> 1.00D;
            case HARD -> 1.25D;
            default -> 1.00D;
        };

        RandomSource random = level.getRandom();
        double randomMultiplier = 0.80D + random.nextDouble() * 0.95D; // 80% to 175%

        double bonusMultiplier = 1.0D;
        bonusMultiplier = rollBonusMultiplier(random, bonusMultiplier, 0.50D, 1.05D, 1.10D);
        bonusMultiplier = rollBonusMultiplier(random, bonusMultiplier, 0.25D, 1.10D, 1.20D);
        bonusMultiplier = rollBonusMultiplier(random, bonusMultiplier, 0.125D, 1.15D, 1.30D);
        bonusMultiplier = rollBonusMultiplier(random, bonusMultiplier, 0.0625D, 1.20D, 1.40D);

        long copper = Math.max(1L, Math.round(base * difficultyMultiplier * randomMultiplier * bonusMultiplier));
        return Math.min(copper, 50_000_000L);
    }

    private static double rollBonusMultiplier(RandomSource random, double current, double chance, double min, double max) {
        if (random.nextDouble() > chance) {
            return current;
        }
        return current * (min + random.nextDouble() * (max - min));
    }

    private static void spawnCoinDrops(ServerLevel level, Vec3 pos, Item item, long count) {
        if (count <= 0L) {
            return;
        }
        int maxStack = item.getDefaultMaxStackSize();
        long remaining = count;
        while (remaining > 0L) {
            int dropCount = (int) Math.min(maxStack, remaining);
            ItemEntity itemEntity = new ItemEntity(
                    level,
                    pos.x(),
                    pos.y() + 0.25D,
                    pos.z(),
                    new ItemStack(item, dropCount)
            );
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
            remaining -= dropCount;
        }
    }

    private static void autoConvertPlayerCoins(ServerPlayer player) {
        if (isAutoConvertPaused(player)) {
            return;
        }

        Inventory inventory = player.getInventory();
        long copperCount = countCoins(inventory, TerrariafabricItems.COPPER_COIN);
        long silverCount = countCoins(inventory, TerrariafabricItems.SILVER_COIN);
        long goldCount = countCoins(inventory, TerrariafabricItems.GOLD_COIN);

        if (copperCount < COPPER_PER_SILVER && silverCount < SILVER_PER_GOLD && goldCount < GOLD_PER_PLATINUM) {
            return;
        }

        long totalCopperValue = toCopperValue(
                copperCount,
                silverCount,
                goldCount,
                countCoins(inventory, TerrariafabricItems.PLATINUM_COIN)
        );

        clearCoinItems(inventory);
        CoinBreakdown breakdown = CoinBreakdown.fromCopper(totalCopperValue);

        insertCoins(player, TerrariafabricItems.PLATINUM_COIN, breakdown.platinum());
        insertCoins(player, TerrariafabricItems.GOLD_COIN, breakdown.gold());
        insertCoins(player, TerrariafabricItems.SILVER_COIN, breakdown.silver());
        insertCoins(player, TerrariafabricItems.COPPER_COIN, breakdown.copper());

        inventory.setChanged();
        player.inventoryMenu.broadcastChanges();
    }

    private static boolean isAutoConvertPaused(ServerPlayer player) {
        Long pausedUntil = AUTO_CONVERT_PAUSE_UNTIL.get(player.getUUID());
        if (pausedUntil == null) {
            return false;
        }
        long now = player.serverLevel().getGameTime();
        if (now < pausedUntil) {
            return true;
        }
        AUTO_CONVERT_PAUSE_UNTIL.remove(player.getUUID());
        return false;
    }

    private static void insertCoins(ServerPlayer player, Item item, long count) {
        if (count <= 0L) {
            return;
        }
        int maxStack = item.getDefaultMaxStackSize();
        long remaining = count;
        while (remaining > 0L) {
            int insertCount = (int) Math.min(maxStack, remaining);
            ItemStack stack = new ItemStack(item, insertCount);
            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                player.drop(stack, false);
            }
            remaining -= insertCount;
        }
    }

    private static long countCoins(Inventory inventory, Item item) {
        long total = 0L;
        for (ItemStack stack : inventory.items) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void clearCoinItems(Inventory inventory) {
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.items.get(i);
            if (isCoin(stack)) {
                inventory.items.set(i, ItemStack.EMPTY);
            }
        }
    }

    private static boolean isCoin(ItemStack stack) {
        return stack.is(TerrariafabricItems.COPPER_COIN)
                || stack.is(TerrariafabricItems.SILVER_COIN)
                || stack.is(TerrariafabricItems.GOLD_COIN)
                || stack.is(TerrariafabricItems.PLATINUM_COIN);
    }

    private static long toCopperValue(long copper, long silver, long gold, long platinum) {
        return copper
                + silver * COPPER_PER_SILVER
                + gold * COPPER_PER_GOLD
                + platinum * COPPER_PER_PLATINUM;
    }

    private record CoinBreakdown(long platinum, long gold, long silver, long copper) {
        static CoinBreakdown fromCopper(long totalCopper) {
            long platinum = totalCopper / COPPER_PER_PLATINUM;
            long remainAfterPlatinum = totalCopper % COPPER_PER_PLATINUM;

            long gold = remainAfterPlatinum / COPPER_PER_GOLD;
            long remainAfterGold = remainAfterPlatinum % COPPER_PER_GOLD;

            long silver = remainAfterGold / COPPER_PER_SILVER;
            long copper = remainAfterGold % COPPER_PER_SILVER;
            return new CoinBreakdown(platinum, gold, silver, copper);
        }
    }
}

