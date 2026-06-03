/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.renderer.BufferBuilder
 *  net.minecraft.client.renderer.Tesselator
 *  net.minecraft.client.renderer.VertexFormat$DrawMode
 *  net.minecraft.client.renderer.DefaultVertexFormat
 *  net.minecraft.client.util.math.PoseStack
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package moscow.rockstar.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.utility.colors.ColorRGBA;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CrystalRenderer {
    private static final Vector3f[] VERTICES = new Vector3f[]{new Vector3f(0.0f, 1.5f, 0.0f), new Vector3f(0.0f, -1.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f)};
    private static final int[][] FACES = new int[][]{{0, 2, 4}, {0, 4, 3}, {0, 3, 5}, {0, 5, 2}, {1, 4, 2}, {1, 3, 4}, {1, 5, 3}, {1, 2, 5}};
    private static final float[] FACE_BRIGHTNESS = new float[]{1.0f, 0.8f, 0.6f, 0.9f, 0.7f, 0.5f, 0.4f, 0.6f};

    public static void render(PoseStack matrices, BufferBuilder buffer, float x, float y, float z, float size, ColorRGBA color) {
        CrystalRenderer.render(matrices, buffer, x, y, z, size, color, false);
    }

    public static void render(PoseStack matrices, BufferBuilder buffer, float x, float y, float z, float size, ColorRGBA color, boolean half) {
        matrices.pushPose();
        matrices.translate(x, y, z);
        matrices.scale(size, size, size);
        Matrix4f transformationMatrix = matrices.last().pose();
        int limit = half ? 4 : FACES.length;
        for (int i = 0; i < limit; ++i) {
            int[] face = FACES[i];
            float brightness = FACE_BRIGHTNESS[i];
            Vector3f v1 = VERTICES[face[0]];
            Vector3f v2 = VERTICES[face[1]];
            Vector3f v3 = VERTICES[face[2]];
            int shadedColor = CrystalRenderer.applyBrightness(color.getRGB(), brightness);
            buffer.addVertex(transformationMatrix, v1.x, v1.y, v1.z).setColor(shadedColor);
            buffer.addVertex(transformationMatrix, v2.x, v2.y, v2.z).setColor(shadedColor);
            buffer.addVertex(transformationMatrix, v3.x, v3.y, v3.z).setColor(shadedColor);
        }
        matrices.popPose();
    }
    public static BufferBuilder createBuffer() {
        CrystalRenderer.setupRenderState();
        return Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    }

    private static void setupRenderState() {
        // TODO: 26.1 - RenderSystem.setShader removed; pipeline setup needs rewrite
    }

    private static int applyBrightness(int color, float brightness) {
        int alpha = color >> 24 & 0xFF;
        int red = (int)((float)(color >> 16 & 0xFF) * brightness);
        int green = (int)((float)(color >> 8 & 0xFF) * brightness);
        int blue = (int)((float)(color & 0xFF) * brightness);
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    @Generated
    private CrystalRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

