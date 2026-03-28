package bhw.voident.xyz.terrariafabric.client;

import bhw.voident.xyz.terrariafabric.client.render.GuideRenderer;
import bhw.voident.xyz.terrariafabric.client.render.MerchantRenderer;
import bhw.voident.xyz.terrariafabric.client.render.TownNpcPlayerRenderer;
import bhw.voident.xyz.terrariafabric.entity.TerrariafabricEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.resources.ResourceLocation;

/** 类用途：客户端入口，注册 NPC 渲染和坐下系统客户端逻辑。 */
public class TerrariafabricClient implements ClientModInitializer {

    private static final ResourceLocation PLACEHOLDER_HUMAN_NPC_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(TerrariafabricEntities.GUIDE, GuideRenderer::new);
        EntityRendererRegistry.register(TerrariafabricEntities.MERCHANT, MerchantRenderer::new);
        EntityRendererRegistry.register(
                TerrariafabricEntities.NURSE,
                context -> new TownNpcPlayerRenderer<>(context, PLACEHOLDER_HUMAN_NPC_TEXTURE)
        );
        TerrariafabricSitClient.register();
    }
}
