package bhw.voident.xyz.terrariafabric.item;

import bhw.voident.xyz.terrariafabric.currency.CoinCurrencySystem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TerrariaCoinItem extends Item {

    private final CoinTier tier;

    public TerrariaCoinItem(Properties properties, CoinTier tier) {
        super(properties);
        this.tier = tier;
    }

    public CoinTier tier() {
        return tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (tier != CoinTier.SILVER || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        if (stack.isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        ItemStack copperStack = new ItemStack(TerrariafabricItems.COPPER_COIN, CoinCurrencySystem.COPPER_PER_SILVER);
        serverPlayer.getInventory().add(copperStack);
        if (!copperStack.isEmpty()) {
            player.drop(copperStack, false);
        }

        // Manual split should not be immediately re-upgraded by auto conversion.
        CoinCurrencySystem.pauseAutoConvertForManualSplit(serverPlayer);
        player.sendSystemMessage(Component.translatable("message.terrariafabric.coin.split.silver_to_copper"));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    public enum CoinTier {
        COPPER,
        SILVER,
        GOLD,
        PLATINUM
    }
}

