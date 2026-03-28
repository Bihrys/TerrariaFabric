package bhw.voident.xyz.terrariafabric.entity;

import bhw.voident.xyz.terrariafabric.npc.spawn.NpcResidenceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 类用途：城镇 NPC 共用实体基类，统一门路、驻留和死亡回调。 */
public abstract class AbstractTownNpcEntity extends PathfinderMob {

    protected AbstractTownNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setCanPickUpLoot(false);
        if (getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(true);
            navigation.setCanPassDoors(true);
        }
    }

    protected static AttributeSupplier.Builder createTownNpcAttributes(
            double maxHealth,
            double armor,
            double knockbackResistance,
            double attackDamage
    ) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    protected void registerTownNpcGoals(boolean canFight) {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(2, new MoveTowardsRestrictionGoal(this, 1.0));
        if (canFight) {
            goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        }
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.6));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    protected abstract String npcId();

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (level() instanceof ServerLevel serverLevel) {
            NpcResidenceManager.onNpcDeath(serverLevel, npcId(), getUUID());
        }
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        return false;
    }
}
