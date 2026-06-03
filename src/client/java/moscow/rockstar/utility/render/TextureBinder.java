package moscow.rockstar.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

/**
 * Binds textures for custom {@link MeshDrawHelper} draws (replaces removed {@code RenderSystem#setShaderTexture}).
 */
public final class TextureBinder implements IMinecraft {
    public static GpuBinding lastBinding;

    private TextureBinder() {
    }

    public static void bind(Identifier id) {
        AbstractTexture texture = mc.getTextureManager().getTexture(id);
        bindTexture(texture);
    }

    public static void bindLinear(Identifier id) {
        AbstractTexture texture = mc.getTextureManager().getTexture(id);
        if (texture != null) {
            lastBinding = new GpuBinding(
                    texture.getTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(com.mojang.blaze3d.textures.FilterMode.LINEAR)
            );
        }
    }


    public static void bindTexture(AbstractTexture texture) {
        if (texture != null) {
            lastBinding = new GpuBinding(texture.getTextureView(), texture.getSampler());
        }
    }

    public static void bindMainColor() {
        lastBinding = new GpuBinding(
                mc.getMainRenderTarget().getColorTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(com.mojang.blaze3d.textures.FilterMode.LINEAR)
        );
    }

    public static void bindBlurResult() {
        UiOverlayRenderer.blurRequestedThisFrame = true;
        var target = moscow.rockstar.framework.shader.impl.BlurProgram.getBlurTarget();
        if (target != null) {
            lastBinding = new GpuBinding(
                    target.getColorTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(com.mojang.blaze3d.textures.FilterMode.LINEAR)
            );
        }
    }

    public static void bindCustomTarget(CustomRenderTarget target) {
        if (target != null) {
            lastBinding = new GpuBinding(
                    target.getColorTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(com.mojang.blaze3d.textures.FilterMode.LINEAR)
            );
        }
    }

    public static void unbind() {
        lastBinding = null;
    }

    public record GpuBinding(
            com.mojang.blaze3d.textures.GpuTextureView view,
            com.mojang.blaze3d.textures.GpuSampler sampler
    ) {
    }
}
