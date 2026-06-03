/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.player.Player
 *  net.minecraft.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ServerboundContainerClosePacket
 *  net.minecraft.network.packet.c2s.play.ServerboundSetCarriedItemPacket
 *  net.minecraft.screen.slot.ContainerInput
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.utility.inventory;

import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.impl.HotbarSlotsGroup;
import moscow.rockstar.utility.inventory.slots.ArmorSlot;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.inventory.ContainerInput;
import org.jetbrains.annotations.NotNull;

public final class InventoryUtility
implements IMinecraft {
    public static HotbarSlot getHotbarSlot(int slotId) {
        return new HotbarSlot(slotId);
    }

    public static InventorySlot getInventorySlot(int slotId) {
        return new InventorySlot(slotId);
    }

    public static ArmorSlot getArmorSlot(int armorIndex) {
        return new ArmorSlot(armorIndex);
    }

    public static ArmorSlot getHelmetSlot() {
        return InventoryUtility.getArmorSlot(3);
    }

    public static ArmorSlot getChestplateSlot() {
        return InventoryUtility.getArmorSlot(2);
    }

    public static ArmorSlot getLeggingsSlot() {
        return InventoryUtility.getArmorSlot(1);
    }

    public static ArmorSlot getBootsSlot() {
        return InventoryUtility.getArmorSlot(0);
    }

    public static OffhandSlot getOffHandSlot() {
        return new OffhandSlot();
    }

    public static boolean hasItemInOffHand(Item item) {
        return InventoryUtility.getOffHandSlot().contains(item);
    }

    public static boolean offHandItemMatches(Predicate<ItemStack> predicate) {
        return InventoryUtility.getOffHandSlot().matches(predicate);
    }

    public static boolean isOffHandEmpty() {
        return InventoryUtility.getOffHandSlot().isEmpty();
    }

    public static void moveItem(ItemSlot from, ItemSlot to) {
        if (mc.getConnection() == null) {
            return;
        }
        from.click();
        to.click();
        if (!to.isEmpty()) {
            from.click();
        }
    }

    public static void quickMove(int from) {
        if (mc.getConnection() == null) {
            return;
        }
        InventoryUtility.mc.gameMode.handleContainerInput(InventoryUtility.mc.player.containerMenu.containerId, from, 0, ContainerInput.QUICK_MOVE, (Player)InventoryUtility.mc.player);
    }

    public static void moveItem(int from, int to) {
        InventoryUtility.moveItem(from, to, false);
    }

    public static void moveItem(int from, int to, boolean back) {
        if (mc.getConnection() == null) {
            return;
        }
        InventoryUtility.mc.gameMode.handleContainerInput(InventoryUtility.mc.player.containerMenu.containerId, from, 0, ContainerInput.PICKUP, (Player)InventoryUtility.mc.player);
        InventoryUtility.mc.gameMode.handleContainerInput(InventoryUtility.mc.player.containerMenu.containerId, to, 0, ContainerInput.PICKUP, (Player)InventoryUtility.mc.player);
        if (back) {
            InventoryUtility.mc.gameMode.handleContainerInput(InventoryUtility.mc.player.containerMenu.containerId, from, 0, ContainerInput.PICKUP, (Player)InventoryUtility.mc.player);
        }
    }

    public static void moveHalf(int from, int to) {
        if (mc.getConnection() == null) {
            return;
        }
        InventoryUtility.mc.gameMode.handleContainerInput(InventoryUtility.mc.player.containerMenu.containerId, from, 1, ContainerInput.PICKUP, (Player)InventoryUtility.mc.player);
        InventoryUtility.mc.gameMode.handleContainerInput(InventoryUtility.mc.player.containerMenu.containerId, to, 0, ContainerInput.PICKUP, (Player)InventoryUtility.mc.player);
    }

    public static void swapOneItem(int from, int to) {
        if (mc.getConnection() == null) {
            return;
        }
        InventoryUtility.mc.gameMode.handleContainerInput(InventoryUtility.mc.player.containerMenu.containerId, from, 0, ContainerInput.PICKUP, (Player)InventoryUtility.mc.player);
        InventoryUtility.mc.gameMode.handleContainerInput(InventoryUtility.mc.player.containerMenu.containerId, to, 1, ContainerInput.PICKUP, (Player)InventoryUtility.mc.player);
        InventoryUtility.mc.gameMode.handleContainerInput(InventoryUtility.mc.player.containerMenu.containerId, from, 0, ContainerInput.PICKUP, (Player)InventoryUtility.mc.player);
    }

    public static void hotbarSwap(int from, int to) {
        if (mc.getConnection() == null) {
            return;
        }
        InventoryUtility.mc.gameMode.handleContainerInput(InventoryUtility.mc.player.containerMenu.containerId, from, to, ContainerInput.SWAP, (Player)InventoryUtility.mc.player);
    }

    public static boolean moveToHotbar(ItemSlot fromSlot, int hotbarSlotId) {
        HotbarSlot hotbarSlot = InventoryUtility.getHotbarSlot(hotbarSlotId);
        InventoryUtility.moveItem(fromSlot, hotbarSlot);
        return true;
    }

    public static boolean moveToArmor(ItemSlot fromSlot, int armorIndex) {
        ArmorSlot armorSlot = InventoryUtility.getArmorSlot(armorIndex);
        InventoryUtility.moveItem(fromSlot, armorSlot);
        return true;
    }

    public static void moveToOffHand(ItemSlot fromSlot) {
        OffhandSlot offHandSlot = InventoryUtility.getOffHandSlot();
        InventoryUtility.moveItem(fromSlot, offHandSlot);
    }

    @NotNull
    public static HotbarSlot getCurrentHotbarSlot() {
        if (InventoryUtility.mc.player == null || InventoryUtility.mc.player.getInventory() == null) {
            return new HotbarSlot(0);
        }
        return InventoryUtility.getHotbarSlot(InventoryUtility.mc.player.getInventory().getSelectedSlot());
    }

    public static void selectHotbarSlot(int slotId) {
        if (InventoryUtility.mc.player == null || InventoryUtility.mc.player.getInventory() == null || mc.getConnection() == null) {
            return;
        }
        if (slotId < 0 || slotId > 8) {
            throw new IllegalArgumentException("Hotbar slot ID must be between 0 and 8");
        }
        InventoryUtility.mc.player.getInventory().setSelectedSlot(slotId);
        mc.getConnection().send((Packet)new ServerboundSetCarriedItemPacket(InventoryUtility.mc.player.getInventory().getSelectedSlot()));
    }

    public static void selectHotbarSlot(HotbarSlot slot) {
        InventoryUtility.selectHotbarSlot(slot.getSlotId());
    }

    public static boolean selectItemInHotbar(Item item) {
        HotbarSlot slot = (HotbarSlot)new HotbarSlotsGroup().findItem(item);
        if (slot != null) {
            InventoryUtility.selectHotbarSlot(slot);
            return true;
        }
        return false;
    }

    public static int findItemInContainer(Predicate<ItemStack> predicate) {
        if (InventoryUtility.mc.player == null || InventoryUtility.mc.player.containerMenu == null) {
            return -1;
        }
        for (int i = 0; i < InventoryUtility.mc.player.containerMenu.slots.size(); ++i) {
            ItemStack stack = InventoryUtility.mc.player.containerMenu.getSlot(i).getItem();
            if (!predicate.test(stack)) continue;
            return i;
        }
        return -1;
    }

    public static int findItemInContainer(Item item) {
        return InventoryUtility.findItemInContainer((ItemStack stack) -> stack.getItem() == item);
    }

    @Generated
    private InventoryUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

