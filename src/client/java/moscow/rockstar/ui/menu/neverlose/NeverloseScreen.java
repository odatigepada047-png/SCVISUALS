package moscow.rockstar.ui.menu.neverlose;

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

import java.util.Arrays;
import java.util.List;

public class NeverloseScreen extends MenuScreen implements IMinecraft, IScaledResolution {

    private float x, y, width, height;
    private String currentCategory = "Fight";
    
    private final List<String> categories = Arrays.asList(
        "General", "Fight", "Movement", "Player", "Render", "Misc"
    );
    
    private final List<String> clientItems = Arrays.asList(
        "Config", "Theme", "Team", "GPS"
    );
    
    private final List<String> gameItems = Arrays.asList(
        "Settings", "Log out"
    );

    public NeverloseScreen() {
        this.width = 600.0f;
        this.height = 400.0f;
    }

    @Override
    protected void init() {
        this.closing = false;
        super.init();
    }

    @Override
    public void render(UIContext context) {
        this.menuAnimation.update(this.closing ? 0.0f : 1.0f);
        
        this.x = sr.getGuiScaledWidth() / 2.0f - this.width / 2.0f;
        this.y = sr.getGuiScaledHeight() / 2.0f - this.height / 2.0f;
        
        float alpha = this.menuAnimation.getRGB();
        
        // Use RenderUtility scale like in ModernScreen to handle opening animation and state cleanup
        RenderUtility.scale(context.pose(), this.x + this.width / 2.0f, this.y + this.height / 2.0f, 0.5f + 0.5f * alpha);
        
        // Main Window Background (Dark, slightly transparent)
        ColorRGBA bgColor = new ColorRGBA(11, 14, 20, (int)(255 * alpha * 0.95f));
//         context.drawRoundedRect(x, y, width, height, BorderRadius.all(8.0f), bgColor);
        
        // Sidebar Background (Darker)
        ColorRGBA sidebarColor = new ColorRGBA(7, 9, 13, (int)(255 * alpha));
//         context.drawRoundedRect(x, y, 140.0f, height, BorderRadius.left(8.0f, 8.0f), sidebarColor);
        
        // Logo "VOID."
        FontBatching semiboldBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.SEMIBOLD);
        context.drawText(Fonts.SEMIBOLD.getFont(14.0f), "VOID.", x + 20.0f, y + 20.0f, new ColorRGBA(255, 255, 255, (int)(255 * alpha)));
        semiboldBatching.draw();
        
        // Sidebar Items
        float yOff = y + 50.0f;
        
        FontBatching regularBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.REGULAR);
        FontBatching mediumBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.MEDIUM);
        
        // General Categories
        context.drawText(Fonts.REGULAR.getFont(6.0f), "General", x + 20.0f, yOff, new ColorRGBA(139, 148, 158, (int)(255 * alpha * 0.5f)));
        yOff += 15.0f;
        
        for (String cat : categories) {
            boolean active = cat.equals(currentCategory);
            ColorRGBA color = active ? new ColorRGBA(255, 255, 255, (int)(255 * alpha)) : new ColorRGBA(139, 148, 158, (int)(255 * alpha));
            
            if (active) {
                // Active category background
//                 context.drawRoundedRect(x + 10.0f, yOff - 4.0f, 120.0f, 18.0f, BorderRadius.all(4.0f), new ColorRGBA(25, 33, 44, (int)(255 * alpha)));
            }
            
            context.drawText(Fonts.MEDIUM.getFont(8.0f), cat, x + 20.0f, yOff, color);
            yOff += 20.0f;
        }
        
        yOff += 10.0f;
        // Client Section
        context.drawText(Fonts.REGULAR.getFont(6.0f), "Client", x + 20.0f, yOff, new ColorRGBA(139, 148, 158, (int)(255 * alpha * 0.5f)));
        yOff += 15.0f;
        
        for (String item : clientItems) {
            context.drawText(Fonts.MEDIUM.getFont(8.0f), item, x + 20.0f, yOff, new ColorRGBA(139, 148, 158, (int)(255 * alpha)));
            yOff += 20.0f;
        }
        
        yOff += 10.0f;
        // Game Section
        context.drawText(Fonts.REGULAR.getFont(6.0f), "Game", x + 20.0f, yOff, new ColorRGBA(139, 148, 158, (int)(255 * alpha * 0.5f)));
        yOff += 15.0f;
        
        for (String item : gameItems) {
            ColorRGBA color = item.equals("Log out") ? new ColorRGBA(255, 100, 100, (int)(255 * alpha)) : new ColorRGBA(139, 148, 158, (int)(255 * alpha));
            context.drawText(Fonts.MEDIUM.getFont(8.0f), item, x + 20.0f, yOff, color);
            yOff += 20.0f;
        }
        
        regularBatching.draw();
        mediumBatching.draw();
        
        // Main Content Area (Modules)
        float contentX = x + 150.0f;
        float contentY = y + 20.0f;
        
        // Draw Card 1: Aura
        drawModuleCard(context, "Aura", contentX, contentY, 210.0f, 200.0f, alpha);
        
        // Draw Card 2: VehicleBreaker
        drawModuleCard(context, "VehicleBreaker", contentX + 230.0f, contentY, 200.0f, 80.0f, alpha);
        
        // Draw Card 3: AimDatasetCollector
        drawModuleCard(context, "AimDatasetCollector", contentX + 230.0f, contentY + 100.0f, 200.0f, 200.0f, alpha);
        
        // End RenderUtility state
        RenderUtility.end(context.pose());
    }
    
    private void drawModuleCard(UIContext context, String name, float cardX, float cardY, float cardWidth, float cardHeight, float alpha) {
        ColorRGBA cardBg = new ColorRGBA(21, 25, 34, (int)(255 * alpha));
//         context.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, BorderRadius.all(6.0f), cardBg);
        
        FontBatching semiboldBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.SEMIBOLD);
        FontBatching regularBatching = new FontBatching(DefaultVertexFormat.POSITION_TEX_COLOR, Fonts.REGULAR);
        
        // Module Name
        context.drawText(Fonts.SEMIBOLD.getFont(10.0f), name, cardX + 10.0f, cardY + 10.0f, new ColorRGBA(255, 255, 255, (int)(255 * alpha)));
        
        // Toggle Button (Top Right of card)
        context.drawText(Fonts.REGULAR.getFont(7.0f), "Toggle", cardX + cardWidth - 40.0f, cardY + 10.0f, new ColorRGBA(139, 148, 158, (int)(255 * alpha)));
        
        // Active Checkbox
