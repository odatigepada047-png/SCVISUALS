/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.Player
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.Mth
 *  net.minecraft.util.math.Vec3
 *  net.minecraft.world.ClipContext
 *  net.minecraft.world.ClipContext$FluidHandling
 *  net.minecraft.world.ClipContext$ShapeType
 */
package moscow.rockstar.utility.game.prediction;

import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

public final class EntityPredictor
implements IMinecraft {
    public static float predictDamage(Entity crystal, Player target) {
        Vec3 crystalPos = crystal.position();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        double dist = targetPos.distanceTo(crystalPos);
        if (dist < 0.5) {
            dist = 0.0;
        }
        double scaledImpact = 1.0 - Mth.clamp((double)(dist / 6.0), (double)0.0, (double)1.0);
        boolean blocked = target.level().clip(new ClipContext(crystalPos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)target)).getType() != HitResult.Type.MISS;
        float exposure = blocked ? 0.7f : 1.0f;
        return (float)((double)exposure * (scaledImpact * 24.0 + 1.0));
    }

    @Generated
    private EntityPredictor() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

