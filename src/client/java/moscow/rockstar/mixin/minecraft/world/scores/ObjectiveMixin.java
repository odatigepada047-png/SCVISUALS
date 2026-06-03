package moscow.rockstar.mixin.minecraft.world.scores;

import moscow.rockstar.systems.modules.modules.other.HiderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Objective.class)
public class ObjectiveMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Component> cir) {
        Component original = cir.getReturnValue();
        Component modified = HiderUtils.patchComponent(original);
        if (modified != original) {
            cir.setReturnValue(modified);
        }
    }
}
