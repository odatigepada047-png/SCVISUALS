package moscow.rockstar.mixin.minecraft.client.gui.screen.multiplayer;

import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerReconfigScreen.class)
public class ServerReconfigScreenMixin extends Screen {

    @Shadow @Final private Connection connection;
    @Shadow private Button disconnectButton;

    protected ServerReconfigScreenMixin(Component title) {
        super(title);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        if (this.connection != null) {
            this.connection.disconnect(Component.translatable("multiplayer.status.and.button.disconnect"));
        }
        super.onClose();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (this.disconnectButton != null) {
            this.disconnectButton.active = true;
        }
    }
}
