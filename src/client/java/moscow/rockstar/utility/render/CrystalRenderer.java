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
import java.util.ArrayList;
import java.util.List;

public final class CrystalRenderer {
    private static final Vector3f[] VERTICES = new Vector3f[]{new Vector3f(0.0f, 1.5f, 0.0f), new Vector3f(0.0f, -1.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f)};
    private static final int[][] FACES = new int[][]{{0, 2, 4}, {0, 4, 3}, {0, 3, 5}, {0, 5, 2}, {1, 4, 2}, {1, 3, 4}, {1, 5, 3}, {1, 2, 5}};
    private static final float[] FACE_BRIGHTNESS = new float[]{1.0f, 0.8f, 0.6f, 0.9f, 0.7f, 0.5f, 0.4f, 0.6f};

    private static final String[] SWORD_GRID = {
        ".............OOO",
        "............OBBO",
        "...........OBBSS",
        "..........OBBSS.",
        ".........OBBSS..",
        "........OBBSS...",
        "..OO...OBBSS....",
        "..OGO.OBBSS.....",
        "...OGGGBBS......",
        "...OGGBBS.......",
        "....OGO.........",
        "...OOHOO........",
        "..OOH.OOH.......",
        "OOHO....OO......",
        "OOO.............",
        "OO.............."
    };

    private static final List<Vector3f> SWORD_VERTICES = new ArrayList<>();
    private static final List<int[]> SWORD_FACES = new ArrayList<>();
    private static final List<Character> SWORD_VOXEL_TYPES = new ArrayList<>();

    static {
        int width = 16;
        int height = 16;
        float scale = 1.0f / 16.0f;
        float thickness = scale * 2.0f;

        int vertexIndex = 0;
        for (int y = 0; y < height; y++) {
            String row = SWORD_GRID[y];
            for (int x = 0; x < width; x++) {
                char type = row.charAt(x);
                if (type != '.') {
                    float minX = (x - width / 2.0f) * scale;
                    float minY = (y - height / 2.0f) * scale;
                    float minZ = -thickness / 2.0f;

                    float maxX = minX + scale;
                    float maxY = minY + scale;
                    float maxZ = thickness / 2.0f;

                    SWORD_VERTICES.add(new Vector3f(minX, minY, minZ));
                    SWORD_VERTICES.add(new Vector3f(maxX, minY, minZ));
                    SWORD_VERTICES.add(new Vector3f(maxX, maxY, minZ));
                    SWORD_VERTICES.add(new Vector3f(minX, maxY, minZ));
                    SWORD_VERTICES.add(new Vector3f(minX, minY, maxZ));
                    SWORD_VERTICES.add(new Vector3f(maxX, minY, maxZ));
                    SWORD_VERTICES.add(new Vector3f(maxX, maxY, maxZ));
                    SWORD_VERTICES.add(new Vector3f(minX, maxY, maxZ));

                    SWORD_FACES.add(new int[]{vertexIndex + 4, vertexIndex + 5, vertexIndex + 6});
                    SWORD_FACES.add(new int[]{vertexIndex + 4, vertexIndex + 6, vertexIndex + 7});
                    SWORD_VOXEL_TYPES.add(type);
                    SWORD_VOXEL_TYPES.add(type);

                    SWORD_FACES.add(new int[]{vertexIndex + 1, vertexIndex + 0, vertexIndex + 3});
                    SWORD_FACES.add(new int[]{vertexIndex + 1, vertexIndex + 3, vertexIndex + 2});
                    SWORD_VOXEL_TYPES.add(type);
                    SWORD_VOXEL_TYPES.add(type);

                    SWORD_FACES.add(new int[]{vertexIndex + 0, vertexIndex + 4, vertexIndex + 7});
                    SWORD_FACES.add(new int[]{vertexIndex + 0, vertexIndex + 7, vertexIndex + 3});
                    SWORD_VOXEL_TYPES.add(type);
                    SWORD_VOXEL_TYPES.add(type);

                    SWORD_FACES.add(new int[]{vertexIndex + 5, vertexIndex + 1, vertexIndex + 2});
                    SWORD_FACES.add(new int[]{vertexIndex + 5, vertexIndex + 2, vertexIndex + 6});
                    SWORD_VOXEL_TYPES.add(type);
                    SWORD_VOXEL_TYPES.add(type);

                    SWORD_FACES.add(new int[]{vertexIndex + 7, vertexIndex + 6, vertexIndex + 2});
                    SWORD_FACES.add(new int[]{vertexIndex + 7, vertexIndex + 2, vertexIndex + 3});
                    SWORD_VOXEL_TYPES.add(type);
                    SWORD_VOXEL_TYPES.add(type);

                    SWORD_FACES.add(new int[]{vertexIndex + 0, vertexIndex + 1, vertexIndex + 5});
                    SWORD_FACES.add(new int[]{vertexIndex + 0, vertexIndex + 5, vertexIndex + 4});
                    SWORD_VOXEL_TYPES.add(type);
                    SWORD_VOXEL_TYPES.add(type);

                    vertexIndex += 8;
                }
            }
        }
    }

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

