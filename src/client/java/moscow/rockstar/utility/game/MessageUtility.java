/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.Component
 */
package moscow.rockstar.utility.game;

import lombok.Generated;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;

public final class MessageUtility
implements IMinecraft {
    private static final Component PREFIX = Component.literal("[%s]".formatted("Sound Cloud Visuals")).withStyle(Style.EMPTY.withColor(new ColorRGBA(140.0f, 80.0f, 255.0f).getRGB()));

    public static void overlay(LogLevel logLevel, Component message) {
        MessageUtility.log(logLevel, message, true);
    }

    public static void info(Component message) {
        if (MessageUtility.mc.player == null) {
            return;
        }
        MessageUtility.log(LogLevel.INFO, message, false);
    }

    public static void warn(Component message) {
        MessageUtility.log(LogLevel.WARN, message, false);
    }

    public static void error(Component message) {
        MessageUtility.log(LogLevel.ERROR, message, false);
    }

    private static void log(LogLevel level, Component message, boolean overlay) {
        if (MessageUtility.mc.player == null) {
            return;
        }
        Component styledMessage = message.copy().withStyle(Style.EMPTY.withColor(level.getColor().getRGB()));
        Component fullMessage = PREFIX.copy().append(" ").append(styledMessage);
        if (overlay) {
            MessageUtility.mc.player.sendOverlayMessage(fullMessage);
        } else {
            MessageUtility.mc.player.sendSystemMessage(fullMessage);
        }
    }

    @Generated
    private MessageUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static enum LogLevel {
        WARN("Warning", new ColorRGBA(247.0f, 206.0f, 59.0f)),
        ERROR("Error", new ColorRGBA(242.0f, 79.0f, 68.0f)),
        INFO("Info", new ColorRGBA(87.0f, 126.0f, 255.0f));

        private final String level;
        private final ColorRGBA color;

        @Generated
        public String getLevel() {
            return this.level;
        }

        @Generated
        public ColorRGBA getColor() {
            return this.color;
        }

        @Generated
        private LogLevel(String level, ColorRGBA color) {
            this.level = level;
            this.color = color;
        }
    }
}
