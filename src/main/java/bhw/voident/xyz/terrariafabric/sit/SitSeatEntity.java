package bhw.voident.xyz.terrariafabric.sit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 类用途：不可见的座位实体，承载坐下状态和下车位置。 */
public class SitSeatEntity extends Entity {
    public SitSeatEntity(EntityType<? extends SitSeatEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
        setInvisible(true);
    }

    public SitSeatEntity(Level level) {
        this(TerrariafabricSit.SIT_ENTITY_TYPE, level);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (passenger instanceof Player player) {
            Vec3 resetPosition = SitUtil.getPreviousPlayerPosition(player, this);
            if (resetPosition != null) {
                discard();
                return resetPosition;
            }
        }

        discard();
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        SitUtil.removeSitEntity(level(), blockPosition());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
