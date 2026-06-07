package moscow.rockstar.ui.hud.impl;

import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.ui.components.animated.AnimatedNumber;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.ScissorUtility;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrapTimeHud extends HudElement {
    private static final float BAR_HEIGHT = 18.0f;
    private static final float FONT_SIZE = 8.0f;
    private static final float ICON_PADDING = 5.0f;
    private static final float ICON_SIZE = 8.0f;
    private static final float ICON_SCALE = 0.55f;
    private static final float TEXT_PAD_LEFT = 18.0f;
    private static final long PREVIEW_TOGGLE_MS = 2000L;

    private final Animation contentAnim = new Animation(250L, 0.0f, Easing.BAKEK);
    private final AnimatedNumber secondsAnim = new AnimatedNumber(Fonts.MEDIUM.getFont(FONT_SIZE), 6.0f, 300L, Easing.BAKEK);

    private String prefix = "";
    private int seconds;

    public TrapTimeHud() {
        super("hud.traptime", "icons/hud/traptimer.png");
        this.showing = true;
        this.width = 120.0f;
        this.height = BAR_HEIGHT;
    }

    public void clear() {
        this.prefix = "";
        this.seconds = 0;
    }

    public void showTrap(long remainedMs, long totalMs) {
        this.prefix = "До конца трапки осталось ";
        this.seconds = (int)Math.max(0L, remainedMs / 1000L);
    }

    public void showDragon(long remainedMs, long totalMs) {
        this.prefix = "До конца драконки ";
        this.seconds = (int)Math.max(0L, remainedMs / 1000L);
    }

    /** Превью только в чате, когда нет активной трапки/драконки */
    private boolean isPreview() {
        return TrapTimeHud.mc.screen instanceof ChatScreen && this.prefix.isEmpty();
    }

    private String previewTimeSuffix() {
        return System.currentTimeMillis() / PREVIEW_TOGGLE_MS % 2L == 0L ? "30s" : "x";
    }

    private float textY(Font font) {
        return this.y + (BAR_HEIGHT - font.height()) / 2.0f + 0.5f;
    }

    @Override
    public void update(UIContext context) {
        boolean active = this.isPreview() || !this.prefix.isEmpty();
        this.contentAnim.update(active ? 1.0f : 0.0f);
        Font font = Fonts.MEDIUM.getFont(FONT_SIZE);
        if (this.isPreview()) {
            String label = "Trapprewiew";
            this.width = TEXT_PAD_LEFT + font.width(label) + font.width("30s") + 10.0f;
        } else {
            this.width = TEXT_PAD_LEFT + font.width(this.prefix) + this.secondsAnim.getWidth() + font.width("s") + 8.0f;
        }
        this.height = BAR_HEIGHT;
        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (this.contentAnim.getValue() <= 0.01f) {
            return;
        }
        float alpha = this.contentAnim.getValue();
        boolean preview = this.isPreview();
        float panelAnim = this.animation.getValue() * alpha;

        context.drawClientRect(this.x, this.y, this.width, this.height, panelAnim, this.dragAnim.getValue(), 3.0f);
        ScissorUtility.push(context.pose(), this.x, this.y, this.width, this.height);

        float itemDrawSize = 16.0f * ICON_SCALE;
        float iconX = this.x + ICON_PADDING + (ICON_SIZE - itemDrawSize) / 2.0f;
        float iconY = this.y + ICON_PADDING + (ICON_SIZE - itemDrawSize) / 2.0f;
        float prevAlpha = moscow.rockstar.utility.render.ShaderColorHelper.getAlpha();
        moscow.rockstar.utility.render.ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha * alpha);
        context.item(Items.NETHERITE_SCRAP.getDefaultInstance(), iconX, iconY, ICON_SCALE);
        moscow.rockstar.utility.render.ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha);

        Font font = Fonts.MEDIUM.getFont(FONT_SIZE);
        float textX = this.x + TEXT_PAD_LEFT;
        float textY = this.textY(font);
        ColorRGBA textColor = Colors.getTextColor().withAlpha(255.0f * alpha);

        if (preview) {
            String label = "Trapprewiew";
            String time = this.previewTimeSuffix();
            context.drawText(font, label, textX, textY, textColor);
            context.drawText(font, time, textX + font.width(label) + 3.0f, textY, textColor);
        } else {
            context.drawText(font, this.prefix, textX, textY, textColor);
            float numberX = textX + font.width(this.prefix);
            this.secondsAnim.settings(true, textColor);
            this.secondsAnim.update(this.seconds);
            this.secondsAnim.pos(numberX, textY);
            this.secondsAnim.render(context);
            context.drawText(font, "s", numberX + this.secondsAnim.getWidth(), textY, textColor);
        }

        ScissorUtility.pop();
    }

    @Override
    public boolean show() {
        if (this.isPreview()) {
            return true;
        }
        return !this.prefix.isEmpty();
    }
}
