package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.constructions.crosshair.CrosshairEditorScreen;
import moscow.rockstar.systems.modules.constructions.crosshair.CrosshairManager;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ButtonSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Vanilla crosshair is cancelled in {@link moscow.rockstar.mixin.minecraft.client.gui.overlay.InGameHudCrosshairMixin};
 * geometry is queued via {@link GuiGraphicsExtractor} during {@code extractCrosshair}.
 */
@ModuleInfo(name = "Custom Crosshair", category = ModuleCategory.VISUALS, desc = "modules.descriptions.custom_crosshair")
public class CustomCrosshair extends BaseModule {
    private final ButtonSetting openEditor = new ButtonSetting(this, "modules.settings.custom_crosshair.open_editor")
            .action(() -> mc.setScreen((Screen) new CrosshairEditorScreen()));

    private final ModeSetting type = new ModeSetting(this, "modules.settings.custom_crosshair.type");
    private final ModeSetting.Value staticType = new ModeSetting.Value(type, "modules.settings.custom_crosshair.type.static").select();
    private final ModeSetting.Value dynamicType = new ModeSetting.Value(type, "modules.settings.custom_crosshair.type.dynamic");

    private final ColorSetting color = new ColorSetting(this, "modules.settings.custom_crosshair.color")
            .color(new ColorRGBA(255, 255, 255, 255))
            .alpha(true);

    private final BooleanSetting redOnTarget = new BooleanSetting(this, "modules.settings.custom_crosshair.red_on_target")
            .enable();

    private final ColorSetting targetColor = new ColorSetting(this, "modules.settings.custom_crosshair.target_color")
            .color(new ColorRGBA(255, 50, 50, 255))
            .alpha(true);

    @Override
    public void onEnable() {
        super.onEnable();
        updateManagerFromSettings();
    }

    private void updateManagerFromSettings() {
        CrosshairManager manager = Rockstar.getInstance().getCrosshairManager();
        manager.setColor(color.getColor());
        manager.setType(dynamicType.isSelected()
                ? CrosshairManager.CrosshairType.DYNAMIC
                : CrosshairManager.CrosshairType.STATIC);
    }

    private boolean isLookingAtEntity() {
        if (mc.hitResult == null || mc.player == null) {
            return false;
        }

        HitResult hitResult = mc.hitResult;
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            Entity entity = entityHit.getEntity();
            return entity instanceof LivingEntity;
        }

        return false;
    }

    /** Packed color for {@link GuiGraphicsExtractor#fill} — Mojang helper, не «сырой» {@link ColorRGBA#getRGB()}. */
    private static int packGuiFillColor(ColorRGBA c) {
        int a = Math.round(c.getAlpha());
        int r = Math.round(c.getRed());
        int g = Math.round(c.getGreen());
        int b = Math.round(c.getBlue());
        return ARGB.color(a, r, g, b);
    }

    /**
     * Один «пиксель» сетки в координатах scaled GUI (~как до порта на 1.21.4): не привязываем к целому guiScale целиком.
     */
    private float gridPixelSize() {
        float g = (float) mc.getWindow().getGuiScale();
        // Раньше использовался ~1 gui-пиксель на клетку; max(2, g) раздувало прицел в ~2 раза на типичных scale.
        return Math.max(1.0f, g * 0.5f);
    }

    /**
     * Called from mixin {@code Gui.extractCrosshair} after cancelling vanilla.
     */
    public void renderIntoExtractor(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        CrosshairManager manager = Rockstar.getInstance().getCrosshairManager();

        if (!manager.hasAnyPixels()) {
            manager.applyDefaultCrosshairPattern();
        }

        manager.setColor(color.getColor());
        manager.setType(dynamicType.isSelected()
                ? CrosshairManager.CrosshairType.DYNAMIC
                : CrosshairManager.CrosshairType.STATIC);

        float delta = tickCounter.getGameTimeDeltaPartialTick(false);
        manager.updateAnimation(delta);

        float centerX = mc.getWindow().getGuiScaledWidth() / 2.0f;
        float centerY = mc.getWindow().getGuiScaledHeight() / 2.0f;
        float pixelSize = gridPixelSize();

        float spread = 0;
        if (mc.player != null) {
            float velocity = (float) mc.player.getDeltaMovement().length();
            float maxSpread = Math.min(velocity * 2.0f, 5.0f);
            spread = maxSpread * manager.getAnimationProgress();
        }

        ColorRGBA renderColor = color.getColor();
        if (redOnTarget.isEnabled() && isLookingAtEntity()) {
            renderColor = targetColor.getColor();
        }

        int argb = packGuiFillColor(renderColor);
        int gridSize = manager.getGridSize();
        int gridCenter = gridSize / 2;

        for (int gx = 0; gx < gridSize; gx++) {
            for (int gy = 0; gy < gridSize; gy++) {
                if (!manager.getPixel(gx, gy)) {
                    continue;
                }

                float offsetX = (gx - gridCenter) * pixelSize;
                float offsetY = (gy - gridCenter) * pixelSize;

                if (spread > 0) {
                    float distFromCenter = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
                    if (distFromCenter > 0) {
                        float spreadFactor = spread / distFromCenter;
                        offsetX += offsetX * spreadFactor * 0.2f;
                        offsetY += offsetY * spreadFactor * 0.2f;
                    }
                }

                int ix = Math.round(centerX + offsetX);
                int iy = Math.round(centerY + offsetY);
                int ps = Math.max(1, Math.round(pixelSize));
                // CROSSHAIR требует UV в вершинах — fill() даёт только POSITION_COLOR → краш «Missing UV0».
                graphics.fill(RenderPipelines.GUI, ix, iy, ix + ps, iy + ps, argb);
            }
        }
    }
}
