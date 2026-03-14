package bhw.voident.xyz.terrariafabric.item;

import bhw.voident.xyz.terrariafabric.player.TerrariafabricHealth;
import bhw.voident.xyz.terrariafabric.player.TerrariafabricMaxHearts;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LifeCrystalItem extends Item {

    public LifeCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof TerrariafabricMaxHearts holder)) {
            return InteractionResultHolder.pass(stack);
        }

        int currentHearts = holder.terrariafabric$getMaxHearts();
        if (currentHearts < TerrariafabricHealth.DEFAULT_HEARTS) {
            currentHearts = TerrariafabricHealth.DEFAULT_HEARTS;
        }

        if (currentHearts >= TerrariafabricHealth.MAX_HEARTS) {
            return InteractionResultHolder.fail(stack);
        }

        holder.terrariafabric$setMaxHearts(currentHearts + 1);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.heal((float) TerrariafabricHealth.HEALTH_PER_HEART);

        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
