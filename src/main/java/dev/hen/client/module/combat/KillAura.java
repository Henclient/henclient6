package dev.hen.client.module.combat;

import dev.hen.client.module.*;
import dev.hen.client.util.CombatUtil;
import dev.hen.client.util.Setting;
import net.minecraft.entity.player.PlayerEntity;

public class KillAura extends Module {
    private final Setting range = add(Setting.number("Range", 3.0, 1.0, 6.0, 0.1));
    private final Setting walls = add(Setting.bool("ThroughWalls", false));
    private final Setting sort = add(Setting.mode("Sort", 0, "Distance", "Health"));
    private final Setting cooldown = add(Setting.number("MinCooldown", 0.95, 0.5, 1.0, 0.05));
    private final Setting rotate = add(Setting.bool("Rotate", true));
    private final Setting crits = add(Setting.bool("OnlyCrits", false));
    private final Setting weapon = add(Setting.bool("AutoWeapon", true));

    public KillAura() { super("KillAura", Category.COMBAT); }

    @Override
    public void onTick() {
        if (mc.currentScreen != null) return;
        PlayerEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (PlayerEntity p : CombatUtil.players()) {
            double d = mc.player.distanceTo(p);
            if (d > range.getNum()) continue;
            if (!walls.getBool() && !CombatUtil.hasLineOfSight(mc.player.getEyePos(), p.getEyePos())) continue;
            double score = sort.getMode() == 1 ? p.getHealth() + p.getAbsorptionAmount() : d;
            if (score < bestScore) { bestScore = score; best = p; }
        }
        if (best == null) return;
        if (weapon.getBool()) CombatUtil.autoWeapon();
        if (rotate.getBool()) {
            double dx = best.getX() - mc.player.getX();
            double dz = best.getZ() - mc.player.getZ();
            double dy = best.getEyeY() - mc.player.getEyeY();
            mc.player.setYaw((float) Math.toDegrees(Math.atan2(dz, dx)) - 90f);
            mc.player.setPitch((float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
        }
        if (mc.player.getAttackCooldownProgress(0f) < cooldown.getNum()) return;
        if (crits.getBool() && !CombatUtil.critReady()) return;
        CombatUtil.attack(best);
    }
}
