package bhw.voident.xyz.terrariafabric;

import bhw.voident.xyz.terrariafabric.command.GuideCommand;
import bhw.voident.xyz.terrariafabric.command.HouseCommand;
import bhw.voident.xyz.terrariafabric.entity.TerrariafabricEntities;
import bhw.voident.xyz.terrariafabric.item.TerrariafabricItems;
import bhw.voident.xyz.terrariafabric.npc.home.HouseAutoDetector;
import bhw.voident.xyz.terrariafabric.npc.spawn.GuideSpawner;
import bhw.voident.xyz.terrariafabric.player.TerrariafabricHealth;
import bhw.voident.xyz.terrariafabric.player.TerrariafabricMaxHearts;
import bhw.voident.xyz.terrariafabric.world.time.sleep.SleepTimeAccelerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Terrariafabric implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("terrariafabric");

    @Override
    public void onInitialize() {
        TerrariafabricEntities.register();
        TerrariafabricItems.register();
        HouseCommand.register();
        GuideCommand.register();
        HouseAutoDetector.register();
        SleepTimeAccelerator.register();
        GuideSpawner.register();
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!(oldPlayer instanceof TerrariafabricMaxHearts oldHearts)) {
                return;
            }
            if (!(newPlayer instanceof TerrariafabricMaxHearts newHearts)) {
                return;
            }
            int hearts = oldHearts.terrariafabric$getMaxHearts();
            if (hearts < TerrariafabricHealth.DEFAULT_HEARTS) {
                hearts = TerrariafabricHealth.DEFAULT_HEARTS;
            }
            newHearts.terrariafabric$setMaxHearts(hearts);
        });
        LOGGER.info("TerrariaFabric 房屋检测指令已注册：/checkhouse");
    }
}