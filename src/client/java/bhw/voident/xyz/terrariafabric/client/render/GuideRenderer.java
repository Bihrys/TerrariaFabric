package bhw.voident.xyz.terrariafabric.client.render;

import bhw.voident.xyz.terrariafabric.entity.GuideEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public class GuideRenderer extends MobRenderer<GuideEntity, VillagerModel<GuideEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("terrariafabric", "textures/entity/guide.png");

    public GuideRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(GuideEntity entity) {
        return TEXTURE;
    }
}
