package bhw.voident.xyz.terrariafabric.mixin.server;

import bhw.voident.xyz.terrariafabric.npc.home.HousingRelevantBlocks;
import bhw.voident.xyz.terrariafabric.npc.spawn.NpcSpawnScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
/**
 * 类用途：破坏房屋相关方块时标记房屋脏区。
 */
public abstract class ServerPlayerGameModeHousingMixin {

    @Shadow
    protected ServerLevel level;

    @Unique
    private BlockPos terrariafabric$dirtyBlockPos;

    @Unique
    private boolean terrariafabric$shouldMarkDirty;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void terrariafabric$captureBrokenBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        terrariafabric$dirtyBlockPos = pos.immutable();
        terrariafabric$shouldMarkDirty = HousingRelevantBlocks.isRelevant(level, pos, level.getBlockState(pos));
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void terrariafabric$markHousingDirty(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (terrariafabric$shouldMarkDirty && cir.getReturnValue()) {
            NpcSpawnScheduler.markHousingDirty(level, terrariafabric$dirtyBlockPos);
        }
        terrariafabric$dirtyBlockPos = null;
        terrariafabric$shouldMarkDirty = false;
    }
}

