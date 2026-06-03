package moscow.rockstar.mixin.accessors;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiRenderer.class)
public interface GuiRendererAccessor {
    @Accessor("guiProjection")
    Projection rockstar$getGuiProjection();

    @Accessor("guiProjectionMatrixBuffer")
    ProjectionMatrixBuffer rockstar$getGuiProjectionMatrixBuffer();

    @Accessor("itemAtlas")
    net.minecraft.client.gui.render.GuiItemAtlas rockstar$getItemAtlas();
}
