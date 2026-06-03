package moscow.rockstar.systems.party;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.network.chat.Component;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PartyManager implements IMinecraft {

    private static class Xor2core {
        private static String getP1() {
            return new String(new char[] { (char) (123 ^ 0x4A), (char) (126 ^ 0x4A), (char) (126 ^ 0x4A) });
        }

        private static String getP2() {
            return new String(new char[] { (char) (100 ^ 0x4A), (char) (121 ^ 0x4A), (char) (123 ^ 0x4A) });
        }

        private static String getP3() {
            return new String(
                    new char[] { (char) (100 ^ 0x4A), (char) (123 ^ 0x4A), (char) (121 ^ 0x4A), (char) (124 ^ 0x4A) });
        }

        private static String getP4() {
            return new String(
                    new char[] { (char) (100 ^ 0x4A), (char) (123 ^ 0x4A), (char) (123 ^ 0x4A), (char) (127 ^ 0x4A) });
        }

        public static String resolve() {
            return getP1() + getP2() + getP3() + getP4();
        }
    }

    private static final String SERVER_IP = Xor2core.resolve();
    private static final int SERVER_PORT = 8080;
    private static final Gson GSON = new Gson();

    private static PartyManager instance;

    private Socket socket;
    private PrintWriter writer;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private String playerName;
    private String currentPartyCode;
    private boolean connected = false;
    private boolean connecting = false;
    private Runnable pendingAction = null;
    private long lastCreateTime = 0;
    private long lastJoinTime = 0;
    private static final long CREATE_COOLDOWN_MS = 5 * 60 * 1000;
    private static final long JOIN_COOLDOWN_MS = 2 * 60 * 1000;

    public final ConcurrentHashMap<String, MemberInfo> members = new ConcurrentHashMap<>();
    
    public static class Waypoint {
        public String name;
        public double x, y, z;
        public String owner;
        public long timestamp;
        
        public Waypoint(String name, double x, double y, double z, String owner) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.owner = owner;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    public final List<Waypoint> waypoints = new ArrayList<>();

    private PartyManager() {
    }

    public static PartyManager getInstance() {
        if (instance == null) {
            instance = new PartyManager();
        }
        return instance;
    }

    public void connect(String name, Runnable afterConnect) {
        if (connecting || connected)
            return;
        this.playerName = name;
        this.connecting = true;
        this.pendingAction = afterConnect;
        executor.submit(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                connected = true;
                connecting = false;
                chat("[Party] §aПодключено к серверу");
                if (pendingAction != null) {
                    pendingAction.run();
                    pendingAction = null;
                }
                startReading();
            } catch (IOException e) {
                connecting = false;
                chat("[Party] §cОшибка подключения: " + e.getMessage());
            }
        });
    }

    private void startReading() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                handleIncoming(line);
            }
        } catch (IOException e) {
            if (connected)
                chat("[Party] §cСоединение разорвано");
        } finally {
            connected = false;
            currentPartyCode = null;
            members.clear();
            waypoints.clear();
        }
    }

    private void handleIncoming(String raw) {
        try {
            JsonObject json = JsonParser.parseString(raw).getAsJsonObject();
            String type = json.get("type").getAsString();

            switch (type) {
                case "created" -> {
                    currentPartyCode = json.get("code").getAsString();
                    chatWithCopy("[Party] §aГруппа создана! Код: ", currentPartyCode, "");
                    chat("[Party] §7Скажи этот код тиммейту для вступления");
                    var partySystem = moscow.rockstar.Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.other.PartySystem.class);
                    if (partySystem != null && !partySystem.isEnabled()) {
                        partySystem.setEnabled(true, true);
                    }
                }
                case "joined" -> {
                    currentPartyCode = json.get("code").getAsString();
                    boolean silent = json.has("silent") && json.get("silent").getAsBoolean();
                    if (!silent)
                        chatWithCopy("[Party] §aВы вошли в группу: ", currentPartyCode, "");
                    var partySystem = moscow.rockstar.Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.other.PartySystem.class);
                    if (partySystem != null && !partySystem.isEnabled()) {
                        partySystem.setEnabled(true, true);
                    }
                }
                case "peer_update" -> {
                    String name = json.get("name").getAsString();
                    double x = 0, y = 0, z = 0;
                    float hp = 0;
                    String server = "?";
                    int anarchy = -1;

                    if (json.has("pos") && !json.get("pos").isJsonNull()) {
                        JsonObject pos = json.get("pos").getAsJsonObject();
                        x = pos.get("x").getAsDouble();
                        y = pos.get("y").getAsDouble();
                        z = pos.get("z").getAsDouble();
                    }
                    if (json.has("hp") && !json.get("hp").isJsonNull())
                        hp = json.get("hp").getAsFloat();
                    if (json.has("server") && !json.get("server").isJsonNull())
                        server = json.get("server").getAsString();
                    if (json.has("anarchy") && !json.get("anarchy").isJsonNull())
                        anarchy = json.get("anarchy").getAsInt();

                    MemberInfo existing = members.get(name);
                    if (existing == null) {
                        members.put(name, new MemberInfo(x, y, z, hp, server, anarchy));
                    } else {
                        int oldAnarchy = existing.anarchy;
                        existing.update(x, y, z, hp, server, anarchy);
                        if (oldAnarchy == -1 && anarchy != -1) {
                            chat("§e" + name + " §fперешёл на анку §c" + anarchy);
                        }
                    }
                }
                case "peer_joined" -> {
                    String name = json.get("name").getAsString();
                    chat("[Party] §e" + name + " §fвошёл в группу");
                }
                case "peer_left" -> {
                    String name = json.get("name").getAsString();
                    members.remove(name);
                    chat("[Party] §e" + name + " §fпокинул группу");
                }
                case "chat" -> {
                    String name = json.get("name").getAsString();
                    String message = json.get("message").getAsString();
                    chat("[Party] §b" + name + "§f: " + message);
                }
                case "waypoint" -> {
                    String name = json.get("name").getAsString();
                    double x = json.get("x").getAsDouble();
                    double y = json.get("y").getAsDouble();
                    double z = json.get("z").getAsDouble();
                    String wpName = json.get("wpName").getAsString();
                    
                    moscow.rockstar.systems.modules.modules.visuals.Waypoints waypointsModule = moscow.rockstar.Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.visuals.Waypoints.class);
                    if (waypointsModule != null) {
                        waypointsModule.addFromParty(wpName, x, y, z, name);
                    }
                }
                case "delete_waypoint" -> {
                    String wpName = json.get("wpName").getAsString();
                    moscow.rockstar.systems.modules.modules.visuals.Waypoints waypointsModule = moscow.rockstar.Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.visuals.Waypoints.class);
                    if (waypointsModule != null) {
                        waypointsModule.deleteWaypoint(wpName);
                    }
                }
                case "error" -> {
                    String msg = json.get("message").getAsString();
                    chat("[Party] §cОшибка: " + msg);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void createParty() {
        String name = getPlayerName();
        if (name == null)
            return;
        long now = System.currentTimeMillis();
        long timeSinceCreate = now - lastCreateTime;
        if (timeSinceCreate < CREATE_COOLDOWN_MS) {
            long remainingSec = (CREATE_COOLDOWN_MS - timeSinceCreate) / 1000;
            long minutes = remainingSec / 60;
            long seconds = remainingSec % 60;
            chat(String.format("[Party] §cПодожди ещё %d:%02d перед созданием новой группы", minutes, seconds));
            return;
        }

        lastCreateTime = now;
        if (connected)
            send(Map.of("type", "create", "name", name, "version", "1.21.4"));
        else
            connect(name, () -> send(Map.of("type", "create", "name", name, "version", "1.21.4")));
    }

    public void joinParty(String code) {
        String name = getPlayerName();
        if (name == null)
            return;

        long now = System.currentTimeMillis();
        long timeSinceJoin = now - lastJoinTime;
        if (timeSinceJoin < JOIN_COOLDOWN_MS) {
            long remainingSec = (JOIN_COOLDOWN_MS - timeSinceJoin) / 1000;
            long minutes = remainingSec / 60;
            long seconds = remainingSec % 60;
            chat(String.format("[Party] §cПодожди ещё %d:%02d перед вступлением в другую группу", minutes, seconds));
            return;
        }

        lastJoinTime = now;
        if (connected)
            send(Map.of("type", "join", "name", name, "code", code, "version", "1.21.4"));
        else
            connect(name, () -> send(Map.of("type", "join", "name", name, "code", code, "version", "1.21.4")));
    }

    public void leaveParty() {
        if (currentPartyCode == null) {
            chat("[Party] §cВы не в группе");
            return;
        }
        send(Map.of("type", "leave", "name", getPlayerName() != null ? getPlayerName() : "Unknown"));
        currentPartyCode = null;
        members.clear();
        waypoints.clear();
        connected = false;
        try {
            if (socket != null)
                socket.close();
        } catch (IOException ignored) {
        }
        chat("[Party] §7Вы покинули группу");
        var partySystem = moscow.rockstar.Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.other.PartySystem.class);
        if (partySystem != null && partySystem.isEnabled()) {
            partySystem.setEnabled(false, true);
        }
    }

    public void sendPositionUpdate() {
        if (writer == null || playerName == null || mc.player == null)
            return;

        String serverName = "singleplayer";
        if (mc.getCurrentServer() != null) {
            ServerData si = mc.getCurrentServer();
            serverName = si.ip != null ? si.ip : (si.name != null ? si.name : "?");
        }

        int anarchy = getAnarchyFromScoreboard();

        String json = String.format(
                java.util.Locale.US,
                "{\"type\":\"update\",\"name\":\"%s\",\"pos\":{\"x\":%.2f,\"y\":%.2f,\"z\":%.2f},\"hp\":%.1f,\"server\":\"%s\",\"anarchy\":%d}",
                playerName, mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getHealth(),
                serverName.replace("\"", ""), anarchy);
        writer.println(json);
    }

    public void sendChatMessage(String msg) {
        if (connected && writer != null) {
            send(Map.of("type", "chat", "name", getPlayerName(), "message", msg));
        }
    }

    public void sendWaypoint(String wpName, double x, double y, double z, boolean isEntity) {
        if (connected) {
            send(Map.of(
                "type", "waypoint",
                "name", getPlayerName(),
                "wpName", wpName,
                "x", String.valueOf(x),
                "y", String.valueOf(y),
                "z", String.valueOf(z),
                "isEntity", String.valueOf(isEntity)
            ));
        }
    }

    public int getAnarchyFromScoreboard() {
        if (mc.level == null)
            return -1;
        try {
            Scoreboard scoreboard = mc.level.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
            if (objective == null)
                return -1;
            String title = objective.getDisplayName().getString();
            title = title.replaceAll("§.", "");
            
            // Сначала ищем по формату HolyWorld "Лайт #"
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)лайт\\s*#\\s*(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(title);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            
            // Если не нашли, ищем по дефолтному формату с дефисом (FunTime)
            if (title.contains("-")) {
                String[] parts = title.split("-");
                if (parts.length > 1) {
                    try {
                        return Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    public void showInfo() {
        if (currentPartyCode == null) {
            chat("[Party] §cВы не в группе");
            return;
        }

        String myServer = "singleplayer";
        if (mc.getCurrentServer() != null) {
            ServerData si = mc.getCurrentServer();
            myServer = si.ip != null ? si.ip : (si.name != null ? si.name : "?");
        }
        int myAnarchy = getAnarchyFromScoreboard();
        String myAnarchyStr = myAnarchy != -1 ? " §c| Anc-" + myAnarchy : "";
        chat("§6[Party] §fТы: §7" + myServer + myAnarchyStr);

        if (members.isEmpty()) {
            chat("§7Нет данных о тиммейтах...");
            return;
        }

        for (Map.Entry<String, MemberInfo> entry : members.entrySet()) {
            String name = entry.getKey();
            MemberInfo info = entry.getValue();

            String hpColor = info.hp > 15 ? "§a" : info.hp > 8 ? "§e" : "§c";
            String serverStr = info.server != null ? info.server : "?";
            String anarchyStr = info.anarchy != -1 ? " §c| Anc-" + info.anarchy : "";

            chat(String.format("§e%s §f| HP: %s%.0f§7/20 §f| §7%s%s",
                    name, hpColor, info.hp, serverStr, anarchyStr));
        }
    }

    private void send(Map<String, String> data) {
        if (writer != null)
            writer.println(GSON.toJson(data));
    }

    private String getPlayerName() {
        return mc.player != null ? mc.player.getName().getString() : null;
    }

    private void chat(String message) {
        if (mc.player != null)
            mc.execute(() -> mc.player.sendSystemMessage(Component.literal(message)));
    }
    private void chatWithCopy(String prefix, String codeToCopy, String suffix) {
        chat(prefix + codeToCopy + (suffix == null ? "" : suffix));
    }

    public String getCurrentPartyCode() {
        return currentPartyCode;
    }

    public boolean isConnected() {
        return connected;
    }
}

