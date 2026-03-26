package bhw.voident.xyz.terrariafabric;

/**
 * 模组目录与职责总览。
 *
 * <p>这个文件只保留维护索引，不承载实际运行逻辑；主入口仍然是 {@link Terrariafabric}。</p>
 *
 * <p>当前公共源码目录职责：</p>
 * <p>- {@code Terrariafabric.java}：服务端/通用主入口，负责按顺序注册实体、物品、指令、时间系统和 NPC 调度。</p>
 * <p>- {@code command/}：所有命令入口。</p>
 * <p>  - {@code GuideCommand.java}：处理 {@code /向导}，把向导绑定到玩家当前所处的合格房屋，并确保实体能真正出现。</p>
 * <p>  - {@code HouseCommand.java}：处理 {@code /checkhouse}，手动执行房屋检测并给玩家回显结果。</p>
 * <p>- {@code entity/}：自定义实体本体与实体注册。</p>
 * <p>  - {@code GuideEntity.java}：向导 NPC 本体，负责属性、基础 AI、开门、近战、防止远离卸载、死亡回调。</p>
 * <p>  - {@code TerrariafabricEntities.java}：统一注册实体类型和默认属性。</p>
 * <p>- {@code item/}：自定义物品与物品注册。</p>
 * <p>  - {@code LifeCrystalItem.java}：生命水晶逻辑，增加泰拉式最大心数并回血。</p>
 * <p>  - {@code TerrariaCoinItem.java}：泰拉货币物品逻辑，支持银币手动拆分为铜币。</p>
 * <p>  - {@code TerrariafabricItems.java}：统一注册物品并放入创造物品栏。</p>
 * <p>- {@code mixin/}：直接改原版行为的补丁层。</p>
 * <p>  - {@code ContainerMaxStackSizeMixin.java}：容器整体堆叠上限改到 9999。</p>
 * <p>  - {@code HousingBlockItemMixin.java}：放置与住房相关的方块后，把附近房屋标记为待复检。</p>
 * <p>  - {@code InventoryAutoStackMixin.java}：玩家背包从 NBT 读取后自动合并同类堆叠，避免 990 被拆成 10 组 99。</p>
 * <p>  - {@code ItemStackCountCodecMixin.java}：放宽 ItemStack 数量编解码上限，避免保存/同步阶段被 99 卡死。</p>
 * <p>  - {@code ItemStackMaxStackSizeMixin.java}：把原版物品堆叠上限统一抬到 9999。</p>
 * <p>  - {@code ItemStackNoDurabilityMixin.java}：让可损耗工具不再消耗耐久。</p>
 * <p>  - {@code SlotMaxStackSizeMixin.java}：槽位层面的堆叠上限同步改到 9999。</p>
 * <p>  - {@code mixin/server/PlayerMaxHeartsMixin.java}：把玩家最大心数挂到玩家 NBT 和属性上。</p>
 * <p>  - {@code mixin/server/PlayerSleepMixin.java}：允许白天睡觉并维持睡眠状态。</p>
 * <p>  - {@code mixin/server/ServerPlayerGameModeHousingMixin.java}：破坏住房相关方块后标记脏房。</p>
 * <p>  - {@code mixin/server/ServerPlayerSleepMixin.java}：服务端玩家层的白天睡觉补丁。</p>
 * <p>  - {@code mixin/server/SleepStatusMixin.java}：禁用原版“一觉直接跳天亮”的判定，改由自定义加速逻辑接管。</p>
 * <p>- {@code npc/}：NPC 通用系统。</p>
 * <p>  - {@code NpcNames.java}：从数据包读取 NPC 名字池，未来所有城镇 NPC 共用这一入口。</p>
 * <p>  - {@code npc/definition/}：每个 NPC 自己的规则定义层。</p>
 * <p>    - {@code NpcDefinition.java}：NPC 定义接口，约束名字、世界初始生成、重生、住房使用条件等。</p>
 * <p>    - {@code GuideNpcDefinition.java}：向导专属定义。</p>
 * <p>    - {@code NpcDefinitions.java}：所有 NPC 定义的统一索引表。</p>
 * <p>  - {@code npc/home/}：住房系统，保持“只管房子，不管具体 NPC 规则”。</p>
 * <p>    - {@code HouseCheckResult.java}：房屋检测结果。</p>
 * <p>    - {@code HouseDetector.java}：BFS 房屋检测核心；这里只做结构判定和房间边界计算，不写 NPC 特判。</p>
 * <p>    - {@code HouseMessages.java}：房屋缺失提示文本拼装。</p>
 * <p>    - {@code HouseMissing.java}：房屋缺失项枚举。</p>
 * <p>    - {@code HouseRoom.java}：房间边界值对象。</p>
 * <p>    - {@code HousingDirtyQueue.java}：脏房锚点队列，避免每 tick 全图扫描。</p>
 * <p>    - {@code HousingRegistry.java}：房屋注册表与房间占用持久化。</p>
 * <p>    - {@code HousingRelevantBlocks.java}：定义哪些方块变化会触发房屋复检。</p>
 * <p>  - {@code npc/spawn/}：NPC 居住和刷新调度层。</p>
 * <p>    - {@code NpcResidenceManager.java}：负责入住、搬家、维持房间内位置、死亡后待重生。</p>
 * <p>    - {@code NpcSpawnScheduler.java}：事件驱动 + 低频兜底的调度器，不做高频全图轮询。</p>
 * <p>  - {@code npc/state/NpcWorldState.java}：NPC 世界级 SavedData，记录是否已经生成过、当前实体 UUID、是否待重生。</p>
 * <p>  - {@code npc/ai/}、{@code npc/dialog/}、{@code npc/shop/}：当前为空，预留给以后补泰拉 NPC AI、对话、商店系统。</p>
 * <p>- {@code player/}：玩家泰拉化数据。</p>
 * <p>  - {@code TerrariafabricHealth.java}：生命值常量，定义 5 心起步、20 心上限、每心 2 点血。</p>
 * <p>  - {@code TerrariafabricMaxHearts.java}：最大心数访问接口，供物品、Mixin、事件系统共用。</p>
 * <p>  - {@code player/sleep/DaySleepFlag.java}：白天睡觉状态标记接口。</p>
 * <p>- {@code currency/CoinCurrencySystem.java}：泰拉货币系统，负责敌怪掉币、自动换币与手动拆分冷却。</p>
 * <p>- {@code world/time/sleep/SleepTimeAccelerator.java}：泰拉式睡觉加速时间，不直接跳过整晚。</p>
 *
 * <p>当前已创建但暂时还没放实现的包，都是后续扩展位：</p>
 * <p>- {@code attribute/}：玩家或实体的泰拉属性系统。</p>
 * <p>- {@code boss/}、{@code boss/ai/}、{@code boss/progress/}：Boss 本体、阶段 AI、进度解锁。</p>
 * <p>- {@code combat/}、{@code combat/effect/}：伤害、击退、暴击、Buff/Debuff。</p>
 * <p>- {@code config/}：模组配置。</p>
 * <p>- {@code crafting/}：工作站与配方体系。</p>
 * <p>- {@code data/}、{@code save/}：世界/玩家自定义数据与持久化封装。</p>
 * <p>- {@code init/}、{@code registry/}：以后如果注册项变多，可把 register 分发逻辑收口到这里。</p>
 * <p>- {@code inventory/}：额外背包或钱币槽、弹药槽之类的泰拉背包系统。</p>
 * <p>- {@code loot/}：掉落表与额外掉落规则。</p>
 * <p>- {@code network/}：多人同步包。</p>
 * <p>- {@code util/}：通用工具。</p>
 * <p>- {@code world/biome/}、{@code world/event/}、{@code world/gen/}、{@code world/time/}：世界机制、生物群系、事件和时间系统扩展。</p>
 *
 * <p>客户端源码目录职责（位于 {@code src/client/java}）：</p>
 * <p>- {@code client/TerrariafabricClient.java}：客户端入口，注册实体渲染。</p>
 * <p>- {@code client/TerrariafabricDataGenerator.java}：数据生成入口，目前只创建基础数据包。</p>
 * <p>- {@code client/render/GuideRenderer.java}：向导渲染器。</p>
 * <p>- {@code client/sound/}、{@code client/ui/}：当前为空，预留给泰拉 HUD、音乐、环境音。</p>
 * <p>- {@code mixin/client/CreativeModeInventoryScreenMixin.java}：补创造模式堆叠、Shift 合并、中键克隆到 9999 的逻辑。</p>
 *
 * <p>资源目录职责：</p>
 * <p>- {@code fabric.mod.json}：模组元数据与入口声明。</p>
 * <p>- {@code terrariafabric.mixins.json}：服务端/通用 Mixin 配置。</p>
 * <p>- {@code src/client/resources/terrariafabric.client.mixins.json}：客户端 Mixin 配置。</p>
 * <p>- {@code assets/terrariafabric/lang/*.json}：中英文文本。</p>
 * <p>- {@code assets/terrariafabric/models/item/life_crystal.json}：生命水晶模型。</p>
 * <p>- {@code assets/terrariafabric/textures/item/life_crystal.png}：生命水晶贴图。</p>
 * <p>- {@code assets/terrariafabric/textures/entity/guide.png}：向导贴图。</p>
 * <p>- {@code data/terrariafabric/npc/names.json}：所有 NPC 的随机名字池。</p>
 *
 * <p>维护边界：</p>
 * <p>- 房屋 BFS 算法只放在 {@code HouseDetector}，不要把“谁能住”“什么时候重生”写进去。</p>
 * <p>- NPC 个体差异只放在 {@code NpcDefinition} 实现类里。</p>
 * <p>- 房间占用与房屋持久化只放在 {@code HousingRegistry}。</p>
 * <p>- NPC 是否已生成、是否待重生只放在 {@code NpcWorldState}。</p>
 * <p>- 原版行为修改走 Mixin；纯业务逻辑优先写在普通类里，避免补丁类越来越重。</p>
 * <p>近期新增补充：</p>
 * <p>- {@code advancement/TerrariafabricAdvancements.java}：成就发放入口（如有家可归）。</p>
 * <p>- {@code data/terrariafabric/advancements/}：成就数据资源。</p>
 * <p>- {@code entity/MerchantEntity.java}：商人 NPC 实体与基础行为。</p>
 * <p>- {@code npc/definition/MerchantNpcDefinition.java}：商人出现条件与名字池。</p>
 * <p>- {@code sit/}：坐下系统入口与逻辑（楼梯/半砖）。</p>
 * <p>- {@code client/render/MerchantRenderer.java}：商人渲染器。</p>
 */
public final class TerrariafabricOverview {

    private TerrariafabricOverview() {
    }
}
