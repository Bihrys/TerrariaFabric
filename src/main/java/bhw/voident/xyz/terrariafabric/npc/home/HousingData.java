package bhw.voident.xyz.terrariafabric.npc.home;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

public final class HousingData extends SavedData {

    public static final String DATA_NAME = "terrariafabric_housing";
    public static final String NPC_GUIDE = "guide";

    private final List<RoomRecord> rooms = new ArrayList<>();

    public static HousingData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(HousingData::new, HousingData::load, DataFixTypes.LEVEL),
                DATA_NAME
        );
    }

    public RoomRecord getOrCreate(HouseRoom room) {
        return getOrCreate(room.min(), room.max());
    }

    public RoomRecord getOrCreate(BlockPos min, BlockPos max) {
        RoomRecord existing = findRoom(min, max);
        if (existing != null) {
            return existing;
        }
        RoomRecord record = new RoomRecord(min, max, null, false);
        rooms.add(record);
        setDirty();
        return record;
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
            if (occupantId.equals(room.getOccupantId())) {
                return room;
            }
        }
        return null;
    }

    public RoomRecord findRandomEmptyRoom(RandomSource random) {
        List<RoomRecord> empty = new ArrayList<>();
        for (RoomRecord room : rooms) {
            if (!room.isOccupied()) {
                empty.add(room);
            }
        }
        if (empty.isEmpty()) {
            return null;
        }
        return empty.get(random.nextInt(empty.size()));
    }

    public void setOccupant(RoomRecord room, String occupantId, boolean manual) {
        if (room == null) {
            return;
        }
        if (occupantId == null && room.getOccupantId() == null && room.isManual() == manual) {
            return;
        }
        if (occupantId != null && occupantId.equals(room.getOccupantId()) && room.isManual() == manual) {
            return;
        }
        room.setOccupant(occupantId, manual);
        setDirty();
    }

    public void clearOccupant(String occupantId) {
        if (occupantId == null) {
            return;
        }
        boolean changed = false;
        for (RoomRecord room : rooms) {
            if (occupantId.equals(room.getOccupantId())) {
                room.setOccupant(null, false);
                changed = true;
            }
        }
        if (changed) {
            setDirty();
        }
    }

    public void removeRoom(RoomRecord room) {
        if (room == null) {
            return;
        }
        if (rooms.remove(room)) {
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
            list.add(roomTag);
        }
        tag.put("rooms", list);
        return tag;
    }

    public static HousingData load(CompoundTag tag, HolderLookup.Provider provider) {
        HousingData data = new HousingData();
        ListTag list = tag.getList("rooms", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag roomTag = list.getCompound(i);
            BlockPos min = new BlockPos(
                    roomTag.getInt("minX"),
                    roomTag.getInt("minY"),
                    roomTag.getInt("minZ")
            );
            BlockPos max = new BlockPos(
                    roomTag.getInt("maxX"),
                    roomTag.getInt("maxY"),
                    roomTag.getInt("maxZ")
            );
            String occupant = null;
            if (roomTag.contains("occupant", Tag.TAG_STRING)) {
                occupant = roomTag.getString("occupant");
            }
            boolean manual = roomTag.contains("manual", Tag.TAG_BYTE) && roomTag.getBoolean("manual");
            data.rooms.add(new RoomRecord(min, max, occupant, manual));
        }
        return data;
    }

    public static final class RoomRecord {
        private final BlockPos min;
        private final BlockPos max;
        private String occupantId;
        private boolean manual;

        private RoomRecord(BlockPos min, BlockPos max, String occupantId, boolean manual) {
            this.min = min;
            this.max = max;
            this.occupantId = occupantId;
            this.manual = manual;
        }

        public BlockPos getMin() {
            return min;
        }

        public BlockPos getMax() {
            return max;
        }

        public BlockPos getCenter() {
            return new BlockPos(
                    (min.getX() + max.getX()) / 2,
                    (min.getY() + max.getY()) / 2,
                    (min.getZ() + max.getZ()) / 2
            );
        }

        public int getRadius() {
            int dx = Math.max(1, max.getX() - min.getX());
            int dz = Math.max(1, max.getZ() - min.getZ());
            return Math.max(dx, dz) / 2 + 2;
        }

        public String getOccupantId() {
            return occupantId;
        }

        public void setOccupant(String occupantId, boolean manual) {
            this.occupantId = occupantId;
            this.manual = manual;
        }

        public boolean isManual() {
            return manual;
        }

        public boolean isOccupied() {
            return occupantId != null && !occupantId.isEmpty();
        }

        public boolean matches(BlockPos otherMin, BlockPos otherMax) {
            return min.equals(otherMin) && max.equals(otherMax);
        }
    }
}
