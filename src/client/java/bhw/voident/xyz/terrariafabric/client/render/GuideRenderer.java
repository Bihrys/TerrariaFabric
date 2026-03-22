package bhw.voident.xyz.terrariafabric.client.render;

import bhw.voident.xyz.terrariafabric.entity.GuideEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GuideRenderer extends MobRenderer<GuideEntity, PlayerModel<GuideEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("terrariafabric", "textures/entity/guide.png");

    public GuideRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(GuideEntity entity) {
        return TEXTURE;
    }
}
