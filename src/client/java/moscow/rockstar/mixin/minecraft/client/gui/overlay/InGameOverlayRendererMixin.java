package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ScreenEffectRenderer.class})
public class InGameOverlayRendererMixin {
    @Inject(method={"renderScreenEffect"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderScreenEffectHook(boolean inWall, boolean onFire, float tickDelta, net.minecraft.client.renderer.SubmitNodeCollector collector, boolean translucent, CallbackInfo ci) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (!removals.isEnabled()) {
            return;
        }
        if (onFire && removals.getFire().isSelected()) {
            ci.cancel();
            return;
        }
        if (inWall && removals.getClip().isSelected()) {
            ci.cancel();
        }
    }
}
