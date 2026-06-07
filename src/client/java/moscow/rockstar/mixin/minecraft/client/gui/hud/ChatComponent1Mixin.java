package moscow.rockstar.mixin.minecraft.client.gui.hud;

import moscow.rockstar.systems.modules.modules.visuals.Beatifuly;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$1")
public class ChatComponent1Mixin {

    @Redirect(
        method = "accept(Lnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleMessage(IFLnet/minecraft/util/FormattedCharSequence;)Z")
    )
    private boolean redirectHandleMessage(
        ChatComponent.ChatGraphicsAccess graphics, 
        int y, 
        float opacity, 
        FormattedCharSequence content,
        GuiMessage.Line line, 
        int ageInTicks, 
        float alpha
    ) {
        if (Beatifuly.isChatAnimEnabled()) {
            float[] anim = Beatifuly.getChatAnimationOffset(line);
            int newY = y + (int) anim[0];
            float newOpacity = opacity * anim[1];
            return graphics.handleMessage(newY, newOpacity, content);
        }
        return graphics.handleMessage(y, opacity, content);
    }

    @Redirect(
        method = "accept(Lnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleTag(IIIIFLnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V")
    )
    private void redirectHandleTag(
        ChatComponent.ChatGraphicsAccess graphics,
        int y,
        int yEnd,
        int x,
        int xEnd,
        float alpha,
        GuiMessageTag tag,
        GuiMessage.Line line,
        int ageInTicks,
        float alphaParam
    ) {
        if (Beatifuly.isChatAnimEnabled()) {
            float[] anim = Beatifuly.getChatAnimationOffset(line);
            graphics.handleTag(y + (int) anim[0], yEnd + (int) anim[0], x, xEnd, alpha * anim[1], tag);
        } else {
            graphics.handleTag(y, yEnd, x, xEnd, alpha, tag);
        }
    }

    @Redirect(
        method = "accept(Lnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleTagIcon(IIZLnet/minecraft/client/multiplayer/chat/GuiMessageTag;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag$Icon;)V")
    )
    private void redirectHandleTagIcon(
        ChatComponent.ChatGraphicsAccess graphics,
        int y,
        int x,
        boolean hovered,
        GuiMessageTag tag,
        GuiMessageTag.Icon icon,
        GuiMessage.Line line,
        int ageInTicks,
        float alphaParam
    ) {
        if (Beatifuly.isChatAnimEnabled()) {
            float[] anim = Beatifuly.getChatAnimationOffset(line);
            graphics.handleTagIcon(y + (int) anim[0], x, hovered, tag, icon);
        } else {
            graphics.handleTagIcon(y, x, hovered, tag, icon);
        }
    }
}
