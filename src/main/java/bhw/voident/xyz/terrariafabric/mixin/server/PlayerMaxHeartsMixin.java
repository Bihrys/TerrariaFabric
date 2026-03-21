package bhw.voident.xyz.terrariafabric.mixin.server;

import bhw.voident.xyz.terrariafabric.player.TerrariafabricHealth;
import bhw.voident.xyz.terrariafabric.player.TerrariafabricMaxHearts;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
/**
 * 类用途：功能实现类，负责该模块的核心业务逻辑。
 */
public abstract class PlayerMaxHeartsMixin implements TerrariafabricMaxHearts {

    @Unique
    private static final String TERRARIAFABRIC_MAX_HEARTS_TAG = "TerrariafabricMaxHearts";

    @Unique
    private int terrariafabric$maxHearts = -1;

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void terrariafabric$readMaxHearts(CompoundTag tag, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!(player instanceof ServerPlayer)) {
            return;
        }

        int hearts = tag.contains(TERRARIAFABRIC_MAX_HEARTS_TAG, Tag.TAG_INT)
                ? tag.getInt(TERRARIAFABRIC_MAX_HEARTS_TAG)
                : TerrariafabricHealth.DEFAULT_HEARTS;
        terrariafabric$maxHearts = Mth.clamp(hearts, TerrariafabricHealth.DEFAULT_HEARTS, TerrariafabricHealth.MAX_HEARTS);
        terrariafabric$applyMaxHearts(player);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void terrariafabric$writeMaxHearts(CompoundTag tag, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!(player instanceof ServerPlayer)) {
            return;
        }

        int hearts = terrariafabric$maxHearts;
        if (hearts < TerrariafabricHealth.DEFAULT_HEARTS) {
            hearts = TerrariafabricHealth.DEFAULT_HEARTS;
        }
        tag.putInt(TERRARIAFABRIC_MAX_HEARTS_TAG, hearts);
    }

    @Override
    public int terrariafabric$getMaxHearts() {
        return terrariafabric$maxHearts;
    }

    @Override
    public void terrariafabric$setMaxHearts(int hearts) {
        Player player = (Player) (Object) this;
        terrariafabric$maxHearts = Mth.clamp(hearts, TerrariafabricHealth.DEFAULT_HEARTS, TerrariafabricHealth.MAX_HEARTS);
        terrariafabric$applyMaxHearts(player);
    }

    @Unique
    private void terrariafabric$applyMaxHearts(Player player) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        if (terrariafabric$maxHearts < TerrariafabricHealth.DEFAULT_HEARTS) {
            return;
        }

        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) {
            return;
        }

        double targetHealth = TerrariafabricHealth.heartsToHealth(terrariafabric$maxHearts);
        attribute.setBaseValue(targetHealth);
        if (player.getHealth() > targetHealth) {
            player.setHealth((float) targetHealth);
        }
    }
}

