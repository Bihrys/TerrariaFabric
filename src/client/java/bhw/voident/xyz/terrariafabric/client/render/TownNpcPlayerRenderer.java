package bhw.voident.xyz.terrariafabric.client.render;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;

/** 类用途：给暂时没有专属贴图的人形城镇 NPC 提供统一占位渲染。 */
public class TownNpcPlayerRenderer<T extends PathfinderMob> extends MobRenderer<T, PlayerModel<T>> {

    private final ResourceLocation texture;

    public TownNpcPlayerRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
