package dev.hen.client.module.combat;

import dev.hen.client.module.*;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    public AutoTotem() { super("AutoTotem", Category.COMBAT); }

    @Override
    public void onTick() {
        if (mc.currentScreen != null) return;
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
        // Screen handler slots: 9-35 main inventory, 36-44 hotbar
        for (int slot = 9; slot <= 44; slot++) {
            if (mc.player.playerScreenHandler.getSlot(slot).getStack().isOf(Items.TOTEM_OF_UNDYING)) {
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, 40,
                        SlotActionType.SWAP, mc.player); // button 40 = swap with offhand
                return;
            }
        }
    }
}
