/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.network.AbstractClientPlayer
 *  net.minecraft.client.renderer.Lighting
 *  net.minecraft.client.renderer.OverlayTexture
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.item.ItemRenderState
 *  org.joml.Matrix3x2fStack
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.item.ItemDisplayContext
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.crash.ReportedException
 *  net.minecraft.util.crash.CrashReport
 *  net.minecraft.util.crash.CrashReportCategory
 *  net.minecraft.util.math.Vec2
 *  net.minecraft.world.Level
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.framework.base;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.MsdfRenderer;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.gradient.Gradient;
import moscow.rockstar.mixin.accessors.DrawContextAccessor;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.HudMatrices;
import moscow.rockstar.utility.render.obj.CustomSprite;
import moscow.rockstar.utility.render.obj.Rect;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.ReportedException;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CustomDrawContext
        extends GuiGraphicsExtractor
        implements IMinecraft {
    private final GuiGraphicsExtractor originalContext;

    protected CustomDrawContext(GuiGraphicsExtractor originalContext) {
        super(((DrawContextAccessor)originalContext).getClient(), ((DrawContextAccessor)originalContext).getGuiRenderState(), ((DrawContextAccessor)originalContext).getMouseX(), ((DrawContextAccessor)originalContext).getMouseY());
        this.originalContext = originalContext;
    }

    public static CustomDrawContext of(GuiGraphicsExtractor originalContext) {
        return new CustomDrawContext(originalContext);
    }

    public void drawClientRect(float x, float y, float width, float height, float alpha, float dragAnim,
            float squircle) {
        if (Interface.showMinimalizm()) {
            this.drawBlurredRect(x, y, width, height, 45.0f, squircle, BorderRadius.all(6.0f),
                    ColorRGBA.WHITE.withAlpha(255.0f * alpha * Interface.minimalizm()));
        }
        if (Interface.showGlass()) {
            this.drawLiquidGlass(x, y, width, height, squircle, 0.08f - 0.07f * dragAnim, BorderRadius.all(6.0f),
                    ColorRGBA.WHITE.withAlpha(255.0f * alpha * Interface.glass()));
        }
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        this.drawSquircle(x, y, width, height, squircle, BorderRadius.all(6.0f),
                Colors.getBackgroundColor().withAlpha(255.0f * (dark ? 0.8f - 0.6f * Interface.glass() : 0.7f)));
    }

    @Override
    public Matrix3x2fStack pose() {
        return this.originalContext.pose();
    }

    public Matrix3x2fStack getMatrices() {
        return this.originalContext.pose();
    }

    public void pushMatrix() {
        this.getMatrices().pushMatrix();
    }

    public void popMatrix() {
        this.getMatrices().popMatrix();
    }

    public void drawRect(float x, float y, float width, float height, ColorRGBA color) {
        DrawUtility.drawRect(this.pose(), x, y, width, height, color);
    }

    public void drawLine(Vec2 from, Vec2 to, ColorRGBA color) {
        DrawUtility.drawLine(copyStack(this.pose()), from, to, color);
    }

    public void drawBezier(Vec2 p0, Vec2 p1, Vec2 p2, Vec2 p3, ColorRGBA color, int resolution) {
        DrawUtility.drawBezier(copyStack(this.pose()), p0, p1, p2, p3, color, resolution);
    }

    public void drawSquircle(float x, float y, float width, float height, float squirt, BorderRadius borderRadius,
            ColorRGBA color) {
        DrawUtility.drawSquircle(this.pose(), x, y, width, height, squirt, borderRadius, color);
    }

    public void drawRoundedRect(float x, float y, float width, float height, BorderRadius borderRadius,
            ColorRGBA color) {
        DrawUtility.drawRoundedRect(copyStack(this.pose()), x, y, width, height, borderRadius, color);
    }

    public void drawRoundedRect(float x, float y, float width, float height, BorderRadius borderRadius,
            Gradient gradient) {
        DrawUtility.drawRoundedRect(copyStack(this.pose()), x, y, width, height, borderRadius, gradient);
    }

    public void drawLiquidGlass(float x, float y, float width, float height, float squirt, float power,
            BorderRadius borderRadius, ColorRGBA color) {
        borderRadius = new BorderRadius(borderRadius.topLeftRadius() * squirt / 2.0f,
                borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f,
                borderRadius.bottomRightRadius() * squirt / 2.0f);
        DrawUtility.drawLiquidGlass(this.pose(), x - 5.0f * Interface.minimalizm(),
                y - 5.0f * Interface.minimalizm(), width + 10.0f * Interface.minimalizm(),
                height + 10.0f * Interface.minimalizm(), borderRadius, color,
                color.getAlpha() / 255.0f * Interface.glass(),
                (float) (height == 240.0f ? 100 : 50) * Interface.glass(), color.withAlpha(255.0f), 1.0f, true, 0.0f,
                power * Interface.glass(), squirt, false);
    }

    public void drawLiquidGlass(float x, float y, float width, float height, float squirt, BorderRadius borderRadius,
            ColorRGBA color, boolean clean) {
        borderRadius = new BorderRadius(borderRadius.topLeftRadius() * squirt / 2.0f,
                borderRadius.topRightRadius() * squirt / 2.0f, borderRadius.bottomLeftRadius() * squirt / 2.0f,
                borderRadius.bottomRightRadius() * squirt / 2.0f);
        DrawUtility.drawLiquidGlass(this.pose(), x, y, width, height, borderRadius, color,
                color.getAlpha() / 255.0f, height == 240.0f ? 100.0f : 50.0f, color.withAlpha(255.0f), 1.0f, true, 0.0f,
                0.08f, squirt, clean);
    }

    public void drawLoadingRect(float x, float y, float width, float height, float progress, BorderRadius borderRadius,
            ColorRGBA color) {
        DrawUtility.drawLoadingRect(this.pose(), x, y, width, height, progress, borderRadius, color);
    }

    public void drawRoundedBorder(float x, float y, float width, float height, float borderThickness,
            BorderRadius borderRadius, ColorRGBA borderColor) {
        DrawUtility.drawRoundedBorder(copyStack(this.pose()), x, y, width, height, borderThickness, borderRadius,
                borderColor);
    }

    public void drawTexture(Identifier identifier, Rect rect) {
        this.drawTexture(identifier, rect.x, rect.y, rect.width, rect.height, Colors.WHITE);
    }

    public void drawTexture(Identifier identifier, Rect rect, ColorRGBA color) {
        this.drawTexture(identifier, rect.x, rect.y, rect.width, rect.height, color);
    }

    public void drawTexture(Identifier identifier, float x, float y, float width, float height) {
        this.drawTexture(identifier, x, y, width, height, Colors.WHITE);
    }

    public void drawTexture(Identifier identifier, float x, float y, float width, float height, float u1, float u2,
            float v1, float v2, ColorRGBA color) {
        DrawUtility.drawTexture(this.pose(), identifier, x, y, width, height, u1, u2, v1, v2, color);
    }

    public void drawTexture(Identifier identifier, float x, float y, float width, float height,
            ColorRGBA textureColor) {
        DrawUtility.drawTexture(this.pose(), identifier, x, y, width, height, textureColor);
    }

    public void drawSprite(CustomSprite sprite, float x, float y, float width, float height, ColorRGBA textureColor) {
        DrawUtility.drawSprite(this.pose(), sprite, x, y, width, height, textureColor);
    }

    private static final PoseStack CACHED_POSE_STACK = new PoseStack();
    private static final Matrix4f CACHED_MATRIX = new Matrix4f();

    private static PoseStack copyStack(Matrix3x2fStack from) {
        CACHED_POSE_STACK.last().pose().set(HudMatrices.toMatrix4f(from, CACHED_MATRIX));
        return CACHED_POSE_STACK;
    }

    public void drawRoundedTexture(Identifier identifier, float x, float y, float width, float height,
            BorderRadius borderRadius) {
        DrawUtility.drawRoundedTexture(this.pose(), identifier, x, y, width, height, borderRadius, ColorRGBA.WHITE);
    }

    public void drawRoundedTexture(Identifier identifier, float x, float y, float width, float height,
            BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawRoundedTexture(this.pose(), identifier, x, y, width, height, borderRadius, color);
    }

    public void drawShadow(float x, float y, float width, float height, float softness, BorderRadius borderRadius,
            ColorRGBA color) {
        DrawUtility.drawShadow(this.pose(), x, y, width, height, softness, borderRadius, color);
    }

    public void drawBlurredRect(float x, float y, float width, float height, float blurRadius,
            BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawBlur(this.pose(), x, y, width, height, blurRadius, borderRadius, color);
    }

    public void drawBlurredRect(float x, float y, float width, float height, float blurRadius, float squirt,
            BorderRadius borderRadius, ColorRGBA color) {
        DrawUtility.drawBlur(this.pose(), x, y, width, height, blurRadius, squirt, borderRadius, color);
    }

    public void drawText(Font font, String text, float x, float y, ColorRGBA color) {
        MsdfRenderer.renderText(font.getFont(), text, font.getSize(), color.getRGB(), this.textMatrix(), x, y, 0.0f);
    }

    public void drawText(Font font, Component text, float x, float y) {
        MsdfRenderer.renderText(font.getFont(), text, font.getSize(), this.textMatrix(), x, y, 0.0f);
    }

    public void drawFadeoutText(Font font, String text, float x, float y, ColorRGBA color, float fadeoutStart,
            float fadeoutEnd) {
        MsdfRenderer.renderText(font.getFont(), text, font.getSize(), color.getRGB(), this.textMatrix(), x, y, 0.0f,
                true, fadeoutStart, fadeoutEnd);
    }

    public void drawFadeoutText(Font font, String text, float x, float y, ColorRGBA color, float fadeoutStart,
            float fadeoutEnd, float maxWidth) {
        MsdfRenderer.renderText(font.getFont(), text, font.getSize(), color.getRGB(), this.textMatrix(), x, y, 0.0f,
                true, fadeoutStart, fadeoutEnd, maxWidth);
    }

    public void drawCenteredText(Font font, String text, float x, float y, ColorRGBA color) {
        this.drawText(font, text, x - font.width(text) / 2.0f, y, color);
    }

    public void drawRightText(Font font, String text, float x, float y, ColorRGBA color) {
        this.drawText(font, text, x - font.width(text), y, color);
    }

    private Matrix4f textMatrix() {
        return HudMatrices.toMatrix4f(this.getMatrices());
    }

    public void drawItem(Item item, float x, float y, float size) {
        this.item(new ItemStack(item), x, y, size);
    }

    public void drawBatchItem(ItemStack stack, int x, int y, int z) {
        this.item(stack, x, y, 1.0f);
    }

    public void drawItem(ItemStack item, float x, float y, float size) {
        this.item(item, x, y, size);
    }

    public void item(ItemStack stack, float x, float y, float scale) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        try {
            var renderer = moscow.rockstar.utility.render.GuiDrawContextHolder.getGuiRenderer();
            var player = Minecraft.getInstance().player;
            if (renderer != null && player != null) {
                var atlas = ((moscow.rockstar.mixin.accessors.GuiRendererAccessor) renderer).rockstar$getItemAtlas();
                if (atlas != null) {
                    var cleanStack = stack;
                    var state = new net.minecraft.client.renderer.item.TrackingItemStackRenderState();
                    Minecraft.getInstance().getItemModelResolver().updateForLiving(state, cleanStack, net.minecraft.world.item.ItemDisplayContext.GUI, player);
                    var slot = atlas.getOrUpdate(state);
                    if (slot != null && slot.textureView() != null) {
                        try {
                            // GuiItemAtlas uses framebuffer-space UVs, so v is flipped:
                            // v0 = bottom of slot in atlas, v1 = top of slot in atlas
                            float u1 = slot.u0();
                            float u2 = slot.u1();
                            float vTop = slot.v0();
                            float vBot = slot.v1();

                            float size = 16.0f * scale;

                            moscow.rockstar.utility.render.TextureBinder.lastBinding = new moscow.rockstar.utility.render.TextureBinder.GpuBinding(
                                slot.textureView(),
                                com.mojang.blaze3d.systems.RenderSystem.getSamplerCache().getClampToEdge(com.mojang.blaze3d.textures.FilterMode.LINEAR)
                            );

                            // Build PoseStack from current HUD matrix (no extra translate - use absolute coords)
                            com.mojang.blaze3d.vertex.PoseStack poseStack = copyStack(this.pose());
                            int color = moscow.rockstar.utility.colors.Colors.WHITE.getRGB();
                            float shaderAlpha = moscow.rockstar.utility.render.ShaderColorHelper.getAlpha();
                            if (shaderAlpha < 1.0f) {
                                int a = (int) (((color >> 24) & 0xFF) * shaderAlpha);
                                color = (color & 0x00FFFFFF) | (a << 24);
                            }
                            org.joml.Matrix4f matrix4f = poseStack.last().pose();
                            moscow.rockstar.framework.shader.GlProgram.usePositionTexColor();
                            com.mojang.blaze3d.vertex.BufferBuilder builder = com.mojang.blaze3d.vertex.Tesselator.getInstance().begin(
                                com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                                com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR
                            );
                            // TL, BL, BR, TR  (screen space: y increases downward)
                            builder.addVertex(matrix4f, x,        y,        0.0f).setUv(u1, vTop).setColor(color);
                            builder.addVertex(matrix4f, x,        y + size, 0.0f).setUv(u1, vBot).setColor(color);
                            builder.addVertex(matrix4f, x + size, y + size, 0.0f).setUv(u2, vBot).setColor(color);
                            builder.addVertex(matrix4f, x + size, y,        0.0f).setUv(u2, vTop).setColor(color);
                            moscow.rockstar.utility.render.MeshDrawHelper.drawBuilt(builder.build());
                            moscow.rockstar.utility.render.TextureBinder.unbind();
                        } catch (Throwable ex) {
                            renderItemFallback(stack, x, y, scale);
                        }
                        return;
                    }

                }
            }
            renderItemFallback(stack, x, y, scale);
        } catch (Throwable t) {
            renderItemFallback(stack, x, y, scale);
        }
    }

    private void renderItemFallback(ItemStack stack, float x, float y, float scale) {
        // Safe fallback - do nothing to prevent next-frame queueing and hotbar flickering.
    }

    public void item(net.minecraft.world.item.Item item, float x, float y, float scale) {
        this.item(new ItemStack(item), x, y, scale);
    }

    public void drawHead(AbstractClientPlayer player, float x, float y, float size, BorderRadius borderRadius,
            ColorRGBA color) {
        DrawUtility.drawPlayerHeadWithHat(this.pose(), player, x, y, size, borderRadius, color);
    }

    public void drawHead(LivingEntity entity, float x, float y, float size, BorderRadius borderRadius,
            ColorRGBA color) {
        DrawUtility.drawEntityHeadWithHat(this.pose(), entity, x, y, size, borderRadius, color);
    }

    public void drawBatchItem(ItemStack item, int x, int y) {
        this.item(item, (float) x, (float) y, 1.0f);
    }

    private void drawBatchItem(@Nullable LivingEntity entity, @Nullable Level world, ItemStack stack, int x, int y,
            int seed) {
        this.item(stack, (float) x, (float) y, 1.0f);
    }

    private void drawBatchItem(@Nullable LivingEntity entity, @Nullable Level world, ItemStack stack, int x, int y,
            int seed, int z) {
        this.item(stack, (float) x, (float) y, 1.0f);
    }
}
