package moscow.rockstar.mixin.minecraft.render.item;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.render.HandRenderEvent;
import moscow.rockstar.systems.modules.modules.visuals.CustomHand;
import moscow.rockstar.utility.colors.Colors;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin {

    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true)
    private SubmitNodeCollector wrapVertexConsumers(SubmitNodeCollector original, AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices) {
        CustomHand customHand = Rockstar.getInstance().getModuleManager().getModule(CustomHand.class);
        if (customHand.isEnabled() && !customHand.isEffectOff()) {
            moscow.rockstar.utility.colors.ColorRGBA color = Colors.getAccentColor();
            float red = color.getRed() / 255.0f;
            float green = color.getGreen() / 255.0f;
            float blue = color.getBlue() / 255.0f;
            float alpha = customHand.getEffectAlpha();
            return new moscow.rockstar.utility.render.HandProvider(original, red, green, blue, alpha);
        }
        return original;
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void onRenderFirstPersonItem(AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, CallbackInfo ci) {
        boolean isMainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm mainArm = net.minecraft.client.Minecraft.getInstance().options.mainHand().get();
        HumanoidArm arm = isMainHand ? mainArm : mainArm.getOpposite();
        matrices.pushPose();
        HandRenderEvent handEvent = new HandRenderEvent(arm, swingProgress, item, equipProgress, matrices);
        Rockstar.getInstance().getEventManager().triggerEvent(handEvent);
        if (handEvent.isCancelled()) {
            matrices.popPose();
            ci.cancel();
        }
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void onRenderFirstPersonItemReturn(AbstractClientPlayer player, float tickProgress, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector submitNodeCollector, int light, CallbackInfo ci) {
        matrices.popPose();
    }

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void onRenderItem(net.minecraft.world.entity.LivingEntity mob, ItemStack itemStack, ItemDisplayContext type, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        CustomHand customHand = Rockstar.getInstance().getModuleManager().getModule(CustomHand.class);
        if (customHand.isEnabled()) {
            HumanoidArm mainArm = net.minecraft.client.Minecraft.getInstance().options.mainHand().get();
            boolean isMainHand = (type == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && mainArm == HumanoidArm.RIGHT) ||
                                 (type == ItemDisplayContext.FIRST_PERSON_LEFT_HAND && mainArm == HumanoidArm.LEFT);
            
            float x = isMainHand ? customHand.getMainOffsetX().getCurrentValue() : customHand.getOffOffsetX().getCurrentValue();
            float y = isMainHand ? customHand.getMainOffsetY().getCurrentValue() : customHand.getOffOffsetY().getCurrentValue();
            float z = isMainHand ? customHand.getMainOffsetZ().getCurrentValue() : customHand.getOffOffsetZ().getCurrentValue();

            HumanoidArm arm = (type == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            float direction = (arm == HumanoidArm.LEFT) ? -1.0f : 1.0f;
            poseStack.translate(x * direction, y, z);

            float scale = isMainHand ? customHand.getMainScale().getCurrentValue() : customHand.getOffScale().getCurrentValue();
            poseStack.scale(scale, scale, scale);
        }
    }

    @Inject(method = "renderPlayerArm", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;getPlayerRenderer(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;"))
    private void onRenderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm, CallbackInfo ci) {
        CustomHand customHand = Rockstar.getInstance().getModuleManager().getModule(CustomHand.class);
        if (customHand.isEnabled()) {
            HumanoidArm mainArm = net.minecraft.client.Minecraft.getInstance().options.mainHand().get();
            boolean isMainHand = (arm == mainArm);
            
            float x = isMainHand ? customHand.getMainOffsetX().getCurrentValue() : customHand.getOffOffsetX().getCurrentValue();
            float y = isMainHand ? customHand.getMainOffsetY().getCurrentValue() : customHand.getOffOffsetY().getCurrentValue();
            float z = isMainHand ? customHand.getMainOffsetZ().getCurrentValue() : customHand.getOffOffsetZ().getCurrentValue();

            float direction = (arm == HumanoidArm.LEFT) ? -1.0f : 1.0f;
            poseStack.translate(x * direction, y, z);

            float scale = isMainHand ? customHand.getMainScale().getCurrentValue() : customHand.getOffScale().getCurrentValue();
            poseStack.scale(scale, scale, scale);
        }
    }

    @Inject(method = "renderMapHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;getPlayerRenderer(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;"))
    private void onRenderMapHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, HumanoidArm arm, CallbackInfo ci) {
        CustomHand customHand = Rockstar.getInstance().getModuleManager().getModule(CustomHand.class);
        if (customHand.isEnabled()) {
            HumanoidArm mainArm = net.minecraft.client.Minecraft.getInstance().options.mainHand().get();
            boolean isMainHand = (arm == mainArm);
            
            float x = isMainHand ? customHand.getMainOffsetX().getCurrentValue() : customHand.getOffOffsetX().getCurrentValue();
            float y = isMainHand ? customHand.getMainOffsetY().getCurrentValue() : customHand.getOffOffsetY().getCurrentValue();
            float z = isMainHand ? customHand.getMainOffsetZ().getCurrentValue() : customHand.getOffOffsetZ().getCurrentValue();

            float direction = (arm == HumanoidArm.LEFT) ? -1.0f : 1.0f;
            poseStack.translate(x * direction, y, z);

            float scale = isMainHand ? customHand.getMainScale().getCurrentValue() : customHand.getOffScale().getCurrentValue();
            poseStack.scale(scale, scale, scale);
        }
    }
}
