package bhw.voident.xyz.terrariafabric.npc.home;

import java.util.List;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public final class HouseCheckResult {
    private final boolean hasStart;
    private final boolean structuralValid;
    private final List<HouseMissing> missing;
    private final HouseRoom room;

    private HouseCheckResult(boolean hasStart, boolean structuralValid, List<HouseMissing> missing, HouseRoom room) {
        this.hasStart = hasStart;
        this.structuralValid = structuralValid;
        this.missing = missing;
        this.room = room;
    }

    public static HouseCheckResult noStart() {
        return new HouseCheckResult(false, false, List.of(), null);
    }

    public static HouseCheckResult invalid() {
        return new HouseCheckResult(true, false, List.of(), null);
    }

    public static HouseCheckResult withRoom(boolean structuralValid, List<HouseMissing> missing, HouseRoom room) {
        return new HouseCheckResult(true, structuralValid, List.copyOf(missing), room);
    }

    public boolean hasStart() {
        return hasStart;
    }

    public boolean hasValidStructure() {
        return hasStart && structuralValid;
    }

    public List<HouseMissing> missing() {
        return missing;
    }

    public boolean isSuitable() {
        return hasValidStructure() && missing.isEmpty() && room != null;
    }

    public HouseRoom room() {
        return room;
    }
}
