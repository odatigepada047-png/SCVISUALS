package moscow.rockstar.framework.msdf;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.systems.modules.modules.other.HiderUtils;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.MeshDrawHelper;
import moscow.rockstar.utility.render.TextureBinder;
import moscow.rockstar.utility.render.batching.Batching;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public final class MsdfRenderer {
    private static GlProgram msdfProgram;

    public static GlProgram useMsdfProgram() {
        if (msdfProgram == null) {
            msdfProgram = new GlProgram(ResourceProvider.getShaderIdentifier("msdf_font/data"), DefaultVertexFormat.POSITION_TEX_COLOR);
            GlProgram.loadAndSetupPrograms();
        }
        return msdfProgram;
    }

    public static void renderText(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z) {
        MsdfRenderer.renderText(font, text, size, color, matrix, x, y, z, false, 0.0f, 1.0f, 0.0f);
    }

    public static void renderText(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z, boolean enableFadeout, float fadeoutStart, float fadeoutEnd, float maxWidth) {
        text = text.replace("\u0456", "i").replace("\u0406", "I");
        float thickness = 0.05f;
        float smoothness = 0.5f;
        float spacing = 0.0f;
        HiderUtils nameProtectModule = Rockstar.getInstance().getModuleManager().getModule(HiderUtils.class);
        if (nameProtectModule.isEnabled()) {
            text = nameProtectModule.patchName(text);
        }
        if (Batching.getActive() != null) {
            font.applyGlyphs(matrix, Batching.getActive().getBuilder(), text, size, thickness * 0.5f * size, spacing, x - 0.75f, y + size * 0.7f, z, color);
            return;
        }
        font.bindTexture();
        GlProgram program = useMsdfProgram();
        program.use();
        program.findUniform("Range").set(font.getAtlas().range());
        program.findUniform("Thickness").set(thickness);
        program.findUniform("Smoothness").set(smoothness);
        program.findUniform("EnableFadeout").set(enableFadeout ? 1.0f : 0.0f);
        program.findUniform("FadeoutStart").set(fadeoutStart);
        program.findUniform("FadeoutEnd").set(fadeoutEnd);
        program.findUniform("MaxWidth").set(maxWidth);
        program.findUniform("TextPosX").set(x);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        font.applyGlyphs(matrix, builder, text, size, thickness * 0.5f * size, spacing, x - 0.75f, y + size * 0.7f, z, color);
        MeshData builtBuffer = builder.build();
        if (builtBuffer != null) {
            if (builtBuffer.drawState().vertexCount() > 0) {
                MeshDrawHelper.drawBuilt(builtBuffer);
            } else {
                builtBuffer.close();
            }
        }
        TextureBinder.unbind();
        GlProgram.clearActive();
    }

    public static void renderText(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z, boolean enableFadeout, float fadeoutStart, float fadeoutEnd) {
        float maxWidth = font.getWidth(text, size) * 2.0f;
        MsdfRenderer.renderText(font, text, size, color, matrix, x, y, z, enableFadeout, fadeoutStart, fadeoutEnd, maxWidth);
    }

    public static void renderText(MsdfFont font, Component text, float size, Matrix4f matrix, float x, float y, float z) {
        MsdfRenderer.renderText(font, text, size, matrix, x, y, z, false, 0.0f, 1.0f, 0.0f);
    }

    public static void renderText(MsdfFont font, Component text, float size, Matrix4f matrix, float x, float y, float z, boolean enableFadeout, float fadeoutStart, float fadeoutEnd, float maxWidth) {
        float thickness = 0.05f;
        float smoothness = 0.5f;
        float spacing = 0.0f;
        List<FormattedTextProcessor.TextSegment> segments = FormattedTextProcessor.processText(text, Colors.WHITE.getRGB());
        float currentX = x;
        if (Batching.getActive() != null) {
            BufferBuilder activeBuilder = Batching.getActive().getBuilder();
            for (FormattedTextProcessor.TextSegment segment : segments) {
                font.applyGlyphs(matrix, activeBuilder, segment.text, size, thickness * 0.5f * size, spacing - 0.3f, currentX - 0.75f, y + size * 0.7f, z, segment.color);
                currentX += font.getWidth(segment.text, size);
            }
            return;
        }
        font.bindTexture();
        GlProgram program = useMsdfProgram();
        program.use();
        program.findUniform("Range").set(font.getAtlas().range());
        program.findUniform("Thickness").set(thickness);
        program.findUniform("Smoothness").set(smoothness);
        program.findUniform("EnableFadeout").set(enableFadeout ? 1.0f : 0.0f);
        program.findUniform("FadeoutStart").set(fadeoutStart);
        program.findUniform("FadeoutEnd").set(fadeoutEnd);
        program.findUniform("MaxWidth").set(maxWidth);
        program.findUniform("TextPosX").set(x);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (FormattedTextProcessor.TextSegment segment : segments) {
            font.applyGlyphs(matrix, builder, segment.text, size, thickness * 0.5f * size, spacing - 0.3f, currentX - 0.75f, y + size * 0.7f, z, segment.color);
            currentX += font.getWidth(segment.text, size);
        }
        MeshData builtBuffer = builder.build();
        if (builtBuffer != null) {
            if (builtBuffer.drawState().vertexCount() > 0) {
                MeshDrawHelper.drawBuilt(builtBuffer);
            } else {
                builtBuffer.close();
            }
        }
        TextureBinder.unbind();
        GlProgram.clearActive();
    }

    public static void renderText(MsdfFont font, Component text, float size, Matrix4f matrix, float x, float y, float z, boolean enableFadeout, float fadeoutStart, float fadeoutEnd) {
        float maxWidth = font.getTextWidth(text, size) * 2.0f;
        MsdfRenderer.renderText(font, text, size, matrix, x, y, z, enableFadeout, fadeoutStart, fadeoutEnd, maxWidth);
    }

    @Generated
    private MsdfRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
