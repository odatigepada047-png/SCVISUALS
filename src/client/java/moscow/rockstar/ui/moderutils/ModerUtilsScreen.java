package moscow.rockstar.ui.moderutils;

import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.modules.modules.other.ModerUtils;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.List;
import java.util.ArrayList;

public class ModerUtilsScreen extends CustomScreen implements IScaledResolution, IMinecraft {
    private final ModerUtils module;
    private int activeTab = 1; // Default to SAC Push tab

    private static final String[] TABS = {"Bot", "SAC Push", "Just push", "AutoForma"};

    private final String targetPlayerOverride;
    private final boolean isTargetMenu;
    private List<AbstractClientPlayer> playersCache = new ArrayList<>();

    private boolean showingReasons = false;
    private String activePushTarget = null;

    public ModerUtilsScreen(ModerUtils module) {
        this(module, null);
    }

    public ModerUtilsScreen(ModerUtils module, String targetPlayer) {
        this.module = module;
        this.targetPlayerOverride = targetPlayer;
        this.isTargetMenu = targetPlayer != null;

        // Capture a static list of players upon opening
        if (mc.level != null && mc.player != null) {
            Vec3 look = MathUtility.getVectorForRotation(mc.player.getXRot(), mc.player.getYRot());
            Vec3 eye = mc.player.getEyePosition(1.0f);

            this.playersCache = mc.level.players().stream()
                    .filter(p -> p != mc.player)
                    .sorted((p1, p2) -> {
                        Vec3 d1 = p1.getEyePosition(1.0f).subtract(eye).normalize();
                        Vec3 d2 = p2.getEyePosition(1.0f).subtract(eye).normalize();
                        double dot1 = look.dot(d1);
                        double dot2 = look.dot(d2);
                        return Double.compare(dot2, dot1);
                    })
                    .toList();
        }
    }

    @Override
    public void render(UIContext context) {
        float width = IScaledResolution.sr.getGuiScaledWidth();
        float height = IScaledResolution.sr.getGuiScaledHeight();

        float panelW = isTargetMenu ? 240.0f : 400.0f;
        float panelH = isTargetMenu ? (showingReasons ? 180.0f : 120.0f) : 280.0f;
        float x = (width - panelW) / 2.0f;
        float y = (height - panelH) / 2.0f;

        // Draw shadow
        context.drawShadow(x, y, panelW, panelH, 20.0f, BorderRadius.all(6.0f), ColorRGBA.BLACK.withAlpha(150.0f));
        // Draw client glassmorphism/minimalism background integrating with the Interface module settings
        context.drawClientRect(x, y, panelW, panelH, 1.0f, 0.0f, 5.0f);
        // Draw black border around GUI card
        context.drawRoundedBorder(x, y, panelW, panelH, 1.5f, BorderRadius.all(6.0f), ColorRGBA.BLACK);

        if (isTargetMenu) {
            float contentX = x + 15.0f;
            float contentY = y + 15.0f;
            float contentW = panelW - 30.0f;
            float contentH = panelH - 30.0f;

            if (showingReasons) {
                drawReasonsMenu(context, contentX, contentY, contentW, contentH, targetPlayerOverride);
            } else {
                drawTargetMenu(context, contentX, contentY, contentW, contentH);
            }
            return;
        }

        // Draw Sidebar background
        float sidebarW = 100.0f;
        context.drawSquircle(x, y, sidebarW, panelH, 5.0f, BorderRadius.left(6.0f, 6.0f), Colors.getAdditionalColor().withAlpha(150.0f));
        context.drawRect(x + sidebarW - 1.0f, y, 1.0f, panelH, Colors.getOutlineColor());

        // Draw Sidebar tab headers
        for (int i = 0; i < TABS.length; i++) {
            float tabY = y + 20.0f + i * 40.0f;
            boolean hovered = isHovered(context.getMouseX(), context.getMouseY(), x + 10, tabY, sidebarW - 20, 30);
            boolean active = activeTab == i;

            if (hovered) {
                CursorUtility.set(CursorType.HAND);
            }

            // Draw active/hover background
            if (active) {
                context.drawSquircle(x + 10, tabY, sidebarW - 20, 30, 7.0f, BorderRadius.all(6.0f), Colors.getAccentColor());
            } else if (hovered) {
                context.drawSquircle(x + 10, tabY, sidebarW - 20, 30, 7.0f, BorderRadius.all(6.0f), Colors.getOutlineColor().withAlpha(60.0f));
            }

            ColorRGBA textColor = active ? Colors.getTextColor() : Colors.getTextColor().mulAlpha(0.6f);
            context.drawCenteredText(Fonts.MEDIUM.getFont(10.0f), TABS[i], x + sidebarW / 2.0f, tabY + 11.0f, textColor);
        }

        // Draw tab content
        float contentX = x + sidebarW + 15.0f;
        float contentY = y + 15.0f;
        float contentW = panelW - sidebarW - 30.0f;
        float contentH = panelH - 30.0f;

        if (activeTab == 0) {
            drawBotTab(context, contentX, contentY, contentW, contentH);
        } else if (activeTab == 1) {
            drawSacPushTab(context, contentX, contentY, contentW, contentH);
        } else if (activeTab == 2) {
            drawJustPushTab(context, contentX, contentY, contentW, contentH);
        } else if (activeTab == 3) {
            drawAutoFormaTab(context, contentX, contentY, contentW, contentH);
        }
    }

