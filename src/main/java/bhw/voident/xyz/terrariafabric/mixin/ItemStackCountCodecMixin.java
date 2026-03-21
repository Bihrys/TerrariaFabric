package bhw.voident.xyz.terrariafabric.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemStack.class)
/**
 * 类用途：功能实现类，负责该模块的核心业务逻辑。
 */
public class ItemStackCountCodecMixin {

    @ModifyConstant(method = "method_57371", constant = @Constant(intValue = 99))
    private static int terrariafabric$raiseCountCodecLimit(int original) {
        return 9999;
    }
}
