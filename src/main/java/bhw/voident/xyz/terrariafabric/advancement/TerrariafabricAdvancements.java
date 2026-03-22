package bhw.voident.xyz.terrariafabric.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class TerrariafabricAdvancements {

    public static final ResourceLocation ROOT = id("terraria/root");
    public static final ResourceLocation HAS_A_HOME = id("terraria/housing/has_a_home");

    private static final String ROOT_CRITERION = "tick";
    private static final String DEFAULT_CRITERION = "unlock";

    private TerrariafabricAdvancements() {
    }

    public static void awardHasAHome(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            award(player, ROOT, ROOT_CRITERION);
            award(player, HAS_A_HOME, DEFAULT_CRITERION);
        }
    }

    public static void award(ServerPlayer player, ResourceLocation advancementId) {
        award(player, advancementId, DEFAULT_CRITERION);
    }

    public static void award(ServerPlayer player, ResourceLocation advancementId, String criterion) {
        AdvancementHolder advancement = player.serverLevel().getServer().getAdvancements().get(advancementId);
        if (advancement == null) {
            return;
        }
        player.getAdvancements().award(advancement, criterion);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.parse("terrariafabric:" + path);
    }
}
