package moscow.rockstar.mixin.minecraft.client.gui.screen;

import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.utility.render.GuiDrawContextHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class CustomScreenMixin extends net.minecraft.client.gui.components.events.AbstractContainerEventHandler {
    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("RETURN"))
    private void rockstar$captureScreenContext(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        GuiDrawContextHolder.capture(context, delta);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void rockstar$captureScreenContext2(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        GuiDrawContextHolder.capture(context, delta);
    }

    @Inject(method = "isPauseScreen", at = @At("HEAD"), cancellable = true)
    private void rockstar$noMenuPause(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof MenuScreen) {
            cir.setReturnValue(false);
        }
    }

}

