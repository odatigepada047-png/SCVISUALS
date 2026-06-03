package moscow.rockstar.utility.game;

import com.mojang.blaze3d.platform.NativeImage;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public final class WindowIconHelper implements IMinecraft {
    private static final int[] ICON_SIZES = {16, 32, 48, 128, 256};

    private WindowIconHelper() {
    }

    public static void applyCustomIcon() {
        if (mc == null || mc.getWindow() == null || mc.getResourceManager() == null) {
            return;
        }

        List<NativeImage> images = new ArrayList<>();
        List<ByteBuffer> pixelBuffers = new ArrayList<>();

        try {
            for (int size : ICON_SIZES) {
                Identifier id = Identifier.fromNamespaceAndPath("minecraft", "icons/icon_" + size + "x" + size + ".png");
                var resource = mc.getResourceManager().getResource(id);
                if (resource.isEmpty()) {
                    continue;
                }

                try (InputStream stream = resource.get().open()) {
                    NativeImage image = NativeImage.read(stream);
                    ByteBuffer pixels = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * 4);
                    IntBuffer intBuffer = pixels.asIntBuffer();
                    intBuffer.put(image.getPixelsABGR());
                    images.add(image);
                    pixelBuffers.add(pixels);
                }
            }

            if (images.isEmpty()) {
                return;
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                GLFWImage.Buffer buffer = GLFWImage.malloc(images.size(), stack);
                for (int i = 0; i < images.size(); i++) {
                    NativeImage image = images.get(i);
                    buffer.position(i);
                    buffer.width(image.getWidth());
                    buffer.height(image.getHeight());
                    buffer.pixels(pixelBuffers.get(i));
                }
                buffer.position(0);
                GLFW.glfwSetWindowIcon(mc.getWindow().handle(), buffer);
            }
        } catch (Exception ignored) {
        } finally {
            for (NativeImage image : images) {
                image.close();
            }
            for (ByteBuffer pixels : pixelBuffers) {
                MemoryUtil.memFree(pixels);
            }
        }
    }
}
