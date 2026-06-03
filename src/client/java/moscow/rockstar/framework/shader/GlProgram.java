package moscow.rockstar.framework.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import moscow.rockstar.utility.render.ShaderColorHelper;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a {@link RenderPipeline} loaded from a Rockstar {@code data.json} descriptor and
 * exposes the legacy {@link #findUniform(String)} API. Custom shader uniforms are packed
 * into a single std140 UBO ({@link #UNIFORM_BLOCK}) because Minecraft 1.26.1 no longer
 * supports loose GLSL uniforms outside of buffer blocks.
 */
public class GlProgram {
    public static final String UNIFORM_BLOCK = "RockstarUniforms";
    private static final Logger LOGGER = LoggerFactory.getLogger("rockstar/GlProgram");
    private static final List<GlProgram> REGISTERED_PROGRAMS = new ArrayList<>();
    private static @Nullable GlProgram ACTIVE;

    private final Identifier dataId;
    private final VertexFormat vertexFormat;
    private final VertexFormat.Mode drawMode;
    private @Nullable RenderPipeline pipeline;
    private List<ShaderDataDescriptor.UniformEntry> uniformLayout = List.of();
    private int uboSize;
    private final Map<String, float[]> uniformValues = new HashMap<>();

    public GlProgram(Identifier id, VertexFormat vertexFormat) {
        this(id, VertexFormat.Mode.QUADS, vertexFormat);
    }

    public GlProgram(Identifier id, VertexFormat.Mode drawMode, VertexFormat vertexFormat) {
        this.dataId = id.withPrefix("core/");
        this.vertexFormat = vertexFormat;
        this.drawMode = drawMode;
        REGISTERED_PROGRAMS.add(this);
    }

    public @Nullable RenderPipeline getPipeline() {
        return this.pipeline;
    }

    public RenderPipeline use() {
        ACTIVE = this;
        return this.pipeline;
    }

    public static @Nullable GlProgram getActive() {
        return ACTIVE;
    }

    public static void clearActive() {
        ACTIVE = null;
    }

    protected void setup() {
    }

    public UniformSetter findUniform(String name) {
        return new UniformSetter(name, this.uniformValues);
    }

    @ApiStatus.Internal
    public static void loadAndSetupPrograms() {
        for (GlProgram program : REGISTERED_PROGRAMS) {
            try {
                program.loadAndRegister();
            } catch (Throwable t) {
                LOGGER.error("Failed to load shader program {}", program.dataId, t);
            }
        }
    }

    private void loadAndRegister() {
        ShaderDataDescriptor desc = ShaderDataLoader.load(this.dataId);
        this.uniformLayout = desc.uniforms();
        this.uboSize = computeUboSize(desc.uniforms());
        for (ShaderDataDescriptor.UniformEntry entry : desc.uniforms()) {
            this.uniformValues.putIfAbsent(entry.name(), entry.defaults().clone());
        }

        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
                .withLocation(this.dataId)
                .withVertexShader(desc.vertex())
                .withFragmentShader(desc.fragment())
                .withVertexFormat(this.vertexFormat, this.drawMode)
                .withDepthStencilState(Optional.empty());

        for (String sampler : desc.samplers()) {
            builder.withSampler(sampler);
        }

        if (!desc.uniforms().isEmpty()) {
            builder.withUniform(UNIFORM_BLOCK, UniformType.UNIFORM_BUFFER);
        }

        this.pipeline = RenderPipelines.register(builder.build());
        this.setup();
    }

    public static void applyActiveUniforms(RenderPass pass) {
        GlProgram program = ACTIVE;
        if (program == null || program.pipeline == null || program.uniformLayout.isEmpty()) {
            return;
        }

        applyUniforms(pass, program, program.uniformValues);
    }

    public boolean hasUniformBlock() {
        return !this.uniformLayout.isEmpty();
    }

    public Map<String, float[]> snapshotUniformValues() {
        Map<String, float[]> snapshot = new HashMap<>();
        for (ShaderDataDescriptor.UniformEntry entry : this.uniformLayout) {
            float[] values = this.uniformValues.get(entry.name());
            if (values != null) {
                snapshot.put(entry.name(), values.clone());
            }
        }
        return snapshot;
    }

    public static void applyUniforms(RenderPass pass, GlProgram program, Map<String, float[]> values) {
        if (program.pipeline == null || program.uniformLayout.isEmpty()) {
            return;
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect(program.uboSize).order(ByteOrder.nativeOrder());
        Std140Builder b = Std140Builder.intoBuffer(buffer);
        for (ShaderDataDescriptor.UniformEntry entry : program.uniformLayout) {
            float[] uniform = values.get(entry.name());
            if (uniform == null || uniform.length < entry.count()) {
                uniform = entry.defaults();
            }
            writeUniform(b, entry, uniform);
        }
        buffer.position(0);

        GpuBuffer gpu = RenderSystem.getDevice().createBuffer(
                () -> "rockstar_uniforms",
                GpuBuffer.USAGE_UNIFORM,
                buffer
        );
        pass.setUniform(UNIFORM_BLOCK, gpu.slice(0, program.uboSize));
        moscow.rockstar.utility.render.MeshDrawHelper.registerBuffer(gpu);
    }

    private static int computeUboSize(List<ShaderDataDescriptor.UniformEntry> entries) {
        if (entries.isEmpty()) {
            return 0;
        }
        Std140SizeCalculator calc = new Std140SizeCalculator();
        for (ShaderDataDescriptor.UniformEntry entry : entries) {
            addUniformSize(calc, entry);
        }
        return calc.get();
    }

    private static void addUniformSize(Std140SizeCalculator calc, ShaderDataDescriptor.UniformEntry entry) {
        boolean isInt = "int".equals(entry.type());
        switch (entry.count()) {
            case 1 -> {
                if (isInt) {
                    calc.putInt();
                } else {
                    calc.putFloat();
                }
            }
            case 2 -> {
                if (isInt) {
                    calc.putIVec2();
                } else {
                    calc.putVec2();
                }
            }
            case 3 -> {
                if (isInt) {
                    calc.putIVec3();
                } else {
                    calc.putVec3();
                }
            }
            case 4 -> {
                if (isInt) {
                    calc.putIVec4();
                } else {
                    calc.putVec4();
                }
            }
            default -> throw new IllegalArgumentException("Unsupported uniform count: " + entry.count());
        }
    }

    private static void writeUniform(Std140Builder builder, ShaderDataDescriptor.UniformEntry entry, float[] values) {
        boolean isInt = "int".equals(entry.type());
        switch (entry.count()) {
            case 1 -> {
                if (isInt) {
                    builder.putInt((int) values[0]);
                } else {
                    builder.putFloat(values[0]);
                }
            }
            case 2 -> {
                if (isInt) {
                    builder.putIVec2((int) values[0], (int) values[1]);
                } else {
                    builder.putVec2(values[0], values[1]);
                }
            }
            case 3 -> {
                if (isInt) {
                    builder.putIVec3((int) values[0], (int) values[1], (int) values[2]);
                } else {
                    builder.putVec3(values[0], values[1], values[2]);
                }
            }
            case 4 -> {
                if (isInt) {
                    builder.putIVec4((int) values[0], (int) values[1], (int) values[2], (int) values[3]);
                } else {
                    builder.putVec4(values[0], values[1], values[2], values[3]);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported uniform count: " + entry.count());
        }
    }

    public static class UniformSetter {
        private final String name;
        private final Map<String, float[]> values;

        UniformSetter(String name, Map<String, float[]> values) {
            this.name = name;
            this.values = values;
        }

        public void set(float value) {
            this.values.put(this.name, new float[]{value});
        }

        public void set(float a, float b) {
            this.values.put(this.name, new float[]{a, b});
        }

        public void set(float a, float b, float c) {
            this.values.put(this.name, new float[]{a, b, c});
        }

        public void set(float a, float b, float c, float d) {
            this.values.put(this.name, new float[]{a, b, c, d});
        }
    }

    public static RenderPipeline usePositionColor() {
        clearActive();
        return RenderPipelines.GUI;
    }

    public static RenderPipeline usePositionTexColor() {
        clearActive();
        return RenderPipelines.GUI_TEXTURED;
    }
}
