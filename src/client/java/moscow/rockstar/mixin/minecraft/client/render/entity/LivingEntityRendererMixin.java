/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.network.LocalPlayer
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.VertexConsumer
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.model.HumanoidModel
 *  net.minecraft.client.renderer.entity.model.EntityModel
 *  net.minecraft.client.renderer.entity.state.EntityRenderState
 *  net.minecraft.client.renderer.entity.state.LivingEntityRenderState
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.decoration.ArmorStand
 *  net.minecraft.entity.player.Player
 *  net.minecraft.util.Identifier
 *  org.joml.Vector3f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package moscow.rockstar.mixin.minecraft.client.render.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.Rockstar;
import moscow.rockstar.mixin.accessors.BipedEntityModelAccessor;
import moscow.rockstar.systems.modules.modules.player.Freelook;
import moscow.rockstar.systems.modules.modules.visuals.FriendMarkers;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.mixins.EntityRenderStateAddition;
import moscow.rockstar.utility.rotations.RotationHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow
    public abstract Identifier getTextureLocation(S var1);

    @Shadow
    protected M model;

    @Unique
    private static moscow.rockstar.systems.modules.modules.visuals.TargetESP rockstar$targetESP;
    @Unique
    private static moscow.rockstar.systems.modules.modules.visuals.FriendMarkers rockstar$friendMarkers;
    @Unique
    private static moscow.rockstar.systems.modules.modules.player.Freelook rockstar$freelook;

    @Unique
    private static moscow.rockstar.systems.modules.modules.visuals.TargetESP rockstar$getTargetESP() {
        if (rockstar$targetESP == null) {
            try {
                rockstar$targetESP = Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.visuals.TargetESP.class);
            } catch (Exception e) {}
        }
        return rockstar$targetESP;
    }

    @Unique
    private static moscow.rockstar.systems.modules.modules.visuals.FriendMarkers rockstar$getFriendMarkers() {
        if (rockstar$friendMarkers == null) {
            try {
                rockstar$friendMarkers = Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.visuals.FriendMarkers.class);
            } catch (Exception e) {}
        }
        return rockstar$friendMarkers;
    }

    @Unique
    private static moscow.rockstar.systems.modules.modules.player.Freelook rockstar$getFreelook() {
        if (rockstar$freelook == null) {
            try {
                rockstar$freelook = Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.player.Freelook.class);
            } catch (Exception e) {}
        }
        return rockstar$freelook;
    }

    @ModifyExpressionValue(method={"extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;solveBodyRot(Lnet/minecraft/world/entity/LivingEntity;FF)F")})
    public float changeYaw(float oldValue, LivingEntity entity) {
        if (!(entity instanceof LocalPlayer)) {
            return oldValue;
        }
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        float yaw = rotationHandler.isIdling() ? oldValue : rotationHandler.getRenderRotation().getYRot();
        rotationHandler.getServerRotation().setYRot(yaw);
        return yaw;
    }

    @ModifyExpressionValue(method={"extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/Mth;rotLerp(FFF)F")})
    public float changeHeadYaw(float oldValue, LivingEntity entity) {
        if (!(entity instanceof LocalPlayer)) {
            return oldValue;
        }
        Freelook freelook = rockstar$getFreelook();
        if (freelook.isFreelookActive()) {
            return freelook.getSavedYaw();
        }
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        float yaw = rotationHandler.isIdling() ? oldValue : rotationHandler.getRenderRotation().getYRot();
        rotationHandler.getServerRotation().setYRot(yaw);
        return yaw;
    }

    @ModifyExpressionValue(method={"extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getXRot(F)F")})
    public float changePitch(float oldValue, LivingEntity entity) {
        if (!(entity instanceof LocalPlayer)) {
            return oldValue;
        }
        Freelook freelook = rockstar$getFreelook();
        if (freelook.isFreelookActive()) {
            return freelook.getSavedPitch();
        }
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        float pitch = rotationHandler.isIdling() ? oldValue : rotationHandler.getRenderRotation().getXRot();
        rotationHandler.getServerRotation().setYRot(pitch);
        return pitch;
    }

    @WrapOperation(method={"submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")}, require=1)
    private void changeModelColor(
        SubmitNodeCollector collector,
        Model instance,
        Object state,
        PoseStack matrixStack,
        RenderType renderType,
        int light,
        int overlay,
        int color,
        TextureAtlasSprite sprite,
        int p9,
        ModelFeatureRenderer.CrumblingOverlay crumbling,
        Operation<Void> original,
        @Local(argsOnly=true) S livingEntityRenderState
    ) {
        Entity entity = ((EntityRenderStateAddition)livingEntityRenderState).rockstar$getEntity();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (instance instanceof HumanoidModel) {
                HumanoidModel model = (HumanoidModel)instance;
                
                // Получаем координаты для Skeleton ESP
                moscow.rockstar.systems.modules.modules.visuals.TargetESP targetESP = rockstar$getTargetESP();
                if (targetESP != null && targetESP.isEnabled() && targetESP.isSkeletonMode() && targetESP.getPrevTarget() != null && targetESP.getPrevTarget().getId() == entity.getId()) {
                    BipedEntityModelAccessor acc = (BipedEntityModelAccessor)model;
                    java.util.List<org.joml.Vector3f[]> lines = new java.util.ArrayList<>();
                    
                    java.util.function.Function<ModelPart, org.joml.Vector3f> getPos = (part) -> {
                        matrixStack.pushPose();
                        part.translateAndRotate(matrixStack);
                        org.joml.Vector3f pos = new org.joml.Vector3f(0, 0, 0);
                        matrixStack.last().pose().transformPosition(pos);
                        matrixStack.popPose();
                        net.minecraft.client.Camera camera = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera();
                        Vec3 cameraPos = camera.position();
                        return pos.add(new org.joml.Vector3f((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z));
                    };
                    java.util.function.BiFunction<ModelPart, org.joml.Vector3f, org.joml.Vector3f> getOffsetPos = (part, offset) -> {
                        matrixStack.pushPose();
                        part.translateAndRotate(matrixStack);
                        org.joml.Vector3f pos = new org.joml.Vector3f(offset);
                        matrixStack.last().pose().transformPosition(pos);
                        matrixStack.popPose();
                        net.minecraft.client.Camera camera = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera();
                        Vec3 cameraPos = camera.position();
                        return pos.add(new org.joml.Vector3f((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z));
                    };
                    
                    org.joml.Vector3f bodyTop = getPos.apply(acc.rockstar$getBody());
                    org.joml.Vector3f bodyBottom = getOffsetPos.apply(acc.rockstar$getBody(), new org.joml.Vector3f(0, 12.0f / 16.0f, 0));
                    
                    org.joml.Vector3f head = getPos.apply(acc.rockstar$getHead());
                    
                    org.joml.Vector3f rightArmTop = getPos.apply(acc.rockstar$getRightArm());
                    org.joml.Vector3f rightArmBot = getOffsetPos.apply(acc.rockstar$getRightArm(), new org.joml.Vector3f(0, 12.0f / 16.0f, 0));
                    
                    org.joml.Vector3f leftArmTop = getPos.apply(acc.rockstar$getLeftArm());
                    org.joml.Vector3f leftArmBot = getOffsetPos.apply(acc.rockstar$getLeftArm(), new org.joml.Vector3f(0, 12.0f / 16.0f, 0));
                    
                    org.joml.Vector3f rightLegTop = getPos.apply(acc.rockstar$getRightLeg());
                    org.joml.Vector3f rightLegBot = getOffsetPos.apply(acc.rockstar$getRightLeg(), new org.joml.Vector3f(0, 12.0f / 16.0f, 0));
                    
                    org.joml.Vector3f leftLegTop = getPos.apply(acc.rockstar$getLeftLeg());
                    org.joml.Vector3f leftLegBot = getOffsetPos.apply(acc.rockstar$getLeftLeg(), new org.joml.Vector3f(0, 12.0f / 16.0f, 0));
                    
                    lines.add(new org.joml.Vector3f[]{head, bodyTop});
                    lines.add(new org.joml.Vector3f[]{bodyTop, bodyBottom});
                    
                    lines.add(new org.joml.Vector3f[]{bodyTop, rightArmTop});
                    lines.add(new org.joml.Vector3f[]{rightArmTop, rightArmBot});
                    
                    lines.add(new org.joml.Vector3f[]{bodyTop, leftArmTop});
                    lines.add(new org.joml.Vector3f[]{leftArmTop, leftArmBot});
                    
                    lines.add(new org.joml.Vector3f[]{bodyBottom, rightLegTop});
                    lines.add(new org.joml.Vector3f[]{rightLegTop, rightLegBot});
                    
                    lines.add(new org.joml.Vector3f[]{bodyBottom, leftLegTop});
                    lines.add(new org.joml.Vector3f[]{leftLegTop, leftLegBot});
                    
                    moscow.rockstar.ui.hud.impl.TargetHud.SKELETON_LINES.clear();
                    moscow.rockstar.ui.hud.impl.TargetHud.SKELETON_LINES.put(entity.getId(), lines);
                }
            }
        }
        moscow.rockstar.systems.modules.modules.visuals.TargetESP targetESP = rockstar$getTargetESP();
        if (targetESP != null && targetESP.isEnabled() && targetESP.isChamsMode() && targetESP.getPrevTarget() != null && targetESP.getPrevTarget().getId() == entity.getId()) {
            if (instance != this.model) {
                return;
            }
            color = targetESP.getColor().withAlpha(127.0f).getRGB();
            light = 0xF000F0;
        }
        original.call(new Object[]{collector, instance, state, matrixStack, renderType, light, overlay, color, sprite, p9, crumbling});
    }
    @ModifyVariable(method = "getRenderType", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private boolean disableLocalPlayerOutline(boolean showOutline, S state) {
        if (showOutline) {
            Entity entity = ((EntityRenderStateAddition) state).rockstar$getEntity();
            moscow.rockstar.systems.modules.modules.visuals.TargetESP targetESP = rockstar$getTargetESP();
            if (targetESP != null && targetESP.isEnabled() && entity instanceof Player) {
                return false;
            }
            if (entity instanceof LocalPlayer) {
                Freelook freelook = rockstar$getFreelook();
                if (freelook != null && freelook.isFreelookActive()) {
                    return false;
                }
            }
        }
        return showOutline;
    }

    @ModifyReturnValue(method={"getRenderType"}, at={@At(value="RETURN")})
    private RenderType changeRenderLayer(RenderType original, S state, boolean showBody, boolean translucent, boolean showOutline) {
        Entity entity = ((moscow.rockstar.utility.mixins.EntityRenderStateAddition)state).rockstar$getEntity();
        if (entity instanceof LivingEntity) {
            moscow.rockstar.systems.modules.modules.visuals.TargetESP targetESP = rockstar$getTargetESP();
            if (targetESP != null && targetESP.isEnabled() && targetESP.isChamsMode() && targetESP.getPrevTarget() != null && targetESP.getPrevTarget().getId() == entity.getId()) {
                Identifier texture = getTextureLocation(state);
                return moscow.rockstar.systems.modules.modules.visuals.TargetESP.getChamsRenderType(texture);
            }
        }
        return original;
    }
}

