package moscow.rockstar.systems.modules.modules.visuals;

import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.menu.api.MenuCloseListener;
import moscow.rockstar.ui.menu.dropdown.DropDownScreen;
import moscow.rockstar.ui.menu.modern.ModernScreen;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.client.gui.screens.Screen;

@ModuleInfo(name = "Menu", category = ModuleCategory.VISUALS, key = 344, desc = "modules.descriptions.menu")
public class MenuModule extends BaseModule {
    @SuppressWarnings("unused")
    private static final MenuCloseListener menuCloseListener = new MenuCloseListener();
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.menu.mode");
    private final ModeSetting.Value dropdown = new ModeSetting.Value(this.mode, "modules.settings.menu.mode.dropdown");
    private final ModeSetting.Value modern = new ModeSetting.Value(this.mode, "modules.settings.menu.mode.modern");

    private ModernScreen modernScreen;
    private DropDownScreen dropDownScreen;

    public ModernScreen getModernScreen() {
        if (this.modernScreen == null) {
            this.modernScreen = new ModernScreen();
        }
        return this.modernScreen;
    }

    public DropDownScreen getDropDownScreen() {
        if (this.dropDownScreen == null) {
            this.dropDownScreen = new DropDownScreen();
        }
        return this.dropDownScreen;
    }

    @Override
    public void onEnable() {
        if (MenuModule.mc.screen instanceof MenuScreen) {
            return;
        }
        if (this.modern.isSelected()) {
            Rockstar.getInstance().setMenuScreen(this.getModernScreen());
        } else {
            Rockstar.getInstance().setMenuScreen(this.getDropDownScreen());
        }
        MenuScreen menuScreen = Rockstar.getInstance().getMenuScreen();
        menuScreen.setClosing(false);
        mc.setScreen(menuScreen);
        Sounds soundsModule = Rockstar.getInstance().getModuleManager().getModule(Sounds.class);
        if (soundsModule.isEnabled()) {
            ClientSounds.CLICKGUI_OPEN.play(soundsModule.getVolume().getCurrentValue());
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (MenuModule.mc.screen instanceof MenuScreen) {
            mc.setScreen(null);
            Rockstar.getInstance().getMenuScreen().setClosing(true);
        }
        this.modernScreen = null;
        this.dropDownScreen = null;
        super.onDisable();
    }

    @Generated
    public ModeSetting.Value getModern() {
        return this.modern;
    }
}
