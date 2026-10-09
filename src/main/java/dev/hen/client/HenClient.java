package dev.hen.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.hen.client.config.Config;
import dev.hen.client.gui.ClickGui;
import dev.hen.client.module.Module;
import dev.hen.client.module.ModuleManager;
import dev.hen.client.util.Projection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class HenClient implements ClientModInitializer {
    public static final String VERSION = "0.2.0";
    public static ModuleManager modules;
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();

        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("henclient", "main"));
        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.henclient.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        // One unbound key per module - bind them in Options > Controls
        for (Module m : modules.modules) {
            m.bind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.henclient." + m.name.toLowerCase(), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, cat));
        }

        Config.load("default");
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> Config.save("default"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openGui.wasPressed()) mc.setScreen(new ClickGui());
            modules.tick();
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, Identifier.of("henclient", "overlay"), (ctx, tick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
            float tp = tick.getTickProgress(false);
            Projection.begin(mc, tp, ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
            for (Module m : modules.modules) if (m.isEnabled()) m.onRender2D(ctx, tp);
        });

        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("hen")
                .then(ClientCommandManager.literal("save").then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes(c -> {
                        String n = StringArgumentType.getString(c, "name");
                        c.getSource().sendFeedback(Text.literal(Config.save(n) ? "Saved config " + n : "Failed to save " + n));
                        return 1;
                    })))
                .then(ClientCommandManager.literal("load").then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes(c -> {
                        String n = StringArgumentType.getString(c, "name");
                        c.getSource().sendFeedback(Text.literal(Config.load(n) ? "Loaded config " + n : "No config named " + n));
                        return 1;
                    })))
                .then(ClientCommandManager.literal("list").executes(c -> {
                    c.getSource().sendFeedback(Text.literal("Configs: " + String.join(", ", Config.list())));
                    return 1;
                }))
                .then(ClientCommandManager.literal("toggle").then(ClientCommandManager.argument("module", StringArgumentType.word())
                    .executes(c -> {
                        Module m = modules.get(StringArgumentType.getString(c, "module"));
                        if (m == null) c.getSource().sendFeedback(Text.literal("Unknown module"));
                        else { m.toggle(); c.getSource().sendFeedback(Text.literal(m.name + (m.isEnabled() ? " ON" : " OFF"))); }
                        return 1;
                    })))
                .then(ClientCommandManager.literal("friend")
                    .then(ClientCommandManager.literal("add").then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(c -> {
                            Config.friends.add(StringArgumentType.getString(c, "name").toLowerCase());
                            c.getSource().sendFeedback(Text.literal("Friend added"));
                            return 1;
                        })))
                    .then(ClientCommandManager.literal("remove").then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(c -> {
                            Config.friends.remove(StringArgumentType.getString(c, "name").toLowerCase());
                            c.getSource().sendFeedback(Text.literal("Friend removed"));
                            return 1;
                        }))))
        ));
    }
}
