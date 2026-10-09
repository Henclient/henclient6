package dev.hen.client.module.movement;

import dev.hen.client.module.*;

public class Sprint extends Module {
    public Sprint() { super("Sprint", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.options.forwardKey.isPressed() && !mc.player.horizontalCollision && !mc.player.isSneaking()) {
            mc.player.setSprinting(true);
        }
    }
}
