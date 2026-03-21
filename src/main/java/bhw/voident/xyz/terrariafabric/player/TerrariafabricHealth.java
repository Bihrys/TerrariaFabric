package bhw.voident.xyz.terrariafabric.player;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public final class TerrariafabricHealth {

    public static final int DEFAULT_HEARTS = 5;
    public static final int MAX_HEARTS = 20;
    public static final double HEALTH_PER_HEART = 2.0;

    private TerrariafabricHealth() {
    }

    public static double heartsToHealth(int hearts) {
        return hearts * HEALTH_PER_HEART;
    }
}
