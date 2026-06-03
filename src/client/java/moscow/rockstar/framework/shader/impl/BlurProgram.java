package moscow.rockstar.framework.shader.impl;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IWindow;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.render.CustomRenderTarget;
import moscow.rockstar.utility.render.MeshDrawHelper;
import moscow.rockstar.utility.render.TextureBinder;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.renderer.RenderPipelines;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class BlurProgram implements IMinecraft, IWindow {
    public static final Supplier<CustomRenderTarget> CACHE = Suppliers.memoize(() -> new CustomRenderTarget(false).setLinear());
    public static final Supplier<CustomRenderTarget> BUFFER = Suppliers.memoize(() -> new CustomRenderTarget(false).setLinear());
    private final Timer timer = new Timer();
    private static KawaseBlurProgram kawaseDownProgram;
    private static KawaseBlurProgram kawaseUpProgram;
    private float blurOffset = 1.0f;
    private float blurDownscale = 0.5f;

    @Compile
    public void initShaders() {
        kawaseDownProgram = new KawaseBlurProgram(Rockstar.id("kawase_down/data"));
        kawaseUpProgram = new KawaseBlurProgram(Rockstar.id("kawase_up/data"));
    }

    public void draw() {
        this.draw(false);
    }

    public void draw(boolean menuOpen) {
        long interval = 25L;
        if (!this.timer.finished(interval)) {
            return;
        }
        this.timer.reset();
        this.blurOffset = 1.0f;
        CustomRenderTarget cache = CACHE.get();
        CustomRenderTarget buffer = BUFFER.get();
        cache.setDownscale(this.blurDownscale).setLinear();
        buffer.setDownscale(this.blurDownscale).setLinear();

        kawaseDownProgram.use();
        cache.setup();
        TextureBinder.bindMainColor();
        kawaseDownProgram.updateUniforms(this.blurOffset, mc.getMainRenderTarget().width, mc.getMainRenderTarget().height);
        this.drawFullscreenQuad(mw.getGuiScaledWidth(), mw.getGuiScaledHeight());
        cache.stop();

        CustomRenderTarget[] buffers = new CustomRenderTarget[]{cache, buffer};
        for (int i = 1; i < 3; i++) {
            int step = i % 2;
            buffers[step].setup();
            TextureBinder.bindCustomTarget(buffers[(step + 1) % 2]);
            kawaseDownProgram.updateUniforms(this.blurOffset, buffers[(step + 1) % 2].width, buffers[(step + 1) % 2].height);
            this.drawFullscreenQuad(mw.getGuiScaledWidth(), mw.getGuiScaledHeight());
            buffers[step].stop();
        }

        kawaseUpProgram.use();
        for (int i = 0; i < 3; i++) {
            int step = i % 2;
            CustomRenderTarget dst = buffers[(step + 1) % 2];
            dst.setup();
            TextureBinder.bindCustomTarget(buffers[step]);
            kawaseUpProgram.updateUniforms(this.blurOffset, buffers[step].width, buffers[step].height);
            this.drawFullscreenQuad(mw.getGuiScaledWidth(), mw.getGuiScaledHeight());
            dst.stop();
        }
        TextureBinder.unbind();
    }

    private void drawFullscreenQuad(float width, float height) {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        int color = -1;
        builder.addVertex(0.0f, 0.0f, 0.0f).setUv(0.0f, 1.0f).setColor(color);
        builder.addVertex(0.0f, height, 0.0f).setUv(0.0f, 0.0f).setColor(color);
        builder.addVertex(width, height, 0.0f).setUv(1.0f, 0.0f).setColor(color);
        builder.addVertex(width, 0.0f, 0.0f).setUv(1.0f, 1.0f).setColor(color);
        GlProgram active = GlProgram.getActive();
        MeshDrawHelper.draw(builder.build(), active != null && active.getPipeline() != null ? active.getPipeline() : RenderPipelines.GUI_TEXTURED, TextureBinder.lastBinding != null ? TextureBinder.lastBinding.view() : null, TextureBinder.lastBinding != null ? TextureBinder.lastBinding.sampler() : null);
    }

    public static CustomRenderTarget getBlurTarget() {
        return BUFFER.get();
    }

    @Generated
    public void setBlurOffset(float blurOffset) {
        this.blurOffset = blurOffset;
    }

    @Generated
    public void setBlurDownscale(float blurDownscale) {
        this.blurDownscale = blurDownscale;
    }
}
