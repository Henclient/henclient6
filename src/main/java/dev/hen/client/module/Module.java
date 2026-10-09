package dev.hen.client.module;

import dev.hen.client.util.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public final String name;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    public KeyBinding bind; // set in HenClient; rebind in vanilla Controls menu
    private boolean enabled;

    protected Module(String name, Category category) { this.name = name; this.category = category; }

    protected Setting add(Setting s) { settings.add(s); return s; }

    public boolean isEnabled() { return enabled; }
    public void toggle() { setEnabled(!enabled); }
    public void setEnabled(boolean on) {
        if (on == enabled) return;
        enabled = on;
        if (on) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onRender2D(DrawContext ctx, float tickProgress) {}
}
