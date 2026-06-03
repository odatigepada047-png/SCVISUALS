package moscow.rockstar.systems.modules.constructions.crosshair;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public class CrosshairEditorScreen extends CustomScreen implements IScaledResolution, IMinecraft {
    private final CrosshairManager manager;
    private boolean isDragging = false;
    private boolean isPainting = true;
    
    private static final float CELL_SIZE = 12.0f;
    private final float GRID_SIZE;
    private final float GRID_TOTAL_SIZE;

    private final moscow.rockstar.framework.msdf.Font titleFont = new moscow.rockstar.framework.msdf.Font(Fonts.BOLD, 14.0f);
    private final moscow.rockstar.framework.msdf.Font textFont = new moscow.rockstar.framework.msdf.Font(Fonts.REGULAR, 11.0f);
    private final moscow.rockstar.framework.msdf.Font smallFont = new moscow.rockstar.framework.msdf.Font(Fonts.REGULAR, 9.0f);
    
    public CrosshairEditorScreen() {
        this.manager = Rockstar.getInstance().getCrosshairManager();
        this.GRID_SIZE = manager.getGridSize();
        this.GRID_TOTAL_SIZE = this.GRID_SIZE * CELL_SIZE;
    }
    
    @Override
    public void render(UIContext context) {
        float screenWidth = IScaledResolution.sr.getGuiScaledWidth();
        float screenHeight = IScaledResolution.sr.getGuiScaledHeight();

        float cardWidth = 440.0f;
        float cardHeight = 290.0f;

        float cardX = (screenWidth - cardWidth) / 2.0f;
        float cardY = (screenHeight - cardHeight) / 2.0f;

        // Shadow and premium background matching client theme styling
        context.drawShadow(cardX, cardY, cardWidth, cardHeight, 15.0f, BorderRadius.all(6.0f), ColorRGBA.BLACK.withAlpha(127.5f));
        if (Interface.showMinimalizm()) {
            context.drawBlurredRect(cardX, cardY, cardWidth, cardHeight, 45.0f, 7.0f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * Interface.minimalizm()));
        }
        if (Interface.showGlass()) {
            context.drawLiquidGlass(cardX, cardY, cardWidth, cardHeight, 7.0f, 0.08f, BorderRadius.all(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * Interface.glass()));
        }
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        context.drawSquircle(cardX, cardY, cardWidth, cardHeight, 7.0f, BorderRadius.all(6.0f), Colors.getBackgroundColor().withAlpha(255.0f * (dark ? 0.8f - 0.6f * Interface.glass() : 0.7f)));
        context.drawRoundedBorder(cardX, cardY, cardWidth, cardHeight, 1.5f, BorderRadius.all(6.0f), Colors.getOutlineColor());

        // Header info
        context.drawText(titleFont, "Редактор Прицела", cardX + 20.0f, cardY + 12.0f, Colors.getTextColor());
        context.drawText(smallFont, "Сетка 19x19", cardX + 20.0f, cardY + 25.0f, Colors.getTextColor().withAlpha(140.0f));

        // Separator line
        context.drawRect(cardX + 20.0f, cardY + 36.0f, cardWidth - 40.0f, 1.0f, Colors.getSeparatorColor());

        float gridX = cardX + 20.0f;
        float gridY = cardY + 42.0f;

        renderGrid(context, gridX, gridY);

        float rightX = cardX + 20.0f + GRID_TOTAL_SIZE + 20.0f;
        float rightWidth = cardWidth - (20.0f + GRID_TOTAL_SIZE + 20.0f) - 20.0f;

        float previewY = cardY + 42.0f;
        renderPreview(context, rightX, previewY, rightWidth);

        float actionsY = cardY + 154.0f;
        renderActionButtons(context, rightX, actionsY, rightWidth);
    }
    
    private void renderGrid(UIContext context, float x, float y) {
        context.drawRoundedRect(x, y, GRID_TOTAL_SIZE, GRID_TOTAL_SIZE, BorderRadius.all(6.0f), Colors.getAdditionalColor().withAlpha(120.0f));
        context.drawRoundedBorder(x, y, GRID_TOTAL_SIZE, GRID_TOTAL_SIZE, 1.0f, BorderRadius.all(6.0f), Colors.getOutlineColor());

        int mouseX = context.getMouseX();
        int mouseY = context.getMouseY();

        int size = (int) GRID_SIZE;
        int centerIndex = size / 2;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                float cellX = x + i * CELL_SIZE;
                float cellY = y + j * CELL_SIZE;

                boolean isPixel = manager.getPixel(i, j);
                boolean isCenter = (i == centerIndex && j == centerIndex);

                ColorRGBA cellColor;
                if (isPixel) {
                    cellColor = manager.getColor();
                } else if (isCenter) {
                    cellColor = Colors.getAccentColor().withAlpha(60.0f);
                } else {
                    cellColor = ColorRGBA.BLACK.withAlpha(30.0f);
                }

                boolean isHovered = mouseX >= cellX && mouseX < cellX + CELL_SIZE &&
                                    mouseY >= cellY && mouseY < cellY + CELL_SIZE;

                if (isHovered && !isPixel) {
                    cellColor = Colors.getAccentColor().withAlpha(120.0f);
                }

                context.drawRect(cellX + 0.5f, cellY + 0.5f, CELL_SIZE - 1.0f, CELL_SIZE - 1.0f, cellColor);
                context.drawRoundedBorder(cellX, cellY, CELL_SIZE, CELL_SIZE, 0.5f, BorderRadius.ZERO, ColorRGBA.WHITE.withAlpha(15.0f));
            }
        }
    }
    
    private void renderPreview(UIContext context, float x, float y, float width) {
        float previewHeight = 100.0f;

        context.drawRoundedRect(x, y, width, previewHeight, BorderRadius.all(6.0f), Colors.getAdditionalColor().withAlpha(100.0f));
        context.drawRoundedBorder(x, y, width, previewHeight, 1.0f, BorderRadius.all(6.0f), Colors.getOutlineColor());

        context.drawText(textFont, "Предпросмотр", x + 8.0f, y + 8.0f, Colors.getTextColor());

        float innerX = x + 8.0f;
        float innerY = y + 24.0f;
        float innerW = width - 16.0f;
        float innerH = previewHeight - 32.0f;

        context.drawRect(innerX, innerY, innerW, innerH, ColorRGBA.BLACK.withAlpha(200.0f));

        float checkSize = 4.0f;
        for (float cx = innerX; cx < innerX + innerW; cx += checkSize) {
            for (float cy = innerY; cy < innerY + innerH; cy += checkSize) {
                float cw = Math.min(checkSize, innerX + innerW - cx);
                float ch = Math.min(checkSize, innerY + innerH - cy);
                if (((int)((cx - innerX) / checkSize) + (int)((cy - innerY) / checkSize)) % 2 == 0) {
                    context.drawRect(cx, cy, cw, ch, ColorRGBA.WHITE.withAlpha(10.0f));
                }
            }
        }

        float centerX = innerX + innerW / 2.0f;
        float centerY = innerY + innerH / 2.0f;

        int size = (int) GRID_SIZE;
        int centerIndex = size / 2;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (manager.getPixel(i, j)) {
                    float px = centerX + (i - centerIndex) * 1.0f;
                    float py = centerY + (j - centerIndex) * 1.0f;
                    context.drawRect(px, py, 1.0f, 1.0f, manager.getColor());
                }
            }
        }
    }
    
    private void renderActionButtons(UIContext context, float x, float y, float width) {
        float btnHeight = 28.0f;
        int mouseX = context.getMouseX();
        int mouseY = context.getMouseY();

        // 1. Очистить (Clear)
        float clearY = y;
        boolean clearHover = mouseX >= x && mouseX <= x + width && mouseY >= clearY && mouseY <= clearY + btnHeight;
        ColorRGBA clearColor = clearHover ? Colors.getAccentColor().withAlpha(180.0f) : Colors.getAdditionalColor().withAlpha(140.0f);
        ColorRGBA clearBorder = clearHover ? Colors.getAccentColor() : Colors.getOutlineColor();
        context.drawSquircle(x, clearY, width, btnHeight, 7.0f, BorderRadius.all(6.0f), clearColor);
        context.drawRoundedBorder(x, clearY, width, btnHeight, 1.0f, BorderRadius.all(6.0f), clearBorder);
        context.drawCenteredText(textFont, "Очистить", x + width / 2.0f, clearY + 10.0f, Colors.getTextColor());

        // 2. По умолчанию (Default)
        float defaultY = clearY + btnHeight + 12.0f;
        boolean defaultHover = mouseX >= x && mouseX <= x + width && mouseY >= defaultY && mouseY <= defaultY + btnHeight;
        ColorRGBA defaultColor = defaultHover ? Colors.getAccentColor().withAlpha(180.0f) : Colors.getAdditionalColor().withAlpha(140.0f);
        ColorRGBA defaultBorder = defaultHover ? Colors.getAccentColor() : Colors.getOutlineColor();
        context.drawSquircle(x, defaultY, width, btnHeight, 7.0f, BorderRadius.all(6.0f), defaultColor);
        context.drawRoundedBorder(x, defaultY, width, btnHeight, 1.0f, BorderRadius.all(6.0f), defaultBorder);
        context.drawCenteredText(textFont, "По умолчанию", x + width / 2.0f, defaultY + 10.0f, Colors.getTextColor());

        // 3. Закрыть (Close)
        float closeY = defaultY + btnHeight + 12.0f;
        boolean closeHover = mouseX >= x && mouseX <= x + width && mouseY >= closeY && mouseY <= closeY + btnHeight;
        ColorRGBA closeColor = closeHover ? Colors.getAccentColor().withAlpha(180.0f) : Colors.getAdditionalColor().withAlpha(140.0f);
        ColorRGBA closeBorder = closeHover ? Colors.getAccentColor() : Colors.getOutlineColor();
        context.drawSquircle(x, closeY, width, btnHeight, 7.0f, BorderRadius.all(6.0f), closeColor);
        context.drawRoundedBorder(x, closeY, width, btnHeight, 1.0f, BorderRadius.all(6.0f), closeBorder);
        context.drawCenteredText(textFont, "Закрыть", x + width / 2.0f, closeY + 10.0f, Colors.getTextColor());
    }
    
    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        float screenWidth = IScaledResolution.sr.getGuiScaledWidth();
        float screenHeight = IScaledResolution.sr.getGuiScaledHeight();

        float cardWidth = 440.0f;
        float cardHeight = 290.0f;
        float cardX = (screenWidth - cardWidth) / 2.0f;
        float cardY = (screenHeight - cardHeight) / 2.0f;

        float gridX = cardX + 20.0f;
        float gridY = cardY + 42.0f;

        if (mouseX >= gridX && mouseX < gridX + GRID_TOTAL_SIZE &&
            mouseY >= gridY && mouseY < gridY + GRID_TOTAL_SIZE) {
            int col = (int) ((mouseX - gridX) / CELL_SIZE);
            int row = (int) ((mouseY - gridY) / CELL_SIZE);
            if (col >= 0 && col < GRID_SIZE && row >= 0 && row < GRID_SIZE) {
                isDragging = true;
                isPainting = (button == MouseButton.LEFT);
                manager.setPixel(col, row, isPainting);
            }
            return;
        }

        float rightX = cardX + 268.0f;
        float rightWidth = cardWidth - 268.0f - 20.0f;

        float previewY = cardY + 42.0f;
        float actionsY = cardY + 154.0f;
        float btnHeight = 28.0f;

        // Clear
        float clearY = actionsY;
        if (mouseX >= rightX && mouseX <= rightX + rightWidth && mouseY >= clearY && mouseY <= clearY + btnHeight) {
            manager.clear();
            manager.save();
            return;
        }

        // Default
        float defaultY = clearY + btnHeight + 12.0f;
        if (mouseX >= rightX && mouseX <= rightX + rightWidth && mouseY >= defaultY && mouseY <= defaultY + btnHeight) {
            manager.clear();
            manager.applyDefaultCrosshairPattern();
            manager.save();
            return;
        }

        // Close
        float closeY = defaultY + btnHeight + 12.0f;
        if (mouseX >= rightX && mouseX <= rightX + rightWidth && mouseY >= closeY && mouseY <= closeY + btnHeight) {
            this.onClose();
            return;
        }
    }
    
    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        if (isDragging) {
            isDragging = false;
            manager.save();
        }
    }
    
    @Override
    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
        if (isDragging) {
            float screenWidth = IScaledResolution.sr.getGuiScaledWidth();
            float screenHeight = IScaledResolution.sr.getGuiScaledHeight();

            float cardWidth = 440.0f;
            float cardHeight = 290.0f;
            float cardX = (screenWidth - cardWidth) / 2.0f;
            float cardY = (screenHeight - cardHeight) / 2.0f;

            float gridX = cardX + 20.0f;
            float gridY = cardY + 42.0f;

            if (mouseX >= gridX && mouseX < gridX + GRID_TOTAL_SIZE &&
                mouseY >= gridY && mouseY < gridY + GRID_TOTAL_SIZE) {
                int col = (int) ((mouseX - gridX) / CELL_SIZE);
                int row = (int) ((mouseY - gridY) / CELL_SIZE);
                if (col >= 0 && col < GRID_SIZE && row >= 0 && row < GRID_SIZE) {
                    manager.setPixel(col, row, isPainting);
                }
            }
        }
    }
    
    @Override
    public void tick() {
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }
    
    @Override
    public void onClose() {
        manager.save();
        super.onClose();
        mc.setScreen((Screen) Rockstar.getInstance().getMenuScreen());
    }
}
