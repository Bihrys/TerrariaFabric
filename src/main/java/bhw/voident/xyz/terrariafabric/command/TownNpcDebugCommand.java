package bhw.voident.xyz.terrariafabric.command;

import bhw.voident.xyz.terrariafabric.npc.definition.NpcDefinition;
import bhw.voident.xyz.terrariafabric.npc.definition.NpcDefinitions;
import bhw.voident.xyz.terrariafabric.npc.home.HousingRegistry;
import bhw.voident.xyz.terrariafabric.npc.spawn.TownNpcSpawnJudge;
import bhw.voident.xyz.terrariafabric.npc.state.NpcWorldState;
import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.ArrayList;
import java.util.List;

/** 类用途：/hushi 和 /shangran 诊断命令，输出 Town NPC 当前缺失的生成或复活条件。 */
public final class TownNpcDebugCommand {

    private TownNpcDebugCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("hushi")
                    .executes(context -> execute(context.getSource(), NpcDefinitions.NURSE, "护士", "hushi")));
            dispatcher.register(Commands.literal("shangran")
                    .executes(context -> execute(context.getSource(), NpcDefinitions.MERCHANT, "商人", "shangran")));
            dispatcher.register(Commands.literal("shangren")
                    .executes(context -> execute(context.getSource(), NpcDefinitions.MERCHANT, "商人", "shangren")));
        });
    }

    private static int execute(CommandSourceStack source, NpcDefinition definition, String label, String commandName) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("只有玩家可以使用 /" + commandName + "。"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        NpcWorldState.NpcRecord record = NpcWorldState.get(level).getOrCreate(definition.id());
        HousingRegistry registry = HousingRegistry.get(level);
        boolean hasEntity = !level.getEntities(EntityTypeTest.forClass(definition.entityClass()), entity -> true).isEmpty();
        boolean hasRoomForThisNpc = registry.findAvailableRoomFor(level, definition) != null;
        TownNpcSpawnJudge.Decision decision = TownNpcSpawnJudge.evaluate(level, definition, record);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("[" + label + "诊断]"));
        lines.add(Component.literal("白天: " + (level.isDay() ? "是" : "否")));
        lines.add(Component.literal("实体已存在: " + (hasEntity ? "是" : "否")));
        lines.add(Component.literal("spawnedOnce: " + record.spawnedOnce()));
        lines.add(Component.literal("pendingRespawn: " + record.pendingRespawn()));
        lines.add(Component.literal("总可用房间: " + registry.countAvailableRooms()));
        lines.add(Component.literal("有可供该 NPC 使用的房间: " + (hasRoomForThisNpc ? "是" : "否")));
        lines.add(Component.literal("当前调度判断: " + describeDecision(decision)));
        definition.appendSpawnDiagnostics(level, lines);

        for (Component line : lines) {
            player.sendSystemMessage(line);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static String describeDecision(TownNpcSpawnJudge.Decision decision) {
        return switch (decision) {
            case WAITING -> "等待";
            case RESPAWN -> "重生";
            case NEW_ARRIVAL -> "首次入住";
        };
    }
}
