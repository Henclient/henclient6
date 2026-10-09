package dev.hen.client.module.render;

import dev.hen.client.module.*;
import dev.hen.client.util.CombatUtil;
import dev.hen.client.util.Projection;
import dev.hen.client.util.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** 2D box ESP drawn on the HUD: always visible through walls. */
public class ESP extends Module {
    private final Setting range = add(Setting.number("Range", 128, 16, 256, 8));
    private final Setting names = add(Setting.bool("Names", true));
    private final Setting health = add(Setting.bool("Health", true));

    public ESP() { super("ESP", Category.RENDER); }

    @Override
    public void onRender2D(DrawContext ctx, float tp) {
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || mc.player.distanceTo(p) > range.getNum()) continue;
            Vec3d pos = p.getLerpedPos(tp);
            Box b = p.getBoundingBox().offset(pos.x - p.getX(), pos.y - p.getY(), pos.z - p.getZ());
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            boolean ok = true;
            for (int i = 0; i < 8 && ok; i++) {
                double[] s = Projection.project(new Vec3d(
                        (i & 1) == 0 ? b.minX : b.maxX, (i & 2) == 0 ? b.minY : b.maxY, (i & 4) == 0 ? b.minZ : b.maxZ));
                if (s == null) { ok = false; break; }
                minX = Math.min(minX, s[0]); maxX = Math.max(maxX, s[0]);
                minY = Math.min(minY, s[1]); maxY = Math.max(maxY, s[1]);
            }
            if (!ok) continue;
            int color = CombatUtil.isFriend(p) ? 0xFF55FF55 : 0xFFFF8A2B;
            int x1 = (int) minX, y1 = (int) minY, x2 = (int) maxX, y2 = (int) maxY;
            ctx.fill(x1, y1, x2, y1 + 1, color);
            ctx.fill(x1, y2 - 1, x2, y2, color);
            ctx.fill(x1, y1, x1 + 1, y2, color);
            ctx.fill(x2 - 1, y1, x2, y2, color);
            if (health.getBool()) {
                float frac = Math.max(0f, Math.min(1f, p.getHealth() / p.getMaxHealth()));
                int barTop = y2 - (int) ((y2 - y1) * frac);
                ctx.fill(x1 - 4, y1, x1 - 2, y2, 0xAA000000);
                ctx.fill(x1 - 4, barTop, x1 - 2, y2, 0xFF55FF55);
            }
            if (names.getBool()) {
                String label = p.getName().getString() + " " + (int) Math.ceil(p.getHealth());
                ctx.drawTextWithShadow(mc.textRenderer, label, (x1 + x2) / 2 - mc.textRenderer.getWidth(label) / 2, y1 - 11, 0xFFFFFFFF);
            }
        }
    }
}
