/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.DeltaTracker
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.util.Mth
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.AspectRatio;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import net.minecraft.client.renderer.Projection;

@Mixin(value={GameRenderer.class})
public abstract class GameRendererMixin {
    @Shadow
    private net.minecraft.client.Minecraft minecraft;

    @Shadow
    private net.minecraft.client.renderer.state.GameRenderState gameRenderState;

    @Shadow
    private net.minecraft.client.Camera mainCamera;

    @Shadow
    private net.minecraft.client.renderer.ProjectionMatrixBuffer levelProjectionMatrixBuffer;
    @Inject(method={"bobHurt"}, at={@At(value="HEAD")}, cancellable=true)
    private void tiltViewWhenHurtHook(CameraRenderState cameraState, PoseStack matrices, CallbackInfo ci) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && removals.getHurtCam().isSelected()) {
            ci.cancel();
        }
    }

    @ModifyReturnValue(method = {"getBasicProjectionMatrix"}, at = @At("RETURN"), require = 0)
    private Matrix4f modifyProjectionMatrix(Matrix4f original) {
        Matrix4f projection = new Matrix4f(original);
        AspectRatio module = Rockstar.getInstance().getModuleManager().getModule(AspectRatio.class);
        if (module != null && module.isEnabled() && module.getAspectRatio() > 0.0f) {
            Minecraft mc = Minecraft.getInstance();
            int fbWidth = mc.getWindow().getWidth();
            int fbHeight = mc.getWindow().getHeight();
            if (fbWidth > 0 && fbHeight > 0) {
                double windowAspect = (double) fbWidth / (double) fbHeight;
                double targetAspect = module.getAspectRatio();
                float scale = (float) (windowAspect / targetAspect);
                projection.scale(scale, 1.0f, 1.0f);
                original.scale(scale, 1.0f, 1.0f);
            }
        }
        return original;
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void modifyLevelProjectionMatrix(DeltaTracker deltaTracker, CallbackInfo ci) {
        AspectRatio module = Rockstar.getInstance().getModuleManager().getModule(AspectRatio.class);
        if (module != null && module.isEnabled() && module.getAspectRatio() > 0.0f) {
            Minecraft mc = Minecraft.getInstance();
            int fbWidth = mc.getWindow().getWidth();
            int fbHeight = mc.getWindow().getHeight();
            if (fbWidth > 0 && fbHeight > 0) {
                double windowAspect = (double) fbWidth / (double) fbHeight;
                double targetAspect = module.getAspectRatio();
                float scale = (float) (windowAspect / targetAspect);
                this.gameRenderState.levelRenderState.cameraRenderState.projectionMatrix.scale(scale, 1.0f, 1.0f);
            }
        }
    }

    @Redirect(method={"renderLevel"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/Mth;lerp(FFF)F"), require=0)
    private float renderWorldHook(float delta, float first, float second) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && removals.getNausea().isSelected()) {
            return 0.0f;
        }
        return Mth.lerp((float)delta, (float)first, (float)second);
    }

    @Redirect(method = "renderItemInHand", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/OptionsRenderState;hideGui:Z"), require = 0)
    private boolean redirectHideGui(net.minecraft.client.renderer.state.OptionsRenderState state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof moscow.rockstar.systems.modules.constructions.customhand.CustomHandScreen 
            || mc.screen instanceof moscow.rockstar.systems.modules.constructions.swinganim.SwingAnimScreen) {
            return false;
        }
        return state.hideGui;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER))
    private void postEntityOutline(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        if (renderLevel) {
            net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState = this.gameRenderState.levelRenderState.cameraRenderState;
            Matrix4f positionMatrix = cameraRenderState.viewRotationMatrix;
            Matrix4f projectionMatrix = cameraRenderState.projectionMatrix;
            
            // Re-apply 3D projection matrix to RenderSystem
            com.mojang.blaze3d.buffers.GpuBufferSlice projBuffer = this.levelProjectionMatrixBuffer.getBuffer(projectionMatrix);
            com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(projBuffer, com.mojang.blaze3d.ProjectionType.PERSPECTIVE);
            
            Utils.onRender(new Matrix4f(positionMatrix), new Matrix4f(projectionMatrix), cameraRenderState.pos);
            net.minecraft.util.profiling.Profiler.get().popPush(Rockstar.MOD_ID + "_renderWorld");
            
            PoseStack matrices = new PoseStack();
            matrices.mulPose(new Matrix4f(positionMatrix));
            
            Rockstar.getInstance().getEventManager().triggerEvent(new Render3DEvent(matrices, new Matrix4f(positionMatrix), new Matrix4f(projectionMatrix), this.mainCamera, deltaTracker.getGameTimeDeltaTicks()));
        }
    }
}


