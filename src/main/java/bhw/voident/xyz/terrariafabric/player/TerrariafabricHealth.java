package bhw.voident.xyz.terrariafabric.player;

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
