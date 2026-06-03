/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package moscow.rockstar.utility.inventory.slots;

import moscow.rockstar.utility.inventory.ItemSlot;
import net.minecraft.world.item.ItemStack;

public class OffhandSlot
extends ItemSlot {
    @Override
    public ItemStack itemStack() {
        if (OffhandSlot.mc.player == null || OffhandSlot.mc.player.getInventory() == null) {
            return ItemStack.EMPTY;
        }
        return OffhandSlot.mc.player.getOffhandItem();
    }

    @Override
    public int getIdForServer() {
        return 45;
    }
}

