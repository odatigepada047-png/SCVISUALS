package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import net.minecraft.world.level.block.state.BlockState;

@ModuleInfo(name = "Optimizer", category = ModuleCategory.OTHER, desc = "Оптимизация производительности игры")
public class Optimizer extends BaseModule {
    private final BooleanSetting hideOres = new BooleanSetting(this, "Hide Ores", "Скрывает отрисовку руд (включая адские и древние обломки) для повышения FPS").enable();

    public Optimizer() {
    }

    public boolean shouldHide(BlockState state) {
        if (!this.isEnabled() || !this.hideOres.isEnabled()) {
            return false;
        }
        String name = state.getBlock().getDescriptionId();
        if (name == null) {
            return false;
        }
        return name.contains("_ore") || name.contains("ancient_debris") || name.contains("gilded_blackstone");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }
}
