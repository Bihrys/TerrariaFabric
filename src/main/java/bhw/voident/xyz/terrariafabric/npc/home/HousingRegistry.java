package bhw.voident.xyz.terrariafabric.npc.home;

import bhw.voident.xyz.terrariafabric.npc.definition.NpcDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class HousingRegistry extends SavedData {

    public static final String DATA_NAME = "terrariafabric_housing";

    private final List<RoomRecord> rooms = new ArrayList<>();

    public static HousingRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(HousingRegistry::new, HousingRegistry::load, DataFixTypes.LEVEL),
                DATA_NAME
        );
    }

    public RoomRecord syncRoom(HouseRoom room, long gameTime) {
        RoomRecord existing = findRoom(room.min(), room.max());
        if (existing == null) {
            existing = new RoomRecord(room.min(), room.max());
            rooms.add(existing);
        }
        existing.dirty = false;
        existing.lastCheckedGameTime = gameTime;
        setDirty();
        return existing;
    }

    public RoomRecord findRoom(BlockPos min, BlockPos max) {
        for (RoomRecord room : rooms) {
            if (room.matches(min, max)) {
                return room;
            }
        }
        return null;
    }

    public RoomRecord findAssignedRoom(String occupantId) {
        if (occupantId == null) {
            return null;
        }
        for (RoomRecord room : rooms) {
            if (occupantId.equals(room.occupantId)) {
                return room;
            }
        }
        return null;
    }

    public RoomRecord findAvailableRoomFor(ServerLevel level, NpcDefinition definition) {
        for (RoomRecord room : rooms) {
            if (room.isOccupied() || room.dirty) {
                continue;
            }
            if (definition.canUseRoom(level, room)) {
                return room;
            }
        }
        return null;
    }

    public void setOccupant(RoomRecord room, String occupantId, boolean manual) {
        if (room == null) {
            return;
        }
        if (occupantId == null && room.occupantId == null && room.manual == manual) {
            return;
        }
        if (occupantId != null && occupantId.equals(room.occupantId) && room.manual == manual) {
            return;
        }
        room.occupantId = occupantId;
        room.manual = manual;
        setDirty();
    }

    public void clearOccupant(String occupantId) {
        if (occupantId == null) {
            return;
        }
        boolean changed = false;
        for (RoomRecord room : rooms) {
            if (occupantId.equals(room.occupantId)) {
                room.occupantId = null;
                room.manual = false;
                changed = true;
            }
        }
        if (changed) {
            setDirty();
        }
    }

    public void markDirtyAround(BlockPos anchor) {
        boolean changed = false;
        for (RoomRecord room : rooms) {
            if (room.touches(anchor)) {
                room.dirty = true;
                changed = true;
            }
        }
        if (changed) {
            setDirty();
        }
    }

    public void reconcileResult(BlockPos anchor, HouseCheckResult result, long gameTime) {
        boolean changed = false;
        Iterator<RoomRecord> iterator = rooms.iterator();
        while (iterator.hasNext()) {
            RoomRecord room = iterator.next();
            if (!room.contains(anchor)) {
                continue;
            }
            if (result.isSuitable() && room.matches(result.room().min(), result.room().max())) {
                room.dirty = false;
                room.lastCheckedGameTime = gameTime;
                changed = true;
                continue;
            }
            iterator.remove();
            changed = true;
        }

        if (result.isSuitable() && result.room() != null) {
            RoomRecord synced = syncRoom(result.room(), gameTime);
            synced.dirty = false;
            synced.lastCheckedGameTime = gameTime;
            return;
        }

        if (changed) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (RoomRecord room : rooms) {
            CompoundTag roomTag = new CompoundTag();
            roomTag.putInt("minX", room.min.getX());
            roomTag.putInt("minY", room.min.getY());
            roomTag.putInt("minZ", room.min.getZ());
            roomTag.putInt("maxX", room.max.getX());
            roomTag.putInt("maxY", room.max.getY());
            roomTag.putInt("maxZ", room.max.getZ());
            if (room.occupantId != null) {
                roomTag.putString("occupant", room.occupantId);
            }
            if (room.manual) {
                roomTag.putBoolean("manual", true);
            }
            if (room.dirty) {
                roomTag.putBoolean("dirty", true);
            }
            roomTag.putLong("lastChecked", room.lastCheckedGameTime);
            list.add(roomTag);
        }
        tag.put("rooms", list);
        return tag;
    }

    public static HousingRegistry load(CompoundTag tag, HolderLookup.Provider provider) {
        HousingRegistry registry = new HousingRegistry();
        ListTag list = tag.getList("rooms", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag roomTag = list.getCompound(i);
            RoomRecord room = new RoomRecord(
                    new BlockPos(roomTag.getInt("minX"), roomTag.getInt("minY"), roomTag.getInt("minZ")),
                    new BlockPos(roomTag.getInt("maxX"), roomTag.getInt("maxY"), roomTag.getInt("maxZ"))
            );
            if (roomTag.contains("occupant", Tag.TAG_STRING)) {
                room.occupantId = roomTag.getString("occupant");
            }
            room.manual = roomTag.getBoolean("manual");
            room.dirty = roomTag.getBoolean("dirty");
            room.lastCheckedGameTime = roomTag.getLong("lastChecked");
            registry.rooms.add(room);
        }
        return registry;
    }

    public static final class RoomRecord {
        private final BlockPos min;
        private final BlockPos max;
        private String occupantId;
        private boolean manual;
        private boolean dirty;
        private long lastCheckedGameTime;

        private RoomRecord(BlockPos min, BlockPos max) {
            this.min = min;
            this.max = max;
        }

        public BlockPos min() {
            return min;
        }

        public BlockPos max() {
            return max;
        }

        public BlockPos center() {
            return new BlockPos(
                    (min.getX() + max.getX()) / 2,
                    (min.getY() + max.getY()) / 2,
                    (min.getZ() + max.getZ()) / 2
            );
        }

        public int radius() {
            int dx = Math.max(1, max.getX() - min.getX());
            int dz = Math.max(1, max.getZ() - min.getZ());
            return Math.max(dx, dz) / 2 + 2;
        }

        public String occupantId() {
            return occupantId;
        }

        public boolean isManual() {
            return manual;
        }

        public boolean isOccupied() {
            return occupantId != null && !occupantId.isEmpty();
        }

        public boolean isDirty() {
            return dirty;
        }

        public long lastCheckedGameTime() {
            return lastCheckedGameTime;
        }

        public boolean matches(BlockPos otherMin, BlockPos otherMax) {
            return min.equals(otherMin) && max.equals(otherMax);
        }

        boolean contains(BlockPos pos) {
            return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                    && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                    && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
        }

        boolean touches(BlockPos pos) {
            return pos.getX() >= min.getX() - 1 && pos.getX() <= max.getX() + 1
                    && pos.getY() >= min.getY() - 1 && pos.getY() <= max.getY() + 1
                    && pos.getZ() >= min.getZ() - 1 && pos.getZ() <= max.getZ() + 1;
        }
    }
}
