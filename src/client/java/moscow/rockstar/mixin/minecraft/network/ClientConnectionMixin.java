package moscow.rockstar.mixin.minecraft.network;

import io.netty.channel.ChannelHandlerContext;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Connection.class})
public class ClientConnectionMixin
implements IMinecraft {
    @Unique
    private static boolean stackOverflowFix;

    @Inject(method={"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void triggerReceivePacketEvent(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        ReceivePacketEvent event = new ReceivePacketEvent(packet);
        Rockstar.getInstance().getEventManager().triggerEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/protocol/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void triggerSendPacketEvent(Packet<?> packet, CallbackInfo ci) {
        Packet<?> newPacket;
        SendPacketEvent event = new SendPacketEvent(packet);
        if (stackOverflowFix) {
            return;
        }
        Rockstar.getInstance().getEventManager().triggerEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
        if ((newPacket = event.getPacket()) != packet) {
            ci.cancel();
            stackOverflowFix = true;
            ((Connection) (Object) this).send(newPacket);
            stackOverflowFix = false;
        }
    }
}
