/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.renderer.DefaultVertexFormat
 *  com.mojang.blaze3d.platform.InputConstants
 */
package moscow.rockstar.ui.menu.dropdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.modules.other.RussianRoulette;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.modules.modules.visuals.MenuModule;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.components.ThemesPanel;
import moscow.rockstar.ui.components.animated.AnimatedText;
import moscow.rockstar.ui.components.textfield.FieldAction;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.api.MenuCategory;
import moscow.rockstar.ui.menu.dropdown.components.MenuPanel;
import moscow.rockstar.ui.menu.dropdown.components.module.ModuleComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BezierSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BindSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.BooleanSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ButtonSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ColorSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.ModeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.RangeSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.SliderSettingComponent;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.StringSettingComponent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.Batching;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import moscow.rockstar.utility.game.KeyUtility;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class DropDownScreen
extends MenuScreen
implements IMinecraft {
    private final Animation searchAnimation = new Animation(300L, Easing.BAKEK);
    private final Animation appendingAnim = new Animation(300L, Easing.BAKEK);
    private boolean closing;
    private List<MenuPanel> panels = new ArrayList<MenuPanel>();
    private float panelWidth;
    private float panelHeight;
    private String desc = "";
    private AnimatedText descText;
    private final List<ColorPicker> colorPickers = new ArrayList<ColorPicker>();
    private TextField searchField;
    private ThemesPanel themesPanel;

    @Compile
    protected void init() {
        if (this.descText == null) {
            this.descText = new AnimatedText(Fonts.REGULAR.getFont(10.0f), 10.0f, 300L, Easing.BAKEK).centered();
        }
        this.closing = false;
        this.panelWidth = 115.0f;
        this.panelHeight = 240.0f;
        if (this.panels == null || this.panels.isEmpty()) {
            this.panels = new ArrayList<>(Arrays.stream(MenuCategory.values())
                    .filter(cat -> cat != MenuCategory.MODELS && cat != MenuCategory.EVENTS)
                    .map(MenuPanel::new)
                    .toList());
        }
        for (MenuPanel panel : this.panels) {
            panel.setWidth(this.panelWidth);
            panel.setHeight(this.panelHeight);
            panel.onInit();
        }
        if (this.searchField == null) {
            this.searchField = new TextField(Fonts.REGULAR.getFont(12.0f));
            HashMap<String, FieldAction> append = new HashMap<String, FieldAction>();
            for (Module module : Rockstar.getInstance().getModuleManager().getModules()) {
                FieldAction action = new FieldAction(module::toggle, () -> this.panels.forEach(panel -> panel.getModuleComponents().stream().filter(component -> component.getModule() == module).forEach(ModuleComponent::open)));
                append.put(module.getName().replace(" ", ""), action);
                append.put(module.getName(), action);
            }
            this.searchField.setAppend(append);
        }
        
        // Инициализация ThemesPanel справа снизу над чатом
        float themesPanelX = this.width - 110.0f;
        float themesPanelY = this.height - 34.0f;
        if (this.themesPanel == null) {
            this.themesPanel = new ThemesPanel(themesPanelX, themesPanelY);
            this.themesPanel.setOnColorPickerCreate(colorPicker -> {
                this.colorPickers.add(colorPicker);
            });
        } else {
            this.themesPanel.pos(themesPanelX, themesPanelY);
        }
        this.colorPickers.clear();
        
        super.init();
    }

    public void tick() {
        this.handleMovementKeys();
        super.tick();
    }

    @Override
    @Compile
    public void render(UIContext context) {
        this.menuAnimation.setEasing(Easing.LINEAR);
        this.menuAnimation.update(this.isClosing() ? 0.0f : 1.0f);
        this.menuAnimation.setDuration(this.isClosing() ? 300L : 500L);
        this.desc = "";
        float spacing = 10.0f;
        float x = ((float)this.width - (this.panelWidth + spacing) * (float)this.panels.size() + spacing) / 2.0f;
        float y = ((float)this.height - this.panelHeight) / 2.0f;
        context.pushMatrix();
        float offset = 0.0f;
        for (MenuPanel menuPanel : this.panels) {
            menuPanel.setX(MathUtility.interpolate(x + offset, (float)this.width / 2.0f - this.panelWidth / 2.0f, this.closing ? (double)(1.0f - this.menuAnimation.getRGB()) : 0.0));
            menuPanel.setY(y);
            menuPanel.setWidth(this.panelWidth);
            menuPanel.setHeight(this.panelHeight);
            menuPanel.updateScale(context);
            offset += this.panelWidth + spacing;
        }
        this.renderPanelsBackdropBlur(context);
        for (MenuPanel menuPanel : this.panels) {
            menuPanel.renderBlur(context);
        }
        for (MenuPanel menuPanel : this.panels) {
            menuPanel.render(context);
        }
        for (MenuPanel panel : this.panels) {
            panel.drawType(context);
        }
        for (MenuPanel panel : this.panels) {
            this.scissor(context, panel, () -> {
                FontBatching font = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.REGULAR);
                panel.drawRegular8(context);
                font.draw();
                panel.drawIcons(context);
                panel.drawSplit(context);
            });
        }
        context.popMatrix();
        if (this.menuAnimation.getRGB() < 0.5f) {
            this.desc = "";
        }
        this.searchAnimation.update(this.searchField.isFocused());
        float f = this.menuAnimation.getRGB() * this.searchAnimation.getRGB();
        if (f > 0.0f) {
            if (Interface.showMinimalizm()) {
                context.drawBlurredRect(this.searchField.x, this.searchField.y, this.searchField.getWidth(), this.searchField.getHeight(), 45.0f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * f * Interface.minimalizm()));
            }
            if (Interface.showGlass()) {
                context.drawLiquidGlass(this.searchField.x, this.searchField.y, this.searchField.getWidth(), this.searchField.getHeight(), 2.0f, 0.08f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * f * Interface.glass()));
            }
            boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
