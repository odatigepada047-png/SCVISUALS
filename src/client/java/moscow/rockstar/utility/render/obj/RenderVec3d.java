/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.math.Vec3
 *  net.minecraft.util.math.Vec3i
 *  org.joml.Vector3f
 */
package moscow.rockstar.utility.render.obj;

import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import org.joml.Vector3f;

public class RenderVec3d
extends Vec3 {
    private final Vec3 prev;

    public RenderVec3d(double x, double y, double z, Vec3 prev) {
        super(x, y, z);
        this.prev = prev;
    }

    public RenderVec3d(Vector3f vec, Vec3 prev) {
        super(vec);
        this.prev = prev;
    }

    public RenderVec3d(Vec3i vec, Vec3 prev) {
        super(vec);
        this.prev = prev;
    }

    @Generated
    public Vec3 getPrev() {
        return this.prev;
    }
}

