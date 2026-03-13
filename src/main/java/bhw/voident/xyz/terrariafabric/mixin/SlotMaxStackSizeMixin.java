package bhw.voident.xyz.terrariafabric.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMaxStackSizeMixin {

    @Shadow
    @Final
    private Container container;

    @Unique
    private static final int TERRARIAFABRIC_MAX_STACK_SIZE = 9999;

    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void terrariafabric$raisePlayerInventoryMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        if (this.container instanceof Inventory) {
            cir.setReturnValue(TERRARIAFABRIC_MAX_STACK_SIZE);
        }
    }
}
