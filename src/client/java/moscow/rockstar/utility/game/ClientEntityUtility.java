package moscow.rockstar.utility.game;

import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ClientEntityUtility implements IMinecraft {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD
    };

    private ClientEntityUtility() {
    }

    public static Iterable<ItemStack> getArmorItems(Player player) {
        return () -> new java.util.Iterator<ItemStack>() {
            private int index;

            @Override
            public boolean hasNext() {
                return this.index < ARMOR_SLOTS.length;
            }

            @Override
            public ItemStack next() {
                return player.getItemBySlot(ARMOR_SLOTS[this.index++]);
            }
        };
    }

    public static Iterable<ItemStack> getArmorItems(LivingEntity entity) {
        return () -> new java.util.Iterator<ItemStack>() {
            private int index;

            @Override
            public boolean hasNext() {
                return this.index < ARMOR_SLOTS.length;
            }

            @Override
            public ItemStack next() {
                return entity.getItemBySlot(ARMOR_SLOTS[this.index++]);
            }
        };
    }

    public static Entity getCrosshairEntity() {
        if (mc == null) {
            return null;
        }
        Entity picked = mc.crosshairPickEntity;
        if (picked != null) {
            return picked;
        }
        HitResult hit = mc.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult entityHit) {
            return entityHit.getEntity();
        }
        return null;
    }
}
