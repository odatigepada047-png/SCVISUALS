package moscow.rockstar.utility.sounds;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LyricsFetcher {
    private static final Pattern LRC_LINE = Pattern.compile("\\[(\\d{1,2}):(\\d{1,2})(?:[.:](\\d{1,3}))?](.*)");

    public static List<TimedLyric> fetchSyncedLyrics(String artist, String title) {
        if (artist == null || title == null || artist.isBlank() || title.isBlank()) {
            return Collections.emptyList();
        }
        try {
            String encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8);
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://lrclib.net/api/get?artist_name=" + encodedArtist + "&track_name=" + encodedTitle))
                    .header("User-Agent", "RockstarClient/1.0 (https://github.com/moscow-rockstar)")
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                if (json.has("syncedLyrics") && !json.get("syncedLyrics").isJsonNull()) {
                    return parseLrc(json.get("syncedLyrics").getAsString());
                }
            }
        } catch (Exception e) {
            System.err.println("LRCLIB synced error: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private static List<TimedLyric> parseLrc(String raw) {
        List<TimedLyric> out = new ArrayList<>();
        for (String line : raw.split("\\r?\\n")) {
            Matcher m = LRC_LINE.matcher(line);
            if (!m.matches()) continue;
            int minutes = Integer.parseInt(m.group(1));
            int seconds = Integer.parseInt(m.group(2));
            int fraction = 0;
            if (m.group(3) != null) {
                String frac = m.group(3);
                int parsed = Integer.parseInt(frac);
                if (frac.length() == 1) parsed *= 100;
                else if (frac.length() == 2) parsed *= 10;
                fraction = parsed;
            }
            long ms = (long) minutes * 60_000L + (long) seconds * 1000L + fraction;
            String text = m.group(4).trim();
            out.add(new TimedLyric(ms, text));
        }
        return out;
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    public static String fetchLyrics(String artist, String title) {
        if (artist == null || title == null || artist.isBlank() || title.isBlank()) {
            System.err.println("Artist or title cannot be empty");
            return null;
        }
        
        // 1. Попробуем получить из LRCLIB
        try {
            String encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8);
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://lrclib.net/api/get?artist_name=" + encodedArtist + "&track_name=" + encodedTitle))
                    .header("User-Agent", "RockstarClient/1.0 (https://github.com/moscow-rockstar)")
                    .header("Accept", "application/json")
                    .GET()
                    .build();
                    
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                if (jsonResponse.has("plainLyrics") && !jsonResponse.get("plainLyrics").isJsonNull()) {
                    return jsonResponse.get("plainLyrics").getAsString();
                }
            }
        } catch (Exception e) {
            System.err.println("LRCLIB error: " + e.getMessage());
        }
        
        // 2. Если не нашли, пробуем спарсить Genius напрямую через генерацию URL
        System.out.println("LRCLIB не нашел текст или произошла ошибка. Пробуем запасной вариант с Genius...");
        try {
            String url = constructGeniusUrl(artist, title);
            return fetchLyricsFromPage(url);
        } catch (Exception e) {
            System.err.println("Genius fallback error: " + e.getMessage());
        }
        
        return null;
    }

    private static String constructGeniusUrl(String artist, String title) {
        String combined = artist + " " + title;
        combined = combined.toLowerCase();
        combined = combined.replaceAll(" & ", " and ");
        combined = combined.replaceAll("[^a-z0-9\\s-]", ""); // Удаляем спецсимволы кроме пробела и дефиса
        combined = combined.replaceAll("\\s+", "-"); // Заменяем пробелы на дефисы
        combined = combined.replaceAll("-+", "-"); // Удаляем дублирующиеся дефисы
        return "https://genius.com/" + combined + "-lyrics";
    }

    private static String fetchLyricsFromPage(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "en-US,en;q=0.9")
                .GET()
                .build();
                
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            System.err.println("Genius page returned status: " + response.statusCode());
            return null;
        }
        
        Pattern pattern = Pattern.compile("(<div[^>]*class=\"[^\"]*Lyrics__Container[^\"]*\"[^>]*>.*?</div>)", 32);
        Matcher matcher = pattern.matcher(response.body());
        StringBuilder lyrics = new StringBuilder();
        
        while (matcher.find()) {
            String snippet = matcher.group(1)
                    .replaceAll("<br\\s*/?>", "\n")
                    .replaceAll("<.*?>", "")
                    .replaceAll("&quot;", "\"")
                    .trim();
            if (snippet.isEmpty()) continue;
            lyrics.append(snippet).append("\n\n");
        }
        
        return lyrics.isEmpty() ? null : lyrics.toString().trim();
    }
}
