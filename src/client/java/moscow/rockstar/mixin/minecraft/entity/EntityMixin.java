package moscow.rockstar.mixin.minecraft.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationTask;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public class EntityMixin
implements IMinecraft {

    @ModifyExpressionValue(method={"move"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;isLocalInstanceAuthoritative()Z")})
    public boolean fixFalldistanceValue(boolean original) {
        if ((Entity)(Object)this == EntityMixin.mc.player) {
            return false;
        }
        return original;
    }

    @Inject(method={"getBoundingBox"}, at={@At(value="HEAD")}, cancellable=true)
    public final void getBoundingBox(CallbackInfoReturnable<AABB> cir) {
        // Hitboxes module removed
    }

    @Redirect(method={"moveRelative"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;getYRot()F"))
    public float movementCorrection(Entity instance) {
        RotationHandler rotationHandler = Rockstar.INSTANCE.getRotationHandler();
        RotationTask currentTask = rotationHandler.getCurrentTask();
        if (currentTask != null && currentTask.getMoveCorrection() != MoveCorrection.NONE && instance instanceof LocalPlayer) {
            return rotationHandler.getCurrentRotation().getYRot();
        }
        return instance.getYRot();
    }
}
