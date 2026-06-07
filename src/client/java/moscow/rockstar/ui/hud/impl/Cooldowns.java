package moscow.rockstar.ui.hud.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.ui.components.animated.AnimatedNumber;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.gui.screens.ChatScreen;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import moscow.rockstar.mixin.minecraft.entity.ItemCooldownsAccessor;
import moscow.rockstar.mixin.minecraft.entity.CooldownInstanceAccessor;

public class Cooldowns extends HudList {
    private final BooleanSetting alwaysDisplay = new BooleanSetting(this, "hud.always_display");

    // Кулдауны предметов (в тиках, 20 тиков = 1 секунда)
    private static final Map<Item, Integer> COOLDOWN_DURATIONS = new HashMap<>();
    private static final Map<Item, String> ITEM_NAMES = new HashMap<>();
    private static final Map<Item, Integer> REMOTE_DURATIONS = new HashMap<>();

    public static void updateRemoteDuration(Item item, int duration) {
        REMOTE_DURATIONS.put(item, duration);
    }

    static {
        // Инициализация кулдаунов (секунды * 20 = тики)
        COOLDOWN_DURATIONS.put(Items.DRIED_KELP, 20 * 20); // Пласт
        COOLDOWN_DURATIONS.put(Items.NETHERITE_SCRAP, 15 * 20); // Трапка
        COOLDOWN_DURATIONS.put(Items.ENDER_PEARL, 60 * 20); // Перка
        COOLDOWN_DURATIONS.put(Items.GOLDEN_APPLE, 60 * 20); // Гэпл
        COOLDOWN_DURATIONS.put(Items.ENCHANTED_GOLDEN_APPLE, 180 * 20); // Чарка
        COOLDOWN_DURATIONS.put(Items.SNOWBALL, 10 * 20); // Снежок
        COOLDOWN_DURATIONS.put(Items.ENDER_EYE, 60 * 20); // Дезик
        COOLDOWN_DURATIONS.put(Items.SUGAR, 30 * 20); // Явная пыль
        COOLDOWN_DURATIONS.put(Items.PHANTOM_MEMBRANE, 15 * 20); // Божка

        // Названия предметов
        ITEM_NAMES.put(Items.DRIED_KELP, "Пласт");
        ITEM_NAMES.put(Items.NETHERITE_SCRAP, "Трапка");
        ITEM_NAMES.put(Items.ENDER_PEARL, "Перка");
        ITEM_NAMES.put(Items.GOLDEN_APPLE, "Гэпл");
        ITEM_NAMES.put(Items.ENCHANTED_GOLDEN_APPLE, "Чарка");
        ITEM_NAMES.put(Items.SNOWBALL, "Снежок");
        ITEM_NAMES.put(Items.ENDER_EYE, "Дезик");
        ITEM_NAMES.put(Items.SUGAR, "Явная пыль");
        ITEM_NAMES.put(Items.PHANTOM_MEMBRANE, "Божка");
    }

    private final Map<Item, CooldownEntry> activeCooldowns = new TreeMap<>((a, b) -> {
        String nameA = ITEM_NAMES.getOrDefault(a, a.toString());
        String nameB = ITEM_NAMES.getOrDefault(b, b.toString());
        return nameA.compareTo(nameB);
    });

    private static class CooldownInfo {
        final int duration;
        final int remaining;

        CooldownInfo(int duration, int remaining) {
            this.duration = duration;
            this.remaining = remaining;
        }
    }

