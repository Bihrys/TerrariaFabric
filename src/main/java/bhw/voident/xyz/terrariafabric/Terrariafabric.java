package bhw.voident.xyz.terrariafabric;

import bhw.voident.xyz.terrariafabric.command.HouseCommand;
import bhw.voident.xyz.terrariafabric.world.time.sleep.SleepTimeAccelerator;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Terrariafabric implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("terrariafabric");

    /*
     * 包结构说明（服务端） / Package map (server-side)
     * command - 指令 / commands
     * config - 配置 / mod configuration
     * init - 统一注册（方块/物品/实体/声音/粒子/标签/网络） / centralized registrations
     * registry - 注册键/工具/常量 / registry keys/helpers/constants
     * util - 工具类 / shared utilities
     *
     * npc - NPC 总系统 / core NPC system
     * npc/ai - NPC AI / NPC AI and behavior
     * npc/home - 房屋检测与分配 / housing detection/assignment
     * npc/shop - 商店库存与价格 / shop inventory and pricing
     * npc/dialog - 对话与解锁条件 / dialogs and unlock conditions
     * npc/spawn - NPC 入住与刷新规则 / NPC spawn/arrival rules
     *
     * combat - 战斗规则与计算 / combat rules and math
     * combat/effect - 增益/减益 / buffs/debuffs
     * attribute - 属性系统 / player/entity stats
     * loot - 掉落系统 / loot tables and drop logic
     *
     * item - 物品基础 / base item logic
     * item/weapon - 武器行为 / weapon behavior
     * item/accessory - 饰品效果 / accessory effects
     * item/consumable - 消耗品 / consumables
     * inventory - 扩展背包 / extra inventory systems
     * crafting - 工作站与配方 / crafting stations and recipes
     *
     * world - 世界系统 / world systems
     * world/gen - 世界生成 / world generation
     * world/biome - 生物群系与扩散 / biome state and spread
     * world/event - 世界事件 / world events
     * world/time - 时间规则 / time rules
     * world/time/sleep - 睡觉加速时间 / sleep time acceleration
     *
     * entity - 自定义实体 / custom entities
     * entity/boss - Boss 实体 / boss entities
     * entity/mob - 小怪实体 / mob entities
     * entity/projectile - 投射物 / projectiles
     *
     * boss - Boss 进度与状态 / boss progression/state
     * boss/ai - Boss AI 与阶段 / boss AI and phases
     * boss/progress - Boss 解锁与击杀记录 / boss unlock/kill tracking
     *
     * network - 数据包 / server/client packets
     * data - 服务端权威数据 / server authoritative data
     * save - 持久化存储 / persistent data storage
     * player - 玩家系统 / player-specific systems
     * player/sleep - 玩家睡眠状态 / player sleep state
     * mixin - Mixin 类 / mixin classes
     * mixin/server - 服务端 Mixin / server-only mixins
     *
     * 客户端目录（src/client/java/.../client） / Client-only packages:
     * client/ui, client/render, client/sound
     *
     * 资源目录（src/main/resources） / Resources:
     * assets/terrariafabric, data/terrariafabric
     */
    @Override
    public void onInitialize() {
        HouseCommand.register();
        SleepTimeAccelerator.register();
        LOGGER.info("TerrariaFabric house check command registered: /checkhouse");
    }
}
