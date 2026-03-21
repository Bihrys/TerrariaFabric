package bhw.voident.xyz.terrariafabric.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public final class TerrariafabricItems {

    public static final Item LIFE_CRYSTAL = register(
            "life_crystal",
            new LifeCrystalItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON))
    );
    public static final Item COPPER_COIN = register(
            "copper_coin",
            new TerrariaCoinItem(new Item.Properties().stacksTo(100), TerrariaCoinItem.CoinTier.COPPER)
    );
    public static final Item SILVER_COIN = register(
            "silver_coin",
            new TerrariaCoinItem(new Item.Properties().stacksTo(100), TerrariaCoinItem.CoinTier.SILVER)
    );
    public static final Item GOLD_COIN = register(
            "gold_coin",
            new TerrariaCoinItem(new Item.Properties().stacksTo(100), TerrariaCoinItem.CoinTier.GOLD)
    );
    public static final Item PLATINUM_COIN = register(
            "platinum_coin",
            new TerrariaCoinItem(new Item.Properties().stacksTo(9999).rarity(Rarity.UNCOMMON), TerrariaCoinItem.CoinTier.PLATINUM)
    );

    private TerrariafabricItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> {
                    entries.accept(LIFE_CRYSTAL);
                    entries.accept(COPPER_COIN);
                    entries.accept(SILVER_COIN);
                    entries.accept(GOLD_COIN);
                    entries.accept(PLATINUM_COIN);
                });
    }

    private static Item register(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("terrariafabric", id), item);
    }
}