//             context.drawRoundedRect(this.searchField.x, this.searchField.y, this.searchField.getWidth(), this.searchField.getHeight(), BorderRadius.all(6.0f), Colors.getBackgroundColor().withAlpha(255.0f * (dark ? 0.9f - 0.7f * Interface.glass() : 0.7f) * f));
            this.searchField.set((float)this.width / 2.0f - this.searchField.getWidth() / 2.0f, (float)(this.height - 20) - 20.0f * f, 100.0f, 20.0f);
            this.searchField.setAlpha(f);
            this.searchField.setTextColor(Colors.getTextColor());
            this.searchField.render(context);
            this.appendingAnim.update(!this.searchField.getAppending().isBlank());
            context.drawCenteredText(Fonts.MEDIUM.getFont(11.0f), Localizator.translate("search.tooltip.tab"), (float)this.width / 2.0f, (float)(this.height - 65) - 10.0f * f * this.appendingAnim.getRGB(), ColorRGBA.WHITE.withAlpha(150.0f * f * this.appendingAnim.getRGB()));
            context.drawCenteredText(Fonts.MEDIUM.getFont(11.0f), Localizator.translate("search.tooltip.enter"), (float)this.width / 2.0f, (float)(this.height - 50) - 10.0f * f * this.appendingAnim.getRGB(), ColorRGBA.WHITE.withAlpha(150.0f * f * this.appendingAnim.getRGB()));
        } else {
            this.searchField.clear();
        }
        context.drawCenteredText(Fonts.MEDIUM.getFont(11.0f), Localizator.translate("search.tooltip"), (float)this.width / 2.0f, (float)(this.height - 20) - 10.0f * this.menuAnimation.getRGB() * (1.0f - this.searchAnimation.getRGB()), ColorRGBA.WHITE.withAlpha(150.0f * this.menuAnimation.getRGB() * (1.0f - this.searchAnimation.getRGB())));
        this.descText.pos((float)this.width / 2.0f, (float)this.height / 2.0f - 150.0f);
        if (!this.desc.contains(".description")) {
            this.descText.update(this.desc);
            this.descText.render(context);
        }
        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.render(context);
            if (DropDownScreen.mc.screen instanceof DropDownScreen) continue;
            colorPicker.setShowing(false);
        }
        
        // Рендерим ThemesPanel
        if (this.themesPanel != null) {
            this.themesPanel.render(context);
        }
        
        this.colorPickers.removeIf(popup -> popup.getAnimation().getValue() == 0.0f && !popup.isShowing());
        RussianRoulette russianRoulette = Rockstar.getInstance().getModuleManager().getModule(RussianRoulette.class);
        if (russianRoulette.isEnabled()) {
            if (russianRoulette.getQrTexture() == null) {
                return;
            }
            if (russianRoulette.getQrAnimation().getValue() == 0.0f && russianRoulette.isQrRemoving()) {
                return;
            }
            float scale = 180.0f;
            float xQR = ((float)mc.getWindow().getGuiScaledWidth() - scale) / 2.0f;
            float yQR = ((float)mc.getWindow().getGuiScaledHeight() - scale) / 2.0f;
            context.drawTexture(russianRoulette.getQrTexture(), xQR, yQR, scale, scale, Colors.WHITE.withAlpha((int)(255.0f * russianRoulette.getQrAnimation().getValue())));
        }
    }

    private void renderPanelsBackdropBlur(UIContext context) {
        if (!Interface.showMinimalizm() || this.panels.isEmpty()) {
            return;
        }
        float alpha = this.menuAnimation.getRGB();
        if (alpha <= 0.01f) {
            return;
        }
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        for (MenuPanel panel : this.panels) {
            minX = Math.min(minX, panel.getX());
            minY = Math.min(minY, panel.getY());
            maxX = Math.max(maxX, panel.getX() + panel.getWidth());
            maxY = Math.max(maxY, panel.getY() + panel.getHeight());
        }
        context.drawBlurredRect(minX, minY, maxX - minX, maxY - minY, 45.0f, 10.0f, BorderRadius.all(10.0f),
                ColorRGBA.WHITE.withAlpha(255.0f * alpha * Interface.minimalizm()));
    }

    @Compile
    private void handleMovementKeys() {
        KeyMapping[] movementKeys;
        if (DropDownScreen.mc.player == null || this.isTyping()) {
            return;
        }
        for (KeyMapping key : movementKeys = new KeyMapping[]{DropDownScreen.mc.options.keyUp, DropDownScreen.mc.options.keyDown, DropDownScreen.mc.options.keyLeft, DropDownScreen.mc.options.keyRight, DropDownScreen.mc.options.keyJump}) {
            key.setDown(KeyUtility.isMappingPressed(key));
        }
        if (DropDownScreen.mc.player.getAbilities().flying) {
            DropDownScreen.mc.options.keyShift.setDown(KeyUtility.isMappingPressed(DropDownScreen.mc.options.keyShift));
        }
    }

    private boolean isTyping() {
        return DropDownScreen.mc.screen != null && TextField.LAST_FIELD != null && TextField.LAST_FIELD.isFocused();
    }

    public boolean isBindingModule() {
        return this.panels.stream().flatMap(panel -> panel.getModuleComponents().stream()).anyMatch(ModuleComponent::isBindingMode);
    }

    private void scissor(UIContext context, MenuPanel panel, Runnable runnable) {
        panel.scale(context);
        panel.push(context);
        runnable.run();
        ScissorUtility.pop();
        RenderUtility.end(context.pose());
    }

    @Override
    @Compile
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (Rockstar.getInstance().getHud().getIsland().handleClick((float)mouseX, (float)mouseY, button.getButtonIndex())) {
            return;
        }
        
        // Обработка кликов по ThemesPanel
        if (this.themesPanel != null && this.themesPanel.isHovered(mouseX, mouseY)) {
            this.themesPanel.onMouseClicked(mouseX, mouseY, button);
            return;
        }
        
        for (ColorPicker colorPicker : this.colorPickers) {
            boolean isPick = colorPicker.isPick();
            colorPicker.onMouseClicked(mouseX, mouseY, button);
            if (colorPicker.isHovered(mouseX, mouseY) || isPick) {
                return;
            }
            colorPicker.setShowing(false);
        }
        for (MenuPanel panel : this.panels) {
            if (!panel.isHovered(mouseX, mouseY)) continue;
            panel.onMouseClicked(mouseX, mouseY, button);
        }
        if (this.searchField.isFocused() && button != MouseButton.MIDDLE) {
            this.searchField.onMouseClicked(mouseX, mouseY, button);
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    @Compile
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.onMouseReleased(mouseX, mouseY, button);
        }
        for (MenuPanel panel : this.panels) {
            panel.onMouseReleased(mouseX, mouseY, button);
        }
        if (this.searchField.isFocused()) {
            this.searchField.onMouseReleased(mouseX, mouseY, button);
        }
        super.onMouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (MenuPanel panel : this.panels) {
            panel.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Compile
    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        int modifiers = event.modifiers();
        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.onKeyPressed(keyCode, scanCode, modifiers);
        }
        if (this.searchField != null && !this.searchField.isFocused() && KeyUtility.hasControlDown() && keyCode == 70) {
            this.searchField.setFocused(true);
        }
        for (MenuPanel panel : this.panels) {
            panel.onKeyPressed(keyCode, scanCode, modifiers);
        }
        if (this.searchField.isFocused() && !this.isBindingModule()) {
            this.searchField.onKeyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(event);
    }

    @Compile
    @Override
    public boolean charTyped(CharacterEvent event) {
        char chr = (char)event.codepoint();
        if (this.searchField.isFocused() && !this.isBindingModule()) {
            this.searchField.charTyped(chr, 0);
        }
        for (MenuPanel panel : this.panels) {
            panel.charTyped(chr, 0);
        }
        return super.charTyped(event);
    }

    @Compile
    public void close() {
        this.closing = true;
        Rockstar.getInstance().getModuleManager().getModule(MenuModule.class).disable();
        Sounds soundsModule = Rockstar.getInstance().getModuleManager().getModule(Sounds.class);
        if (soundsModule.isEnabled()) {
            ClientSounds.CLICKGUI_OPEN.play(soundsModule.getVolume().getCurrentValue(), 1.0f);
        }
        Rockstar.getInstance().getFileManager().writeFile("client");
        if (Rockstar.getInstance().getConfigManager().getCurrent() != null) {
            Rockstar.getInstance().getConfigManager().getCurrent().save();
        }
        if (TextField.LAST_FIELD != null) {
            TextField.LAST_FIELD.setFocused(false);
        }
        onClose();
    }

    public boolean shouldPause() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void removed() {
        this.colorPickers.clear();
        if (this.panels != null) {
            this.panels.clear();
        }
        if (TextField.LAST_FIELD != null) {
            TextField.LAST_FIELD.setFocused(false);
            TextField.LAST_FIELD = null;
        }
        super.removed();
    }


    @Generated
    public Animation getSearchAnimation() {
        return this.searchAnimation;
    }

    @Generated
    public Animation getAppendingAnim() {
        return this.appendingAnim;
    }

    @Override
    @Generated
    public boolean isClosing() {
        return this.closing;
    }

    @Generated
    public List<MenuPanel> getPanels() {
        return this.panels;
    }

    @Generated
    public float getPanelWidth() {
        return this.panelWidth;
    }

    @Generated
    public float getPanelHeight() {
        return this.panelHeight;
    }

    @Generated
    public String getDesc() {
        return this.desc;
    }

    @Generated
    public AnimatedText getDescText() {
        return this.descText;
    }

    @Generated
    public List<ColorPicker> getColorPickers() {
        return this.colorPickers;
    }

    @Override
    @Generated
    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    @Generated
    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Generated
    public TextField getSearchField() {
        return this.searchField;
    }

    static {
        new MenuPanel(null);
        new BezierSettingComponent(null, null);
        new BindSettingComponent(null, null);
        new BooleanSettingComponent(null, null);
        new ModeSettingComponent(null, null);
        new ButtonSettingComponent(null, null);
        new ColorSettingComponent(null, null);
        new StringSettingComponent(null, null);
        new RangeSettingComponent(null, null);
        new SliderSettingComponent(null, null);
    }
}
