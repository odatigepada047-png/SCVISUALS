package moscow.rockstar.systems.theme;

import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;

public class CustomTheme {
    private String name;
    private ColorRGBA accentColor;
    private final Animation animation = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation hoverAnimation = new Animation(200L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private boolean showing = true;

    public CustomTheme(String name, ColorRGBA accentColor) {
        this.name = name;
        this.accentColor = accentColor;
    }

    public void updateAnimation() {
        this.animation.update(this.showing);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColorRGBA getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(ColorRGBA accentColor) {
        this.accentColor = accentColor;
    }

    public Animation getAnimation() {
        return animation;
    }

    public Animation getHoverAnimation() {
        return hoverAnimation;
    }

    public boolean isShowing() {
        return showing;
    }

    public void setShowing(boolean showing) {
        this.showing = showing;
    }
}
