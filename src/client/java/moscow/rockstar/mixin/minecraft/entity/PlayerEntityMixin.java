/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.Player
 *  net.minecraft.util.math.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.systems.event.impl.game.PostAttackEvent;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Player.class})
public class PlayerEntityMixin {
    @Inject(method={"attack"}, at={@At(value="HEAD")}, cancellable=true)
    private void attackAHook2(Entity target, CallbackInfo ci) {
        AttackEvent event = new AttackEvent(target);
        Rockstar.getInstance().getEventManager().triggerEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"attack"}, at={@At(value="RETURN")}, cancellable=true)
    private void attackAHook(Entity target, CallbackInfo ci) {
        PostAttackEvent event = new PostAttackEvent(target);
        Rockstar.getInstance().getEventManager().triggerEvent(event);
    }

}


