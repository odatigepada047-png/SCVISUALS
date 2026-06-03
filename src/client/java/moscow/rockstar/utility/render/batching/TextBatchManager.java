package moscow.rockstar.utility.render.batching;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.framework.msdf.MsdfFont;
import moscow.rockstar.framework.msdf.MsdfRenderer;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.render.MeshDrawHelper;
import moscow.rockstar.utility.render.TextureBinder;
import org.joml.Matrix4f;

public final class TextBatchManager {
    private static final Map<Integer, Batch> batches = new HashMap<>();
    private static boolean globalBegun = false;

    private TextBatchManager() {
    }

    public static void beginFrame() {
        if (globalBegun) {
            return;
        }
        globalBegun = true;
    }

    public static void addText(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z, float thickness, float spacing) {
        if (!globalBegun) {
            beginFrame();
        }
        Batch batch = batches.computeIfAbsent(font.getTextureId(), key -> {
            GlProgram program = MsdfRenderer.useMsdfProgram();
            font.bindTexture();
            program.findUniform("Range").set(font.getAtlas().range());
            program.findUniform("Thickness").set(thickness);
            program.findUniform("Smoothness").set(0.5f);
            program.findUniform("EnableFadeout").set(0.0f);
            return new Batch(font);
        });
        font.applyGlyphs(matrix, batch.builder, text, size, thickness * 0.5f * size, spacing, x - 0.75f, y + size * 0.7f, z, color);
    }

    public static void addTextWithFade(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z, float thickness, float spacing, float fadeoutStart, float fadeoutEnd, float maxWidth, float textPosX) {
        if (!globalBegun) {
            beginFrame();
        }
        Batch batch = batches.computeIfAbsent(font.getTextureId(), key -> {
            GlProgram program = MsdfRenderer.useMsdfProgram();
            font.bindTexture();
            program.findUniform("Range").set(font.getAtlas().range());
            program.findUniform("Thickness").set(thickness);
            program.findUniform("Smoothness").set(0.5f);
            return new Batch(font);
        });
        GlProgram program = MsdfRenderer.useMsdfProgram();
        font.bindTexture();
        program.findUniform("EnableFadeout").set(1.0f);
        program.findUniform("FadeoutStart").set(fadeoutStart);
        program.findUniform("FadeoutEnd").set(fadeoutEnd);
        program.findUniform("MaxWidth").set(maxWidth);
        program.findUniform("TextPosX").set(textPosX);
        font.applyGlyphs(matrix, batch.builder, text, size, thickness * 0.5f * size, spacing, x - 0.75f, y + size * 0.7f, z, color);
    }

    public static void endFrame() {
        if (!globalBegun) {
            return;
        }
        GlProgram program = MsdfRenderer.useMsdfProgram();
        program.use();
        for (Batch batch : batches.values()) {
            batch.font.bindTexture();
            MeshData mesh = batch.builder.build();
            if (mesh != null) {
                MeshDrawHelper.drawBuilt(mesh);
            }
        }
        TextureBinder.unbind();
        GlProgram.clearActive();
        batches.clear();
        globalBegun = false;
    }

    private static final class Batch {
        final MsdfFont font;
        final BufferBuilder builder;

        Batch(MsdfFont font) {
            this.font = font;
            this.builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        }
    }
}
