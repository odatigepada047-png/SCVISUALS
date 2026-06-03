package moscow.rockstar.mixin.accessors;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={NativeImage.class})
public interface NativeImageAccessor {
    @Invoker(value="setPixel")
    void invokeSetColor(int x, int y, int color);
}
