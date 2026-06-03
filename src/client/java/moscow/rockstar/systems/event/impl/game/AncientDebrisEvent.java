/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.systems.event.impl.game;

import java.util.Collections;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class AncientDebrisEvent
extends Event {
    private final List<BlockPos> positions;
    private final Vec3 explosionCenter;

    public AncientDebrisEvent(List<BlockPos> positions, Vec3 center) {
        this.positions = Collections.unmodifiableList(positions);
        this.explosionCenter = center;
    }

    @Generated
    public List<BlockPos> getPositions() {
        return this.positions;
    }

    @Generated
    public Vec3 getExplosionCenter() {
        return this.explosionCenter;
    }
}

