/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.utility.colors;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.animation.types.ColorAnimation;
import moscow.rockstar.utility.colors.ColorRGBA;

public final class Colors {
    public static final ColorRGBA RED = new ColorRGBA(255.0f, 0.0f, 0.0f);
    public static final ColorRGBA GREEN = new ColorRGBA(0.0f, 255.0f, 0.0f);
    public static final ColorRGBA BLUE = new ColorRGBA(0.0f, 0.0f, 255.0f);
    public static final ColorRGBA WHITE = new ColorRGBA(255.0f, 255.0f, 255.0f);
    public static final ColorRGBA BLACK = new ColorRGBA(0.0f, 0.0f, 0.0f);
    public static final ColorRGBA ACCENT = new ColorRGBA(151.0f, 71.0f, 255.0f);
    private static final long ANIMATION_DURATION = 500L;
    private static final ColorAnimation BACKGROUND_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation ADDITIONAL_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation TEXT_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation OUTLINE_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation FLAT_COLOR_ANIMATION = new ColorAnimation(500L);
    private static final ColorAnimation ACCENT_COLOR_ANIMATION = new ColorAnimation(500L, ACCENT);

    private static Theme getTheme() {
        return Rockstar.getInstance().getThemeManager().getCurrentTheme();
    }

    public static ColorRGBA getBackgroundColor() {
        return Colors.getAnimatedColor(BACKGROUND_COLOR_ANIMATION, Colors.getTheme().getBackgroundColor());
    }

    public static ColorRGBA getAdditionalColor() {
        return Colors.getAnimatedColor(ADDITIONAL_COLOR_ANIMATION, Colors.getTheme().getAdditionalColor());
    }

    public static ColorRGBA getTextColor() {
        return Colors.getAnimatedColor(TEXT_COLOR_ANIMATION, Colors.getTheme().getTextColor());
    }

    public static ColorRGBA getOutlineColor() {
        return Colors.getAnimatedColor(OUTLINE_COLOR_ANIMATION, Colors.getTheme().getOutlineColor());
    }

    public static ColorRGBA getFlatColor() {
        return Colors.getAnimatedColor(FLAT_COLOR_ANIMATION, Colors.getTheme().getFlatColor());
    }

    public static ColorRGBA getAccentColor() {
        return Colors.getAnimatedColor(ACCENT_COLOR_ANIMATION, Rockstar.getInstance().getThemeManager().getAccentColor());
    }

    public static ColorRGBA getSeparatorColor() {
        return ColorRGBA.BLACK.withAlpha(255.0f * (Colors.getTheme() == Theme.DARK ? 0.08f : 0.05f));
    }

    public static ColorRGBA getHudIconColor(float alpha) {
        float minimalism = Interface.minimalizm();
        ColorRGBA iconColor = Colors.getTextColor().mix(Colors.getAccentColor(), minimalism);
        return iconColor.withAlpha(alpha);
    }

    private static ColorRGBA getAnimatedColor(ColorAnimation animation, ColorRGBA color) {
        animation.update(color);
        return animation.getColor();
    }

    @Generated
    private Colors() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
