package moscow.rockstar.mixin.minecraft.client.render.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.Rockstar;
import moscow.rockstar.mixin.accessors.BipedEntityModelAccessor;
import moscow.rockstar.systems.modules.modules.visuals.FriendMarkers;
import moscow.rockstar.utility.mixins.EntityRenderStateAddition;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ModelFeatureRenderer.class})
public abstract class ModelFeatureRendererMixin {

    @WrapOperation(
        method="renderModel(Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;Lnet/minecraft/client/renderer/rendertype/RenderType;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/OutlineBufferSource;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V",
        at=@At(value="INVOKE", target="Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"),
        require=1
    )
    private void scaleFriendHead(Model instance, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color, Operation<Void> original, @Local(argsOnly=true) net.minecraft.client.renderer.SubmitNodeStorage.ModelSubmit submit) {
        boolean scaleFriendHead = false;
        float oldHeadXScale = 1.0f, oldHeadYScale = 1.0f, oldHeadZScale = 1.0f;
        float oldHatXScale = 1.0f, oldHatYScale = 1.0f, oldHatZScale = 1.0f;

        Object state = submit.state();
        if (state instanceof LivingEntityRenderState livingState) {
            Entity entity = ((EntityRenderStateAddition) livingState).rockstar$getEntity();
            if (entity instanceof Player player) {
                FriendMarkers markers = Rockstar.getInstance().getModuleManager().getModule(FriendMarkers.class);
                if (markers != null && markers.isEnabled() && markers.getHeads().isSelected() && Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())) {
                    if (instance instanceof HumanoidModel model) {
                        BipedEntityModelAccessor accessor = (BipedEntityModelAccessor) model;
                        float scale = 1.5f;
                        ModelPart head = accessor.rockstar$getHead();
                        ModelPart hat = accessor.rockstar$getHat();
                        oldHeadXScale = head.xScale;
                        oldHeadYScale = head.yScale;
                        oldHeadZScale = head.zScale;
                        oldHatXScale = hat.xScale;
                        oldHatYScale = hat.yScale;
                        oldHatZScale = hat.zScale;
                        head.xScale = scale;
                        head.yScale = scale;
                        head.zScale = scale;
                        hat.xScale = scale;
                        hat.yScale = scale;
                        hat.zScale = scale;
                        scaleFriendHead = true;
                    }
                }
            }
        }

        original.call(new Object[]{instance, poseStack, consumer, light, overlay, color});

        if (scaleFriendHead && instance instanceof HumanoidModel) {
            BipedEntityModelAccessor accessor = (BipedEntityModelAccessor) instance;
            ModelPart head = accessor.rockstar$getHead();
            ModelPart hat = accessor.rockstar$getHat();
            head.xScale = oldHeadXScale;
            head.yScale = oldHeadYScale;
            head.zScale = oldHeadZScale;
            hat.xScale = oldHatXScale;
            hat.yScale = oldHatYScale;
            hat.zScale = oldHatZScale;
        }
    }
}
