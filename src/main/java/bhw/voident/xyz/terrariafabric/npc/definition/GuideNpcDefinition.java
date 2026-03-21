package bhw.voident.xyz.terrariafabric.npc.definition;

import bhw.voident.xyz.terrariafabric.entity.GuideEntity;
import bhw.voident.xyz.terrariafabric.entity.TerrariafabricEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.levelgen.Heightmap;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

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
    public boolean spawnsWithWorld() {
        return true;
    }

    @Override
    public boolean needsHousingForRespawn() {
        return true;
    }

    @Override
    public BlockPos findWorldSpawnPosition(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        BlockPos base = player.blockPosition();
        for (int i = 0; i < 16; i++) {
            int dx = random.nextInt(WORLD_SPAWN_RADIUS * 2 + 1) - WORLD_SPAWN_RADIUS;
            int dz = random.nextInt(WORLD_SPAWN_RADIUS * 2 + 1) - WORLD_SPAWN_RADIUS;
            BlockPos candidate = base.offset(dx, 0, dz);
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
            BlockPos spawnPos = surface.above();
            if (level.getBlockState(spawnPos).isAir()) {
                return spawnPos;
            }
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base).above();
    }
}

