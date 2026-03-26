package bhw.voident.xyz.terrariafabric.npc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.RandomSource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 * 类用途：读取 NPC 名字池并提供随机名。

 */

public final class NpcNames {

    private static final String RESOURCE_PATH = "data/terrariafabric/npc/names.json";
    private static volatile Map<String, List<String>> cached;

    private NpcNames() {
    }

    public static String pick(String npcId, RandomSource random, String[] fallback) {
        List<String> names = getNames(npcId);
        if (!names.isEmpty()) {
            return names.get(random.nextInt(names.size()));
        }
        if (fallback != null && fallback.length > 0) {
            return fallback[random.nextInt(fallback.length)];
        }
        return npcId;
    }

    public static List<String> getNames(String npcId) {
        if (npcId == null) {
            return Collections.emptyList();
        }
        Map<String, List<String>> map = load();
        List<String> names = map.get(npcId.toLowerCase());
        if (names == null) {
            return Collections.emptyList();
        }
        return names;
    }

    private static Map<String, List<String>> load() {
        Map<String, List<String>> local = cached;
        if (local != null) {
            return local;
        }
        synchronized (NpcNames.class) {
            if (cached != null) {
                return cached;
            }
            cached = readResource();
            return cached;
        }
    }

    private static Map<String, List<String>> readResource() {
        InputStream stream = NpcNames.class.getClassLoader().getResourceAsStream(RESOURCE_PATH);
        if (stream == null) {
            return Collections.emptyMap();
        }
        try (stream) {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            JsonElement rootElement = JsonParser.parseString(json);
            if (!rootElement.isJsonObject()) {
                return Collections.emptyMap();
            }
            JsonObject root = rootElement.getAsJsonObject();
            Map<String, List<String>> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (!entry.getValue().isJsonArray()) {
                    continue;
                }
                JsonArray array = entry.getValue().getAsJsonArray();
                List<String> names = new ArrayList<>();
                for (JsonElement element : array) {
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                        String name = element.getAsString().trim();
                        if (!name.isEmpty()) {
                            names.add(name);
                        }
                    }
                }
                map.put(entry.getKey().toLowerCase(), List.copyOf(names));
            }
            return map;
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }
}

