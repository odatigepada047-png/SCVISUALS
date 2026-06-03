package moscow.rockstar.mixin.minecraft.client.gui.screen;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.window.ChatClickEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ChatScreen.class})
public class ChatScreenMixin implements IMinecraft {
    @Shadow
    protected EditBox input;
    @Shadow
    private CommandSuggestions commandSuggestions;

    @Inject(method={"handleChatInput"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendMessage(String text, boolean addToHistory, CallbackInfo ci) {
        if (Rockstar.getInstance().getCommandManager().dispatch(text)) {
            if (addToHistory) {
                mc.gui.getChat().addRecentChat(text);
            }
            ci.cancel();
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="RETURN")})
    public void captureChatContext(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        moscow.rockstar.utility.render.GuiDrawContextHolder.capture(context, delta);
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")})
    private void onMouseClick(MouseButtonEvent event, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        Rockstar.getInstance().getEventManager().triggerEvent(new ChatClickEvent((float)event.x(), (float)event.y(), event.button()));
    }

}
