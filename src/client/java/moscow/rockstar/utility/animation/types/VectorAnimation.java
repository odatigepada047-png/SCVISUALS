/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.NonNull
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.utility.animation.types;

import lombok.NonNull;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import net.minecraft.world.phys.Vec3;

public class VectorAnimation {
    private static final Easing DEFAULT_EASING = Easing.CUBIC_IN_OUT;
    private final long duration;
    private final Animation x;
    private final Animation y;
    private final Animation z;

    public VectorAnimation(long duration, Easing easing) {
        this.duration = duration;
        this.x = new Animation(duration, easing);
        this.y = new Animation(duration, easing);
        this.z = new Animation(duration, easing);
    }

    public VectorAnimation(long duration) {
        this(duration, DEFAULT_EASING);
    }

    public VectorAnimation(long duration, Vec3 initalVec, Easing easing) {
        this.duration = duration;
        this.x = new Animation(duration, (float)initalVec.x, easing);
        this.y = new Animation(duration, (float)initalVec.y, easing);
        this.z = new Animation(duration, (float)initalVec.z, easing);
    }

    public VectorAnimation(long duration, Vec3 initalVec) {
        this(duration, initalVec, DEFAULT_EASING);
    }

    public void update(@NonNull Vec3 vec) {
        if (vec == null) {
            throw new NullPointerException("vec is marked non-null but is null");
        }
        this.x.setValue((float)vec.x);
        this.y.setValue((float)vec.y);
        this.z.setValue((float)vec.z);
    }

    public Vec3 getVec() {
        return new Vec3((double)((int)this.x.getValue()), (double)((int)this.y.getValue()), (double)((int)this.z.getValue()));
    }

    public void setEasing(Easing easing) {
        this.x.setEasing(easing);
        this.y.setEasing(easing);
        this.z.setEasing(easing);
    }

    public void setDuration(long duration) {
        this.x.setDuration(duration);
        this.y.setDuration(duration);
        this.z.setDuration(duration);
    }

    public void setVec(@NonNull Vec3 vec) {
        if (vec == null) {
            throw new NullPointerException("vec is marked non-null but is null");
        }
        this.x.setValue((float)vec.x);
        this.y.setValue((float)vec.y);
        this.z.setValue((float)vec.z);
    }
}

