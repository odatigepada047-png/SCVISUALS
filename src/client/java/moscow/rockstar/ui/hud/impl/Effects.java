/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.renderer.DefaultVertexFormat
 *  net.minecraft.client.texture.TextureAtlasSprite
 *  net.minecraft.entity.effect.MobEffect
 *  net.minecraft.entity.effect.StatusEffectCategory
 *  net.minecraft.entity.effect.MobEffectInstance
 */
package moscow.rockstar.ui.hud.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.components.animated.AnimatedNumber;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.mixins.StatusEffectInstanceAddition;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import moscow.rockstar.utility.render.batching.impl.RectBatching;
import net.minecraft.client.gui.screens.ChatScreen;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

public class Effects
extends HudList {
    private final BooleanSetting alwaysDisplay = new BooleanSetting(this, "hud.always_display");
    int lastSize = -1;
    private final Map<String, MobEffectInstance> effects = new TreeMap<String, MobEffectInstance>();
    private final Map<MobEffect, Boolean> ended = new HashMap<MobEffect, Boolean>();
    private final BooleanSetting alert = new BooleanSetting(this, "hud.effects.alert");

    public Effects() {
        super("hud.effects", "icons/hud/potion.png");
    }

    @Override
    public void update(UIContext context) {
        this.width = 92.0f;
        this.height = 18.0f;
        if (Effects.mc.player == null || Effects.mc.level == null) {
            super.update(context);
            return;
        }
        Collection<MobEffectInstance> original = Effects.mc.player.getActiveEffects();
        for (MobEffectInstance eff : original) {
            MobEffect potion = (MobEffect)eff.getEffect().value();
            String realName = potion.getDisplayName().getString();
            if (realName == null || ServerUtility.isCM()) continue;
            if (this.effects.containsKey(realName)) {
                this.effects.replace(realName, eff);
                Animation anim = ((StatusEffectInstanceAddition)eff).rockstar$getAnimPotion();
                if (anim.getRGB() != 0.0f) continue;
                anim.setValue(1.0f);
                continue;
            }
            this.effects.put(realName, eff);
        }
        if (!this.effects.isEmpty()) {
            this.height += 5.0f;
        }
        for (MobEffectInstance eff : this.effects.values()) {
            Animation anim = ((StatusEffectInstanceAddition)eff).rockstar$getAnimPotion();
            MobEffect potion = (MobEffect)eff.getEffect().value();
            if (this.alert.isEnabled()) {
                String effectName = potion.getDisplayName().getString() + " " + String.valueOf(eff.getAmplifier() > 0 ? Integer.valueOf(eff.getAmplifier() + 1) : "");
                if (!Effects.mc.player.hasEffect(eff.getEffect())) {
                    if (!this.ended.getOrDefault(potion, false).booleanValue() && !potion.getCategory().equals((Object)MobEffectCategory.HARMFUL)) {
                        Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.INFO, "\u042d\u0444\u0444\u0435\u043a\u0442 " + effectName + " \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u0441\u044f", "\u0414\u0435\u0439\u0441\u0442\u0432\u0438\u0435 \u044d\u0444\u0444\u0435\u043a\u0442\u0430 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u043e");
                        this.ended.put(potion, true);
                    }
                } else {
                    this.ended.put(potion, false);
                }
            }
            anim.update(original.contains(eff));
            anim.setEasing(Easing.BAKEK);
            this.width = Math.max(Fonts.REGULAR.getFont(7.0f).width(potion.getDisplayName().getString()) + 60.0f, this.width);
            this.height += 18.0f * anim.getRGB();
        }
        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (Effects.mc.player == null || Effects.mc.level == null) {
            return;
        }
        Font font = Fonts.REGULAR.getFont(7.0f);
        float offset = 22.0f;
        super.renderComponent(context);
        MobEffectInstance toRemove = null;
        RectBatching split = new RectBatching(DefaultVertexFormat.POSITION_COLOR);
        for (MobEffectInstance statusEffectInstance : this.effects.values()) {
            Animation animation = ((StatusEffectInstanceAddition)statusEffectInstance).rockstar$getAnimPotion();
            if (animation.getRGB() == 0.0f) {
                toRemove = statusEffectInstance;
                continue;
            }
            float off = -4.5f + 4.5f * animation.getRGB();
            if (offset != 22.0f) {
                context.drawRect(this.x, this.y + offset + off, this.width, 0.5f, Colors.getTextColor().withAlpha(5.1f * animation.getRGB()));
            }
            offset += 18.0f * animation.getRGB();
        }
        ((Batching)split).draw();
        offset = 22.0f;
        for (MobEffectInstance statusEffectInstance : this.effects.values()) {
            Animation anim = ((StatusEffectInstanceAddition)statusEffectInstance).rockstar$getAnimPotion();
            if (anim.getRGB() == 0.0f) continue;
            float off = -4.5f + 4.5f * anim.getRGB();
            MobEffect effect = statusEffectInstance.getEffect().value();
            Identifier effectTexture = Identifier.withDefaultNamespace(
                    "textures/mob_effect/" + BuiltInRegistries.MOB_EFFECT.getKey(effect).getPath() + ".png"
            );
            context.drawTexture(
                    effectTexture,
                    this.x + 7.0f * anim.getRGB(),
                    this.y + offset + off + GuiUtility.getMiddleOfBox(8.0f, 18.0f) + 1.0f,
                    8.0f,
                    8.0f,
                    ColorRGBA.WHITE.withAlpha(255.0f * anim.getRGB())
            );
            offset += 18.0f * anim.getRGB();
        }
        FontBatching fontBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, font.getFont());
        offset = 22.0f;
        for (MobEffectInstance eff : this.effects.values()) {
            Animation anim = ((StatusEffectInstanceAddition)eff).rockstar$getAnimPotion();
            AnimatedNumber timeAnimation = ((StatusEffectInstanceAddition)eff).rockstar$getTimeAnimation();
            MobEffect potion = (MobEffect)eff.getEffect().value();
            if (anim.getRGB() == 0.0f) continue;
            float off = -4.5f + 4.5f * anim.getRGB();
            String effectName = potion.getDisplayName().getString() + " " + String.valueOf(eff.getAmplifier() > 0 ? Integer.valueOf(eff.getAmplifier() + 1) : "");
            if (eff.isInfiniteDuration() || eff.getDuration() >= 999999999) {
                String duration = "**:**";
                float timeX = this.x + this.width - 7.0f * anim.getRGB();
                float timeY = this.y + offset + off + GuiUtility.getMiddleOfBox(font.height(), 18.0f);
                context.drawRightText(font, duration, timeX, timeY, Colors.getTextColor().withAlpha((int)(255.0f * anim.getRGB())));
            } else {
                int totalSeconds = eff.getDuration() / 20;
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                String timeStr = String.format("%02d:%02d", minutes, seconds);
                String minutesAndSeparator = String.format("%02d:", minutes);
                float timeX = this.x + this.width - 7.0f * anim.getRGB();
                float timeY = this.y + offset + off + GuiUtility.getMiddleOfBox(font.height(), 18.0f);
                float minutesWidth = font.width(minutesAndSeparator);
                float totalWidth = font.width(timeStr);
                context.drawText(font, minutesAndSeparator, timeX - totalWidth, timeY, Colors.getTextColor().withAlpha(255.0f * anim.getRGB()));
                timeAnimation.settings(true, Colors.getTextColor().withAlpha(255.0f * anim.getRGB()));
                timeAnimation.update(seconds);
                timeAnimation.pos(timeX - totalWidth + minutesWidth, timeY);
                timeAnimation.render(context);
            }
            context.drawText(font, effectName, this.x + 13.0f + 7.0f * anim.getRGB(), this.y + offset + off + GuiUtility.getMiddleOfBox(font.height(), 18.0f), Colors.getTextColor().withAlpha(255.0f * anim.getRGB()));
            offset += 18.0f * anim.getRGB();
        }
        ((Batching)fontBatching).draw();
        if (toRemove != null) {
            MobEffect statusEffect = (MobEffect)toRemove.getEffect().value();
            this.effects.remove(statusEffect.getDisplayName().getString(), toRemove);
        }
    }

    @Override
    public boolean show() {
        if (Effects.mc.player == null || Effects.mc.level == null) {
            return false;
        }
        return (!Effects.mc.player.getActiveEffects().isEmpty() || Effects.mc.screen instanceof ChatScreen || this.alwaysDisplay.isEnabled()) && !ServerUtility.isCM();
    }
}


