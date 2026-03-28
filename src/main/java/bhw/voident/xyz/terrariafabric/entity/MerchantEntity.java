package bhw.voident.xyz.terrariafabric.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** 类用途：商人 NPC 实体，当前负责驻留和基础占位行为。 */
public class MerchantEntity extends AbstractTownNpcEntity {

    public MerchantEntity(EntityType<? extends MerchantEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTownNpcAttributes(220.0, 24.0, 0.35, 6.0);
    }

    @Override
    protected void registerGoals() {
        registerTownNpcGoals(false);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.EMERALD));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected String npcId() {
        return "merchant";
    }
}
