/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.renderer.BufferBuilder
 *  net.minecraft.client.renderer.BufferRenderer
 *  net.minecraft.client.renderer.MeshData
 *  net.minecraft.client.renderer.Camera
 *  net.minecraft.client.renderer.VertexFormat$DrawMode
 *  net.minecraft.client.renderer.DefaultVertexFormat
/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.renderer.BufferBuilder
 *  net.minecraft.client.renderer.BufferRenderer
 *  net.minecraft.client.renderer.MeshData
 *  net.minecraft.client.renderer.Camera
 *  net.minecraft.client.renderer.VertexFormat$DrawMode
 *  net.minecraft.client.renderer.DefaultVertexFormat
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.AABB
 *  net.minecraft.util.math.Vec3
 *  org.joml.Quaternionf
 */
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@ModuleInfo(name="World", category=ModuleCategory.VISUALS, desc="\u0412\u0438\u0437\u0443\u0430\u043b\u044c\u043d\u044b\u0435 \u0434\u043e\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u044f \u043c\u0438\u0440\u0430")
public class World
extends BaseModule {
    private final List<Particle> particles = new ArrayList<Particle>();
    private final BooleanSetting syncWithTheme = new BooleanSetting(this, "modules.settings.sync_with_theme").enable();
    private final ColorSetting color = new ColorSetting(this, "color", () -> this.syncWithTheme.isEnabled()).color(Colors.getAccentColor());
    
    public ColorRGBA getColor() {
        return this.syncWithTheme.isEnabled() ? Colors.getAccentColor() : this.color.getColor();
    }
    private final EventListener<Render3DEvent> on3DRender = event -> {
        if (RenderSystem.outputColorTextureOverride != null) {
            return;
        }
        GLStateSnapshot glState = GLStateSnapshot.capture();
        
        try {
            PoseStack ms = event.pose();
            Camera camera = World.mc.gameRenderer.getMainCamera();
            Vec3 cameraPos = camera.position();
            
            GL11.glEnable((int)3042);
            GL11.glBlendFunc((int)770, (int)1);
            GL11.glEnable((int)2929);
            GL11.glDisable((int)2884);
            GL11.glDepthMask((boolean)false);
            
            Identifier id = Rockstar.id("textures/bloom.png");
            TextureBinder.bind(id);
            GlProgram.usePositionTexColor();
            
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (Particle particle : this.particles) {
                Vec3 pos = Utils.getInterpolatedPos(particle.prev, particle.pos, event.getGameTimeDeltaPartialTick());
                float bigSize = 4.0f * particle.size;
                ms.pushPose();
                RenderUtility.prepareMatrices(ms, pos);
                ms.mulPose(camera.rotation());
                DrawUtility.drawImage(ms, builder, (double)(-bigSize / 2.0f), (double)(-bigSize / 2.0f), 0.0, (double)bigSize, (double)bigSize, this.getColor().withAlpha(255.0f * particle.alpha.getRGB() * 0.4f));
                ms.popPose();
            }
            MeshData builtTexBuffer = builder.build();
            if (builtTexBuffer != null) {
                MeshDrawHelper.drawBuilt(builtTexBuffer);
            }
            
            TextureBinder.unbind();
            GlProgram.usePositionColor();
            
            BufferBuilder linesBuffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
            for (Particle particle : this.particles) {
                particle.alpha.update(!particle.timer.finished(particle.liveTicks));
                Vec3 pos = Utils.getInterpolatedPos(particle.prev, particle.pos, event.getGameTimeDeltaPartialTick());
                Vec3 rot = Utils.getInterpolatedPos(particle.prevRot, particle.rotate, event.getGameTimeDeltaPartialTick());
                ms.pushPose();
                ms.translate(pos.add(-cameraPos.x, -cameraPos.y, -cameraPos.z));
                ms.mulPose(new Quaternionf().rotationXYZ((float)rot.x, (float)rot.y, (float)rot.z));
                ms.scale(particle.size, particle.size, particle.size);
                
                ColorRGBA diagColor = this.getColor().withAlpha(255.0f * particle.alpha.getRGB() * 0.4f);
                ColorRGBA outlineColor = this.getColor().withAlpha(205.0f * particle.alpha.getRGB());
                this.drawParticleBox(ms, linesBuffer, 1.0f, outlineColor, diagColor);
                
                ms.popPose();
            }
            MeshData builtLinesBuffer = linesBuffer.build();
            if (builtLinesBuffer != null) {
                MeshDrawHelper.drawBuilt(builtLinesBuffer);
            }
        } finally {
            GlProgram.clearActive();
            glState.restore();
        }
    };

    private void drawParticleBox(PoseStack ms, BufferBuilder buffer, float size, ColorRGBA boxColor, ColorRGBA diagColor) {
        float min = -0.5f * size;
        float max = 0.5f * size;
        
        Vec3[] boxVertices = {
            new Vec3(min, min, min), new Vec3(max, min, min),
            new Vec3(max, min, min), new Vec3(max, min, max),
            new Vec3(max, min, max), new Vec3(min, min, max),
            new Vec3(min, min, max), new Vec3(min, min, min),
            
            new Vec3(min, max, min), new Vec3(max, max, min),
            new Vec3(max, max, min), new Vec3(max, max, max),
            new Vec3(max, max, max), new Vec3(min, max, max),
            new Vec3(min, max, max), new Vec3(min, max, min),
            
            new Vec3(min, min, min), new Vec3(min, max, min),
            new Vec3(max, min, min), new Vec3(max, max, min),
            new Vec3(max, min, max), new Vec3(max, max, max),
            new Vec3(min, min, max), new Vec3(min, max, max)
        };
        
        Vec3[] diagVertices = {
            new Vec3(min, min, min), new Vec3(max, max, max),
            new Vec3(max, min, min), new Vec3(min, max, max),
            new Vec3(min, min, max), new Vec3(max, max, min),
            new Vec3(max, min, max), new Vec3(min, max, min)
        };
        
        PoseStack.Pose entry = ms.last();
        Matrix4f matrix = entry.pose();
        
        int boxRgb = boxColor.getRGB();
        for (int i = 0; i < boxVertices.length; i += 2) {
            Vec3 start = boxVertices[i];
            Vec3 end = boxVertices[i + 1];
            Vec3 normal = end.subtract(start).normalize();
            buffer.addVertex(matrix, (float)start.x, (float)start.y, (float)start.z)
                  .setColor(boxRgb)
                  .setNormal(entry, (float)normal.x, (float)normal.y, (float)normal.z)
                  .setLineWidth(1.0f);
            buffer.addVertex(matrix, (float)end.x, (float)end.y, (float)end.z)
                  .setColor(boxRgb)
                  .setNormal(entry, (float)normal.x, (float)normal.y, (float)normal.z)
                  .setLineWidth(1.0f);
        }
        
        int diagRgb = diagColor.getRGB();
        for (int i = 0; i < diagVertices.length; i += 2) {
            Vec3 start = diagVertices[i];
            Vec3 end = diagVertices[i + 1];
            Vec3 normal = end.subtract(start).normalize();
            buffer.addVertex(matrix, (float)start.x, (float)start.y, (float)start.z)
                  .setColor(diagRgb)
                  .setNormal(entry, (float)normal.x, (float)normal.y, (float)normal.z)
                  .setLineWidth(1.0f);
            buffer.addVertex(matrix, (float)end.x, (float)end.y, (float)end.z)
                  .setColor(diagRgb)
                  .setNormal(entry, (float)normal.x, (float)normal.y, (float)normal.z)
                  .setLineWidth(1.0f);
        }
    }

    @Override
    public void tick() {
        this.particles.removeIf(particle -> particle.alpha.getRGB() == 0.0f && particle.timer.finished(particle.liveTicks));
        for (Particle particle2 : this.particles) {
            particle2.tick();
        }
        if (this.particles.size() < 100) {
        this.particles.add(new Particle(World.mc.player.position().add((double)MathUtility.random(-20.0, 20.0), (double)MathUtility.random(0.0, 5.0), (double)MathUtility.random(-20.0, 20.0)), Vec3.ZERO, new Vec3((double)MathUtility.random(-1.0, 1.0), (double)MathUtility.random(0.0, 2.0), (double)MathUtility.random(-1.0, 1.0)), new Vec3((double)MathUtility.random(-1.0, 1.0), (double)MathUtility.random(-1.0, 1.0), (double)MathUtility.random(-1.0, 1.0)), (long)MathUtility.random(1500.0, 4500.0), MathUtility.random(0.1f, 0.3f)));
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



