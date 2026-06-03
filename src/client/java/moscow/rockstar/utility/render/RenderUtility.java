/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.renderer.BufferBuilder
 *  net.minecraft.client.renderer.BufferRenderer
 *  net.minecraft.client.renderer.MeshData
 *  net.minecraft.client.renderer.Camera
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.util.math.Axis
 *  net.minecraft.util.math.Vec3
 *  org.joml.Matrix3x2fStack
 */
package moscow.rockstar.utility.render;

// import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IWindow;
import com.mojang.blaze3d.vertex.BufferBuilder;
// import net.minecraft.client.renderer.BufferRenderer;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

public final class RenderUtility
implements IMinecraft,
IWindow {
    public static void rotate(PoseStack ms, float x, float y, float value) {
        ms.pushPose();
        ms.translate(x, y, 0.0f);
        ms.mulPose(Axis.ZP.rotationDegrees(value));
        ms.translate(-x, -y, 0.0f);
    }

    public static void rotate(Matrix3x2fStack ms, float x, float y, float value) {
        ms.pushMatrix();
        ms.translate(x, y);
        ms.rotate((float)Math.toRadians(value));
        ms.translate(-x, -y);
    }

    public static void scale(PoseStack ms, float x, float y, float scale) {
        RenderUtility.scale(ms, x, y, scale, scale);
    }

    public static void scale(Matrix3x2fStack ms, float x, float y, float scale) {
        RenderUtility.scale(ms, x, y, scale, scale);
    }

    public static void scale(PoseStack ms, float x, float y, float scaleX, float scaleY) {
        ms.pushPose();
        ms.translate(x, y, 0.0f);
        ms.scale(scaleX, scaleY, 1.0f);
        ms.translate(-x, -y, 0.0f);
    }

    public static void scale(Matrix3x2fStack ms, float x, float y, float scaleX, float scaleY) {
        ms.pushMatrix();
        ms.translate(x, y);
        ms.scale(scaleX, scaleY);
        ms.translate(-x, -y);
    }

    public static void end(PoseStack ms) {
        ms.popPose();
    }

    public static void end(Matrix3x2fStack ms) {
        ms.popMatrix();
    }

    public static void prepareMatrices(PoseStack matrices) {
        Camera camera = RenderUtility.mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.position();
        Vec3 renderPos = Vec3.ZERO.subtract(cameraPos);
        matrices.translate(renderPos.x, renderPos.y, renderPos.z);
    }

    public static void prepareMatrices(PoseStack matrices, Vec3 pos) {
        Camera camera = RenderUtility.mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.position();
        Vec3 renderPos = pos.subtract(cameraPos);
        matrices.translate(renderPos.x, renderPos.y, renderPos.z);
    }

    public static void setupRender3D(boolean bloomColor) {
//         RenderSystem.enableBlend();
        // RenderSystem.disableCull();
        // RenderSystem.disableDepthTest();
//         RenderSystem.defaultBlendFunc();
        // TODO: 26.1 - depth/blend state moved to render pipeline
        if (bloomColor) {
        } else {
        }
    }

    public static void endRender3D() {
        // RenderSystem.depthMask(true);
//         RenderSystem.disableBlend();
        // RenderSystem.enableDepthTest();
        // RenderSystem.enableCull();
    }

    public static void buildBuffer(BufferBuilder builder) {
        MeshData builtBuffer = builder.build();
        if (builtBuffer != null) {
//             BufferRenderer.drawWithGlobalProgram((MeshData)builtBuffer);
        }
    }

    @Generated
    private RenderUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

