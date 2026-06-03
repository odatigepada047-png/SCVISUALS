/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.menu.dropdown.components.settings.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.ui.menu.dropdown.components.settings.MenuSettingComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.time.Timer;

public class SliderSettingComponent
extends MenuSettingComponent<SliderSetting> {
    private final Animation animation = new Animation(500L, Easing.BAKEK_PAGES);
    private final Animation moving = new Animation(500L, Easing.FIGMA_EASE_IN_OUT);
    private final Timer timer = new Timer();
    private boolean drag;
    private static SliderSettingComponent current;

    public SliderSettingComponent(SliderSetting setting, CustomComponent parent) {
        super(setting, parent);
    }

    @Override
    protected void renderComponent(UIContext context) {
        float x = this.x + 9.0f;
        float y = this.y + 2.0f;
        float width = this.width - 18.0f;
        Font nameFont = Fonts.REGULAR.getFont(8.0f);
        float leftPadding = 10.0f;
        float nameHeight = Fonts.REGULAR.getFont(7.0f).height();
        float headerHeight = 19.0f;
        this.animation.update(((SliderSetting)this.setting).getCurrentValue());
        this.hoverAnimation.update(this.isHovered(context.getMouseX(), context.getMouseY()));
        Theme theme = Rockstar.getInstance().getThemeManager().getCurrentTheme();
        boolean dark = theme == Theme.DARK;
        float trackAlpha = (255.0f - 100.0f * Interface.glass()) * (dark ? 0.55f : 0.7f);
        ColorRGBA trackColor = Colors.getOutlineColor().withAlpha(trackAlpha);
        ColorRGBA fillColor = Colors.getAccentColor();
        ColorRGBA knobColor = Colors.getFlatColor();
        float percent = GuiUtility.getPercent(this.animation.getRGB(), ((SliderSetting)this.setting).getMin(), ((SliderSetting)this.setting).getMax());
        context.drawRoundedRect(x, y + this.height - 12.0f, width, 2.0f, BorderRadius.all(0.25f), trackColor);
        context.drawRoundedRect(x, y + this.height - 12.0f, width * percent, 2.0f, BorderRadius.all(0.25f), fillColor);
        if (this.timer.finished(1000L)) {
            DrawUtility.updateBuffer();
            this.timer.reset();
        }
        float knobX = x + width * percent;
        if (Interface.showGlass()) {
            context.drawShadow(knobX - 4.5f - 3.0f * this.moving.getRGB(), y + this.height - 11.0f - 3.0f - 2.0f * this.moving.getRGB(), 9.0f + 6.0f * this.moving.getRGB(), 6.0f + 4.0f * this.moving.getRGB(), 10.0f, BorderRadius.all(3.0f + this.moving.getRGB() * 2.0f), ColorRGBA.BLACK.withAlpha(255.0f * (0.25f + 0.2f * this.moving.getRGB()) * Interface.glass()));
            context.drawSquircle(knobX - 4.5f - 3.0f * this.moving.getRGB(), y + this.height - 11.0f - 3.0f - 2.0f * this.moving.getRGB(), 9.0f + 6.0f * this.moving.getRGB(), 6.0f + 4.0f * this.moving.getRGB(), 7.0f, BorderRadius.all(3.0f + this.moving.getRGB()), knobColor.withAlpha(255.0f * (1.0f - this.moving.getRGB()) * Interface.glass()));
            context.drawLiquidGlass(knobX - 4.5f - 3.0f * this.moving.getRGB(), y + this.height - 11.0f - 3.0f - 2.0f * this.moving.getRGB(), 9.0f + 6.0f * this.moving.getRGB(), 6.0f + 4.0f * this.moving.getRGB(), 7.0f, BorderRadius.all(3.0f + this.moving.getRGB()), fillColor.withAlpha(255.0f * this.moving.getRGB() * Interface.glass()), true);
        }
        if (Interface.showMinimalizm()) {
            context.drawShadow(knobX - 3.0f, y + this.height - 14.0f + this.moving.getRGB(), 6.0f, 6.0f - this.moving.getRGB() * 2.0f, 10.0f, BorderRadius.all(3.0f - this.moving.getRGB() * 2.0f), ColorRGBA.BLACK.withAlpha(63.75f * Interface.minimalizm()));
            context.drawRoundedRect(knobX - 3.0f, y + this.height - 14.0f + this.moving.getRGB(), 6.0f, 6.0f - this.moving.getRGB() * 2.0f, BorderRadius.all(3.0f - this.moving.getRGB() * 2.0f), knobColor.withAlpha(255.0f * Interface.minimalizm()));
        }
        String value = TextUtility.formatNumberClean(this.animation.getRGB()) + ((SliderSetting)this.setting).getSuffix();
        context.drawFadeoutText(nameFont, Localizator.translate(((SliderSetting)this.setting).getName()), this.x + leftPadding, y + 11.0f - nameFont.height(), Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getRGB())), 0.8f, 1.0f, this.getParent().getWidth() - leftPadding - Fonts.REGULAR.getFont(7.0f).width(value) - 10.0f);
        context.drawRightText(Fonts.REGULAR.getFont(7.0f), value, x + width, y + 11.0f - nameHeight, Colors.getTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.hoverAnimation.getRGB())));
        if (this.isHovered(context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
        this.moving.setDuration(200L);
        this.moving.update(this.drag ? 1.0f : 0.0f);
        if (this.drag) {
            float xValue = GuiUtility.getSliderValue(((SliderSetting)this.setting).getMin(), ((SliderSetting)this.setting).getMax(), x, width, context.getMouseX());
            ((SliderSetting)this.setting).setCurrentValue(xValue);
            CursorUtility.set(CursorType.ARROW_HORIZONTAL);
            current = this;
        }
    }

    @Override
    public void drawSplit(UIContext context) {
        float separatorHeight = 0.5f;
        context.drawRect(this.x, this.y + this.height, this.width, separatorHeight, Colors.getTextColor().withAlpha(5.1f));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.isHovered(mouseX, mouseY)) {
            this.drag = true;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.drag = false;
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 262 || keyCode == 263) && current == this) {
            ((SliderSetting)current.getSetting()).setCurrentValue(((SliderSetting)current.getSetting()).getCurrentValue() + ((SliderSetting)current.getSetting()).getStep() * 0.7f * (float)(keyCode == 262 ? 1 : -1));
        }
    }

    @Override
    public float getHeight() {
        this.height = 29.0f;
        return 29.0f;
    }
}


