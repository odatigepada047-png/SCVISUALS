package moscow.rockstar.mixin.minecraft.client.gui.components.events;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.window.ChatReleaseEvent;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {
    @Inject(method = "mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z", at = @At("HEAD"))
    private void rockstar$onChatMouseRelease(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ChatScreen) {
            Rockstar.getInstance().getEventManager().triggerEvent(
                    new ChatReleaseEvent((float) event.x(), (float) event.y(), event.button()));
        }
    }
}
