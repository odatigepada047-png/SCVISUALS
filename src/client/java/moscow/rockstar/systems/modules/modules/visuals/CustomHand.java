package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HandRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.constructions.customhand.CustomHandScreen;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ButtonSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.HumanoidArm;

@ModuleInfo(name="Custom Hand", category=ModuleCategory.VISUALS, desc="modules.descriptions.custom_hand")
public class CustomHand extends BaseModule {
    private final ButtonSetting openMenu = new ButtonSetting(this, "modules.settings.custom_hand.open_menu").action(() -> mc.setScreen((Screen)new CustomHandScreen(this)));

    private final SliderSetting mainScale = new SliderSetting(this, "modules.settings.custom_hand.main_scale").min(0.2f).max(3.0f).currentValue(1.0f).step(0.01f);
    private final SliderSetting mainOffsetX = new SliderSetting(this, "modules.settings.custom_hand.main_offset_x").min(-2.5f).max(2.5f).currentValue(0.0f).step(0.01f);
    private final SliderSetting mainOffsetY = new SliderSetting(this, "modules.settings.custom_hand.main_offset_y").min(-2.5f).max(2.5f).currentValue(0.0f).step(0.01f);
    private final SliderSetting mainOffsetZ = new SliderSetting(this, "modules.settings.custom_hand.main_offset_z").min(-2.5f).max(2.5f).currentValue(0.0f).step(0.01f);

    private final SliderSetting offScale = new SliderSetting(this, "modules.settings.custom_hand.off_scale").min(0.2f).max(3.0f).currentValue(1.0f).step(0.01f);
    private final SliderSetting offOffsetX = new SliderSetting(this, "modules.settings.custom_hand.off_offset_x").min(-2.5f).max(2.5f).currentValue(0.0f).step(0.01f);
    private final SliderSetting offOffsetY = new SliderSetting(this, "modules.settings.custom_hand.off_offset_y").min(-2.5f).max(2.5f).currentValue(0.0f).step(0.01f);
    private final SliderSetting offOffsetZ = new SliderSetting(this, "modules.settings.custom_hand.off_offset_z").min(-2.5f).max(2.5f).currentValue(0.0f).step(0.01f);

    private final ModeSetting effectMode = new ModeSetting(this, "modules.settings.custom_hand.effect_mode");
    private final ModeSetting.Value effectOff = new ModeSetting.Value(this.effectMode, "modules.settings.custom_hand.effect_mode.none");
    private final ModeSetting.Value effectGlass = new ModeSetting.Value(this.effectMode, "modules.settings.custom_hand.effect_mode.glass");
    private final ModeSetting.Value effectBlur = new ModeSetting.Value(this.effectMode, "modules.settings.custom_hand.effect_mode.blur");
    private final SliderSetting effectAlpha = new SliderSetting(this, "modules.settings.custom_hand.effect_alpha").min(0.1f).max(1.0f).currentValue(0.65f).step(0.01f);

    private final EventListener<HandRenderEvent> onHandRender = event -> {
        // Transformations are now handled in HeldItemRendererMixin for better precision and effect support.
    };

    public ButtonSetting getOpenMenu() {
        return this.openMenu;
    }

    public SliderSetting getMainScale() {
        return this.mainScale;
    }

    public SliderSetting getMainOffsetX() {
        return this.mainOffsetX;
    }

    public SliderSetting getMainOffsetY() {
        return this.mainOffsetY;
    }

    public SliderSetting getMainOffsetZ() {
        return this.mainOffsetZ;
    }

    public SliderSetting getOffScale() {
        return this.offScale;
    }

    public SliderSetting getOffOffsetX() {
        return this.offOffsetX;
    }

    public SliderSetting getOffOffsetY() {
        return this.offOffsetY;
    }

    public SliderSetting getOffOffsetZ() {
        return this.offOffsetZ;
    }

    public ModeSetting getEffectMode() {
        return this.effectMode;
    }

    public boolean isEffectOff() {
        return this.effectOff.isSelected();
    }

    public boolean isEffectGlass() {
        return this.effectGlass.isSelected();
    }

    public boolean isEffectBlur() {
        return this.effectBlur.isSelected();
    }

    public float getEffectAlpha() {
        return this.effectAlpha.getCurrentValue();
    }

    public SliderSetting getEffectAlphaSetting() {
        return this.effectAlpha;
    }
}
