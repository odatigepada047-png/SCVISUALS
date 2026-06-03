package moscow.rockstar.mixin.minecraft.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.ShulkerPreview;
import moscow.rockstar.utility.mixins.EntityRenderStateAddition;
import moscow.rockstar.utility.render.ShulkerOverlayCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {

    @Unique
    private boolean isShulkerItem(ItemEntityRenderState state) {
        Entity entity = ((EntityRenderStateAddition) state).rockstar$getEntity();
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock;
        }
        return false;
    }

    @Unique
    private float getShulkerScale(ItemEntityRenderState state) {
        ShulkerPreview shulkerPreview = Rockstar.getInstance().getModuleManager().getModule(ShulkerPreview.class);
        if (shulkerPreview != null && shulkerPreview.isEnabled() && shulkerPreview.scale.isEnabled()) {
            if (isShulkerItem(state)) {
                Entity entity = ((EntityRenderStateAddition) state).rockstar$getEntity();
                if (entity instanceof ItemEntity itemEntity) {
                    if (shulkerPreview.shouldHighlight(itemEntity.getItem())) {
                        return shulkerPreview.scaleValue.getCurrentValue();
                    }
                }
            }
        }
        return 1.0f;
    }

    @Unique
    private int getShulkerColor(ItemEntityRenderState state) {
        ShulkerPreview shulkerPreview = Rockstar.getInstance().getModuleManager().getModule(ShulkerPreview.class);
        if (shulkerPreview != null && shulkerPreview.isEnabled()) {
            if (isShulkerItem(state)) {
                Entity entity = ((EntityRenderStateAddition) state).rockstar$getEntity();
                if (entity instanceof ItemEntity itemEntity) {
                    if (shulkerPreview.shouldHighlight(itemEntity.getItem())) {
                        return shulkerPreview.color.getColor().getRGB();
                    }
                }
            }
        }
        return -1;
    }

    @Unique
    private boolean getShulkerChams(ItemEntityRenderState state) {
        ShulkerPreview shulkerPreview = Rockstar.getInstance().getModuleManager().getModule(ShulkerPreview.class);
        if (shulkerPreview != null && shulkerPreview.isEnabled()) {
            if (isShulkerItem(state)) {
                Entity entity = ((EntityRenderStateAddition) state).rockstar$getEntity();
                if (entity instanceof ItemEntity itemEntity) {
                    if (shulkerPreview.shouldHighlight(itemEntity.getItem())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @ModifyVariable(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), argsOnly = true)
    private SubmitNodeCollector wrapCollector(SubmitNodeCollector original, ItemEntityRenderState state) {
        int color = getShulkerColor(state);
        if (color != -1) {
            return new ShulkerOverlayCollector(original, color, getShulkerChams(state));
        }
        return original;
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"))
    private void onSubmitHead(ItemEntityRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraState, CallbackInfo ci) {
        float s = getShulkerScale(state);
        if (s != 1.0f) {
            matrices.pushPose();
            matrices.scale(s, s, s);
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("RETURN"))
    private void onSubmitReturn(ItemEntityRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraState, CallbackInfo ci) {
        float s = getShulkerScale(state);
        if (s != 1.0f) {
            matrices.popPose();
        }
    }
}
