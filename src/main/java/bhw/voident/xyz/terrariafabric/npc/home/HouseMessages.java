package bhw.voident.xyz.terrariafabric.npc.home;

import net.minecraft.network.chat.Component;

import java.util.List;

/**

 * 类用途：拼装房屋检测提示文本。

 */

public final class HouseMessages {
    private HouseMessages() {
    }

    public static Component buildMissingMessage(List<HouseMissing> missing) {
        int size = missing.size();
        if (size == 0) {
            return Component.translatable("message.terrariafabric.house.invalid");
        }

        Component[] parts = new Component[size];
        for (int i = 0; i < size; i++) {
            parts[i] = Component.translatable(missing.get(i).translationKey());
        }

        return switch (size) {
            case 1 -> Component.translatable("message.terrariafabric.house.missing.one", parts[0]);
            case 2 -> Component.translatable("message.terrariafabric.house.missing.two", parts[0], parts[1]);
            case 3 -> Component.translatable("message.terrariafabric.house.missing.three", parts[0], parts[1], parts[2]);
            case 4 -> Component.translatable("message.terrariafabric.house.missing.four", parts[0], parts[1], parts[2], parts[3]);
            default -> Component.translatable("message.terrariafabric.house.invalid");
        };
    }
}

