package moscow.rockstar.mixin.minecraft.client.render;

import moscow.rockstar.utility.game.EntityUtility;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={DeltaTracker.Timer.class})
public class DynamicMixin {
    @Shadow
    private float deltaTicks;
    @Shadow
    private float deltaTickResidual;
    @Shadow
    private long lastMs;
    @Final
    @Shadow
    private float msPerTick;

    @Inject(at={@At(value="FIELD", target="Lnet/minecraft/client/DeltaTracker$Timer;lastMs:J", opcode=181, ordinal=0)}, method={"advanceGameTime(J)I"}, cancellable=true)
    public void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        if (EntityUtility.getTimer() == 1.0f) {
            return;
        }
        this.deltaTicks = (float)(timeMillis - this.lastMs) / this.msPerTick * EntityUtility.getTimer();
        this.lastMs = timeMillis;
        this.deltaTickResidual += this.deltaTicks;
        int i = (int)this.deltaTickResidual;
        this.deltaTickResidual -= (float)i;
        cir.setReturnValue(i);
    }
}
