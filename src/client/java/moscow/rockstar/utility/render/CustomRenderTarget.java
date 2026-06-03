package moscow.rockstar.utility.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IWindow;
import org.jetbrains.annotations.Nullable;

public class CustomRenderTarget extends TextureTarget implements IMinecraft, IWindow {
    private static @Nullable CustomRenderTarget active;

    private boolean linear;
    private float downscale = 1.0f;

    public CustomRenderTarget(boolean useDepth) {
        super("rockstar_rt", 1, 1, useDepth);
    }

    public CustomRenderTarget(int width, int height, boolean useDepth) {
        super("rockstar_rt", width, height, useDepth);
    }

    public CustomRenderTarget setLinear() {
        this.linear = true;
        return this;
    }

    public CustomRenderTarget setDownscale(float factor) {
        this.downscale = Math.max(0.1f, Math.min(1.0f, factor));
        return this;
    }

    private void resizeFramebuffer() {
        if (this.needsNewFramebuffer()) {
            int targetWidth = Math.max((int) Math.floor(mw.getGuiScaledWidth() * this.downscale), 1);
            int targetHeight = Math.max((int) Math.floor(mw.getGuiScaledHeight() * this.downscale), 1);
            this.resize(targetWidth, targetHeight);
        }
    }

    public void setup(boolean clear) {
        this.resizeFramebuffer();
        if (clear) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.getColorTexture(), 0);
        }
        active = this;
    }

    public void setup() {
        this.setup(true);
    }

    public void stop() {
        if (active == this) {
            active = null;
        }
    }

    public static @Nullable GpuTextureView getActiveColorView() {
        return active != null ? active.getColorTextureView() : null;
    }

    /**
     * Clears which RT {@link MeshDrawHelper} treats as the color target — must not leak across frames.
     */
    public static void clearActiveBinding() {
        active = null;
    }

    private boolean needsNewFramebuffer() {
        int targetWidth = Math.max((int) Math.floor(mw.getGuiScaledWidth() * this.downscale), 1);
        int targetHeight = Math.max((int) Math.floor(mw.getGuiScaledHeight() * this.downscale), 1);
        return this.width != targetWidth || this.height != targetHeight;
    }
}
