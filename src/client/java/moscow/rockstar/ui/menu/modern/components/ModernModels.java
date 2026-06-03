package moscow.rockstar.ui.menu.modern.components;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.theme.Theme;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.gui.ScrollHandler;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.ScissorUtility;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import moscow.rockstar.utility.render.ShaderColorHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ModernModels extends CustomComponent implements IMinecraft {
    private final List<File> models = new ArrayList<>();
    private final ScrollHandler scrollHandler = new ScrollHandler();
    private static String selectedModelPath = "";
    private static String lastLoadedWorld = "";
    private float yaw = 180.0f;
    private float pitch = 0.0f;
    private boolean draggingModel = false;
    private double lastMouseX, lastMouseY;

    public ModernModels() {
        refreshModels();
    }

    public void refreshModels() {
        models.clear();
        File modelsDir = new File(mc.gameDirectory, "figura/avatars");
        if (modelsDir.exists() && modelsDir.isDirectory()) {
            File[] files = modelsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Figura avatars can be folders or zip files (.avatar)
                    if (file.isDirectory() || file.getName().toLowerCase().endsWith(".avatar")) {
                        models.add(file);
                    }
                }
            }
        }
    }

    @Override
    protected void renderComponent(UIContext context) {
        if (models.isEmpty() && mc.player != null && (mc.player.tickCount + 1) % 100 == 0) {
            refreshModels();
        }
        ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, ShaderColorHelper.getAlpha());
        
        boolean dark = Rockstar.getInstance().getThemeManager().getCurrentTheme() == Theme.DARK;
        
        // Split line
        float splitX = x + width * 0.45f;
        context.drawRect(splitX, y + 5, 1.0f, height - 10, Colors.getSeparatorColor().mulAlpha(0.8f));

        // List of models
        scrollHandler.update();
        float modelYStart = y + 40; // Reduced offset from title
        float modelY = modelYStart - (float)scrollHandler.getRGB();
        
        // Internal Scissor for model list
        ScissorUtility.push(context.pose(), x + 5, modelYStart - 5, splitX - x - 10, height - 50);
        
        // 1. Render all medium texts (Names)
        FontBatching mediumBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
        float tempY = modelY;
        for (File model : models) {
            if (tempY + 32 >= modelYStart - 10 && tempY <= y + height - 10) {
                boolean selected = selectedModelPath.equals(model.getAbsolutePath());
                context.drawText(Fonts.MEDIUM.getFont(7.5f), model.getName().replace(".avatar", ""), x + 18, tempY + 9.5f, selected ? Colors.getAccentColor() : Colors.getTextColor());
            }
            tempY += 36;
        }
        mediumBatching.draw();
        
        // 2. Render all regular texts (Sub-text)
        FontBatching regularBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.REGULAR);
        tempY = modelY;
        for (File model : models) {
            if (tempY + 32 >= modelYStart - 10 && tempY <= y + height - 10) {
                context.drawText(Fonts.REGULAR.getFont(6.0f), "Figura Avatar", x + 18, tempY + 18.0f, Colors.getTextColor().mulAlpha(0.4f));
            }
            tempY += 36;
        }
        regularBatching.draw();

        // 3. Render backgrounds (Already immediate)
        tempY = modelY;
        for (File model : models) {
            if (tempY + 32 < modelYStart - 10 || tempY > y + height - 10) {
                tempY += 36;
                continue;
            }
            boolean hovered = GuiUtility.isHovered(x + 10, tempY, splitX - x - 20, 32, context);
            boolean selected = selectedModelPath.equals(model.getAbsolutePath());
            
            // Base background for all items to separate from main menu
//             context.drawRoundedRect(x + 10, tempY, splitX - x - 20, 32, BorderRadius.all(6.5f), Colors.getTextColor().mulAlpha(0.08f));
            
            // Hover/Select background
            if (selected || hovered) {
//                 context.drawRoundedRect(x + 10, tempY, splitX - x - 20, 32, BorderRadius.all(6.5f), (selected ? Colors.getAccentColor() : Colors.getTextColor()).mulAlpha(selected ? 0.2f : 0.12f));
                if (hovered) {
                    CursorUtility.set(CursorType.HAND);
                }
            }
            tempY += 36;
        }
        
        ScissorUtility.pop();
        
        // 3D Preview Area
        float previewX = splitX + 25;
        float previewY = y + 10;
        float previewWidth = width - (previewX - x) - 25;
        float previewHeight = height - 20;
        float previewSize = Math.min(previewWidth, previewHeight);
        
        // Center it in the right area
        float drawX = previewX + (previewWidth - previewSize) / 2;
        float drawY = previewY + (previewHeight - previewSize) / 2;

        // Background square for 3D projection
        context.drawSquircle(drawX, drawY, previewSize, previewSize, 10, BorderRadius.all(16), (dark ? Colors.getAdditionalColor() : Colors.getBackgroundColor()).mulAlpha(0.4f));
        
        // TODO: 26.1 - entity preview in menu needs GuiGraphics.renderEntityInInventory migration
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        float splitX = x + width * 0.45f;
        float previewX = splitX + 25;
        float previewY = y + 25;
        float previewWidth = width - (previewX - x) - 25;
        float previewHeight = height - 50;
        float previewSize = Math.min(previewWidth, previewHeight);
        float drawX = previewX + (previewWidth - previewSize) / 2;
        float drawY = previewY + (previewHeight - previewSize) / 2;

        if (GuiUtility.isHovered(drawX, drawY, previewSize, previewSize, mouseX, mouseY)) {
            draggingModel = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else if (GuiUtility.isHovered(x, y, splitX - x, height, mouseX, mouseY)) {
            float modelYStart = y + 40;
            float modelY = modelYStart - (float)scrollHandler.getRGB();
            for (File model : models) {
                if (GuiUtility.isHovered(x + 10, modelY, splitX - x - 20, 32, mouseX, mouseY)) {
                    selectedModelPath = model.getAbsolutePath();
                    applyModel(model);
                    Rockstar.getInstance().getFileManager().getClientFile("client").write();
                    break;
                }
                modelY += 36;
            }
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    public void onMouseDragged(double mouseX, double mouseY) {
        if (draggingModel) {
            yaw += (float)(mouseX - lastMouseX) * 1.5f;
            pitch += (float)(mouseY - lastMouseY) * 1.5f;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        draggingModel = false;
    }

    public static void onTick() {
        if (mc.player != null && mc.level != null) {
            String currentWorld = mc.level.dimension().identifier().toString() + (mc.getConnection() != null && mc.getConnection().getServerData() != null ? mc.getConnection().getServerData().ip : "local");
            if (!currentWorld.equals(lastLoadedWorld)) {
                reloadAvatar();
                lastLoadedWorld = currentWorld;
            }
        }
    }

    public static void reloadAvatar() {
        if (!selectedModelPath.isEmpty()) {
            File file = new File(selectedModelPath);
            if (file.exists()) {
                applyModel(file);
            }
        }
    }

    private static void applyModel(File model) {
        String name = model.getName().replace(".avatar", "");
        if (mc.getConnection() != null) {
            mc.getConnection().sendCommand("figura load " + name);
        }
    }

    public boolean onScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float splitX = x + width * 0.45f;
        if (GuiUtility.isHovered(x, y, splitX - x, height, mouseX, mouseY)) {
            scrollHandler.scroll(verticalAmount);
            scrollHandler.setMax(-Math.max(0, models.size() * 36 - (height - 50)));
            return true;
        }
        return false;
    }
    
    public static String getSelectedModelPath() {
        return selectedModelPath;
    }

    public static void setSelectedModelPath(String path) {
        selectedModelPath = path;
    }

    public boolean isDraggingModel() {
        return draggingModel;
    }
}
