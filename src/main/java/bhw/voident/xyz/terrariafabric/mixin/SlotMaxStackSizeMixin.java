package bhw.voident.xyz.terrariafabric.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
/**
 * 类用途：功能实现类，负责该模块的核心业务逻辑。
 */
public abstract class SlotMaxStackSizeMixin {

    @Unique
    private static final int TERRARIAFABRIC_MAX_STACK_SIZE = 9999;

    @Inject(method = "getMaxStackSize()I", at = @At("HEAD"), cancellable = true)
    private void terrariafabric$raiseSlotMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(TERRARIAFABRIC_MAX_STACK_SIZE);
    }
}
