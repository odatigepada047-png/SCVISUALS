/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.util.InteractionHand
 */
package moscow.rockstar.systems.modules.constructions.swinganim;

import java.util.Collection;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.modules.constructions.swinganim.PopupEvent;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingManager;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingPhase;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.PresetComponent;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPreset;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPresetManager;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.components.textfield.TextField;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.InteractionHand;

public class SwingAnimScreen
extends CustomScreen
implements IScaledResolution,
IMinecraft {
    private final Popup presets = new Popup(100.0f, 100.0f).title("presets");
    private final Popup shared = new Popup(100.0f, 100.0f).title("shared");
    private final Popup start = new Popup(300.0f, 100.0f).title("anim_from");
    private final Popup end = new Popup(500.0f, 100.0f).title("anim_to");

    public SwingAnimScreen() {
        SwingManager manager = Rockstar.getInstance().getSwingManager();
        Rockstar.getInstance().getSwingPresetManager().refresh();
        this.presets.add(new PresetComponent());
        this.applySettings(manager.getSharedSettings().settings, this.shared);
        this.applySettings(manager.getStartPhase().settings, this.start);
        this.applySettings(manager.getEndPhase().settings, this.end);
        SwingManager swingManager = Rockstar.getInstance().getSwingManager();
        String swing = swingManager.getCurrent();
        for (SwingPreset value : Rockstar.getInstance().getSwingManager().getPresets()) {
            if (!value.getName().equals(swing)) continue;
            swingManager.getBezier().start(value.getBezierStart()).end(value.getBezierEnd());
            swingManager.getBack().enabled(value.isSwingBack());
            swingManager.getSpeed().setCurrentValue(value.getSpeed());
            SwingPhase start = swingManager.getStartPhase();
            start.getAnchorX().setCurrentValue(value.getFrom().anchorX());
            start.getAnchorY().setCurrentValue(value.getFrom().anchorY());
            start.getAnchorZ().setCurrentValue(value.getFrom().anchorZ());
            start.getMoveX().setCurrentValue(value.getFrom().moveX());
            start.getMoveY().setCurrentValue(value.getFrom().moveY());
            start.getMoveZ().setCurrentValue(value.getFrom().moveZ());
            start.getRotateX().setCurrentValue(value.getFrom().rotateX());
            start.getRotateY().setCurrentValue(value.getFrom().rotateY());
            start.getRotateZ().setCurrentValue(value.getFrom().rotateZ());
            SwingPhase end = swingManager.getEndPhase();
            end.getAnchorX().setCurrentValue(value.getTo().anchorX());
            end.getAnchorY().setCurrentValue(value.getTo().anchorY());
            end.getAnchorZ().setCurrentValue(value.getTo().anchorZ());
            end.getMoveX().setCurrentValue(value.getTo().moveX());
            end.getMoveY().setCurrentValue(value.getTo().moveY());
            end.getMoveZ().setCurrentValue(value.getTo().moveZ());
            end.getRotateX().setCurrentValue(value.getTo().rotateX());
            end.getRotateY().setCurrentValue(value.getTo().rotateY());
            end.getRotateZ().setCurrentValue(value.getTo().rotateZ());
            swingManager.setCurrent(swing);
        }
    }

    @Override
    public void render(UIContext context) {
        float startX = IScaledResolution.sr.getGuiScaledWidth() / 2.0f - 360.0f + 4.0f;
        this.presets.setX(startX);
        this.shared.setX(startX += 180.0f);
        this.start.setX(startX += 180.0f);
        this.end.setX(startX += 180.0f);
        this.popupEvent(popup -> popup.render(context));
        this.popupEvent(popup -> popup.setY(sr.getGuiScaledHeight() / 2.0f - this.end.getHeight() / 2.0f));
        this.popupEvent(popup -> popup.setWidth(170.0f));
        if (SwingAnimScreen.mc.player.tickCount % 20 == 0) {
            SwingAnimScreen.mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        this.popupEvent(popup -> popup.onMouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.popupEvent(popup -> popup.onMouseReleased(mouseX, mouseY, button));
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.popupEvent(popup -> popup.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        this.popupEvent(popup -> popup.onKeyPressed(event.key(), event.scancode(), event.modifiers()));
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        char chr = (char) event.codepoint();
        this.popupEvent(popup -> popup.charTyped(chr, 0));
        return super.charTyped(event);
    }

    private void applySettings(Collection<Setting> settings, Popup target) {
        for (Setting setting : settings) {
            target.setting(setting);
        }
    }

    private void popupEvent(PopupEvent event) {
        event.call(this.presets);
        event.call(this.shared);
        event.call(this.start);
        event.call(this.end);
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
        SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
        if (manager.getCurrent() != null) {
            manager.getCurrent().save();
        }
        if (TextField.LAST_FIELD != null) {
            TextField.LAST_FIELD.setFocused(false);
        }
        super.onClose();
        mc.setScreen((Screen)Rockstar.getInstance().getMenuScreen());
    }
}

