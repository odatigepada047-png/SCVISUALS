/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.viaversion.viafabricplus.ViaFabricPlus
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.network.chat.Component
 */
package moscow.rockstar.systems.event.handlers;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ServerConnectionEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.ui.menu.modern.components.ModernModels;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

public class ServerConnectionHandler
implements IMinecraft {
    private boolean messageSent = false;
    private boolean connected;
    private final EventListener<ServerConnectionEvent> onServerConnection = event -> {
        this.connected = true;
        this.messageSent = false;
    };
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTick = event -> {
        if (this.connected && !this.messageSent && ServerConnectionHandler.mc.player != null && ServerConnectionHandler.mc.player.tickCount > 100 && mc.getConnection() != null && mc.getConnection().getServerData() != null) {
            String warning = Localizator.translate("chat.connection_warning", mc.getConnection().getServerData().ip, "Unknown");
            MessageUtility.info(Component.literal((String)warning));
            this.messageSent = true;
        }
    };

    public ServerConnectionHandler() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }
}

