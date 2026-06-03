/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.Player
 *  net.minecraft.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.screen.slot.ContainerInput
 */
package moscow.rockstar.utility.inventory;

import java.util.function.Predicate;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.InventoryUtility;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerInput;

public abstract class ItemSlot
implements IMinecraft {
    public abstract ItemStack itemStack();

    public abstract int getIdForServer();

    public int syncId() {
        if (ItemSlot.mc.player == null || ItemSlot.mc.player.containerMenu == null) {
            return 0;
        }
        return ItemSlot.mc.player.containerMenu.containerId;
    }

    public Item item() {
        return this.itemStack().getItem();
    }

    public boolean isEmpty() {
        return this.itemStack().isEmpty();
    }

    public boolean contains(Item item) {
        return this.itemStack().getItem() == item;
    }

    public boolean matches(Predicate<ItemStack> predicate) {
        return predicate.test(this.itemStack());
    }

    public void swapTo(ItemSlot newSlot) {
        InventoryUtility.moveItem(this, newSlot);
    }

    public void moveToOffHand() {
        InventoryUtility.moveToOffHand(this);
    }

    public void click() {
        if (ItemSlot.mc.gameMode == null) {
            return;
        }
        ItemSlot.mc.gameMode.handleContainerInput(this.syncId(), this.getIdForServer(), 0, ContainerInput.PICKUP, ItemSlot.mc.player);
    }
}

