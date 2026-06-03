/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.renderer.BufferBuilder
 *  net.minecraft.client.renderer.Camera
 *  net.minecraft.client.renderer.VertexConsumer
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.client.util.math.PoseStack$Entry
 *  net.minecraft.util.math.AABB
 *  net.minecraft.util.math.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package moscow.rockstar.utility.render;

// import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Draw3DUtility
implements IMinecraft {
    public static void renderGlowingBox(PoseStack matrices, BufferBuilder buffer, AABB box, ColorRGBA color) {
        float r = color.getRed();
        float g = color.getGreen();
        float b = color.getBlue();
        float baseAlpha = color.getAlpha();
//         RenderSystem.enableBlend();
        // RenderSystem.disableDepthTest();
        // RenderSystem.disableCull();
        // TODO: 26.1 - blendFunc/setShader API changed
        int glowLayers = 3;
        float glowStep = 0.1f;
        for (int i = glowLayers; i >= 1; --i) {
            float expand = (float)i * glowStep;
            float alpha = baseAlpha * (0.15f / (float)i);
            Draw3DUtility.renderFilledBox(matrices, buffer, box.inflate((double)expand), new ColorRGBA(r, g, b, alpha));
        }
        Draw3DUtility.renderFilledBox(matrices, buffer, box, new ColorRGBA(r, g, b, baseAlpha));
        // RenderSystem.enableCull();
        // RenderSystem.enableDepthTest();
//         RenderSystem.disableBlend();
    }

    public static void renderFilledBox(PoseStack matrices, BufferBuilder buffer, AABB box, ColorRGBA color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;
        Draw3DUtility.renderFilledBox(matrices, buffer, box, r, g, b, a);
    }

    public static void renderBoxInternalDiagonals(PoseStack matrices, BufferBuilder buf, AABB box, ColorRGBA color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;
        float minX = (float)box.minX;
        float minY = (float)box.minY;
        float minZ = (float)box.minZ;
        float maxX = (float)box.maxX;
        float maxY = (float)box.maxY;
        float maxZ = (float)box.maxZ;
        Matrix4f matrix = matrices.last().pose();
        buf.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
    }

    public static void renderFilledBox(PoseStack matrices, BufferBuilder buffer, AABB box, float r, float g, float b, float a) {
        float minX = (float)box.minX;
        float minY = (float)box.minY;
        float minZ = (float)box.minZ;
        float maxX = (float)box.maxX;
        float maxY = (float)box.maxY;
        float maxZ = (float)box.maxZ;
        Matrix4f matrix = matrices.last().pose();
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
    }

    public static void renderOutlinedBox(PoseStack matrices, BufferBuilder buffer, AABB box, ColorRGBA color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;
        float minX = (float)box.minX;
        float minY = (float)box.minY;
        float minZ = (float)box.minZ;
        float maxX = (float)box.maxX;
        float maxY = (float)box.maxY;
        float maxZ = (float)box.maxZ;
        Matrix4f matrix = matrices.last().pose();
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buffer.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
    }

    public static void drawLine(PoseStack matrices, VertexConsumer vertexConsumer, Vec3 startPos, Vec3 endPos, ColorRGBA color) {
        PoseStack.Pose entry = matrices.last();
        Vec3 normalized = endPos.subtract(startPos).normalize();
        Vector3f startVector = new Vector3f((float)startPos.x, (float)startPos.y, (float)startPos.z);
        vertexConsumer.addVertex(entry, startVector).setColor(color.getRGB()).setNormal(entry, (float)normalized.x, (float)normalized.y, (float)normalized.z);
        vertexConsumer.addVertex(entry, (float)endPos.x, (float)endPos.y, (float)endPos.z).setColor(color.getRGB()).setNormal(entry, (float)normalized.x, (float)normalized.y, (float)normalized.z);
    }

    public static void drawLine(PoseStack matrices, BufferBuilder builder, Vec3 startPos, Vec3 endPos, ColorRGBA color) {
        PoseStack.Pose matrixEntry = matrices.last();
        Matrix4f matrix4f = matrixEntry.pose();
        Vec3 normalized = endPos.subtract(startPos).normalize();
        builder.addVertex(matrix4f, (float)startPos.x, (float)startPos.y, (float)startPos.z).setColor(color.getRGB()).setNormal(matrixEntry, (float)normalized.x, (float)normalized.y, (float)normalized.z);
        builder.addVertex(matrix4f, (float)endPos.x, (float)endPos.y, (float)endPos.z).setColor(color.getRGB()).setNormal(matrixEntry, (float)normalized.x, (float)normalized.y, (float)normalized.z);
    }

    public static void renderLineFromPlayer(PoseStack matrices, BufferBuilder builder, Vec3 endPos, ColorRGBA color) {
        Camera camera = Draw3DUtility.mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.position();
        Vec3 cameraRotationVector = new Vec3(new Vector3f(camera.forwardVector()).mul(27.0f));
        Vec3 cameraRelatedEndPos = endPos.subtract(cameraPos);
        Vec3 startPos = new Vec3(cameraRotationVector.x, cameraRotationVector.y, cameraRotationVector.z);
        Draw3DUtility.drawLine(matrices, builder, startPos, cameraRelatedEndPos, color);
    }

    @Generated
    private Draw3DUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

