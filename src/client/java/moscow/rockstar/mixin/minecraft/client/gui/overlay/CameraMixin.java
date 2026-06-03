package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.player.Freelook;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {Camera.class})
public abstract class CameraMixin {

    private float rockstar$smoothedZoom = 4.0f;
    private boolean rockstar$freelookWasActive;

    @Shadow
    private Entity entity;

    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    protected abstract void move(float distance, float vertical, float sideways);

    @Shadow
    private float getMaxZoom(float zoom) {
        throw new AssertionError();
    }

    @Shadow
    public abstract float getCameraEntityPartialTicks(DeltaTracker tracker);

    @Shadow
    public abstract Matrix4f getViewRotationMatrix(Matrix4f dest);

    @Shadow
    public abstract Matrix4f createProjectionMatrixForCulling();

    @Shadow
    public abstract Vec3 position();

    @Shadow
    private void prepareCullFrustum(Matrix4fc viewRotationMatrix, Matrix4f projectionMatrix, Vec3 cameraPos) {
        throw new AssertionError();
    }

    @Inject(method = {"update"}, at = {@At(value = "TAIL")})
    private void applyFreelookCamera(DeltaTracker tracker, CallbackInfo ci) {
        Freelook freelook = Rockstar.getInstance().getModuleManager().getModule(Freelook.class);
        boolean freelookActive = freelook.isFreelookActive();
        if (!freelookActive) {
            this.rockstar$freelookWasActive = false;
            return;
        }
        if (this.entity == null) {
            return;
        }
        if (!this.rockstar$freelookWasActive) {
            this.rockstar$smoothedZoom = 4.0f;
            this.rockstar$freelookWasActive = true;
        }

        float partialTicks = this.getCameraEntityPartialTicks(tracker);
        float yaw = freelook.getCameraYaw(partialTicks);
        float pitch = freelook.getCameraPitch(partialTicks);

        // Pivot at player eyes (F5 orbit center), not at the old body-facing offset.
        double x = Mth.lerp((double) partialTicks, this.entity.xo, this.entity.getX());
        double y = Mth.lerp((double) partialTicks, this.entity.yo, this.entity.getY())
                + (double) Mth.lerp(partialTicks, this.eyeHeightOld, this.eyeHeight);
        double z = Mth.lerp((double) partialTicks, this.entity.zo, this.entity.getZ());
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);

        float scale = 1.0f;
        float cameraDistance = 4.0f;
        if (this.entity instanceof LivingEntity living) {
            scale = living.getScale();
            cameraDistance = (float) living.getAttributeValue(Attributes.CAMERA_DISTANCE);
        }

        float targetZoom = this.getMaxZoom(Math.max(scale * cameraDistance, scale * 4.0f));
        this.rockstar$smoothedZoom = Mth.lerp(0.35f, this.rockstar$smoothedZoom, targetZoom);
        this.move(-this.rockstar$smoothedZoom, 0.0f, 0.0f);

        // Recalculate frustum culling for the new freelook camera position/orientation
        Matrix4f viewRot = this.getViewRotationMatrix(new Matrix4f());
        Matrix4f proj = this.createProjectionMatrixForCulling();
        this.prepareCullFrustum(viewRot, proj, this.position());
    }

    @Inject(method = {"getMaxZoom"}, at = {@At(value = "HEAD")}, cancellable = true)
    private void allowCameraClip(float zoom, CallbackInfoReturnable<Float> cir) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals != null && removals.isEnabled() && removals.getClip().isSelected()) {
            cir.setReturnValue(zoom);
        }
    }

    @Inject(method = {"getFluidInCamera"}, at = {@At(value = "RETURN")}, cancellable = true)
    private void getSubmergedFluidState(CallbackInfoReturnable<FogType> ci) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && (removals.getWater().isSelected() || removals.getWaterBlur().isSelected())
                && ci.getReturnValue() == FogType.WATER) {
            ci.setReturnValue(FogType.NONE);
        }
    }
}
