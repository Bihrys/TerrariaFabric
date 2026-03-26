package bhw.voident.xyz.terrariafabric.mixin;

import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
/**
 * 类用途：容器层最大堆叠上限提升到 9999。
 */
public interface ContainerMaxStackSizeMixin {

    @Inject(method = "getMaxStackSize()I", at = @At("HEAD"), cancellable = true)
    default void terrariafabric$raiseContainerMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(9999);
    }
}
