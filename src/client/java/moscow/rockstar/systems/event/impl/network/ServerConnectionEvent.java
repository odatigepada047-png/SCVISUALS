/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.network.CookieStorage
 *  net.minecraft.client.network.ServerAddress
 *  net.minecraft.client.network.ServerInfo
 */
package moscow.rockstar.systems.event.impl.network;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;

public class ServerConnectionEvent
extends Event {
    private final ServerAddress address;
    private final ServerData info;
    private final TransferState transferState;

    @Generated
    public ServerAddress getAddress() {
        return this.address;
    }

    @Generated
    public ServerData getInfo() {
        return this.info;
    }

    @Generated
    public TransferState getTransferState() {
        return this.transferState;
    }

    @Generated
    public ServerConnectionEvent(ServerAddress address, ServerData info, TransferState transferState) {
        this.address = address;
        this.info = info;
        this.transferState = transferState;
    }
}

