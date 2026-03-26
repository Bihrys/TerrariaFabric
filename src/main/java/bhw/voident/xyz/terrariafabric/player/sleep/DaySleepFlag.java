package bhw.voident.xyz.terrariafabric.player.sleep;

/**

 * 类用途：标记玩家是否处于白天睡觉状态。

 */

public interface DaySleepFlag {
    void terrariafabric$setDaySleepForced(boolean value);

    boolean terrariafabric$isDaySleepForced();
}
