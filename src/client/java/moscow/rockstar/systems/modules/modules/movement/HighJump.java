/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  com.mojang.blaze3d.platform.InputConstants
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.c2s.play.ServerboundUseItemPacket
 *  net.minecraft.util.InteractionHand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.world.ClipContext
 *  net.minecraft.world.ClipContext$FluidHandling
 *  net.minecraft.world.ClipContext$ShapeType
 */
package moscow.rockstar.systems.modules.modules.movement;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.inventory.slots.OffhandSlot;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

@ModuleInfo(name="High Jump", category=ModuleCategory.MOVEMENT)
public class HighJump
extends BaseModule {
    @Override
    public void tick() {
        HighJump.mc.options.keyShift.setDown(true);
        HighJump.mc.player.setXRot(90.0f);
        HighJump.mc.options.keyUse.setDown(true);
        if (HighJump.mc.player.onGround() && !HighJump.mc.player.getCooldowns().isOnCooldown(Items.WIND_CHARGE.getDefaultInstance())) {
            this.useWindCharge();
        }
    }

    @Override
    public void onDisable() {
        int keyCode = HighJump.mc.options.keyShift.getDefaultKey().getValue();
        HighJump.mc.options.keyShift.setDown(moscow.rockstar.utility.game.KeyUtility.isKeyPressed(keyCode));
        keyCode = HighJump.mc.options.keyUse.getDefaultKey().getValue();
        HighJump.mc.options.keyUse.setDown(moscow.rockstar.utility.game.KeyUtility.isKeyPressed(keyCode));
    }

    private void useWindCharge() {
        if (HighJump.mc.level == null || HighJump.mc.player == null || HighJump.mc.gameMode == null) {
            return;
        }
        SlotGroup<ItemSlot> slotsToSearch = SlotGroups.offhand().and(SlotGroups.hotbar());
        ItemSlot slot = slotsToSearch.findItem(Items.WIND_CHARGE);
        boolean isOffhand = slot instanceof OffhandSlot;
        if (slot != null) {
            float pitch;
            HotbarSlot hotbarSlot;
            int oldHotbarSlotId = HighJump.mc.player.getInventory().getSelectedSlot();
            if (slot instanceof HotbarSlot && HighJump.mc.player.getInventory().getSelectedSlot() != (hotbarSlot = (HotbarSlot)slot).getSlotId()) {
                InventoryUtility.selectHotbarSlot(hotbarSlot);
            }
            InteractionHand hand = isOffhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            float yaw = HighJump.mc.player.getYRot();
            if (!HighJump.mc.player.onGround() && EntityUtility.getBlock(0.0, -2.0, 0.0) == Blocks.AIR && HighJump.mc.player.getDeltaMovement().y > (double)0.4f) {
                pitch = 75.0f;
                for (int i = 0; i < 360; i += 45) {
                    BlockHitResult result = HighJump.mc.level.clip(new ClipContext(HighJump.mc.player.getEyePosition(1.0f), HighJump.mc.player.getEyePosition(1.0f).add(Vec3.directionFromRotation(pitch, (float)i).scale(1.5)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, HighJump.mc.player));
                    if (result.getType() != HitResult.Type.BLOCK) continue;
                    yaw = RotationMath.adjustAngle(HighJump.mc.player.getYRot(), i);
                }
            } else {
                pitch = 90.0f;
            }
            float finalYaw = yaw;
            HighJump.mc.gameMode.useItem(HighJump.mc.player, hand);
            HighJump.mc.player.swing(hand);
            if (slot instanceof HotbarSlot) {
                InventoryUtility.selectHotbarSlot(oldHotbarSlotId);
            }
        }
    }
}

