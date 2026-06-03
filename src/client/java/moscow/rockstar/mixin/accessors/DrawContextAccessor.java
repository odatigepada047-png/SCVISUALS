package moscow.rockstar.mixin.accessors;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphicsExtractor.class)
public interface DrawContextAccessor {
    @Accessor("guiRenderState")
    net.minecraft.client.renderer.state.gui.GuiRenderState getGuiRenderState();

    @Accessor("minecraft")
    net.minecraft.client.Minecraft getClient();

    @Accessor("mouseX")
    int getMouseX();

    @Accessor("mouseY")
    int getMouseY();

    @Accessor("pose")
    org.joml.Matrix3x2fStack getMatrices();
}
