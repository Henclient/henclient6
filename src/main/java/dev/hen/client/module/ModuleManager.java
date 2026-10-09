package dev.hen.client.module;

import dev.hen.client.module.combat.*;
import dev.hen.client.module.movement.*;
import dev.hen.client.module.render.*;
import dev.hen.client.module.visual.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        modules.add(new KillAura());
        modules.add(new TriggerBot());
        modules.add(new AutoTotem());
        modules.add(new Sprint());
        modules.add(new ESP());
        modules.add(new Tracers());
        modules.add(new Fullbright());
        modules.add(new HUD());
    }

    public Module get(String name) {
        for (Module m : modules) if (m.name.equalsIgnoreCase(name)) return m;
        return null;
    }

    public List<Module> byCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : modules) if (m.category == c) out.add(m);
        return out;
    }

    public void tick() {
        if (Module.mc.player == null || Module.mc.world == null) return;
        for (Module m : modules) {
            if (m.bind != null) while (m.bind.wasPressed()) m.toggle();
            if (m.isEnabled()) m.onTick();
        }
    }
}