    private void drawTargetMenu(UIContext context, float x, float y, float w, float h) {
        context.drawCenteredText(Fonts.BOLD.getFont(12.0f), "Цель: " + targetPlayerOverride, x + w / 2.0f, y + 5.0f, Colors.getTextColor());

        // 1. SAC Push Button
        float sacY = y + 25.0f;
        boolean sacHovered = isHovered(context.getMouseX(), context.getMouseY(), x, sacY, w, 24.0f);
        if (sacHovered) {
            CursorUtility.set(CursorType.HAND);
        }
        ColorRGBA sacColor = sacHovered ? Colors.getAccentColor() : Colors.getAdditionalColor().withAlpha(120.0f);
        context.drawSquircle(x, sacY, w, 24.0f, 6.0f, BorderRadius.all(5.0f), sacColor);
        context.drawRoundedBorder(x, sacY, w, 24.0f, 1.0f, BorderRadius.all(5.0f), Colors.getOutlineColor());
        context.drawCenteredText(Fonts.MEDIUM.getFont(9.5f), "SAC Punish", x + w / 2.0f, sacY + 7.5f, Colors.getTextColor());

        // 2. Just Push Button
        float pushY = y + 55.0f;
        boolean pushHovered = isHovered(context.getMouseX(), context.getMouseY(), x, pushY, w, 24.0f);
        if (pushHovered) {
            CursorUtility.set(CursorType.HAND);
        }
        ColorRGBA pushColor = pushHovered ? Colors.getAccentColor() : Colors.getAdditionalColor().withAlpha(120.0f);
        context.drawSquircle(x, pushY, w, 24.0f, 6.0f, BorderRadius.all(5.0f), pushColor);
        context.drawRoundedBorder(x, pushY, w, 24.0f, 1.0f, BorderRadius.all(5.0f), Colors.getOutlineColor());
        context.drawCenteredText(Fonts.MEDIUM.getFont(9.5f), "Just Push", x + w / 2.0f, pushY + 7.5f, Colors.getTextColor());
    }

