package bhw.voident.xyz.terrariafabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
/**
 * 类用途：功能实现类，负责该模块的核心业务逻辑。
 */
public class ItemStackNoDurabilityMixin {

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void terrariafabric$preventDurabilityServer(int amount, ServerLevel level, ServerPlayer player,
                                                        Consumer<Item> onBroken, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isDamageableItem()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void terrariafabric$preventDurabilityLiving(int amount, LivingEntity entity, EquipmentSlot slot,
                                                        CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isDamageableItem()) {
            ci.cancel();
        }
    }
}

