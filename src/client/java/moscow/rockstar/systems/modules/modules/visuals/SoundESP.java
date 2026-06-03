package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SelectSetting;

@ModuleInfo(name="Sound ESP", category=ModuleCategory.VISUALS, enabledByDefault=true, desc="Показывает где был воспроизведен звук")
public class SoundESP extends BaseModule {
    private final SelectSetting select = new SelectSetting(this, "Отображать");
    private final SelectSetting.Value trident = new SelectSetting.Value(this.select, "Трезубец").select();
    private final SelectSetting.Value tnt = new SelectSetting.Value(this.select, "Динамит");
    private final SelectSetting.Value fireworks = new SelectSetting.Value(this.select, "Фейерверки");
}

