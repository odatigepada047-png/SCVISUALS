/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.util.Mth
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.systems.target;

import java.util.Comparator;
import java.util.function.Function;
import moscow.rockstar.utility.game.ClientEntityUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TargetComparators
implements IMinecraft {
    public static final Comparator<Entity> DISTANCE = Comparator.comparingDouble(entity -> entity.distanceTo((Entity)TargetComparators.mc.player));
    public static final Comparator<Entity> HEALTH = Comparator.comparingDouble(entity -> {
        double d;
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            d = living.getHealth();
        } else {
            d = 0.0;
        }
        return d;
    });
    public static final Comparator<Entity> FOV = Comparator.comparingDouble(entity -> {
        if (TargetComparators.mc.player == null) {
            return Double.MAX_VALUE;
        }
        Vec3 playerPos = TargetComparators.mc.player.position();
        Vec3 entityPos = entity.position();
        Vec3 playerLook = TargetComparators.mc.player.getLookAngle();
        Vec3 toEntity = entityPos.subtract(playerPos).normalize();
        double dot = playerLook.dot(toEntity);
        return Math.acos(Mth.clamp((double)dot, (double)-1.0, (double)1.0)) * 57.29577951308232;
    });
    public static final Comparator<Entity> BAD_ARMOR = Comparator.comparingDouble(entity -> {
        if (!(entity instanceof Player)) {
            return Double.MAX_VALUE;
        }
        Player player = (Player)entity;
        double totalArmor = 0.0;
        for (ItemStack armorStack : ClientEntityUtility.getArmorItems(player)) {
            if (armorStack == null || armorStack.isEmpty()) continue;
            totalArmor += (double)armorStack.getItem().getDefaultInstance().getCount();
        }
        return totalArmor;
    });
    public static final Comparator<Entity> GOOD_ARMOR = BAD_ARMOR.reversed();

    public static Comparator<Entity> byValue(Function<Entity, Double> valueExtractor) {
        return Comparator.comparingDouble(valueExtractor::apply);
    }

    public static Comparator<Entity> byValueReversed(Function<Entity, Double> valueExtractor) {
        return Comparator.comparingDouble(valueExtractor::apply).reversed();
    }
}



