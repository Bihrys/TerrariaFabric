package bhw.voident.xyz.terrariafabric.npc.definition;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**

 * 类用途：功能实现类，负责该模块的核心业务逻辑。

 */

public final class NpcDefinitions {

    public static final NpcDefinition GUIDE = new GuideNpcDefinition();

    private static final List<NpcDefinition> ALL = List.of(GUIDE);
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

