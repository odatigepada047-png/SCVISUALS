package moscow.rockstar.mixin.minecraft.client.network;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.CloseScreenEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEndEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.SlowDownEvent;
import moscow.rockstar.systems.modules.modules.player.InvUtils;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.mixins.ClientPlayerEntityAddition;
import moscow.rockstar.utility.rotations.RotationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LocalPlayer.class})
public class ClientPlayerEntityMixin
implements ClientPlayerEntityAddition,
IMinecraft {
    @Unique
    private int groundTicks = 0;

    @Redirect(method={"aiStep"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"), require=0)
    private boolean onIsUsingItemRedirect(LocalPlayer player) {
        SlowDownEvent slowDownEvent = new SlowDownEvent();
        Rockstar.getInstance().getEventManager().triggerEvent(slowDownEvent);
        return player.isUsingItem() && player.getVehicle() == null && !slowDownEvent.isCancelled();
    }

    @ModifyExpressionValue(method={"aiStep"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/KeyMapping;isPressed()Z")}, require=0)
    public boolean unpressSprintKey(boolean original) {
        return original;
    }

    @ModifyExpressionValue(method={"aiStep"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;canSprint()Z")}, require=0)
    private boolean disallowSprinting(boolean original) {
        return original;
    }

    @WrapWithCondition(method={"clientSideCloseContainer"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")})
    private boolean preventCloseScreen(Minecraft instance, Screen screen) {
        Rockstar.getInstance().getEventManager().triggerEvent(new CloseScreenEvent(screen));
        return true;
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    public void triggerTickEvent(CallbackInfo ci) {
        Rockstar.getInstance().getEventManager().triggerEvent(new ClientPlayerTickEvent());
    }

    @Inject(method={"tick"}, at={@At(value="RETURN")})
    public void triggerTickEndEvent(CallbackInfo ci) {
        Rockstar.getInstance().getEventManager().triggerEvent(new ClientPlayerTickEndEvent());
    }

    @Inject(method={"aiStep"}, at={@At(value="HEAD")}, require=0)
    public void updateOnGroundTicks(CallbackInfo ci) {
        this.groundTicks = ClientPlayerEntityMixin.mc.player != null && ClientPlayerEntityMixin.mc.player.onGround() ? ++this.groundTicks : 0;
    }

    @Redirect(method={"tick"}, slice=@Slice(from=@At(value="NEW", target="Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket$Rot;"), to=@At(value="INVOKE", target="Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket$Rot;<init>(FFZZ)V")), at=@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
    private float replaceMovePacketYaw(LocalPlayer instance) {
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        float yaw = rotationHandler.isIdling() ? instance.getYRot() : rotationHandler.getCurrentRotation().getYRot();
        rotationHandler.getServerRotation().setYRot(yaw);
        return yaw;
    }

    @Redirect(method={"tick"}, slice=@Slice(from=@At(value="NEW", target="Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket$Rot;"), to=@At(value="INVOKE", target="Lnet/minecraft/network/protocol/game/ServerboundMovePlayerPacket$Rot;<init>(FFZZ)V")), at=@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
    private float replaceMovePacketPitch(LocalPlayer instance) {
        RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
        float pitch = rotationHandler.isIdling() ? instance.getXRot() : rotationHandler.getCurrentRotation().getXRot();
        rotationHandler.getServerRotation().setXRot(pitch);
        return pitch;
    }

    @Inject(method={"drop"}, at={@At(value="HEAD")}, cancellable=true)
    private void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        InvUtils slotLock = Rockstar.getInstance().getModuleManager().getModule(InvUtils.class);
        if (slotLock.isEnabled() && slotLock.getSlotLock().isSelected() && slotLock.isLocked(ClientPlayerEntityMixin.mc.player.getInventory().getSelectedSlot())) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Override
    public int rockstar$getOnGroundTicks() {
        return this.groundTicks;
    }
}
