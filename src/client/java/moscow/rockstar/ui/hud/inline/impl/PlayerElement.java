/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.hud.inline.impl;

import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.hud.inline.InlineElement;
import moscow.rockstar.ui.hud.inline.InlineValue;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;

public class PlayerElement
extends InlineElement {
    private final InlineValue fps;
    private final InlineValue speed;
    private final BooleanSetting ySpeed;
    private final Animation animation;

    public PlayerElement() {
        super("hud.player", "icons/hud/player.png");
        this.fps = new InlineValue(this.elements, "FPS", "FPS");
        this.speed = new InlineValue(this.elements, "speed", "BPS");
        this.ySpeed = new BooleanSetting(this, "hud.player.speedY").enable();
        this.animation = new Animation(300L, 0.0f, Easing.SMOOTH_STEP);
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        this.fps.update("" + Math.round(this.animation.update(mc.getFps())));
        if (PlayerElement.mc.player == null) {
            return;
        }
        double motion = !this.ySpeed.isEnabled() ? Math.hypot(PlayerElement.mc.player.getX() - PlayerElement.mc.player.xo, PlayerElement.mc.player.getZ() - PlayerElement.mc.player.zo) : Math.hypot(PlayerElement.mc.player.getY() - PlayerElement.mc.player.yo, Math.hypot(PlayerElement.mc.player.getX() - PlayerElement.mc.player.xo, PlayerElement.mc.player.getZ() - PlayerElement.mc.player.zo));
        this.speed.update(String.format("%.2f", motion * 20.0).replace(",", "."));
    }
}

