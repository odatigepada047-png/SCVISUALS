package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "PacketCancel", category = ModuleCategory.OTHER, desc = "Cancels outgoing inventory packets and releases them in a burst")
public class PacketCancel extends BaseModule {
    private final List<Packet<?>> canceledPackets = new ArrayList<>();
    private final BooleanSetting cancelInventory = new BooleanSetting(this, "Inventory Only").enable();

    private final EventListener<SendPacketEvent> onSendPacket = event -> {
        if (mc.player == null) return;
        
        Packet<?> p = event.getPacket();
        if (cancelInventory.isEnabled()) {
            if (p instanceof ServerboundContainerClickPacket || p instanceof ServerboundContainerClosePacket || p instanceof ServerboundPlayerActionPacket) {
                canceledPackets.add(p);
                event.cancel();
            }
        } else {
            canceledPackets.add(p);
            event.cancel();
        }
    };

    @Override
    public void onDisable() {
        if (mc.player != null && mc.player.connection != null) {
            // Выпускаем все накопленные пакеты разом при выключении модуля
            for (Packet<?> p : canceledPackets) {
                mc.player.connection.send(p);
            }
        }
        canceledPackets.clear();
        super.onDisable();
    }
}
