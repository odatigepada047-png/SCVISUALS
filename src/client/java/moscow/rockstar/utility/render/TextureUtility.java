package moscow.rockstar.utility.render;

import com.mojang.blaze3d.platform.NativeImage;
import moscow.rockstar.mixin.accessors.NativeImageAccessor;
import moscow.rockstar.Rockstar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class TextureUtility {
    private TextureUtility() {
    }

    public static DynamicTexture createDynamicTexture(NativeImage image) {
        return new DynamicTexture(() -> Rockstar.MOD_ID + "/dynamic", image);
    }

    public static void register(Identifier id, DynamicTexture texture) {
        Minecraft.getInstance().getTextureManager().register(id, texture);
    }

    public static void release(Identifier id) {
        Minecraft.getInstance().getTextureManager().release(id);
    }

    public static void setPixelArgb(NativeImage image, int x, int y, int argb) {
        ((NativeImageAccessor)(Object)image).invokeSetColor(x, y, argb);
    }

    public static int getPixelArgb(NativeImage image, int x, int y) {
        return image.getPixel(x, y);
    }
}
