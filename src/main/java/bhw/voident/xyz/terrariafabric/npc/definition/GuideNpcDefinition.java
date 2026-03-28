package bhw.voident.xyz.terrariafabric.npc.definition;

import bhw.voident.xyz.terrariafabric.entity.GuideEntity;
import bhw.voident.xyz.terrariafabric.entity.TerrariafabricEntities;
import bhw.voident.xyz.terrariafabric.npc.spawn.TownNpcSpawnLocations;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;

import java.util.List;

public final class GuideNpcDefinition implements NpcDefinition {

    private static final int WORLD_SPAWN_RADIUS = 8;
    private static final String[] FALLBACK_NAMES = new String[]{
            "Andrew", "Asher", "Bradley", "Brandon", "Brett", "Brian", "Cody", "Cole",
            "Colin", "Connor", "Daniel", "Dylan", "Garrett", "Harley", "Jack", "Jacob",
            "Jake", "Jan", "Jeff", "Jeffrey", "Joe", "Kevin", "Kyle", "Levi", "Logan",
            "Luke", "Marty", "Maxwell", "Ryan", "Scott", "Seth", "Steve", "Tanner",
            "Trent", "Wyatt", "Zach"
    };

    @Override
    public String id() {
        return "guide";
    }

    @Override
    public EntityType<? extends PathfinderMob> entityType() {
        return TerrariafabricEntities.GUIDE;
    }

    @Override
    public Class<? extends PathfinderMob> entityClass() {
        return GuideEntity.class;
    }

    @Override
    public String[] fallbackNames() {
        return FALLBACK_NAMES;
    }

    @Override
    public String professionSuffix() {
        return "向导";
    }

    @Override
    public boolean spawnsWithWorld() {
        return true;
    }

    @Override
    public boolean needsHousingForRespawn() {
        return true;
    }

    @Override
    public BlockPos findWorldSpawnPosition(ServerLevel level, ServerPlayer player) {
        return TownNpcSpawnLocations.findArrivalSpawnPosition(level, player, WORLD_SPAWN_RADIUS, 16);
    }

    @Override
    public void appendSpawnDiagnostics(ServerLevel level, List<Component> lines) {
        lines.add(Component.literal("向导属于世界初始城镇 NPC，没有额外解锁条件。"));
    }
}
