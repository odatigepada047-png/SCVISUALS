package moscow.rockstar.mixin.minecraft.world;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.other.Optimizer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractBlockStateMixin {

    @Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
    private void onGetRenderShape(CallbackInfoReturnable<RenderShape> cir) {
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase) (Object) this;
        BlockState blockState = (BlockState) state;
        if (Rockstar.getInstance() != null && Rockstar.getInstance().getModuleManager() != null) {
            Optimizer optimizer = Rockstar.getInstance().getModuleManager().getModule(Optimizer.class);
            if (optimizer != null && optimizer.shouldHide(blockState)) {
                cir.setReturnValue(RenderShape.INVISIBLE);
            }
        }
    }
}
