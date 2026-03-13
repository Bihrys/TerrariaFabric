package bhw.voident.xyz.terrariafabric.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMaxStackSizeMixin {

    @Unique
    private static final int TERRARIAFABRIC_MAX_STACK_SIZE = 9999;

    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void terrariafabric$raiseMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        int original = cir.getReturnValue();
        if (original > 1) {
            cir.setReturnValue(TERRARIAFABRIC_MAX_STACK_SIZE);
        }
    }
}
