package moscow.rockstar.mixin.minecraft.client.gui.hud;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatHudMixin {
    @Inject(method={"addClientSystemMessage", "addServerSystemMessage"}, at=@At("HEAD"), cancellable=true)
    private void onAddMessage(Component message, CallbackInfo ci) {
        if (message != null && message.getString().contains("Loading avatar!")) {
            ci.cancel();
        }
    }
}
