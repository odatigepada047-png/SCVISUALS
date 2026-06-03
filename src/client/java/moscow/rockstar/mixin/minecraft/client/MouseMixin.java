package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.systems.modules.modules.player.Freelook;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.util.SmoothDouble;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MouseHandler.class})
public class MouseMixin
implements IMinecraft {
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;
    @Shadow
    private SmoothDouble smoothTurnX;
    @Shadow
    private SmoothDouble smoothTurnY;

    @Inject(method={"turnPlayer"}, at={@At(value="HEAD")}, cancellable=true)
    private void onTurnPlayer(double timeDelta, CallbackInfo ci) {
        Freelook freelook = Rockstar.getInstance().getModuleManager().getModule(Freelook.class);
        if (freelook.isFreelookActive()) {
            freelook.updateRotation(this.accumulatedDX, this.accumulatedDY, timeDelta, this.smoothTurnX, this.smoothTurnY);
            ci.cancel();
        }
    }

    @Inject(method={"handleAccumulatedMovement"}, at={@At(value="RETURN")})
    private void tick(CallbackInfo ci) {
        if (CursorUtility.getCurrentType() != CursorUtility.getPrev()) {
            GLFW.glfwSetCursor((long)mc.getWindow().handle(), (long)CursorUtility.getCurrentType().getCode());
        }
        CursorUtility.setPrev(CursorUtility.getCurrentType());
        CursorUtility.set(CursorType.DEFAULT);
    }

    @Inject(method = "onButton", at = @At("HEAD"))
    private void onButton(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        Rockstar.getInstance().getEventManager().triggerEvent(new MouseEvent(info.button(), action));
    }
}