    private static ColorRGBA getVoxelColor(char type, ColorRGBA themeColor) {
        switch (type) {
            case 'O': // Outline: slightly darker theme color
                return themeColor.mix(ColorRGBA.BLACK, 0.35f).withAlpha(themeColor.getAlpha());
            case 'B': // Blade center: full bright theme color
                return themeColor;
            case 'S': // Blade shadow/edge: a bit darker theme color
                return themeColor.mix(ColorRGBA.BLACK, 0.15f).withAlpha(themeColor.getAlpha());
            case 'G': // Guard: slightly darker theme color
                return themeColor.mix(ColorRGBA.BLACK, 0.10f).withAlpha(themeColor.getAlpha());
            case 'H': // Hilt/Handle: medium dark theme color
                return themeColor.mix(ColorRGBA.BLACK, 0.30f).withAlpha(themeColor.getAlpha());
            default:
                return themeColor;
        }
    }

    public static void renderSword(PoseStack matrices, BufferBuilder buffer, float scale, ColorRGBA themeColor) {
        matrices.pushPose();
        matrices.scale(scale, scale, scale);
        // Rotate -45 degrees around Z axis to make the diagonal sword model point straight downwards.
        matrices.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-45.0f));
        Matrix4f transformationMatrix = matrices.last().pose();

        for (int i = 0; i < SWORD_FACES.size(); i++) {
            int[] face = SWORD_FACES.get(i);
            char type = SWORD_VOXEL_TYPES.get(i);
            ColorRGBA voxelColor = getVoxelColor(type, themeColor);
            
            int faceDirection = (i / 2) % 6;
            float brightness = 1.0f;
            switch (faceDirection) {
                case 0: brightness = 1.0f; break; // Front
                case 1: brightness = 0.5f; break; // Back
                case 2: brightness = 0.6f; break; // Left
                case 3: brightness = 0.8f; break; // Right
                case 4: brightness = 0.9f; break; // Top
                case 5: brightness = 0.4f; break; // Bottom
            }
            
            int rgb = applyBrightness(voxelColor.getRGB(), brightness);
            
            Vector3f v1 = SWORD_VERTICES.get(face[0]);
            Vector3f v2 = SWORD_VERTICES.get(face[1]);
            Vector3f v3 = SWORD_VERTICES.get(face[2]);
            
            buffer.addVertex(transformationMatrix, v1.x, v1.y, v1.z).setColor(rgb);
            buffer.addVertex(transformationMatrix, v2.x, v2.y, v2.z).setColor(rgb);
            buffer.addVertex(transformationMatrix, v3.x, v3.y, v3.z).setColor(rgb);
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

