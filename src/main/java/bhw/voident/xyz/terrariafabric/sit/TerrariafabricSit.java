package bhw.voident.xyz.terrariafabric.sit;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public final class TerrariafabricSit {

    public static final String MOD_ID = "terrariafabric";
    public static final EntityType<SitSeatEntity> SIT_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "entity_sit"),
            FabricEntityTypeBuilder.<SitSeatEntity>create(MobCategory.MISC, SitSeatEntity::new)
                    .dimensions(EntityDimensions.fixed(0.001F, 0.001F))
                    .trackRangeBlocks(16)
                    .trackedUpdateRate(1)
                    .build()
    );

    private TerrariafabricSit() {
    }

    public static void register() {
        SitConfig.get();
        UseBlockCallback.EVENT.register(TerrariafabricSit::handleBlockUse);
        UseEntityCallback.EVENT.register(TerrariafabricSit::handleEntityUse);
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide()) {
                return;
            }

            SitSeatEntity seat = SitUtil.getSitEntity(world, pos);
            if (seat == null) {
                seat = SitLogic.findSeat(world, pos);
            }

            if (seat != null) {
                SitUtil.removeSitEntity(world, pos);
                seat.ejectPassengers();
                seat.discard();
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers()
                .forEach(SitLogic::tickServerPlayer));
        ServerTickEvents.END_WORLD_TICK.register(SitLogic::cleanupSeats);
    }

    private static InteractionResult handleBlockUse(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!SitLogic.canSitOn(state)) {
            return InteractionResult.PASS;
        }

        if (state.getBlock() instanceof StairBlock && SitLogic.isSeatOccupied(level, pos)) {
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            return SitLogic.canAttemptSit(player, level, pos, state, hitResult)
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        return SitLogic.trySitOnBlock(serverPlayer, serverLevel, pos, state, hitResult)
                ? InteractionResult.SUCCESS
                : InteractionResult.PASS;
    }

    private static InteractionResult handleEntityUse(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        if (!(entity instanceof Player targetPlayer) || !SitLogic.canStack(player, hand, targetPlayer)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        return SitLogic.tryStackOnPlayer(serverPlayer, targetPlayer) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
