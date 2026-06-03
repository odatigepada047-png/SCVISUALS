package moscow.rockstar.mixin.minecraft.client.gui.screen;

import java.util.Optional;
import java.util.function.Consumer;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.ui.components.gif.Gif;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LoadingOverlay.class})
public class SplashOverlayMixin
implements IScaledResolution,
IMinecraft {
    @Unique
    private Gif daunGif;
    @Unique
    private Animation fadeOutAnimation;
    @Shadow
    private long fadeOutStart = -1L;
    @Final
    @Shadow
    private Consumer<Optional<Throwable>> onFinish;
    @Shadow
    @Final
    private ReloadInstance reload;
    @Shadow
    @Final
    private boolean fadeIn;
    @Shadow
    private long fadeInStart;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void init(Minecraft client, ReloadInstance monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading, CallbackInfo ci) {
        try {
            this.daunGif = new Gif(Rockstar.id("gifs/loading.gif"), 100.0f, 100.0f, 100.0f, 100.0f);
        } catch (RuntimeException ex) {
            Rockstar.LOGGER.error("Failed to load loading GIF (add assets/rockstar/gifs/loading.gif)", ex);
            this.daunGif = null;
        }
        this.fadeOutAnimation = new Animation(3000L, 1.0f, Easing.CUBIC_IN_OUT);
    }

    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void replaceRendering(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        float g;
        if (Rockstar.getInstance().isPanic()) {
            return;
        }
        ci.cancel();
        int width = context.guiWidth();
        int height = context.guiHeight();
        UIContext uiContext = UIContext.of(context, 0, 0, delta);
        long currentTime = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = currentTime;
        }
        float f = this.fadeOutStart > -1L ? (float)(currentTime - this.fadeOutStart) / 1000.0f : -1.0f;
        float f2 = g = this.fadeInStart > -1L ? (float)(currentTime - this.fadeInStart) / 500.0f : -1.0f;
        if (f >= 1.0f) {
            if (SplashOverlayMixin.mc.screen != null) {
                SplashOverlayMixin.mc.screen.extractRenderState(context, 0, 0, delta);
            }
            int k = Mth.ceil((float)((1.0f - Mth.clamp((float)(f - 1.0f), (float)0.0f, (float)1.0f)) * 255.0f));
            context.fill(0, 0, width, height, Colors.BLACK.withAlpha(k).getRGB());
        } else if (this.fadeIn && SplashOverlayMixin.mc.screen != null && g < 1.0f) {
            SplashOverlayMixin.mc.screen.extractRenderState(context, mouseX, mouseY, delta);
            int k = Mth.ceil((double)(Mth.clamp((double)g, (double)0.15, (double)1.0) * 255.0));
            context.fill(0, 0, width, height, Colors.BLACK.withAlpha(k).getRGB());
        }
        if (f < 1.0f && this.daunGif != null) {
            this.daunGif.set(0.0f, sr.getGuiScaledHeight() / 2.0f - sr.getGuiScaledWidth() / 1920.0f * 1080.0f / 2.0f, sr.getGuiScaledWidth(), sr.getGuiScaledWidth() / 1920.0f * 1080.0f);
            this.daunGif.setAlpha(1.0f);
            this.daunGif.render(uiContext);
        }
        if (f >= 2.0f) {
            mc.setOverlay(null);
            if (this.daunGif != null) {
                this.daunGif.dispose();
            }
        }
        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || g >= 2.0f)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            }
            catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }
            this.fadeOutStart = currentTime;
            if (SplashOverlayMixin.mc.screen != null) {
                SplashOverlayMixin.mc.screen.init(context.guiWidth(), context.guiHeight());
            }
        }
    }
}
