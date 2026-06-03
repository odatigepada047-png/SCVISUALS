package moscow.rockstar.utility.render.batching.impl;

import moscow.rockstar.framework.msdf.MsdfFont;
import moscow.rockstar.framework.msdf.MsdfRenderer;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.render.TextureBinder;
import moscow.rockstar.utility.render.batching.Batching;
import com.mojang.blaze3d.vertex.VertexFormat;

public class FadeOutBatching extends Batching {
    protected MsdfFont font;
    private final float fadeoutStart;
    private final float fadeoutEnd;
    private final float maxWidth;
    private final float x;

    public FadeOutBatching(VertexFormat vertexFormat, MsdfFont font, float fadeoutStart, float fadeoutEnd, float maxWidth, float x) {
        super(vertexFormat);
        this.font = font;
        this.fadeoutStart = fadeoutStart;
        this.fadeoutEnd = fadeoutEnd;
        this.maxWidth = maxWidth;
        this.x = x;
    }

    @Override
    public void draw() {
        this.font.bindTexture();
        GlProgram program = MsdfRenderer.useMsdfProgram();
        program.use();
        program.findUniform("Range").set(this.font.getAtlas().range());
        program.findUniform("Thickness").set(0.05f);
        program.findUniform("Smoothness").set(0.5f);
        program.findUniform("EnableFadeout").set(1.0f);
        program.findUniform("FadeoutStart").set(this.fadeoutStart);
        program.findUniform("FadeoutEnd").set(this.fadeoutEnd);
        program.findUniform("MaxWidth").set(this.maxWidth);
        program.findUniform("TextPosX").set(this.x);
        this.build();
        TextureBinder.unbind();
        GlProgram.clearActive();
        if (active == this) {
            active = null;
        }
    }
}
