package dev.hen.client.module.render;

import dev.hen.client.module.*;
import dev.hen.client.util.CombatUtil;
import dev.hen.client.util.Projection;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Tracers extends Module {
    public Tracers() { super("Tracers", Category.RENDER); }

    @Override
    public void onRender2D(DrawContext ctx, float tp) {
        int ox = Projection.width() / 2, oy = Projection.height();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            double[] s = Projection.project(p.getLerpedPos(tp).add(0, p.getHeight() / 2, 0));
            if (s == null) continue;
            int color = CombatUtil.isFriend(p) ? 0xFF55FF55 : 0xFF33CCFF;
            line(ctx, ox, oy, (int) s[0], (int) s[1], color);
        }
    }

    private static void line(DrawContext ctx, int x0, int y0, int x1, int y1, int color) {
        int dx = x1 - x0, dy = y1 - y0;
        int steps = Math.min(2000, Math.max(Math.abs(dx), Math.abs(dy)));
        if (steps == 0) return;
        for (int i = 0; i <= steps; i++) {
            int x = x0 + dx * i / steps, y = y0 + dy * i / steps;
            ctx.fill(x, y, x + 1, y + 1, color);
        }
    }
}
