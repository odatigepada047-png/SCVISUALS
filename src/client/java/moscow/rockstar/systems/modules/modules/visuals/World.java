
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.*;
import moscow.rockstar.utility.time.Timer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@ModuleInfo(name="World", category=ModuleCategory.VISUALS, desc="Визуальные дополнения мира")
public class World extends BaseModule {
    private final List<Particle> particles = new ArrayList<>();
    private final BooleanSetting syncWithTheme = new BooleanSetting(this, "modules.settings.sync_with_theme").enable();
    private final ColorSetting color = new ColorSetting(this, "color", () -> this.syncWithTheme.isEnabled()).color(Colors.getAccentColor());

    public ColorRGBA getColor() {
        return this.syncWithTheme.isEnabled() ? Colors.getAccentColor() : this.color.getColor();
    }

    private final EventListener<Render3DEvent> on3DRender = event -> {
        if (RenderSystem.outputColorTextureOverride != null || this.particles.isEmpty()) {
            return;
        }
        GLStateSnapshot glState = GLStateSnapshot.capture();

        try {
            PoseStack ms = event.pose();
            Camera camera = World.mc.gameRenderer.getMainCamera();
            Vec3 cameraPos = camera.position();
            float partialTicks = event.getGameTimeDeltaPartialTick();

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDepthMask(false);

            // 1. РЕНДЕР ТЕКСТУР БЛУМА
            Identifier id = Rockstar.id("textures/bloom.png");
            TextureBinder.bind(id);
            GlProgram.usePositionTexColor();

            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (Particle particle : this.particles) {
                Vec3 pos = Utils.getInterpolatedPos(particle.prev, particle.pos, partialTicks);
                float bigSize = 4.0f * particle.size;

                ms.pushPose();
                RenderUtility.prepareMatrices(ms, pos);
                ms.mulPose(camera.rotation());
                DrawUtility.drawImage(ms, builder, -bigSize / 2.0f, -bigSize / 2.0f, 0.0, bigSize, bigSize, this.getColor().withAlpha(255.0f * particle.alpha.getRGB() * 0.3f));
                ms.popPose();
            }
            MeshData builtTexBuffer = builder.build();
            if (builtTexBuffer != null) {
                MeshDrawHelper.drawBuilt(builtTexBuffer);
            }

            TextureBinder.unbind();

            // 2. РЕНДЕР КУБОВ (Переведено на ПОЛУПРОЗРАЧНЫЕ КВАДРАТЫ вместо линий, чтобы убрать баг)
            GlProgram.usePositionColor();
            BufferBuilder cubeBuffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (Particle particle : this.particles) {
                particle.alpha.update(!particle.timer.finished(particle.liveTicks));

                Vec3 pos = Utils.getInterpolatedPos(particle.prev, particle.pos, partialTicks);
                Vec3 rot = Utils.getInterpolatedPos(particle.prevRot, particle.rotate, partialTicks);

                ms.pushPose();
                ms.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);
                ms.mulPose(new Quaternionf().rotationXYZ((float)rot.x, (float)rot.y, (float)rot.z));
                ms.scale(particle.size, particle.size, particle.size);

                ColorRGBA cubeColor = this.getColor().withAlpha(255.0f * particle.alpha.getRGB() * 0.25f);

                this.drawSolidCube(ms, cubeBuffer, 1.0f, cubeColor.getRGB());

                ms.popPose();
            }

            MeshData builtCubeBuffer = cubeBuffer.build();
            if (builtCubeBuffer != null) {
                MeshDrawHelper.drawBuilt(builtCubeBuffer);
            }
        } catch (Exception e) {
            // Игнорируем ошибки интерполяции при резкой смене мира
        } finally {
            GlProgram.clearActive();
            glState.restore();
        }
    };

    private void drawSolidCube(PoseStack ms, BufferBuilder buffer, float size, int rgb) {
        float min = -0.5f * size;
        float max = 0.5f * size;

        Matrix4f matrix = ms.last().pose();

        // Передняя грань
        buffer.addVertex(matrix, min, min, max).setColor(rgb);
        buffer.addVertex(matrix, max, min, max).setColor(rgb);
        buffer.addVertex(matrix, max, max, max).setColor(rgb);
        buffer.addVertex(matrix, min, max, max).setColor(rgb);

        // Задняя грань
        buffer.addVertex(matrix, min, min, min).setColor(rgb);
        buffer.addVertex(matrix, min, max, min).setColor(rgb);
        buffer.addVertex(matrix, max, max, min).setColor(rgb);
        buffer.addVertex(matrix, max, min, min).setColor(rgb);

        // Верхняя грань
        buffer.addVertex(matrix, min, max, min).setColor(rgb);
        buffer.addVertex(matrix, min, max, max).setColor(rgb);
        buffer.addVertex(matrix, max, max, max).setColor(rgb);
        buffer.addVertex(matrix, max, max, min).setColor(rgb);

        // Нижняя грань
        buffer.addVertex(matrix, min, min, min).setColor(rgb);
        buffer.addVertex(matrix, max, min, min).setColor(rgb);
        buffer.addVertex(matrix, max, min, max).setColor(rgb);
        buffer.addVertex(matrix, min, min, max).setColor(rgb);

        // Правая грань
        buffer.addVertex(matrix, max, min, min).setColor(rgb);
        buffer.addVertex(matrix, max, max, min).setColor(rgb);
        buffer.addVertex(matrix, max, max, max).setColor(rgb);
        buffer.addVertex(matrix, max, min, max).setColor(rgb);

        // Левая грань
        buffer.addVertex(matrix, min, min, min).setColor(rgb);
        buffer.addVertex(matrix, min, min, max).setColor(rgb);
        buffer.addVertex(matrix, min, max, max).setColor(rgb);
        buffer.addVertex(matrix, min, max, min).setColor(rgb);
    }

    @Override
    public void tick() {
        this.particles.removeIf(particle -> particle.alpha.getRGB() == 0.0f && particle.timer.finished(particle.liveTicks));

        for (Particle particle : this.particles) {
            particle.tick();
        }

        if (this.particles.size() < 100 && World.mc.player != null) {
            this.particles.add(new Particle(
                    World.mc.player.position().add(MathUtility.random(-20.0, 20.0), MathUtility.random(0.0, 5.0), MathUtility.random(-20.0, 20.0)),
                    Vec3.ZERO,
                    new Vec3(MathUtility.random(-1.0, 1.0), MathUtility.random(0.0, 2.0), MathUtility.random(-1.0, 1.0)),
                    new Vec3(MathUtility.random(-1.0, 1.0), MathUtility.random(-1.0, 1.0), MathUtility.random(-1.0, 1.0)),
                    (long) MathUtility.random(1500.0, 4500.0),
                    MathUtility.random(0.1f, 0.3f)
            ));
        }
    }

    static class Particle {
        Vec3 prev;
        Vec3 prevRot;
        Vec3 pos;
        Vec3 rotate;
        Vec3 motion;
        Vec3 rotateMotion;
        final long liveTicks;
        float size;
        final Timer timer = new Timer();
        final Animation alpha = new Animation(300L, Easing.FIGMA_EASE_IN_OUT);

        public Particle(Vec3 pos, Vec3 rotate, Vec3 motion, Vec3 rotateMotion, long liveTicks, float size) {
            this.pos = pos;
            this.rotate = rotate;
            this.motion = motion.scale(0.04f);
            this.rotateMotion = rotateMotion.scale(0.04f);
            this.liveTicks = liveTicks;
            this.size = size;
            this.prevRot = rotate;
            this.prev = pos;
            this.alpha.setDuration(1000L);
        }

        void tick() {
            this.prev = this.pos;
            this.prevRot = this.rotate;
            this.pos = this.pos.add(this.motion);
            this.rotate = this.rotate.add(this.rotateMotion);
            this.motion = this.motion.scale(0.98);
            this.rotateMotion = this.rotateMotion.scale(0.98);
        }
    }
}

