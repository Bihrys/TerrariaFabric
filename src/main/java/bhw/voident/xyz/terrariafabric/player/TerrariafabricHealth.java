package bhw.voident.xyz.terrariafabric.player;

/**

 * 类用途：生命值常量与基础配置。

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
