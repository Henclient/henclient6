package dev.hen.client.module.visual;

import dev.hen.client.HenClient;
import dev.hen.client.module.*;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;

public class HUD extends Module {
    public HUD() { super("HUD", Category.VISUAL); setEnabled(true); }

    @Override
    public void onRender2D(DrawContext ctx, float tp) {
        var tr = mc.textRenderer;
        ctx.drawTextWithShadow(tr, "HenClient " + HenClient.VERSION, 4, 4, 0xFFFF8A2B);
        List<Module> active = HenClient.modules.modules.stream()
                .filter(m -> m.isEnabled() && m != this)
                .sorted(Comparator.comparingInt((Module m) -> tr.getWidth(m.name)).reversed())
                .toList();
        int w = ctx.getScaledWindowWidth();
        int y = 4;
        for (Module m : active) {
            int tw = tr.getWidth(m.name);
            ctx.fill(w - tw - 6, y - 1, w - 2, y + 10, 0x90000000);
            ctx.drawTextWithShadow(tr, m.name, w - tw - 4, y, 0xFFFFFFFF);
            y += 11;
        }
    }
}
