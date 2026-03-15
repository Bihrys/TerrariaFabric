package bhw.voident.xyz.terrariafabric.command;

import bhw.voident.xyz.terrariafabric.entity.GuideEntity;
import bhw.voident.xyz.terrariafabric.npc.home.HouseCheckResult;
import bhw.voident.xyz.terrariafabric.npc.home.HouseDetector;
import bhw.voident.xyz.terrariafabric.npc.home.HouseMessages;
import bhw.voident.xyz.terrariafabric.npc.home.HousingData;
import bhw.voident.xyz.terrariafabric.npc.spawn.GuideSpawner;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

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

        HousingData data = HousingData.get(level);
        HousingData.RoomRecord room = data.getOrCreate(result.room());
        String occupant = room.getOccupantId();
        if (occupant != null && !HousingData.NPC_GUIDE.equals(occupant)) {
            player.sendSystemMessage(Component.translatable("message.terrariafabric.house.occupied"));
            return 0;
        }

        data.clearOccupant(HousingData.NPC_GUIDE);
        data.setOccupant(room, HousingData.NPC_GUIDE, true);

        GuideEntity guide = GuideSpawner.findGuide(level);
        if (guide != null) {
            GuideSpawner.assignGuideToRoom(guide, room);
            player.sendSystemMessage(Component.translatable("message.terrariafabric.guide.bound"));
            return Command.SINGLE_SUCCESS;
        }

        if (level.isDay()) {
            GuideEntity spawned = GuideSpawner.spawnGuideInRoom(level, room);
            if (spawned != null) {
                player.sendSystemMessage(Component.translatable("message.terrariafabric.guide.bound"));
                return Command.SINGLE_SUCCESS;
            }
        }

        player.sendSystemMessage(Component.translatable("message.terrariafabric.guide.wait_day"));
        return Command.SINGLE_SUCCESS;
    }
}
