package bhw.voident.xyz.terrariafabric.command;

import bhw.voident.xyz.terrariafabric.npc.home.HouseCheckResult;
import bhw.voident.xyz.terrariafabric.npc.home.HouseDetector;
import bhw.voident.xyz.terrariafabric.npc.home.HouseMessages;
import bhw.voident.xyz.terrariafabric.npc.home.HousingRegistry;
import bhw.voident.xyz.terrariafabric.npc.spawn.NpcResidenceManager;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class HouseCommand {

    private HouseCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("checkhouse")
                        .executes(context -> execute(context.getSource()))));
    }

    private static int execute(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("message.terrariafabric.house.only_player"));
            return 0;
        }

        checkHouseAndReport(player);
        return Command.SINGLE_SUCCESS;
    }

    private static void checkHouseAndReport(ServerPlayer player) {
        HouseCheckResult result = HouseDetector.check(player);
        if (!result.hasValidStructure()) {
            player.sendSystemMessage(Component.translatable("message.terrariafabric.house.invalid"));
            return;
        }
        if (!result.missing().isEmpty()) {
            player.sendSystemMessage(HouseMessages.buildMissingMessage(result.missing()));
            return;
        }
        if (result.room() == null) {
            player.sendSystemMessage(Component.translatable("message.terrariafabric.house.invalid"));
            return;
        }

        HousingRegistry registry = HousingRegistry.get(player.serverLevel());
        HousingRegistry.RoomRecord room = registry.syncRoom(result.room(), player.serverLevel().getGameTime());
        if (room.isOccupied()) {
            if (!NpcResidenceManager.reconcileRoom(player.serverLevel(), room)) {
                player.sendSystemMessage(Component.translatable("message.terrariafabric.house.suitable"));
                return;
            }
            player.sendSystemMessage(Component.translatable("message.terrariafabric.house.occupied"));
            return;
        }
        player.sendSystemMessage(Component.translatable("message.terrariafabric.house.suitable"));
    }
}
