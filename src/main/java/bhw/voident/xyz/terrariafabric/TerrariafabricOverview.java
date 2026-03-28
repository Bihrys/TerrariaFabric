package bhw.voident.xyz.terrariafabric;

/**
 * 模组目录与职责总览。
 *
 * <p>这个文件只保留维护索引，不承载运行逻辑；主入口仍然是 {@link Terrariafabric}。</p>
 *
 * <p>当前核心模块：</p>
 * <p>- {@code command/}：命令入口，包含 {@code /checkhouse} 和 {@code /guide}。</p>
 * <p>- {@code currency/}：泰拉币掉落、自动换币和手动拆分冷却。</p>
 * <p>- {@code entity/}：NPC 实体本体与实体注册，当前包含向导和商人。</p>
 * <p>- {@code item/}：生命水晶、泰拉币以及物品注册。</p>
 * <p>- {@code npc/definition/}：每个 NPC 自己的生成、命名和入住规则。</p>
 * <p>- {@code npc/home/}：房屋 BFS 检测、缺失条件、房屋登记与增量复检。</p>
 * <p>- {@code npc/spawn/}：NPC 入住、重生和自动房屋检测调度。</p>
 * <p>- {@code npc/state/}：NPC 世界级持久化状态。</p>
 * <p>- {@code player/}：最大心数和白天睡觉状态。</p>
 * <p>- {@code sit/}：坐下系统。{@code TerrariafabricSit} 负责注册事件，{@code SitLogic} 负责半砖和台阶坐下判定、座位复用与朝向，{@code SitSeatEntity} 是不可见座位实体，{@code SitUtil} 保存座位映射，{@code SitConfig} 读取配置。</p>
 * <p>- {@code advancement/}：自定义成就发放入口。</p>
 * <p>- {@code world/time/sleep/}：泰拉式睡觉加速，而不是直接跳过整晚。</p>
 *
 * <p>客户端目录：</p>
 * <p>- {@code client/TerrariafabricClient.java}：客户端总入口。</p>
 * <p>- {@code client/TerrariafabricSitClient.java}：坐下系统客户端入口，注册座位空渲染器并每 tick 修正骑乘姿态。</p>
 * <p>- {@code client/render/GuideRenderer.java}：向导渲染器，适配玩家皮肤格式。</p>
 * <p>- {@code client/render/MerchantRenderer.java}：商人渲染器。</p>
 * <p>- {@code client/render/SitSeatRenderer.java}：坐下座位实体的空渲染器。</p>
 *
 * <p>Mixin 边界：</p>
 * <p>- 通用行为改动放在 {@code mixin/}。</p>
 * <p>- 服务端玩家与睡觉相关补丁放在 {@code mixin/server/}。</p>
 * <p>- 客户端界面补丁放在 {@code mixin/client/}。</p>
 *
 * <p>近期补充过的内容：</p>
 * <p>- 商人 NPC、名字池、入住条件和客户端渲染。</p>
 * <p>- 房屋入住提示、自动检测倒计时和检测完成提示。</p>
 * <p>- {@code data/terrariafabric/advancements/} 下的成就资源与“有家可归”发放链路。</p>
 * <p>- 按原 SIT 源码补齐的客户端姿态更新与空座位渲染。</p>
 */
public final class TerrariafabricOverview {

    private TerrariafabricOverview() {
    }
}
