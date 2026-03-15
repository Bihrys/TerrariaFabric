package bhw.voident.xyz.terrariafabric.client;

import bhw.voident.xyz.terrariafabric.client.render.GuideRenderer;
import bhw.voident.xyz.terrariafabric.entity.TerrariafabricEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class TerrariafabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(TerrariafabricEntities.GUIDE, GuideRenderer::new);
    }
}
