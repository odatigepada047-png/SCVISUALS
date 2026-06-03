/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.player.AbstractClientPlayer
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  net.minecraft.client.render.BufferRenderer
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat.Mode
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  net.minecraft.client.render.entity.EntityRenderer
 *  net.minecraft.client.render.entity.LivingEntityRenderer
 *  net.minecraft.client.render.entity.model.EntityModel
 *  net.minecraft.client.render.entity.state.LivingEntityRenderState
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.phys.Vec2
 *  org.joml.Matrix4f
 */
package moscow.rockstar.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.gradient.Gradient;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.framework.shader.impl.BlurProgram;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IWindow;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.ColorUtility;
import moscow.rockstar.utility.render.CustomRenderTarget;
import moscow.rockstar.utility.render.HookLimiter;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import moscow.rockstar.utility.render.batching.impl.RoundedRectBatching;
import moscow.rockstar.utility.render.batching.impl.ShadowBatching;
import moscow.rockstar.utility.render.batching.impl.SquircleBatching;
import moscow.rockstar.utility.render.obj.CustomSprite;
import moscow.rockstar.utility.render.penis.PenisSprite;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.player.AbstractClientPlayer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;
import org.joml.Matrix3x2fStack;
import moscow.rockstar.utility.render.MeshDrawHelper;
import moscow.rockstar.utility.render.TextureBinder;

import ru.kotopushka.compiler.sdk.annotations.Initialization;