    private void drawReasonsMenu(UIContext context, float x, float y, float w, float h, String targetName) {
        context.drawCenteredText(Fonts.BOLD.getFont(11.0f), "Причина для " + targetName, x + w / 2.0f, y + 2.0f, Colors.getTextColor());

        String[] reasons = {"2.2", "2.2.2", "2.2.3", "2.8"};
        String[] descriptions = {"Слизь (2.2)", "Магма крем (2.2.2)", "Иглобрюх (2.2.3)", "Слеза гаста (2.8)"};

        float startY = y + 18.0f;
        for (int i = 0; i < reasons.length; i++) {
            float btnY = startY + i * 26.0f;
            boolean hovered = isHovered(context.getMouseX(), context.getMouseY(), x, btnY, w, 22.0f);

            if (hovered) {
                CursorUtility.set(CursorType.HAND);
            }

            ColorRGBA btnColor = hovered ? Colors.getAccentColor() : Colors.getAdditionalColor().withAlpha(120.0f);
            context.drawSquircle(x, btnY, w, 22.0f, 6.0f, BorderRadius.all(5.0f), btnColor);
            context.drawRoundedBorder(x, btnY, w, 22.0f, 1.0f, BorderRadius.all(5.0f), Colors.getOutlineColor());

            context.drawCenteredText(Fonts.MEDIUM.getFont(9.0f), descriptions[i], x + w / 2.0f, btnY + 7.0f, Colors.getTextColor());
        }

        // Back button
        float backY = startY + reasons.length * 26.0f + 5.0f;
        boolean backHovered = isHovered(context.getMouseX(), context.getMouseY(), x, backY, w, 20.0f);
        if (backHovered) {
            CursorUtility.set(CursorType.HAND);
        }
        ColorRGBA backColor = backHovered ? Colors.getOutlineColor().withAlpha(120.0f) : Colors.getOutlineColor().withAlpha(60.0f);
        context.drawSquircle(x, backY, w, 20.0f, 5.0f, BorderRadius.all(4.0f), backColor);
        context.drawRoundedBorder(x, backY, w, 20.0f, 1.0f, BorderRadius.all(4.0f), Colors.getOutlineColor());
        context.drawCenteredText(Fonts.MEDIUM.getFont(8.5f), "Назад", x + w / 2.0f, backY + 6.0f, Colors.getTextColor().mulAlpha(0.8f));
    }

    private void handleReasonsClick(double mouseX, double mouseY, float x, float y, float w, float h, String targetName) {
        String[] reasons = {"2.2", "2.2.2", "2.2.3", "2.8"};
        float startY = y + 18.0f;
        for (int i = 0; i < reasons.length; i++) {
            float btnY = startY + i * 26.0f;
            if (isHovered(mouseX, mouseY, x, btnY, w, 22.0f)) {
                module.executePush(targetName, reasons[i]);
                mc.setScreen(null);
                return;
            }
        }

        // Back click
        float backY = startY + reasons.length * 26.0f + 5.0f;
        if (isHovered(mouseX, mouseY, x, backY, w, 20.0f)) {
            showingReasons = false;
            if (!isTargetMenu) {
                activePushTarget = null;
            }
        }
    }

    private void drawBotTab(UIContext context, float x, float y, float w, float h) {
        context.drawText(Fonts.BOLD.getFont(14.0f), "Bot Manager", x, y + 5.0f, Colors.getTextColor());
        context.drawText(Fonts.REGULAR.getFont(9.0f), "Автоматический бот для поиска ресурсов", x, y + 25.0f, Colors.getTextColor().mulAlpha(0.5f));

        float groupY = y + 45.0f;
        context.drawRoundedRect(x, groupY, w, 180.0f, BorderRadius.all(6.0f), Colors.getAdditionalColor());
        context.drawRoundedBorder(x, groupY, w, 180.0f, 1.0f, BorderRadius.all(6.0f), Colors.getOutlineColor());

        context.drawCenteredText(Fonts.REGULAR.getFont(10.0f), "Bot settings will appear here", x + w/2.0f, groupY + 80.0f, Colors.getTextColor().mulAlpha(0.4f));
    }

