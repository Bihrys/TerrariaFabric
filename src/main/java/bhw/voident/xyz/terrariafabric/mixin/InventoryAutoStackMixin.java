package bhw.voident.xyz.terrariafabric.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryAutoStackMixin {

    @Shadow
    @Final
    public NonNullList<ItemStack> items;

    @Inject(method = "load(Lnet/minecraft/nbt/ListTag;)V", at = @At("TAIL"))
    private void terrariafabric$compactStacksOnLoad(ListTag tag, CallbackInfo ci) {
        terrariafabric$compactInventoryStacks(items);
    }

    @Unique
    private static void terrariafabric$compactInventoryStacks(NonNullList<ItemStack> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ItemStack target = list.get(i);
            if (target.isEmpty()) {
                continue;
            }
            int max = target.getMaxStackSize();
            if (max <= 1) {
                continue;
            }
            for (int j = i + 1; j < size; j++) {
                ItemStack other = list.get(j);
                if (other.isEmpty()) {
                    continue;
                }
                if (!ItemStack.isSameItemSameComponents(target, other)) {
                    continue;
                }
                if (target.getCount() >= max) {
                    break;
                }
                int space = max - target.getCount();
                int move = Math.min(space, other.getCount());
                if (move <= 0) {
                    continue;
                }
                target.grow(move);
                other.shrink(move);
                if (other.isEmpty()) {
                    list.set(j, ItemStack.EMPTY);
                }
            }
        }
    }
}
