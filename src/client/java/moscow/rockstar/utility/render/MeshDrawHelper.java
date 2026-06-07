package moscow.rockstar.utility.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public final class MeshDrawHelper implements IMinecraft {
    public static final RenderPipeline GUI_LINES = net.minecraft.client.renderer.RenderPipelines.register(
            RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.GUI_SNIPPET)
                    .withLocation(moscow.rockstar.Rockstar.id("pipeline/gui_lines"))
                    .withVertexFormat(com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR, com.mojang.blaze3d.vertex.VertexFormat.Mode.DEBUG_LINES)
                    .build()
    );

    public static final RenderPipeline GUI_LINE_STRIP = net.minecraft.client.renderer.RenderPipelines.register(
            RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.GUI_SNIPPET)
                    .withLocation(moscow.rockstar.Rockstar.id("pipeline/gui_line_strip"))
                    .withVertexFormat(com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR, com.mojang.blaze3d.vertex.VertexFormat.Mode.DEBUG_LINE_STRIP)
                    .build()
    );

    private static final int BUFFER_SIZE = 786432;
    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(BUFFER_SIZE);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    public static boolean disableDepthOverride = false;
    private static boolean forceMainColorTarget = false;

    private static final java.util.List<java.util.List<GpuBuffer>> DEFERRED_BUFFERS = new java.util.ArrayList<>();
    private static java.util.List<GpuBuffer> currentFrameBuffers = new java.util.ArrayList<>();

    public static synchronized void registerBuffer(GpuBuffer buffer) {
        if (buffer != null) {
            currentFrameBuffers.add(buffer);
        }
    }

    public static synchronized void cleanupBuffers() {
        DEFERRED_BUFFERS.add(currentFrameBuffers);
        currentFrameBuffers = new java.util.ArrayList<>();
        while (DEFERRED_BUFFERS.size() > 3) {
            java.util.List<GpuBuffer> oldest = DEFERRED_BUFFERS.remove(0);
            for (GpuBuffer buffer : oldest) {
                try {
                    buffer.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    private MeshDrawHelper() {
    }

    public static void draw(MeshData mesh, RenderPipeline pipeline) {
        draw(mesh, pipeline, null, null);
    }

    public static void draw(MeshData mesh, RenderPipeline pipeline, GpuTextureView texture, GpuSampler sampler) {
        if (mesh == null || pipeline == null) {
            if (mesh != null) {
                mesh.close();
            }
            GlProgram.clearActive();
            return;
        }

        GpuBuffer vertices = null;
        try {
            MeshData.DrawState drawState = mesh.drawState();
            if (drawState.vertexCount() <= 0 || drawState.indexCount() <= 0) {
                return;
            }
            VertexFormat format = drawState.format();
            vertices = upload(drawState, format, mesh);
            drawUploaded(mc, pipeline, mesh, drawState, vertices, texture, sampler);
        } finally {
            mesh.close();
            GlProgram.clearActive();
            if (vertices != null) {
                registerBuffer(vertices);
            }
        }
    }

    public static void drawBuilt(MeshData mesh) {
        GlProgram program = GlProgram.getActive();
        if (program == null) {
            if (mesh != null && mesh.drawState().format() == com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR) {
                draw(mesh, net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED);
            } else if (mesh != null && mesh.drawState().format() == com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH) {
                draw(mesh, net.minecraft.client.renderer.RenderPipelines.LINES_TRANSLUCENT);
            } else if (mesh != null && mesh.drawState().format() == com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR) {
                if (mesh.drawState().mode() == com.mojang.blaze3d.vertex.VertexFormat.Mode.DEBUG_LINE_STRIP) {
                    draw(mesh, GUI_LINE_STRIP);
                } else if (mesh.drawState().mode() == com.mojang.blaze3d.vertex.VertexFormat.Mode.DEBUG_LINES) {
                    draw(mesh, GUI_LINES);
                } else {
                    draw(mesh, net.minecraft.client.renderer.RenderPipelines.GUI);
                }
            } else {
                draw(mesh, net.minecraft.client.renderer.RenderPipelines.GUI);
            }
            return;
        }
        draw(mesh, program.getPipeline());
    }

    public static void drawBuiltMainTarget(MeshData mesh) {
        boolean prev = forceMainColorTarget;
        forceMainColorTarget = true;
        try {
            drawBuilt(mesh);
        } finally {
            forceMainColorTarget = prev;
        }
    }

    public static void drawBuiltWorld(MeshData mesh, RenderPipeline pipeline, Matrix4f modelView) {
        if (mesh == null || pipeline == null) {
            if (mesh != null) {
                mesh.close();
            }
            return;
        }
        GpuBuffer vertices = null;
        try {
            MeshData.DrawState drawState = mesh.drawState();
            if (drawState.vertexCount() <= 0 || drawState.indexCount() <= 0) {
                return;
            }
            VertexFormat format = drawState.format();
            vertices = upload(drawState, format, mesh);
            drawUploadedWorld(mc, pipeline, mesh, drawState, vertices, modelView);
        } finally {
            mesh.close();
            GlProgram.clearActive();
            if (vertices != null) {
                registerBuffer(vertices);
            }
        }
    }

    private static GpuBuffer upload(MeshData.DrawState drawState, VertexFormat format, MeshData mesh) {
        int vertexBytes = drawState.vertexCount() * format.getVertexSize();
        ByteBuffer src = mesh.vertexBuffer();
        ByteBuffer dup = MemoryUtil.memAlloc(vertexBytes);
        ByteBuffer srcDup = src.duplicate();
        srcDup.limit(srcDup.position() + vertexBytes);
        dup.put(srcDup);
        dup.flip();

        GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
                () -> "rockstar_ui_vertices",
                GpuBuffer.USAGE_VERTEX,
                dup
        );
        MemoryUtil.memFree(dup);
        return buffer;
    }


    private static void drawUploaded(
            Minecraft client,
            RenderPipeline pipeline,
            MeshData mesh,
            MeshData.DrawState drawState,
            GpuBuffer vertices,
            GpuTextureView texture,
            GpuSampler sampler
    ) {
        IndexUpload indices = resolveIndices(pipeline, mesh, drawState);

        float[] mod = ShaderColorHelper.getShaderColor();
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                UiRenderMatrices.modelViewMatrix(),
                new Vector4f(mod[0], mod[1], mod[2], mod[3]),
                new Vector3f(),
                TEXTURE_MATRIX
        );

        GpuTextureView colorTarget = CustomRenderTarget.getActiveColorView();
        if (colorTarget == null && !forceMainColorTarget) {
            colorTarget = RenderSystem.outputColorTextureOverride;
        }
        if (colorTarget == null) {
            colorTarget = client.getMainRenderTarget().getColorTextureView();
        }

        var mainTarget = client.getMainRenderTarget();
        GpuTextureView depthTarget = (colorTarget == mainTarget.getColorTextureView() && mainTarget.useDepth && !disableDepthOverride) ? mainTarget.getDepthTextureView() : null;

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "rockstar_ui_pass",
                colorTarget,
                OptionalInt.empty(),
                depthTarget,
                OptionalDouble.empty()
        )) {
            ScissorUtility.applyTo(pass);
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            GlProgram.applyActiveUniforms(pass);
            TextureBinder.GpuBinding binding = TextureBinder.lastBinding;
            if (texture != null && sampler != null) {
                pass.bindTexture("Sampler0", texture, sampler);
            } else if (binding != null && binding.view() != null && binding.sampler() != null) {
                pass.bindTexture("Sampler0", binding.view(), binding.sampler());
            }
            pass.setVertexBuffer(0, vertices);
            pass.setIndexBuffer(indices.buffer, indices.type);
            pass.drawIndexed(0, 0, drawState.indexCount(), 1);
        } finally {
            indices.close();
        }
    }

    private static void drawUploadedWorld(
            Minecraft client,
            RenderPipeline pipeline,
            MeshData mesh,
            MeshData.DrawState drawState,
            GpuBuffer vertices,
            Matrix4f modelView
    ) {
        IndexUpload indices = resolveIndices(pipeline, mesh, drawState);

        float[] mod = ShaderColorHelper.getShaderColor();
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                modelView,
                new Vector4f(mod[0], mod[1], mod[2], mod[3]),
                new Vector3f(),
                TEXTURE_MATRIX
        );

        GpuTextureView colorTarget = forceMainColorTarget ? null : RenderSystem.outputColorTextureOverride;
        if (colorTarget == null) {
            colorTarget = client.getMainRenderTarget().getColorTextureView();
        }

        var mainTarget = client.getMainRenderTarget();
        GpuTextureView depthTarget = (colorTarget == mainTarget.getColorTextureView() && mainTarget.useDepth && !disableDepthOverride) ? mainTarget.getDepthTextureView() : null;

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "rockstar_world_pass",
                colorTarget,
                OptionalInt.empty(),
                depthTarget,
                OptionalDouble.empty()
        )) {
            ScissorUtility.applyTo(pass);
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            GlProgram.applyActiveUniforms(pass);
            pass.setVertexBuffer(0, vertices);
            pass.setIndexBuffer(indices.buffer, indices.type);
            pass.drawIndexed(0, 0, drawState.indexCount(), 1);
        } finally {
            indices.close();
        }
    }

    private static IndexUpload resolveIndices(RenderPipeline pipeline, MeshData mesh, MeshData.DrawState drawState) {
        if (drawState.mode() == VertexFormat.Mode.QUADS) {
            mesh.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
        }
        ByteBuffer indices = mesh.indexBuffer();
        if (indices != null) {
            return uploadIndexBytes(indices, drawState.indexType());
        }

        RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer =
                RenderSystem.getSequentialBuffer(drawState.mode());
        return new IndexUpload(shapeIndexBuffer.getBuffer(drawState.indexCount()), shapeIndexBuffer.type(), false);
    }

    private static IndexUpload uploadIndexBytes(ByteBuffer indices, VertexFormat.IndexType indexType) {
        ByteBuffer dup = MemoryUtil.memAlloc(indices.remaining());
        dup.put(indices.duplicate());
        dup.flip();
        GpuBuffer buffer = RenderSystem.getDevice()
                .createBuffer(() -> "rockstar_quad_indices", GpuBuffer.USAGE_INDEX, dup);
        MemoryUtil.memFree(dup);
        return new IndexUpload(buffer, indexType, true);
    }
    private record IndexUpload(GpuBuffer buffer, VertexFormat.IndexType type, boolean owned) {
        void close() {
            if (this.owned) {
                registerBuffer(this.buffer);
            }
        }
    }
}
