/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  moscow.rockstar.Rockstar
 *  moscow.rockstar.systems.event.EventListener
 *  moscow.rockstar.systems.event.impl.render.Render3DEvent
 *  moscow.rockstar.systems.modules.api.ModuleCategory
 *  moscow.rockstar.systems.modules.api.ModuleInfo
 *  moscow.rockstar.systems.modules.impl.BaseModule
 *  moscow.rockstar.systems.setting.SettingsContainer
 *  moscow.rockstar.systems.setting.settings.BooleanSetting
 *  moscow.rockstar.systems.setting.settings.ColorSetting
 *  moscow.rockstar.systems.setting.settings.ModeSetting
 *  moscow.rockstar.systems.setting.settings.ModeSetting$Value
 *  moscow.rockstar.ui.hud.impl.TargetHud
 *  moscow.rockstar.utility.animation.base.Animation
 *  moscow.rockstar.utility.animation.base.Easing
 *  moscow.rockstar.utility.colors.ColorRGBA
 *  moscow.rockstar.utility.colors.Colors
 *  moscow.rockstar.utility.game.EntityUtility
 *  moscow.rockstar.utility.math.MathUtility
 *  moscow.rockstar.utility.render.CrystalRenderer
 *  moscow.rockstar.utility.render.Draw3DUtility
 *  moscow.rockstar.utility.render.DrawUtility
 *  moscow.rockstar.utility.render.RenderUtility
 *  moscow.rockstar.utility.time.Timer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10142
 *  net.minecraft.RenderPipeline
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.class_239$class_240
 *  net.minecraft.Vec3
 *  net.minecraft.class_286
 *  net.minecraft.BufferBuilder
 *  net.minecraft.class_290
 *  net.minecraft.class_293$class_5596
 *  net.minecraft.Identifier
 *  net.minecraft.class_310
 *  net.minecraft.Mth
 *  net.minecraft.ClipContext
 *  net.minecraft.ClipContext$class_242
 *  net.minecraft.ClipContext$class_3960
 *  net.minecraft.Camera
 *  net.minecraft.PoseStack
 *  net.minecraft.class_7833
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.ui.hud.impl.TargetHud;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.CrystalRenderer;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.GLStateSnapshot;
import moscow.rockstar.utility.render.RenderUtility;
import com.mojang.blaze3d.vertex.Tesselator;
import moscow.rockstar.utility.render.MeshDrawHelper;
import moscow.rockstar.utility.render.TextureBinder;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.MeshData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@ModuleInfo(name="Target ESP", category=ModuleCategory.VISUALS, desc="\u041f\u043e\u043c\u0435\u0447\u0430\u0435\u0442 \u0430\u043a\u0442\u0438\u0432\u043d\u0443\u044e \u0446\u0435\u043b\u044c")
public class TargetESP
extends BaseModule {
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.target_esp.mode");
    private final ModeSetting.Value souls = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.souls");
    private final ModeSetting.Value crystals = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.crystals").select();
    private final ModeSetting.Value energy = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.energy");
    private final ModeSetting.Value crown = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.crown");
    private final ModeSetting.Value circles = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.circles");
    private final ModeSetting.Value marker = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.marker");
    private final ModeSetting.Value jelly = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.jelly");
    private final ModeSetting.Value skeleton = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.skeleton");
    private final ModeSetting.Value chains = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.chains");
    private final ModeSetting.Value chams = new ModeSetting.Value(this.mode, "modules.settings.target_esp.mode.chams");
    private final ModeSetting soulsStyle = new ModeSetting(this, "modules.settings.target_esp.souls_style", () -> !this.souls.isSelected());
    private final ModeSetting.Value soulsWraith = new ModeSetting.Value(this.soulsStyle, "modules.settings.target_esp.souls_style.wraith").select();
    private final ModeSetting.Value soulsTriangle = new ModeSetting.Value(this.soulsStyle, "modules.settings.target_esp.souls_style.triangle");
    private final ModeSetting.Value soulsXFormatted = new ModeSetting.Value(this.soulsStyle, "modules.settings.target_esp.souls_style.x_formatted");
    private final BooleanSetting syncWithTheme = new BooleanSetting(this, "modules.settings.sync_with_theme").enable();
    private final ColorSetting colorTarget = new ColorSetting(this, "color", () -> this.syncWithTheme.isEnabled()).color(Colors.getAccentColor());
    private final Animation animation = new Animation(300L, 0.0f, Easing.BOTH_CUBIC);
    private final Animation moving = new Animation(70L, 0.0f, Easing.LINEAR);
    private LivingEntity prevTarget;
    private final Timer targetTimer = new Timer();
    private float prevHealth = 0.0f;
    private float smoothHitOffset = 0.0f;
    private static final float SPEED_GHOSTS = 0.85f;
    private static final float SPEED_CHAINS = 0.85f;
    private static final int TRIANGLE_ORBIT_COUNT = 5;
    private static final int X_ORBIT_COUNT = 4;
    private static final double MIN_CAMERA_DISTANCE_SQR = 0.01;

    public static final com.mojang.blaze3d.pipeline.RenderPipeline LINES_SEE_THROUGH = net.minecraft.client.renderer.RenderPipelines.register(
        com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.LINES_SNIPPET)
            .withLocation(moscow.rockstar.Rockstar.id("pipeline/lines_see_through"))
            .withDepthStencilState(java.util.Optional.empty())
            .build()
    );

    public static final com.mojang.blaze3d.pipeline.RenderPipeline ENTITY_SEE_THROUGH = net.minecraft.client.renderer.RenderPipelines.register(
        com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.ENTITY_SNIPPET)
            .withLocation(moscow.rockstar.Rockstar.id("pipeline/entity_see_through"))
            .withDepthStencilState(java.util.Optional.empty())
            .build()
    );

    public static final com.mojang.blaze3d.pipeline.RenderPipeline ADDITIVE_TEXTURED = net.minecraft.client.renderer.RenderPipelines.register(
        com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(moscow.rockstar.Rockstar.id("pipeline/additive_textured"))
            .withColorTargetState(new com.mojang.blaze3d.pipeline.ColorTargetState(com.mojang.blaze3d.pipeline.BlendFunction.LIGHTNING))
            .build()
    );

    public static final com.mojang.blaze3d.pipeline.RenderPipeline ADDITIVE_COLOR = net.minecraft.client.renderer.RenderPipelines.register(
        com.mojang.blaze3d.pipeline.RenderPipeline.builder(net.minecraft.client.renderer.RenderPipelines.GUI_SNIPPET)
            .withLocation(moscow.rockstar.Rockstar.id("pipeline/additive_color"))
            .withColorTargetState(new com.mojang.blaze3d.pipeline.ColorTargetState(com.mojang.blaze3d.pipeline.BlendFunction.LIGHTNING))
            .build()
    );

    private final EventListener<Render3DEvent> onRender3D = event -> {
        if (!EntityUtility.isInGame()) {
            this.prevTarget = null;
            return;
        }
        if (RenderSystem.outputColorTextureOverride != null) {
            return;
        }
        moscow.rockstar.utility.render.ShaderColorHelper.reset();
        moscow.rockstar.utility.render.TextureBinder.unbind();
        moscow.rockstar.framework.shader.GlProgram.clearActive();
        LivingEntity target = this.getTarget();
        this.animation.setEasing(Easing.FIGMA_EASE_IN_OUT);
        this.animation.update(target != null);
        this.moving.update(this.moving.getRGB() + 10.0f + 50.0f);
        if (target != null) {
            this.prevTarget = target;
        }
        if (this.prevTarget == null) {
            return;
        }
        if (this.animation.getRGB() == 0.0f) {
            this.prevTarget = null;
            return;
        }
        Vec3 renderPos = this.getRenderPos(this.prevTarget);
        if (event.getMainCamera().position().distanceToSqr(renderPos) < MIN_CAMERA_DISTANCE_SQR) {
            return;
        }
        PoseStack ms = event.pose();
        ms.pushPose();
        
        GLStateSnapshot glState = GLStateSnapshot.capture();
        
        try {
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_BLEND);
            org.lwjgl.opengl.GL11.glBlendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
            org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
            if (TargetESP.mc.level.clip(new ClipContext(TargetESP.mc.gameRenderer.getMainCamera().position(), this.prevTarget.getEyePosition(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, TargetESP.mc.player)).getType() != HitResult.Type.MISS) {
                org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
            }
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
            org.lwjgl.opengl.GL11.glDepthMask(false);
            if (this.crystals.isSelected()) {
                this.drawCrystals(ms, this.prevTarget);
            } else if (this.circles.isSelected()) {
                this.drawCircles(ms, this.prevTarget);
            } else if (this.marker.isSelected()) {
                // Marker disabled until rewrite.
            } else if (this.jelly.isSelected()) {
                this.drawJelly(ms, this.prevTarget);
            } else if (this.energy.isSelected()) {
                this.drawEnergy(ms, this.prevTarget);
            } else if (this.crown.isSelected()) {
                this.drawCrown(ms, this.prevTarget);
            } else if (this.skeleton.isSelected()) {
                this.drawSkeleton(ms, this.prevTarget);
            } else if (this.chains.isSelected()) {
                this.drawChains(ms, this.prevTarget);
            } else if (this.chams.isSelected()) {
                // Chams is handled via Mixin
            } else {
                this.drawSouls(ms, this.prevTarget);
            }
        } finally {
            TextureBinder.unbind();
            moscow.rockstar.framework.shader.GlProgram.clearActive();
            glState.restore();
            ms.popPose();
        }
    };

    public TargetESP() {
    }

    public LivingEntity getPrevTarget() {
        return this.prevTarget;
    }

    public boolean isSkeletonMode() {
        return this.skeleton.isSelected();
    }

    public boolean isChamsMode() {
        return this.chams.isSelected();
    }

    public ColorRGBA getColor() {
        return this.syncWithTheme.isEnabled() ? Colors.getAccentColor() : this.colorTarget.getColor();
    }

    private LivingEntity getTarget() {
        LivingEntity livingEntity2;
        LivingEntity target2;
        Entity target1 = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        LivingEntity mainTarget = target1 instanceof LivingEntity ? (target2 = (LivingEntity)target1) : null;
        LivingEntity livingEntity = mainTarget;
        if (mainTarget != null && mainTarget != TargetESP.mc.player && !mainTarget.isDeadOrDying() && mainTarget.isAlive()) {
            this.targetTimer.reset();
            return mainTarget;
        }
        Entity entity = TargetESP.mc.crosshairPickEntity;
        if (entity instanceof LivingEntity && entity != TargetESP.mc.player && !(livingEntity2 = (LivingEntity)entity).isDeadOrDying() && livingEntity2.isAlive()) {
            this.targetTimer.reset();
            return livingEntity2;
        }
        if (this.prevTarget != null && this.prevTarget != TargetESP.mc.player && !this.prevTarget.isDeadOrDying() && this.prevTarget.isAlive() && !this.targetTimer.finished(2000L)) {
            return this.prevTarget;
        }
        return null;
    }

    private void drawSkeleton(PoseStack ms, LivingEntity target) {
        List<Vector3f[]> lines = TargetHud.SKELETON_LINES.get(target.getId());
        if (lines == null || lines.isEmpty()) {
            return;
        }
        ms.pushPose();
        RenderUtility.prepareMatrices((PoseStack)ms);
        GlProgram.clearActive();
        org.lwjgl.opengl.GL11.glLineWidth((float)3.0f);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
        ColorRGBA color = this.getColor();
        int a = (int)(255.0f * this.animation.getRGB());
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
        
        PoseStack.Pose entry = ms.last();
        Matrix4f matrix = entry.pose();
        int rgb = color.withAlpha((float)a).getRGB();
        
        for (Vector3f[] line : lines) {
            Vec3 start = new Vec3((double)line[0].x, (double)line[0].y, (double)line[0].z);
            Vec3 end = new Vec3((double)line[1].x, (double)line[1].y, (double)line[1].z);
            Vec3 normalized = end.subtract(start).normalize();
            if (normalized.lengthSqr() < 1.0E-6f) {
                normalized = new Vec3(0, 1, 0);
            }
            builder.addVertex(matrix, (float)start.x, (float)start.y, (float)start.z)
                   .setColor(rgb)
                   .setNormal(entry, (float)normalized.x, (float)normalized.y, (float)normalized.z)
                   .setLineWidth(3.0f);
            builder.addVertex(matrix, (float)end.x, (float)end.y, (float)end.z)
                   .setColor(rgb)
                   .setNormal(entry, (float)normalized.x, (float)normalized.y, (float)normalized.z)
                   .setLineWidth(3.0f);
        }
        MeshData mesh = builder.build();
        if (mesh != null) {
            try {
                moscow.rockstar.utility.render.MeshDrawHelper.disableDepthOverride = true;
                MeshDrawHelper.draw(mesh, LINES_SEE_THROUGH);
            } finally {
                moscow.rockstar.utility.render.MeshDrawHelper.disableDepthOverride = false;
            }
        }
        org.lwjgl.opengl.GL11.glLineWidth((float)1.0f);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
        ms.popPose();
    }

    private void drawCrystals(PoseStack ms, LivingEntity target) {
        Camera camera = TargetESP.mc.gameRenderer.getMainCamera();
        ColorRGBA color = this.getColor();
        float width = this.prevTarget.getBbWidth() * 1.5f;
        Vec3 renderPos = this.getRenderPos(this.prevTarget);
        RenderUtility.prepareMatrices((PoseStack)ms, (Vec3)renderPos);
        BufferBuilder builder = CrystalRenderer.createBuffer();
        for (int i = 0; i < 360; i += 20) {
            float val = 1.2f - 0.5f * this.animation.getRGB();
            float sin = (float)(MathUtility.sin((double)((float)Math.toRadians((float)i + this.moving.getRGB() * 0.3f))) * (double)width * (double)val);
            float cos = (float)(MathUtility.cos((double)((float)Math.toRadians((float)i + this.moving.getRGB() * 0.3f))) * (double)width * (double)val);
            float size = 0.1f;
            float y = 0.1f + (float)(target.getBbHeight() * Math.abs(MathUtility.sin((double)i)));
            this.renderCrystal(ms, builder, target, sin, y, cos, size, color.withAlpha(255.0f * this.animation.getRGB()), false);
        }
        this.drawCrystalBuffer(builder);
        BufferBuilder bloomBuffer = this.beginBloomBuffer();
        float alpha = this.animation.getRGB();
        for (int i = 0; i < 360; i += 20) {
            float val = 1.2f - 0.5f * alpha;
            float sin = (float)(MathUtility.sin((double)((float)Math.toRadians((float)i + this.moving.getRGB() * 0.3f))) * (double)width * (double)val);
            float cos = (float)(MathUtility.cos((double)((float)Math.toRadians((float)i + this.moving.getRGB() * 0.3f))) * (double)width * (double)val);
            float y = 0.1f + (float)(target.getBbHeight() * Math.abs(MathUtility.sin((double)i)));
            this.drawCrystalBloomLayer(ms, camera, bloomBuffer, sin, y, cos, alpha, color);
        }
        this.drawTexBuffer(bloomBuffer);
    }

    private void drawCrown(PoseStack ms, LivingEntity target) {
        boolean wasHit;
        Camera camera = TargetESP.mc.gameRenderer.getMainCamera();
        ColorRGBA color = this.getColor();
        float width = target.getBbWidth() / 2.0f;
        float height = target.getBbHeight();
        Vec3 renderPos = this.getRenderPos(this.prevTarget);
        RenderUtility.prepareMatrices((PoseStack)ms, (Vec3)renderPos);
        BufferBuilder builder = CrystalRenderer.createBuffer();
        float currentHealth = target.getHealth();
        boolean bl = wasHit = currentHealth < this.prevHealth;
        if (wasHit) {
            this.prevHealth = currentHealth;
        } else if (target.hurtTime == 0) {
            this.prevHealth = currentHealth;
        }
        float targetHitOffset = 0.0f;
        if (target.hurtTime > 0) {
            float hitProgress = (10.0f - (float)target.hurtTime) / 10.0f;
            float t = Mth.clamp(hitProgress, 0.0f, 1.0f);
            targetHitOffset = t * t * (3.0f - 2.0f * t) * 0.22f;
        }
        float lerpSpeed = targetHitOffset > this.smoothHitOffset ? 0.07f : 0.11f;
        this.smoothHitOffset += (targetHitOffset - this.smoothHitOffset) * lerpSpeed;
        float hitOffset = this.smoothHitOffset;
        float offset = 0.15f;
        float[][] topPositions = new float[][]{{width + offset, height + offset, width + offset}, {-width - offset, height + offset, width + offset}, {-width - offset, height + offset, -width - offset}, {width + offset, height + offset, -width - offset}};
        float size = 0.12f * this.animation.getRGB();
        float[][] crystalPositions = new float[4][3];
        for (int i = 0; i < 4; ++i) {
            float baseX = topPositions[i][0];
            float baseY = topPositions[i][1];
            float baseZ = topPositions[i][2];
            Vec3 arrowPos = new Vec3((double)baseX, (double)baseY, (double)baseZ);
            Vec3 targetCorner = new Vec3(baseX > 0.0f ? (double)width : (double)(-width), (double)height, baseZ > 0.0f ? (double)width : (double)(-width));
            Vector3f direction = new Vector3f((float)(targetCorner.x - arrowPos.x), (float)(targetCorner.y - arrowPos.y), (float)(targetCorner.z - arrowPos.z)).normalize();
            float x = baseX + direction.x * hitOffset;
            float y = baseY + direction.y * hitOffset;
            float z = baseZ + direction.z * hitOffset;
            crystalPositions[i][0] = x;
            crystalPositions[i][1] = y;
            crystalPositions[i][2] = z;
            this.renderCrystal(ms, builder, target, x, y, z, size, color.withAlpha(255.0f * this.animation.getRGB()), true);
        }
        this.drawCrystalBuffer(builder);
        BufferBuilder bloomBuffer = this.beginBloomBuffer();
        float alpha = this.animation.getRGB();
        for (int i = 0; i < crystalPositions.length; ++i) {
            float[] pos = crystalPositions[i];
            this.drawCrystalBloomLayer(ms, camera, bloomBuffer, pos[0], pos[1], pos[2], alpha, color);
        }
        this.drawTexBuffer(bloomBuffer);
    }

    private void addBillboardQuad(Matrix4f matrix, Vector3f right, Vector3f up, BufferBuilder builder, float cx, float cy, float cz, float size, ColorRGBA color) {
        float half = size / 2.0f;
        int rgb = color.getRGB();
        
        float x0 = cx - right.x * half - up.x * half;
        float y0 = cy - right.y * half - up.y * half;
        float z0 = cz - right.z * half - up.z * half;
        
        float x1 = cx + right.x * half - up.x * half;
        float y1 = cy + right.y * half - up.y * half;
        float z1 = cz + right.z * half - up.z * half;
        
        float x2 = cx + right.x * half + up.x * half;
        float y2 = cy + right.y * half + up.y * half;
        float z2 = cz + right.z * half + up.z * half;
        
        float x3 = cx - right.x * half + up.x * half;
        float y3 = cy - right.y * half + up.y * half;
        float z3 = cz - right.z * half + up.z * half;
        
        builder.addVertex(matrix, x0, y0, z0).setUv(0.0f, 1.0f).setColor(rgb);
        builder.addVertex(matrix, x1, y1, z1).setUv(1.0f, 1.0f).setColor(rgb);
        builder.addVertex(matrix, x2, y2, z2).setUv(1.0f, 0.0f).setColor(rgb);
        builder.addVertex(matrix, x3, y3, z3).setUv(0.0f, 0.0f).setColor(rgb);
    }

    private void drawEnergy(PoseStack ms, LivingEntity target) {
        Camera camera = TargetESP.mc.gameRenderer.getMainCamera();
        ColorRGBA color = this.getColor();
        Identifier id = Rockstar.id((String)"textures/bloom.png");
        TextureBinder.bind(id);
        GlProgram.usePositionTexColor();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        RenderUtility.prepareMatrices((PoseStack)ms, (Vec3)this.getRenderPos(this.prevTarget));
        Matrix4f matrix = ms.last().pose();
        
        Quaternionf rot = camera.rotation();
        Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        rot.transform(right);
        rot.transform(up);
        
        float halfWidth = target.getBbWidth() / 2.0f;
        float height = target.getBbHeight();
        float cornerRadius = (float)((double)halfWidth * Math.sqrt(2.0)) + 0.05f;
        for (int streamIndex = 0; streamIndex < 4; ++streamIndex) {
            float startAngle = (float)Math.toRadians(streamIndex * 90 + 45);
            float offsetX = (float)(Math.cos(startAngle) * (double)cornerRadius);
            float offsetZ = (float)(Math.sin(startAngle) * (double)cornerRadius);
            for (int i = 0; i < 360; i += 10) {
                float progress = (this.moving.getRGB() * 0.7f + (float)(streamIndex * 90) + (float)i) % 720.0f;
                float yPos = progress < 360.0f ? height * (progress / 360.0f) : height * (1.0f - (progress - 360.0f) / 360.0f);
                float spiralAngle = (float)Math.toRadians(progress * 0.6f + (float)i);
                float spiralRadius = 0.15f;
                float xPos = offsetX + (float)(Math.cos(spiralAngle) * (double)spiralRadius);
                float zPos = offsetZ + (float)(Math.sin(spiralAngle) * (double)spiralRadius);
                float alpha = this.animation.getRGB();
                if (progress > 360.0f) {
                    alpha *= 0.6f;
                }
                float size = 0.12f;
                float glowSize = 0.5f;
                
                this.addBillboardQuad(matrix, right, up, builder, xPos, yPos, zPos, glowSize, color.withAlpha(color.getAlpha() * alpha * 0.15f));
                this.addBillboardQuad(matrix, right, up, builder, xPos, yPos, zPos, size, color.withAlpha(color.getAlpha() * alpha));
            }
        }
        this.drawTexBuffer(builder);
    }

    private void drawGhosts(PoseStack ms, LivingEntity target) {
        Camera camera = TargetESP.mc.gameRenderer.getMainCamera();
        ColorRGBA color = this.getColor();
        Identifier id = Rockstar.id((String)"textures/bloom.png");
        float width = target.getBbWidth() * 1.5f;
        TextureBinder.bind(id);
        GlProgram.usePositionTexColor();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        RenderUtility.prepareMatrices((PoseStack)ms, (Vec3)this.getRenderPos(target));
        Matrix4f matrix = ms.last().pose();
        
        Quaternionf rot = camera.rotation();
        Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        rot.transform(right);
        rot.transform(up);
        
        float time = this.moving.getRGB() * SPEED_GHOSTS;
        int step = 2;
        int wormTick = 0;
        int wormCD = 0;
        int wormCount = 0;
        
        ColorRGBA bigColor = color.withAlpha(color.getAlpha() * this.animation.getRGB() * 0.05f);
        ColorRGBA smallColor = color.withAlpha(color.getAlpha() * this.animation.getRGB());
        
        for (int i = 0; i < 360; i += step) {
            float size = 0.13f + 0.005f * (float)wormTick;
            float bigSize = 0.7f + 0.005f * (float)wormTick;
            if (wormCD > 0) {
                wormCD -= step;
                continue;
            }
            if ((wormTick += step) > 50) {
                wormCD = 100;
                wormTick = 0;
                ++wormCount;
                continue;
            }
            float val = Math.max(0.5f, 1.2f - 0.5f * this.animation.getRGB());
            float sin = (float)(MathUtility.sin((double)((float)Math.toRadians((float)i + time * 1.0f))) * (double)width * (double)val);
            float cos = (float)(MathUtility.cos((double)((float)Math.toRadians((float)i + time * 1.0f))) * (double)width * (double)val);
            
            float cx = sin;
            float cy = (float)((target.getBbHeight() / 1.5f) + (target.getBbHeight() / 3.0f) * MathUtility.sin((double)Math.toRadians((float)i / 2.0f + time / 5.0f)));
            float cz = cos;
            
            this.addBillboardQuad(matrix, right, up, builder, cx, cy, cz, bigSize, bigColor);
            this.addBillboardQuad(matrix, right, up, builder, cx, cy, cz, size, smallColor);
        }
        this.drawTexBuffer(builder);
    }

    private void drawSouls(PoseStack ms, LivingEntity target) {
        if (this.soulsTriangle.isSelected()) {
            this.drawSoulTriangle(ms, target);
            return;
        }
        if (this.soulsXFormatted.isSelected()) {
            this.drawSoulXFormatted(ms, target);
            return;
        }
        if (this.soulsWraith.isSelected()) {
            this.drawGhosts(ms, target);
            this.drawSoulCore(ms, target, 0.9f, 0.12f, false);
            return;
        }
        this.drawSoulCore(ms, target, 0.9f, 0.12f, true);
    }

    private void drawSoulCore(PoseStack ms, LivingEntity target, float pulseSpeed, float orbitRadius, boolean prepare) {
        Camera camera = TargetESP.mc.gameRenderer.getMainCamera();
        ColorRGBA color = ColorRGBA.WHITE.mix(this.getColor(), 0.08f);
        Identifier id = Rockstar.id((String)"textures/bloom.png");
        TextureBinder.bind(id);
        GlProgram.usePositionTexColor();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        if (prepare) {
            RenderUtility.prepareMatrices((PoseStack)ms, (Vec3)this.getRenderPos(target));
        }
        Matrix4f matrix = ms.last().pose();
        
        Quaternionf rot = camera.rotation();
        Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        rot.transform(right);
        rot.transform(up);
        
        float time = this.moving.getRGB();
        float y = target.getBbHeight() * 0.52f;
        float pulse = 0.82f + 0.18f * (float)Math.sin(time * 0.05f * pulseSpeed);
        float coreSize = 0.24f * pulse;
        float coreGlow = 1.15f * pulse;
        float alpha = this.animation.getRGB();
        
        this.addBillboardQuad(matrix, right, up, builder, 0.0f, y, 0.0f, coreGlow, color.withAlpha(color.getAlpha() * alpha * 0.28f));
        this.addBillboardQuad(matrix, right, up, builder, 0.0f, y, 0.0f, coreSize, color.withAlpha(color.getAlpha() * alpha));
        
        for (int i = 0; i < 14; ++i) {
            float angle = (float)i * 25.714285f + time * 1.4f;
            float x = (float)Math.cos(Math.toRadians(angle)) * orbitRadius;
            float z = (float)Math.sin(Math.toRadians(angle)) * orbitRadius;
            float orbY = y + 0.05f * (float)Math.sin(Math.toRadians((float)i * 25.0f + time * 1.8f));
            float size = 0.05f + 0.02f * (float)Math.sin(Math.toRadians((float)i * 40.0f + time));
            float glow = size * 3.6f;
            float orbAlpha = alpha * (0.7f + 0.3f * (float)Math.sin(Math.toRadians((float)i * 31.0f + time * 2.0f)));
            
            this.addBillboardQuad(matrix, right, up, builder, x, orbY, z, glow, color.withAlpha(color.getAlpha() * orbAlpha * 0.2f));
            this.addBillboardQuad(matrix, right, up, builder, x, orbY, z, size, color.withAlpha(color.getAlpha() * orbAlpha));
        }
        this.drawTexBuffer(builder);
    }

    private void drawCircles(PoseStack ms, LivingEntity target) {
        Camera camera = TargetESP.mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.position();
        Vec3 targetPos = this.getRenderPos(target);
        double entX = targetPos.x - cameraPos.x();
        double entY = targetPos.y - cameraPos.y();
        double entZ = targetPos.z - cameraPos.z();
        float movingValue = this.moving.getRGB() * 0.7f;
        float width = target.getBbWidth() * 1.45f;
        float baseVal = Math.max(0.5f, 0.8f - 0.1f * this.animation.getRGB());
        ColorRGBA color = this.getColor();
        Identifier bloomTexture = Rockstar.id((String)"textures/bloom.png");
        TextureBinder.bind(bloomTexture);
        GlProgram.usePositionTexColor();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        
        ms.pushPose();
        RenderUtility.prepareMatrices((PoseStack)ms, Vec3.ZERO);
        Matrix4f matrix = ms.last().pose();
        
        Quaternionf rot = camera.rotation();
        Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        rot.transform(right);
        rot.transform(up);
        
        int step = 3;
        float size = 0.32f;
        float bigSize = 0.64f;
        int alpha = (int)(this.animation.getRGB() * 255.0f);
        ColorRGBA baseColor = color.withAlpha((float)alpha);
        ColorRGBA bigColor = baseColor.withAlpha((float)alpha * 0.1f);
        
        for (int i = 0; i < 360; i += step) {
            if ((int)((float)i / 45.0f) % 2 == 0) continue;
            double rad = Math.toRadians((float)i + movingValue);
            float sin = (float)(entX + Math.sin(rad) * (double)width * (double)baseVal);
            float cos = (float)(entZ + Math.cos(rad) * (double)width * (double)baseVal);
            double radAngle = Math.toRadians(movingValue);
            float waveValue = (float)((1.0 - Math.cos(radAngle)) / 2.0);
            float yPos = (float)(entY + (double)target.getBbHeight() * (double)waveValue);
            
            this.addBillboardQuad(matrix, right, up, builder, sin, yPos, cos, bigSize, bigColor);
            this.addBillboardQuad(matrix, right, up, builder, sin, yPos, cos, size, baseColor);
        }
        ms.popPose();
        this.drawTexBuffer(builder);
    }

    private void drawJelly(PoseStack ms, LivingEntity target) {
        float jellySquish;
        Camera camera = TargetESP.mc.gameRenderer.getMainCamera();
        ColorRGBA color = this.getColor();
        Identifier id = Rockstar.id((String)"textures/bloom.png");
        TextureBinder.bind(id);
        GlProgram.usePositionTexColor();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        RenderUtility.prepareMatrices((PoseStack)ms, (Vec3)this.getRenderPos(this.prevTarget));
        Matrix4f matrix = ms.last().pose();
        
        Quaternionf rot = camera.rotation();
        Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        rot.transform(right);
        rot.transform(up);
        
        float halfWidth = target.getBbWidth() / 2.0f;
        float height = target.getBbHeight();
        float time = this.moving.getRGB();
        float waveProgress = (float)((Math.sin(Math.toRadians(time * 0.5f)) + 1.0) / 2.0);
        float yPos = waveProgress * height;
        float baseRadius = (float)((double)halfWidth * Math.sqrt(2.0)) + 0.08f;
        float radiusMultiplier = jellySquish = 1.0f + (float)Math.sin(Math.toRadians(time * 0.5f + 90.0f)) * 0.15f;
        int step = 1;
        for (int i = 0; i < 360; i += step) {
            float angle = (float)Math.toRadians(i);
            float localSquish = 1.0f + (float)Math.sin(Math.toRadians((float)(i * 2) + time * 0.8f)) * 0.08f;
            float radius = baseRadius * radiusMultiplier * localSquish;
            float xPos = (float)(Math.cos(angle) * (double)radius);
            float zPos = (float)(Math.sin(angle) * (double)radius);
            float alpha = this.animation.getRGB();
            float size = 0.09f;
            float glowSize = 0.5f;
            
            this.addBillboardQuad(matrix, right, up, builder, xPos, yPos, zPos, glowSize, color.withAlpha(color.getAlpha() * alpha * 0.25f));
            this.addBillboardQuad(matrix, right, up, builder, xPos, yPos, zPos, size, color.withAlpha(color.getAlpha() * alpha * 0.8f));
        }
        this.drawTexBuffer(builder);
    }

    private void drawChains(PoseStack ms, LivingEntity target) {
        if (target == null) {
            return;
        }
        Camera camera = TargetESP.mc.gameRenderer.getMainCamera();
        Vec3 targetPos = this.getRenderPos(target);
        Vec3 cameraPos = camera.position();
        double entX = targetPos.x - cameraPos.x();
        double entY = targetPos.y - cameraPos.y() - 0.5;
        double entZ = targetPos.z - cameraPos.z();
        float rotSpeed = 0.5f * SPEED_CHAINS;
        float chainSize = 4.0f;
        float down = 1.0f;
        float movingValue = this.moving.getRGB() * SPEED_CHAINS;
        float gradusX = (float)(20.0 * Math.min(1.0 + Math.sin(Math.toRadians(movingValue)), 1.0));
        float gradusZ = (float)(20.0 * (Math.min(1.0 + Math.sin(Math.toRadians(movingValue)), 2.0) - 1.0));
        float width = target.getBbWidth() * 1.5f;
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_BLEND);
        org.lwjgl.opengl.GL11.glBlendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
        Identifier chainTexture = Rockstar.id((String)"textures/chain.png");
        for (int chain = 0; chain < 2; ++chain) {
            float val = 1.2f - 0.5f * this.animation.getRGB();
            ms.pushPose();
            ms.translate(entX, entY + (double)(target.getBbHeight() / 2.0f), entZ);
            float x = 0.0f;
            float y = 0.0f;
            float z = 0.0f;
            ms.mulPose(Axis.YP.rotationDegrees(chain == 0 ? gradusX : -gradusX));
            ms.mulPose(Axis.XP.rotationDegrees(chain == 0 ? gradusZ : -gradusZ));
            Matrix4f matrix = ms.last().pose();
            int alpha = (int)(this.animation.getRGB() * 255.0f);
            ColorRGBA baseColor = this.getColor();
            int r = (int)baseColor.getRed();
            int g = (int)baseColor.getGreen();
            int b = (int)baseColor.getBlue();
            TextureBinder.bind(chainTexture);
            GlProgram.usePositionTexColor();
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            int modif = 22;
            for (int i = 0; i < 720; i += modif) {
                float prevSin = (float)((double)(x + (chain == 0 ? gradusX : -gradusX) / 100.0f) + Math.sin(Math.toRadians((float)(i - modif) + movingValue * rotSpeed)) * (double)width * (double)val);
                float prevCos = (float)((double)(z + (chain == 0 ? -gradusZ : gradusZ) / 100.0f) + Math.cos(Math.toRadians((float)(i - modif) + movingValue * rotSpeed)) * (double)width * (double)val);
                float sin = (float)((double)(x + (chain == 0 ? gradusX : -gradusX) / 100.0f) + Math.sin(Math.toRadians((float)i + movingValue * rotSpeed)) * (double)width * (double)val);
                float cos = (float)((double)(z + (chain == 0 ? -gradusZ : gradusZ) / 100.0f) + Math.cos(Math.toRadians((float)i + movingValue * rotSpeed)) * (double)width * (double)val);
                float rawU1 = 0.0027777778f * (float)(i - modif) * chainSize;
                float rawU2 = 0.0027777778f * (float)i * chainSize;
                float u1 = (rawU1 % 1.0f + 1.0f) % 1.0f;
                float u2 = (rawU2 % 1.0f + 1.0f) % 1.0f;
                if (u2 <= 0.001f && u1 > 0.5f) {
                    u2 = 1.0f;
                }
                builder.addVertex(matrix, prevSin, y, prevCos).setUv(u1, 0.0f).setColor(r, g, b, alpha);
                builder.addVertex(matrix, sin, y, cos).setUv(u2, 0.0f).setColor(r, g, b, alpha);
                builder.addVertex(matrix, sin, y + down, cos).setUv(u2, 0.99f).setColor(r, g, b, alpha);
                builder.addVertex(matrix, prevSin, y + down, prevCos).setUv(u1, 0.99f).setColor(r, g, b, alpha);
            }
            this.drawTexBuffer(builder);
            ms.popPose();
        }
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
    }

    private BufferBuilder beginBloomBuffer() {
        TextureBinder.unbind();
        GlProgram.clearActive();
        TextureBinder.bind(Rockstar.id("textures/bloom.png"));
        GlProgram.usePositionTexColor();
        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    private void drawBloomBillboard(PoseStack ms, Camera camera, BufferBuilder buffer, float x, float y, float z, float size, ColorRGBA color) {
        ms.pushPose();
        ms.translate((double)x, (double)y, (double)z);
        ms.mulPose(camera.rotation());
        DrawUtility.drawImage((PoseStack)ms, buffer, (double)(-size / 2.0f), (double)(-size / 2.0f), (double)0.0, (double)size, (double)size, color);
        ms.popPose();
    }

    private void drawCrystalBloomLayer(PoseStack ms, Camera camera, BufferBuilder buffer, float x, float y, float z, float alpha, ColorRGBA color) {
        this.drawBloomBillboard(ms, camera, buffer, x, y, z, 0.95f, color.withAlpha(255.0f * alpha * 0.22f));
        this.drawBloomBillboard(ms, camera, buffer, x, y, z, 0.4f, color.withAlpha(255.0f * alpha * 0.5f));
    }

    private void drawCrystalBuffer(BufferBuilder builder) {
        MeshData mesh = builder.build();
        if (mesh != null) {
            TextureBinder.unbind();
            GlProgram.clearActive();
            try {
                moscow.rockstar.utility.render.MeshDrawHelper.disableDepthOverride = true;
                MeshDrawHelper.draw(mesh, ADDITIVE_COLOR);
            } finally {
                moscow.rockstar.utility.render.MeshDrawHelper.disableDepthOverride = false;
            }
        }
    }

    private void drawTexBuffer(BufferBuilder builder) {
        MeshData mesh = builder.build();
        if (mesh != null) {
            try {
                moscow.rockstar.utility.render.MeshDrawHelper.disableDepthOverride = true;
                MeshDrawHelper.draw(mesh, ADDITIVE_TEXTURED);
            } finally {
                moscow.rockstar.utility.render.MeshDrawHelper.disableDepthOverride = false;
            }
        }
    }

    private void renderCrystal(PoseStack ms, BufferBuilder builder, LivingEntity target, float localX, float localY, float localZ, float size, ColorRGBA color, boolean half) {
        ms.pushPose();
        ms.translate((double)localX, (double)localY, (double)localZ);
        Vector3f direction = new Vector3f(
            -localX,
            (float)((double)target.getBbHeight() / 2.0 - (double)localY),
            -localZ
        );
        if (direction.lengthSquared() > 1.0E-6f) {
            direction.normalize();
            ms.mulPose(new Quaternionf().rotationTo(new Vector3f(0.0f, 1.0f, 0.0f), direction));
        }
        CrystalRenderer.render(ms, builder, 0.0f, 0.0f, 0.0f, size, color, half);
        ms.popPose();
    }

    private void drawSoulTriangle(PoseStack ms, LivingEntity target) {
        Camera camera = TargetESP.mc.gameRenderer.getMainCamera();
        Vec3 renderPos = this.getRenderPos(target);
        ColorRGBA color = this.getColor();
        float width = target.getBbWidth() * 1.2f;
        float size = 0.13f * this.animation.getRGB();
        float alpha = this.animation.getRGB();
        RenderUtility.prepareMatrices(ms, renderPos);
        BufferBuilder builder = CrystalRenderer.createBuffer();
        float time = this.moving.getRGB();
        float orbitSpeed = 0.52f;
        float[][] crystalPositions = new float[TRIANGLE_ORBIT_COUNT][3];
        for (int i = 0; i < TRIANGLE_ORBIT_COUNT; ++i) {
            float angle = (float)Math.toRadians(360.0f / (float)TRIANGLE_ORBIT_COUNT * (float)i + time * orbitSpeed);
            float sin = (float)(Math.cos((double)angle) * (double)width);
            float cos = (float)(Math.sin((double)angle) * (double)width);
            float y = target.getBbHeight() * 0.5f + (float)Math.sin((double)time * 0.035 + (double)i * 1.15) * 0.06f;
            crystalPositions[i][0] = sin;
            crystalPositions[i][1] = y;
            crystalPositions[i][2] = cos;
            this.renderCrystal(ms, builder, target, sin, y, cos, size, color.withAlpha(255.0f * alpha), true);
        }
        this.drawCrystalBuffer(builder);
        BufferBuilder bloomBuffer = this.beginBloomBuffer();
        for (int i = 0; i < TRIANGLE_ORBIT_COUNT; ++i) {
            float[] pos = crystalPositions[i];
            this.drawCrystalBloomLayer(ms, camera, bloomBuffer, pos[0], pos[1], pos[2], alpha, color);
        }
        this.drawTexBuffer(bloomBuffer);
        this.drawSoulCore(ms, target, 0.75f, 0.1f, false);
    }

    private void drawSoulXFormatted(PoseStack ms, LivingEntity target) {
        Camera camera = TargetESP.mc.gameRenderer.getMainCamera();
        Vec3 renderPos = this.getRenderPos(target);
        ColorRGBA color = this.getColor();
        float width = target.getBbWidth() * 1.05f;
        float height = target.getBbHeight() * 0.55f;
        float size = 0.12f * this.animation.getRGB();
        float alpha = this.animation.getRGB();
        RenderUtility.prepareMatrices(ms, renderPos);
        BufferBuilder builder = CrystalRenderer.createBuffer();
        float time = this.moving.getRGB();
        float orbitSpeed = 0.12f;
        float[][] crystalPositions = new float[X_ORBIT_COUNT][3];
        for (int i = 0; i < X_ORBIT_COUNT; ++i) {
            float angle = (float)Math.toRadians((float)(i * 90) + time * (i % 2 == 0 ? orbitSpeed : -orbitSpeed));
            float tilt = (float)Math.toRadians(i % 2 == 0 ? 30.0f : -30.0f);
            float x = (float)(Math.cos(angle) * width);
            float z = (float)(Math.sin(angle) * width * Math.cos(tilt));
            float y = height + (float)(Math.sin(angle) * width * Math.sin(tilt));
            y += (float)Math.sin(time * 0.04 + (double)i * 1.5) * 0.04f;
            crystalPositions[i][0] = x;
            crystalPositions[i][1] = y;
            crystalPositions[i][2] = z;
            this.renderCrystal(ms, builder, target, x, y, z, size, color.withAlpha(255.0f * alpha), true);
        }
        this.drawCrystalBuffer(builder);
        BufferBuilder bloomBuffer = this.beginBloomBuffer();
        for (int i = 0; i < X_ORBIT_COUNT; ++i) {
            float[] pos = crystalPositions[i];
            this.drawCrystalBloomLayer(ms, camera, bloomBuffer, pos[0], pos[1], pos[2], alpha, color);
        }
        this.drawTexBuffer(bloomBuffer);
        this.drawSoulCore(ms, target, 0.8f, 0.09f, false);
    }

    private static final java.util.Map<Identifier, net.minecraft.client.renderer.rendertype.RenderType> CHAMS_RENDER_TYPES = new java.util.concurrent.ConcurrentHashMap<>();

    public static net.minecraft.client.renderer.rendertype.RenderType getChamsRenderType(Identifier texture) {
        return CHAMS_RENDER_TYPES.computeIfAbsent(texture, t -> {
            net.minecraft.client.renderer.rendertype.RenderSetup setup = net.minecraft.client.renderer.rendertype.RenderSetup.builder(ENTITY_SEE_THROUGH)
                .withTexture("Sampler0", Rockstar.id("textures/white.png"))
                .useOverlay()
                .createRenderSetup();
            return net.minecraft.client.renderer.rendertype.RenderType.create("entity_see_through_chams_" + t.getPath().replace('/', '_'), setup);
        });
    }

    private Vec3 getRenderPos(LivingEntity target) {
        return Utils.getInterpolatedPos(target, TargetESP.mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.prevTarget = null;
        this.animation.setValue(0.0f);
    }

    public void tick() {
        super.tick();
    }
}
