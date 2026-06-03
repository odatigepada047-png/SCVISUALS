package moscow.rockstar.mixin.minecraft.client.network;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.player.Freelook;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityFreelookMixin implements IMinecraft {

    @Inject(method = "tick", at = @At("HEAD"))
    private void lockBodyRotationDuringFreelook(CallbackInfo ci) {
        Freelook freelook = Rockstar.getInstance().getModuleManager().getModule(Freelook.class);
        if (!freelook.isFreelookActive() || mc.player == null) {
            return;
        }

        float yaw = freelook.getSavedYaw();
        float pitch = freelook.getSavedPitch();
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
        mc.player.yRotO = yaw;
        mc.player.xRotO = pitch;
    }
}
