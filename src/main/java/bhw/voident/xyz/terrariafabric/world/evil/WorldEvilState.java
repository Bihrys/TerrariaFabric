package bhw.voident.xyz.terrariafabric.world.evil;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/** 类用途：保存 Terraria 式世界状态，包括主邪恶类型、蔓延开关和 hardmode 标记。 */
public final class WorldEvilState extends SavedData {

    private static final String DATA_NAME = "terrariafabric_world_evil_state";

    private EvilBiomeType primaryEvil = EvilBiomeType.CORRUPTION;
    private boolean primaryEvilChosen;
    private boolean hardmodeUnlocked;
    private boolean biomeSpreadEnabled = true;
    private boolean initialPatchGenerated;

    public static WorldEvilState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WorldEvilState::new, WorldEvilState::load, DataFixTypes.LEVEL),
                DATA_NAME
        );
    }

    public EvilBiomeType primaryEvil() {
        return primaryEvil;
    }

    public boolean primaryEvilChosen() {
        return primaryEvilChosen;
    }

    public void choosePrimaryEvil(EvilBiomeType type) {
        if (type == null) {
            return;
        }
        if (primaryEvilChosen && primaryEvil == type) {
            return;
        }
        primaryEvil = type;
        primaryEvilChosen = true;
        setDirty();
    }

    public boolean hardmodeUnlocked() {
        return hardmodeUnlocked;
    }

    public void setHardmodeUnlocked(boolean hardmodeUnlocked) {
        if (this.hardmodeUnlocked == hardmodeUnlocked) {
            return;
        }
        this.hardmodeUnlocked = hardmodeUnlocked;
        setDirty();
    }

    public boolean biomeSpreadEnabled() {
        return biomeSpreadEnabled;
    }

    public void setBiomeSpreadEnabled(boolean biomeSpreadEnabled) {
        if (this.biomeSpreadEnabled == biomeSpreadEnabled) {
            return;
        }
        this.biomeSpreadEnabled = biomeSpreadEnabled;
        setDirty();
    }

    public boolean initialPatchGenerated() {
        return initialPatchGenerated;
    }

    public void setInitialPatchGenerated(boolean initialPatchGenerated) {
        if (this.initialPatchGenerated == initialPatchGenerated) {
            return;
        }
        this.initialPatchGenerated = initialPatchGenerated;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putString("primaryEvil", primaryEvil.id());
        tag.putBoolean("primaryEvilChosen", primaryEvilChosen);
        tag.putBoolean("hardmodeUnlocked", hardmodeUnlocked);
        tag.putBoolean("biomeSpreadEnabled", biomeSpreadEnabled);
        tag.putBoolean("initialPatchGenerated", initialPatchGenerated);
        return tag;
    }

    public static WorldEvilState load(CompoundTag tag, HolderLookup.Provider provider) {
        WorldEvilState state = new WorldEvilState();
        state.primaryEvil = EvilBiomeType.fromId(tag.getString("primaryEvil"));
        state.primaryEvilChosen = tag.getBoolean("primaryEvilChosen");
        state.hardmodeUnlocked = tag.getBoolean("hardmodeUnlocked");
        if (tag.contains("biomeSpreadEnabled")) {
            state.biomeSpreadEnabled = tag.getBoolean("biomeSpreadEnabled");
        }
        if (tag.contains("initialPatchGenerated")) {
            state.initialPatchGenerated = tag.getBoolean("initialPatchGenerated");
        }
        return state;
    }
}