    private static CooldownInfo getDynamicCooldown(Item item) {
        if (mc.player == null) {
            return null;
        }
        net.minecraft.world.item.ItemCooldowns tracker = mc.player.getCooldowns();
        if (tracker instanceof ItemCooldownsAccessor accessor) {
            Map<Item, ?> cooldownsMap = accessor.getCooldowns();
            if (cooldownsMap != null && cooldownsMap.containsKey(item)) {
                Object instance = cooldownsMap.get(item);
                if (instance instanceof CooldownInstanceAccessor instanceAccessor) {
                    int startTime = instanceAccessor.getStartTime();
                    int endTime = instanceAccessor.getEndTime();
                    int tickCount = accessor.getTickCount();
                    int duration = endTime - startTime;
                    int remaining = endTime - tickCount;
                    return new CooldownInfo(duration, remaining);
                }
            }
        }
        return null;
    }

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null)
            return;

        // Обновляем существующие кулдауны
        activeCooldowns.entrySet().removeIf(entry -> {
            Item item = entry.getKey();
            CooldownEntry cooldown = entry.getValue();

            CooldownInfo info = getDynamicCooldown(item);
            if (info != null) {
                cooldown.remainingTicks = info.remaining;
                cooldown.maxTicks = info.duration;
            } else {
                float progress = mc.player.getCooldowns().getCooldownPercent(item.getDefaultInstance(), 0.0f);
                if (progress <= 0.0f) {
                    return true;
                }
                int duration = REMOTE_DURATIONS.getOrDefault(item, COOLDOWN_DURATIONS.getOrDefault(item, 0));
                if (duration > 0) {
                    cooldown.remainingTicks = (int) (duration * progress);
                } else {
                    cooldown.remainingTicks--;
                }
            }

            return cooldown.remainingTicks <= 0;
        });

        // Проверяем новые кулдауны
        for (Item item : ITEM_NAMES.keySet()) {
            if (mc.player.getCooldowns().isOnCooldown(item.getDefaultInstance())) {
                if (!activeCooldowns.containsKey(item)) {
                    CooldownInfo info = getDynamicCooldown(item);
                    int duration;
                    int remaining;
                    if (info != null) {
                        duration = info.duration;
                        remaining = info.remaining;
                    } else {
                        duration = REMOTE_DURATIONS.getOrDefault(item, COOLDOWN_DURATIONS.getOrDefault(item, 0));
                        float progress = mc.player.getCooldowns().getCooldownPercent(item.getDefaultInstance(), 0.0f);
                        remaining = (int) (duration * progress);
                    }
                    activeCooldowns.put(item, new CooldownEntry(item, duration, remaining));
                }
            }
        }
    };

    public Cooldowns() {
        super("hud.cooldowns", "icons/hud/cooldown.png");
        this.setShowing(true); // Включаем по умолчанию
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    @Override
    public void update(UIContext context) {
        this.width = 92.0f;
        this.height = 18.0f;

        if (!activeCooldowns.isEmpty()) {
            this.height += 5.0f;
        }

        Font font = Fonts.REGULAR.getFont(7.0f);
        for (CooldownEntry entry : activeCooldowns.values()) {
            entry.animation.update(true);
            entry.animation.setEasing(Easing.BAKEK);

            String itemName = ITEM_NAMES.getOrDefault(entry.item, entry.item.toString());
            this.width = Math.max(font.width(itemName) + 60.0f, this.width);
            this.height += 18.0f * entry.animation.getRGB();
        }

        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        Font font = Fonts.REGULAR.getFont(7.0f);
        float offset = 22.0f;
        super.renderComponent(context);

        // Рендер разделителей
        for (CooldownEntry entry : activeCooldowns.values()) {
            if (entry.animation.getRGB() == 0.0f)
                continue;

            float off = -4.5f + 4.5f * entry.animation.getRGB();
            if (offset != 22.0f) {
                context.drawRect(this.x, this.y + offset + off, this.width, 0.5f,
                        Colors.getTextColor().withAlpha(5.1f * entry.animation.getRGB()));
            }
            offset += 18.0f * entry.animation.getRGB();
        }

        // Рендер иконок предметов через context.item (не ломает ванильный HUD)
        offset = 22.0f;
        for (CooldownEntry entry : activeCooldowns.values()) {
            if (entry.animation.getRGB() == 0.0f)
                continue;

            float off = -4.5f + 4.5f * entry.animation.getRGB();
            float scale = 0.5f;
            float itemSize = 16.0f * scale;
            float iconX = this.x + 5.0f * entry.animation.getRGB() + (16.0f - itemSize) / 2.0f;
            float iconY = this.y + offset + off + (18.0f - itemSize) / 2.0f;

            float prevAlpha = moscow.rockstar.utility.render.ShaderColorHelper.getAlpha();
            moscow.rockstar.utility.render.ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha * entry.animation.getRGB());
            context.item(entry.item.getDefaultInstance(), iconX, iconY, scale);
            moscow.rockstar.utility.render.ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha);

            offset += 18.0f * entry.animation.getRGB();
        }

        // Рендер текста
        offset = 22.0f;

        for (CooldownEntry entry : activeCooldowns.values()) {
            if (entry.animation.getRGB() == 0.0f)
                continue;

            float off = -4.5f + 4.5f * entry.animation.getRGB();
            String itemName = ITEM_NAMES.getOrDefault(entry.item, entry.item.toString());

            // Рендер названия предмета
            context.drawText(font, itemName,
                    this.x + 14.0f + 5.0f * entry.animation.getRGB(),
                    this.y + offset + off + GuiUtility.getMiddleOfBox(font.height(), 18.0f),
                    Colors.getTextColor().withAlpha(255.0f * entry.animation.getRGB()));

            // Определяем реальное оставшееся время
            int totalSeconds = Math.max(0, entry.remainingTicks) / 20;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            // Показываем полное время только если оставшееся == максимальное
            // Для коротких кулдаунов (< 60 сек) показываем только секунды
            ColorRGBA timeColor;
            if (entry.maxTicks > 0 && entry.remainingTicks >= entry.maxTicks - 2) {
                // При максимальном кулдауне — акцентный цвет
                timeColor = Colors.getAccentColor().withAlpha(255.0f * entry.animation.getRGB());
            } else {
                timeColor = Colors.getTextColor().withAlpha(255.0f * entry.animation.getRGB());
            }

            float timeX = this.x + this.width - 5.0f * entry.animation.getRGB();
            float timeY = this.y + offset + off + GuiUtility.getMiddleOfBox(font.height(), 18.0f);

            if (totalSeconds > 60) {
                // mm:ss формат
                String minutesAndSeparator = String.format("%d:", minutes);
                String timeStr = minutesAndSeparator + String.format("%02d", seconds);
                float totalWidth = font.width(timeStr);
                float minutesWidth = font.width(minutesAndSeparator);

                context.drawText(font, minutesAndSeparator, timeX - totalWidth, timeY, timeColor);

                entry.timeAnimation.settings(true, timeColor);
                entry.timeAnimation.update(seconds);
                entry.timeAnimation.pos(timeX - totalWidth + minutesWidth, timeY);
                entry.timeAnimation.render(context);
            } else {
                // Только секунды — быстрый счёт для коротких кулдаунов
                entry.timeAnimation.settings(true, timeColor);
                entry.timeAnimation.update(totalSeconds);

                String sText = " s";
                float sWidth = font.width(sText);
                float numWidth = entry.timeAnimation.getWidth();
                float totalWidth = numWidth + sWidth;

                entry.timeAnimation.pos(timeX - totalWidth, timeY);
                entry.timeAnimation.render(context);
                context.drawText(font, sText, timeX - sWidth, timeY, timeColor);
            }

            offset += 18.0f * entry.animation.getRGB();
        }
        
        // Сбрасываем цвет шейдера, чтобы не ломать последующий рендер (сердечки, еду и т.д.)
        
    }

    public java.util.Collection<Item> getActiveCooldownItems() {
        return this.activeCooldowns.keySet();
    }

    @Override
    public boolean show() {
        if (mc.player == null || mc.level == null) {
            return false;
        }
        return !activeCooldowns.isEmpty() || mc.screen instanceof ChatScreen || this.alwaysDisplay.isEnabled();
    }


    // Внутренний класс для хранения информации о кулдауне
    private static class CooldownEntry {
        final Item item;
        int remainingTicks;
        int maxTicks;
        final Animation animation;
        final AnimatedNumber timeAnimation;

        CooldownEntry(Item item, int maxTicks, int remainingTicks) {
            this.item = item;
            this.maxTicks = maxTicks;
            this.remainingTicks = remainingTicks;
            this.animation = new Animation(300L, 0.0f, Easing.BAKEK);
            this.animation.setValue(1.0f);
            this.timeAnimation = new AnimatedNumber(Fonts.REGULAR.getFont(7.0f), 3.0f, 300L, Easing.FIGMA_EASE_IN_OUT);
        }
    }
}

