package bhw.voident.xyz.terrariafabric.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class TerrariafabricAdvancements {

    public static final ResourceLocation HAS_A_HOME = id("terraria/housing/has_a_home");

    private static final String DEFAULT_CRITERION = "unlock";
    private static final double HOUSING_REWARD_RADIUS_SQR = 48.0D * 48.0D;

    private TerrariafabricAdvancements() {
    }

    public static void awardNearbyHousingPlayers(ServerLevel level, BlockPos center, ResourceLocation advancementId) {
        double x = center.getX() + 0.5D;
        double y = center.getY() + 0.5D;
        double z = center.getZ() + 0.5D;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            if (player.distanceToSqr(x, y, z) > HOUSING_REWARD_RADIUS_SQR) {
                continue;
            }
            award(player, advancementId);
        }
    }

    public static void award(ServerPlayer player, ResourceLocation advancementId) {
        AdvancementHolder advancement = player.serverLevel().getServer().getAdvancements().get(advancementId);
        if (advancement == null) {
            return;
        }
        player.getAdvancements().award(advancement, DEFAULT_CRITERION);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.parse("terrariafabric:" + path);
    }
}
