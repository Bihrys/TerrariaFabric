package bhw.voident.xyz.terrariafabric.npc.home;

/**

 * 类用途：房屋缺失条件枚举。

 */

public enum HouseMissing {
    DOOR("message.terrariafabric.house.missing.door"),
    TABLE("message.terrariafabric.house.missing.table"),
    CHAIR("message.terrariafabric.house.missing.chair"),
    LIGHT("message.terrariafabric.house.missing.light");

    private final String translationKey;

    HouseMissing(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
