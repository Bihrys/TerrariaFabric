package bhw.voident.xyz.terrariafabric.mixin.server;

import bhw.voident.xyz.terrariafabric.player.sleep.DaySleepFlag;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerPlayer.class)
/**
 * 类用途：功能实现类，负责该模块的核心业务逻辑。
 */
public abstract class ServerPlayerSleepMixin {

    @Inject(method = "startSleepInBed", at = @At("RETURN"), cancellable = true)
    private void terrariafabric$allowSleepAnytime(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        Either<Player.BedSleepingProblem, Unit> result = cir.getReturnValue();
        if (result == null || result.right().isPresent()) {
            return;
        }

        Optional<Player.BedSleepingProblem> problem = result.left();
        if (problem.isEmpty()) {
            return;
        }

        Player.BedSleepingProblem reason = problem.get();
        if (reason != Player.BedSleepingProblem.NOT_POSSIBLE_NOW && reason != Player.BedSleepingProblem.NOT_SAFE) {
            return;
        }

        ServerPlayer player = (ServerPlayer) (Object) this;
        BlockState state = player.level().getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return;
        }

        player.startSleeping(pos);
        player.setRespawnPosition(player.serverLevel().dimension(), pos, player.getYRot(), false, true);
        ((DaySleepFlag) (Object) player).terrariafabric$setDaySleepForced(true);
        cir.setReturnValue(Either.right(Unit.INSTANCE));
    }
}

