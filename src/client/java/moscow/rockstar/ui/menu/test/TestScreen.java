package moscow.rockstar.ui.menu.test;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.config.ConfigFile;
import moscow.rockstar.systems.localization.Language;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.modules.modules.visuals.MenuModule;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.ui.components.ColorPicker;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.api.MenuCategory;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.KeyUtility;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.obj.Rect;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import java.util.*;

public class TestScreen extends MenuScreen implements IMinecraft, IScaledResolution {

    private final Rect menuWindow;
    private float dragX;
    private float dragY;
    private boolean drag;
    private final ScrollHandler scrollHandler = new ScrollHandler();
    private int activeTab = 0; // 0: Combat, 1: Movement, 2: Player, 3: Visuals, 4: Misc, 5: Configs, 6: Themes

    private final List<ColorPicker> colorPickers = new LinkedList<>();
    private final TextField searchField;
    private final TextField newConfigField;
    private boolean searchFocused = false;
    private final Map<moscow.rockstar.systems.setting.Setting, Boolean> expandedSettings = new HashMap<>();
    private Module bindingModule = null;

    // Draggable Slider context
    private moscow.rockstar.systems.setting.settings.SliderSetting draggingSlider = null;

    // Static assets placeholders
    private static final Identifier SEARCH_ICON = Rockstar.id("icons/search.png");
    private static final Identifier CLOSE_ICON = Rockstar.id("icons/close.png");
    private static final Identifier ARROW_ICON = Rockstar.id("icons/arrow.png");
    private static final Identifier CONFIG_ICON = Rockstar.id("icons/hud/drag.png"); // using standard drag icon for config

    public TestScreen() {
        float width = 600.0f;
        float height = 370.0f;
        this.menuWindow = new Rect(
                sr.getGuiScaledWidth() / 2.0f - width / 2.0f,
                sr.getGuiScaledHeight() / 2.0f - height / 2.0f,
                width,
                height
        );
        this.searchField = new TextField(Fonts.MEDIUM.getFont(6.0f));
        this.searchField.setPreview("Search...");
        this.newConfigField = new TextField(Fonts.MEDIUM.getFont(7.0f));
        this.newConfigField.setPreview("Config Name...");
    }

    @Override
    protected void init() {
        this.closing = false;
        float width = 600.0f;
        float height = 370.0f;
        if (this.menuWindow != null) {
            this.menuWindow.setX(sr.getGuiScaledWidth() / 2.0f - width / 2.0f);
            this.menuWindow.setY(sr.getGuiScaledHeight() / 2.0f - height / 2.0f);
        }
        this.colorPickers.clear();
        this.expandedSettings.clear();
        this.bindingModule = null;
        this.draggingSlider = null;
        super.init();
    }

    @Override
    public void tick() {
        this.handleMovementKeys();
        super.tick();
    }

    private void handleMovementKeys() {
        if (mc.player == null || this.isTyping()) {
            return;
        }
        KeyMapping[] movementKeys = new KeyMapping[]{
                mc.options.keyUp, mc.options.keyDown,
                mc.options.keyLeft, mc.options.keyRight,
                mc.options.keyJump
        };
        for (KeyMapping key : movementKeys) {
            key.setDown(KeyUtility.isMappingPressed(key));
        }
        if (mc.player.getAbilities().flying) {
            mc.options.keyShift.setDown(KeyUtility.isMappingPressed(mc.options.keyShift));
        }
    }

    private boolean isTyping() {
        return (this.searchField.isFocused()) || (this.newConfigField.isFocused());
    }

    @Override
    @Compile
    public void render(UIContext context) {
        this.menuAnimation.update(this.closing ? 0.0f : 1.0f);
        this.menuAnimation.setEasing(!this.closing ? Easing.BAKEK : Easing.BAKEK_BACK);
        this.menuAnimation.setDuration(400L);
        this.scrollHandler.update();

        if (this.drag) {
            this.menuWindow.setX((float) context.getMouseX() - this.dragX);
            this.menuWindow.setY((float) context.getMouseY() - this.dragY);
        }

        // Handle dragging slider value update
        if (this.draggingSlider != null) {
            if (GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS) {
                // Update based on mouse coordinate relative to slider layout
                // We'll calculate slider hit coordinates during drawing or retrieve last computed coordinates.
                // For simplicity, we can do value adjustment in mouseDragged or mouseClicked.
            } else {
                this.draggingSlider = null;
            }
        }

        float alpha = Math.min(1.0f, this.menuAnimation.getRGB());
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;

        // Apply scale transition animation
        RenderUtility.scale(context.pose(), this.menuWindow.x + this.menuWindow.getWidth() / 2.0f, this.menuWindow.y + this.menuWindow.getHeight() / 2.0f, 0.5f + 0.5f * this.menuAnimation.getRGB());

        // Blur background & shadow
        context.drawBlurredRect(this.menuWindow.x, this.menuWindow.y, this.menuWindow.getWidth(), this.menuWindow.getHeight(), 45.0f, 5.0f, BorderRadius.all(12.0f), Colors.WHITE);
        context.drawSquircle(
                this.menuWindow.x, this.menuWindow.y,
                this.menuWindow.getWidth(), this.menuWindow.getHeight(),
                5.0f, BorderRadius.all(12.0f),
                dark ? new ColorRGBA(10.0f, 13.0f, 20.0f, 255.0f * alpha * 0.98f) : new ColorRGBA(240.0f, 240.0f, 245.0f, 255.0f * alpha * 0.98f)
        );

        // Render Top Bar
        this.renderTopBar(context, alpha);

        // Render Content Area with Scissor
        float contentX = this.menuWindow.x + 10.0f;
        float contentY = this.menuWindow.y + 35.0f;
        float contentWidth = this.menuWindow.getWidth() - 20.0f;
        float contentHeight = this.menuWindow.getHeight() - 45.0f;

        ScissorUtility.push(context.pose(), contentX, contentY, contentWidth, contentHeight);

        if (this.activeTab >= 0 && this.activeTab <= 4) {
            this.renderModulesGrid(context, contentX, contentY, contentWidth, contentHeight, alpha);
        } else if (this.activeTab == 5) {
            this.renderConfigsTab(context, contentX, contentY, contentWidth, contentHeight, alpha);
        } else if (this.activeTab == 6) {
            this.renderThemesTab(context, contentX, contentY, contentWidth, contentHeight, alpha);
        }

        ScissorUtility.pop();

        RenderUtility.end(context.pose());

        // Render color pickers in screen space
        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.render(context);
        }

