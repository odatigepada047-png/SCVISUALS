/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.Mth
 *  net.minecraft.util.math.Vec3
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.utility.rotations;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class RotationMath
implements IMinecraft {
    public static Vec3 getNearestPoint(LivingEntity entity) {
        Vec3 pos = RotationMath.mc.player.getEyePosition(1.0f);
        return new Vec3(Mth.clamp((double)pos.x, (double)entity.getBoundingBox().minX, (double)entity.getBoundingBox().maxX), Mth.clamp((double)pos.y, (double)entity.getBoundingBox().minY, (double)entity.getBoundingBox().maxY), Mth.clamp((double)pos.z, (double)entity.getBoundingBox().minZ, (double)entity.getBoundingBox().maxZ));
    }

    public static Vec3 getNearestPoint(LivingEntity entity, Vec3 pos) {
        return entity.position().subtract(entity.position()).add(RotationMath.getNearestPoint(entity));
    }

    public static Rotation getRotationTo(Vec3 targetedEntity) {
        double posX = targetedEntity.x;
        double posY = targetedEntity.y;
        double posZ = targetedEntity.z;
        double deltaX = posX - RotationMath.mc.player.getX();
        double deltaY = posY - (RotationMath.mc.player.getY() + (double)RotationMath.mc.player.getEyeHeight(RotationMath.mc.player.getPose()));
        double deltaZ = posZ - RotationMath.mc.player.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance)));
        return new Rotation(yaw, pitch);
    }

    public static double getGcd() {
        double sensitivity = RotationMath.mc.options.sensitivity().get() * 0.6 + 0.2;
        double sensitivityPow3 = sensitivity * sensitivity * sensitivity;
        return sensitivityPow3 * 8.0 * (double)0.15f;
    }

    @NotNull
    public static Rotation correctRotation(@NotNull Rotation rotation) {
        double gcd = RotationMath.getGcd();
        float yaw = (float)((double)rotation.getYRot() - (double)rotation.getYRot() % gcd);
        float pitch = (float)((double)rotation.getXRot() - (double)rotation.getXRot() % gcd);
        return new Rotation(yaw, pitch);
    }

    public static float getAngleDifference(float current, float target) {
        float diff;
        for (diff = target - current; diff > 180.0f; diff -= 360.0f) {
        }
        while (diff < -180.0f) {
            diff += 360.0f;
        }
        return diff;
    }

    public static float adjustAngle(float currentAngle, float targetAngle) {
        float adjustedTarget;
        float difference;
        float normalizedTarget;
        float normalizedCurrent = currentAngle % 360.0f;
        if (normalizedCurrent < 0.0f) {
            normalizedCurrent += 360.0f;
        }
        if ((normalizedTarget = targetAngle % 360.0f) < 0.0f) {
            normalizedTarget += 360.0f;
        }
        int revolutions = (int)(currentAngle / 360.0f);
        if (currentAngle < 0.0f && currentAngle % 360.0f != 0.0f) {
            --revolutions;
        }
        if ((difference = (adjustedTarget = normalizedTarget + (float)(revolutions * 360)) - currentAngle) > 180.0f) {
            adjustedTarget -= 360.0f;
        } else if (difference < -180.0f) {
            adjustedTarget += 360.0f;
        }
        return adjustedTarget;
    }

    @Generated
    private RotationMath() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

