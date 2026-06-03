package moscow.rockstar.ui.hud.impl;

import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.game.ClientEntityUtility;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ArmorHud extends HudElement {
    private final BooleanSetting breakAlert = new BooleanSetting(this, "hud.armorhud.alert");
    private final BooleanSetting showPercent = new BooleanSetting(this, "hud.armorhud.percent").enable();
    
    private final Animation[] items = new Animation[4];
    private final Animation orientationAnim = new Animation(300L, 0.0f, Easing.BAKEK);
    private final Map<Integer, Boolean> alertedSlots = new HashMap<>();
    private final Timer alertCooldown = new Timer();
    
    private boolean isVertical = false;
    private float lastTargetWidth = -1.0f;
    private float lastTargetHeight = -1.0f;
    
    public ArmorHud() {
        super("hud.armorhud", "icons/hud/armor.png");
        this.setShowing(true); // Включаем по умолчанию
        for (int i = 0; i < this.items.length; ++i) {
            this.items[i] = new Animation(300L, 0.0f, Easing.BAKEK);
        }
    }

    @Override
    public void update(UIContext context) {
        // Определяем ориентацию только от левого/правого края экрана
        float screenWidth = IScaledResolution.sr.getGuiScaledWidth();
        float edgeThreshold = 50.0f;
        
        boolean nearLeftEdge = this.x < edgeThreshold;
        boolean nearRightEdge = this.x + this.width > screenWidth - edgeThreshold;
        
        this.isVertical = nearLeftEdge || nearRightEdge;
        this.orientationAnim.update(this.isVertical);
        
        // Подсчитываем количество надетой брони
        int armorCount = 0;
        if (mc.player != null) {
            for (ItemStack item : ClientEntityUtility.getArmorItems(mc.player)) {
                if (!item.isEmpty()) {
                    armorCount++;
                }
            }
        }
        
        // Размеры в зависимости от ориентации и количества брони
        float itemSize = 19.5f; // Увеличено в 1.5 раза (было 13.0f)
        float spacing = 2.0f;
        
        float targetWidth;
        float targetHeight;
        if (armorCount == 0) {
            targetWidth = itemSize;
            targetHeight = itemSize;
        } else {
            float horizontalWidth = armorCount * itemSize + (armorCount - 1) * spacing;
            float horizontalHeight = itemSize;
            float verticalWidth = itemSize;
            float verticalHeight = armorCount * itemSize + (armorCount - 1) * spacing;
            
            float t = this.orientationAnim.getRGB();
            targetWidth = horizontalWidth + (verticalWidth - horizontalWidth) * t;
            targetHeight = horizontalHeight + (verticalHeight - horizontalHeight) * t;
        }
        
        if (this.lastTargetWidth == -1.0f) {
            this.lastTargetWidth = targetWidth;
            this.lastTargetHeight = targetHeight;
        }
        
        // Масштабируем dragX/dragY если идет перетаскивание и размеры изменились
        if (this.isDragging() && this.lastTargetWidth > 0.0f && this.lastTargetHeight > 0.0f) {
            this.setDragX((this.getDragX() / this.lastTargetWidth) * targetWidth);
            this.setDragY((this.getDragY() / this.lastTargetHeight) * targetHeight);
        }
        
        this.lastTargetWidth = targetWidth;
        this.lastTargetHeight = targetHeight;
        
        this.width = targetWidth;
        this.height = targetHeight;
        
        super.update(context);
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (mc.player == null) return;
        
        // Сохраняем текущее состояние рендера
        float[] prevColor = new float[]{1.0f,1.0f,1.0f,1.0f};
        
        Font semibold6 = Fonts.SEMIBOLD.getFont(6.0f);
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        ColorRGBA bgColor = Colors.getBackgroundColor().withAlpha(255.0f * (dark ? 0.8f - 0.6f * Interface.glass() : 0.7f));
        
        // Рендер отдельных слотов брони
        this.renderArmor(context, semibold6, bgColor);
        
        // Полностью восстанавливаем состояние рендера
        
    }

    private void renderArmor(UIContext context, Font font, ColorRGBA bgColor) {
        if (mc.player == null) return;
        float hudAlpha = this.animation.getRGB() * this.visible.getRGB();
        
        // Получаем броню в правильном порядке: ботинки, штаны, нагрудник, шлем
        ItemStack[] armorItemsRaw = new ItemStack[4];
        int index = 0;
        for (ItemStack item : ClientEntityUtility.getArmorItems(mc.player)) {
            armorItemsRaw[index++] = item;
        }
        
        // Переворачиваем порядок: шлем, нагрудник, штаны, ботинки
        ItemStack[] armorItems = new ItemStack[4];
        armorItems[0] = armorItemsRaw[3]; // Шлем
        armorItems[1] = armorItemsRaw[2]; // Нагрудник
        armorItems[2] = armorItemsRaw[1]; // Штаны
        armorItems[3] = armorItemsRaw[0]; // Ботинки
        
        float itemSize = 19.5f; // Увеличено в 1.5 раза
        float spacing = 2.0f;
        float t = this.orientationAnim.getRGB();
        
        int visibleIndex = 0; // Индекс для видимых элементов
        
        for (int i = 0; i < 4; i++) {
            ItemStack armorItem = armorItems[i];
            
            // Используем правильный индекс для анимации (обратный порядок)
            int animIndex = 3 - i;
            
            // Обновляем анимацию только если есть предмет
            this.items[animIndex].update(!armorItem.isEmpty());
            
            float a = this.items[animIndex].getValue();
            if (a <= 0.01f) continue;
            
            // Позиция элемента с плавной интерполяцией
            float horizontalX = this.x + visibleIndex * (itemSize + spacing);
            float horizontalY = this.y;
            float verticalX = this.x;
            float verticalY = this.y + visibleIndex * (itemSize + spacing);
            
            float itemX = horizontalX + (verticalX - horizontalX) * t;
            float itemY = horizontalY + (verticalY - horizontalY) * t;
            
            // Фон элемента с тенью
            context.drawShadow(itemX - 2.0f, itemY - 2.0f, itemSize + 4.0f, itemSize + 4.0f, 10.0f, BorderRadius.all(3.0f), ColorRGBA.BLACK.withAlpha(63.75f * this.dragAnim.getRGB() * a));
            
            if (Interface.showMinimalizm()) {
                context.drawBlurredRect(itemX, itemY, itemSize, itemSize, 45.0f, 7.0f, BorderRadius.all(3.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getRGB() * Interface.minimalizm() * a));
            }
            if (Interface.showGlass()) {
                context.drawLiquidGlass(itemX, itemY, itemSize, itemSize, 7.0f, 0.08f - 0.07f * this.dragAnim.getRGB(), BorderRadius.all(3.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getRGB() * Interface.glass() * a));
            }
            context.drawSquircle(itemX, itemY, itemSize, itemSize, 7.0f, BorderRadius.all(3.0f), bgColor.withAlpha(bgColor.getAlpha() * a));
            
            // Рендер предмета
            if (!armorItem.isEmpty()) {
                float durabilityPercent = calculateDurabilityPercent(armorItem);
                
                // Проверка на низкую прочность для уведомления
                if (this.breakAlert.isEnabled() && durabilityPercent < 20.0f && this.alertCooldown.finished(5000L)) {
                    if (!this.alertedSlots.getOrDefault(animIndex, false)) {
                        Rockstar.getInstance().getNotificationManager().addNotificationOther(
                            NotificationType.ERROR,
                            "Броня ломается",
                            String.format("Прочность: %.0f%%", durabilityPercent)
                        );
                        this.alertedSlots.put(animIndex, true);
                        this.alertCooldown.reset();
                    }
                } else if (durabilityPercent >= 20.0f) {
                    this.alertedSlots.put(animIndex, false);
                }
                
                // Сохраняем/настраиваем альфу перед рендером предмета
                float prevAlpha = moscow.rockstar.utility.render.ShaderColorHelper.getAlpha();
                
                if (this.showPercent.isEnabled()) {
                    // Обычный рендер предмета
                    moscow.rockstar.utility.render.ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha * a * hudAlpha);
                    context.drawBatchItem(armorItem, (int)(itemX + 1.5f), (int)(itemY + 1.5f));
                    moscow.rockstar.utility.render.ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha);
                    
                    // Цвет процентов в зависимости от прочности
                    ColorRGBA percentColor;
                    if (durabilityPercent >= 50.0f) {
                        percentColor = ColorRGBA.WHITE;
                    } else if (durabilityPercent >= 20.0f) {
                        percentColor = new ColorRGBA(255.0f, 220.0f, 0.0f, 255.0f); // Желтый
                    } else {
                        percentColor = new ColorRGBA(255.0f, 60.0f, 60.0f, 255.0f); // Красный
                    }
                    
                    // Рендер процентов с тенью для читаемости поверх иконки
                    String percentText = String.format("%.0f%%", durabilityPercent);
                    float textWidth = font.width(percentText);
                    float textX = itemX + (itemSize - textWidth) / 2.0f;
                    float textY = itemY + itemSize - font.height() - 1.5f;
                    
                    // Тень текста для читаемости
                    context.drawText(font, percentText, textX + 0.5f, textY + 0.5f, ColorRGBA.BLACK.withAlpha(180.0f * a));
                    context.drawText(font, percentText, textX, textY, percentColor.withAlpha(255.0f * a));
                } else {
                    // Обычный рендер предмета
                    moscow.rockstar.utility.render.ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha * a * hudAlpha);
                    context.drawBatchItem(armorItem, (int)(itemX + 1.5f), (int)(itemY + 1.5f));
                    moscow.rockstar.utility.render.ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha);
                }
            }
            
            visibleIndex++; // Увеличиваем индекс только для видимых элементов
        }
    }

    private float calculateDurabilityPercent(ItemStack itemStack) {
        if (itemStack.isEmpty() || !itemStack.isDamageableItem()) return 100.0f;
        int max = itemStack.getMaxDamage();
        int cur = itemStack.getDamageValue();
        return (float)(100.0 - (double)cur / (double)max * 100.0);
    }

    @Override
    public boolean show() {
        if (mc.player == null) return false;
        
        // Показываем только если есть хотя бы один предмет брони или открыт чат
        boolean hasArmor = false;
        for (ItemStack item : ClientEntityUtility.getArmorItems(mc.player)) {
            if (!item.isEmpty()) {
                hasArmor = true;
                break;
            }
        }
        
        return hasArmor || mc.screen instanceof ChatScreen;
    }
}


