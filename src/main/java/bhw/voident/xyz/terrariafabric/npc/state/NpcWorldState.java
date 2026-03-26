package bhw.voident.xyz.terrariafabric.npc.state;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**

 * 类用途：NPC 世界持久化状态（生成/重生/UUID）。

 */

public final class NpcWorldState extends SavedData {

    private static final String DATA_NAME = "terrariafabric_npc_state";

    private final Map<String, NpcRecord> records = new HashMap<>();

    public static NpcWorldState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(NpcWorldState::new, NpcWorldState::load, DataFixTypes.LEVEL),
                DATA_NAME
        );
    }

    public NpcRecord getOrCreate(String npcId) {
        NpcRecord record = records.get(npcId);
        if (record != null) {
            return record;
        }
        NpcRecord created = new NpcRecord();
        records.put(npcId, created);
        setDirty();
        return created;
    }

    public void setTrackedEntity(String npcId, UUID entityUuid) {
        NpcRecord record = getOrCreate(npcId);
        if (entityUuid != null && entityUuid.equals(record.entityUuid) && record.spawnedOnce && !record.pendingRespawn) {
            return;
        }
        record.entityUuid = entityUuid;
        record.spawnedOnce = true;
        record.pendingRespawn = false;
        setDirty();
    }

    public void markPendingRespawn(String npcId) {
        NpcRecord record = getOrCreate(npcId);
        if (record.pendingRespawn && record.entityUuid == null) {
            return;
        }
        record.entityUuid = null;
        record.pendingRespawn = true;
        record.spawnedOnce = true;
        setDirty();
    }

    public void setSpawnedOnce(String npcId) {
        NpcRecord record = getOrCreate(npcId);
        if (record.spawnedOnce) {
            return;
        }
        record.spawnedOnce = true;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<String, NpcRecord> entry : records.entrySet()) {
            CompoundTag recordTag = new CompoundTag();
            recordTag.putString("id", entry.getKey());
            NpcRecord record = entry.getValue();
            recordTag.putBoolean("spawnedOnce", record.spawnedOnce);
            recordTag.putBoolean("pendingRespawn", record.pendingRespawn);
            if (record.entityUuid != null) {
                recordTag.putUUID("entityUuid", record.entityUuid);
            }
            list.add(recordTag);
        }
        tag.put("records", list);
        return tag;
    }

    public static NpcWorldState load(CompoundTag tag, HolderLookup.Provider provider) {
        NpcWorldState state = new NpcWorldState();
        ListTag list = tag.getList("records", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag recordTag = list.getCompound(i);
            String id = recordTag.getString("id");
            if (id.isEmpty()) {
                continue;
            }
            NpcRecord record = new NpcRecord();
            record.spawnedOnce = recordTag.getBoolean("spawnedOnce");
            record.pendingRespawn = recordTag.getBoolean("pendingRespawn");
            if (recordTag.hasUUID("entityUuid")) {
                record.entityUuid = recordTag.getUUID("entityUuid");
            }
            state.records.put(id, record);
        }
        return state;
    }

    public static final class NpcRecord {
        private UUID entityUuid;
        private boolean spawnedOnce;
        private boolean pendingRespawn;

        public UUID entityUuid() {
            return entityUuid;
        }

        public boolean spawnedOnce() {
            return spawnedOnce;
        }

        public boolean pendingRespawn() {
            return pendingRespawn;
        }
    }
}