public final class DrawUtility
implements IMinecraft,
IWindow {
    public static final float DEFAULT_SMOOTHNESS = 0.5f;
    public static final HookLimiter limiter = new HookLimiter(true);
    public static GlProgram rectangleProgram;
    private static GlProgram squircleProgram;
    private static GlProgram roundedTextureProgram;
    private static GlProgram squircleTextureProgram;
    private static GlProgram borderProgram;
    private static GlProgram loadingProgram;
    private static GlProgram glassProgram;
    private static GlProgram gradientRectangleProgram;
    public static BlurProgram blurProgram;
    private static final CustomRenderTarget buffer;

    @Initialization
    public static void initializeShaders() {
        rectangleProgram = new GlProgram(Rockstar.id("rectangle/data"), DefaultVertexFormat.POSITION_COLOR);
        squircleProgram = new GlProgram(Rockstar.id("squircle/data"), DefaultVertexFormat.POSITION_COLOR);
        squircleTextureProgram = new GlProgram(Rockstar.id("squircle_texture/data"), DefaultVertexFormat.POSITION_TEX_COLOR);
        roundedTextureProgram = new GlProgram(Rockstar.id("texture/data"), DefaultVertexFormat.POSITION_TEX_COLOR);
        borderProgram = new GlProgram(Rockstar.id("border/data"), DefaultVertexFormat.POSITION_COLOR);
        loadingProgram = new GlProgram(Rockstar.id("loading/data"), DefaultVertexFormat.POSITION_COLOR);
        glassProgram = new GlProgram(Rockstar.id("liquidglass/data"), DefaultVertexFormat.POSITION_TEX_COLOR);
        gradientRectangleProgram = new GlProgram(Rockstar.id("gradient_rectangle/data"), DefaultVertexFormat.POSITION_COLOR);
        blurProgram = new BlurProgram();
        blurProgram.initShaders();
    }

    public static void updateBuffer() {
        buffer.setup();
        GlProgram.usePositionTexColor();
        TextureBinder.bindMainColor();
        DrawUtility.drawQuad(0.0f, 0.0f, mw.getGuiScaledWidth(), mw.getGuiScaledHeight(), true);
        buffer.stop();
    }

    private static void drawQuad(float x, float y, float width, float height, boolean flip) {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        int color = -1;
        float vTop = flip ? 0.0f : 1.0f;
        float vBottom = flip ? 1.0f : 0.0f;
        builder.addVertex(x, y, 0.0f).setUv(0.0f, vBottom).setColor(-1);
        builder.addVertex(x, y + height, 0.0f).setUv(0.0f, vTop).setColor(-1);
        builder.addVertex(x + width, y + height, 0.0f).setUv(1.0f, vTop).setColor(-1);
        builder.addVertex(x + width, y, 0.0f).setUv(1.0f, vBottom).setColor(-1);
        MeshDrawHelper.drawBuilt(builder.build());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawLine(PoseStack matrices, Vec2 from, Vec2 to, ColorRGBA color) {
        matrices.pushPose();
        try {
            Matrix4f matrix4f = matrices.last().pose();
            GlProgram.usePositionColor();
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            builder.addVertex(matrix4f, from.x, from.y, 0.0f).setColor(color.getRGB());
            builder.addVertex(matrix4f, to.x, to.y, 0.0f).setColor(color.getRGB());
            MeshDrawHelper.drawBuilt(builder.build());
        }
        finally {
            matrices.popPose();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawBezier(PoseStack matrices, Vec2 p0, Vec2 p1, Vec2 p2, Vec2 p3, ColorRGBA color, int resolution) {
        matrices.pushPose();
        try {
            Matrix4f matrix4f = matrices.last().pose();
            GlProgram.usePositionColor();
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= resolution; ++i) {
                float t = (float)i / (float)resolution;
                float x = (float)MathUtility.cubicBezier(t, p0.x, p1.x, p2.x, p3.x);
                float y = (float)MathUtility.cubicBezier(t, p0.y, p1.y, p2.y, p3.y);
                builder.addVertex(matrix4f, x, y, 0.0f).setColor(color.getRGB());
            }
            MeshDrawHelper.drawBuilt(builder.build());
        }
        finally {
            matrices.popPose();
        }
    }

    private static float cubicBezier(float t, float p0, float p1, float p2, float p3) {
        float u = 1.0f - t;
        float tt = t * t;
        float uu = u * u;
        return uu * u * p0 + 3.0f * uu * t * p1 + 3.0f * u * tt * p2 + tt * t * p3;
    }

    public static void drawRect(Matrix3x2fStack matrices, float x, float y, float width, float height, ColorRGBA color) {
        DrawUtility.drawRect(copyStack(matrices), x, y, width, height, color);
    }

    public static void drawRect(PoseStack matrices, float x, float y, float width, float height, ColorRGBA color) {
        BufferBuilder builder;
        Batching batching = Batching.getActive();
        if (batching instanceof RectBatching batching2) {
            builder = batching2.getBuilder();
            Matrix4f matrix4f = matrices.last().pose();
            addGuiQuadVertices(builder, matrix4f, x, y, width, height, color.getRGB());
            return;
        }
        matrices.pushPose();
        Matrix4f matrix4f = matrices.last().pose();
        GlProgram.usePositionColor();
        DrawUtility.drawSetup();
        builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        addGuiQuadVertices(builder, matrix4f, x, y, width, height, color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        matrices.popPose();
    }

    public static void drawSquircle(PoseStack matrices, float x, float y, float width, float height, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        matrices.pushPose();
        Matrix4f m = matrices.last().pose();
        float smoothness = 0.5f;
        Batching batching = Batching.getActive();
        if (batching instanceof SquircleBatching) {
            SquircleBatching sb = (SquircleBatching)batching;
            sb.add(m, x, y, width, height, borderRadius.topLeftRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f, borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomRightRadius() * squirt / 2.0f, color.getRGB());
            matrices.popPose();
            return;
        }
        squircleProgram.use();
        squircleProgram.findUniform("Size").set(width, height);
        squircleProgram.findUniform("Radius").set(borderRadius.topLeftRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f, borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomRightRadius() * squirt / 2.0f);
        squircleProgram.findUniform("Smoothness").set(smoothness);
        squircleProgram.findUniform("CornerSmoothness").set(squirt);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float ax = x - horizontalPadding / 2.0f;
        float ay = y - verticalPadding / 2.0f;
        float aw = width + horizontalPadding;
        float ah = height + verticalPadding;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.addVertex(m, ax, ay, 0.0f).setColor(color.getRGB());
        builder.addVertex(m, ax, ay + ah, 0.0f).setColor(color.getRGB());
        builder.addVertex(m, ax + aw, ay + ah, 0.0f).setColor(color.getRGB());
        builder.addVertex(m, ax + aw, ay, 0.0f).setColor(color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        matrices.popPose();
    }

    public static void drawLoadingRect(PoseStack matrices, float x, float y, float width, float height, float progress, BorderRadius borderRadius, ColorRGBA color) {
        matrices.pushPose();
        Matrix4f matrix4f = matrices.last().pose();
        float smoothness = 0.5f;
        loadingProgram.use();
        loadingProgram.findUniform("Size").set(width, height);
        loadingProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        loadingProgram.findUniform("Smoothness").set(smoothness);
        loadingProgram.findUniform("Progress").set(progress);
        loadingProgram.findUniform("StripeWidth").set(0.0f);
        loadingProgram.findUniform("Fade").set(0.5f);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.addVertex(matrix4f, adjustedX, adjustedY, 0.0f).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).setColor(color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        matrices.popPose();
    }

    public static void drawLiquidGlass(PoseStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color, float globalAlpha, float fresnelPower, ColorRGBA fresnelColor, float baseAlpha, boolean fresnelInvert, float fresnelMix, float distortStrength, float squirt, boolean clean) {
        Matrix4f matrix = matrices.last().pose();
        DrawUtility.drawSetup();
        if (clean) {
            TextureBinder.bindMainColor();
        } else {
            TextureBinder.bindBlurResult();
        }
        glassProgram.use();
        glassProgram.findUniform("GlobalAlpha").set(globalAlpha);
        glassProgram.findUniform("Size").set(width, height);
        glassProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        glassProgram.findUniform("Smoothness").set(0.5f);
        glassProgram.findUniform("FresnelPower").set(fresnelPower);
        float[] fresnelRgb = ColorUtility.getRGBf(fresnelColor.getRGB());
        glassProgram.findUniform("FresnelColor").set(fresnelRgb[0], fresnelRgb[1], fresnelRgb[2]);
        glassProgram.findUniform("FresnelAlpha").set(ColorUtility.alphaf(fresnelColor.getRGB()));
        glassProgram.findUniform("BaseAlpha").set(baseAlpha);
        glassProgram.findUniform("FresnelInvert").set(fresnelInvert ? 1 : 0);
        glassProgram.findUniform("FresnelMix").set(fresnelMix);
        glassProgram.findUniform("DistortStrength").set(distortStrength);
        glassProgram.findUniform("CornerSmoothness").set(squirt);
        int screenWidth = mw.getGuiScaledWidth();
        int screenHeight = mw.getGuiScaledHeight();
        float u = x / (float)screenWidth;
        float v = ((float)screenHeight - y - height) / (float)screenHeight;
        float texWidth = width / (float)screenWidth;
        float texHeight = height / (float)screenHeight;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix, x, y, 0.0f).setUv(u, v + texHeight).setColor(color.getRGB());
        builder.addVertex(matrix, x, y + height, 0.0f).setUv(u, v).setColor(color.getRGB());
        builder.addVertex(matrix, x + width, y + height, 0.0f).setUv(u + texWidth, v).setColor(color.getRGB());
        builder.addVertex(matrix, x + width, y, 0.0f).setUv(u + texWidth, v + texHeight).setColor(color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        TextureBinder.unbind();
        DrawUtility.drawEnd();
    }

    public static void drawRoundedRect(PoseStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        matrices.pushPose();
        Matrix4f m = matrices.last().pose();
        float smoothness = 0.5f;
        Batching batching = Batching.getActive();
        if (batching instanceof RoundedRectBatching) {
            RoundedRectBatching rb = (RoundedRectBatching)batching;
            rb.add(m, x, y, width, height, borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius(), color.getRGB());
            matrices.popPose();
            return;
        }
        rectangleProgram.use();
        rectangleProgram.findUniform("Size").set(width, height);
        rectangleProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        rectangleProgram.findUniform("Smoothness").set(smoothness);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float ax = x - horizontalPadding / 2.0f;
        float ay = y - verticalPadding / 2.0f;
        float aw = width + horizontalPadding;
        float ah = height + verticalPadding;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.addVertex(m, ax, ay, 0.0f).setColor(color.getRGB());
        builder.addVertex(m, ax, ay + ah, 0.0f).setColor(color.getRGB());
        builder.addVertex(m, ax + aw, ay + ah, 0.0f).setColor(color.getRGB());
        builder.addVertex(m, ax + aw, ay, 0.0f).setColor(color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        matrices.popPose();
    }

    public static void drawRoundedRect(PoseStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color1, ColorRGBA color2, ColorRGBA color3, ColorRGBA color4) {
        matrices.pushPose();
        Matrix4f matrix4f = matrices.last().pose();
        float smoothness = 0.5f;
        gradientRectangleProgram.use();
        gradientRectangleProgram.findUniform("Size").set(width, height);
        gradientRectangleProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        gradientRectangleProgram.findUniform("Smoothness").set(smoothness);
        gradientRectangleProgram.findUniform("TopLeftColor").set(color1.getRed() / 255.0f, color1.getGreen() / 255.0f, color1.getBlue() / 255.0f, color1.getAlpha() / 255.0f);
        gradientRectangleProgram.findUniform("BottomLeftColor").set(color2.getRed() / 255.0f, color2.getGreen() / 255.0f, color2.getBlue() / 255.0f, color2.getAlpha() / 255.0f);
        gradientRectangleProgram.findUniform("BottomRightColor").set(color3.getRed() / 255.0f, color3.getGreen() / 255.0f, color3.getBlue() / 255.0f, color3.getAlpha() / 255.0f);
        gradientRectangleProgram.findUniform("TopRightColor").set(color4.getRed() / 255.0f, color4.getGreen() / 255.0f, color4.getBlue() / 255.0f, color4.getAlpha() / 255.0f);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.addVertex(matrix4f, adjustedX, adjustedY, 0.0f).setColor(color1.getRGB());
        builder.addVertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).setColor(color2.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).setColor(color3.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).setColor(color4.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        matrices.popPose();
    }

    public static void drawRoundedRect(PoseStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, Gradient gradient) {
        DrawUtility.drawRoundedRect(matrices, x, y, width, height, borderRadius, gradient.getTopLeftColor(), gradient.getBottomLeftColor(), gradient.getBottomRightColor(), gradient.getTopRightColor());
    }

    public static void drawRoundedBorder(PoseStack matrices, float x, float y, float width, float height, float borderThickness, BorderRadius borderRadius, ColorRGBA borderColor) {
        matrices.pushPose();
        Matrix4f matrix4f = matrices.last().pose();
        float internalSmoothness = 0.5f;
        float externalSmoothness = 1.0f;
        borderProgram.use();
        borderProgram.findUniform("Size").set(width, height);
        borderProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        borderProgram.findUniform("Smoothness").set(internalSmoothness, externalSmoothness);
        borderProgram.findUniform("Thickness").set(borderThickness);
        DrawUtility.drawSetup();
        float horizontalPadding = -externalSmoothness / 2.0f + externalSmoothness * 2.0f;
        float verticalPadding = externalSmoothness / 2.0f + externalSmoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.addVertex(matrix4f, adjustedX, adjustedY, 0.0f).setColor(borderColor.getRGB());
        builder.addVertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).setColor(borderColor.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).setColor(borderColor.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).setColor(borderColor.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        matrices.popPose();
    }

    public static void drawTexture(PoseStack matrices, Identifier identifier, float x, float y, float width, float height, ColorRGBA textureColor) {
        Batching batching = Batching.getActive();
        if (batching instanceof IconBatching iconBatching) {
            BufferBuilder builder = iconBatching.getBuilder();
            Matrix4f matrix4f = matrices.last().pose();
            TextureBinder.bind(identifier);
            builder.addVertex(matrix4f, x, y, 0.0f).setUv(0.0f, 0.0f).setColor(textureColor.getRGB());
            builder.addVertex(matrix4f, x, y + height, 0.0f).setUv(0.0f, 1.0f).setColor(textureColor.getRGB());
            builder.addVertex(matrix4f, x + width, y + height, 0.0f).setUv(1.0f, 1.0f).setColor(textureColor.getRGB());
            builder.addVertex(matrix4f, x + width, y, 0.0f).setUv(1.0f, 0.0f).setColor(textureColor.getRGB());
            return;
        }
        matrices.pushPose();
        Matrix4f matrix4f = matrices.last().pose();
        GlProgram.usePositionTexColor();
        TextureBinder.bind(identifier);
        DrawUtility.drawSetup();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix4f, x, y, 0.0f).setUv(0.0f, 0.0f).setColor(textureColor.getRGB());
        builder.addVertex(matrix4f, x, y + height, 0.0f).setUv(0.0f, 1.0f).setColor(textureColor.getRGB());
        builder.addVertex(matrix4f, x + width, y + height, 0.0f).setUv(1.0f, 1.0f).setColor(textureColor.getRGB());
        builder.addVertex(matrix4f, x + width, y, 0.0f).setUv(1.0f, 0.0f).setColor(textureColor.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        TextureBinder.unbind();
        matrices.popPose();
    }

    public static void drawTexture(PoseStack matrices, Identifier identifier, float x, float y, float width, float height, float u1, float u2, float v1, float v2, ColorRGBA clor) {
        Batching batching = Batching.getActive();
        if (batching instanceof IconBatching iconBatching) {
            BufferBuilder builder = iconBatching.getBuilder();
            Matrix4f matrix4f = matrices.last().pose();
            int color = clor.getRGB();
            float x2 = x + width;
            float y2 = y + height;
            TextureBinder.bind(identifier);
            builder.addVertex(matrix4f, x, y, 0.0f).setUv(u1, v1).setColor(color);
            builder.addVertex(matrix4f, x, y2, 0.0f).setUv(u1, v2).setColor(color);
            builder.addVertex(matrix4f, x2, y2, 0.0f).setUv(u2, v2).setColor(color);
            builder.addVertex(matrix4f, x2, y, 0.0f).setUv(u2, v1).setColor(color);
            return;
        }
        matrices.pushPose();
        int color = clor.getRGB();
        Matrix4f matrix4f = matrices.last().pose();
        float x2 = x + width;
        float y2 = y + height;
        GlProgram.usePositionTexColor();
        TextureBinder.bind(identifier);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix4f, x, y, 0.0f).setUv(u1, v1).setColor(color);
        builder.addVertex(matrix4f, x, y2, 0.0f).setUv(u1, v2).setColor(color);
        builder.addVertex(matrix4f, x2, y2, 0.0f).setUv(u2, v2).setColor(color);
        builder.addVertex(matrix4f, x2, y, 0.0f).setUv(u2, v1).setColor(color);
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        TextureBinder.unbind();
        matrices.popPose();
    }

    public static void drawAnimationSprite(PoseStack matrices, PenisSprite sprite, float x, float y, float width, float height, ColorRGBA color) {
        if (sprite == null) {
            return;
        }
        DrawUtility.drawTexture(matrices, sprite.texture(), x, y, width, height, sprite.u1(), sprite.u2(), sprite.v1(), sprite.v2(), color);
    }

    public static void drawAnimationSprite(Matrix3x2fStack matrices, PenisSprite sprite, float x, float y, float width, float height, ColorRGBA color) {
        DrawUtility.drawAnimationSprite(copyStack(matrices), sprite, x, y, width, height, color);
    }

    public static void drawSprite(PoseStack matrices, CustomSprite sprite, float x, float y, float width, float height, ColorRGBA color) {
        DrawUtility.drawTexture(matrices, Rockstar.id(sprite.getTexture().getTexture()), x, y, width, height, sprite.x / sprite.getTexture().getWidth(), (sprite.x + sprite.getTexture().getStep()) / sprite.getTexture().getWidth(), 0.0f, 1.0f, color);
    }

    public static void drawSprite(Matrix3x2fStack matrices, CustomSprite sprite, float x, float y, float width, float height, ColorRGBA color) {
        DrawUtility.drawTexture(matrices, Rockstar.id(sprite.getTexture().getTexture()), x, y, width, height, sprite.x / sprite.getTexture().getWidth(), (sprite.x + sprite.getTexture().getStep()) / sprite.getTexture().getWidth(), 0.0f, 1.0f, color);
    }

    public static void drawRoundedTexture(PoseStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius) {
        DrawUtility.drawRoundedTexture(matrices, identifier, x, y, width, height, borderRadius, Colors.WHITE);
    }

    public static void drawRoundedTexture(PoseStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        matrices.pushPose();
        Matrix4f matrix4f = matrices.last().pose();
        float smoothness = 0.5f;
        roundedTextureProgram.use();
        TextureBinder.bind(identifier);
        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        roundedTextureProgram.findUniform("Smoothness").set(smoothness);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix4f, adjustedX, adjustedY, 0.0f).setUv(0.0f, 0.0f).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).setUv(0.0f, 1.0f).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).setUv(1.0f, 1.0f).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).setUv(1.0f, 0.0f).setColor(color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        TextureBinder.unbind();
        matrices.popPose();
    }

    public static void drawShadow(PoseStack matrices, float x, float y, float width, float height, float softness, BorderRadius borderRadius, ColorRGBA color) {
        matrices.pushPose();
        Matrix4f matrix4f = matrices.last().pose();
        Batching batching = Batching.getActive();
        if (batching instanceof ShadowBatching shadowBatching) {
            BufferBuilder builder = shadowBatching.getBuilder();
            matrix4f = matrices.last().pose();
            float horizontalPadding = -softness / 2.0f + softness * 2.0f;
            float verticalPadding = softness / 2.0f + softness;
            float adjustedX = x - horizontalPadding / 2.0f;
            float adjustedY = y - verticalPadding / 2.0f;
            float adjustedWidth = width + horizontalPadding;
            float adjustedHeight = height + verticalPadding;
            builder.addVertex(matrix4f, adjustedX, adjustedY, 0.0f).setColor(color.getRGB());
            builder.addVertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).setColor(color.getRGB());
            builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).setColor(color.getRGB());
            builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).setColor(color.getRGB());
            matrices.popPose();
            return;
        }
        rectangleProgram.use();
        rectangleProgram.findUniform("Size").set(width, height);
        rectangleProgram.findUniform("Radius").set(borderRadius.topLeftRadius() * 3.0f, borderRadius.bottomLeftRadius() * 3.0f, borderRadius.topRightRadius() * 3.0f, borderRadius.bottomRightRadius() * 3.0f);
        rectangleProgram.findUniform("Smoothness").set(softness);
        DrawUtility.drawSetup();
        float horizontalPadding = -softness / 2.0f + softness * 2.0f;
        float verticalPadding = softness / 2.0f + softness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        builder.addVertex(matrix4f, adjustedX, adjustedY, 0.0f).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).setColor(color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        matrices.popPose();
    }

    public static void drawBlur(PoseStack matrices, float x, float y, float width, float height, float blurRadius, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        float smoothness = 0.03f;
        blurRadius /= 22.5f;
        if (blurRadius <= 0.0f) {
            return;
        }
        matrices.pushPose();
        Matrix4f matrix4f = matrices.last().pose();
        blurProgram.setBlurOffset(2.0f);
        squircleTextureProgram.use();
        TextureBinder.bindBlurResult();
        squircleTextureProgram.findUniform("Size").set(width, height);
        squircleTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f, borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomRightRadius() * squirt / 2.0f);
        squircleTextureProgram.findUniform("Smoothness").set(0.1f);
        squircleTextureProgram.findUniform("CornerSmoothness").set(squirt);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        int screenWidth = mw.getGuiScaledWidth();
        int screenHeight = mw.getGuiScaledHeight();
        float u = adjustedX / (float)screenWidth;
        float v = ((float)screenHeight - adjustedY - adjustedHeight) / (float)screenHeight;
        float texWidth = adjustedWidth / (float)screenWidth;
        float texHeight = adjustedHeight / (float)screenHeight;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix4f, adjustedX, adjustedY, 0.0f).setUv(u, v + texHeight).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).setUv(u, v).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).setUv(u + texWidth, v).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).setUv(u + texWidth, v + texHeight).setColor(color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        TextureBinder.unbind();
        matrices.popPose();
    }

    public static void drawBlur(PoseStack matrices, float x, float y, float width, float height, float blurRadius, BorderRadius borderRadius, ColorRGBA color) {
        blurRadius /= 22.5f;
        if (blurRadius <= 0.0f) {
            return;
        }
        matrices.pushPose();
        Matrix4f matrix4f = matrices.last().pose();
        blurProgram.setBlurOffset(2.0f);
        roundedTextureProgram.use();
        TextureBinder.bindBlurResult();
        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        roundedTextureProgram.findUniform("Smoothness").set(0.01f);
        DrawUtility.drawSetup();
        int screenWidth = mw.getGuiScaledWidth();
        int screenHeight = mw.getGuiScaledHeight();
        float u = x / (float)screenWidth;
        float v = ((float)screenHeight - y - height) / (float)screenHeight;
        float texWidth = width / (float)screenWidth;
        float texHeight = height / (float)screenHeight;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix4f, x, y, 0.0f).setUv(u, v + texHeight).setColor(color.getRGB());
        builder.addVertex(matrix4f, x, y + height, 0.0f).setUv(u, v).setColor(color.getRGB());
        builder.addVertex(matrix4f, x + width, y + height, 0.0f).setUv(u + texWidth, v).setColor(color.getRGB());
        builder.addVertex(matrix4f, x + width, y, 0.0f).setUv(u + texWidth, v + texHeight).setColor(color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        TextureBinder.unbind();
        matrices.popPose();
    }

    public static void drawImage(PoseStack matrices, BufferBuilder builder, double x, double y, double z, double width, double height, ColorRGBA color) {
        Matrix4f matrix = matrices.last().pose();
        builder.addVertex(matrix, (float)x, (float)(y + height), (float)z).setUv(0.0f, 1.0f).setColor(color.getRGB());
        builder.addVertex(matrix, (float)(x + width), (float)(y + height), (float)z).setUv(1.0f, 1.0f).setColor(color.getRGB());
        builder.addVertex(matrix, (float)(x + width), (float)y, (float)z).setUv(1.0f, 0.0f).setColor(color.getRGB());
        builder.addVertex(matrix, (float)x, (float)y, (float)z).setUv(0.0f, 0.0f).setColor(color.getRGB());
    }

    public static void drawImage(PoseStack matrices, Identifier identifier, double x, double y, double z, double width, double height, ColorRGBA color) {
        TextureBinder.bind(identifier);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f matrix = matrices.last().pose();
        builder.addVertex(matrix, (float)x, (float)(y + height), (float)z).setUv(0.0f, 1.0f).setColor(color.getRGB());
        builder.addVertex(matrix, (float)(x + width), (float)(y + height), (float)z).setUv(1.0f, 1.0f).setColor(color.getRGB());
        builder.addVertex(matrix, (float)(x + width), (float)y, (float)z).setUv(1.0f, 0.0f).setColor(color.getRGB());
        builder.addVertex(matrix, (float)x, (float)y, (float)z).setUv(0.0f, 0.0f).setColor(color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
    }

    public static void drawPlayerHeadWithHat(PoseStack matrices, AbstractClientPlayer player, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        Identifier skinTexture = player.getSkin().body().texturePath();
        DrawUtility.drawPlayerHeadWithRoundedShader(matrices, skinTexture, x, y, size, borderRadius, color);
        DrawUtility.drawPlayerHatLayerWithRoundedShader(matrices, skinTexture, x, y, size, borderRadius, color);
    }

    public static <T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> void drawEntityHeadWithHat(PoseStack matrices, T entity, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer(entity);
        if (renderer instanceof LivingEntityRenderer) {
            LivingEntityRenderer renderer1 = (LivingEntityRenderer)renderer;
            LivingEntityRenderer livingRenderer = (LivingEntityRenderer)renderer;
            LivingEntityRenderState state = (LivingEntityRenderState)livingRenderer.createRenderState();
            Identifier skinTexture = livingRenderer.getTextureLocation(state);
            DrawUtility.drawPlayerHeadWithRoundedShader(matrices, skinTexture, x, y, size, borderRadius, color);
            DrawUtility.drawPlayerHatLayerWithRoundedShader(matrices, skinTexture, x, y, size, borderRadius, color);
        }
    }

    public static void drawPlayerHeadWithRoundedShader(PoseStack matrices, Identifier skinTexture, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawRoundedTextureWithUV(matrices, skinTexture, x, y, size, size, borderRadius, color, 0.125f, 0.125f, 0.25f, 0.25f);
    }

    private static void drawPlayerHatLayerWithRoundedShader(PoseStack matrices, Identifier skinTexture, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawRoundedTextureWithUV(matrices, skinTexture, x, y, size, size, borderRadius, color, 0.625f, 0.125f, 0.75f, 0.25f);
    }

    public static void drawRoundedTextureWithUV(PoseStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color, float u1, float v1, float u2, float v2) {
        matrices.pushPose();
        Matrix4f matrix4f = matrices.last().pose();
        float smoothness = 0.5f;
        roundedTextureProgram.use();
        TextureBinder.bind(identifier);
        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(borderRadius.topLeftRadius(), borderRadius.bottomLeftRadius(), borderRadius.topRightRadius(), borderRadius.bottomRightRadius());
        roundedTextureProgram.findUniform("Smoothness").set(smoothness);
        DrawUtility.drawSetup();
        float horizontalPadding = -smoothness / 2.0f + smoothness * 2.0f;
        float verticalPadding = smoothness / 2.0f + smoothness;
        float adjustedX = x - horizontalPadding / 2.0f;
        float adjustedY = y - verticalPadding / 2.0f;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix4f, adjustedX, adjustedY, 0.0f).setUv(u1, v1).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0f).setUv(u1, v2).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0f).setUv(u2, v2).setColor(color.getRGB());
        builder.addVertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0f).setUv(u2, v1).setColor(color.getRGB());
        MeshDrawHelper.drawBuilt(builder.build());
        DrawUtility.drawEnd();
        TextureBinder.unbind();
        matrices.popPose();
    }

    /**
     * Vertex order must match {@code rvertexcoord(gl_VertexID)} in rockstar GUI shaders (TL, BL, BR, TR).
     */
    private static void addGuiQuadVertices(
            BufferBuilder builder,
            Matrix4f matrix,
            float x,
            float y,
            float width,
            float height,
            int color
    ) {
        builder.addVertex(matrix, x, y, 0.0f).setColor(color);
        builder.addVertex(matrix, x, y + height, 0.0f).setColor(color);
        builder.addVertex(matrix, x + width, y + height, 0.0f).setColor(color);
        builder.addVertex(matrix, x + width, y, 0.0f).setColor(color);
    }

    public static void drawSetup() {
    }

    public static void drawEnd() {
    }

    public static void drawArcRing(Matrix3x2fStack matrices, float centerX, float centerY, float outerRadius, float innerRadius, float startRadians, float sweepRadians, ColorRGBA color) {
        DrawUtility.drawArcRing(copyStack(matrices), centerX, centerY, outerRadius, innerRadius, startRadians, sweepRadians, color);
    }

    public static void drawArcRing(PoseStack matrices, float centerX, float centerY, float outerRadius, float innerRadius, float startRadians, float sweepRadians, ColorRGBA color) {
        if (Math.abs(sweepRadians) < 0.001f || outerRadius <= innerRadius) {
            return;
        }
        matrices.pushPose();
        try {
            Matrix4f matrix = matrices.last().pose();
            GlProgram.usePositionColor();
            DrawUtility.drawSetup();
            int segments = Math.max(36, (int)(80.0f * Math.abs(sweepRadians) / ((float)Math.PI * 2.0f)));
            int rgb = color.getRGB();
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i < segments; ++i) {
                float a0 = startRadians + sweepRadians * (float)i / (float)segments;
                float a1 = startRadians + sweepRadians * (float)(i + 1) / (float)segments;
                float cos0 = (float)Math.cos(a0);
                float sin0 = (float)Math.sin(a0);
                float cos1 = (float)Math.cos(a1);
                float sin1 = (float)Math.sin(a1);
                float ox0 = centerX + cos0 * outerRadius;
                float oy0 = centerY + sin0 * outerRadius;
                float ox1 = centerX + cos1 * outerRadius;
                float oy1 = centerY + sin1 * outerRadius;
                float ix0 = centerX + cos0 * innerRadius;
                float iy0 = centerY + sin0 * innerRadius;
                float ix1 = centerX + cos1 * innerRadius;
                float iy1 = centerY + sin1 * innerRadius;
                builder.addVertex(matrix, ox0, oy0, 0.0f).setColor(rgb);
                builder.addVertex(matrix, ox1, oy1, 0.0f).setColor(rgb);
                builder.addVertex(matrix, ix1, iy1, 0.0f).setColor(rgb);
                builder.addVertex(matrix, ix0, iy0, 0.0f).setColor(rgb);
            }
            MeshDrawHelper.drawBuilt(builder.build());
            DrawUtility.drawEnd();
        } finally {
            matrices.popPose();
        }
    }

    @Generated
    private DrawUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static GlProgram getSquircleProgram() {
        return squircleProgram;
    }

    public static PoseStack copyStack(Matrix3x2fStack from) {
        PoseStack stack = new PoseStack();
        stack.last().pose().set(HudMatrices.toMatrix4f(from));
        return stack;
    }

    public static void drawSquircle(Matrix3x2fStack matrices, float x, float y, float width, float height, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawSquircle(copyStack(matrices), x, y, width, height, squirt, borderRadius, color);
    }

    public static void drawLiquidGlass(Matrix3x2fStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color, float globalAlpha, float fresnelPower, ColorRGBA fresnelColor, float baseAlpha, boolean fresnelInvert, float fresnelMix, float distortStrength, float squirt, boolean clean) {
        DrawUtility.drawLiquidGlass(copyStack(matrices), x, y, width, height, borderRadius, color, globalAlpha, fresnelPower, fresnelColor, baseAlpha, fresnelInvert, fresnelMix, distortStrength, squirt, clean);
    }

    public static void drawLoadingRect(Matrix3x2fStack matrices, float x, float y, float width, float height, float progress, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawLoadingRect(copyStack(matrices), x, y, width, height, progress, borderRadius, color);
    }

    public static void drawTexture(Matrix3x2fStack matrices, Identifier identifier, float x, float y, float width, float height, float u1, float u2, float v1, float v2, ColorRGBA color) {
        DrawUtility.drawTexture(copyStack(matrices), identifier, x, y, width, height, u1, u2, v1, v2, color);
    }

    public static void drawTexture(Matrix3x2fStack matrices, Identifier identifier, float x, float y, float width, float height, ColorRGBA textureColor) {
        DrawUtility.drawTexture(copyStack(matrices), identifier, x, y, width, height, textureColor);
    }

    public static void drawRoundedTexture(Matrix3x2fStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawRoundedTexture(copyStack(matrices), identifier, x, y, width, height, borderRadius, color);
    }

    public static void drawShadow(Matrix3x2fStack matrices, float x, float y, float width, float height, float softness, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawShadow(copyStack(matrices), x, y, width, height, softness, borderRadius, color);
    }

    public static void drawBlur(Matrix3x2fStack matrices, float x, float y, float width, float height, float blurRadius, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawBlur(copyStack(matrices), x, y, width, height, blurRadius, borderRadius, color);
    }

    public static void drawBlur(Matrix3x2fStack matrices, float x, float y, float width, float height, float blurRadius, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawBlur(copyStack(matrices), x, y, width, height, blurRadius, squirt, borderRadius, color);
    }

    public static void drawPlayerHeadWithHat(Matrix3x2fStack matrices, AbstractClientPlayer player, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawPlayerHeadWithHat(copyStack(matrices), player, x, y, size, borderRadius, color);
    }

    public static <T extends LivingEntity> void drawEntityHeadWithHat(Matrix3x2fStack matrices, T entity, float x, float y, float size, BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawEntityHeadWithHat(copyStack(matrices), entity, x, y, size, borderRadius, color);
    }

    static {
        buffer = new CustomRenderTarget(false);
    }

    record HeadUV(float u1, float v1, float uSize, float vSize) {
    }
}

