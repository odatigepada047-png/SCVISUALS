package moscow.rockstar.utility.render;

import moscow.rockstar.mixin.accessors.DrawContextAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.Nullable;

/**
 * Holds the GUI context captured during extract; consumed after {@code GuiRenderer.render()}.
 */
public final class GuiDrawContextHolder {
    private static GuiGraphicsExtractor extractor;
    private static float tickDelta;
    private static int mouseX;
    private static int mouseY;
    private static net.minecraft.client.gui.render.GuiRenderer guiRenderer;

    private GuiDrawContextHolder() {
    }

    public static void capture(GuiGraphicsExtractor context, float delta) {
        extractor = context;
        tickDelta = delta;
        mouseX = ((DrawContextAccessor) context).getMouseX();
        mouseY = ((DrawContextAccessor) context).getMouseY();
    }

    public static void captureGuiRenderer(net.minecraft.client.gui.render.GuiRenderer renderer) {
        guiRenderer = renderer;
    }

    @Nullable
    public static net.minecraft.client.gui.render.GuiRenderer getGuiRenderer() {
        return guiRenderer;
    }

    @Nullable
    public static GuiGraphicsExtractor getExtractor() {
        return extractor;
    }

    public static float getTickDelta() {
        return tickDelta;
    }

    public static int getMouseX() {
        return mouseX;
    }

    public static int getMouseY() {
        return mouseY;
    }

    public static void clear() {
        extractor = null;
        guiRenderer = null;
    }
}
