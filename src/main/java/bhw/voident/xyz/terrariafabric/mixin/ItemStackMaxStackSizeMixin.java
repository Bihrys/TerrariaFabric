package bhw.voident.xyz.terrariafabric.mixin;

import bhw.voident.xyz.terrariafabric.item.TerrariaCoinItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
/**
 * 类用途：功能实现类，负责该模块的核心业务逻辑。
 */
public class ItemStackMaxStackSizeMixin {

    @Unique
    private static final int TERRARIAFABRIC_MAX_STACK_SIZE = 9999;

    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void terrariafabric$raiseMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        // Coins keep Terraria-specific stack caps (copper/silver/gold 100, platinum 9999).
        if (stack.getItem() instanceof TerrariaCoinItem) {
            return;
        }
        int original = cir.getReturnValue();
        if (original > 1) {
            cir.setReturnValue(TERRARIAFABRIC_MAX_STACK_SIZE);
        }
    }
}

