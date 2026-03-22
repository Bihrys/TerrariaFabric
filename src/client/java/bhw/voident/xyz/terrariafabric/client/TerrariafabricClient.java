package bhw.voident.xyz.terrariafabric.client;

import bhw.voident.xyz.terrariafabric.client.render.GuideRenderer;
import bhw.voident.xyz.terrariafabric.client.render.MerchantRenderer;
import bhw.voident.xyz.terrariafabric.entity.TerrariafabricEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public class TerrariafabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(TerrariafabricEntities.GUIDE, GuideRenderer::new);
        EntityRendererRegistry.register(TerrariafabricEntities.MERCHANT, MerchantRenderer::new);
    }
}
