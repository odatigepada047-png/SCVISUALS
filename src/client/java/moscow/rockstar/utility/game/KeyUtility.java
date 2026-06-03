package moscow.rockstar.utility.game;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class KeyUtility {
    private KeyUtility() {
    }

    public static boolean isKeyPressed(int key) {
        return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(), key) == GLFW.GLFW_PRESS;
    }

    public static boolean hasControlDown() {
        return isKeyPressed(341) || isKeyPressed(345);
    }

    public static boolean isMappingPressed(KeyMapping mapping) {
        if (mapping.isUnbound()) {
            return false;
        }
        return isKeyPressed(mapping.key.getValue());
    }
}
