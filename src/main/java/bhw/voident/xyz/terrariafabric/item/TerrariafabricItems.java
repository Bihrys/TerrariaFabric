package bhw.voident.xyz.terrariafabric.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class TerrariafabricItems {

    public static final Item LIFE_CRYSTAL = register(
            "life_crystal",
            new LifeCrystalItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON))
    );

    private TerrariafabricItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(LIFE_CRYSTAL));
    }

    private static Item register(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("terrariafabric", id), item);
    }
}
