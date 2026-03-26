package bhw.voident.xyz.terrariafabric.mixin;

import bhw.voident.xyz.terrariafabric.currency.CoinCurrencySystem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
/**
 * 类用途：物品栏变更时触发自动换币。
 */
public abstract class InventoryCoinAutoConvertMixin {

    @Shadow
    @Final
    public Player player;

    @Inject(method = "setChanged", at = @At("TAIL"))
    private void terrariafabric$autoConvertCoinsOnInventoryChange(CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            CoinCurrencySystem.tryAutoConvertNow(serverPlayer);
        }
    }
}


