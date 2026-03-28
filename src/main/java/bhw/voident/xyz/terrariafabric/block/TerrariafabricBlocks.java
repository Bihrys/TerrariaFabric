package bhw.voident.xyz.terrariafabric.block;

import bhw.voident.xyz.terrariafabric.world.evil.EvilBiomeType;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** 类用途：注册邪恶方块和对应方块物品。 */
public final class TerrariafabricBlocks {

    public static final Block CORRUPTION_GRASS_BLOCK = register(
            "corruption_grass_block",
            new EvilGrassBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(0.6F)
                            .sound(SoundType.GRASS)
            )
    );
    public static final Block CRIMSON_GRASS_BLOCK = register(
            "crimson_grass_block",
            new EvilGrassBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.CRIMSON_STEM)
                            .strength(0.6F)
                            .sound(SoundType.GRASS)
            )
    );

    private TerrariafabricBlocks() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS)
                .register(entries -> {
                    entries.accept(CORRUPTION_GRASS_BLOCK);
                    entries.accept(CRIMSON_GRASS_BLOCK);
                });
    }

    public static Block primaryGrass(EvilBiomeType type) {
        return type == EvilBiomeType.CRIMSON ? CRIMSON_GRASS_BLOCK : CORRUPTION_GRASS_BLOCK;
    }

    public static boolean isPrimaryGrass(Block block, EvilBiomeType type) {
        return block == primaryGrass(type);
    }

    private static Block register(String id, Block block) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath("terrariafabric", id);
        Block registered = Registry.register(BuiltInRegistries.BLOCK, key, block);
        Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(registered, new Item.Properties()));
        return registered;
    }
}
