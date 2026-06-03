/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.gui.screens.ingame.InventoryScreen
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.MobEffects
 *  net.minecraft.entity.player.Player
 *  net.minecraft.item.AxeItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.MaceItem
 *  net.minecraft.item.ShieldItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ServerboundSwingPacket
 *  net.minecraft.network.packet.c2s.play.ServerboundSetCarriedItemPacket
 *  net.minecraft.registry.ResourceKey
 *  net.minecraft.util.InteractionHand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.AABB
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.utility.game;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.combat.Criticals;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CombatUtility
implements IMinecraft {
    public static HotbarSlot getMace() {
        boolean useWindBurst = CombatUtility.mc.player.fallDistance > 2.0f;
        ResourceKey<Enchantment> targetEnchantment = useWindBurst ? Enchantments.WIND_BURST : Enchantments.BREACH;
        SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
        HotbarSlot slot = slotsToSearch.findItem(stack -> CombatUtility.hasMaceEnchantment(targetEnchantment, stack));
        if (slot == null) {
            slot = slotsToSearch.findItem(Items.MACE);
        }
        return slot;
    }

    public static float getFallDistance(LivingEntity target) {
        SlotGroup<HotbarSlot> slotsToSearch;
        HotbarSlot slot;
        if (CombatUtility.mc.player.getMainHandItem().getItem() instanceof MaceItem) {
            // empty if block
        }
        if ((slot = (slotsToSearch = SlotGroups.hotbar()).findItem(Items.MACE)) != null) {
            return 0.7f;
        }
        return 0.0f;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean canPerformCriticalHit(LivingEntity target, boolean ignoreSprint) {
        if (CombatUtility.mc.level == null) return false;
        if (CombatUtility.mc.player == null) {
            return false;
        }
        Block blockAboveHead = CombatUtility.mc.level.getBlockState(CombatUtility.mc.player.blockPosition().above(2)).getBlock();
        if (CombatUtility.mc.player.onClimbable()) return true;
        if (CombatUtility.mc.screen instanceof InventoryScreen) return true;
        if (CombatUtility.mc.player.isInWater() && EntityUtility.getBlock(0.0, 1.0, 0.0) == Blocks.WATER) {
            if (CombatUtility.mc.player.fallDistance <= 0.0f) return true;
        }
        if (CombatUtility.mc.player.isSwimming()) return true;
        if (CombatUtility.mc.level.getBlockState(CombatUtility.mc.player.blockPosition()).is(Blocks.COBWEB)) return true;
        if (CombatUtility.mc.player.isInLava()) return true;
        if (CombatUtility.mc.player.hasEffect(MobEffects.BLINDNESS)) return true;
        if (CombatUtility.mc.player.hasEffect(MobEffects.LEVITATION)) return true;
        if (CombatUtility.mc.player.hasEffect(MobEffects.SLOW_FALLING)) return true;
        if (CombatUtility.mc.player.isPassenger()) return true;
        if (CombatUtility.mc.player.getMainHandItem().getItem() instanceof MaceItem) {
            if (CombatUtility.mc.player.fallDistance > 1.0f) {
                return true;
            }
        } else if (CombatUtility.mc.player.fallDistance > CombatUtility.getFallDistance(target)) return true;
        if (!Rockstar.getInstance().getModuleManager().getModule(Criticals.class).canCritical()) return false;
        return true;
    }

    public static boolean canBreakShield(LivingEntity target) {
        if (CombatUtility.mc.player == null || CombatUtility.mc.player.isDeadOrDying()) {
            return false;
        }
        if (target.isDeadOrDying()) {
            return false;
        }
        HotbarSlot axeSlot = SlotGroups.hotbar().findItem(itemStack -> itemStack.getItem() instanceof AxeItem);
        if (axeSlot == null) {
            return false;
        }
        Vec3 facingVector = Rotation.rotationVector(target.getXRot(), target.getYRot());
        Vec3 deltaPos = new Vec3(target.position().x - CombatUtility.mc.player.position().x, 0.0, target.position().z - CombatUtility.mc.player.position().z);
        return deltaPos.dot(facingVector) < 0.0;
    }

    public static boolean shouldBreakShield(LivingEntity target) {
        return target.isUsingItem() && target.getActiveItem().getItem() instanceof ShieldItem;
    }

    public static void tryBreakShield(LivingEntity target) {
        if (CombatUtility.mc.player == null || CombatUtility.mc.gameMode == null) {
            return;
        }
        SlotGroup<HotbarSlot> slotsToSearch = SlotGroups.hotbar();
        HotbarSlot slot = slotsToSearch.findItem(item -> item.getItem() instanceof AxeItem);
        if (slot != null && target instanceof Player && target.isUsingItem() && target.getActiveItem().getItem() instanceof ShieldItem) {
            CombatUtility.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(slot.getSlotId()));
            CombatUtility.mc.gameMode.attack((Player)CombatUtility.mc.player, (Entity)target);
            CombatUtility.mc.player.connection.send((Packet)new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            CombatUtility.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(CombatUtility.mc.player.getInventory().getSelectedSlot()));
        }
    }

    public static boolean stalin(LivingEntity target) {
        Vec3 pos = target.position();
        AABB hitbox = target.getBoundingBox();
        float off = 0.05f;
        return !CombatUtility.isAir(hitbox.minX - (double)off, pos.y, hitbox.minZ - (double)off) || !CombatUtility.isAir(hitbox.maxX + (double)off, pos.y, hitbox.minZ - (double)off) || !CombatUtility.isAir(hitbox.minX - (double)off, pos.y, hitbox.maxZ + (double)off) || !CombatUtility.isAir(hitbox.maxX + (double)off, pos.y, hitbox.maxZ + (double)off);
    }

    private static boolean isAir(double x, double y, double z) {
        return CombatUtility.mc.level.getBlockState(new BlockPos((int)x, (int)y, (int)z)).getBlock() == Blocks.AIR;
    }

    @Generated
    private CombatUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static boolean hasMaceEnchantment(ResourceKey targetEnchantment, ItemStack stack) {
        if (!(stack.getItem() instanceof MaceItem)) {
            return false;
        }
        return EnchantmentUtility.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)targetEnchantment) > 0;
    }
}
