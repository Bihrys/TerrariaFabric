package bhw.voident.xyz.terrariafabric.npc.definition;

import bhw.voident.xyz.terrariafabric.npc.NpcNames;
import bhw.voident.xyz.terrariafabric.npc.home.HousingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;

public interface NpcDefinition {

    String id();

    EntityType<? extends PathfinderMob> entityType();

    Class<? extends PathfinderMob> entityClass();

    String[] fallbackNames();

    boolean spawnsWithWorld();

    boolean needsHousingForRespawn();

    BlockPos findWorldSpawnPosition(ServerLevel level, ServerPlayer player);

    default String pickName(RandomSource random) {
        return NpcNames.pick(id(), random, fallbackNames());
    }

    default boolean canUseRoom(ServerLevel level, HousingRegistry.RoomRecord room) {
        return true;
    }

    default void onSpawn(PathfinderMob npc, ServerLevel level) {
        npc.setCustomNameVisible(true);
        npc.setPersistenceRequired();
    }
}
