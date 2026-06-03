package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.inventory.InventoryUtility;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ContainerInput;

@ModuleInfo(name = "BundleAura", category = ModuleCategory.OTHER, desc = "Automatically interacts with bundles and items")
public class BundleAura extends BaseModule {
    private final BooleanSetting autoFill = new BooleanSetting(this, "Auto Fill").enable();
    private final SliderSetting delay = new SliderSetting(this, "Delay")
            .min(0.0f).max(1000.0f).currentValue(100.0f).step(10.0f);

    private long lastTime = 0;

    @Override
    public void tick() {
        if (mc.player == null || mc.player.containerMenu == null) return;
        if (System.currentTimeMillis() - lastTime < delay.getCurrentValue()) return;

        int bundleSlot = InventoryUtility.findItemInContainer(Items.BUNDLE);
        if (bundleSlot == -1) return;

        if (autoFill.isEnabled()) {
            // Пытаемся автоматически заполнить мешочек предметами из текущего окна
            for (int i = 0; i < mc.player.containerMenu.slots.size(); i++) {
                if (i == bundleSlot) continue;
                if (!mc.player.containerMenu.getSlot(i).getItem().isEmpty()) {
                    // Берем мешочек, кликаем правой кнопкой (1) по предмету, кладем мешочек на место
                    InventoryUtility.mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, bundleSlot, 0, ContainerInput.PICKUP, mc.player);
                    InventoryUtility.mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, i, 1, ContainerInput.PICKUP, mc.player);
                    InventoryUtility.mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, bundleSlot, 0, ContainerInput.PICKUP, mc.player);
                    lastTime = System.currentTimeMillis();
                    break;
                }
            }
        }
    }
}
