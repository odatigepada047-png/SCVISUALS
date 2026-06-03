package moscow.rockstar.mixin.minecraft.client.option;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.Ambience;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={OptionInstance.class})
public class SimpleOptionMixin<T> {
    @Shadow
    @Final
    private Component caption;
    @Shadow
    private T value;

    @Inject(method={"get"}, at={@At(value="HEAD")}, cancellable=true)
    public void getGammaValue(CallbackInfoReturnable<Double> cir) {
        if (Rockstar.getInstance().getModuleManager() == null) {
            return;
        }
        Ambience ambienceModule = Rockstar.getInstance().getModuleManager().getModule(Ambience.class);
        if (ambienceModule.isEnabled() && ambienceModule.getBright().isEnabled() && this.caption.equals((Object)Component.translatable((String)"options.gamma"))) {
            cir.setReturnValue(1337.0);
        }
    }

    @Inject(method={"set"}, at={@At(value="HEAD")}, cancellable=true)
    public void setGammaValue(T value, CallbackInfo ci) {
        if (this.caption.equals((Object)Component.translatable((String)"options.gamma"))) {
            this.value = value;
            ci.cancel();
        }
    }
}
