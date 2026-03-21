package bhw.voident.xyz.terrariafabric.npc.home;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

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
