package moscow.rockstar.mixin.accessors;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSetup.class)
public interface RenderSetupAccessor {
    @Accessor("textures")
    Map<String, ?> getTextures();
}
