package moscow.rockstar.utility.sounds;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

public class MediaSeeker {
    private static boolean loaded = false;

    static {
        try {
            System.loadLibrary("MediaControl");
            loaded = true;
        } catch (Throwable ignored) {}

        if (!loaded) {
            try {
                Path tempDir = Files.createTempDirectory("mediacontrol-", new FileAttribute[0]);
                Path dllFile = tempDir.resolve("MediaControl.dll");
                try (InputStream inputStream = MediaSeeker.class.getResourceAsStream("/mediaplayerinfo/natives/win/MediaControl.dll")) {
                    if (inputStream != null) {
                        Files.write(dllFile, inputStream.readAllBytes(), new OpenOption[0]);
                        System.load(dllFile.toAbsolutePath().toString());
                        loaded = true;
                    }
                }
                try {
                    Files.deleteIfExists(dllFile);
                    Files.deleteIfExists(tempDir);
                } catch (IOException ignored) {
                    dllFile.toFile().deleteOnExit();
                    tempDir.toFile().deleteOnExit();
                }
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Set the playback position of the current media session.
     * @param seconds position in seconds
     */
    public static void seekTo(long seconds) {
        if (!loaded) return;
        try {
            setPosition(seconds);
        } catch (Throwable ignored) {}
    }

    public static boolean isAvailable() {
        return loaded;
    }

    private static native void setPosition(long seconds);
}
