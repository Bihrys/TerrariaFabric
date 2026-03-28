package bhw.voident.xyz.terrariafabric.world.evil;

/** 类用途：记录世界主邪恶类型，给腐化/猩红判定和后续蔓延逻辑共用。 */
public enum EvilBiomeType {
    CORRUPTION("corruption"),
    CRIMSON("crimson");

    private final String id;

    EvilBiomeType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static EvilBiomeType fromId(String id) {
        for (EvilBiomeType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return CORRUPTION;
    }
}
