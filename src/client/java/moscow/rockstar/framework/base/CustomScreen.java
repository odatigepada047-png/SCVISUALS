package moscow.rockstar.framework.base;

import moscow.rockstar.framework.objects.MouseButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class CustomScreen extends Screen {
    protected CustomScreen() {
        super(Component.empty());
    }

    public abstract void render(UIContext context);

    public void renderCustomUi(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.render(UIContext.of(context, mouseX, mouseY, delta));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // RockReady used an empty renderBackground(); block vanilla fullscreen blur in-world.
    }


    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        this.onMouseClicked(event.x(), event.y(), MouseButton.fromButtonIndex(event.button()));
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.onMouseReleased(event.x(), event.y(), MouseButton.fromButtonIndex(event.button()));
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        this.onMouseDragged(event.x(), event.y(), MouseButton.fromButtonIndex(event.button()), dragX, dragY);
        return super.mouseDragged(event, dragX, dragY);
    }

    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
    }

    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
    }

    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
    }
}
