/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.effect.MobEffects
 *  net.minecraft.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.registry.ResourceKey
 *  net.minecraft.registry.entry.Holder
 *  net.minecraft.util.math.AABB
 *  net.minecraft.util.Mth
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.utility.game.prediction;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class FallPredictor
implements IMinecraft {
    private static Holder<Enchantment> FALL = null;
    private static final double GRAVITY = 0.08;
    private static final double DRAG = 0.98;

    public static float predictFallDamage(Player player, int futureTicks) {
        float raw;
        Vec3 pos = player.position();
        Vec3 vel = player.getDeltaMovement();
        AABB bbox = player.getBoundingBox();
        double fallDist = 0.0;
        for (int t = 0; t < futureTicks; ++t) {
            vel = vel.add(0.0, -0.08, 0.0).scale(0.98);
            pos = pos.add(vel);
            if (!FallPredictor.mc.level.noCollision((bbox = bbox.move(vel)).move(0.0, -0.001, 0.0))) break;
            if (!(vel.y < 0.0)) continue;
            fallDist -= vel.y;
        }
        if ((raw = (float)fallDist) <= 3.0f) {
            return 0.0f;
        }
        int distanceBlocks = Mth.floor((float)(raw - 3.0f));
        float damage = distanceBlocks;
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        int ffLevel = EnchantmentUtility.getEnchantmentLevel(boots, (ResourceKey<Enchantment>)Enchantments.FEATHER_FALLING);
        if (ffLevel > 0) {
            damage = Math.max(damage - damage * 0.15f * (float)ffLevel, 0.0f);
        }
        if (player.hasEffect(MobEffects.SLOW_FALLING)) {
            return 0.0f;
        }
        return damage;
    }

    @Generated
    private FallPredictor() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

