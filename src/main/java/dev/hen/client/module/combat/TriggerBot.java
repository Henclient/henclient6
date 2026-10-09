package dev.hen.client.module.combat;

import dev.hen.client.module.*;
import dev.hen.client.util.CombatUtil;
import dev.hen.client.util.Setting;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Optional;

public class TriggerBot extends Module {
    private final Setting reach = add(Setting.number("Reach", 3.0, 1.0, 6.0, 0.1));
    private final Setting walls = add(Setting.bool("ThroughWalls", true));
    private final Setting cooldown = add(Setting.number("MinCooldown", 0.95, 0.5, 1.0, 0.05));
    private final Setting crits = add(Setting.bool("OnlyCrits", false));
    private final Setting weapon = add(Setting.bool("AutoWeapon", true));

    public TriggerBot() { super("TriggerBot", Category.COMBAT); }

    @Override
    public void onTick() {
        if (mc.currentScreen != null) return;
        Optional<PlayerEntity> t = CombatUtil.rayTarget(reach.getNum(), walls.getBool());
        if (t.isEmpty()) return;
        if (weapon.getBool()) CombatUtil.autoWeapon();
        if (mc.player.getAttackCooldownProgress(0f) < cooldown.getNum()) return;
        if (crits.getBool() && !CombatUtil.critReady()) return;
        CombatUtil.attack(t.get());
    }
}
