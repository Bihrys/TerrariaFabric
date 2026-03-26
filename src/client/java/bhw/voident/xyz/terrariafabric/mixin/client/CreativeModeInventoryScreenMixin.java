package bhw.voident.xyz.terrariafabric.mixin.client;

import bhw.voident.xyz.terrariafabric.item.TerrariaCoinItem;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
/**
 * 类用途：创造模式物品栏堆叠与合并适配。
 */
public abstract class CreativeModeInventoryScreenMixin {

    @Shadow
    private boolean isCreativeSlot(Slot slot) {
        throw new AssertionError("Shadowed");
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void terrariafabric$handleCreativeShiftAndClone(Slot slot, int slotId, int button, ClickType clickType, CallbackInfo ci) {
        if (clickType == ClickType.CLONE) {
            terrariafabric$handleClone(slot, ci);
            return;
        }
        if (clickType == ClickType.QUICK_MOVE) {
            terrariafabric$handleQuickMove(slot, ci);
        }
    }

    @Unique
    private void terrariafabric$handleClone(Slot slot, CallbackInfo ci) {
        if (slot == null || !slot.hasItem()) {
            return;
        }
        AbstractContainerMenu currentMenu = terrariafabric$getMenu();
        if (!currentMenu.getCarried().isEmpty()) {
            return;
        }
        ItemStack source = slot.getItem();
        int max = source.getMaxStackSize();
        if (max <= 1) {
            return;
        }
        if (!(source.getItem() instanceof TerrariaCoinItem) && max < 9999) {
            max = 9999;
        }
        currentMenu.setCarried(source.copyWithCount(max));
        ci.cancel();
    }

    @Unique
    private void terrariafabric$handleQuickMove(Slot slot, CallbackInfo ci) {
        if (slot == null || !slot.hasItem()) {
            return;
        }
        if (!isCreativeSlot(slot)) {
            return;
        }

        LocalPlayer player = terrariafabric$getPlayer();
        MultiPlayerGameMode gameMode = terrariafabric$getGameMode();
        if (player == null || gameMode == null) {
            return;
        }

        ItemStack toAdd = slot.getItem().copy();
        if (toAdd.isEmpty()) {
            return;
        }
        int max = toAdd.getMaxStackSize();
        if (max <= 1) {
            return;
        }
        if (!(toAdd.getItem() instanceof TerrariaCoinItem) && max < 9999) {
            max = 9999;
        }
        toAdd.setCount(max);

        AbstractContainerMenu invMenu = player.inventoryMenu;
        boolean moved = false;

        // Merge into existing stacks first.
        for (int i = 0; i < invMenu.slots.size() && !toAdd.isEmpty(); i++) {
            Slot invSlot = invMenu.getSlot(i);
            if (terrariafabric$isPlayerMainInventorySlot(invSlot, player.getInventory())) {
                ItemStack existing = invSlot.getItem();
                if (existing.isEmpty()) {
                    continue;
                }
                if (!ItemStack.isSameItemSameComponents(existing, toAdd)) {
                    continue;
                }
                int existingMax = existing.getMaxStackSize();
                if (!(existing.getItem() instanceof TerrariaCoinItem) && existingMax < 9999 && existingMax > 1) {
                    existingMax = 9999;
                }
                int space = existingMax - existing.getCount();
                if (space <= 0) {
                    continue;
                }
                int move = Math.min(space, toAdd.getCount());
                if (move <= 0) {
                    continue;
                }
                existing.grow(move);
                invSlot.set(existing);
                gameMode.handleCreativeModeItemAdd(existing, i);
                toAdd.shrink(move);
                moved = true;
            }
        }

        // Fill empty slots if there is remaining.
        for (int i = 0; i < invMenu.slots.size() && !toAdd.isEmpty(); i++) {
            Slot invSlot = invMenu.getSlot(i);
            if (terrariafabric$isPlayerMainInventorySlot(invSlot, player.getInventory())) {
                if (!invSlot.getItem().isEmpty()) {
                    continue;
                }
                int move = Math.min(toAdd.getCount(), max);
                ItemStack placed = toAdd.copyWithCount(move);
                invSlot.set(placed);
                gameMode.handleCreativeModeItemAdd(placed, i);
                toAdd.shrink(move);
                moved = true;
            }
        }

        if (moved) {
            invMenu.broadcastChanges();
            ci.cancel();
        }
    }

    @Unique
    private static boolean terrariafabric$isPlayerMainInventorySlot(Slot slot, Inventory inventory) {
        if (slot.container != inventory) {
            return false;
        }
        int containerSlot = slot.getContainerSlot();
        return containerSlot >= 0 && containerSlot < 36;
    }

    @Unique
    private AbstractContainerMenu terrariafabric$getMenu() {
        return ((AbstractContainerScreen<?>) (Object) this).getMenu();
    }

    @Unique
    private LocalPlayer terrariafabric$getPlayer() {
        return net.minecraft.client.Minecraft.getInstance().player;
    }

    @Unique
    private MultiPlayerGameMode terrariafabric$getGameMode() {
        return net.minecraft.client.Minecraft.getInstance().gameMode;
    }
}

