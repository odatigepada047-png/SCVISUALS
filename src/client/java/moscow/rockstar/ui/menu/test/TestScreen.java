package moscow.rockstar.ui.menu.test;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
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

    private static final float WINDOW_WIDTH = 600.0f;
    private static final float WINDOW_HEIGHT = 370.0f;
    private static final float TOP_BAR_HEIGHT = 28.0f;
    private static final float CONTENT_PADDING = 8.0f;
    private static final float CARD_GAP = 8.0f;
    private static final float CARD_RADIUS = 4.0f;
    private static final ColorRGBA EXPENSIVE_BG = new ColorRGBA(7.0f, 10.0f, 15.0f);
    private static final ColorRGBA EXPENSIVE_PANEL = new ColorRGBA(14.0f, 18.0f, 24.0f);
    private static final ColorRGBA EXPENSIVE_FIELD = new ColorRGBA(20.0f, 25.0f, 33.0f);
    private static final ColorRGBA EXPENSIVE_STROKE = new ColorRGBA(20.0f, 25.0f, 34.0f);
    private static final ColorRGBA EXPENSIVE_MUTED = new ColorRGBA(126.0f, 132.0f, 143.0f);
    private static final ColorRGBA EXPENSIVE_TEXT = new ColorRGBA(222.0f, 226.0f, 235.0f);
    private static final ColorRGBA EXPENSIVE_ACCENT = new ColorRGBA(112.0f, 91.0f, 190.0f);

    private final Rect menuWindow;
    private float dragX;
    private float dragY;
    private boolean drag;
    private float tabSelectorX = -1.0f;
    private final ScrollHandler scrollHandler = new ScrollHandler();
    private int activeTab = 0; // 0: Combat, 1: Movement, 2: Player, 3: Visuals, 4: Misc, 5: Configs, 6: Themes

    private final List<ColorPicker> colorPickers = new LinkedList<>();
    private final TextField searchField;
    private final TextField newConfigField;
    private boolean searchFocused = false;
    private final Map<moscow.rockstar.systems.setting.Setting, Boolean> expandedSettings = new HashMap<>();
    private Module bindingModule = null;
    private moscow.rockstar.systems.setting.settings.BindSetting bindingSetting = null;
    private final Map<moscow.rockstar.systems.setting.Setting, Animation> settingAnimations = new HashMap<>();
    private final Map<Module, Animation> toggleAnimations = new HashMap<>();
    private final Map<Module, float[]> cardPositions = new HashMap<>();
    private int lastTab = -1;

    // Draggable Slider context
    private moscow.rockstar.systems.setting.settings.SliderSetting draggingSlider = null;
    private moscow.rockstar.systems.setting.settings.RangeSetting draggingRange = null;
    private int draggingKnob = 0; // 1 for firstValue, 2 for secondValue

    private static final Identifier SEARCH_ICON = Rockstar.id("icons/search.png");
    private static final Identifier ADD_ICON = Rockstar.id("icons/add.png");
    private static final Identifier LANGUAGE_ICON = Rockstar.id("testicon/language.png");
    private static final Identifier MULTIENUM_ICON = Rockstar.id("testicon/multienum.png");
    private static final Identifier FRAME_ICON = Rockstar.id("testicon/frame.png");
    private static final Identifier KEYBOARD_ICON = Rockstar.id("testicon/keyboard.png");
    private static final Identifier ENUM_ICON = Rockstar.id("testicon/enum.png");
    private static final Identifier COMBAT_ICON = Rockstar.id("testicon/combat.png");
    private static final Identifier MOVEMENT_ICON = Rockstar.id("testicon/movement.png");
    private static final Identifier PLAYER_ICON = Rockstar.id("testicon/player.png");
    private static final Identifier RENDER_ICON = Rockstar.id("testicon/render.png");
    private static final Identifier MISC_ICON = Rockstar.id("testicon/misc.png");
    private static final Identifier CONFIGS_ICON = Rockstar.id("testicon/configs.png");
    private static final Identifier THEMES_ICON = Rockstar.id("testicon/themes.png");
    private static final Identifier STAR_ICON = Rockstar.id("testicon/star.png");
    private static final Identifier STAR_FILL_ICON = Rockstar.id("testicon/star_fill.png");
    private static final Identifier CHECKMARK_ICON = Rockstar.id("testicon/checkmark.png");
    private static final ColorRGBA FAVORITE_YELLOW = new ColorRGBA(254.0f, 203.0f, 47.0f);

    public TestScreen() {
        this.menuWindow = new Rect(
                sr.getGuiScaledWidth() / 2.0f - WINDOW_WIDTH / 2.0f,
                sr.getGuiScaledHeight() / 2.0f - WINDOW_HEIGHT / 2.0f,
                WINDOW_WIDTH,
                WINDOW_HEIGHT
        );
        this.searchField = new TextField(Fonts.MEDIUM.getFont(6.0f));
        this.searchField.setPreview("Search...");
        this.newConfigField = new TextField(Fonts.MEDIUM.getFont(7.0f));
        this.newConfigField.setPreview("Config Name...");
    }

    @Override
    protected void init() {
        this.closing = false;
        this.menuAnimation.setValue(0.0f);
        this.tabSelectorX = -1.0f;
        if (this.menuWindow != null) {
            this.menuWindow.setX(sr.getGuiScaledWidth() / 2.0f - WINDOW_WIDTH / 2.0f);
            this.menuWindow.setY(sr.getGuiScaledHeight() / 2.0f - WINDOW_HEIGHT / 2.0f);
        }
        this.colorPickers.clear();
        this.expandedSettings.clear();
        this.bindingModule = null;
        this.bindingSetting = null;
        this.draggingSlider = null;
        this.draggingRange = null;
        this.draggingKnob = 0;
        this.cardPositions.clear();
        this.lastTab = -1;
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
            if (GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
                this.draggingSlider = null;
            }
        }
        if (this.draggingRange != null) {
            if (GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
                this.draggingRange = null;
            }
        }

        float alpha = Math.min(1.0f, this.menuAnimation.getRGB());
        RenderUtility.scale(context.pose(), this.menuWindow.x + this.menuWindow.getWidth() / 2.0f, this.menuWindow.y + this.menuWindow.getHeight() / 2.0f, 0.8f + 0.2f * this.menuAnimation.getRGB());

        context.drawShadow(this.menuWindow.x + 2.0f, this.menuWindow.y + 4.0f, this.menuWindow.getWidth() - 4.0f, this.menuWindow.getHeight() - 6.0f, 28.0f, BorderRadius.all(10.0f), Colors.BLACK.withAlpha(170.0f * alpha));
        context.drawBlurredRect(this.menuWindow.x, this.menuWindow.y, this.menuWindow.getWidth(), this.menuWindow.getHeight(), 45.0f, 5.0f, BorderRadius.all(10.0f), Colors.WHITE);
        context.drawSquircle(
                this.menuWindow.x, this.menuWindow.y,
                this.menuWindow.getWidth(), this.menuWindow.getHeight(),
                5.0f, BorderRadius.all(10.0f),
                EXPENSIVE_BG.withAlpha(255.0f * alpha * 0.985f)
        );

        this.renderTopBar(context, alpha);

        float contentX = this.menuWindow.x + CONTENT_PADDING;
        float contentY = this.menuWindow.y + TOP_BAR_HEIGHT + 14.0f;
        float contentWidth = this.menuWindow.getWidth() - CONTENT_PADDING * 2.0f;
        float contentHeight = this.menuWindow.getHeight() - TOP_BAR_HEIGHT - 20.0f;

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

    private void drawFolderIcon(UIContext context, float x, float y, float size, ColorRGBA color) {
        float bodyW = size;
        float bodyH = size * 0.7f;
        float bodyY = y + size * 0.3f;
        // Draw back tab
        context.drawRoundedRect(x + 1.0f, y + 1.0f, size * 0.45f, size * 0.3f, BorderRadius.all(1.0f), color);
        // Draw main body
        context.drawRoundedRect(x, bodyY, bodyW, bodyH, BorderRadius.all(1.5f), color);
    }

    private void drawPaintbrushIcon(UIContext context, float x, float y, float size, ColorRGBA color) {
        float bandW = size * 0.4f;
        float bandH = size * 0.2f;
        float bandX = x + (size - bandW) / 2.0f;
        float bandY = y + size * 0.4f;
        
        // bristles
        context.drawRoundedRect(bandX, bandY + bandH, bandW, size * 0.4f, BorderRadius.all(1.0f), color);
        // metal band
        context.drawRoundedRect(bandX - 0.5f, bandY, bandW + 1.0f, bandH, BorderRadius.all(0.5f), color.withAlpha(color.getAlpha() * 0.7f));
        // handle
        context.drawRoundedRect(x + (size - size * 0.15f) / 2.0f, y + 1.0f, size * 0.15f, size * 0.4f, BorderRadius.all(1.0f), color.withAlpha(color.getAlpha() * 0.5f));
    }

    private void drawBindButton(UIContext context, float x, float y, float width, float height, String text, boolean hovered, float alpha) {
        float rx = Math.round(x);
        float ry = Math.round(y);
        float rw = Math.round(width);
        float rh = Math.round(height);
        ColorRGBA bg = EXPENSIVE_FIELD.withAlpha(255.0f * alpha);
        ColorRGBA border = EXPENSIVE_STROKE.withAlpha(255.0f * alpha);
        ColorRGBA color = (hovered ? EXPENSIVE_TEXT : EXPENSIVE_TEXT.withAlpha(180.0f)).withAlpha(alpha * 255.0f);
        
        context.drawRoundedRect(rx, ry, rw, rh, BorderRadius.all(3.0f), bg);
        context.drawRoundedBorder(rx, ry, rw, rh, 1.0f, BorderRadius.all(3.0f), border);
        
        // Draw keyboard texture icon on the left
        float kbSize = 8.0f;
        float kbX = rx + 4.0f;
        float kbY = Math.round(ry + (rh - kbSize) / 2.0f);
        context.drawTexture(KEYBOARD_ICON, kbX, kbY, kbSize, kbSize, color);
        
        float textY = Math.round(ry + (rh - Fonts.REGULAR.getFont(6.0f).height()) / 2.0f);
        context.drawText(Fonts.REGULAR.getFont(6.0f), text, rx + 14.0f, textY, color);
    }

    private void renderTopBar(UIContext context, float alpha) {
        float x = this.menuWindow.x;
        float y = this.menuWindow.y;
        float width = this.menuWindow.getWidth();
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;

        // 2. Draw Navigation Tabs Center
        float navBarWidth = 175.0f;
        float navBarHeight = 22.0f;
        float navBarX = x + (width - navBarWidth) / 2.0f;
        float navBarY = y + 7.0f;

        ColorRGBA navBg = EXPENSIVE_PANEL.withAlpha(230.0f * alpha);
        ColorRGBA navBorder = EXPENSIVE_STROKE.withAlpha(255.0f * alpha);

        context.drawRoundedRect(navBarX, navBarY, navBarWidth, navBarHeight, BorderRadius.all(11.0f), navBg);
        context.drawRoundedBorder(navBarX, navBarY, navBarWidth, navBarHeight, 1.0f, BorderRadius.all(11.0f), navBorder);

        float step = navBarWidth / 7.0f;
        for (int i = 0; i < 7; i++) {
            float tabX = navBarX + i * step;
            boolean hovered = GuiUtility.isHovered(tabX, navBarY, step, navBarHeight, context);
            boolean selected = (i == this.activeTab);

            ColorRGBA iconColor = selected ? ColorRGBA.WHITE : (hovered ? EXPENSIVE_TEXT : EXPENSIVE_MUTED);
            float iconSize = 10.0f;
            float iconX = tabX + (step - iconSize) / 2.0f;
            float iconY = navBarY + (navBarHeight - iconSize) / 2.0f;
 
            if (i == 0) {
                context.drawTextureLinear(COMBAT_ICON, iconX, iconY, iconSize, iconSize, iconColor.withAlpha(alpha * 255.0f));
            } else if (i == 1) {
                context.drawTextureLinear(MOVEMENT_ICON, iconX, iconY, iconSize, iconSize, iconColor.withAlpha(alpha * 255.0f));
            } else if (i == 2) {
                context.drawTextureLinear(PLAYER_ICON, iconX, iconY, iconSize, iconSize, iconColor.withAlpha(alpha * 255.0f));
            } else if (i == 3) {
                context.drawTextureLinear(RENDER_ICON, iconX, iconY, iconSize, iconSize, iconColor.withAlpha(alpha * 255.0f));
            } else if (i == 4) {
                context.drawTextureLinear(MISC_ICON, iconX, iconY, iconSize, iconSize, iconColor.withAlpha(alpha * 255.0f));
            } else if (i == 5) {
                context.drawTextureLinear(CONFIGS_ICON, iconX, iconY, iconSize, iconSize, iconColor.withAlpha(alpha * 255.0f));
            } else {
                context.drawTextureLinear(THEMES_ICON, iconX, iconY, iconSize, iconSize, iconColor.withAlpha(alpha * 255.0f));
            }

            if (hovered) {
                CursorUtility.set(CursorType.HAND);
            }
        }

        // Draw animated sliding active tab indicator line under categories
        float lineW = 10.0f;
        float targetLineX = navBarX + this.activeTab * step + (step - lineW) / 2.0f;
        if (this.tabSelectorX == -1.0f) {
            this.tabSelectorX = targetLineX;
        } else {
            this.tabSelectorX += (targetLineX - this.tabSelectorX) * 0.2f;
        }
        context.drawRoundedRect(this.tabSelectorX, navBarY + navBarHeight - 1.5f, lineW, 1.0f, BorderRadius.all(0.5f), EXPENSIVE_ACCENT.withAlpha(alpha * 255.0f));

        // 3. Draw Search and Right actions
        float actionsEndX = x + width - 15.0f;
        float iconSize = 10.0f;
        float iconY = y + 13.0f;

        // Three dots (More icon, non-clickable)
        float dotX = actionsEndX - iconSize;
        float dotY = y + 17.0f;
        context.drawRoundedRect(dotX + 1.0f, dotY, 2.0f, 2.0f, BorderRadius.all(1.0f), EXPENSIVE_MUTED.withAlpha(alpha * 120.0f));
        context.drawRoundedRect(dotX + 4.5f, dotY, 2.0f, 2.0f, BorderRadius.all(1.0f), EXPENSIVE_MUTED.withAlpha(alpha * 120.0f));
        context.drawRoundedRect(dotX + 8.0f, dotY, 2.0f, 2.0f, BorderRadius.all(1.0f), EXPENSIVE_MUTED.withAlpha(alpha * 120.0f));
        actionsEndX -= (iconSize + 8.0f);

        // Language switcher icon
        float langX = actionsEndX - iconSize;
        boolean langHovered = GuiUtility.isHovered(actionsEndX - iconSize - 2.0f, y + 4.0f, iconSize + 4.0f, 20.0f, context);
        context.drawTextureLinear(LANGUAGE_ICON, langX, iconY, iconSize, iconSize, (langHovered ? EXPENSIVE_ACCENT : EXPENSIVE_MUTED).withAlpha(alpha * 255.0f));
        if (langHovered) {
            CursorUtility.set(CursorType.HAND);
        }
        actionsEndX -= (iconSize + 8.0f);

        // Search icon
        float searchX = actionsEndX - iconSize;
        boolean searchHovered = GuiUtility.isHovered(actionsEndX - iconSize - 2.0f, y + 4.0f, iconSize + 4.0f, 20.0f, context);
        context.drawTextureLinear(SEARCH_ICON, searchX, iconY, iconSize, iconSize, (searchHovered || this.searchFocused ? EXPENSIVE_ACCENT : EXPENSIVE_MUTED).withAlpha(alpha * 255.0f));
        if (searchHovered) {
            CursorUtility.set(CursorType.HAND);
        }
        actionsEndX -= (iconSize + 6.0f);

        // If search is active/focused, draw text field next to it
        if (this.searchFocused) {
            float fieldWidth = 60.0f;
            float fieldY = y + 12.0f;
            this.searchField.set(actionsEndX - fieldWidth, fieldY, fieldWidth, 12.0f);
            this.searchField.setTextColor(EXPENSIVE_TEXT.withAlpha(alpha * 220.0f));
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
        float colWidth = (width - CARD_GAP * 2.0f) / 3.0f;
        float[] colHeights = new float[3];
        Arrays.fill(colHeights, 0.0f);

        // Adjust scroll offset
        float scroll = (float) (-this.scrollHandler.getRGB());

        List<Runnable> dropdownDrawables = new ArrayList<>();

        if (this.activeTab != this.lastTab) {
            this.cardPositions.clear();
            this.lastTab = this.activeTab;
        }

        int index = 0;
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
            float targetX = startX + colIndex * (colWidth + CARD_GAP);
            float targetY = startY + minHeight + scroll;

            int finalIndex = index;
            float[] pos = this.cardPositions.computeIfAbsent(module, m -> new float[]{targetX, targetY + 20.0f + finalIndex * 8.0f});
            pos[0] += (targetX - pos[0]) * 0.15f;
            pos[1] += (targetY - pos[1]) * 0.15f;
            if (Math.abs(pos[0] - targetX) < 0.05f) pos[0] = targetX;
            if (Math.abs(pos[1] - targetY) < 0.05f) pos[1] = targetY;

            float cardX = pos[0];
            float cardY = pos[1];

            // Calculate a local card fade-in alpha based on how close it is to targetY
            float slideDistance = pos[1] - targetY;
            float cardAlpha = alpha;
            if (slideDistance > 0.0f) {
                float totalPlannedSlide = 20.0f + finalIndex * 8.0f;
                float progress = Math.clamp(1.0f - (slideDistance / totalPlannedSlide), 0.0f, 1.0f);
                cardAlpha = alpha * progress;
            }

            // Render the module card
            this.drawModuleCard(context, module, cardX, cardY, colWidth, cardHeight, cardAlpha, dropdownDrawables);

            // Increment column height
            colHeights[colIndex] += cardHeight + CARD_GAP;
            index++;
        }

        // Draw dropdown overlays on top of everything
        for (Runnable drawable : dropdownDrawables) {
            drawable.run();
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
                    height += 16.0f;
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.SliderSetting) {
                    height += 23.0f;
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.RangeSetting) {
                    height += 23.0f;
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.SelectSetting) {
                    height += 28.0f;
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.ModeSetting) {
                    height += 28.0f;
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.ColorSetting) {
                    height += 16.0f;
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.BindSetting) {
                    height += 18.0f;
                } else if (setting instanceof moscow.rockstar.systems.setting.settings.ButtonSetting) {
                    height += 20.0f;
                } else {
                    height += 16.0f;
                }
            }
            height += 3.0f; // extra spacing at bottom
        }
        return height;
    }


    private void drawModuleCard(UIContext context, Module module, float x, float y, float width, float height, float alpha, List<Runnable> dropdownDrawables) {
        boolean dark = true;
        ColorRGBA cardBg = EXPENSIVE_PANEL.withAlpha(242.0f * alpha);
        ColorRGBA outlineColor = EXPENSIVE_STROKE.withAlpha(255.0f * alpha);

        // Draw Card Background and Border Outline
        context.drawRoundedRect(x, y, width, height, BorderRadius.all(CARD_RADIUS), cardBg);
        context.drawRoundedBorder(x, y, width, height, 1.0f, BorderRadius.all(CARD_RADIUS), outlineColor);

        // Render Header: Name, Star (Favorite), Keybind, Enable toggle
        float padding = 7.0f;
        float headerCenterY = y + 11.0f;

        // Star Favorite Icon on left (near text) or next to toggle. Let's place it near the toggle.
        float actionsX = x + width - padding;

        // 1. Toggle switch on far right
        actionsX -= 18.0f;
        boolean toggleHovered = GuiUtility.isHovered(actionsX, headerCenterY - 8.0f, 18.0f, 16.0f, context);
        Animation toggleAnim = this.toggleAnimations.computeIfAbsent(module, m -> new Animation(150L, Easing.BAKEK));
        toggleAnim.update(module.isEnabled() ? 1.0f : 0.0f);
        float toggleVal = toggleAnim.getRGB();
        ColorRGBA trackColor = EXPENSIVE_MUTED.withAlpha(55.0f).mix(EXPENSIVE_ACCENT, toggleVal);
        
        float tx = actionsX;
        float ty = headerCenterY - 5.0f;
        float tw = 18.0f;
        float th = 10.0f;
        
        context.drawRoundedRect(tx, ty, tw, th, BorderRadius.all(5.0f), trackColor.withAlpha(trackColor.getAlpha() * alpha));
        float knobSize = 8.0f;
        float knobX = tx + 1.0f + toggleVal * 8.0f;
        float knobY = ty + 1.0f + toggleVal * 0.25f;
        context.drawRoundedRect(knobX, knobY, knobSize, knobSize, BorderRadius.all(4.0f), ColorRGBA.WHITE.withAlpha(255.0f * alpha));
        if (toggleHovered) CursorUtility.set(CursorType.HAND);
 
        // 2. Bind button
        int key = module.getKey();
        String bindText = (this.bindingModule == module) ? "..." : (key == -1 ? "None" : TextUtility.getKeyName(key));
        float bindTextWidth = Fonts.REGULAR.getFont(6.0f).width(bindText) + 18.0f;
        actionsX -= (bindTextWidth + 4.0f);
        float bindH = 10.0f;
        float bindY = headerCenterY - bindH / 2.0f;
        boolean bindHovered = GuiUtility.isHovered(actionsX, bindY, bindTextWidth, bindH, context);
        
        this.drawBindButton(context, actionsX, bindY, bindTextWidth, bindH, bindText, bindHovered, alpha);
        if (bindHovered) CursorUtility.set(CursorType.HAND);
        actionsX -= 4.0f;
 
        // 3. Star favorite icon
        actionsX -= 10.0f;
        float starSize = 8.0f;
        float starY = headerCenterY - starSize / 2.0f;
        float starHitX = actionsX + starSize / 2.0f - 8.0f;
        float starHitY = starY + starSize / 2.0f - 8.0f;
        boolean starHovered = GuiUtility.isHovered(starHitX, starHitY, 16.0f, 16.0f, context);
        ColorRGBA starColor = module.isFavorite() ? FAVORITE_YELLOW : EXPENSIVE_MUTED.withAlpha(90.0f);
        context.drawTexture(module.isFavorite() ? STAR_FILL_ICON : STAR_ICON, actionsX, starY, starSize, starSize, starColor.withAlpha(alpha * 255.0f));
        if (starHovered) CursorUtility.set(CursorType.HAND);
 
        // Module Name on far left (neutral coloring, no purple accent)
        ColorRGBA titleColor = module.isEnabled() ? ColorRGBA.WHITE : EXPENSIVE_MUTED;
        ColorRGBA frameColor = module.isEnabled() ? ColorRGBA.WHITE : EXPENSIVE_MUTED;
        float titleH = Fonts.MEDIUM.getFont(7.5f).height();
        float titleY = headerCenterY - titleH / 2.0f;
        float frameSize = 8.0f;
        float frameY = headerCenterY - frameSize / 2.0f;
        context.drawTexture(FRAME_ICON, x + padding, frameY, frameSize, frameSize, frameColor.withAlpha(alpha * 255.0f));
        context.drawText(Fonts.MEDIUM.getFont(7.5f), module.getName(), x + padding + 12.0f, titleY, titleColor.withAlpha(alpha * 255.0f));

        // Render settings underneath
        float currentY = y + 24.0f;
        if (!module.getSettings().isEmpty()) {
            for (moscow.rockstar.systems.setting.Setting setting : module.getSettings()) {
                if (!setting.isVisible()) continue;

                if (setting instanceof moscow.rockstar.systems.setting.settings.BooleanSetting) {
                    moscow.rockstar.systems.setting.settings.BooleanSetting bs = (moscow.rockstar.systems.setting.settings.BooleanSetting) setting;
                    boolean hovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 16.0f, context);

                    float textHeight = Fonts.REGULAR.getFont(7.0f).height();
                    float textY = currentY + (16.0f - textHeight) / 2.0f - 0.5f;
                    
                    context.drawText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(bs.getName()), x + padding, textY, (hovered ? EXPENSIVE_TEXT : EXPENSIVE_MUTED).withAlpha((hovered ? 230.0f : 190.0f) * alpha));

                    // draw checkbox and keyboard icon (no Y coordinate rounding)
                    float boxSize = 10.0f;
                    float boxX = x + width - padding - boxSize;
                    float boxY = currentY + (16.0f - boxSize) / 2.0f;
                    
                    // Draw keyboard icon before checkbox (vertically centered)
                    float kbSize = 8.0f;
                    float kbX = boxX - kbSize - 4.0f;
                    float kbY = boxY + (boxSize - kbSize) / 2.0f;
                    context.drawTexture(KEYBOARD_ICON, kbX, kbY, kbSize, kbSize, (hovered ? EXPENSIVE_TEXT : EXPENSIVE_MUTED).withAlpha((hovered ? 230.0f : 190.0f) * alpha));
                    
                    // Checkbox smooth fill animation
                    Animation anim = this.settingAnimations.computeIfAbsent(bs, s -> new Animation(150L, Easing.BAKEK));
                    anim.update(bs.isEnabled() ? 1.0f : 0.0f);
                    float progress = anim.getRGB();
                    
                    ColorRGBA boxBgColor = EXPENSIVE_FIELD.mix(EXPENSIVE_ACCENT, progress);
                    context.drawRoundedRect(boxX, boxY, boxSize, boxSize, BorderRadius.all(2.0f), boxBgColor.withAlpha(255.0f * alpha));
                    
                    // Blend outline border color with active accent using animation progress to avoid outline gap
                    ColorRGBA boxBorderColor = outlineColor.mix(EXPENSIVE_ACCENT, progress);
                    context.drawRoundedBorder(boxX, boxY, boxSize, boxSize, 1.0f, BorderRadius.all(2.0f), boxBorderColor);
                    
                    if (progress > 0.0f) {
                        float checkSize = 8.0f * progress;
                        float checkX = boxX + (boxSize - checkSize) / 2.0f;
                        float checkY = boxY + (boxSize - checkSize) / 2.0f;
                        context.drawTexture(CHECKMARK_ICON, checkX, checkY, checkSize, checkSize, ColorRGBA.WHITE.withAlpha(alpha * progress * 255.0f));
                    }
                    if (hovered) CursorUtility.set(CursorType.HAND);
                    currentY += 16.0f;


                } else if (setting instanceof moscow.rockstar.systems.setting.settings.SliderSetting) {
                    moscow.rockstar.systems.setting.settings.SliderSetting ss = (moscow.rockstar.systems.setting.settings.SliderSetting) setting;
                    boolean hovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 20.0f, context);

                    // Name on left, value on right
                    context.drawText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(ss.getName()), x + padding, currentY + 1.0f, EXPENSIVE_TEXT.withAlpha(185.0f * alpha));

                    String valStr = String.format("%.2f", ss.getCurrentValue()) + (ss.getSuffix() != null ? " " + ss.getSuffix() : "");
                    context.drawRightText(Fonts.REGULAR.getFont(7.0f), valStr, x + width - padding, currentY + 1.0f, EXPENSIVE_ACCENT.withAlpha(alpha * 255.0f));

                    // Slider bar
                    float barY = currentY + 13.0f;
                    float barWidth = width - padding * 2;
                    float barHeight = 3.0f;
                    float fillWidth = barWidth * ((ss.getCurrentValue() - ss.getMin()) / (ss.getMax() - ss.getMin()));

                    context.drawRoundedRect(x + padding, barY, barWidth, barHeight, BorderRadius.all(1.5f), EXPENSIVE_BG.withAlpha(255.0f * alpha));
                    context.drawRoundedRect(x + padding, barY, fillWidth, barHeight, BorderRadius.all(1.5f), EXPENSIVE_ACCENT.withAlpha(255.0f * alpha));
                    context.drawRoundedRect(x + padding + fillWidth - 2.5f, barY - 1.0f, 5.0f, 5.0f, BorderRadius.all(2.5f), ColorRGBA.WHITE.withAlpha(alpha * 255.0f));

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
                    currentY += 23.0f;

                } else if (setting instanceof moscow.rockstar.systems.setting.settings.RangeSetting) {
                    moscow.rockstar.systems.setting.settings.RangeSetting rs = (moscow.rockstar.systems.setting.settings.RangeSetting) setting;
                    boolean hovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 20.0f, context);

                    // Name on left, value range on right
                    context.drawText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(rs.getName()), x + padding, currentY + 1.0f, EXPENSIVE_TEXT.withAlpha(185.0f * alpha));

                    String valStr = String.format("%.1f - %.1f", rs.getFirstValue(), rs.getSecondValue());
                    context.drawRightText(Fonts.REGULAR.getFont(7.0f), valStr, x + width - padding, currentY + 1.0f, EXPENSIVE_ACCENT.withAlpha(alpha * 255.0f));

                    // Slider track
                    float barY = currentY + 13.0f;
                    float barWidth = width - padding * 2;
                    float barHeight = 3.0f;

                    float p1 = (rs.getFirstValue() - rs.getMin()) / (rs.getMax() - rs.getMin());
                    float p2 = (rs.getSecondValue() - rs.getMin()) / (rs.getMax() - rs.getMin());
                    
                    float fillX = x + padding + Math.min(p1, p2) * barWidth;
                    float fillW = Math.abs(p2 - p1) * barWidth;

                    context.drawRoundedRect(x + padding, barY, barWidth, barHeight, BorderRadius.all(1.5f), EXPENSIVE_BG.withAlpha(255.0f * alpha));
                    context.drawRoundedRect(fillX, barY, fillW, barHeight, BorderRadius.all(1.5f), EXPENSIVE_ACCENT.withAlpha(255.0f * alpha));

                    // Draw both knobs
                    context.drawRoundedRect(x + padding + p1 * barWidth - 2.5f, barY - 1.0f, 5.0f, 5.0f, BorderRadius.all(2.5f), ColorRGBA.WHITE.withAlpha(alpha * 255.0f));
                    context.drawRoundedRect(x + padding + p2 * barWidth - 2.5f, barY - 1.0f, 5.0f, 5.0f, BorderRadius.all(2.5f), ColorRGBA.WHITE.withAlpha(alpha * 255.0f));

                    // Dragging handler
                    if (hovered && GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS && this.draggingRange == null) {
                        this.draggingRange = rs;
                        float clickX = (float) (context.getMouseX() - (x + padding));
                        float k1X = p1 * barWidth;
                        float k2X = p2 * barWidth;
                        if (Math.abs(clickX - k1X) < Math.abs(clickX - k2X)) {
                            this.draggingKnob = 1;
                        } else {
                            this.draggingKnob = 2;
                        }
                    }
                    if (this.draggingRange == rs) {
                        double mouseOffset = context.getMouseX() - (x + padding);
                        float percent = Math.clamp((float) (mouseOffset / barWidth), 0.0f, 1.0f);
                        float newValue = rs.getMin() + percent * (rs.getMax() - rs.getMin());
                        float increment = rs.getStep();
                        newValue = Math.round(newValue / increment) * increment;
                        newValue = Math.clamp(newValue, rs.getMin(), rs.getMax());
                        if (this.draggingKnob == 1) {
                            rs.setFirstValue(newValue);
                        } else {
                            rs.setSecondValue(newValue);
                        }
                    }

                    if (hovered) CursorUtility.set(CursorType.HAND);
                    currentY += 23.0f;

                } else if (setting instanceof moscow.rockstar.systems.setting.settings.SelectSetting) {
                    moscow.rockstar.systems.setting.settings.SelectSetting ss = (moscow.rockstar.systems.setting.settings.SelectSetting) setting;
                    boolean expanded = this.expandedSettings.getOrDefault(ss, false);
                    Animation anim = this.settingAnimations.computeIfAbsent(ss, s -> new Animation(200L, Easing.BAKEK));
                    anim.update(expanded ? 1.0f : 0.0f);
                    float progress = anim.getRGB();

                    boolean headerHovered = GuiUtility.isHovered(x + padding, currentY + 11.0f, width - padding * 2, 15.0f, context);
                    
                    // Name on top
                    context.drawText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(ss.getName()), x + padding, currentY + 1.0f, EXPENSIVE_TEXT.withAlpha(185.0f * alpha));
                    
                    // Decorative multienum icon on the right of title (neutral!)
                    float titleRightX = x + width - padding - 6.0f;
                    float titleRightY = currentY + 1.0f + (Fonts.REGULAR.getFont(7.0f).height() - 6.0f) / 2.0f;
                    context.drawTexture(MULTIENUM_ICON, titleRightX, titleRightY, 6.0f, 6.0f, EXPENSIVE_MUTED.withAlpha(alpha * 120.0f));

                    // Box under
                    context.drawRoundedRect(x + padding, currentY + 11.0f, width - padding * 2, 15.0f, BorderRadius.all(3.0f), EXPENSIVE_FIELD.withAlpha(255.0f * alpha));
                    context.drawRoundedBorder(x + padding, currentY + 11.0f, width - padding * 2, 15.0f, 1.0f, BorderRadius.all(3.0f), outlineColor);

                    // Dropdown closed state enum icon (neutral!)
                    float boxY = currentY + 11.0f;
                    float boxH = 15.0f;
                    float centerY = boxY + boxH / 2.0f;
                    float enumSize = 8.0f;
                    float enumX = x + padding + 4.0f;
                    float enumY = centerY - enumSize / 2.0f;
                    context.drawTexture(MULTIENUM_ICON, enumX, enumY, enumSize, enumSize, EXPENSIVE_TEXT.withAlpha(alpha * 200.0f));

                    // Selected values list string
                    String selectedStr = ss.getSelectedValues().isEmpty() ? "None" : Localizator.translate(ss.getSelectedValues().get(0).getName());
                    if (ss.getSelectedValues().size() > 1) {
                        selectedStr += " +" + (ss.getSelectedValues().size() - 1);
                    }
                    float textHeight = Fonts.REGULAR.getFont(7.0f).height();
                    float textY = centerY - textHeight / 2.0f - 0.5f;
                    context.drawText(Fonts.REGULAR.getFont(7.0f), selectedStr, x + padding + 15.0f, textY, EXPENSIVE_TEXT.withAlpha(alpha * 220.0f));

                    // Dropdown arrow icon
                    float arrowHeight = Fonts.REGULAR.getFont(6.0f).height();
                    float arrowY = centerY - arrowHeight / 2.0f;
                    context.drawText(Fonts.REGULAR.getFont(6.0f), expanded ? "▲" : "▼", x + width - padding - 10.0f, arrowY, Colors.getTextColor().withAlpha(120.0f * alpha));

                    if (headerHovered) CursorUtility.set(CursorType.HAND);

                    float choicesH = ss.getValues().size() * 15.0f + 4.0f;
                    if (progress > 0.0f) {
                        float finalCurrentY = currentY;
                        dropdownDrawables.add(() -> {
                            ScissorUtility.push(context.pose(), x + padding, finalCurrentY + 28.0f, width - padding * 2, progress * choicesH);
                            float choicesY = finalCurrentY + 28.0f;
                            // Draw dropdown box background and border
                            context.drawRoundedRect(x + padding, choicesY, width - padding * 2, progress * choicesH, BorderRadius.all(3.0f), EXPENSIVE_FIELD.withAlpha(255.0f * alpha));
                            context.drawRoundedBorder(x + padding, choicesY, width - padding * 2, progress * choicesH, 1.0f, BorderRadius.all(3.0f), outlineColor);
                            
                            choicesY += 2.0f; // top padding
                            for (moscow.rockstar.systems.setting.settings.SelectSetting.Value value : ss.getValues()) {
                                boolean itemHovered = GuiUtility.isHovered(x + padding, choicesY, width - padding * 2, 15.0f, context);
                                if (value.isSelected()) {
                                    context.drawRoundedRect(x + padding + 2.0f, choicesY + 1.0f, width - padding * 2 - 4.0f, 13.0f, BorderRadius.all(1.5f), EXPENSIVE_ACCENT.withAlpha(60.0f * alpha));
                                    float checkSize = 8.0f;
                                    float checkX = x + width - padding - 14.0f;
                                    float checkY = choicesY + (15.0f - checkSize) / 2.0f;
                                    context.drawTexture(CHECKMARK_ICON, checkX, checkY, checkSize, checkSize, ColorRGBA.WHITE.withAlpha(alpha * 255.0f));
                                }
                                float valTextY = choicesY + (15.0f - Fonts.REGULAR.getFont(7.0f).height()) / 2.0f - 0.5f;
                                context.drawText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(value.getName()), x + padding + 6.0f, valTextY, (itemHovered || value.isSelected() ? EXPENSIVE_TEXT : EXPENSIVE_MUTED).withAlpha((itemHovered || value.isSelected() ? 240.0f : 160.0f) * alpha));

                                choicesY += 15.0f;
                            }
                            ScissorUtility.pop();
                        });
                    }
                    currentY += 28.0f;

                } else if (setting instanceof moscow.rockstar.systems.setting.settings.ModeSetting) {
                    moscow.rockstar.systems.setting.settings.ModeSetting ms = (moscow.rockstar.systems.setting.settings.ModeSetting) setting;
                    boolean expanded = this.expandedSettings.getOrDefault(ms, false);
                    Animation anim = this.settingAnimations.computeIfAbsent(ms, s -> new Animation(200L, Easing.BAKEK));
                    anim.update(expanded ? 1.0f : 0.0f);
                    float progress = anim.getRGB();

                    boolean headerHovered = GuiUtility.isHovered(x + padding, currentY + 11.0f, width - padding * 2, 15.0f, context);
                    
                    // Name on top
                    context.drawText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(ms.getName()), x + padding, currentY + 1.0f, EXPENSIVE_TEXT.withAlpha(185.0f * alpha));

                    // Decorative enum icon on the right of title (neutral!)
                    float titleRightX = x + width - padding - 6.0f;
                    float titleRightY = currentY + 1.0f + (Fonts.REGULAR.getFont(7.0f).height() - 6.0f) / 2.0f;
                    context.drawTexture(ENUM_ICON, titleRightX, titleRightY, 6.0f, 6.0f, EXPENSIVE_MUTED.withAlpha(alpha * 120.0f));

                    // Box under
                    context.drawRoundedRect(x + padding, currentY + 11.0f, width - padding * 2, 15.0f, BorderRadius.all(3.0f), EXPENSIVE_FIELD.withAlpha(255.0f * alpha));
                    context.drawRoundedBorder(x + padding, currentY + 11.0f, width - padding * 2, 15.0f, 1.0f, BorderRadius.all(3.0f), outlineColor);

                    // Dropdown closed state enum icon (neutral!)
                    float boxY = currentY + 11.0f;
                    float boxH = 15.0f;
                    float centerY = boxY + boxH / 2.0f;
                    float enumSize = 8.0f;
                    float enumX = x + padding + 4.0f;
                    float enumY = centerY - enumSize / 2.0f;
                    context.drawTexture(ENUM_ICON, enumX, enumY, enumSize, enumSize, EXPENSIVE_TEXT.withAlpha(alpha * 200.0f));

                    // Current chosen mode value
                    String activeModeName = ms.getValue() != null ? Localizator.translate(ms.getValue().getName()) : "None";
                    float textHeight = Fonts.REGULAR.getFont(7.0f).height();
                    float textY = centerY - textHeight / 2.0f - 0.5f;
                    context.drawText(Fonts.REGULAR.getFont(7.0f), activeModeName, x + padding + 15.0f, textY, EXPENSIVE_TEXT.withAlpha(alpha * 220.0f));

                    // Dropdown arrow icon
                    float arrowHeight = Fonts.REGULAR.getFont(6.0f).height();
                    float arrowY = centerY - arrowHeight / 2.0f;
                    context.drawText(Fonts.REGULAR.getFont(6.0f), expanded ? "▲" : "▼", x + width - padding - 10.0f, arrowY, Colors.getTextColor().withAlpha(120.0f * alpha));

                    if (headerHovered) CursorUtility.set(CursorType.HAND);

                    float choicesH = ms.getValues().size() * 15.0f + 4.0f;
                    if (progress > 0.0f) {
                        float finalCurrentY = currentY;
                        dropdownDrawables.add(() -> {
                            ScissorUtility.push(context.pose(), x + padding, finalCurrentY + 28.0f, width - padding * 2, progress * choicesH);
                            float choicesY = finalCurrentY + 28.0f;
                            // Draw dropdown box background and border
                            context.drawRoundedRect(x + padding, choicesY, width - padding * 2, progress * choicesH, BorderRadius.all(3.0f), EXPENSIVE_FIELD.withAlpha(255.0f * alpha));
                            context.drawRoundedBorder(x + padding, choicesY, width - padding * 2, progress * choicesH, 1.0f, BorderRadius.all(3.0f), outlineColor);
                            
                            choicesY += 2.0f; // top padding
                            for (moscow.rockstar.systems.setting.settings.ModeSetting.Value value : ms.getValues()) {
                                boolean itemHovered = GuiUtility.isHovered(x + padding, choicesY, width - padding * 2, 15.0f, context);
                                if (value.isSelected()) {
                                    context.drawRoundedRect(x + padding + 2.0f, choicesY + 1.0f, width - padding * 2 - 4.0f, 13.0f, BorderRadius.all(1.5f), EXPENSIVE_ACCENT.withAlpha(60.0f * alpha));
                                    float checkSize = 8.0f;
                                    float checkX = x + width - padding - 14.0f;
                                    float checkY = choicesY + (15.0f - checkSize) / 2.0f;
                                    context.drawTexture(CHECKMARK_ICON, checkX, checkY, checkSize, checkSize, ColorRGBA.WHITE.withAlpha(alpha * 255.0f));
                                }
                                float valTextY = choicesY + (15.0f - Fonts.REGULAR.getFont(7.0f).height()) / 2.0f - 0.5f;
                                context.drawText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(value.getName()), x + padding + 6.0f, valTextY, (itemHovered || value.isSelected() ? EXPENSIVE_TEXT : EXPENSIVE_MUTED).withAlpha((itemHovered || value.isSelected() ? 240.0f : 160.0f) * alpha));

                                choicesY += 15.0f;
                            }
                            ScissorUtility.pop();
                        });
                    }
                    currentY += 28.0f;

                } else if (setting instanceof moscow.rockstar.systems.setting.settings.BindSetting) {
                    moscow.rockstar.systems.setting.settings.BindSetting bindSetting = (moscow.rockstar.systems.setting.settings.BindSetting) setting;
                    boolean hovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 18.0f, context);

                    // Label on left
                    float labelTextY = currentY + (18.0f - Fonts.REGULAR.getFont(7.0f).height()) / 2.0f - 0.5f;
                    context.drawText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(bindSetting.getName()), x + padding, labelTextY, EXPENSIVE_TEXT.withAlpha(185.0f * alpha));

                    // Button on right
                    int bindKey = bindSetting.getKey();
                    String settingBindText = (this.bindingSetting == bindSetting) ? "..." : (bindKey == -1 ? "None" : TextUtility.getKeyName(bindKey));
                    float btnW = Fonts.REGULAR.getFont(6.0f).width(settingBindText) + 18.0f;
                    float btnX = x + width - padding - btnW;
                    float btnY = currentY + 3.0f;
                    float btnH = 12.0f;

                    boolean btnHovered = GuiUtility.isHovered(btnX, btnY, btnW, btnH, context);
                    this.drawBindButton(context, btnX, btnY, btnW, btnH, settingBindText, btnHovered, alpha);
                    if (btnHovered) CursorUtility.set(CursorType.HAND);

                    currentY += 18.0f;

                } else if (setting instanceof moscow.rockstar.systems.setting.settings.ColorSetting) {
                    moscow.rockstar.systems.setting.settings.ColorSetting cs = (moscow.rockstar.systems.setting.settings.ColorSetting) setting;
                    boolean hovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 16.0f, context);

                    float labelTextY = currentY + (16.0f - Fonts.REGULAR.getFont(7.0f).height()) / 2.0f - 0.5f;
                    context.drawText(Fonts.REGULAR.getFont(7.0f), Localizator.translate(cs.getName()), x + padding, labelTextY, EXPENSIVE_TEXT.withAlpha(185.0f * alpha));

                    // draw color square preview (sized 10.0f, centered with setting row, same size as checkbox)
                    float boxSize = 10.0f;
                    float boxX = x + width - padding - boxSize;
                    float boxY = currentY + (16.0f - boxSize) / 2.0f;
                    context.drawRoundedRect(boxX, boxY, boxSize, boxSize, BorderRadius.all(2.0f), cs.getColor().withAlpha(alpha * 255.0f));

                    if (hovered) CursorUtility.set(CursorType.HAND);
                    currentY += 16.0f;

                } else if (setting instanceof moscow.rockstar.systems.setting.settings.ButtonSetting) {
                    moscow.rockstar.systems.setting.settings.ButtonSetting bs = (moscow.rockstar.systems.setting.settings.ButtonSetting) setting;
                    boolean hovered = GuiUtility.isHovered(x + padding, currentY, width - padding * 2, 20.0f, context);

                    float btnW = width - padding * 2;
                    float btnH = 14.0f;
                    float btnX = x + padding;
                    float btnY = currentY + 3.0f;

                    ColorRGBA btnBg = hovered ? EXPENSIVE_FIELD.mix(EXPENSIVE_ACCENT, 0.15f) : EXPENSIVE_FIELD;
                    context.drawRoundedRect(btnX, btnY, btnW, btnH, BorderRadius.all(3.0f), btnBg.withAlpha(255.0f * alpha));
                    context.drawRoundedBorder(btnX, btnY, btnW, btnH, 1.0f, BorderRadius.all(3.0f), outlineColor);

                    Font nameFont = Fonts.REGULAR.getFont(7.0f);
                    float textY = btnY + (btnH - nameFont.height()) / 2.0f - 0.5f;
                    context.drawCenteredText(nameFont, Localizator.translate(bs.getName()), btnX + btnW / 2.0f, textY, Colors.getTextColor().withAlpha(255.0f * alpha));

                    if (hovered) CursorUtility.set(CursorType.HAND);
                    currentY += 20.0f;
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
        // Leave empty as requested
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
        float iconSize = 10.0f;
        
        actionsEndX -= (iconSize + 8.0f); // Skip More icon
        
        if (GuiUtility.isHovered(actionsEndX - iconSize - 2.0f, y + 4.0f, iconSize + 4.0f, 20.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
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
        actionsEndX -= (iconSize + 8.0f);

        // 2. Search icon click detection
        if (GuiUtility.isHovered(actionsEndX - iconSize - 2.0f, y + 4.0f, iconSize + 4.0f, 20.0f, mouseX, mouseY) && button == MouseButton.LEFT) {
            this.searchFocused = !this.searchFocused;
            this.searchField.setFocused(this.searchFocused);
            ClientSounds.CLICKGUI_OPEN.play(1.0f, 1.1f);
            return;
        }
        actionsEndX -= (iconSize + 6.0f);


        if (this.searchFocused) {
            this.searchField.onMouseClicked(mouseX, mouseY, button);
        }

        // 3. Tab navigation clicks
        float navBarWidth = 175.0f;
        float navBarHeight = 22.0f;
        float navBarX = x + (width - navBarWidth) / 2.0f;
        float navBarY = y + 7.0f;
        float step = navBarWidth / 7.0f;
        for (int i = 0; i < 7; i++) {
            float tabX = navBarX + i * step;
            if (GuiUtility.isHovered(tabX, navBarY, step, navBarHeight, mouseX, mouseY) && button == MouseButton.LEFT) {
                this.activeTab = i;
                this.scrollHandler.reset();
                this.scrollHandler.scroll(0);
                ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.0f);
                return;
            }
        }

        // Module/Setting keys binding handler
        if (this.bindingModule != null) {
            if (button == MouseButton.LEFT || button == MouseButton.RIGHT) {
                this.bindingModule.setKey(button.getButtonIndex());
                this.bindingModule = null;
                return;
            }
        }
        if (this.bindingSetting != null) {
            if (button == MouseButton.LEFT || button == MouseButton.RIGHT) {
                this.bindingSetting.setKey(button.getButtonIndex());
                this.bindingSetting = null;
                return;
            }
        }

        // Click inside grid content
        float contentX = this.menuWindow.x + CONTENT_PADDING;
        float contentY = this.menuWindow.y + TOP_BAR_HEIGHT + 14.0f;
        float contentWidth = this.menuWindow.getWidth() - CONTENT_PADDING * 2.0f;
        float contentHeight = this.menuWindow.getHeight() - TOP_BAR_HEIGHT - 20.0f;

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

                float colWidth = (contentWidth - CARD_GAP * 2.0f) / 3.0f;
                float[] colHeights = new float[3];
                Arrays.fill(colHeights, 0.0f);
                float scroll = (float) (-this.scrollHandler.getRGB());
                float padding = 7.0f;

                // Pass 1: Check clicks inside expanded dropdown options first
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
                    float[] animatedPos = this.cardPositions.get(module);
                    float cardX = (animatedPos != null) ? animatedPos[0] : (contentX + colIndex * (colWidth + CARD_GAP));
                    float cardY = (animatedPos != null) ? animatedPos[1] : (contentY + minHeight + scroll);

                    float currentSettingY = cardY + 24.0f;
                    for (moscow.rockstar.systems.setting.Setting setting : module.getSettings()) {
                        if (!setting.isVisible()) continue;

                        if (setting instanceof moscow.rockstar.systems.setting.settings.BooleanSetting) {
                            currentSettingY += 16.0f;
                        } else if (setting instanceof moscow.rockstar.systems.setting.settings.SliderSetting) {
                            currentSettingY += 23.0f;
                        } else if (setting instanceof moscow.rockstar.systems.setting.settings.RangeSetting) {
                            currentSettingY += 23.0f;
                        } else if (setting instanceof moscow.rockstar.systems.setting.settings.SelectSetting) {
                            moscow.rockstar.systems.setting.settings.SelectSetting ss = (moscow.rockstar.systems.setting.settings.SelectSetting) setting;
                            boolean expanded = this.expandedSettings.getOrDefault(ss, false);
                            if (expanded) {
                                float choicesH = ss.getValues().size() * 15.0f + 4.0f;
                                if (mouseY >= currentSettingY + 28.0f && mouseY < currentSettingY + 28.0f + choicesH) {
                                    float choicesY = currentSettingY + 28.0f + 2.0f;
                                    for (moscow.rockstar.systems.setting.settings.SelectSetting.Value value : ss.getValues()) {
                                        if (GuiUtility.isHovered(cardX + padding, choicesY, colWidth - padding * 2, 15.0f, mouseX, mouseY)) {
                                            value.toggle();
                                            ClientSounds.CLICKGUI_OPEN.play(0.3f, 1.1f);
                                            return;
                                        }
                                        choicesY += 15.0f;
                                    }
                                }
                            }
                            currentSettingY += 28.0f;
                        } else if (setting instanceof moscow.rockstar.systems.setting.settings.ModeSetting) {
                            moscow.rockstar.systems.setting.settings.ModeSetting ms = (moscow.rockstar.systems.setting.settings.ModeSetting) setting;
                            boolean expanded = this.expandedSettings.getOrDefault(ms, false);
                            if (expanded) {
                                float choicesH = ms.getValues().size() * 15.0f + 4.0f;
                                if (mouseY >= currentSettingY + 28.0f && mouseY < currentSettingY + 28.0f + choicesH) {
                                    float choicesY = currentSettingY + 28.0f + 2.0f;
                                    for (moscow.rockstar.systems.setting.settings.ModeSetting.Value value : ms.getValues()) {
                                        if (GuiUtility.isHovered(cardX + padding, choicesY, colWidth - padding * 2, 15.0f, mouseX, mouseY)) {
                                            value.select();
                                            this.expandedSettings.put(ms, false);
                                            ClientSounds.CLICKGUI_OPEN.play(0.3f, 1.1f);
                                            return;
                                        }
                                        choicesY += 15.0f;
                                    }
                                }
                            }
                            currentSettingY += 28.0f;
                        } else if (setting instanceof moscow.rockstar.systems.setting.settings.BindSetting) {
                            currentSettingY += 18.0f;
                        } else if (setting instanceof moscow.rockstar.systems.setting.settings.ColorSetting) {
                            currentSettingY += 16.0f;
                        } else if (setting instanceof moscow.rockstar.systems.setting.settings.ButtonSetting) {
                            currentSettingY += 20.0f;
                        }
                    }
                    colHeights[colIndex] += cardHeight + CARD_GAP;
                }


                // Reset heights for Pass 2 (normal clicks)
                Arrays.fill(colHeights, 0.0f);

                // Pass 2: Handle header elements and non-expanded setting clicks
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
                    float[] animatedPos = this.cardPositions.get(module);
                    float cardX = (animatedPos != null) ? animatedPos[0] : (contentX + colIndex * (colWidth + CARD_GAP));
                    float cardY = (animatedPos != null) ? animatedPos[1] : (contentY + minHeight + scroll);

                    // If hovered the card y range
                    if (mouseY >= contentY && mouseY <= contentY + contentHeight && GuiUtility.isHovered(cardX, cardY, colWidth, cardHeight, mouseX, mouseY)) {
                        float headerCenterY = cardY + 11.0f;
                        float actionsX = cardX + colWidth - padding;

                        // Switch toggle
                        actionsX -= 18.0f;
                        if (GuiUtility.isHovered(actionsX, headerCenterY - 8.0f, 18.0f, 16.0f, mouseX, mouseY)) {
                            module.toggle();
                            ClientSounds.CLICKGUI_OPEN.play(0.5f, module.isEnabled() ? 1.0f : 0.8f);
                            return;
                        }

                        // Bind button
                        int key = module.getKey();
                        String bindText = (this.bindingModule == module) ? "..." : (key == -1 ? "None" : TextUtility.getKeyName(key));
                        float bindTextWidth = Fonts.REGULAR.getFont(6.0f).width(bindText) + 18.0f;
                        actionsX -= (bindTextWidth + 4.0f);
                        float bindH = 10.0f;
                        float bindY = headerCenterY - bindH / 2.0f;
                        if (GuiUtility.isHovered(actionsX, bindY, bindTextWidth, bindH, mouseX, mouseY)) {
                            this.bindingModule = module;
                            this.bindingSetting = null;
                            return;
                        }

                        // Star favorite button
                        actionsX -= 10.0f;
                        float starSize = 8.0f;
                        float starY = headerCenterY - starSize / 2.0f;
                        float starHitX = actionsX + starSize / 2.0f - 8.0f;
                        float starHitY = starY + starSize / 2.0f - 8.0f;
                        if (GuiUtility.isHovered(starHitX, starHitY, 16.0f, 16.0f, mouseX, mouseY)) {
                            module.setFavorite(!module.isFavorite());
                            Rockstar.getInstance().getFileManager().writeFile("client");
                            ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.3f);
                            return;
                        }

                        // Toggle module if header clicked outside settings
                        if (mouseY < cardY + 24.0f) {
                            module.toggle();
                            return;
                        }

                        // Check clicks inside settings rows
                        float currentSettingY = cardY + 24.0f;

                        for (moscow.rockstar.systems.setting.Setting setting : module.getSettings()) {
                            if (!setting.isVisible()) continue;

                            if (setting instanceof moscow.rockstar.systems.setting.settings.BooleanSetting) {
                                moscow.rockstar.systems.setting.settings.BooleanSetting bs = (moscow.rockstar.systems.setting.settings.BooleanSetting) setting;
                                if (GuiUtility.isHovered(cardX + padding, currentSettingY, colWidth - padding * 2, 16.0f, mouseX, mouseY)) {
                                    bs.setEnabled(!bs.isEnabled());
                                    ClientSounds.CLICKGUI_OPEN.play(0.3f, 1.2f);
                                    return;
                                }
                                currentSettingY += 16.0f;

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.SliderSetting) {
                                currentSettingY += 23.0f;

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.RangeSetting) {
                                currentSettingY += 23.0f;

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.SelectSetting) {
                                moscow.rockstar.systems.setting.settings.SelectSetting ss = (moscow.rockstar.systems.setting.settings.SelectSetting) setting;
                                boolean expanded = this.expandedSettings.getOrDefault(ss, false);

                                if (GuiUtility.isHovered(cardX + padding, currentSettingY + 11.0f, colWidth - padding * 2, 15.0f, mouseX, mouseY)) {
                                    this.expandedSettings.put(ss, !expanded);
                                    ClientSounds.CLICKGUI_OPEN.play(0.4f, 1.0f);
                                    return;
                                }
                                currentSettingY += 28.0f;

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.ModeSetting) {
                                moscow.rockstar.systems.setting.settings.ModeSetting ms = (moscow.rockstar.systems.setting.settings.ModeSetting) setting;
                                boolean expanded = this.expandedSettings.getOrDefault(ms, false);

                                if (GuiUtility.isHovered(cardX + padding, currentSettingY + 11.0f, colWidth - padding * 2, 15.0f, mouseX, mouseY)) {
                                    this.expandedSettings.put(ms, !expanded);
                                    ClientSounds.CLICKGUI_OPEN.play(0.4f, 1.0f);
                                    return;
                                }
                                currentSettingY += 28.0f;

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.BindSetting) {
                                moscow.rockstar.systems.setting.settings.BindSetting bindSetting = (moscow.rockstar.systems.setting.settings.BindSetting) setting;
                                int bindKey = bindSetting.getKey();
                                String settingBindText = (this.bindingSetting == bindSetting) ? "..." : (bindKey == -1 ? "None" : TextUtility.getKeyName(bindKey));
                                float btnW = Fonts.REGULAR.getFont(6.0f).width(settingBindText) + 18.0f;
                                float btnX = cardX + colWidth - padding - btnW;
                                float btnY = currentSettingY + 3.0f;
                                float btnH = 12.0f;

                                if (GuiUtility.isHovered(btnX, btnY, btnW, btnH, mouseX, mouseY)) {
                                    this.bindingSetting = bindSetting;
                                    this.bindingModule = null;
                                    return;
                                }
                                currentSettingY += 18.0f;

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.ColorSetting) {
                                moscow.rockstar.systems.setting.settings.ColorSetting cs = (moscow.rockstar.systems.setting.settings.ColorSetting) setting;
                                if (GuiUtility.isHovered(cardX + padding, currentSettingY, colWidth - padding * 2, 16.0f, mouseX, mouseY)) {
                                    ColorPicker picker = new ColorPicker((float) mouseX, (float) mouseY, 6.0f, cs.isAlpha(), cs.getColor(), Localizator.translate(cs.getName()));
                                    picker.setOnClose(() -> cs.setColor(picker.built()));
                                    this.colorPickers.add(picker);
                                    ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.1f);
                                    return;
                                }
                                currentSettingY += 16.0f;

                            } else if (setting instanceof moscow.rockstar.systems.setting.settings.ButtonSetting) {
                                moscow.rockstar.systems.setting.settings.ButtonSetting bs = (moscow.rockstar.systems.setting.settings.ButtonSetting) setting;
                                if (GuiUtility.isHovered(cardX + padding, currentSettingY + 3.0f, colWidth - padding * 2, 14.0f, mouseX, mouseY)) {
                                    if (bs.getAction() != null) {
                                        bs.getAction().run();
                                    }
                                    ClientSounds.CLICKGUI_OPEN.play(0.5f, 1.2f);
                                    return;
                                }
                                currentSettingY += 20.0f;
                            }
                        }
                    }

                    colHeights[colIndex] += cardHeight + CARD_GAP;
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
            // Themes tab click checks are empty as requested
        }

        // Draggable screen support (top bar only)
        if (GuiUtility.isHovered(this.menuWindow.x, this.menuWindow.y, this.menuWindow.getWidth(), TOP_BAR_HEIGHT, mouseX, mouseY) && button == MouseButton.LEFT) {
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
        this.draggingRange = null;
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

        if (this.bindingSetting != null) {
            if (keyCode == 256 || keyCode == 261) { // Esc or Delete
                this.bindingSetting.setKey(-1);
            } else {
                this.bindingSetting.setKey(keyCode);
            }
            this.bindingSetting = null;
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
        if (this.bindingModule != null || this.bindingSetting != null) {
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
