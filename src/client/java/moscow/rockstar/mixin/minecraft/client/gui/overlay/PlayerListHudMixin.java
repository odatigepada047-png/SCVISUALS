package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerTabOverlay.class})
public class PlayerListHudMixin {
    @Unique
    private final Animation rockstar$anim = new Animation(250L, 0.0f, Easing.BAKEK);
    @Unique
    private long rockstar$lastRenderTime = 0L;

    @Inject(method="extractRenderState", at=@At("HEAD"))
    private void onRenderHead(GuiGraphicsExtractor context, int scaledWindowWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        long time = System.currentTimeMillis();
        if (time - this.rockstar$lastRenderTime > 100L) {
            this.rockstar$anim.setValue(0.0f);
        }
        this.rockstar$lastRenderTime = time;

        this.rockstar$anim.update(1.0f);
        context.pose().pushMatrix();
        
        float value = this.rockstar$anim.getValue();
        boolean islandActive = Rockstar.getInstance().getHud().getIsland().isShowing();
        float endY = islandActive ? 45.0f : 0.0f;
        float startY = endY - 150.0f;
        float currentY = startY + (endY - startY) * value;
        
        context.pose().translate(0.0f, currentY);
    }

    @Inject(method="extractRenderState", at=@At("RETURN"))
    private void onRenderTail(GuiGraphicsExtractor context, int scaledWindowWidth, Scoreboard scoreboard, Objective objective, CallbackInfo ci) {
        context.pose().popMatrix();
    }

    @Inject(method="getNameForDisplay", at=@At("RETURN"), cancellable=true)
    private void onGetPlayerName(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
        if (net.minecraft.client.Minecraft.getInstance().player != null && 
            entry.getProfile().id().equals(net.minecraft.client.Minecraft.getInstance().player.getUUID())) {
            
            MutableComponent name = cir.getReturnValue().copy();
            int color = moscow.rockstar.utility.colors.Colors.getAccentColor().getRGB();
            name.setStyle(name.getStyle().withColor(color));
            cir.setReturnValue(name);
        }
    }
}
