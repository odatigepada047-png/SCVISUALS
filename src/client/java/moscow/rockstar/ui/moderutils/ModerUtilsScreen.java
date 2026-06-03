package moscow.rockstar.ui.moderutils;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.modules.modules.other.ModerUtils;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.entity.player.Player;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class ModerUtilsScreen extends CustomScreen implements IScaledResolution, IMinecraft {
    private final ModerUtils module;
    private int activeTab = 1;
    private static int activeTheme = 0; // static to persist between opens

    private static final String[] TABS = {"Bot", "SAC Push", "Just Push", "Other"};
    private static final String[] THEMES = {"Default", "Cyber", "Ocean", "Sunset", "Neon"};

    private final String targetPlayerOverride;
    private final boolean isTargetMenu;
    private List<AbstractClientPlayer> playersCache = new ArrayList<>();

    private boolean showingReasons = false;
    private String activePushTarget = null;

    // Bind capture mode
    private boolean bindingMode = false;
    private String bindingSettingName = null;

    public ModerUtilsScreen(ModerUtils module) {
        this(module, null);
    }

    public ModerUtilsScreen(ModerUtils module, String targetPlayer) {
        this.module = module;
        this.targetPlayerOverride = targetPlayer;
        this.isTargetMenu = targetPlayer != null;

        if (mc.level != null && mc.player != null) {
            Vec3 eye = mc.player.getEyePosition(1.0f);

            this.playersCache = mc.level.players().stream()
                    .filter(p -> p != mc.player)
                    .sorted(Comparator.comparingDouble(p -> -mc.player.distanceTo(p)))
                    .toList();
        }
    }

    @Override
    public void render(UIContext context) {
        float width = IScaledResolution.sr.getGuiScaledWidth();
        float height = IScaledResolution.sr.getGuiScaledHeight();

        float panelW = isTargetMenu ? 260.0f : 420.0f;
        float panelH = isTargetMenu ? (showingReasons ? 200.0f : 140.0f) : 320.0f;
        float x = (width - panelW) / 2.0f;
        float y = (height - panelH) / 2.0f;

        ColorRGBA[] theme = getThemeColors(activeTheme);

        // Background shadow
        context.drawShadow(x, y, panelW, panelH, 25.0f, BorderRadius.all(8.0f), ColorRGBA.BLACK.withAlpha(180.0f));

        // Main panel
        context.drawRoundedRect(x, y, panelW, panelH, BorderRadius.all(8.0f), theme[0].withAlpha(230.0f));
        context.drawRoundedBorder(x, y, panelW, panelH, 1.5f, BorderRadius.all(8.0f), theme[1]);

        if (isTargetMenu) {
            float contentX = x + 15.0f;
            float contentY = y + 15.0f;
            float contentW = panelW - 30.0f;
            float contentH = panelH - 30.0f;

            if (showingReasons) {
                drawReasonsMenu(context, contentX, contentY, contentW, contentH, targetPlayerOverride, theme);
            } else {
                drawTargetMenu(context, contentX, contentY, contentW, contentH, theme);
            }
            return;
        }

        // Sidebar
        float sidebarW = 90.0f;
        context.drawRoundedRect(x, y, sidebarW, panelH, BorderRadius.left(8.0f, 8.0f), theme[2].withAlpha(100.0f));
        context.drawRoundedBorder(x, y, sidebarW, panelH, 1.0f, BorderRadius.left(8.0f, 8.0f), theme[1].withAlpha(80.0f));

        // Tab headers
        for (int i = 0; i < TABS.length; i++) {
            float tabY = y + 15.0f + i * 44.0f;
            boolean hovered = isHovered(context.getMouseX(), context.getMouseY(), x + 8, tabY, sidebarW - 16, 36);
            boolean active = activeTab == i;

            if (hovered && !bindingMode) {
                CursorUtility.set(CursorType.HAND);
            }

            if (active) {
                context.drawRoundedRect(x + 8, tabY, sidebarW - 16, 36, BorderRadius.all(6.0f), theme[3]);
                context.drawRoundedBorder(x + 8, tabY, sidebarW - 16, 36, 1.0f, BorderRadius.all(6.0f), theme[3]);
            } else if (hovered) {
                context.drawRoundedRect(x + 8, tabY, sidebarW - 16, 36, BorderRadius.all(6.0f), theme[1].withAlpha(40.0f));
            }

            ColorRGBA textColor = active ? theme[4] : theme[4].mulAlpha(0.6f);
            context.drawCenteredText(Fonts.MEDIUM.getFont(9.5f), TABS[i], x + sidebarW / 2.0f, tabY + 13.0f, textColor);
        }

        // Content area
        float contentX = x + sidebarW + 12.0f;
        float contentY = y + 12.0f;
        float contentW = panelW - sidebarW - 24.0f;
        float contentH = panelH - 24.0f;

        // Content header
        context.drawText(Fonts.BOLD.getFont(13.0f), TABS[activeTab], contentX, contentY + 5.0f, theme[4]);

        if (activeTab == 0) {
            drawBotTab(context, contentX, contentY, contentW, contentH, theme);
        } else if (activeTab == 1) {
            drawSacPushTab(context, contentX, contentY, contentW, contentH, theme);
        } else if (activeTab == 2) {
            drawJustPushTab(context, contentX, contentY, contentW, contentH, theme);
        } else if (activeTab == 3) {
            drawOtherTab(context, contentX, contentY, contentW, contentH, theme);
        }

        // Binding mode overlay
        if (bindingMode) {
            context.drawRoundedRect(x, y, panelW, 30.0f, BorderRadius.top(8.0f, 8.0f), ColorRGBA.BLACK.withAlpha(200.0f));
            context.drawCenteredText(Fonts.BOLD.getFont(10.0f), "Нажмите любую клавишу для бинда...", width / 2.0f, y + 10.0f, theme[3]);
        }
    }

    private ColorRGBA[] getThemeColors(int themeIndex) {
        // bg, border, sidebar, accent, text
        switch (themeIndex) {
            case 1: // Cyber
                return new ColorRGBA[]{
                    new ColorRGBA(10, 15, 25, 255),
                    new ColorRGBA(0, 255, 255, 255),
                    new ColorRGBA(0, 30, 50, 255),
                    new ColorRGBA(0, 255, 255, 150),
                    new ColorRGBA(200, 255, 255, 255)
                };
            case 2: // Ocean
                return new ColorRGBA[]{
                    new ColorRGBA(15, 30, 50, 255),
                    new ColorRGBA(50, 150, 255, 255),
                    new ColorRGBA(20, 60, 100, 255),
                    new ColorRGBA(50, 150, 255, 150),
                    new ColorRGBA(200, 230, 255, 255)
                };
            case 3: // Sunset
                return new ColorRGBA[]{
                    new ColorRGBA(30, 15, 20, 255),
                    new ColorRGBA(255, 100, 50, 255),
                    new ColorRGBA(60, 30, 40, 255),
                    new ColorRGBA(255, 100, 50, 150),
                    new ColorRGBA(255, 200, 180, 255)
                };
            case 4: // Neon
                return new ColorRGBA[]{
                    new ColorRGBA(15, 10, 25, 255),
                    new ColorRGBA(255, 0, 200, 255),
                    new ColorRGBA(40, 15, 60, 255),
                    new ColorRGBA(255, 0, 200, 150),
                    new ColorRGBA(255, 200, 255, 255)
                };
            default: // Default
                return new ColorRGBA[]{
                    Colors.getAdditionalColor().mulAlpha(0.85f),
                    Colors.getOutlineColor(),
                    Colors.getAdditionalColor(),
                    Colors.getAccentColor(),
                    Colors.getTextColor()
                };
        }
    }

    private void drawTargetMenu(UIContext context, float x, float y, float w, float h, ColorRGBA[] theme) {
        context.drawText(Fonts.BOLD.getFont(11.0f), "Цель: " + targetPlayerOverride, x + 5.0f, y + 5.0f, theme[4]);

        // SAC Push
        float sacY = y + 25.0f;
        boolean sacHover = isHovered(context.getMouseX(), context.getMouseY(), x, sacY, w, 26.0f);
        if (sacHover && !bindingMode) CursorUtility.set(CursorType.HAND);
        ColorRGBA sacColor = sacHover ? theme[3] : theme[2].withAlpha(140.0f);
        context.drawRoundedRect(x, sacY, w, 26.0f, BorderRadius.all(5.0f), sacColor);
        context.drawRoundedBorder(x, sacY, w, 26.0f, 1.0f, BorderRadius.all(5.0f), theme[1]);
        context.drawCenteredText(Fonts.MEDIUM.getFont(9.5f), "SAC Punish", x + w / 2.0f, sacY + 9.0f, theme[4]);

        // Just Push
        float pushY = y + 57.0f;
        boolean pushHover = isHovered(context.getMouseX(), context.getMouseY(), x, pushY, w, 26.0f);
        if (pushHover && !bindingMode) CursorUtility.set(CursorType.HAND);
        ColorRGBA pushColor = pushHover ? theme[3] : theme[2].withAlpha(140.0f);
        context.drawRoundedRect(x, pushY, w, 26.0f, BorderRadius.all(5.0f), pushColor);
        context.drawRoundedBorder(x, pushY, w, 26.0f, 1.0f, BorderRadius.all(5.0f), theme[1]);
        context.drawCenteredText(Fonts.MEDIUM.getFont(9.5f), "Just Push", x + w / 2.0f, pushY + 9.0f, theme[4]);

        // Dupe IP
        float dupeY = y + 89.0f;
        boolean dupeHover = isHovered(context.getMouseX(), context.getMouseY(), x, dupeY, w, 26.0f);
        if (dupeHover && !bindingMode) CursorUtility.set(CursorType.HAND);
        ColorRGBA dupeColor = dupeHover ? theme[3] : theme[2].withAlpha(140.0f);
        context.drawRoundedRect(x, dupeY, w, 26.0f, BorderRadius.all(5.0f), dupeColor);
        context.drawRoundedBorder(x, dupeY, w, 26.0f, 1.0f, BorderRadius.all(5.0f), theme[1]);
        context.drawCenteredText(Fonts.MEDIUM.getFont(9.5f), "Dupe IP", x + w / 2.0f, dupeY + 9.0f, theme[4]);
    }

    private void drawReasonsMenu(UIContext context, float x, float y, float w, float h, String target, ColorRGBA[] theme) {
        context.drawText(Fonts.BOLD.getFont(10.5f), "Причина: " + target, x + 5.0f, y + 3.0f, theme[4]);

        String[] reasons = {"2.2", "2.2.2", "2.2.3", "2.8"};
        String[] desc = {"Слизь", "Магма", "Иглобрюх", "Слеза"};

        float startY = y + 20.0f;
        for (int i = 0; i < reasons.length; i++) {
            float btnY = startY + i * 28.0f;
            boolean hovered = isHovered(context.getMouseX(), context.getMouseY(), x, btnY, w, 24.0f);
            if (hovered && !bindingMode) CursorUtility.set(CursorType.HAND);

            ColorRGBA btnColor = hovered ? theme[3] : theme[2].withAlpha(140.0f);
            context.drawRoundedRect(x, btnY, w, 24.0f, BorderRadius.all(5.0f), btnColor);
            context.drawRoundedBorder(x, btnY, w, 24.0f, 1.0f, BorderRadius.all(5.0f), theme[1]);
            context.drawText(Fonts.MEDIUM.getFont(9.0f), reasons[i] + " - " + desc[i], x + 10.0f, btnY + 7.0f, theme[4]);
        }

        // Back
        float backY = startY + reasons.length * 28.0f + 8.0f;
        boolean backHover = isHovered(context.getMouseX(), context.getMouseY(), x, backY, w, 22.0f);
        if (backHover && !bindingMode) CursorUtility.set(CursorType.HAND);
        context.drawRoundedRect(x, backY, w, 22.0f, BorderRadius.all(4.0f), theme[1].withAlpha(50.0f));
        context.drawRoundedBorder(x, backY, w, 22.0f, 1.0f, BorderRadius.all(4.0f), theme[1]);
        context.drawCenteredText(Fonts.MEDIUM.getFont(8.5f), "Назад", x + w / 2.0f, backY + 7.0f, theme[4].mulAlpha(0.8f));
    }

    private void drawBotTab(UIContext context, float x, float y, float w, float h, ColorRGBA[] theme) {
        float contentY = y + 30.0f;
        context.drawRoundedRect(x, contentY, w, h - 40.0f, BorderRadius.all(6.0f), theme[2].withAlpha(80.0f));
        context.drawRoundedBorder(x, contentY, w, h - 40.0f, 1.0f, BorderRadius.all(6.0f), theme[1].withAlpha(50.0f));
        context.drawCenteredText(Fonts.REGULAR.getFont(9.5f), "Bot Manager", x + w / 2.0f, contentY + 50.0f, theme[4].mulAlpha(0.5f));
    }

    private void drawSacPushTab(UIContext context, float x, float y, float w, float h, ColorRGBA[] theme) {
        float listY = y + 30.0f;
        int maxShow = 7;

        if (playersCache.isEmpty()) {
            context.drawCenteredText(Fonts.REGULAR.getFont(9.5f), "Нет игроков рядом", x + w / 2.0f, listY + 40.0f, theme[4].mulAlpha(0.5f));
            return;
        }

        for (int i = 0; i < Math.min(playersCache.size(), maxShow); i++) {
            Player p = playersCache.get(i);
            float itemY = listY + i * 32.0f;
            boolean hovered = isHovered(context.getMouseX(), context.getMouseY(), x, itemY, w, 28.0f);

            if (hovered && !bindingMode) CursorUtility.set(CursorType.HAND);

            if (hovered) {
                context.drawRoundedRect(x, itemY, w, 28.0f, BorderRadius.all(5.0f), theme[1].withAlpha(50.0f));
            } else {
                context.drawRoundedRect(x, itemY, w, 28.0f, BorderRadius.all(5.0f), theme[2].withAlpha(100.0f));
            }
            context.drawRoundedBorder(x, itemY, w, 28.0f, 1.0f, BorderRadius.all(5.0f), theme[1].withAlpha(60.0f));

            context.drawHead(p, x + 6.0f, itemY + 4.0f, 20.0f, BorderRadius.all(3.0f), ColorRGBA.WHITE);
            context.drawText(Fonts.MEDIUM.getFont(9.0f), p.getName().getString(), x + 32.0f, itemY + 9.0f, theme[4]);

            String dist = String.format("%.1fm", mc.player.distanceTo(p));
            context.drawRightText(Fonts.REGULAR.getFont(8.0f), dist, x + w - 8.0f, itemY + 10.0f, theme[4].mulAlpha(0.6f));
        }
    }

    private void drawJustPushTab(UIContext context, float x, float y, float w, float h, ColorRGBA[] theme) {
        if (showingReasons && activePushTarget != null) {
            drawReasonsMenu(context, x, y + 25.0f, w, h - 30.0f, activePushTarget, theme);
            return;
        }

        drawSacPushTab(context, x, y, w, h, theme);
    }

    private void drawOtherTab(UIContext context, float x, float y, float w, float h, ColorRGBA[] theme) {
        float contentY = y + 30.0f;

        // Theme selector - move label ABOVE buttons
        float themeLabelY = contentY;
        context.drawText(Fonts.MEDIUM.getFont(9.5f), "Тема меню:", x, themeLabelY, theme[4]);

        float themeBtnY = themeLabelY + 12.0f;
        float themeX = x;
        for (int i = 0; i < THEMES.length; i++) {
            float btnW = 55.0f;
            boolean hovered = isHovered(context.getMouseX(), context.getMouseY(), themeX, themeBtnY, btnW, 20.0f);
            if (hovered && !bindingMode) CursorUtility.set(CursorType.HAND);

            ColorRGBA btnColor = activeTheme == i ? theme[3] : theme[2].withAlpha(100.0f);
            context.drawRoundedRect(themeX, themeBtnY, btnW, 20.0f, BorderRadius.all(4.0f), btnColor);
            context.drawRoundedBorder(themeX, themeBtnY, btnW, 20.0f, 1.0f, BorderRadius.all(4.0f), theme[1]);
            context.drawCenteredText(Fonts.REGULAR.getFont(7.5f), THEMES[i], themeX + btnW / 2.0f, themeBtnY + 8.0f, theme[4]);

            themeX += btnW + 5.0f;
        }

        // Bind settings
        float bindY = themeBtnY + 35.0f;
        context.drawText(Fonts.MEDIUM.getFont(9.5f), "Бинды:", x, bindY, theme[4]);

        String[] bindLabels = {"Lock Target", "SAC Punish", "Dupe IP", "Just Push", "Open Menu"};
        BindSetting[] binds = {
            module.lockTargetBind,
            module.sacPunish,
            module.dupeIpBind,
            module.pushSlime,
            module.openBind
        };

        float itemY = bindY + 15.0f;
        for (int i = 0; i < bindLabels.length; i++) {
            boolean hovered = isHovered(context.getMouseX(), context.getMouseY(), x, itemY, w, 24.0f);
            if (hovered && !bindingMode) CursorUtility.set(CursorType.HAND);

            context.drawRoundedRect(x, itemY, w, 24.0f, BorderRadius.all(4.0f), theme[2].withAlpha(80.0f));
            context.drawRoundedBorder(x, itemY, w, 24.0f, 1.0f, BorderRadius.all(4.0f), theme[1].withAlpha(50.0f));

            context.drawText(Fonts.REGULAR.getFont(9.0f), bindLabels[i], x + 8.0f, itemY + 7.0f, theme[4]);

            // Current key - show actual key name
            int keyVal = binds[i].getKey();
            String keyName = keyVal > 0 ? getKeyName(keyVal) : "None";
            ColorRGBA keyColor = hovered ? theme[3] : theme[4].mulAlpha(0.7f);
            context.drawRightText(Fonts.MEDIUM.getFont(8.5f), "[ " + keyName + " ]", x + w - 8.0f, itemY + 8.0f, keyColor);

            itemY += 28.0f;
        }

        // Hint text
        float hintY = itemY + 10.0f;
        context.drawText(Fonts.REGULAR.getFont(8.0f), "ЛКМ/СКМ на функцию для бинда", x, hintY, theme[4].mulAlpha(0.6f));
    }

    // Helper to convert key code to readable name
    private String getKeyName(int keyCode) {
        switch (keyCode) {
            case 256: return "ESC";
            case 257: return "ENTER";
            case 258: return "TAB";
            case 259: return "BACK";
            case 260: return "CAPS";
            case 261: return "SPACE";
            case 262: return "LSHIFT";
            case 263: return "LSHIFT";
            case 264: return "RSHIFT";
            case 265: return "UP";
            case 266: return "DOWN";
            case 267: return "LEFT";
            case 268: return "RIGHT";
            case 269: return "INSERT";
            case 270: return "HOME";
            case 271: return "PGUP";
            case 272: return "DEL";
            case 273: return "END";
            case 274: return "PGDN";
            default:
                if (keyCode >= 48 && keyCode <= 57) return String.valueOf((char)keyCode);
                if (keyCode >= 65 && keyCode <= 90) return String.valueOf((char)keyCode);
                if (keyCode >= 1 && keyCode <= 24) return "M" + keyCode;
                return "Key" + keyCode;
        }
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (bindingMode) {
            bindingMode = false;
            bindingSettingName = null;
            return;
        }

        if (button == MouseButton.RIGHT) {
            mc.setScreen(null);
            return;
        }

        // Handle LEFT or MIDDLE mouse click for bind capture in Other tab
        if ((button == MouseButton.MIDDLE || button == MouseButton.LEFT) && activeTab == 3) {
            float width = IScaledResolution.sr.getGuiScaledWidth();
            float height = IScaledResolution.sr.getGuiScaledHeight();
            float panelW = 420.0f;
            float panelH = 320.0f;
            float x = (width - panelW) / 2.0f;
            float y = (height - panelH) / 2.0f;

            float bindY = y + 96.0f;
            BindSetting[] binds = {
                module.lockTargetBind,
                module.sacPunish,
                module.dupeIpBind,
                module.pushSlime,
                module.openBind
            };

            for (int i = 0; i < binds.length; i++) {
                if (isHovered(mouseX, mouseY, x + 102.0f, bindY + i * 28.0f, 278.0f, 24.0f)) {
                    mc.setScreen(new BindCaptureScreen(binds[i], this));
                    return;
                }
            }
            return;
        }

        if (button != MouseButton.LEFT) return;

        float width = IScaledResolution.sr.getGuiScaledWidth();
        float height = IScaledResolution.sr.getGuiScaledHeight();

        float panelW = isTargetMenu ? 260.0f : 420.0f;
        float panelH = isTargetMenu ? (showingReasons ? 200.0f : 140.0f) : 320.0f;
        float x = (width - panelW) / 2.0f;
        float y = (height - panelH) / 2.0f;

        if (isTargetMenu) {
            handleTargetMenuClick(mouseX, mouseY, x, y, panelW, panelH);
            return;
        }

        // Sidebar tabs
        float sidebarW = 90.0f;
        for (int i = 0; i < TABS.length; i++) {
            float tabY = y + 15.0f + i * 44.0f;
            if (isHovered(mouseX, mouseY, x + 8, tabY, sidebarW - 16, 36)) {
                activeTab = i;
                showingReasons = false;
                activePushTarget = null;
                return;
            }
        }

        float contentX = x + sidebarW + 12.0f;
        float contentY = y + 12.0f;

        if (activeTab == 1) {
            handleSacPushClick(mouseX, mouseY, contentX, contentY);
        } else if (activeTab == 2) {
            handleJustPushClick(mouseX, mouseY, contentX, contentY);
        } else if (activeTab == 3) {
            handleOtherClick(mouseX, mouseY, x, y, panelW, panelH);
        }
    }

    private void handleTargetMenuClick(double mx, double my, float x, float y, float w, float h) {
        float contentX = x + 15.0f;
        float contentY = y + 15.0f;

        if (showingReasons) {
            String[] reasons = {"2.2", "2.2.2", "2.2.3", "2.8"};
            float startY = contentY + 20.0f;
            for (int i = 0; i < reasons.length; i++) {
                float btnY = startY + i * 28.0f;
                if (isHovered(mx, my, contentX, btnY, w - 30.0f, 24.0f)) {
                    module.executePush(targetPlayerOverride, reasons[i]);
                    mc.setScreen(null);
                    return;
                }
            }
            float backY = startY + reasons.length * 28.0f + 8.0f;
            if (isHovered(mx, my, contentX, backY, w - 30.0f, 22.0f)) {
                showingReasons = false;
            }
            return;
        }

        // SAC Push
        if (isHovered(mx, my, contentX, contentY + 25.0f, w - 30.0f, 26.0f)) {
            module.setLastPunishTarget(targetPlayerOverride);
            if (mc.player.connection != null) {
                mc.player.connection.sendCommand("sac punish " + targetPlayerOverride);
            }
            mc.setScreen(null);
            return;
        }

        // Just Push
        if (isHovered(mx, my, contentX, contentY + 57.0f, w - 30.0f, 26.0f)) {
            showingReasons = true;
            return;
        }

        // Dupe IP
        if (isHovered(mx, my, contentX, contentY + 89.0f, w - 30.0f, 26.0f)) {
            if (mc.player.connection != null) {
                mc.player.connection.sendCommand("dupeip " + targetPlayerOverride);
            }
            mc.setScreen(null);
        }
    }

    private void handleSacPushClick(double mx, double my, float cx, float cy) {
        float listY = cy + 30.0f;
        int maxShow = 7;

        for (int i = 0; i < Math.min(playersCache.size(), maxShow); i++) {
            float itemY = listY + i * 32.0f;
            if (isHovered(mx, my, cx, itemY, 278.0f, 28.0f)) {
                String name = playersCache.get(i).getName().getString();
                module.setLastPunishTarget(name);
                if (mc.player.connection != null) {
                    mc.player.connection.sendCommand("sac punish " + name);
                }
                mc.setScreen(null);
                return;
            }
        }
    }

    private void handleJustPushClick(double mx, double my, float cx, float cy) {
        if (showingReasons && activePushTarget != null) {
            String[] reasons = {"2.2", "2.2.2", "2.2.3", "2.8"};
            float startY = cy + 55.0f;
            for (int i = 0; i < reasons.length; i++) {
                float btnY = startY + i * 28.0f;
                if (isHovered(mx, my, cx, btnY, 278.0f, 24.0f)) {
                    module.executePush(activePushTarget, reasons[i]);
                    mc.setScreen(null);
                    return;
                }
            }
            float backY = startY + reasons.length * 28.0f + 8.0f;
            if (isHovered(mx, my, cx, backY, 278.0f, 22.0f)) {
                showingReasons = false;
                activePushTarget = null;
            }
            return;
        }

        float listY = cy + 30.0f;
        int maxShow = 7;

        for (int i = 0; i < Math.min(playersCache.size(), maxShow); i++) {
            float itemY = listY + i * 32.0f;
            if (isHovered(mx, my, cx, itemY, 278.0f, 28.0f)) {
                activePushTarget = playersCache.get(i).getName().getString();
                showingReasons = true;
                return;
            }
        }
    }

    private void handleOtherClick(double mx, double my, float x, float y, float w, float h) {
        // Theme clicks
        float themeY = y + 44.0f;
        float themeX = x + 102.0f;
        for (int i = 0; i < THEMES.length; i++) {
            if (isHovered(mx, my, themeX, themeY, 55.0f, 20.0f)) {
                activeTheme = i;
                return;
            }
            themeX += 60.0f;
        }

        // Note: Middle mouse binding is handled in onMouseClicked with MouseButton.MIDDLE
    }

    public boolean keyPressed(KeyEvent event) {
        if (bindingMode && bindingSettingName != null) {
            applyBind(event.key());
            bindingMode = false;
            bindingSettingName = null;
            return true;
        }
        return super.keyPressed(event);
    }

    private void applyBind(int keyCode) {
        // Bind will be set via BindCaptureScreen
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }
}