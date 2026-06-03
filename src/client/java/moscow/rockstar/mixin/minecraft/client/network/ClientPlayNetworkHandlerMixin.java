package moscow.rockstar.mixin.minecraft.client.network;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.PickupEvent;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.ui.hud.impl.TargetHud;
import moscow.rockstar.utility.chunkanimator.ChunkAnimator;
import moscow.rockstar.utility.game.WorldUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public abstract class ClientPlayNetworkHandlerMixin
extends ClientCommonPacketListenerImpl
implements IMinecraft {
    @Unique
    private Rotation oldRotation = Rotation.ZERO;

    protected ClientPlayNetworkHandlerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method={"handleTakeItemEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;getEntity(I)Lnet/minecraft/world/entity/Entity;", ordinal=0)})
    private void onItemPickupAnimation(ClientboundTakeItemEntityPacket packet, CallbackInfo info) {
        Entity itemEntity = this.minecraft.level.getEntity(packet.getItemId());
        Entity entity = this.minecraft.level.getEntity(packet.getPlayerId());
        if (itemEntity instanceof ItemEntity && entity == this.minecraft.player) {
            Rockstar.getInstance().getEventManager().triggerEvent(new PickupEvent(((ItemEntity)itemEntity).getItem(), packet.getAmount()));
        }
    }

    @Inject(method={"handleLogin"}, at={@At(value="TAIL")})
    private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        WorldUtility.blockEntities.clear();
        ChunkAnimator.clear();
        TargetHud.SKELETON_LINES.clear();
        Fonts.clearWidthCaches();
        Rockstar.getInstance().getEventManager().triggerEvent(new WorldChangeEvent());
    }

    @Inject(method={"handleMovePlayer"}, at={@At(value="HEAD")})
    public void savePlayerRotation(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (ClientPlayNetworkHandlerMixin.mc.player == null) {
            return;
        }
        this.oldRotation = new Rotation(ClientPlayNetworkHandlerMixin.mc.player.getYRot(), ClientPlayNetworkHandlerMixin.mc.player.getXRot());
    }

    @Inject(method={"handleMovePlayer"}, at={@At(value="RETURN")})
    public void modifyPlayerRotation(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (ClientPlayNetworkHandlerMixin.mc.player == null) {
            return;
        }
        Rotation realServerRotation = new Rotation(packet.change().yRot(), packet.change().xRot());
    }
}
