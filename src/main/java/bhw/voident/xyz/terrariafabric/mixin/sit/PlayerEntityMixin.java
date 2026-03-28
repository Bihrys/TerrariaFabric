package bhw.voident.xyz.terrariafabric.mixin.sit;

import bhw.voident.xyz.terrariafabric.sit.SitLogic;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 类用途：拦截潜行下车，让叠坐时由 sit 逻辑接管按键。 */
@Mixin(Player.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    private void terrariafabric$handleShiftForSitRides(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (SitLogic.shouldShiftBeHandledByMod(player)) {
            cir.setReturnValue(false);
        }
    }
}
