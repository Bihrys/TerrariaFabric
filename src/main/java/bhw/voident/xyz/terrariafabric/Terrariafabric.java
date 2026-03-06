package bhw.voident.xyz.terrariafabric;

import bhw.voident.xyz.terrariafabric.command.HouseCommand;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Terrariafabric implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("terrariafabric");

    @Override
    public void onInitialize() {
        HouseCommand.register();
        LOGGER.info("TerrariaFabric house check command registered: /checkhouse");
    }
}
