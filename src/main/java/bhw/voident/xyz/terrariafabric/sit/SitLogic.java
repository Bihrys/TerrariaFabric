package bhw.voident.xyz.terrariafabric.sit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class SitLogic {
    private static final double SEAT_SEARCH_RADIUS = 0.35D;
    private static final Set<UUID> ACTIVE_SNEAKS = new HashSet<>();

    private SitLogic() {
    }

    public static boolean canSitOn(BlockState state) {
        if (state.getBlock() instanceof SlabBlock) {
            return state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
        }

        if (state.getBlock() instanceof StairBlock) {
            return state.getValue(StairBlock.HALF) == Half.BOTTOM;
        }

        return false;
    }

    public static boolean canStack(Player rider, InteractionHand hand, Player target) {
        return rider.isShiftKeyDown()
                && hand == InteractionHand.MAIN_HAND
                && rider.getItemInHand(hand).isEmpty()
                && rider != target
                && target.isAlive()
                && rider.getVehicle() == null;
    }

    public static boolean trySitOnBlock(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state, BlockHitResult hitResult) {
        if (!canAttemptSit(player, level, pos, state, hitResult)) {
            return false;
        }

        SitSeatEntity seat = findSeat(level, pos);
        if (seat == null) {
            seat = new SitSeatEntity(level);
            seat.moveTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, getSeatYaw(state, player), 0.0F);
            SitUtil.addSitEntity(level, pos, seat, player.position());
            level.addFreshEntity(seat);
        } else if (!seat.getPassengers().isEmpty()) {
            return false;
        } else {
            SitUtil.addSitEntity(level, pos, seat, player.position());
        }

        boolean mounted = player.startRiding(seat, true);
        if (!mounted) {
            seat.discard();
        }

        return mounted;
    }

    public static boolean canAttemptSit(Player player, Level level, BlockPos pos, BlockState state, BlockHitResult hitResult) {
        if (!canSitOn(state)) {
            return false;
        }

        if (hitResult.getDirection() != Direction.UP) {
            return false;
        }

        if (player.isShiftKeyDown() || SitUtil.isPlayerSitting(player)) {
            return false;
        }

        if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            return false;
        }

        if (!level.mayInteract(player, pos)) {
            return false;
        }

        if (!isPlayerInRange(player, pos)) {
            return false;
        }

        return !isSeatOccupied(level, pos);
    }

    public static boolean tryStackOnPlayer(ServerPlayer rider, Player target) {
        if (!target.isAlive()) {
            return false;
        }

        if (target.hasPassenger(rider) || rider.hasPassenger(target) || rider.getRootVehicle() == target.getRootVehicle()) {
            return false;
        }

        return rider.startRiding(target, true);
    }

    public static void handleSneak(ServerPlayer player) {
        UUID playerId = player.getUUID();

        if (!player.isShiftKeyDown()) {
            ACTIVE_SNEAKS.remove(playerId);
            return;
        }

        if (!ACTIVE_SNEAKS.add(playerId)) {
            return;
        }

        Entity passenger = player.getFirstPassenger();
        if (passenger instanceof Player rider) {
            rider.stopRiding();
            rider.setShiftKeyDown(false);
        }
    }

    public static void tickServerPlayer(ServerPlayer player) {
        handleSneak(player);
        updateRiderPose(player);
    }

    public static void cleanupSeats(ServerLevel level) {
        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        List<? extends SitSeatEntity> seats = level.getEntities(EntityTypeTest.forClass(SitSeatEntity.class), entity -> true);
        for (SitSeatEntity seat : seats) {
            if (seat.getPassengers().isEmpty() || !isValidSeatBlock(level, seat.blockPosition())) {
                seat.discard();
            }
        }
    }

    public static boolean shouldShiftBeHandledByMod(Player player) {
        return !player.getPassengers().isEmpty();
    }

    public static boolean isModRide(Entity vehicle) {
        return isSeatEntity(vehicle) || vehicle instanceof Player;
    }

    public static boolean isSeatEntity(Entity entity) {
        return entity instanceof SitSeatEntity;
    }

    public static boolean isSeatOccupied(Level level, BlockPos pos) {
        SitSeatEntity seat = SitUtil.getSitEntity(level, pos);
        if (seat != null) {
            return !seat.getPassengers().isEmpty();
        }

        seat = findSeat(level, pos);
        return seat != null && !seat.getPassengers().isEmpty();
    }

    public static void updateRiderPose(Player rider) {
        Entity vehicle = rider.getVehicle();
        if (vehicle == null) {
            return;
        }

        if (vehicle instanceof Player) {
            alignBodyToHead(rider);
            return;
        }

        if (!isSeatEntity(vehicle)) {
            return;
        }

        BlockState state = rider.level().getBlockState(vehicle.blockPosition());
        if (state.getBlock() instanceof StairBlock) {
            rider.setYBodyRot(getStairSeatYaw(state));
            return;
        }

        alignBodyToHead(rider);
    }

    private static void alignBodyToHead(Player rider) {
        rider.setYBodyRot(rider.getYHeadRot());
    }

    private static float getSeatYaw(BlockState state, Player player) {
        if (state.getBlock() instanceof StairBlock) {
            return getStairSeatYaw(state);
        }

        return player.getYRot();
    }

    private static float getStairSeatYaw(BlockState state) {
        Direction facing = state.getValue(StairBlock.FACING);
        return facing.getOpposite().toYRot();
    }

    public static SitSeatEntity findSeat(Level level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB searchBox = new AABB(
                center.x - SEAT_SEARCH_RADIUS,
                center.y - 0.6D,
                center.z - SEAT_SEARCH_RADIUS,
                center.x + SEAT_SEARCH_RADIUS,
                center.y + 0.6D,
                center.z + SEAT_SEARCH_RADIUS
        );
        List<SitSeatEntity> seats = level.getEntitiesOfClass(SitSeatEntity.class, searchBox, entity -> true);
        return seats.isEmpty() ? null : seats.get(0);
    }

    private static boolean isValidSeatBlock(Level level, BlockPos pos) {
        return canSitOn(level.getBlockState(pos));
    }

    private static boolean isPlayerInRange(Player player, BlockPos pos) {
        int blockReachDistance = SitConfig.get().getBlockReachDistance();
        BlockPos playerPos = player.blockPosition();

        if (blockReachDistance == 0) {
            return playerPos.getY() - pos.getY() <= 1
                    && playerPos.getX() == pos.getX()
                    && playerPos.getZ() == pos.getZ();
        }

        return Math.abs(playerPos.getX() - pos.getX()) <= blockReachDistance
                && Math.abs(playerPos.getY() - pos.getY()) <= blockReachDistance
                && Math.abs(playerPos.getZ() - pos.getZ()) <= blockReachDistance;
    }
}
