package bhw.voident.xyz.terrariafabric.npc.definition;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 类用途：集中维护已接入的城镇 NPC 定义顺序。 */
public final class NpcDefinitions {

    public static final NpcDefinition GUIDE = new GuideNpcDefinition();
    public static final NpcDefinition MERCHANT = new MerchantNpcDefinition();
    public static final NpcDefinition NURSE = new NurseNpcDefinition();

    private static final List<NpcDefinition> ALL = List.of(GUIDE, MERCHANT, NURSE);
    private static final Map<String, NpcDefinition> BY_ID = ALL.stream()
            .collect(Collectors.toUnmodifiableMap(NpcDefinition::id, Function.identity()));

    private NpcDefinitions() {
    }

    public static List<NpcDefinition> all() {
        return ALL;
    }

    public static NpcDefinition byId(String id) {
        if (id == null) {
            return null;
        }
        return BY_ID.get(id);
    }
}
