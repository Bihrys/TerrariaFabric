package bhw.voident.xyz.terrariafabric.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** 类用途：向导 NPC 实体，当前负责基础战斗和住房驻留。 */
public class GuideEntity extends AbstractTownNpcEntity {

    public GuideEntity(EntityType<? extends GuideEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTownNpcAttributes(250.0, 30.0, 0.5, 8.0);
    }

    @Override
    protected void registerGoals() {
        registerTownNpcGoals(true);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected String npcId() {
        return "guide";
    }
}
