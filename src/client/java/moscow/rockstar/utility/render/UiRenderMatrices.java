package moscow.rockstar.utility.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.mixin.accessors.GameRendererAccessor;
import moscow.rockstar.mixin.accessors.GuiRendererAccessor;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public final class UiRenderMatrices implements IMinecraft {
    private static final float GUI_MODEL_VIEW_Z = -11000.0f;

    private static boolean active;
    private static boolean changedProjection;

    private UiRenderMatrices() {
    }

    public static void begin() {
        if (active) {
            return;
        }
        active = true;
        changedProjection = false;

        GameRenderer gameRenderer = mc.gameRenderer;
        GuiRenderer guiRenderer = ((GameRendererAccessor) gameRenderer).rockstar$getGuiRenderer();
        Projection guiProjection = ((GuiRendererAccessor) guiRenderer).rockstar$getGuiProjection();
        ProjectionMatrixBuffer projectionBuffer = ((GuiRendererAccessor) guiRenderer).rockstar$getGuiProjectionMatrixBuffer();

        Matrix4f projectionMatrix = guiProjection.getMatrix(new Matrix4f());
        GpuBufferSlice projectionSlice = projectionBuffer.getBuffer(projectionMatrix);

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionSlice, ProjectionType.ORTHOGRAPHIC);
        changedProjection = true;

        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.identity();
        stack.translate(0.0f, 0.0f, GUI_MODEL_VIEW_Z);
    }

    public static void end() {
        if (!active) {
            return;
        }
        active = false;
        RenderSystem.getModelViewStack().popMatrix();
        if (changedProjection) {
            RenderSystem.restoreProjectionMatrix();
            changedProjection = false;
        }
    }

    private static final Matrix4f IDENTITY = new Matrix4f();

    public static Matrix4f modelViewMatrix() {
        return active ? RenderSystem.getModelViewMatrix() : IDENTITY;
    }

    public static boolean isActive() {
        return active;
    }
}
