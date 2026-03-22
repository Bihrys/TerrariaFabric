package bhw.voident.xyz.terrariafabric.npc.definition;

import bhw.voident.xyz.terrariafabric.currency.CoinCurrencySystem;
import bhw.voident.xyz.terrariafabric.entity.MerchantEntity;
import bhw.voident.xyz.terrariafabric.entity.TerrariafabricEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;

public final class MerchantNpcDefinition implements NpcDefinition {

    private static final long REQUIRED_COPPER = 50L * CoinCurrencySystem.COPPER_PER_SILVER;
    private static final String[] FALLBACK_NAMES = new String[]{
            "Alfred",
            "Barney",
            "Calvin",
            "Edmund",
            "Edwin",
            "Eugene",
            "Frank",
            "Frederick",
            "Gilbert",
            "Gus",
            "Harold",
            "Howard",
            "Humphrey",
            "Isaac",
            "Jason Xbox One",
            "Joseph",
            "Kristian",
            "Logan Xbox One",
            "Louis",
            "Milton",
            "Mortimer",
            "Ralph",
            "Seymour",
            "Steve Xbox One",
            "Walter",
            "Wilbur"
    };

    @Override
    public String id() {
        return "merchant";
    }

    @Override
    public EntityType<? extends PathfinderMob> entityType() {
        return TerrariafabricEntities.MERCHANT;
    }

    @Override
    public Class<? extends PathfinderMob> entityClass() {
        return MerchantEntity.class;
    }

    @Override
    public String[] fallbackNames() {
        return FALLBACK_NAMES;
    }

    @Override
    public String professionSuffix() {
        return "商人";
    }

    @Override
    public boolean spawnsWithWorld() {
        return false;
    }

    @Override
    public boolean needsHousingForRespawn() {
        return true;
    }

    @Override
    public boolean canSpawnNaturally(ServerLevel level) {
        return hasEnoughMoney(level);
    }

    @Override
    public boolean canRespawn(ServerLevel level) {
        return hasEnoughMoney(level);
    }

    @Override
    public BlockPos findWorldSpawnPosition(ServerLevel level, ServerPlayer player) {
        return player.blockPosition();
    }

    private boolean hasEnoughMoney(ServerLevel level) {
        boolean hasAnyPlayer = false;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            hasAnyPlayer = true;
            if (CoinCurrencySystem.getInventoryCopperValue(player) <= REQUIRED_COPPER) {
                return false;
            }
        }
        return hasAnyPlayer;
    }
}
