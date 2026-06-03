package moscow.rockstar.utility.render.batching.impl;

import moscow.rockstar.framework.msdf.MsdfFont;
import moscow.rockstar.framework.msdf.MsdfRenderer;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.render.TextureBinder;
import moscow.rockstar.utility.render.batching.Batching;
import com.mojang.blaze3d.vertex.VertexFormat;

public class FontBatching extends Batching {
    protected final MsdfFont font;

    public FontBatching(VertexFormat vertexFormat, MsdfFont font) {
        super(vertexFormat);
        this.font = font;
    }

    @Override
    public void draw() {
        this.font.bindTexture();
        GlProgram program = MsdfRenderer.useMsdfProgram();
        program.use();
        program.findUniform("Range").set(this.font.getAtlas().range());
        program.findUniform("Thickness").set(0.05f);
        program.findUniform("Smoothness").set(0.5f);
        program.findUniform("EnableFadeout").set(0.0f);
        this.build();
        TextureBinder.unbind();
        GlProgram.clearActive();
        if (active == this) {
            active = null;
        }
    }
}
