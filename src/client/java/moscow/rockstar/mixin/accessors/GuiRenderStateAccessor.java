package moscow.rockstar.mixin.accessors;

import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Set;

@Mixin(GuiRenderState.class)
public interface GuiRenderStateAccessor {
    @Accessor("itemModelIdentities")
    Set<Object> rockstar$getItemModelIdentities();
}
