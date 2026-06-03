package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.CustomFog;
import moscow.rockstar.utility.colors.ColorRGBA;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={FogRenderer.class})
public class BackgroundRendererMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void onSetupFog(Camera camera, int fogType, DeltaTracker deltaTracker, float viewDistance, ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        FogData data = cir.getReturnValue();
        if (data != null) {
            CustomFog module = Rockstar.getInstance().getModuleManager().getModule(CustomFog.class);
            if (module != null && module.shouldModifyFog(camera)) {
                float start = module.getDistance().getFirstValue();
                float end = module.getDistance().getSecondValue();
                data.environmentalStart = start;
                data.renderDistanceStart = start;
                data.environmentalEnd = end;
                data.renderDistanceEnd = end;

                ColorRGBA color = module.getColor();
                data.color.set(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
            }
        }
    }
}