        this.colorPickers.removeIf(colorPicker -> colorPicker.getAnimation().getValue() == 0.0f && !colorPicker.isShowing());
    }

    private void renderTopBar(UIContext context, float alpha) {
        float x = this.menuWindow.x;
        float y = this.menuWindow.y;
        float width = this.menuWindow.getWidth();

        // 1. Draw Void/Icon Logo on left
        context.drawRoundedRect(x + 12.0f, y + 8.0f, 12.0f, 12.0f, BorderRadius.all(3.0f), Colors.getAccentColor().withAlpha(alpha * 255.0f));
        context.drawCenteredText(Fonts.BOLD.getFont(8.0f), "V", x + 18.0f, y + 10.5f, ColorRGBA.WHITE.withAlpha(alpha * 255.0f));

        // 2. Draw Navigation Tabs Center
        String[] tabs = new String[]{"Combat", "Movement", "Player", "Visuals", "Misc", "Configs", "Themes"};
        float tabsStartX = x + width / 2.0f - 150.0f;
        for (int i = 0; i < tabs.length; i++) {
            float tabX = tabsStartX + i * 40.0f;
            float tabWidth = 36.0f;
            boolean hovered = GuiUtility.isHovered(tabX, y + 4.0f, tabWidth, 20.0f, context);
            boolean selected = (i == this.activeTab);

            ColorRGBA textColor = selected ? Colors.getAccentColor() : (hovered ? Colors.getTextColor() : Colors.getTextColor().withAlpha(120.0f));
            if (i == 6) {
                // paint brush icon for themes
                context.drawCenteredText(Fonts.MEDIUM.getFont(7.0f), "🎨", tabX + tabWidth / 2.0f, y + 10.0f, textColor.withAlpha(alpha * 255.0f));
            } else if (i == 5) {
                // configs icon
                context.drawCenteredText(Fonts.MEDIUM.getFont(7.0f), "📁", tabX + tabWidth / 2.0f, y + 10.0f, textColor.withAlpha(alpha * 255.0f));
            } else {
                context.drawCenteredText(Fonts.MEDIUM.getFont(7.0f), tabs[i], tabX + tabWidth / 2.0f, y + 10.0f, textColor.withAlpha(alpha * 255.0f));
            }

            // Draw line under active tab
            if (selected) {
                context.drawRect(tabX + 4.0f, y + 22.0f, tabWidth - 8.0f, 1.5f, Colors.getAccentColor().withAlpha(alpha * 255.0f));
            }

            if (hovered) {
                CursorUtility.set(CursorType.HAND);
            }
        }

        // 3. Draw Search and Right actions
        float actionsEndX = x + width - 15.0f;

        // Message bubble chat icon (muted, non-clickable)
        context.drawRightText(Fonts.MEDIUM.getFont(9.0f), "💬", actionsEndX, y + 9.0f, Colors.getTextColor().withAlpha(alpha * 60.0f));
        actionsEndX -= 16.0f;

        // Language switcher icon
        boolean langHovered = GuiUtility.isHovered(actionsEndX - 10.0f, y + 6.0f, 12.0f, 12.0f, context);
        context.drawRightText(Fonts.MEDIUM.getFont(9.0f), "🌐", actionsEndX, y + 9.0f, (langHovered ? Colors.getAccentColor() : Colors.getTextColor()).withAlpha(alpha * 255.0f));
        if (langHovered) {
            CursorUtility.set(CursorType.HAND);
        }
        actionsEndX -= 18.0f;

        // Search icon
        boolean searchHovered = GuiUtility.isHovered(actionsEndX - 10.0f, y + 6.0f, 12.0f, 12.0f, context);
        context.drawRightText(Fonts.MEDIUM.getFont(9.0f), "🔍", actionsEndX, y + 9.0f, (searchHovered || this.searchFocused ? Colors.getAccentColor() : Colors.getTextColor()).withAlpha(alpha * 255.0f));
        if (searchHovered) {
            CursorUtility.set(CursorType.HAND);
        }
        actionsEndX -= 15.0f;

        // If search is active/focused, draw text field next to it
        if (this.searchFocused) {
            float fieldWidth = 60.0f;
            this.searchField.set(actionsEndX - fieldWidth, y + 6.0f, fieldWidth, 12.0f);
            this.searchField.setTextColor(Colors.getTextColor().withAlpha(alpha * 200.0f));
            this.searchField.render(context);
        }
    }

    private void renderModulesGrid(UIContext context, float startX, float startY, float width, float height, float alpha) {
        // Collect modules of current category
        ModuleCategory currentCategory = this.getCategoryFromTab(this.activeTab);
        if (currentCategory == null) return;

        List<Module> filteredModules = Rockstar.getInstance().getModuleManager().getModules().stream()
                .filter(module -> module.getCategory() == currentCategory)
                .filter(module -> {
                    String query = this.searchField.getBuiltText().toLowerCase().trim();
                    return query.isEmpty() || module.getName().toLowerCase().contains(query) || module.getDescription().toLowerCase().contains(query);
                })
                .sorted((m1, m2) -> {
                    // Sort favorites first
                    if (m1.isFavorite() && !m2.isFavorite()) return -1;
                    if (!m1.isFavorite() && m2.isFavorite()) return 1;
                    return m1.getName().compareTo(m2.getName());
                })
                .toList();

        // 3 column waterfall masonry layout
        float colWidth = (width - 16.0f) / 3.0f;
        float[] colHeights = new float[3];
        Arrays.fill(colHeights, 0.0f);

        // Adjust scroll offset
        float scroll = (float) (-this.scrollHandler.getRGB());

        for (Module module : filteredModules) {
            // Find index of column with minimum height
            int colIndex = 0;
            float minHeight = colHeights[0];
            for (int i = 1; i < 3; i++) {
                if (colHeights[i] < minHeight) {
                    minHeight = colHeights[i];
                    colIndex = i;
                }
            }

            // Calculate card height dynamically based on expanded settings
            float cardHeight = this.calculateCardHeight(module);

            // Compute positions
            float cardX = startX + colIndex * (colWidth + 8.0f);
            float cardY = startY + minHeight + scroll;

            // Render the module card
            this.drawModuleCard(context, module, cardX, cardY, colWidth, cardHeight, alpha);

            // Increment column height
            colHeights[colIndex] += cardHeight + 8.0f;
        }

        // Set max scroll limit based on tallest column
        float maxVal = 0;
        for (float h : colHeights) {
            if (h > maxVal) maxVal = h;
        }
        float visibleHeight = height;
        this.scrollHandler.setMax(-Math.max(0.0f, maxVal - visibleHeight - 10.0f));
    }

    private float calculateCardHeight(Module module) {
        float height = 24.0f; // Header padding + spacing
        if (!module.getSettings().isEmpty()) {
            for (moscow.rockstar.systems.setting.Setting setting : module.getSettings()) {
                if (!setting.isVisible()) continue;
                if (setting instanceof moscow.rockstar.systems.setting.settings.BooleanSetting) {
                    height += 15.0f;
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.SliderSetting) {
                    height += 22.0f;
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.SelectSetting) {
                    boolean expanded = this.expandedSettings.getOrDefault(setting, false);
                    if (expanded) {
                        moscow.rockstar.systems.setting.settings.SelectSetting ss = (moscow.rockstar.systems.setting.settings.SelectSetting) setting;
                        height += 16.0f + ss.getValues().size() * 12.0f + 4.0f;
                    } else {
                        height += 16.0f;
                    }
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.ModeSetting) {
                    boolean expanded = this.expandedSettings.getOrDefault(setting, false);
                    if (expanded) {
                        moscow.rockstar.systems.setting.settings.ModeSetting ms = (moscow.rockstar.systems.setting.settings.ModeSetting) setting;
                        height += 16.0f + ms.getValues().size() * 12.0f + 4.0f;
                    } else {
                        height += 16.0f;
                    }
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.ColorSetting) {
                    height += 15.0f;
                } else {
                    height += 14.0f;
                }
            }
            height += 4.0f; // extra spacing at bottom
        }
        return height;
    }

    private void drawModuleCard(UIContext context, Module module, float x, float y, float width, float height, float alpha) {
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        ColorRGBA cardBg = dark ? new ColorRGBA(16.0f, 21.0f, 30.0f, 255.0f * alpha * 0.95f) : new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f * alpha * 0.95f);
        ColorRGBA outlineColor = dark ? new ColorRGBA(28.0f, 35.0f, 48.0f, 255.0f * alpha) : new ColorRGBA(220.0f, 220.0f, 225.0f, 255.0f * alpha);

        // Draw Card Background and Border Outline
        context.drawRoundedRect(x, y, width, height, BorderRadius.all(6.0f), cardBg);
        context.drawRoundedBorder(x, y, width, height, 0.5f, BorderRadius.all(6.0f), outlineColor);

        // Render Header: Name, Star (Favorite), Keybind, Enable toggle
        float padding = 8.0f;
        float headerY = y + 5.0f;

        // Star Favorite Icon on left (near text) or next to toggle. Let's place it near the toggle.
        float actionsX = x + width - padding;

        // 1. Toggle switch on far right
        actionsX -= 12.0f;
        boolean toggleHovered = GuiUtility.isHovered(actionsX, headerY, 12.0f, 10.0f, context);
        ColorRGBA toggleColor = module.isEnabled() ? Colors.getAccentColor() : Colors.getTextColor().withAlpha(80.0f);
        context.drawRoundedRect(actionsX, headerY + 1.0f, 12.0f, 6.0f, BorderRadius.all(3.0f), toggleColor);
        context.drawRoundedRect(actionsX + (module.isEnabled() ? 6.0f : 1.0f), headerY + 2.0f, 4.0f, 4.0f, BorderRadius.all(2.0f), ColorRGBA.WHITE.withAlpha(255.0f * alpha));
        if (toggleHovered) CursorUtility.set(CursorType.HAND);
        actionsX -= 6.0f;

        // 2. Bind button
        int key = module.getKey();
        String bindText = (this.bindingModule == module) ? "..." : (key == -1 ? "None" : TextUtility.getKeyName(key));
        float bindTextWidth = Fonts.REGULAR.getFont(5.5f).width(bindText) + 6.0f;
        actionsX -= bindTextWidth;
        boolean bindHovered = GuiUtility.isHovered(actionsX, headerY - 1.0f, bindTextWidth, 10.0f, context);
        context.drawRoundedRect(actionsX, headerY - 1.0f, bindTextWidth, 10.0f, BorderRadius.all(2.0f), dark ? new ColorRGBA(24.0f, 32.0f, 46.0f, 255.0f * alpha) : new ColorRGBA(210.0f, 210.0f, 215.0f, 255.0f * alpha));
        context.drawCenteredText(Fonts.REGULAR.getFont(5.5f), bindText, actionsX + bindTextWidth / 2.0f, headerY + 1.0f, (bindHovered ? Colors.getAccentColor() : Colors.getTextColor().withAlpha(180.0f)).withAlpha(alpha * 255.0f));
        if (bindHovered) CursorUtility.set(CursorType.HAND);
        actionsX -= 6.0f;

        // 3. Star favorite icon
        actionsX -= 10.0f;
        boolean starHovered = GuiUtility.isHovered(actionsX, headerY, 8.0f, 8.0f, context);
        String starChar = module.isFavorite() ? "★" : "☆";
        ColorRGBA starColor = module.isFavorite() ? new ColorRGBA(255.0f, 215.0f, 0.0f) : Colors.getTextColor().withAlpha(80.0f);
        context.drawText(Fonts.MEDIUM.getFont(7.0f), starChar, actionsX, headerY + 0.5f, starColor.withAlpha(alpha * 255.0f));
        if (starHovered) CursorUtility.set(CursorType.HAND);

        // Module Name on far left
        ColorRGBA titleColor = module.isEnabled() ? Colors.getAccentColor() : Colors.getTextColor();
        context.drawText(Fonts.MEDIUM.getFont(7.5f), module.getName(), x + padding, headerY + 1.0f, titleColor.withAlpha(alpha * 255.0f));

        // Render settings underneath
        float currentY = y + 20.0f;
        if (!module.getSettings().isEmpty()) {
            for (moscow.rockstar.systems.setting.Setting setting : module.getSettings()) {
                if (!setting.isVisible()) continue;

                if (setting instanceof moscow.rockstar.systems.setting.settings.BooleanSetting) {
                    moscow.rockstar.systems.setting.settings.BooleanSetting bs = (moscow.rockstar.systems.setting.settings.BooleanSetting) setting;
                    boolean hovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 12.0f, context);

                    context.drawText(Fonts.REGULAR.getFont(6.5f), Localizator.translate(bs.getName()), x + padding, currentY + 3.0f, Colors.getTextColor().withAlpha((hovered ? 255.0f : 160.0f) * alpha));

                    // draw checkbox
                    float boxSize = 8.0f;
                    float boxX = x + width - padding - boxSize;
                    context.drawRoundedRect(boxX, currentY + 2.0f, boxSize, boxSize, BorderRadius.all(1.5f), bs.isEnabled() ? Colors.getAccentColor() : outlineColor);
                    if (bs.isEnabled()) {
                        context.drawCenteredText(Fonts.BOLD.getFont(6.0f), "✓", boxX + boxSize / 2.0f, currentY + 3.0f, ColorRGBA.WHITE.withAlpha(alpha * 255.0f));
                    }
                    if (hovered) CursorUtility.set(CursorType.HAND);
                    currentY += 15.0f;

                } else if (setting instanceof moscow.rockstar.systems.setting.settings.SliderSetting) {
                    moscow.rockstar.systems.setting.settings.SliderSetting ss = (moscow.rockstar.systems.setting.settings.SliderSetting) setting;
                    boolean hovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 18.0f, context);

                    // Name on left, value on right
                    context.drawText(Fonts.REGULAR.getFont(6.5f), Localizator.translate(ss.getName()), x + padding, currentY + 1.0f, Colors.getTextColor().withAlpha(160.0f * alpha));

                    String valStr = String.format("%.2f", ss.getCurrentValue()) + (ss.getSuffix() != null ? " " + ss.getSuffix() : "");
                    context.drawRightText(Fonts.REGULAR.getFont(6.5f), valStr, x + width - padding, currentY + 1.0f, Colors.getAccentColor().withAlpha(alpha * 255.0f));

                    // Slider bar
                    float barY = currentY + 11.0f;
                    float barWidth = width - padding * 2;
                    float barHeight = 2.0f;
                    float fillWidth = barWidth * ((ss.getCurrentValue() - ss.getMin()) / (ss.getMax() - ss.getMin()));

                    context.drawRoundedRect(x + padding, barY, barWidth, barHeight, BorderRadius.all(1.0f), outlineColor);
                    context.drawRoundedRect(x + padding, barY, fillWidth, barHeight, BorderRadius.all(1.0f), Colors.getAccentColor());
                    context.drawRoundedRect(x + padding + fillWidth - 2.0f, barY - 1.0f, 4.0f, 4.0f, BorderRadius.all(2.0f), ColorRGBA.WHITE.withAlpha(alpha * 255.0f));

                    // Dragging handler
                    if (hovered && GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS && this.draggingSlider == null) {
                        this.draggingSlider = ss;
                    }
                    if (this.draggingSlider == ss) {
                        double mouseOffset = context.getMouseX() - (x + padding);
                        float percent = Math.clamp((float) (mouseOffset / barWidth), 0.0f, 1.0f);
                        float newValue = ss.getMin() + percent * (ss.getMax() - ss.getMin());
                        // round value to slider precision
                        float increment = ss.getStep();
                        newValue = Math.round(newValue / increment) * increment;
                        ss.setCurrentValue(Math.clamp(newValue, ss.getMin(), ss.getMax()));
                    }

                    if (hovered) CursorUtility.set(CursorType.HAND);
                    currentY += 22.0f;

                } else if (setting instanceof moscow.rockstar.systems.setting.settings.SelectSetting) {
                    moscow.rockstar.systems.setting.settings.SelectSetting ss = (moscow.rockstar.systems.setting.settings.SelectSetting) setting;
                    boolean expanded = this.expandedSettings.getOrDefault(ss, false);

                    // Draw select header box
                    boolean headerHovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 12.0f, context);
                    context.drawRoundedRect(x + padding, currentY + 1.0f, width - padding * 2, 11.0f, BorderRadius.all(2.0f), dark ? new ColorRGBA(24.0f, 32.0f, 46.0f, 255.0f * alpha) : new ColorRGBA(210.0f, 210.0f, 215.0f, 255.0f * alpha));

                    context.drawText(Fonts.REGULAR.getFont(6.0f), Localizator.translate(ss.getName()), x + padding + 4.0f, currentY + 3.5f, Colors.getTextColor().withAlpha(160.0f * alpha));

                    // Selected values list string
                    String selectedStr = ss.getSelectedValues().isEmpty() ? "None" : ss.getSelectedValues().get(0).getName();
                    if (ss.getSelectedValues().size() > 1) {
                        selectedStr += " +" + (ss.getSelectedValues().size() - 1);
                    }
                    context.drawRightText(Fonts.REGULAR.getFont(6.0f), selectedStr, x + width - padding - 12.0f, currentY + 3.5f, Colors.getAccentColor().withAlpha(alpha * 255.0f));

                    // Dropdown arrow icon
                    context.drawText(Fonts.REGULAR.getFont(6.0f), expanded ? "▲" : "▼", x + width - padding - 8.0f, currentY + 3.0f, Colors.getTextColor().withAlpha(120.0f * alpha));

                    if (headerHovered) CursorUtility.set(CursorType.HAND);

                    currentY += 16.0f;

                    // If expanded, draw settings choices
                    if (expanded) {
                        float choicesY = currentY;
                        for (moscow.rockstar.systems.setting.settings.SelectSetting.Value value : ss.getValues()) {
                            boolean itemHovered = GuiUtility.isHovered(x + padding, choicesY, width - padding * 2, 11.0f, context);
                            if (value.isSelected()) {
                                context.drawRoundedRect(x + padding + 2.0f, choicesY + 1.0f, width - padding * 2 - 4.0f, 9.0f, BorderRadius.all(1.5f), Colors.getAccentColor().withAlpha(60.0f * alpha));
                            }
                            context.drawText(Fonts.REGULAR.getFont(6.0f), value.getName(), x + padding + 6.0f, choicesY + 2.5f, Colors.getTextColor().withAlpha((itemHovered || value.isSelected() ? 255.0f : 140.0f) * alpha));

                            if (itemHovered) CursorUtility.set(CursorType.HAND);
                            choicesY += 12.0f;
                        }
                        currentY += ss.getValues().size() * 12.0f + 4.0f;
                    }

                } else if (setting instanceof moscow.rockstar.systems.setting.settings.ModeSetting) {
                    moscow.rockstar.systems.setting.settings.ModeSetting ms = (moscow.rockstar.systems.setting.settings.ModeSetting) setting;
                    boolean expanded = this.expandedSettings.getOrDefault(ms, false);

                    // Draw mode header box
                    boolean headerHovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 12.0f, context);
                    context.drawRoundedRect(x + padding, currentY + 1.0f, width - padding * 2, 11.0f, BorderRadius.all(2.0f), dark ? new ColorRGBA(24.0f, 32.0f, 46.0f, 255.0f * alpha) : new ColorRGBA(210.0f, 210.0f, 215.0f, 255.0f * alpha));

                    context.drawText(Fonts.REGULAR.getFont(6.0f), Localizator.translate(ms.getName()), x + padding + 4.0f, currentY + 3.5f, Colors.getTextColor().withAlpha(160.0f * alpha));

                    // Current chosen mode value
                    String activeModeName = ms.getValue() != null ? ms.getValue().getName() : "None";
                    context.drawRightText(Fonts.REGULAR.getFont(6.0f), activeModeName, x + width - padding - 12.0f, currentY + 3.5f, Colors.getAccentColor().withAlpha(alpha * 255.0f));

                    // Dropdown arrow icon
                    context.drawText(Fonts.REGULAR.getFont(6.0f), expanded ? "▲" : "▼", x + width - padding - 8.0f, currentY + 3.0f, Colors.getTextColor().withAlpha(120.0f * alpha));

                    if (headerHovered) CursorUtility.set(CursorType.HAND);

                    currentY += 16.0f;

                    // If expanded, draw choices
                    if (expanded) {
                        float choicesY = currentY;
                        for (moscow.rockstar.systems.setting.settings.ModeSetting.Value value : ms.getValues()) {
                            boolean itemHovered = GuiUtility.isHovered(x + padding, choicesY, width - padding * 2, 11.0f, context);
                            if (value.isSelected()) {
                                context.drawRoundedRect(x + padding + 2.0f, choicesY + 1.0f, width - padding * 2 - 4.0f, 9.0f, BorderRadius.all(1.5f), Colors.getAccentColor().withAlpha(60.0f * alpha));
                            }
                            context.drawText(Fonts.REGULAR.getFont(6.0f), value.getName(), x + padding + 6.0f, choicesY + 2.5f, Colors.getTextColor().withAlpha((itemHovered || value.isSelected() ? 255.0f : 140.0f) * alpha));

                            if (itemHovered) CursorUtility.set(CursorType.HAND);
                            choicesY += 12.0f;
                        }
                        currentY += ms.getValues().size() * 12.0f + 4.0f;
                    }

                } else if (setting instanceof moscow.rockstar.systems.setting.settings.ColorSetting) {
                    moscow.rockstar.systems.setting.settings.ColorSetting cs = (moscow.rockstar.systems.setting.settings.ColorSetting) setting;
                    boolean hovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 12.0f, context);

                    context.drawText(Fonts.REGULAR.getFont(6.5f), Localizator.translate(cs.getName()), x + padding, currentY + 3.0f, Colors.getTextColor().withAlpha(160.0f * alpha));

                    // draw color square preview
                    float boxSize = 8.0f;
                    float boxX = x + width - padding - boxSize;
                    context.drawRoundedRect(boxX, currentY + 2.0f, boxSize, boxSize, BorderRadius.all(2.0f), cs.getColor().withAlpha(alpha * 255.0f));

                    if (hovered) CursorUtility.set(CursorType.HAND);
                    currentY += 15.0f;
                }
            }
        }
    }

    private void renderConfigsTab(UIContext context, float x, float y, float width, float height, float alpha) {
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        ColorRGBA outlineColor = dark ? new ColorRGBA(28.0f, 35.0f, 48.0f, 255.0f * alpha) : new ColorRGBA(220.0f, 220.0f, 225.0f, 255.0f * alpha);

        // 1. Render Left panel: Create Config
        float panelW = (width - 15.0f) / 2.0f;
        context.drawRoundedRect(x, y, panelW, height - 10.0f, BorderRadius.all(6.0f), dark ? new ColorRGBA(16.0f, 21.0f, 30.0f, 255.0f * alpha) : new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f * alpha));
        context.drawRoundedBorder(x, y, panelW, height - 10.0f, 0.5f, BorderRadius.all(6.0f), outlineColor);

        float leftX = x + 10.0f;
        context.drawText(Fonts.SEMIBOLD.getFont(9.0f), Localizator.translate("configs.new_name"), leftX, y + 15.0f, Colors.getTextColor().withAlpha(alpha * 255.0f));

        // Config name text field
        float fieldY = y + 35.0f;
        float fieldW = panelW - 20.0f;
        context.drawRoundedRect(leftX, fieldY, fieldW, 16.0f, BorderRadius.all(4.0f), dark ? new ColorRGBA(24.0f, 32.0f, 46.0f, 255.0f * alpha) : new ColorRGBA(210.0f, 210.0f, 215.0f, 255.0f * alpha));
        this.newConfigField.set(leftX + 4.0f, fieldY + 3.0f, fieldW - 8.0f, 10.0f);
        this.newConfigField.setTextColor(Colors.getTextColor().withAlpha(alpha * 255.0f));
        this.newConfigField.render(context);

        // Create Button
        float btnY = y + 60.0f;
        boolean createHovered = GuiUtility.isHovered(leftX, btnY, fieldW, 16.0f, context);
        context.drawRoundedRect(leftX, btnY, fieldW, 16.0f, BorderRadius.all(4.0f), createHovered ? Colors.getAccentColor() : Colors.getAccentColor().withAlpha(120.0f));
        context.drawCenteredText(Fonts.MEDIUM.getFont(8.0f), Localizator.translate("configs.create"), leftX + fieldW / 2.0f, btnY + 4.0f, ColorRGBA.WHITE.withAlpha(alpha * 255.0f));
        if (createHovered) CursorUtility.set(CursorType.HAND);

        // 2. Render Right panel: Config List
        float rightX = x + panelW + 15.0f;
        context.drawRoundedRect(rightX, y, panelW, height - 10.0f, BorderRadius.all(6.0f), dark ? new ColorRGBA(16.0f, 21.0f, 30.0f, 255.0f * alpha) : new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f * alpha));
        context.drawRoundedBorder(rightX, y, panelW, height - 10.0f, 0.5f, BorderRadius.all(6.0f), outlineColor);

        // Refresh configs lists
        List<ConfigFile> configs = Rockstar.getInstance().getConfigManager().getConfigFiles();
        float listY = y + 10.0f;
        for (ConfigFile config : configs) {
            boolean itemHovered = GuiUtility.isHovered(rightX + 10.0f, listY, panelW - 20.0f, 22.0f, context);
            boolean isCurrent = config == Rockstar.getInstance().getConfigManager().getCurrent();

            // Background card
            context.drawRoundedRect(rightX + 10.0f, listY, panelW - 20.0f, 20.0f, BorderRadius.all(3.0f),
                    isCurrent ? Colors.getAccentColor().withAlpha(60.0f * alpha) : (itemHovered ? outlineColor.withAlpha(100.0f * alpha) : outlineColor.withAlpha(40.0f * alpha)));

            // Name
            context.drawText(Fonts.MEDIUM.getFont(7.5f), config.getFileName(), rightX + 16.0f, listY + 5.5f, Colors.getTextColor().withAlpha(alpha * 255.0f));

            // Load / Save / Delete action buttons inside the row
            float itemActionsX = rightX + panelW - 15.0f;

            // Delete (Trash) button
            itemActionsX -= 12.0f;
            boolean deleteHovered = GuiUtility.isHovered(itemActionsX - 2.0f, listY + 4.0f, 12.0f, 12.0f, context);
            context.drawText(Fonts.MEDIUM.getFont(8.0f), "🗑", itemActionsX, listY + 5.5f, (deleteHovered ? Colors.RED : Colors.getTextColor().withAlpha(120.0f)).withAlpha(alpha * 255.0f));
            if (deleteHovered) CursorUtility.set(CursorType.HAND);
            itemActionsX -= 8.0f;

            // Save button
            itemActionsX -= 12.0f;
            boolean saveHovered = GuiUtility.isHovered(itemActionsX - 2.0f, listY + 4.0f, 12.0f, 12.0f, context);
            context.drawText(Fonts.MEDIUM.getFont(8.0f), "💾", itemActionsX, listY + 5.5f, (saveHovered ? Colors.getAccentColor() : Colors.getTextColor().withAlpha(120.0f)).withAlpha(alpha * 255.0f));
            if (saveHovered) CursorUtility.set(CursorType.HAND);
            itemActionsX -= 8.0f;

            // Load button
            itemActionsX -= 12.0f;
            boolean loadHovered = GuiUtility.isHovered(itemActionsX - 2.0f, listY + 4.0f, 12.0f, 12.0f, context);
            context.drawText(Fonts.MEDIUM.getFont(8.0f), "📂", itemActionsX, listY + 5.5f, (loadHovered ? Colors.getAccentColor() : Colors.getTextColor().withAlpha(120.0f)).withAlpha(alpha * 255.0f));
            if (loadHovered) CursorUtility.set(CursorType.HAND);

            listY += 25.0f;
        }
    }

    private void renderThemesTab(UIContext context, float x, float y, float width, float height, float alpha) {
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        ColorRGBA outlineColor = dark ? new ColorRGBA(28.0f, 35.0f, 48.0f, 255.0f * alpha) : new ColorRGBA(220.0f, 220.0f, 225.0f, 255.0f * alpha);

        // Render central themes management box
        float boxW = 280.0f;
        float boxH = 140.0f;
        float boxX = x + width / 2.0f - boxW / 2.0f;
        float boxY = y + height / 2.0f - boxH / 2.0f - 10.0f;

        context.drawRoundedRect(boxX, boxY, boxW, boxH, BorderRadius.all(8.0f), dark ? new ColorRGBA(16.0f, 21.0f, 30.0f, 255.0f * alpha) : new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f * alpha));
        context.drawRoundedBorder(boxX, boxY, boxW, boxH, 0.5f, BorderRadius.all(8.0f), outlineColor);

        // Theme Style Toggle: Dark / Light
        context.drawText(Fonts.SEMIBOLD.getFont(9.0f), "Style:", boxX + 15.0f, boxY + 20.0f, Colors.getTextColor().withAlpha(alpha * 255.0f));

        float toggleDarkX = boxX + 80.0f;
        boolean darkBtnHover = GuiUtility.isHovered(toggleDarkX, boxY + 15.0f, 60.0f, 16.0f, context);
        context.drawRoundedRect(toggleDarkX, boxY + 15.0f, 60.0f, 16.0f, BorderRadius.all(4.0f), dark ? Colors.getAccentColor() : (darkBtnHover ? outlineColor : outlineColor.withAlpha(100.0f)));
        context.drawCenteredText(Fonts.MEDIUM.getFont(7.5f), "Dark", toggleDarkX + 30.0f, boxY + 19.5f, (dark ? ColorRGBA.WHITE : Colors.getTextColor()).withAlpha(alpha * 255.0f));
        if (darkBtnHover) CursorUtility.set(CursorType.HAND);

        float toggleLightX = boxX + 145.0f;
        boolean lightBtnHover = GuiUtility.isHovered(toggleLightX, boxY + 15.0f, 60.0f, 16.0f, context);
        context.drawRoundedRect(toggleLightX, boxY + 15.0f, 60.0f, 16.0f, BorderRadius.all(4.0f), !dark ? Colors.getAccentColor() : (lightBtnHover ? outlineColor : outlineColor.withAlpha(100.0f)));
        context.drawCenteredText(Fonts.MEDIUM.getFont(7.5f), "Light", toggleLightX + 30.0f, boxY + 19.5f, (!dark ? ColorRGBA.WHITE : Colors.getTextColor()).withAlpha(alpha * 255.0f));
        if (lightBtnHover) CursorUtility.set(CursorType.HAND);

        // Predefined Accent Colors
        context.drawText(Fonts.SEMIBOLD.getFont(9.0f), "Accent:", boxX + 15.0f, boxY + 60.0f, Colors.getTextColor().withAlpha(alpha * 255.0f));

        ColorRGBA[] colors = new ColorRGBA[]{
                new ColorRGBA(151.0f, 71.0f, 255.0f), // Purple
                new ColorRGBA(255.0f, 80.0f, 80.0f),   // Red
                new ColorRGBA(80.0f, 255.0f, 80.0f),   // Green
                new ColorRGBA(80.0f, 180.0f, 255.0f),  // Blue
                new ColorRGBA(255.0f, 170.0f, 0.0f),   // Orange
                new ColorRGBA(0.0f, 230.0f, 230.0f)    // Cyan
        };

        float startColorX = boxX + 80.0f;
        ColorRGBA currentAccent = Rockstar.getInstance().getThemeManager().getAccentColor();

        for (int i = 0; i < colors.length; i++) {
            float cX = startColorX + i * 20.0f;
            boolean cHovered = GuiUtility.isHovered(cX, boxY + 57.0f, 12.0f, 12.0f, context);
            boolean isSelected = colors[i].equals(currentAccent);

            context.drawRoundedRect(cX, boxY + 57.0f, 12.0f, 12.0f, BorderRadius.all(6.0f), colors[i].withAlpha(alpha * 255.0f));
            if (isSelected) {
                context.drawRoundedBorder(cX - 2.0f, boxY + 55.0f, 16.0f, 16.0f, 1.0f, BorderRadius.all(8.0f), Colors.getAccentColor());
            } else if (cHovered) {
                context.drawRoundedBorder(cX - 2.0f, boxY + 55.0f, 16.0f, 16.0f, 1.0f, BorderRadius.all(8.0f), Colors.getTextColor().withAlpha(100.0f));
                CursorUtility.set(CursorType.HAND);
            }
        }

        // Custom Color Accent setting button
        float pickerBtnX = boxX + 80.0f;
        float pickerBtnY = boxY + 95.0f;
        boolean pickerHovered = GuiUtility.isHovered(pickerBtnX, pickerBtnY, 120.0f, 16.0f, context);
        context.drawRoundedRect(pickerBtnX, pickerBtnY, 120.0f, 16.0f, BorderRadius.all(4.0f), pickerHovered ? outlineColor : outlineColor.withAlpha(100.0f));
        context.drawCenteredText(Fonts.MEDIUM.getFont(7.0f), "Choose Custom Color", pickerBtnX + 60.0f, pickerBtnY + 4.5f, Colors.getTextColor().withAlpha(alpha * 255.0f));
        if (pickerHovered) CursorUtility.set(CursorType.HAND);
    }

    private ModuleCategory getCategoryFromTab(int tabIndex) {
        return switch (tabIndex) {
            case 0 -> ModuleCategory.COMBAT;
            case 1 -> ModuleCategory.MOVEMENT;
            case 2 -> ModuleCategory.PLAYER;
            case 3 -> ModuleCategory.VISUALS;
            case 4 -> ModuleCategory.OTHER;
            default -> null;
        };
    }

    @Override
    @Compile
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        // Handle color pickers first
        for (ColorPicker colorPicker : this.colorPickers) {
            boolean isPick = colorPicker.isPick();
            colorPicker.onMouseClicked(mouseX, mouseY, button);
            if (colorPicker.isHovered(mouseX, mouseY) || isPick) {
                return;
            }
            colorPicker.setShowing(false);
        }

        float x = this.menuWindow.x;
        float y = this.menuWindow.y;
        float width = this.menuWindow.getWidth();

        // 1. Language switcher click detection
        float actionsEndX = x + width - 15.0f;
        actionsEndX -= 16.0f; // Chat icon
        if (GuiUtility.isHovered(actionsEndX - 10.0f, y + 6.0f, 12.0f, 12.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
            // Cycle languages
            Language current = Localizator.getCurrentLanguage();
            Language next = switch (current) {
                case RU_RU -> Language.EN_US;
                case EN_US -> Language.UK_UA;
                case UK_UA -> Language.PL_PL;
                case PL_PL -> Language.RU_RU;
            };
            Localizator.setLanguage(next);
            ClientSounds.CLICKGUI_OPEN.play(1.0f, 1.2f);
            return;
        }
        actionsEndX -= 18.0f; // Language icon

        // 2. Search icon click detection
        if (GuiUtility.isHovered(actionsEndX - 10.0f, y + 6.0f, 12.0f, 12.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
            this.searchFocused = !this.searchFocused;
            this.searchField.setFocused(this.searchFocused);
            ClientSounds.CLICKGUI_OPEN.play(1.0f, 1.1f);
            return;
        }
        actionsEndX -= 15.0f; // Search icon

        if (this.searchFocused) {
            this.searchField.onMouseClicked(mouseX, mouseY, button);
        }

        // 3. Tab navigation clicks
        float tabsStartX = x + width / 2.0f - 150.0f;
        for (int i = 0; i < 7; i++) {
            float tabX = tabsStartX + i * 40.0f;
            if (GuiUtility.isHovered(tabX, y + 4.0f, 36.0f, 20.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
                this.activeTab = i;
                this.scrollHandler.reset();
                this.scrollHandler.scroll(0);
                ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.0f);
                return;
            }
        }

        // Module keys binding handler
        if (this.bindingModule != null) {
            if (button == MouseButton.LEFT || button == MouseButton.RIGHT) {
                this.bindingModule.setKey(button.getButtonIndex());
                this.bindingModule = null;
                return;
            }
        }

        // Click inside grid content
        float contentX = this.menuWindow.x + 10.0f;
        float contentY = this.menuWindow.y + 35.0f;
        float contentWidth = this.menuWindow.getWidth() - 20.0f;
        float contentHeight = this.menuWindow.getHeight() - 45.0f;

        if (this.activeTab >= 0 && this.activeTab <= 4) {
            ModuleCategory currentCategory = this.getCategoryFromTab(this.activeTab);
            if (currentCategory != null) {
                List<Module> filteredModules = Rockstar.getInstance().getModuleManager().getModules().stream()
                        .filter(module -> module.getCategory() == currentCategory)
                        .filter(module -> {
                            String query = this.searchField.getBuiltText().toLowerCase().trim();
                            return query.isEmpty() || module.getName().toLowerCase().contains(query);
                        })
                        .sorted((m1, m2) -> {
                            if (m1.isFavorite() && !m2.isFavorite()) return -1;
                            if (!m1.isFavorite() && m2.isFavorite()) return 1;
                            return m1.getName().compareTo(m2.getName());
                        })
                        .toList();

                float colWidth = (contentWidth - 16.0f) / 3.0f;
                float[] colHeights = new float[3];
                Arrays.fill(colHeights, 0.0f);
                float scroll = (float) (-this.scrollHandler.getRGB());

                for (Module module : filteredModules) {
                    int colIndex = 0;
                    float minHeight = colHeights[0];
                    for (int i = 1; i < 3; i++) {
                        if (colHeights[i] < minHeight) {
                            minHeight = colHeights[i];
                            colIndex = i;
                        }
                    }

                    float cardHeight = this.calculateCardHeight(module);
                    float cardX = contentX + colIndex * (colWidth + 8.0f);
                    float cardY = contentY + minHeight + scroll;

                    // If hovered the card y range
                    if (mouseY >= contentY && mouseY <= contentY + contentHeight && GuiUtility.isHovered(cardX, cardY, colWidth, cardHeight, mouseX, mouseY)) {
                        float padding = 8.0f;
                        float headerY = cardY + 5.0f;
                        float actionsX = cardX + colWidth - padding;

                        // Switch toggle
                        actionsX -= 12.0f;
                        if (GuiUtility.isHovered(actionsX, headerY, 12.0f, 10.0f, mouseX, mouseY)) {
                            module.toggle();
                            return;
                        }
                        actionsX -= 6.0f;

                        // Bind button
                        int key = module.getKey();
                        String bindText = (key == -1) ? "None" : TextUtility.getKeyName(key);
                        float bindTextWidth = Fonts.REGULAR.getFont(5.5f).width(bindText) + 6.0f;
                        actionsX -= bindTextWidth;
                        if (GuiUtility.isHovered(actionsX, headerY - 1.0f, bindTextWidth, 10.0f, mouseX, mouseY)) {
                            this.bindingModule = module;
                            return;
                        }
                        actionsX -= 6.0f;

                        // Star favorite button
                        actionsX -= 10.0f;
                        if (GuiUtility.isHovered(actionsX, headerY, 8.0f, 8.0f, mouseX, mouseY)) {
                            module.setFavorite(!module.isFavorite());
                            Rockstar.getInstance().getFileManager().writeFile("client");
                            ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.3f);
                            return;
                        }

                        // Toggle module if header clicked outside settings
                        if (mouseY < cardY + 20.0f) {
                            module.toggle();
                            return;
                        }

                        // Check clicks inside settings rows
                        float currentSettingY = cardY + 20.0f;
                        for (moscow.rockstar.systems.setting.Setting setting : module.getSettings()) {
                            if (!setting.isVisible()) continue;

                            if (setting instanceof moscow.rockstar.systems.setting.settings.BooleanSetting) {
                                moscow.rockstar.systems.setting.settings.BooleanSetting bs = (moscow.rockstar.systems.setting.settings.BooleanSetting) setting;
                                if (GuiUtility.isHovered(cardX + padding, currentSettingY, colWidth - padding * 2, 12.0f, mouseX, mouseY)) {
                                    bs.setEnabled(!bs.isEnabled());
                                    ClientSounds.CLICKGUI_OPEN.play(0.3f, 1.2f);
                                    return;
                                }
                                currentSettingY += 15.0f;

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.SliderSetting) {
                                currentSettingY += 22.0f; // Slider is handled in rendering/drag loop

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.SelectSetting) {
                                moscow.rockstar.systems.setting.settings.SelectSetting ss = (moscow.rockstar.systems.setting.settings.SelectSetting) setting;
                                boolean expanded = this.expandedSettings.getOrDefault(ss, false);

                                if (GuiUtility.isHovered(cardX + padding, currentSettingY, colWidth - padding * 2, 12.0f, mouseX, mouseY)) {
                                    this.expandedSettings.put(ss, !expanded);
                                    ClientSounds.CLICKGUI_OPEN.play(0.4f, 1.0f);
                                    return;
                                }
                                currentSettingY += 16.0f;

                                if (expanded) {
                                    float choicesY = currentSettingY;
                                    for (moscow.rockstar.systems.setting.settings.SelectSetting.Value value : ss.getValues()) {
                                        if (GuiUtility.isHovered(cardX + padding, choicesY, colWidth - padding * 2, 11.0f, mouseX, mouseY)) {
                                            value.toggle();
                                            ClientSounds.CLICKGUI_OPEN.play(0.3f, 1.1f);
                                            return;
                                        }
                                        choicesY += 12.0f;
                                    }
                                    currentSettingY += ss.getValues().size() * 12.0f + 4.0f;
                                }

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.ModeSetting) {
                                moscow.rockstar.systems.setting.settings.ModeSetting ms = (moscow.rockstar.systems.setting.settings.ModeSetting) setting;
                                boolean expanded = this.expandedSettings.getOrDefault(ms, false);

                                if (GuiUtility.isHovered(cardX + padding, currentSettingY, colWidth - padding * 2, 12.0f, mouseX, mouseY)) {
                                    this.expandedSettings.put(ms, !expanded);
                                    ClientSounds.CLICKGUI_OPEN.play(0.4f, 1.0f);
                                    return;
                                }
                                currentSettingY += 16.0f;

                                if (expanded) {
                                    float choicesY = currentSettingY;
                                    for (moscow.rockstar.systems.setting.settings.ModeSetting.Value value : ms.getValues()) {
                                        if (GuiUtility.isHovered(cardX + padding, choicesY, colWidth - padding * 2, 11.0f, mouseX, mouseY)) {
                                            value.select();
                                            this.expandedSettings.put(ms, false); // collapse after select
                                            ClientSounds.CLICKGUI_OPEN.play(0.3f, 1.1f);
                                            return;
                                        }
                                        choicesY += 12.0f;
                                    }
                                    currentSettingY += ms.getValues().size() * 12.0f + 4.0f;
                                }

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.ColorSetting) {
                                moscow.rockstar.systems.setting.settings.ColorSetting cs = (moscow.rockstar.systems.setting.settings.ColorSetting) setting;
                                if (GuiUtility.isHovered(cardX + padding, currentSettingY, colWidth - padding * 2, 12.0f, mouseX, mouseY)) {
                                    ColorPicker picker = new ColorPicker((float) mouseX, (float) mouseY, 6.0f, cs.isAlpha(), cs.getColor(), Localizator.translate(cs.getName()));
                                    picker.setOnClose(() -> cs.setColor(picker.built()));
                                    this.colorPickers.add(picker);
                                    ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.1f);
                                    return;
                                }
                                currentSettingY += 15.0f;
                            }
                        }
                    }

                    colHeights[colIndex] += cardHeight + 8.0f;
                }
            }
        } else if (this.activeTab == 5) {
            // Configs tab click checks
            float panelW = (contentWidth - 15.0f) / 2.0f;
            float leftX = contentX + 10.0f;
            float btnY = y + 35.0f + 60.0f;
            float fieldW = panelW - 20.0f;

            this.newConfigField.onMouseClicked(mouseX, mouseY, button);

            // Create Config
            if (GuiUtility.isHovered(leftX, btnY, fieldW, 16.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
                String configName = this.newConfigField.getBuiltText().trim();
                if (!configName.isEmpty()) {
                    Rockstar.getInstance().getConfigManager().createConfig(configName);
                    this.newConfigField.clear();
                    ClientSounds.CLICKGUI_OPEN.play(1.0f, 1.3f);
                }
                return;
            }

            // Config list actions click checks
            float rightX = contentX + panelW + 15.0f;
            List<ConfigFile> configs = Rockstar.getInstance().getConfigManager().getConfigFiles();
            float listY = y + 35.0f + 10.0f;

            for (ConfigFile config : configs) {
                float itemActionsX = rightX + panelW - 15.0f;

                // Delete (Trash) button
                itemActionsX -= 12.0f;
                if (GuiUtility.isHovered(itemActionsX - 2.0f, listY + 4.0f, 12.0f, 12.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
                    config.delete();
                    ClientSounds.CLICKGUI_OPEN.play(0.5f, 0.8f);
                    return;
                }
                itemActionsX -= 8.0f;

                // Save button
                itemActionsX -= 12.0f;
                if (GuiUtility.isHovered(itemActionsX - 2.0f, listY + 4.0f, 12.0f, 12.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
                    config.save();
                    ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.2f);
                    return;
                }
                itemActionsX -= 8.0f;

                // Load button
                itemActionsX -= 12.0f;
                if (GuiUtility.isHovered(itemActionsX - 2.0f, listY + 4.0f, 12.0f, 12.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
                    config.load();
                    ClientSounds.CLICKGUI_OPEN.play(1.0f, 1.0f);
                    return;
                }

                listY += 25.0f;
            }
        } else if (this.activeTab == 6) {
            // Themes tab click checks
            float boxW = 280.0f;
            float boxH = 140.0f;
            float boxX = contentX + contentWidth / 2.0f - boxW / 2.0f;
            float boxY = contentY + contentHeight / 2.0f - boxH / 2.0f - 10.0f;

            // Dark/Light toggle buttons
            float toggleDarkX = boxX + 80.0f;
            if (GuiUtility.isHovered(toggleDarkX, boxY + 15.0f, 60.0f, 16.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
                Rockstar.getInstance().getThemeManager().setCurrentTheme(Theme.DARK);
                ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.1f);
                return;
            }

            float toggleLightX = boxX + 145.0f;
            if (GuiUtility.isHovered(toggleLightX, boxY + 15.0f, 60.0f, 16.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
                Rockstar.getInstance().getThemeManager().setCurrentTheme(Theme.LIGHT);
                ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.1f);
                return;
            }

            // Accent color selections
            ColorRGBA[] colors = new ColorRGBA[]{
                    new ColorRGBA(151.0f, 71.0f, 255.0f), // Purple
                    new ColorRGBA(255.0f, 80.0f, 80.0f),   // Red
                    new ColorRGBA(80.0f, 255.0f, 80.0f),   // Green
                    new ColorRGBA(80.0f, 180.0f, 255.0f),  // Blue
                    new ColorRGBA(255.0f, 170.0f, 0.0f),   // Orange
                    new ColorRGBA(0.0f, 230.0f, 230.0f)    // Cyan
            };
            float startColorX = boxX + 80.0f;
            for (int i = 0; i < colors.length; i++) {
                float cX = startColorX + i * 20.0f;
                if (GuiUtility.isHovered(cX, boxY + 57.0f, 12.0f, 12.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
                    Rockstar.getInstance().getThemeManager().setAccentColor(colors[i]);
                    ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.2f);
                    return;
                }
            }

            // Custom color accent picker button
            float pickerBtnX = boxX + 80.0f;
            float pickerBtnY = boxY + 95.0f;
            if (GuiUtility.isHovered(pickerBtnX, pickerBtnY, 120.0f, 16.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
                ColorRGBA currentAccent = Rockstar.getInstance().getThemeManager().getAccentColor();
                ColorPicker picker = new ColorPicker((float) mouseX, (float) mouseY, 6.0f, false, currentAccent, "Accent Color");
                picker.setOnClose(() -> Rockstar.getInstance().getThemeManager().setAccentColor(picker.built()));
                this.colorPickers.add(picker);
                ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.1f);
                return;
            }
        }

        // Draggable screen support
        if (GuiUtility.isHovered(this.menuWindow, mouseX, mouseY) && button == MouseButton.LEFT) {
            this.drag = true;
            this.dragX = (float) (mouseX - this.menuWindow.x);
            this.dragY = (float) (mouseY - this.menuWindow.y);
        }

        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.drag = false;
        this.draggingSlider = null;
        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.onMouseReleased(mouseX, mouseY, button);
        }
        if (this.searchField.isFocused()) {
            this.searchField.onMouseReleased(mouseX, mouseY, button);
        }
        if (this.newConfigField.isFocused()) {
            this.newConfigField.onMouseReleased(mouseX, mouseY, button);
        }
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
        super.onMouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Compile
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (GuiUtility.isHovered(this.menuWindow, mouseX, mouseY)) {
            this.scrollHandler.scroll(verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Compile
    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        int modifiers = event.modifiers();

        // Check search field activation keybind
        if (!this.searchField.isFocused() && KeyUtility.hasControlDown() && keyCode == 70) {
            this.searchFocused = true;
            this.searchField.setFocused(true);
            return true;
        }

        if (this.bindingModule != null) {
            if (keyCode == 256 || keyCode == 261) { // Esc or Delete
                this.bindingModule.setKey(-1);
            } else {
                this.bindingModule.setKey(keyCode);
            }
            this.bindingModule = null;
            return true;
        }

        this.scrollHandler.onKeyPressed(keyCode);

        for (ColorPicker colorPicker : this.colorPickers) {
            colorPicker.onKeyPressed(keyCode, scanCode, modifiers);
        }

        if (this.searchField.isFocused()) {
            this.searchField.onKeyPressed(keyCode, scanCode, modifiers);
        }
        if (this.newConfigField.isFocused()) {
            this.newConfigField.onKeyPressed(keyCode, scanCode, modifiers);
        }

        return super.keyPressed(event);
    }

    @Compile
    @Override
    public boolean charTyped(CharacterEvent event) {
        char chr = (char) event.codepoint();
        if (this.bindingModule != null) {
            return true;
        }
        if (this.searchField.isFocused()) {
            this.searchField.charTyped(chr, 0);
        }
        if (this.newConfigField.isFocused()) {
            this.newConfigField.charTyped(chr, 0);
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

    @Override
    public void removed() {
        this.colorPickers.clear();
        this.expandedSettings.clear();
        if (TextField.LAST_FIELD != null) {
            TextField.LAST_FIELD.setFocused(false);
            TextField.LAST_FIELD = null;
        }
        super.removed();
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
}
