package moscow.rockstar.mixin.minecraft.client.render.block.entity;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.ShulkerPreview;
import moscow.rockstar.utility.interfaces.IShulkerRenderState;
import moscow.rockstar.utility.render.ShulkerOverlayCollector;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxRenderer.class)
public class ShulkerBoxBlockEntityRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/ShulkerBoxBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/ShulkerBoxRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("RETURN"))
    private void onExtractRenderState(ShulkerBoxBlockEntity entity, ShulkerBoxRenderState state, float tickProgress, Vec3 offset, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        if (!(state instanceof IShulkerRenderState shulkerState)) {
            return;
        }
        ShulkerPreview shulkerPreview = Rockstar.getInstance().getModuleManager().getModule(ShulkerPreview.class);
        if (shulkerPreview.isEnabled() && shulkerPreview.shouldHighlight(entity)) {
            shulkerState.rockstar$setHighlight(true);
            shulkerState.rockstar$setColor(shulkerPreview.color.getColor().getRGB());
            shulkerState.rockstar$setUseChams(true);
            shulkerState.rockstar$setScale(1.0f);
        } else {
            shulkerState.rockstar$setHighlight(false);
            shulkerState.rockstar$setScale(1.0f);
            shulkerState.rockstar$setColor(-1);
            shulkerState.rockstar$setUseChams(false);
        }
    }

    @ModifyVariable(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ShulkerBoxRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), argsOnly = true)
    private SubmitNodeCollector wrapCollector(SubmitNodeCollector original, ShulkerBoxRenderState state) {
        if (state instanceof IShulkerRenderState shulkerState && shulkerState.rockstar$shouldHighlight()) {
            return new ShulkerOverlayCollector(original, shulkerState.rockstar$getColor(), shulkerState.rockstar$shouldUseChams());
        }
        return original;
    }
}
