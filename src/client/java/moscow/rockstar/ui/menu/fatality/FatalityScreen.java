package moscow.rockstar.ui.menu.fatality;

import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.utility.render.batching.impl.FontBatching;
import moscow.rockstar.utility.render.RenderUtility;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.framework.objects.MouseButton;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FatalityScreen extends MenuScreen implements IMinecraft, IScaledResolution {

    private float x, y, width, height;
    private String currentCategory = "Combat";
    private String currentSubCategory = "General";
    
    private boolean drag = false;
    private float dragX, dragY;
    
    private final List<String> categories = Arrays.asList(
        "Combat", "Movement", "Visuals", "Misc", "Models", "Api"
    );

    public FatalityScreen() {
        this.width = 700.0f;
        this.height = 500.0f;
    }

    @Override
    protected void init() {
        this.closing = false;
        // Center the menu initially
        this.x = sr.getGuiScaledWidth() / 2.0f - this.width / 2.0f;
        this.y = sr.getGuiScaledHeight() / 2.0f - this.height / 2.0f;
        super.init();
    }

    @Override
    public void render(UIContext context) {
        this.menuAnimation.update(this.closing ? 0.0f : 1.0f);
        
        float alpha = this.menuAnimation.getRGB();
        
        // Handle dragging
        if (this.drag) {
            this.x = (float)context.getMouseX() - this.dragX;
            this.y = (float)context.getMouseY() - this.dragY;
        }
        
        RenderUtility.scale(context.pose(), this.x + this.width / 2.0f, this.y + this.height / 2.0f, 0.5f + 0.5f * alpha);
        
        // Main Window Background (Fatality dark theme)
        ColorRGBA bgColor = new ColorRGBA(20, 20, 20, (int)(255 * alpha));
//         context.drawRoundedRect(x, y, width, height, BorderRadius.all(4.0f), bgColor);
        
        // Top Bar Background
        ColorRGBA topBarColor = new ColorRGBA(15, 15, 15, (int)(255 * alpha));
//         context.drawRoundedRect(x, y, width, 40.0f, BorderRadius.top(4.0f, 4.0f), topBarColor);
        
        // Top Bar Orange Line
        ColorRGBA orangeAccent = new ColorRGBA(224, 83, 0, (int)(255 * alpha));
//         context.drawRoundedRect(x, y + 38.0f, width, 2.0f, BorderRadius.ZERO, orangeAccent);
        
        // Logo "FATALITY"
        FontBatching semiboldBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.SEMIBOLD);
        context.drawText(Fonts.SEMIBOLD.getFont(14.0f), "FATALITY", x + 15.0f, y + 12.0f, orangeAccent);
        semiboldBatching.draw();
        
        // Top Bar Categories
        FontBatching mediumBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
        float xOff = x + 120.0f;
        for (String cat : categories) {
            boolean active = cat.equals(currentCategory);
            ColorRGBA color = active ? orangeAccent : new ColorRGBA(150, 150, 150, (int)(255 * alpha));
            
            context.drawText(Fonts.MEDIUM.getFont(9.0f), cat.toUpperCase(), xOff, y + 15.0f, color);
            xOff += 70.0f;
        }
        mediumBatching.draw();
        
        // Left Sidebar (Sub-categories)
        ColorRGBA sidebarColor = new ColorRGBA(15, 15, 15, (int)(255 * alpha));
//         context.drawRoundedRect(x, y + 40.0f, 100.0f, height - 70.0f, BorderRadius.ZERO, sidebarColor);
        
        FontBatching regularBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.REGULAR);
        context.drawText(Fonts.REGULAR.getFont(8.0f), currentSubCategory, x + 15.0f, y + 60.0f, new ColorRGBA(255, 255, 255, (int)(255 * alpha)));
        regularBatching.draw();
        
        // Content Area Background
        float contentX = x + 110.0f;
        float contentY = y + 50.0f;
        float boxWidth = 180.0f;
        float spacing = 10.0f;
        
        // Fetch Real Modules based on selected category
        List<Module> modules = Rockstar.getInstance().getModuleManager().getModules();
        List<Module> filteredModules = modules.stream().filter(m -> {
            if (currentCategory.equals("Combat")) return m.getCategory() == ModuleCategory.COMBAT;
            if (currentCategory.equals("Movement")) return m.getCategory() == ModuleCategory.MOVEMENT;
            if (currentCategory.equals("Visuals")) return m.getCategory() == ModuleCategory.VISUALS;
            if (currentCategory.equals("Misc")) return m.getCategory() == ModuleCategory.OTHER || m.getCategory() == ModuleCategory.PLAYER;
            if (currentCategory.equals("Models")) return m.getCategory() == ModuleCategory.MODELS;
            if (currentCategory.equals("Api")) return m.getCategory() == ModuleCategory.EVENTS;
            return false;
        }).collect(Collectors.toList());
        
        // Arrange modules in 3 columns
        int column = 0;
        float[] columnY = new float[]{contentY, contentY, contentY};
        
        for (Module m : filteredModules) {
            float bX = contentX + column * (boxWidth + spacing);
            float bY = columnY[column];
            
            // Calculate box height based on number of settings
            float boxHeight = 40.0f + m.getSettings().size() * 12.0f;
            if (boxHeight < 60.0f) boxHeight = 60.0f; // Min height
            
            drawModuleBox(context, m, bX, bY, boxWidth, boxHeight, alpha);
            
            columnY[column] += boxHeight + spacing;
            column = (column + 1) % 3;
        }
        
        // Bottom Bar
        ColorRGBA bottomBarColor = new ColorRGBA(15, 15, 15, (int)(255 * alpha));
