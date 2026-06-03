package moscow.rockstar.protection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import moscow.rockstar.Rockstar;

public final class HwidNative {
    private static final String DLL_NAME = "scvis_native.dll";
    private static boolean loaded;

    private HwidNative() {
    }

    static {
        loaded = tryLoad();
    }

    private static boolean tryLoad() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("windows")) {
            return false;
        }

        Path external = Path.of("C:\\SCVisuals\\native", DLL_NAME);
        if (Files.isRegularFile(external)) {
            try {
                System.load(external.toAbsolutePath().toString());
                return true;
            } catch (UnsatisfiedLinkError ignored) {
                Rockstar.LOGGER.warn("Failed to load external native DLL: {}", external);
            }
        }

        try {
            Path tempDir = Files.createTempDirectory("scvis-native-");
            Path tempDll = tempDir.resolve(DLL_NAME);
            try (InputStream in = HwidNative.class.getResourceAsStream("/native/win_x64/" + DLL_NAME)) {
                if (in == null) {
                    return false;
                }
                Files.copy(in, tempDll, StandardCopyOption.REPLACE_EXISTING);
            }
            System.load(tempDll.toAbsolutePath().toString());
            tempDll.toFile().deleteOnExit();
            tempDir.toFile().deleteOnExit();
            return true;
        } catch (IOException | UnsatisfiedLinkError e) {
            Rockstar.LOGGER.warn("Native DLL not loaded: {}", e.getMessage());
            return false;
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static String getHwidHash() {
        if (loaded) {
            try {
                return nativeGetHwidHash();
            } catch (UnsatisfiedLinkError ignored) {
            }
        }
        return ClientSession.readHwidFallback();
    }

    public static String getSessionLogin() {
        if (loaded) {
            try {
                String login = nativeGetSessionLogin();
                if (login != null && !login.isBlank()) {
                    return login.trim();
                }
            } catch (UnsatisfiedLinkError ignored) {
            }
        }
        return ClientSession.readLogin().orElse("");
    }

    public static String getDisplayTitle() {
        if (loaded) {
            try {
                String title = nativeGetDisplayTitle();
                if (title != null && !title.isBlank()) {
                    return title.trim();
                }
            } catch (UnsatisfiedLinkError ignored) {
            }
        }
        return ClientSession.buildDisplayTitle(ClientSession.readLogin().orElse(""));
    }

    private static native String nativeGetHwidHash();

    private static native String nativeGetSessionLogin();

    private static native String nativeGetDisplayTitle();
}