//         context.drawRoundedRect(cardX + 10.0f, cardY + 30.0f, 12.0f, 12.0f, BorderRadius.all(2.0f), new ColorRGBA(35, 43, 54, (int)(255 * alpha)));
        context.drawText(Fonts.REGULAR.getFont(8.0f), "Active", cardX + 28.0f, cardY + 32.0f, new ColorRGBA(255, 255, 255, (int)(255 * alpha)));
        
        // Description
        context.drawText(Fonts.REGULAR.getFont(6.0f), "No description available", cardX + 10.0f, cardY + 50.0f, new ColorRGBA(139, 148, 158, (int)(255 * alpha * 0.7f)));
        
        // Example Slider (if card is big enough)
        if (cardHeight > 100.0f) {
            context.drawText(Fonts.REGULAR.getFont(7.0f), "Range", cardX + 10.0f, cardY + 70.0f, new ColorRGBA(255, 255, 255, (int)(255 * alpha)));
            context.drawText(Fonts.REGULAR.getFont(7.0f), "3.0", cardX + cardWidth - 25.0f, cardY + 70.0f, new ColorRGBA(255, 255, 255, (int)(255 * alpha)));
            
            // Slider bar
//             context.drawRoundedRect(cardX + 10.0f, cardY + 85.0f, cardWidth - 20.0f, 4.0f, BorderRadius.all(2.0f), new ColorRGBA(35, 43, 54, (int)(255 * alpha)));
            // Slider fill
//             context.drawRoundedRect(cardX + 10.0f, cardY + 85.0f, (cardWidth - 20.0f) * 0.6f, 4.0f, BorderRadius.all(2.0f), new ColorRGBA(0, 168, 255, (int)(255 * alpha)));
            // Slider knob
//             context.drawRoundedRect(cardX + 10.0f + (cardWidth - 20.0f) * 0.6f - 3.0f, cardY + 84.0f, 6.0f, 6.0f, BorderRadius.all(3.0f), new ColorRGBA(255, 255, 255, (int)(255 * alpha)));
        }
        
        // Example Dropdown (if card is big enough)
        if (cardHeight > 150.0f) {
            context.drawText(Fonts.REGULAR.getFont(7.0f), "Target sort", cardX + 10.0f, cardY + 105.0f, new ColorRGBA(255, 255, 255, (int)(255 * alpha)));
            
            // Dropdown box
//             context.drawRoundedRect(cardX + 10.0f, cardY + 120.0f, cardWidth - 20.0f, 18.0f, BorderRadius.all(4.0f), new ColorRGBA(35, 43, 54, (int)(255 * alpha)));
            context.drawText(Fonts.REGULAR.getFont(7.0f), "Distance", cardX + 15.0f, cardY + 125.0f, new ColorRGBA(255, 255, 255, (int)(255 * alpha)));
        }
        
        semiboldBatching.draw();
        regularBatching.draw();
    }
}
