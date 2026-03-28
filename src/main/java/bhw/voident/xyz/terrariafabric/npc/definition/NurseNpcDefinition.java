package bhw.voident.xyz.terrariafabric.npc.definition;

import bhw.voident.xyz.terrariafabric.entity.MerchantEntity;
import bhw.voident.xyz.terrariafabric.entity.NurseEntity;
import bhw.voident.xyz.terrariafabric.entity.TerrariafabricEntities;
import bhw.voident.xyz.terrariafabric.npc.spawn.TownNpcSpawnLocations;
import bhw.voident.xyz.terrariafabric.player.TerrariafabricHealth;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;

import java.util.List;

/** 类用途：护士 NPC 定义，按 Terraria 的基础入住条件接入系统。 */
public final class NurseNpcDefinition implements NpcDefinition {

    private static final int WORLD_SPAWN_RADIUS = 8;
    private static final int REQUIRED_HEARTS = TerrariafabricHealth.DEFAULT_HEARTS + 1;
    private static final String[] FALLBACK_NAMES = new String[]{
            "Abigail",
            "Allison",
            "Amy",
            "Caitlin",
            "Carly",
            "Claire",
            "Emily",
            "Emma",
            "Hannah",
            "Heather",
            "Helen",
            "Holly",
            "Jenna",
            "Kaitlin",
            "Kaitlyn",
            "Katelyn",
            "Katherine",
            "Kathryn",
            "Katie",
            "Kayla",
            "Lisa",
            "Lorraine",
            "Madeline",
            "Molly"
    };

    @Override
    public String id() {
        return "nurse";
    }

    @Override
    public EntityType<? extends PathfinderMob> entityType() {
        return TerrariafabricEntities.NURSE;
    }

    @Override
    public Class<? extends PathfinderMob> entityClass() {
        return NurseEntity.class;
    }

    @Override
    public String[] fallbackNames() {
        return FALLBACK_NAMES;
    }

    @Override
    public String professionSuffix() {
        return "护士";
    }

    @Override
    public boolean spawnsWithWorld() {
        return false;
    }

    @Override
    public boolean needsHousingForRespawn() {
        return true;
    }

    @Override
    public boolean canSpawnNaturally(ServerLevel level) {
        return hasUnlockConditions(level);
    }

    @Override
    public boolean canRespawn(ServerLevel level) {
        return TownNpcConditions.hasUnlocked(level, id()) || hasUnlockConditions(level);
    }

    @Override
    public void appendSpawnDiagnostics(ServerLevel level, List<Component> lines) {
        boolean merchantActive = TownNpcConditions.hasActiveNpc(level, MerchantEntity.class);
        int highestHearts = TownNpcConditions.highestActivePlayerHearts(level);
        lines.add(Component.literal("商人已存在: " + (merchantActive ? "是" : "否")));
        lines.add(Component.literal("玩家最高心数: " + highestHearts + " / 需要 >= " + REQUIRED_HEARTS));
        lines.add(Component.literal("护士解锁条件: " + (hasUnlockConditions(level) ? "满足" : "不满足")));
    }

    @Override
    public BlockPos findWorldSpawnPosition(ServerLevel level, ServerPlayer player) {
        return TownNpcSpawnLocations.findArrivalSpawnPosition(level, player, WORLD_SPAWN_RADIUS, 16);
    }

    private boolean hasUnlockConditions(ServerLevel level) {
        return TownNpcConditions.hasActiveNpc(level, MerchantEntity.class)
                && TownNpcConditions.anyActivePlayerHasAtLeastHearts(level, REQUIRED_HEARTS);
    }
}
