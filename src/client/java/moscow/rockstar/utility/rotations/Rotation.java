/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.utility.rotations;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Rotation
implements IMinecraft {
    public static final Rotation ZERO = new Rotation(0.0f, 0.0f);
    private float yaw;
    private float pitch;

    public Rotation difference(Rotation other) {
        float diffYaw = MathUtility.angleDifference(this.yaw, other.yaw);
        float diffPitch = MathUtility.angleDifference(this.pitch, other.pitch);
        return new Rotation(diffYaw, diffPitch);
    }

    public float differenceValue(Rotation other) {
        float diffYaw = MathUtility.angleDifference(this.yaw, other.yaw);
        float diffPitch = MathUtility.angleDifference(this.pitch, other.pitch);
        return Math.abs(diffYaw) + Math.abs(diffPitch);
    }

    public Vec3 getRotationVector() {
        return Rotation.rotationVector(this.pitch, this.yaw);
    }

    public static Vec3 rotationVector(float pitch, float yaw) {
        float pitchRad = pitch * ((float)Math.PI / 180);
        float yawRad = -yaw * ((float)Math.PI / 180);
        float cosPitch = Mth.cos(pitchRad);
        return new Vec3(Mth.sin(yawRad) * cosPitch, -Mth.sin(pitchRad), Mth.cos(yawRad) * cosPitch);
    }

    public float getYRot() {
        return this.yaw;
    }

    public float getXRot() {
        return this.pitch;
    }

    public void setYRot(float yaw) {
        this.yaw = yaw;
    }

    public void setXRot(float pitch) {
        this.pitch = pitch;
    }

    public void modify(float yaw, float pitch) {
        this.yaw += yaw;
        this.pitch += pitch;
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Generated
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Rotation)) {
            return false;
        }
        Rotation other = (Rotation)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (Float.compare(this.getYRot(), other.getYRot()) != 0) {
            return false;
        }
        return Float.compare(this.getXRot(), other.getXRot()) == 0;
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof Rotation;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + Float.floatToIntBits(this.getYRot());
        result = result * 59 + Float.floatToIntBits(this.getXRot());
        return result;
    }

    @Generated
    public String toString() {
        return "Rotation(yaw=" + this.getYRot() + ", pitch=" + this.getXRot() + ")";
    }

    @Generated
    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
}

