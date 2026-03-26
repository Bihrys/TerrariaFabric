package bhw.voident.xyz.terrariafabric.command;

import bhw.voident.xyz.terrariafabric.npc.home.HouseCheckResult;
import bhw.voident.xyz.terrariafabric.npc.home.HouseDetector;
import bhw.voident.xyz.terrariafabric.npc.home.HouseMessages;
import bhw.voident.xyz.terrariafabric.npc.home.HousingRegistry;
import bhw.voident.xyz.terrariafabric.npc.definition.NpcDefinitions;
import bhw.voident.xyz.terrariafabric.npc.spawn.NpcResidenceManager;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**

 * 类用途：/guide 命令入口，绑定向导入住当前房屋。

 */

public final class GuideCommand {

    private GuideCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("向导")
                        .executes(context -> execute(context.getSource()))));
    }

    private static int execute(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("message.terrariafabric.guide.only_player"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        HouseCheckResult result = HouseDetector.check(player);
        if (!result.hasValidStructure()) {
            player.sendSystemMessage(Component.translatable("message.terrariafabric.house.invalid"));
            return 0;
        }
        if (!result.missing().isEmpty()) {
            player.sendSystemMessage(HouseMessages.buildMissingMessage(result.missing()));
            return 0;
        }
        if (result.room() == null) {
            player.sendSystemMessage(Component.translatable("message.terrariafabric.house.invalid"));
            return 0;
        }

        HousingRegistry registry = HousingRegistry.get(level);
        HousingRegistry.RoomRecord room = registry.syncRoom(result.room(), level.getGameTime());
        String occupant = room.occupantId();
        if (occupant != null && !NpcDefinitions.GUIDE.id().equals(occupant)) {
            player.sendSystemMessage(Component.translatable("message.terrariafabric.house.occupied"));
            return 0;
        }

        if (NpcResidenceManager.assignRoom(level, NpcDefinitions.GUIDE, room, true, true)) {
            player.sendSystemMessage(Component.translatable("message.terrariafabric.guide.bound"));
            return Command.SINGLE_SUCCESS;
        }

        player.sendSystemMessage(Component.translatable("message.terrariafabric.guide.spawn_failed"));
        return Command.SINGLE_SUCCESS;
    }
}

