package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.Rockstar;
import moscow.rockstar.protection.client.MinecraftClientMixinProtection;
import moscow.rockstar.systems.event.impl.game.GameTickEvent;
import moscow.rockstar.utility.game.WindowIconHelper;
import moscow.rockstar.utility.render.penis.PenisAtlas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public class MinecraftClientMixin {

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    public void tick(CallbackInfo ci) {
        Rockstar.getInstance().getEventManager().triggerEvent(new GameTickEvent());
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void endInitialize(GameConfig args, CallbackInfo ci) {
        WindowIconHelper.applyCustomIcon();
        MinecraftClientMixinProtection.init();
        PenisAtlas atlas = PenisAtlas.getOrCreateAtlasFor(16, 16);
        atlas.registerAnimationFromPenisFile(Rockstar.id("penises/combat.penis"));
        atlas.registerAnimationFromPenisFile(Rockstar.id("penises/movement.penis"));
        atlas.registerAnimationFromPenisFile(Rockstar.id("penises/visuals.penis"));
        atlas.registerAnimationFromPenisFile(Rockstar.id("penises/player.penis"));
        atlas.registerAnimationFromPenisFile(Rockstar.id("penises/other.penis"));
        atlas.registerAnimationFromPenisFile(Rockstar.id("penises/search.penis"));
        atlas.buildAtlas();
        PenisAtlas atlas12 = PenisAtlas.getOrCreateAtlasFor(12, 12);
        atlas12.registerAnimationFromPenisFile(Rockstar.id("penises/check_enable.penis"));
        atlas12.registerAnimationFromPenisFile(Rockstar.id("penises/check_disable.penis"));
        atlas12.buildAtlas();
    }

    @Inject(method={"stop"}, at={@At(value="HEAD")})
    public void shutdownClient(CallbackInfo ci) {
        MinecraftClientMixinProtection.shutdown();
    }

    @Inject(method={"updateTitle"}, at={@At(value="HEAD")}, cancellable=true)
    private void changeWindowTitle(CallbackInfo ci) {
        MinecraftClientMixinProtection.updateTitle((Minecraft)(Object)this, ci);
    }
}
