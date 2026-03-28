package bhw.voident.xyz.terrariafabric.client;

import bhw.voident.xyz.terrariafabric.client.render.SitSeatRenderer;
import bhw.voident.xyz.terrariafabric.sit.SitLogic;
import bhw.voident.xyz.terrariafabric.sit.TerrariafabricSit;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.world.entity.player.Player;

/** 类用途：坐下系统的客户端逻辑，负责座位空渲染器和坐姿朝向更新。 */
public final class TerrariafabricSitClient {

    private TerrariafabricSitClient() {
    }

    public static void register() {
        EntityRendererRegistry.register(TerrariafabricSit.SIT_ENTITY_TYPE, SitSeatRenderer::new);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                return;
            }

            for (Player player : client.level.players()) {
                SitLogic.updateRiderPose(player);
            }
        });
    }
}
