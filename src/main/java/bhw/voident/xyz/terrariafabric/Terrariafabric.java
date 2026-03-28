package bhw.voident.xyz.terrariafabric;

import bhw.voident.xyz.terrariafabric.block.TerrariafabricBlocks;
import bhw.voident.xyz.terrariafabric.command.GuideCommand;
import bhw.voident.xyz.terrariafabric.command.HouseCommand;
import bhw.voident.xyz.terrariafabric.currency.CoinCurrencySystem;
import bhw.voident.xyz.terrariafabric.entity.TerrariafabricEntities;
import bhw.voident.xyz.terrariafabric.item.TerrariafabricItems;
import bhw.voident.xyz.terrariafabric.npc.spawn.NpcSpawnScheduler;
import bhw.voident.xyz.terrariafabric.player.TerrariafabricHealth;
import bhw.voident.xyz.terrariafabric.player.TerrariafabricMaxHearts;
import bhw.voident.xyz.terrariafabric.sit.TerrariafabricSit;
import bhw.voident.xyz.terrariafabric.world.evil.WorldEvilSystem;
import bhw.voident.xyz.terrariafabric.world.time.sleep.SleepTimeAccelerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 模组主入口。详细目录说明见 {@link TerrariafabricOverview}。 */
public class Terrariafabric implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("terrariafabric");

    @Override
    public void onInitialize() {
        TerrariafabricEntities.register(); // 注册实体类型和默认属性。
        TerrariafabricBlocks.register(); // 注册邪恶方块和对应方块物品。
        TerrariafabricItems.register(); // 注册物品和创造栏入口。

        HouseCommand.register(); // 注册 /checkhouse。
        GuideCommand.register(); // 注册 /向导。
        SleepTimeAccelerator.register(); // 注册睡觉时间加速。
        NpcSpawnScheduler.register(); // 注册房屋和 NPC 调度。
        WorldEvilSystem.register(); // 注册世界主邪恶选择、初始生成和蔓延。
        CoinCurrencySystem.register(); // 注册货币掉落和自动换币。
        TerrariafabricSit.register(); // 注册坐下系统。

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> { // 重生或复制玩家时同步最大心数。
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
