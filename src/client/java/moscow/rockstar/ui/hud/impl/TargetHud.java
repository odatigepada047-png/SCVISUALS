package moscow.rockstar.ui.hud.impl;

import java.util.Arrays;
import moscow.rockstar.utility.game.ClientEntityUtility;
import moscow.rockstar.utility.render.ShaderColorHelper;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TargetHud extends HudElement {
    private final BooleanSetting rayTrace = new BooleanSetting(this, "hud.targethud.look");
    private final ModeSetting armor = new ModeSetting(this, "hud.targethud.armor");
    private final ModeSetting.Value armorNone = new ModeSetting.Value(this.armor, "hud.targethud.armor.none");
    private final ModeSetting.Value armorNumber = new ModeSetting.Value(this.armor, "hud.targethud.armor.number").select();
    private final ModeSetting.Value armorIcon = new ModeSetting.Value(this.armor, "hud.targethud.armor.icon");
    private final BooleanSetting ignoreFriends = new BooleanSetting(this, "hud.targethud.ignore_friends");

    public static final java.util.Map<Integer, java.util.List<org.joml.Vector3f[]>> SKELETON_LINES = new java.util.concurrent.ConcurrentHashMap<>();

    private final Animation content = new Animation(300L, 0.0f, Easing.BAKEK_SIZE);
    private final Animation health = new Animation(300L, 0.0f, Easing.BAKEK);
    private final Animation golden = new Animation(300L, 0.0f, Easing.BAKEK);
    private final Animation number = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation absNumberAnim = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation goldenTextAnim = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation itemsX = new Animation(300L, 0.0f, Easing.BAKEK);
    private final Animation copy = new Animation(300L, 0.0f, Easing.BAKEK);
    private final Animation success = new Animation(500L, 0.0f, Easing.BAKEK_SIZE);
    private final Animation eatingPulse = new Animation(250L, 0.0f, Easing.BAKEK);
    private final Animation pulseIntensity = new Animation(50L, 0.0f, Easing.SINE_IN_OUT);
    private final Animation[] items = new Animation[4];

    private LivingEntity target;
    private final Timer copyTimer = new Timer();
    private final Timer targetTimer = new Timer();
    private boolean copied;

    public TargetHud() {
        super("hud.targethud", "icons/hud/target.png");
        for (int i = 0; i < this.items.length; ++i) {
            this.items[i] = new Animation(300L, 0.0f, Easing.BAKEK);
        }
    }

    public LivingEntity getTargetEntity() {
        return this.target;
    }

    @Override
    public void update(UIContext context) {
        if (this.visible.getValue() == 0.0f) {
            this.target = null;
        }
        this.width = 103.0f;
        this.height = 31.0f;
        super.update(context);
    }

    @Override
    public void setShowing(boolean showing) {
        super.setShowing(showing);
        if (!showing) {
            this.target = null;
        }
    }

    @Override
    protected void renderComponent(UIContext context) {
        float prevAlpha = ShaderColorHelper.getAlpha();
        LivingEntity currentTarget = this.getTarget();
        if (currentTarget != null) {
            this.target = currentTarget;
        }
        if (this.target == null) {
            return;
        }

        Font regular7 = Fonts.REGULAR.getFont(7.0f);
        Font semibold6 = Fonts.SEMIBOLD.getFont(6.0f);
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        ColorRGBA bgColor = Colors.getBackgroundColor().withAlpha(255.0f * (dark ? 0.8f - 0.6f * Interface.glass() : 0.7f));

        float contentYAnim = 6.0f * this.content.getRGB();
        float g = this.goldenTextAnim.getRGB();
        float textY = this.y + 3.0f + contentYAnim - 2.0f * g;

        // Логика наведения для копирования
        boolean hover = GuiUtility.isHovered(this.x + 30.0f, textY, 60.0, 6.0, context);
        if (!hover || this.copyTimer.finished(1000L)) {
            this.copied = false;
        }
        this.copy.update(hover);
        this.success.update(this.copied);

        // Логика проверки еды/зелий
        boolean isUsingConsumable = this.target.isUsingItem() &&
                (this.target.getActiveItem().has(DataComponents.FOOD) ||
                        this.target.getActiveItem().getItem() == Items.POTION ||
                        this.target.getActiveItem().getItem() == Items.MILK_BUCKET);

        this.eatingPulse.update(isUsingConsumable);
        if (isUsingConsumable) {
            float pulse = (float)Math.sin((double)System.currentTimeMillis() / 150.0) * 0.5f + 0.5f;
            this.pulseIntensity.setValue(pulse);
        }

        this.content.update(this.animation.getRGB() * this.visible.getRGB() >= 1.0f);

        float rawHp = (this.target instanceof Player) ? EntityUtility.getHealth((Player)this.target) : this.target.getHealth();
        float abs = this.target.getAbsorptionAmount();
        float baseHp = Math.max(0.0f, rawHp - abs);

        this.health.update(baseHp / this.target.getMaxHealth());
        this.golden.update(abs / 20.0f);
        this.number.update(baseHp);
        this.absNumberAnim.update(abs);
        this.goldenTextAnim.update(abs > 0.01f ? 1.0f : 0.0f);

        if (this.animation.getRGB() == 0.0f) return;

        // Отрисовка брони
        if (!this.armorNone.isSelected()) {
            this.renderArmor(context, semibold6, bgColor);
        }

        // Основной фон и тени
        float hudAlpha = this.animation.getRGB() * this.visible.getRGB();
        context.drawShadow(this.x - 5.0f, this.y - 5.0f, this.width + 10.0f, this.height + 10.0f, 15.0f, BorderRadius.all(6.0f), ColorRGBA.BLACK.withAlpha(63.75f * this.dragAnim.getRGB() * hudAlpha));
        if (Interface.showMinimalizm()) {
            context.drawBlurredRect(this.x, this.y, this.width, this.height, 45.0f, 7.0f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * hudAlpha * Interface.minimalizm()));
        }
        if (Interface.showGlass()) {
            context.drawLiquidGlass(this.x, this.y, this.width, this.height, 7.0f, 0.08f - 0.07f * this.dragAnim.getRGB(), BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * hudAlpha * Interface.glass()));
        }
        context.drawSquircle(this.x, this.y, this.width, this.height, 7.0f, BorderRadius.all(6.0f), bgColor.withAlpha(bgColor.getAlpha() * hudAlpha));

        float alpha = 255.0f * this.content.getRGB();
        ScissorUtility.push(context.pose(), this.x, this.y, this.width, this.height);

        // Голова игрока
        if (this.target instanceof AbstractClientPlayer) {
            context.drawHead((AbstractClientPlayer)this.target, this.x + 6.0f * this.content.getRGB(), this.y + 6.0f, 19.0f, BorderRadius.all(3.0f), Colors.WHITE.withAlpha(alpha));
        } else {
            context.drawRoundedTexture(Rockstar.id(Interface.glassSelected() ? "icons/hud/whoglass.png" : (dark ? "icons/hud/whodark.png" : "icons/hud/who.png")), this.x + 6.0f * this.content.getRGB(), this.y + 6.0f, 19.0f, 19.0f, BorderRadius.all(3.0f), Colors.WHITE.withAlpha(alpha));
        }

        // Выравнивание текста и иконок
        float eatOffset = 9.0f * this.eatingPulse.getRGB(); // На сколько отъезжает ник если челик ест
        float copyOffset = 8.0f * this.copy.getRGB();       // На сколько отъезжает ник при наведении

        String name = this.target.getName().getString();
        String hpText = rawHp == 1000.0f ? "?" : TextUtility.formatNumber(this.number.getRGB()).replace(",", ".");
        String absText = " + " + TextUtility.formatNumber(this.absNumberAnim.getRGB()).replace(",", ".");
        float hpY = this.y + 3.5f + contentYAnim - 2.0f * g;
        float goldenHpY = hpY + 7.0f * g;

        // Рендер иконки еды (когда используется)
        if (this.eatingPulse.getRGB() > 0.001f) {
            ItemStack activeStack = this.target.getActiveItem();
            if (!activeStack.isEmpty()) {
                float foodX = this.x + 30.0f;
                float foodY = textY - 0.5f; // Центрирование по высоте относительно шрифта
                float pAlpha = (0.7f + 0.3f * this.pulseIntensity.getRGB()) * (alpha / 255f) * this.eatingPulse.getRGB();

                ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha * pAlpha);
                context.item(activeStack, foodX, foodY, 0.45f);
                ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha);
            }
        }

        // Рендер ника
        context.drawFadeoutText(regular7, name,
                this.x + 30.0f + eatOffset + copyOffset,
                textY,
                Colors.getTextColor().withAlpha(alpha), 0.7f, 1.0f,
                this.width - 40.0f - (eatOffset + copyOffset) - semibold6.width(hpText));

        // Рендер иконки копирования
        if (this.copy.getRGB() > 0.001f) {
            float cIconX = this.x + 25.0f + 5.0f * this.copy.getRGB() + eatOffset;

            RenderUtility.rotate(context.pose(), cIconX + 3.0f, textY + 3.0f, 90.0f * this.success.getRGB());
            context.drawTexture(Rockstar.id("icons/hud/copy.png"), cIconX, textY, 6.0f, 6.0f, Colors.getTextColor().withAlpha(alpha * this.copy.getRGB() * (1.0f - this.success.getRGB())));
            RenderUtility.end(context.pose());

            RenderUtility.rotate(context.pose(), cIconX + 3.0f, textY + 3.0f, -90.0f + 90.0f * this.success.getRGB());
            context.drawTexture(Rockstar.id("icons/check.png"), cIconX, textY, 6.0f, 6.0f, Colors.GREEN.withAlpha(alpha * this.copy.getRGB() * this.success.getRGB()));
            RenderUtility.end(context.pose());
        }

        // ХП текст справа
        float rightX = this.x + this.width - 7.0f;
        context.drawRightText(semibold6, hpText, rightX, hpY, Colors.getAccentColor().withAlpha(alpha));

        if (g > 0.01f) {
            context.drawRightText(semibold6, absText, rightX, goldenHpY, new ColorRGBA(255.0f, 220.0f, 81.0f, alpha * g));
        }

        // Полоски здоровья
        float barWidth = 65.0f;
        float barHeight = 3.0f - 0.5f * g;
        float regBarY = this.y + this.height - 6.0f - contentYAnim - 2.0f * g;

        context.drawRoundedRect(this.x + 30.0f, regBarY, barWidth, barHeight, BorderRadius.all(0.7f), Colors.getAdditionalColor().withAlpha(alpha * (1.0f - 0.7f * Interface.glass())));
        context.drawRoundedRect(this.x + 30.0f, regBarY, barWidth * Math.clamp(this.health.getRGB(), 0.0f, 1.0f), barHeight, BorderRadius.all(0.7f), Colors.getAccentColor().withAlpha(alpha));

        if (g > 0.01f) {
            float goldBarY = regBarY + 5.0f * g;
            float goldenBarWidth = barWidth * Math.clamp(this.golden.getRGB(), 0.0f, 1.0f);
            context.drawRoundedRect(this.x + 30.0f, goldBarY, barWidth, barHeight, BorderRadius.all(0.7f), Colors.getAdditionalColor().withAlpha(alpha * g * (1.0f - 0.7f * Interface.glass())));
            context.drawRoundedRect(this.x + 30.0f, goldBarY, goldenBarWidth, barHeight, BorderRadius.all(0.7f), new ColorRGBA(255.0f, 220.0f, 81.0f, alpha * g));
        }

        ScissorUtility.pop();
    }

    private void renderArmor(UIContext context, Font semibold6, ColorRGBA bgColor) {
        float prev = ShaderColorHelper.getAlpha();

        float animOff = 0.0f;
        int i = 0;
        ItemStack[] hands = {this.target.getMainHandItem(), this.target.getOffhandItem()};

        for (ItemStack item : ClientEntityUtility.getArmorItems(this.target)) {
            if (item.isEmpty()) continue;
            animOff += (this.armorIcon.isSelected() ? 11.0f : 5.0f + semibold6.width(calculateDurabilityPercent(item))) + 2.0f;
        }
        if (this.armorIcon.isSelected()) {
            for (ItemStack hand : hands) {
                if (!hand.isEmpty()) animOff += 13.0f;
            }
        }

        this.itemsX.update(animOff - 2.0f);
        float xOff = -this.itemsX.getRGB() / 2.0f;
        float hudAlpha = this.animation.getRGB() * this.visible.getRGB();

        for (ItemStack armorItem : ClientEntityUtility.getArmorItems(this.target)) {
            this.items[i].update(!armorItem.isEmpty());
            float a = this.content.getRGB() * this.items[i].getValue();
            if (a <= 0.01f) { i++; continue; }
            String dur = calculateDurabilityPercent(armorItem);
            boolean iconMode = this.armorIcon.isSelected();
            float pW = iconMode ? 11.0f : 5.0f + semibold6.width(dur);
            float pX = this.x + this.width / 2.0f + xOff;
            float pY = this.y + this.height - 4.0f + 6.0f * a;

            ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prev * a * hudAlpha);
            if (!Interface.showMinimalizm()) {
                context.drawBlurredRect(pX, pY, pW, 11.0f, 5.0f, BorderRadius.all(1.5f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getRGB() * hudAlpha * a));
            }
            context.drawRoundedRect(pX, pY, pW, 11.0f, BorderRadius.all(1.5f), bgColor.withAlpha(bgColor.getAlpha() * a * hudAlpha));

            ScissorUtility.push(context.pose(), pX, pY, pW, 11.0f);
            context.item(armorItem, pX - 11.0f + pW / 2.0f + (this.armorNumber.isSelected() ? 2.0f : 5.5f), pY - (this.armorNumber.isSelected() ? 4.0f : 0.0f), this.armorNumber.isSelected() ? 1.0f : 0.7f);
            ScissorUtility.pop();

            if (this.armorNumber.isSelected()) {
                context.drawText(semibold6, dur, pX + 3.0f, pY + 2.5f, Colors.getTextColor().withAlpha(255.0f * a * hudAlpha));
            }
            xOff += (pW + 2.0f) * a;
            i++;
        }

        ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prev);

        float hW = Arrays.stream(hands).mapToInt(item -> item.isEmpty() ? 0 : 13).sum() - 2;
        float hX = this.armorNumber.isSelected() ? -hW / 2.0f : xOff;
        for (ItemStack hand : hands) {
            if (hand.isEmpty()) continue;
            float hItemX = this.x + this.width / 2.0f + hX;
            float hItemY = this.y + this.height - 4.0f + 6.0f * this.content.getRGB() + (this.armorNumber.isSelected() ? 12.0f : 0.0f);
            float activePulse = (this.target.getActiveItem() == hand) ? 0.5f + 0.5f * this.pulseIntensity.getRGB() : 1.0f;

            context.drawRoundedRect(hItemX, hItemY, 11.0f, 11.0f, BorderRadius.all(1.5f), bgColor.withAlpha(bgColor.getAlpha() * this.content.getRGB() * activePulse * hudAlpha));
            ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prev * this.content.getRGB() * hudAlpha);
            context.item(hand, hItemX, hItemY, 0.7f);
            ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prev);
            hX += 13.0f;
        }
    }

    private String calculateDurabilityPercent(ItemStack itemStack) {
        if (itemStack.isEmpty() || !itemStack.isDamageableItem()) return "100%";
        int max = itemStack.getMaxDamage();
        int cur = itemStack.getDamageValue();
        return String.format("%.0f%%", 100.0 - (double)cur / (double)max * 100.0);
    }

    private LivingEntity getTarget() {
        // Priority 0: Check locked target from ModerUtils
        moscow.rockstar.systems.modules.modules.other.ModerUtils moderUtils = 
            Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.other.ModerUtils.class);
        if (moderUtils != null && moderUtils.isEnabled()) {
            net.minecraft.world.entity.player.Player lockedTarget = moderUtils.getLockedTarget();
            if (lockedTarget != null && lockedTarget.isAlive()) {
                this.targetTimer.reset();
                return lockedTarget;
            }
        }
        
        if (!EntityUtility.isInGame()) {
            this.target = null;
            return null;
        }
        Entity t = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        if (t instanceof LivingEntity && t.isAlive()) {
            if (this.ignoreFriends.isEnabled() && Rockstar.getInstance().getFriendManager().isFriend(t.getName().getString())) {
                return null;
            }
            this.targetTimer.reset();
            return (LivingEntity)t;
        }
        Entity crosshairEntity = ClientEntityUtility.getCrosshairEntity();
        if (this.rayTrace.isEnabled() && crosshairEntity instanceof LivingEntity livingCrosshair && livingCrosshair.isAlive()) {
            if (this.ignoreFriends.isEnabled() && Rockstar.getInstance().getFriendManager().isFriend(livingCrosshair.getName().getString())) {
                return null;
            }
            this.targetTimer.reset();
            return livingCrosshair;
        }
        if (TargetHud.mc.screen instanceof ChatScreen) {
            this.targetTimer.reset();
            return TargetHud.mc.player;
        }
        if (this.target != null && this.target.isAlive() && !this.targetTimer.finished(2000L)) {
            if (this.ignoreFriends.isEnabled() && Rockstar.getInstance().getFriendManager().isFriend(this.target.getName().getString())) {
                return null;
            }
            return this.target;
        }
        return null;
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        float g = this.goldenTextAnim.getRGB();
        if (GuiUtility.isHovered((this.x + 30.0f), (this.y + 3.0f + 6.0f * this.content.getRGB() - 2.0f * g), 60.0, 6.0, mouseX, mouseY)) {
            if (this.target != null) {
                TextUtility.copyText(this.target.getName().getString());
                this.copyTimer.reset();
                this.copied = true;
            }
            return;
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean show() {
        return this.getTarget() != null;
    }
}
