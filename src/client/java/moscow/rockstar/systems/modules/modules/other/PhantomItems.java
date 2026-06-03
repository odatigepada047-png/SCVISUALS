package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;

@ModuleInfo(name = "Phantom Items", category = ModuleCategory.OTHER, desc = "modules.descriptions.phantom_items")
public class PhantomItems extends BaseModule {
    private final SliderSetting loadDelay = new SliderSetting((SettingsContainer)this, "load_delay")
            .currentValue(1000.0f)
            .min(100.0f)
            .max(3000.0f)
            .step(50.0f)
            .suffix(" ms");

    private Screen lastScreen = null;
    private long openTime = 0;

    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        if (mc.player == null || mc.screen == null) return;

        Screen currentScreen = mc.screen;
        if (currentScreen instanceof AbstractContainerScreen) {
            if (currentScreen != lastScreen) {
                openTime = System.currentTimeMillis();
                lastScreen = currentScreen;
            }

            Packet<?> packet = event.getPacket();
            if (packet instanceof ClientboundContainerSetSlotPacket || packet instanceof ClientboundContainerSetContentPacket) {
                if (System.currentTimeMillis() - openTime > (long) loadDelay.getCurrentValue()) {
                    event.cancel();
                }
            }
        } else {
            lastScreen = null;
            openTime = 0;
        }
    };

    @Override
    public void tick() {
        if (mc.screen == null) {
            lastScreen = null;
            openTime = 0;
        }
    }

    public SliderSetting getLoadDelay() {
        return this.loadDelay;
    }
}
