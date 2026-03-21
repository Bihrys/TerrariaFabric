package bhw.voident.xyz.terrariafabric.mixin;

import bhw.voident.xyz.terrariafabric.npc.home.HousingRelevantBlocks;
import bhw.voident.xyz.terrariafabric.npc.spawn.NpcSpawnScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
/**
 * 类用途：功能实现类，负责该模块的核心业务逻辑。
 */
public abstract class HousingBlockItemMixin {

    @Inject(method = "place", at = @At("RETURN"))
    private void terrariafabric$markHousingDirty(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!cir.getReturnValue().consumesAction()) {
            return;
        }

        BlockPos clickedPos = context.getClickedPos();
        if (HousingRelevantBlocks.isRelevant(level, clickedPos, level.getBlockState(clickedPos))) {
            NpcSpawnScheduler.markHousingDirty(level, clickedPos);
            return;
        }

        Direction face = context.getClickedFace();
        BlockPos placedPos = clickedPos.relative(face);
        if (HousingRelevantBlocks.isRelevant(level, placedPos, level.getBlockState(placedPos))) {
            NpcSpawnScheduler.markHousingDirty(level, placedPos);
        }
    }
}

