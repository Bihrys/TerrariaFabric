package bhw.voident.xyz.terrariafabric.npc.definition;

import bhw.voident.xyz.terrariafabric.npc.NpcNames;
import bhw.voident.xyz.terrariafabric.npc.home.HousingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;

/**

 * 类用途：NPC 定义接口（名字、生成、入住规则）。

 */

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

    default String professionSuffix() {
        return id();
    }

    default String formatDisplayName(RandomSource random) {
        return pickName(random) + "(" + professionSuffix() + ")";
    }

    default boolean canUseRoom(ServerLevel level, HousingRegistry.RoomRecord room) {
        return true;
    }

    default boolean canSpawnNaturally(ServerLevel level) {
        return false;
    }

    default boolean canRespawn(ServerLevel level) {
        return true;
    }

    default void onSpawn(PathfinderMob npc, ServerLevel level) {
        npc.setCustomNameVisible(true);
        npc.setPersistenceRequired();
    }
}
