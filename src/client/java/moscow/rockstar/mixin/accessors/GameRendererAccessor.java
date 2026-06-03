package moscow.rockstar.mixin.accessors;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={GameRenderer.class})
public interface GameRendererAccessor {
    @Accessor(value="renderBuffers")
    public RenderBuffers buffers();

    @Accessor(value="lightmap")
    public Lightmap lightmapTextureManager();

    @Accessor("guiRenderer")
    GuiRenderer rockstar$getGuiRenderer();
}
