package dev.hen.client.config;

import com.google.gson.*;
import dev.hen.client.HenClient;
import dev.hen.client.module.Module;
import dev.hen.client.util.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/** Named JSON profiles in .minecraft/config/henclient/<name>.json */
public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Set<String> friends = new LinkedHashSet<>();

    private static Path dir() {
        Path d = FabricLoader.getInstance().getConfigDir().resolve("henclient");
        try { Files.createDirectories(d); } catch (IOException ignored) {}
        return d;
    }

    private static String clean(String name) { return name.replaceAll("[^a-zA-Z0-9_-]", ""); }

    public static boolean save(String name) {
        JsonObject root = new JsonObject();
        JsonObject mods = new JsonObject();
        for (Module m : HenClient.modules.modules) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.isEnabled());
            JsonObject s = new JsonObject();
            for (Setting st : m.settings) s.addProperty(st.name, st.value);
            o.add("settings", s);
            mods.add(m.name, o);
        }
        root.add("modules", mods);
        JsonArray f = new JsonArray();
        friends.forEach(f::add);
        root.add("friends", f);
        try {
            Files.writeString(dir().resolve(clean(name) + ".json"), GSON.toJson(root));
            return true;
        } catch (IOException e) { return false; }
    }

    public static boolean load(String name) {
        Path p = dir().resolve(clean(name) + ".json");
        if (!Files.exists(p)) return false;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            JsonObject mods = root.getAsJsonObject("modules");
            for (Module m : HenClient.modules.modules) {
                if (mods == null || !mods.has(m.name)) continue;
                JsonObject o = mods.getAsJsonObject(m.name);
                if (o.has("settings")) {
                    JsonObject s = o.getAsJsonObject("settings");
                    for (Setting st : m.settings) if (s.has(st.name)) st.value = s.get(st.name).getAsDouble();
                }
                if (o.has("enabled")) m.setEnabled(o.get("enabled").getAsBoolean());
            }
            friends.clear();
            if (root.has("friends")) root.getAsJsonArray("friends").forEach(e -> friends.add(e.getAsString().toLowerCase()));
            return true;
        } catch (Exception e) { return false; }
    }

    public static List<String> list() {
        List<String> out = new ArrayList<>();
        try (Stream<Path> s = Files.list(dir())) {
            s.filter(x -> x.toString().endsWith(".json"))
             .forEach(x -> out.add(x.getFileName().toString().replace(".json", "")));
        } catch (IOException ignored) {}
        return out;
    }
}
