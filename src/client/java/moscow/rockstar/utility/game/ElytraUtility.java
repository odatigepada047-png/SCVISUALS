/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.renderer.BufferBuilder
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.Player
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemLike
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.equipment.EquipmentType
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ServerboundUseItemPacket
 *  net.minecraft.network.packet.c2s.play.ServerboundSetCarriedItemPacket
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.util.InteractionHand
 *  net.minecraft.util.math.AABB
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.utility.game;

import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.CombatUtility;
import moscow.rockstar.utility.game.prediction.ElytraPredictionSystem;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.InventorySlot;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import moscow.rockstar.utility.time.Timer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
// import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ElytraUtility
implements IMinecraft {
    private static Vec3 lastVec;
    private static final Timer fireworkTimer;

    public static void swapInHotbar(boolean chestplate) {
        HotbarSlot slot;
        SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
        HotbarSlot hotbarSlot = slot = chestplate ? slotsToSearch.findItem(itemStack -> {
            net.minecraft.world.item.equipment.Equippable equippable = itemStack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
            return equippable != null && equippable.slot() == net.minecraft.world.entity.EquipmentSlot.CHEST;
        }) : slotsToSearch.findItem(Items.ELYTRA);
        if (slot != null) {
            HotbarSlot currentItem = InventoryUtility.getCurrentHotbarSlot();
            ElytraUtility.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(slot.getSlotId()));
            InventoryUtility.selectHotbarSlot(slot);
            ElytraUtility.mc.gameMode.useItem((Player)ElytraUtility.mc.player, InteractionHand.MAIN_HAND);
            ((Slot)ElytraUtility.mc.player.containerMenu.slots.get(6)).set(new ItemStack((ItemLike)(chestplate ? Items.NETHERITE_CHESTPLATE : Items.ELYTRA)));
            InventoryUtility.selectHotbarSlot(currentItem);
            ElytraUtility.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(ElytraUtility.mc.player.getInventory().getSelectedSlot()));
        }
    }

    public static void drawBoxes(PoseStack matrices, BufferBuilder linesBuffer, AABB box, ColorRGBA color) {
        Draw3DUtility.renderOutlinedBox(matrices, linesBuffer, box, color);
        Draw3DUtility.renderBoxInternalDiagonals(matrices, linesBuffer, box, color);
    }

    public static boolean leaving() {
        Player player;
        Entity entity;
        return (entity = Rockstar.getInstance().getTargetManager().getCurrentTarget()) instanceof Player && !ElytraPredictionSystem.isLeaving(player = (Player)entity) || CombatUtility.getMace() != null;
    }

    public static void useFirework(float selectedSlot) {
        SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
        HotbarSlot slot = slotsToSearch.findItem(Items.FIREWORK_ROCKET);
        if (slot != null) {
            LivingEntity target = Rockstar.getInstance().getTargetManager().getLivingTarget();
            Rotation rot = target == null ? Rockstar.getInstance().getRotationHandler().getPlayerRotation() : RotationMath.getRotationTo(ElytraUtility.leaving() ? ElytraUtility.mc.player.getEyePosition(1.0f).add(ElytraUtility.leaveVec(target)) : ElytraUtility.targetPoint(target));
            ElytraUtility.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(slot.getSlotId()));
            ElytraUtility.mc.gameMode.useItem(ElytraUtility.mc.player, net.minecraft.world.InteractionHand.MAIN_HAND);
            ElytraUtility.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(ElytraUtility.mc.player.getInventory().getSelectedSlot()));
            fireworkTimer.reset();
            return;
        }
        SlotGroup<InventorySlot> search = SlotGroups.inventory();
        InventorySlot invSlot = search.findItem(Items.FIREWORK_ROCKET);
        if (invSlot != null) {
            InventoryUtility.hotbarSwap(invSlot.getIdForServer(), (int)(selectedSlot - 1.0f));
            fireworkTimer.reset();
        }
    }

    public static Vec3 targetPoint(LivingEntity target) {
        Vec3 vec3d = target.position();
        return RotationMath.getNearestPoint(target, vec3d);
    }

    public static Vec3 leaveVec(LivingEntity target) {
        List<Vec3> leaveVectors = List.of(new Vec3(0.0, 20.0, 0.0), new Vec3(0.0, -20.0, 0.0), new Vec3(20.0, 0.0, 0.0), new Vec3(-20.0, 0.0, 0.0), new Vec3(0.0, 0.0, 20.0), new Vec3(0.0, 0.0, -20.0));
        if (CombatUtility.getMace() != null) {
            leaveVectors = List.of(new Vec3(0.0, 20.0, 0.0));
        }
        Vec3 leaveVec = Vec3.ZERO;
        for (Vec3 vector : leaveVectors) {
            if (!MathUtility.canSeen(target.getEyePosition(1.0f).add(vector)) || vector.equals((Object)lastVec)) continue;
            leaveVec = vector;
            break;
        }
        return leaveVec;
    }

    public static float[] getLeftRightYaw45NotMultipleOf90(float yaw) {
        float nearest45;
        float baseYaw = yaw - yaw % 360.0f;
        float yawNormalized = yaw % 360.0f;
        float left;
        float right;
        if (yawNormalized < 0.0f) {
            yawNormalized += 360.0f;
            baseYaw -= 360.0f;
        }
        if ((nearest45 = (float)Math.round(yawNormalized / 45.0f) * 45.0f) % 90.0f == 0.0f) {
            float candidateLeft = (nearest45 - 45.0f) % 360.0f;
            float candidateRight = (nearest45 + 45.0f) % 360.0f;
            left = candidateLeft < yawNormalized ? candidateLeft : candidateLeft - 45.0f;
            right = candidateRight > yawNormalized ? candidateRight : candidateRight + 45.0f;
        } else if (nearest45 < yawNormalized) {
            left = nearest45;
            right = nearest45 + 90.0f;
        } else {
            left = nearest45 - 90.0f;
            right = nearest45;
        }
        return new float[]{left += baseYaw, right += baseYaw};
    }

    @Generated
    private ElytraUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setLastVec(Vec3 lastVec) {
        ElytraUtility.lastVec = lastVec;
    }

    @Generated
    public static Timer getFireworkTimer() {
        return fireworkTimer;
    }

    static {
        fireworkTimer = new Timer();
    }
}
