package bhw.voident.xyz.terrariafabric.boss.state;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** 类用途：保存 Boss 击败进度，给 NPC 解锁、hardmode 和掉落条件复用。 */
public final class BossProgressionState extends SavedData {

    private static final String DATA_NAME = "terrariafabric_boss_progression";

    private final Set<String> defeatedBosses = new HashSet<>();

    public static BossProgressionState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(BossProgressionState::new, BossProgressionState::load, DataFixTypes.LEVEL),
                DATA_NAME
        );
    }

    public boolean isDefeated(String bossId) {
        return bossId != null && defeatedBosses.contains(bossId);
    }

    public boolean markDefeated(String bossId) {
        if (bossId == null || bossId.isBlank()) {
            return false;
        }
        if (!defeatedBosses.add(bossId)) {
            return false;
        }
        setDirty();
        return true;
    }

    public Set<String> defeatedBosses() {
        return Collections.unmodifiableSet(defeatedBosses);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (String bossId : defeatedBosses) {
            list.add(StringTag.valueOf(bossId));
        }
        tag.put("defeatedBosses", list);
        return tag;
    }

    public static BossProgressionState load(CompoundTag tag, HolderLookup.Provider provider) {
        BossProgressionState state = new BossProgressionState();
        ListTag list = tag.getList("defeatedBosses", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            String bossId = list.getString(i);
            if (!bossId.isBlank()) {
                state.defeatedBosses.add(bossId);
            }
        }
        return state;
    }
}
