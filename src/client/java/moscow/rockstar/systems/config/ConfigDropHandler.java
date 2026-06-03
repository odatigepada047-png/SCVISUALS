/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.chat.Component
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWDropCallback
 *  org.lwjgl.glfw.GLFWDropCallbackI
 */
package moscow.rockstar.systems.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.config.ConfigFile;
import moscow.rockstar.systems.config.ConfigManager;
import moscow.rockstar.systems.file.FileManager;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.glfw.GLFWDropCallbackI;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.Initialization;

public final class ConfigDropHandler
implements IMinecraft {
    private static boolean initialized;

    @Compile
    @Initialization
    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        long handle = mc.getWindow().handle();
        GLFWDropCallbackI[] previous = new GLFWDropCallbackI[1];
        GLFWDropCallbackI callback = (window, count, names) -> {
            if (previous[0] != null) {
                previous[0].invoke(window, count, names);
            }
            for (int i = 0; i < count; ++i) {
                String path = GLFWDropCallback.getName((long)names, (int)i);
                ConfigDropHandler.handleDrop(path);
            }
        };
        previous[0] = GLFW.glfwSetDropCallback((long)handle, (GLFWDropCallbackI)callback);
    }

    private static void handleDrop(String path) {
        try {
            File src = new File(path);
            if (!src.isFile()) {
                return;
            }
            if (!src.getName().endsWith("." + ConfigManager.CONFIG_EXTENSION) && !src.getName().endsWith("." + ConfigManager.LEGACY_CONFIG_EXTENSION)) {
                return;
            }
            File destDir = new File(FileManager.DIRECTORY, "configs");
            if (!destDir.exists() && !destDir.mkdirs()) {
                Rockstar.LOGGER.error("Failed to create directory {}", (Object)destDir.getAbsolutePath());
                return;
            }
            String normalizedName = ConfigManager.normalizeConfigName(src.getName());
            File dest = new File(destDir, normalizedName + "." + ConfigManager.CONFIG_EXTENSION);
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            String name = normalizedName;
            ConfigManager manager = Rockstar.getInstance().getConfigManager();
            manager.refresh();
            ConfigFile cfg = manager.getConfig(name);
            if (cfg == null) {
                cfg = new ConfigFile(name);
                manager.getConfigFiles().add(cfg);
            }
            cfg.load();
            MessageUtility.info(Component.literal((String)("\u041a\u043e\u043d\u0444\u0438\u0433 " + name + " \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d")));
            Rockstar.getInstance().getNotificationManager().addNotification(NotificationType.SUCCESS, Component.translatable((String)"configs.loaded").getString());
        }
        catch (Exception e) {
            Rockstar.LOGGER.error("Failed to load dropped config {}", (Object)path, (Object)e);
        }
    }

    @Generated
    private ConfigDropHandler() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
