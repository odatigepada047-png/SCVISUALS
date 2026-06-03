package moscow.rockstar.protection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import moscow.rockstar.Rockstar;

public final class ClientSession {
    public static final Path SESSION_FILE = Path.of("C:\\SCVisuals\\client-session.json");

    private ClientSession() {
    }

    public static Optional<String> readLogin() {
        try {
            if (!Files.isRegularFile(SESSION_FILE)) {
                return Optional.empty();
            }
            String json = Files.readString(SESSION_FILE, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("login")) {
                return Optional.empty();
            }
            String login = root.get("login").getAsString();
            return login == null || login.isBlank() ? Optional.empty() : Optional.of(login.trim());
        } catch (Exception e) {
            Rockstar.LOGGER.warn("Could not read client session: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public static String readHwidFallback() {
        try {
            if (!Files.isRegularFile(SESSION_FILE)) {
                return "UNKNOWN";
            }
            String json = Files.readString(SESSION_FILE, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("hwid")) {
                String hwid = root.get("hwid").getAsString();
                if (hwid != null && !hwid.isBlank()) {
                    return hwid.trim();
                }
            }
        } catch (Exception ignored) {
        }
        return "UNKNOWN";
    }

    public static String buildDisplayTitle(String login) {
        if (login == null || login.isBlank()) {
            return Rockstar.NAME + " (Beta)";
        }
        return Rockstar.NAME + " (Beta) For " + login;
    }
}
