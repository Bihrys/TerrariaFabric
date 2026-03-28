package bhw.voident.xyz.terrariafabric.npc.spawn;

import bhw.voident.xyz.terrariafabric.npc.definition.NpcDefinition;
import bhw.voident.xyz.terrariafabric.npc.state.NpcWorldState;
import net.minecraft.server.level.ServerLevel;

/** 类用途：集中判断 Town NPC 现在应该首次生成、等待重生，还是暂时不处理。 */
public final class TownNpcSpawnJudge {

    private TownNpcSpawnJudge() {
    }

    public static Decision evaluate(ServerLevel level, NpcDefinition definition, NpcWorldState.NpcRecord record) {
        if (record.pendingRespawn()) {
            if (definition.needsHousingForRespawn() && !level.isDay()) {
                return Decision.WAITING;
            }
            return definition.canRespawn(level) ? Decision.RESPAWN : Decision.WAITING;
        }

        // 商人/护士这类非世界初始 NPC 可能卡在 spawnedOnce=true 但没有 pendingRespawn 的死状态，
        // 这里继续按重生处理；向导仍然只走世界初始生成或明确的 pendingRespawn，避免重复刷两只。
        if (record.spawnedOnce() && !definition.spawnsWithWorld()) {
            if (definition.needsHousingForRespawn() && !level.isDay()) {
                return Decision.WAITING;
            }
            return definition.canRespawn(level) ? Decision.RESPAWN : Decision.WAITING;
        }

        if (definition.canSpawnNaturally(level)) {
            return Decision.NEW_ARRIVAL;
        }

        return Decision.WAITING;
    }

    public enum Decision {
        WAITING,
        RESPAWN,
        NEW_ARRIVAL
    }
}
