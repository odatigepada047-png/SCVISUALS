/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 */
package moscow.rockstar.ui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;

import moscow.rockstar.systems.theme.CustomTheme;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.modern.ModernScreen;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;

public class ThemesPanel
extends CustomComponent
implements IScaledResolution {
    private final Animation animation = new Animation(500L, 0.0f, Easing.LINEAR);
    private final Animation expandAnimation = new Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final Animation addHoverAnimation = new Animation(200L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private boolean showing;
    private boolean expanded;
    private Consumer<ColorPicker> onColorPickerCreate;
    
    public ThemesPanel(float x, float y) {
        super(x, y, 100.0f, 24.0f);
        this.showing = true;
    }
    
    public void setOnColorPickerCreate(Consumer<ColorPicker> callback) {
        this.onColorPickerCreate = callback;
    }

    private float calculateCurrentHeight() {
        float itemHeight = 26.0f;
        float padding = 6.0f;
        List<CustomTheme> themes = Rockstar.getInstance().getThemeManager().getCustomThemes();
        
        float themesHeight = 0;
        for (CustomTheme theme : themes) {
            themesHeight += itemHeight * theme.getAnimation().getValue();
        }
        
        float expandedHeight = 24.0f + themesHeight + itemHeight + padding * 2;
        return 24.0f + (expandedHeight - 24.0f) * this.expandAnimation.getRGB();
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        float currentHeight = calculateCurrentHeight();
        float renderY = this.y - (currentHeight - 24.0f);
        return mouseX >= this.x && mouseX <= this.x + this.width && 
               mouseY >= renderY && mouseY <= renderY + currentHeight;
    }

    @Override
    protected void renderComponent(UIContext context) {
        // Синхронизируем состояние видимости с закрытием меню
        if (mc.screen instanceof MenuScreen menu) {
            this.showing = !menu.isClosing();
        }
        
        this.animation.update(this.showing);
        this.expandAnimation.update(this.expanded);
        float alpha = this.animation.getRGB();
        
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        ColorRGBA bgColor = Colors.getBackgroundColor().withAlpha(255.0f * (dark ? 0.95f : 0.95f));
        ColorRGBA additionalColor = Colors.getAdditionalColor().withAlpha(255.0f * (dark ? 0.4f : 0.4f));
        
        float itemHeight = 26.0f;
        float padding = 6.0f;
        float currentHeight = calculateCurrentHeight();
        float renderY = this.y - (currentHeight - 24.0f);
        
        context.drawShadow(this.x - 5.0f, renderY - 5.0f, this.width + 10.0f, currentHeight + 10.0f, 20.0f, BorderRadius.all(8.0f), ColorRGBA.BLACK.withAlpha(255.0f * 0.25f * alpha));
        
        if (Interface.showMinimalizm()) {
            context.drawBlurredRect(this.x, renderY, this.width, currentHeight, 45.0f, 7.0f, BorderRadius.all(8.0f), ColorRGBA.WHITE.withAlpha(255.0f * alpha * Interface.minimalizm()));
        }
        
        if (Interface.showGlass()) {
            context.drawLiquidGlass(this.x, renderY, this.width, currentHeight, 7.0f, 0.05f, BorderRadius.all(8.0f), ColorRGBA.WHITE.withAlpha(255.0f * alpha * Interface.glass()));
        }
        
        context.drawSquircle(this.x, renderY, this.width, currentHeight, 7.0f, BorderRadius.all(8.0f), bgColor.withAlpha(255.0f * alpha));
        
        List<CustomTheme> themes = Rockstar.getInstance().getThemeManager().getCustomThemes();
        
        // Моментальная подхватка цвета из Color Picker для тем
        if (mc.screen instanceof MenuScreen screen) {
            List<ColorPicker> pickers = null;
            if (screen instanceof ModernScreen modern) {
                pickers = modern.getColorPickers();
            } else if (screen instanceof DropDownScreen dropDown) {
                pickers = dropDown.getColorPickers();
            }
            if (pickers != null) {
                for (ColorPicker picker : pickers) {
                    if (picker.isShowing()) {
                        for (CustomTheme theme : themes) {
                            if (picker.getTitle().equals(theme.getName())) {
                                ColorRGBA currentColor = picker.built();
                                if (!theme.getAccentColor().equals(currentColor)) {
                                    theme.setAccentColor(currentColor);
                                    Rockstar.getInstance().getThemeManager().setAccentColor(currentColor);
                                    Rockstar.getInstance().getThemeManager().saveTheme(theme);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        ColorRGBA currentAccent = Rockstar.getInstance().getThemeManager().getAccentColor();

        // Рендерим темы если панель раскрыта
        if (this.expandAnimation.getRGB() > 0.01f) {
            float yOffset = padding;
            float expandValue = this.expandAnimation.getRGB();
            
            // Удаляем темы которые закончили анимацию исчезновения
            themes.removeIf(theme -> !theme.isShowing() && theme.getAnimation().getValue() == 0.0f);
            
            for (CustomTheme theme : themes) {
                float themeY = renderY + yOffset;
                
                theme.updateAnimation();
                float themeAnim = theme.getAnimation().getValue();
                
                if (themeAnim > 0.001f) {
                    theme.getHoverAnimation().update(GuiUtility.isHovered(this.x + 6.0f, themeY, this.width - 12.0, itemHeight, context.getMouseX(), context.getMouseY()));
                    
                    float itemAlpha = expandValue * alpha * themeAnim;
                    float hoverAlpha = 0.3f + 0.2f * theme.getHoverAnimation().getValue();
                    
                    // Проверка на активность
                    boolean isActive = theme.getAccentColor().equals(currentAccent);
                    if (isActive) {
                        context.drawRoundedRect(this.x + 6.0f, themeY, this.width - 12.0f, itemHeight, BorderRadius.all(5.0f), Colors.getAccentColor().withAlpha(100.0f * itemAlpha));
                    }
                    
                    // Фон темы с hover эффектом
                    context.drawRoundedRect(this.x + 6.0f, themeY, this.width - 12.0f, itemHeight, BorderRadius.all(5.0f), additionalColor.withAlpha(255.0f * hoverAlpha * itemAlpha));
                    
                    // Цветной квадратик
                    context.drawRoundedRect(this.x + 10.0f, themeY + 5.0f, 16.0f, 16.0f, BorderRadius.all(4.0f), theme.getAccentColor().withAlpha(255.0f * itemAlpha));
                    
                    // Название темы
                    context.drawText(Fonts.MEDIUM.getFont(7.0f), theme.getName(), this.x + 30.0f, themeY + 10.0f, Colors.getTextColor().withAlpha(255.0f * itemAlpha));
                    
                    // Иконка корзины (только для кастомных тем, кроме Purple)
                    if (!theme.getName().equalsIgnoreCase("Purple") && theme.getHoverAnimation().getValue() > 0.01f) {
                        float trashX = this.x + this.width - 22.0f;
                        float trashY = themeY + 8.0f;
                        float trashSize = 10.0f;
                        float hoverAnim = theme.getHoverAnimation().getValue();
                        
                        boolean isTrashHovered = GuiUtility.isHovered(trashX - 2.0f, trashY - 2.0f, trashSize + 4.0f, trashSize + 4.0f, context.getMouseX(), context.getMouseY());
                        ColorRGBA trashColor = isTrashHovered ? Colors.RED : Colors.getHudIconColor(255.0f);
                        context.drawTexture(Rockstar.id("icons/trash.png"), trashX, trashY, trashSize, trashSize, trashColor.withAlpha((int)(255.0f * itemAlpha * hoverAnim)));
                        
                        if (isTrashHovered) {
                            CursorUtility.set(CursorType.HAND);
                        }
                    }
                    
                    if (GuiUtility.isHovered(this.x + 6.0f, themeY, this.width - 12.0, itemHeight, context.getMouseX(), context.getMouseY())) {
                        CursorUtility.set(CursorType.HAND);
                    }
                }
                
                yOffset += itemHeight * expandValue * themeAnim;
            }
            
            // Кнопка "Add Theme"
            float addY = renderY + yOffset;
            boolean isAddHovered = GuiUtility.isHovered(this.x + 6.0f, addY, this.width - 12.0, itemHeight, context.getMouseX(), context.getMouseY());
            this.addHoverAnimation.update(isAddHovered);
            
            float addHoverAlpha = 0.3f + 0.2f * this.addHoverAnimation.getRGB();
            context.drawRoundedRect(this.x + 6.0f, addY, this.width - 12.0f, itemHeight, BorderRadius.all(5.0f), additionalColor.withAlpha(255.0f * addHoverAlpha * expandValue * alpha));
            
            if (this.addHoverAnimation.getRGB() > 0.01f) {
                context.drawRoundedRect(this.x + 10.0f, addY + 5.0f, 16.0f, 16.0f, BorderRadius.all(4.0f), Colors.getAccentColor().withAlpha(127.5f * expandValue * alpha * this.addHoverAnimation.getRGB()));
                context.drawTexture(Rockstar.id("icons/colorpicker/plus.png"), this.x + 12.0f, addY + 7.0f, 12.0f, 12.0f, Colors.getHudIconColor(255.0f * expandValue * alpha * this.addHoverAnimation.getRGB()));
            }
            
            context.drawText(Fonts.MEDIUM.getFont(7.0f), "Add Theme", this.x + 30.0f, addY + 10.0f, Colors.getTextColor().withAlpha(255.0f * expandValue * alpha));
            
            if (isAddHovered) {
                CursorUtility.set(CursorType.HAND);
            }
        }
        
        float headerY = renderY + currentHeight - 24.0f;
        if (this.expandAnimation.getRGB() > 0.01f) {
            context.drawRect(this.x + 8.0f, headerY - 1.0f, this.width - 16.0f, 1.0f, Colors.getAdditionalColor().withAlpha(127.5f * this.expandAnimation.getRGB() * alpha));
        }
        
        context.drawText(Fonts.MEDIUM.getFont(8.0f), "Themes", this.x + 10.0f, headerY + 9.0f, Colors.getTextColor().withAlpha(255.0f * alpha));
        
        float arrowRotation = -90.0f + this.expandAnimation.getRGB() * 180.0f;
        RenderUtility.rotate(context.pose(), this.x + this.width - 14.0f, headerY + 12.0f, arrowRotation);
        context.drawTexture(Rockstar.id("icons/arrow.png"), this.x + this.width - 18.0f, headerY + 8.0f, 8.0f, 8.0f, Colors.getHudIconColor(255.0f * alpha));
        RenderUtility.end(context.pose());
        
        if (GuiUtility.isHovered(this.x, headerY, this.width, 24.0, context.getMouseX(), context.getMouseY())) {
            CursorUtility.set(CursorType.HAND);
        }
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.animation.getRGB() < 0.5f) return;
        
        float itemHeight = 26.0f;
        float padding = 6.0f;
        List<CustomTheme> themes = Rockstar.getInstance().getThemeManager().getCustomThemes();
        float currentHeight = calculateCurrentHeight();
        float renderY = this.y - (currentHeight - 24.0f);
        float headerY = renderY + currentHeight - 24.0f;
        
        if (this.isInBounds(mouseX, mouseY, this.x, headerY, this.width, 24.0f)) {
            if (button.getButtonIndex() == 0) {
                this.expanded = !this.expanded;
            }
            return;
        }
        
        if (!this.expanded || this.expandAnimation.getRGB() < 0.5f) return;
        
        float yOffset = padding;
        float expandValue = this.expandAnimation.getRGB();
        
        for (int i = 0; i < themes.size(); i++) {
            CustomTheme theme = themes.get(i);
            if (!theme.isShowing()) continue;
            
            float themeY = renderY + yOffset;
            
            if (this.isInBounds(mouseX, mouseY, this.x + 6.0f, themeY, this.width - 12.0f, itemHeight)) {
                if (button.getButtonIndex() == 0) {
                    // Если кликнули на корзину (удаление темы)
                    float trashX = this.x + this.width - 22.0f;
                    float trashY = themeY + 8.0f;
                    float trashSize = 10.0f;
                    if (!theme.getName().equalsIgnoreCase("Purple") && mouseX >= trashX - 2.0f && mouseX <= trashX + trashSize + 2.0f && mouseY >= trashY - 2.0f && mouseY <= trashY + trashSize + 2.0f) {
                        theme.setShowing(false);
                        
                        // Закрываем колорпикер для этой темы если он открыт (анимированно)
                        if (mc.screen instanceof MenuScreen screen) {
                            if (screen instanceof ModernScreen modern) {
                                modern.getColorPickers().stream()
                                    .filter(picker -> picker.getTitle().equals(theme.getName()))
                                    .forEach(picker -> picker.setShowing(false));
                            } else if (screen instanceof DropDownScreen dropDown) {
                                dropDown.getColorPickers().stream()
                                    .filter(picker -> picker.getTitle().equals(theme.getName()))
                                    .forEach(picker -> picker.setShowing(false));
                            }
                        }
                        
                        Rockstar.getInstance().getThemeManager().deleteTheme(theme);
                        Rockstar.getInstance().getFileManager().writeFile("client");
                        return;
                    }

                    // Если кликнули на цветной квадратик (от x + 10.0f до x + 26.0f)
                    if (mouseX >= this.x + 10.0f && mouseX <= this.x + 26.0f && this.onColorPickerCreate != null) {
                        float pickerY = renderY - 140.0f; // Примерная высота колорпикера
                        ColorPicker picker = new ColorPicker(this.x, pickerY, 2.0f, false, theme.getAccentColor(), theme.getName());
                        picker.setOnClose(() -> {
                            ColorRGBA selectedColor = picker.built();
                            theme.setAccentColor(selectedColor);
                            Rockstar.getInstance().getThemeManager().setAccentColor(selectedColor);
                            Rockstar.getInstance().getThemeManager().saveTheme(theme);
                            Rockstar.getInstance().getFileManager().writeFile("client");
                        });
                        this.onColorPickerCreate.accept(picker);
                    } else {
                        // Просто применяем
                        Rockstar.getInstance().getThemeManager().setAccentColor(theme.getAccentColor());
                    }
                }
                return;
            }
            yOffset += itemHeight * expandValue * theme.getAnimation().getValue();
        }
        
        float addY = renderY + yOffset;
        if (this.isInBounds(mouseX, mouseY, this.x + 6.0f, addY, this.width - 12.0f, itemHeight)) {
            if (button.getButtonIndex() == 0 && this.onColorPickerCreate != null) {
                // Создаем тему сразу
                String themeName = "Theme " + (themes.size() + 1);
                ColorRGBA defaultColor = new ColorRGBA(151.0f, 71.0f, 255.0f);
                CustomTheme newTheme = new CustomTheme(themeName, defaultColor);
                themes.add(newTheme);
                
                // Применяем её сразу
                Rockstar.getInstance().getThemeManager().setAccentColor(defaultColor);
                Rockstar.getInstance().getThemeManager().saveTheme(newTheme);
                
                // Открываем колорпикер НАД темой
                float pickerY = renderY - 140.0f; // Примерная высота колорпикера
                ColorPicker picker = new ColorPicker(this.x, pickerY, 2.0f, false, defaultColor, themeName);
                
                picker.setOnClose(() -> {
                    ColorRGBA selectedColor = picker.built();
                    newTheme.setAccentColor(selectedColor);
                    Rockstar.getInstance().getThemeManager().setAccentColor(selectedColor);
                    Rockstar.getInstance().getThemeManager().saveTheme(newTheme);
                    Rockstar.getInstance().getFileManager().writeFile("client");
                });
                
                // Обновляем цвет в реальном времени при изменении (если ColorPicker поддерживает это)
                // Но так как у нас нет callback на изменение, цвет обновится при закрытии или если ColorPicker вызывает onClose при каждом изменении
                
                this.onColorPickerCreate.accept(picker);
            }
            return;
        }
    }
    
    private boolean isInBounds(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    public void addTheme(String name, ColorRGBA color) {
        Rockstar.getInstance().getThemeManager().getCustomThemes().add(new CustomTheme(name, color));
    }

    public Animation getAnimation() {
        return this.animation;
    }

    public boolean isShowing() {
        return this.showing;
    }

    public void setShowing(boolean showing) {
        this.showing = showing;
    }
}
