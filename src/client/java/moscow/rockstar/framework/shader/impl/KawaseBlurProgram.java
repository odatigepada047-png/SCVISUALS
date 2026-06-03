package moscow.rockstar.framework.shader.impl;

import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.interfaces.IWindow;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.Identifier;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;

public class KawaseBlurProgram extends GlProgram implements IWindow {
    public KawaseBlurProgram(Identifier identifier) {
        super(identifier, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    @CompileBytecode
    public void updateUniforms(float offset) {
        this.findUniform("Offset").set(offset);
        this.findUniform("Resolution").set(1.0f / mw.getGuiScaledWidth(), 1.0f / mw.getGuiScaledHeight());
        this.findUniform("Saturation").set(1.0f);
        this.findUniform("TintIntensity").set(0.0f);
        this.findUniform("TintColor").set(1.0f, 1.0f, 1.0f);
    }

    public void updateUniforms(float offset, int textureWidth, int textureHeight) {
        this.findUniform("Offset").set(offset);
        float invW = textureWidth > 0 ? 1.0f / textureWidth : 0.0f;
        float invH = textureHeight > 0 ? 1.0f / textureHeight : 0.0f;
        this.findUniform("Resolution").set(invW, invH);
        this.findUniform("Saturation").set(1.0f);
        this.findUniform("TintIntensity").set(0.0f);
        this.findUniform("TintColor").set(1.0f, 1.0f, 1.0f);
    }

    @Override
    protected void setup() {
        super.setup();
    }
}