    private void drawSacPushTab(UIContext context, float x, float y, float w, float h) {
        context.drawText(Fonts.BOLD.getFont(14.0f), "SAC Punish", x, y + 5.0f, Colors.getTextColor());
        context.drawText(Fonts.REGULAR.getFont(9.0f), "Выберите игрока для отправки /sac punish", x, y + 25.0f, Colors.getTextColor().mulAlpha(0.5f));

        if (playersCache.isEmpty()) {
            context.drawCenteredText(Fonts.REGULAR.getFont(10.0f), "Игроки в зоне рендера не найдены", x + w/2.0f, y + h/2.0f + 10.0f, Colors.getTextColor().mulAlpha(0.4f));
            return;
        }

        // Draw player entries
        float listY = y + 45.0f;
        int maxFit = 6;
        for (int i = 0; i < Math.min(playersCache.size(), maxFit); i++) {
            Player player = playersCache.get(i);
            float itemY = listY + i * 32.0f;

            boolean hovered = isHovered(context.getMouseX(), context.getMouseY(), x, itemY, w, 28.0f);

            if (hovered) {
                CursorUtility.set(CursorType.HAND);
            }

            // Draw item card
            if (hovered) {
                context.drawSquircle(x, itemY, w, 28.0f, 7.0f, BorderRadius.all(6.0f), Colors.getOutlineColor().withAlpha(60.0f));
            } else {
                context.drawSquircle(x, itemY, w, 28.0f, 7.0f, BorderRadius.all(6.0f), Colors.getAdditionalColor().withAlpha(120.0f));
            }
            context.drawRoundedBorder(x, itemY, w, 28.0f, 1.0f, BorderRadius.all(6.0f), Colors.getOutlineColor());

            // Draw player head
            context.drawHead(player, x + 5.0f, itemY + 4.0f, 20.0f, BorderRadius.all(3.0f), ColorRGBA.WHITE);

            // Draw player name
            context.drawText(Fonts.MEDIUM.getFont(9.5f), player.getName().getString(), x + 32.0f, itemY + 9.0f, Colors.getTextColor());

            // Draw player distance
            String dist = String.format("%.1f m", mc.player.distanceTo(player));
            context.drawRightText(Fonts.REGULAR.getFont(8.0f), dist, x + w - 10.0f, itemY + 10.0f, Colors.getTextColor().mulAlpha(0.6f));
        }
    }

    private void drawJustPushTab(UIContext context, float x, float y, float w, float h) {
        if (showingReasons && activePushTarget != null) {
            drawReasonsMenu(context, x, y, w, h, activePushTarget);
            return;
        }

        context.drawText(Fonts.BOLD.getFont(14.0f), "Just Push", x, y + 5.0f, Colors.getTextColor());
        context.drawText(Fonts.REGULAR.getFont(9.0f), "Выберите игрока для авто-толкания (/push)", x, y + 25.0f, Colors.getTextColor().mulAlpha(0.5f));

        if (playersCache.isEmpty()) {
            context.drawCenteredText(Fonts.REGULAR.getFont(10.0f), "Игроки в зоне рендера не найдены", x + w/2.0f, y + h/2.0f + 10.0f, Colors.getTextColor().mulAlpha(0.4f));
            return;
        }

        // Draw player entries
        float listY = y + 45.0f;
        int maxFit = 6;
        for (int i = 0; i < Math.min(playersCache.size(), maxFit); i++) {
            Player player = playersCache.get(i);
            float itemY = listY + i * 32.0f;

            boolean hovered = isHovered(context.getMouseX(), context.getMouseY(), x, itemY, w, 28.0f);

            if (hovered) {
                CursorUtility.set(CursorType.HAND);
            }

            // Draw item card
            if (hovered) {
                context.drawSquircle(x, itemY, w, 28.0f, 7.0f, BorderRadius.all(6.0f), Colors.getOutlineColor().withAlpha(60.0f));
            } else {
                context.drawSquircle(x, itemY, w, 28.0f, 7.0f, BorderRadius.all(6.0f), Colors.getAdditionalColor().withAlpha(120.0f));
            }
            context.drawRoundedBorder(x, itemY, w, 28.0f, 1.0f, BorderRadius.all(6.0f), Colors.getOutlineColor());

            // Draw player head
            context.drawHead(player, x + 5.0f, itemY + 4.0f, 20.0f, BorderRadius.all(3.0f), ColorRGBA.WHITE);

            // Draw player name
            context.drawText(Fonts.MEDIUM.getFont(9.5f), player.getName().getString(), x + 32.0f, itemY + 9.0f, Colors.getTextColor());

            // Draw player distance
            String dist = String.format("%.1f m", mc.player.distanceTo(player));
            context.drawRightText(Fonts.REGULAR.getFont(8.0f), dist, x + w - 10.0f, itemY + 10.0f, Colors.getTextColor().mulAlpha(0.6f));
        }
    }

