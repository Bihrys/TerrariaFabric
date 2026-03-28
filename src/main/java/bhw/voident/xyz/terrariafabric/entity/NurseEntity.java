package bhw.voident.xyz.terrariafabric.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** 类用途：护士 NPC 的占位实体，先接入城镇 NPC 调度和住房系统。 */
public class NurseEntity extends AbstractTownNpcEntity {

    public NurseEntity(EntityType<? extends NurseEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTownNpcAttributes(220.0, 20.0, 0.35, 4.0);
    }

    @Override
    protected void registerGoals() {
        registerTownNpcGoals(false);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.POTION));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected String npcId() {
        return "nurse";
    }
}
