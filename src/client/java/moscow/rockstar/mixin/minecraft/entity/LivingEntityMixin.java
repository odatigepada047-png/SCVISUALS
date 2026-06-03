package moscow.rockstar.mixin.minecraft.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.EntityDeathEvent;
import moscow.rockstar.systems.event.impl.game.EntityJumpEvent;
import moscow.rockstar.utility.interfaces.ILivingEntity;
import moscow.rockstar.systems.modules.modules.visuals.SwingAnimation;
import moscow.rockstar.utility.rotations.MoveCorrection;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationTask;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin implements ILivingEntity {
    @Shadow
    private int noJumpDelay;

    @Override
    public void setJumpDelayTimer(int timer) {
        this.noJumpDelay = timer;
    }

    @ModifyReturnValue(method={"getCurrentSwingDuration"}, at={@At(value="RETURN")})
    public int replaceSwingSpeed(int original) {
        SwingAnimation swingAnimationModule = Rockstar.getInstance().getModuleManager().getModule(SwingAnimation.class);
        LivingEntity self = (LivingEntity)(Object)this;
        if (!swingAnimationModule.isEnabled() || !swingAnimationModule.shouldApplyAnimation(self.getMainHandItem())) {
            return original;
        }
        return (int)((float)original * Rockstar.getInstance().getSwingManager().getSpeed().getCurrentValue());
    }

    @Inject(method={"jumpFromGround"}, at={@At(value="HEAD")}, cancellable=true)
    public void triggerJumpEvent(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        EntityJumpEvent event = new EntityJumpEvent(livingEntity);
        Rockstar.getInstance().getEventManager().triggerEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method={"jumpFromGround"}, at={@At(value="NEW", target="(DDD)Lnet/minecraft/world/phys/Vec3;")})
    public Vec3 movementCorrection(Vec3 original) {
        RotationHandler rotationHandler = Rockstar.INSTANCE.getRotationHandler();
        RotationTask currentTask = rotationHandler.getCurrentTask();
        if ((LivingEntity)(Object)this != Minecraft.getInstance().player) {
            return original;
        }
        if (currentTask != null && currentTask.getMoveCorrection() != MoveCorrection.NONE) {
            float yaw = rotationHandler.getCurrentRotation().getYRot() * ((float)Math.PI / 180);
            return new Vec3((double)(-Mth.sin((float)yaw) * 0.2f), 0.0, (double)(Mth.cos((float)yaw) * 0.2f));
        }
        return original;
    }

    @Inject(method={"die"}, at={@At(value="TAIL")})
    public void triggerEntityDeathEvent(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        Rockstar.getInstance().getEventManager().triggerEvent(new EntityDeathEvent(entity, damageSource));
    }

}