//         context.drawRoundedRect(x, y + height - 30.0f, width, 30.0f, BorderRadius.bottom(4.0f, 4.0f), bottomBarColor);
        
        // Bottom Bar Icons (Mocking search, save, folder)
        FontBatching iconBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.REGULAR);
        context.drawText(Fonts.REGULAR.getFont(10.0f), "🔍", x + 15.0f, y + height - 20.0f, new ColorRGBA(150, 150, 150, (int)(255 * alpha)));
        context.drawText(Fonts.REGULAR.getFont(10.0f), "💾", x + 40.0f, y + height - 20.0f, new ColorRGBA(150, 150, 150, (int)(255 * alpha)));
        context.drawText(Fonts.REGULAR.getFont(10.0f), "📁", x + 65.0f, y + height - 20.0f, new ColorRGBA(150, 150, 150, (int)(255 * alpha)));
        iconBatching.draw();
        
        RenderUtility.end(context.pose());
    }
    
    private void drawModuleBox(UIContext context, Module module, float boxX, float boxY, float boxWidth, float boxHeight, float alpha) {
        ColorRGBA boxBg = new ColorRGBA(15, 15, 15, (int)(255 * alpha));
//         context.drawRoundedRect(boxX, boxY, boxWidth, boxHeight, BorderRadius.all(2.0f), boxBg);
        
        FontBatching semiboldBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.SEMIBOLD);
        context.drawText(Fonts.SEMIBOLD.getFont(8.0f), module.getName(), boxX + 10.0f, boxY + 10.0f, new ColorRGBA(255, 255, 255, (int)(255 * alpha)));
        semiboldBatching.draw();
        
        FontBatching regularBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.REGULAR);
        
        // Toggle Checkbox for Module Enable State
        drawCheckbox(context, "Enabled", boxX + 10.0f, boxY + 25.0f, module.isEnabled(), alpha);
        
        // Draw Settings
        float sY = boxY + 40.0f;
        for (Setting setting : module.getSettings()) {
            context.drawText(Fonts.REGULAR.getFont(7.0f), setting.getName(), boxX + 10.0f, sY, new ColorRGBA(180, 180, 180, (int)(255 * alpha)));
            sY += 12.0f;
        }
        
        regularBatching.draw();
    }
    
    private void drawCheckbox(UIContext context, String label, float cbX, float cbY, boolean checked, float alpha) {
        ColorRGBA orangeAccent = new ColorRGBA(224, 83, 0, (int)(255 * alpha));
        ColorRGBA textColor = new ColorRGBA(200, 200, 200, (int)(255 * alpha));
        
        // AABB
//         context.drawRoundedRect(cbX, cbY, 10.0f, 10.0f, BorderRadius.all(1.0f), new ColorRGBA(30, 30, 30, (int)(255 * alpha)));
        
        if (checked) {
//             context.drawRoundedRect(cbX + 2.0f, cbY + 2.0f, 6.0f, 6.0f, BorderRadius.ZERO, orangeAccent);
        }
        
        context.drawText(Fonts.REGULAR.getFont(7.0f), label, cbX + 15.0f, cbY + 2.0f, textColor);
    }
    
    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        // Handle dragging
        if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height) {
            // Check if clicked on top bar for dragging
            if (mouseY <= this.y + 40.0f) {
                this.drag = true;
                this.dragX = (float)(mouseX - this.x);
                this.dragY = (float)(mouseY - this.y);
                return;
            }
            
            // Check category clicks
            float xOff = this.x + 120.0f;
            for (String cat : categories) {
                if (mouseX >= xOff && mouseX <= xOff + 50.0f && mouseY >= this.y + 10.0f && mouseY <= this.y + 30.0f) {
                    this.currentCategory = cat;
                    return;
                }
                xOff += 70.0f;
            }
            
            // Check module toggle clicks
            float contentX = x + 110.0f;
            float contentY = y + 50.0f;
            float boxWidth = 180.0f;
            float spacing = 10.0f;
            
            List<Module> modules = Rockstar.getInstance().getModuleManager().getModules();
            List<Module> filteredModules = modules.stream().filter(m -> {
                if (currentCategory.equals("Combat")) return m.getCategory() == ModuleCategory.COMBAT;
                if (currentCategory.equals("Movement")) return m.getCategory() == ModuleCategory.MOVEMENT;
                if (currentCategory.equals("Visuals")) return m.getCategory() == ModuleCategory.VISUALS;
                if (currentCategory.equals("Misc")) return m.getCategory() == ModuleCategory.OTHER || m.getCategory() == ModuleCategory.PLAYER;
                if (currentCategory.equals("Models")) return m.getCategory() == ModuleCategory.MODELS;
                if (currentCategory.equals("Api")) return m.getCategory() == ModuleCategory.EVENTS;
                return false;
            }).collect(Collectors.toList());
            
            int column = 0;
            float[] columnY = new float[]{contentY, contentY, contentY};
            
            for (Module m : filteredModules) {
                float bX = contentX + column * (boxWidth + spacing);
                float bY = columnY[column];
                
                float boxHeight = 40.0f + m.getSettings().size() * 12.0f;
                if (boxHeight < 60.0f) boxHeight = 60.0f;
                
                // Check if clicked on checkbox (relative to box)
                if (mouseX >= bX + 10.0f && mouseX <= bX + 20.0f && mouseY >= bY + 25.0f && mouseY <= bY + 35.0f) {
                    m.toggle();
                    return;
                }
                
                columnY[column] += boxHeight + spacing;
                column = (column + 1) % 3;
            }
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.drag = false;
        super.onMouseReleased(mouseX, mouseY, button);
    }
}
