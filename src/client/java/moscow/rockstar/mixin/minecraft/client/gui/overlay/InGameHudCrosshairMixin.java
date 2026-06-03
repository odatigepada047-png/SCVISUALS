package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.CustomCrosshair;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudCrosshairMixin {
    
    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        CustomCrosshair customCrosshair = Rockstar.getInstance().getModuleManager().getModule(CustomCrosshair.class);
        if (customCrosshair != null && customCrosshair.isEnabled()) {
            ci.cancel();
            // Обязательно класть геометрию в очередь GuiRenderer (как ваниль), иначе MeshDrawHelper с HudRenderEvent не виден.
            customCrosshair.renderIntoExtractor(context, tickCounter);
        }
    }
}
