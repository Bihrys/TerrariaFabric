package bhw.voident.xyz.terrariafabric.client.render;

import bhw.voident.xyz.terrariafabric.sit.SitSeatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.culling.Frustum;

/** 类用途：坐下座位实体的空渲染器，避免客户端渲染崩溃。 */
public class SitSeatRenderer extends EntityRenderer<SitSeatEntity> {

    public SitSeatRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SitSeatEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // intentionally empty: invisible seat
    }

    @Override
    public boolean shouldRender(SitSeatEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(SitSeatEntity entity) {
        return null;
    }
}
