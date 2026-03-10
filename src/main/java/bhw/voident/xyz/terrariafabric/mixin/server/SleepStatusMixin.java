package bhw.voident.xyz.terrariafabric.mixin.server;

import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SleepStatus.class)
public class SleepStatusMixin {

    @Inject(method = "areEnoughSleeping", at = @At("HEAD"), cancellable = true)
    private void terrariafabric$disableVanillaSleepSkip(int playerCount, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
