/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;

@ModuleInfo(name="Aspect Ratio", category=ModuleCategory.VISUALS, desc="modules.descriptions.aspect_ratio")
public class AspectRatio
extends BaseModule {
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.aspect_ratio.mode");
    private final ModeSetting.Value ratio4x3 = new ModeSetting.Value(this.mode, "modules.settings.aspect_ratio.mode.4x3");
    private final ModeSetting.Value ratio16x9 = new ModeSetting.Value(this.mode, "modules.settings.aspect_ratio.mode.16x9").select();
    private final ModeSetting.Value ratio1x1 = new ModeSetting.Value(this.mode, "modules.settings.aspect_ratio.mode.1x1");
    private final ModeSetting.Value ratioCustom = new ModeSetting.Value(this.mode, "modules.settings.aspect_ratio.mode.custom");
    
    private final SliderSetting customRatio = new SliderSetting(this, "modules.settings.aspect_ratio.custom", () -> !this.ratioCustom.isSelected())
        .min(0.1f)
        .max(2.0f)
        .currentValue(1.0f)
        .step(0.01f);

    public float getAspectRatio() {
        if (!this.isEnabled()) {
            return 0.0f; // Default aspect ratio (will use window's aspect ratio)
        }
        
        if (this.ratio4x3.isSelected()) {
            return 4.0f / 3.0f;
        } else if (this.ratio16x9.isSelected()) {
            return 16.0f / 9.0f;
        } else if (this.ratio1x1.isSelected()) {
            return 1.0f;
        } else if (this.ratioCustom.isSelected()) {
            return this.customRatio.getCurrentValue();
        }
        
        return 0.0f;
    }
}
