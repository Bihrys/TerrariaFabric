package bhw.voident.xyz.terrariafabric.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;



public final class TerrariafabricEntities {

    public static final EntityType<GuideEntity> GUIDE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("terrariafabric", "guide"),
            FabricEntityTypeBuilder.create(MobCategory.CREATURE, GuideEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                    .trackRangeBlocks(8)
                    .trackedUpdateRate(3)
                    .build()
    );
    public static final EntityType<MerchantEntity> MERCHANT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("terrariafabric", "merchant"),
            FabricEntityTypeBuilder.create(MobCategory.CREATURE, MerchantEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                    .trackRangeBlocks(8)
                    .trackedUpdateRate(3)
                    .build()
    );

    private TerrariafabricEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(GUIDE, GuideEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(MERCHANT, MerchantEntity.createAttributes());
    }
}