    private void drawAutoFormaTab(UIContext context, float x, float y, float w, float h) {
        context.drawText(Fonts.BOLD.getFont(14.0f), "AutoForma Настройки", x, y + 5.0f, Colors.getTextColor());
        context.drawText(Fonts.REGULAR.getFont(9.0f), "Автоматическое заполнение форм отчетов", x, y + 25.0f, Colors.getTextColor().mulAlpha(0.5f));

        float groupY = y + 45.0f;
        context.drawRoundedRect(x, groupY, w, 180.0f, BorderRadius.all(6.0f), Colors.getAdditionalColor());
        context.drawRoundedBorder(x, groupY, w, 180.0f, 1.0f, BorderRadius.all(6.0f), Colors.getOutlineColor());

        context.drawCenteredText(Fonts.REGULAR.getFont(10.0f), "Forma settings will appear here", x + w/2.0f, groupY + 80.0f, Colors.getTextColor().mulAlpha(0.4f));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (button != MouseButton.LEFT) return;

        float width = IScaledResolution.sr.getGuiScaledWidth();
        float height = IScaledResolution.sr.getGuiScaledHeight();

        float panelW = isTargetMenu ? 240.0f : 400.0f;
        float panelH = isTargetMenu ? (showingReasons ? 180.0f : 120.0f) : 280.0f;
        float x = (width - panelW) / 2.0f;
        float y = (height - panelH) / 2.0f;

        if (isTargetMenu) {
            float contentX = x + 15.0f;
            float contentY = y + 15.0f;
            float contentW = panelW - 30.0f;
            float contentH = panelH - 30.0f;

            if (showingReasons) {
                handleReasonsClick(mouseX, mouseY, contentX, contentY, contentW, contentH, targetPlayerOverride);
            } else {
                float sacY = contentY + 25.0f;
                if (isHovered(mouseX, mouseY, contentX, sacY, contentW, 24.0f)) {
                    module.setLastPunishTarget(targetPlayerOverride);
                    if (mc.player.connection != null) {
                        mc.player.connection.sendCommand("sac punish " + targetPlayerOverride);
                    }
                    mc.setScreen(null);
                    return;
                }

                float pushY = contentY + 55.0f;
                if (isHovered(mouseX, mouseY, contentX, pushY, contentW, 24.0f)) {
                    showingReasons = true;
                    return;
                }
            }
            return;
        }

        float sidebarW = 100.0f;

        // Check sidebar click
        for (int i = 0; i < TABS.length; i++) {
            float tabY = y + 20.0f + i * 40.0f;
            if (isHovered(mouseX, mouseY, x + 10, tabY, sidebarW - 20, 30)) {
                activeTab = i;
                showingReasons = false;
                activePushTarget = null;
                return;
            }
        }

        float contentX = x + sidebarW + 15.0f;
        float contentY = y + 15.0f;
        float listY = contentY + 45.0f;
        float contentW = panelW - sidebarW - 30.0f;
        float contentH = panelH - 30.0f;

        // Check SAC Push player clicks
        if (activeTab == 1 && mc.level != null && mc.player != null) {
            int maxFit = 6;
            for (int i = 0; i < Math.min(playersCache.size(), maxFit); i++) {
                Player player = playersCache.get(i);
                float itemY = listY + i * 32.0f;
                if (isHovered(mouseX, mouseY, contentX, itemY, contentW, 28.0f)) {
                    String name = player.getName().getString();
                    module.setLastPunishTarget(name);
                    if (mc.player.connection != null) {
                        mc.player.connection.sendCommand("sac punish " + name);
                    }
                    mc.setScreen(null);
                    return;
                }
            }
        }

        // Check Just push clicks
        if (activeTab == 2) {
            if (showingReasons && activePushTarget != null) {
                handleReasonsClick(mouseX, mouseY, contentX, contentY, contentW, contentH, activePushTarget);
                return;
            }

            int maxFit = 6;
            for (int i = 0; i < Math.min(playersCache.size(), maxFit); i++) {
                Player player = playersCache.get(i);
                float itemY = listY + i * 32.0f;
                if (isHovered(mouseX, mouseY, contentX, itemY, contentW, 28.0f)) {
                    activePushTarget = player.getName().getString();
                    showingReasons = true;
                    return;
                }
            }
        }
    }

    private boolean isHovered(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // block background dim
    }
}
