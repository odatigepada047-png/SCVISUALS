package moscow.rockstar.systems.modules.constructions.customhand;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.modules.visuals.CustomHand;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

import static moscow.rockstar.utility.interfaces.IMinecraft.mc;

public class CustomHandScreen extends CustomScreen implements IScaledResolution {
    private final CustomHand module;
    private final Popup mainHand = new Popup(140.0f, 100.0f).title("modules.settings.custom_hand.main_group");
    private final Popup offHand = new Popup(340.0f, 100.0f).title("modules.settings.custom_hand.off_group");
    private final Popup effects = new Popup(540.0f, 100.0f).title("modules.settings.custom_hand.effects_group");

    public CustomHandScreen(CustomHand module) {
        this.module = module;
        this.mainHand.setting(module.getMainScale());
        this.mainHand.setting(module.getMainOffsetX());
        this.mainHand.setting(module.getMainOffsetY());
        this.mainHand.setting(module.getMainOffsetZ());

        this.offHand.setting(module.getOffScale());
        this.offHand.setting(module.getOffOffsetX());
        this.offHand.setting(module.getOffOffsetY());
        this.offHand.setting(module.getOffOffsetZ());

        this.effects.setting(module.getEffectMode());
        this.effects.setting(module.getEffectAlphaSetting());
    }

    @Override
    public void render(UIContext context) {
        float baseX = IScaledResolution.sr.getGuiScaledWidth() / 2.0f - 290.0f;
        float baseY = IScaledResolution.sr.getGuiScaledHeight() / 2.0f - 110.0f;

        this.mainHand.setX(baseX);
        this.offHand.setX(baseX + 195.0f);
        this.effects.setX(baseX + 390.0f);

        this.mainHand.setY(baseY);
        this.offHand.setY(baseY);
        this.effects.setY(baseY);

        this.mainHand.setWidth(185.0f);
        this.offHand.setWidth(185.0f);
        this.effects.setWidth(185.0f);

        this.mainHand.render(context);
        this.offHand.render(context);
        this.effects.render(context);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        this.mainHand.onMouseClicked(mouseX, mouseY, button);
        this.offHand.onMouseClicked(mouseX, mouseY, button);
        this.effects.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.mainHand.onMouseReleased(mouseX, mouseY, button);
        this.offHand.onMouseReleased(mouseX, mouseY, button);
        this.effects.onMouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.mainHand.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        this.offHand.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        this.effects.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        this.mainHand.onKeyPressed(event.key(), event.scancode(), event.modifiers());
        this.offHand.onKeyPressed(event.key(), event.scancode(), event.modifiers());
        this.effects.onKeyPressed(event.key(), event.scancode(), event.modifiers());
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char chr = (char) event.codepoint();
        this.mainHand.charTyped(chr, 0);
        this.offHand.charTyped(chr, 0);
        this.effects.charTyped(chr, 0);
        return super.charTyped(event);
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
        if (TextField.LAST_FIELD != null) {
            TextField.LAST_FIELD.setFocused(false);
        }
        super.onClose();
        mc.setScreen((Screen) Rockstar.getInstance().getMenuScreen());
    }
}
