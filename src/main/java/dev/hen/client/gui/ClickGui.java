package dev.hen.client.gui;

import dev.hen.client.HenClient;
import dev.hen.client.module.Category;
import dev.hen.client.module.Module;
import dev.hen.client.util.Setting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

/** Left click = toggle module / toggle bool / next; right click = expand settings / previous. */
public class ClickGui extends Screen {
    private static final int COL_W = 120, ROW_H = 14;
    private final Set<Module> expanded = new HashSet<>();

    public ClickGui() { super(Text.literal("HenClient")); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        int x = 10;
        for (Category c : Category.values()) {
            int y = 10;
            ctx.fill(x, y, x + COL_W, y + ROW_H, 0xFFFF8A2B);
            ctx.drawTextWithShadow(textRenderer, c.name(), x + 4, y + 3, 0xFFFFFFFF);
            y += ROW_H;
            for (Module m : HenClient.modules.byCategory(c)) {
                ctx.fill(x, y, x + COL_W, y + ROW_H, m.isEnabled() ? 0xE0803F10 : 0xE0141414);
                ctx.drawTextWithShadow(textRenderer, m.name, x + 4, y + 3, 0xFFFFFFFF);
                y += ROW_H;
                if (expanded.contains(m)) {
                    for (Setting s : m.settings) {
                        ctx.fill(x, y, x + COL_W, y + ROW_H, 0xE0262626);
                        ctx.drawTextWithShadow(textRenderer, s.name + ": " + s.display(), x + 8, y + 3, 0xFFCCCCCC);
                        y += ROW_H;
                    }
                }
            }
            x += COL_W + 8;
        }
        ctx.drawTextWithShadow(textRenderer, "Commands: /hen save|load|list|friend|toggle   Keybinds: Options > Controls > HenClient",
                10, height - 14, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int button = click.button();
        int x = 10;
        for (Category c : Category.values()) {
            int y = 10 + ROW_H;
            for (Module m : HenClient.modules.byCategory(c)) {
                if (hit(mx, my, x, y)) {
                    if (button == 0) m.toggle();
                    else if (button == 1) { if (!expanded.remove(m)) expanded.add(m); }
                    return true;
                }
                y += ROW_H;
                if (expanded.contains(m)) {
                    for (Setting s : m.settings) {
                        if (hit(mx, my, x, y)) {
                            if (s.isBool) { if (button == 0) s.toggle(); }
                            else s.adjust(button == 0 ? 1 : -1);
                            return true;
                        }
                        y += ROW_H;
                    }
                }
            }
            x += COL_W + 8;
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean hit(double mx, double my, int x, int y) {
        return mx >= x && mx <= x + COL_W && my >= y && my < y + ROW_H;
    }

    @Override public boolean shouldPause() { return false; }
}
