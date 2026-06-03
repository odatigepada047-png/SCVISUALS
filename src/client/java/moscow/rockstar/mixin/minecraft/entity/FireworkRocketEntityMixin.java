/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.projectile.FireworkRocketEntity
 *  net.minecraft.util.math.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.FireworkEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={FireworkRocketEntity.class})
public abstract class FireworkRocketEntityMixin
implements IMinecraft {
    @Redirect(method={"tick"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void redirectSetVelocity(LivingEntity shooter, Vec3 velocity) {
        FireworkRocketEntity rocketEntity = (FireworkRocketEntity)(Object)this;
        FireworkEvent event = new FireworkEvent(shooter, velocity, rocketEntity);
        Rockstar.getInstance().getEventManager().triggerEvent(event);
        shooter.setDeltaMovement(event.getVelocity());
    }

}
