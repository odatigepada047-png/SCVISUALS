package moscow.rockstar.framework.shader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Loads Rockstar's {@code data.json} shader descriptors from the active resource pack.
 *
 * <p>Format (legacy, kept for parity with the 1.21.4 RockReady project):
 * <pre>{@code
 * {
 *     "vertex": "rockstar:core/<path>",
 *     "fragment": "rockstar:core/<path>",
 *     "samplers": [ { "name": "Sampler0" }, ... ],
 *     "uniforms": [ { "name": "X", "type": "float|int", "count": N, "values": [...] }, ... ]
 * }
 * }</pre>
 */
public final class ShaderDataLoader {
    private ShaderDataLoader() {
    }

    public static ShaderDataDescriptor load(Identifier dataId) {
        Identifier resourcePath = Identifier.fromNamespaceAndPath(dataId.getNamespace(), "shaders/" + dataId.getPath() + ".json");
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        Resource resource = resources.getResource(resourcePath).orElseThrow(() ->
                new IllegalStateException("Missing shader data.json: " + resourcePath));

        JsonObject root;
        try (var reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            root = parsed.getAsJsonObject();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read shader descriptor " + resourcePath, e);
        }

        Identifier vertex = parseIdentifier(root.get("vertex").getAsString());
        Identifier fragment = parseIdentifier(root.get("fragment").getAsString());

        List<String> samplers = new ArrayList<>();
        if (root.has("samplers")) {
            for (JsonElement el : root.getAsJsonArray("samplers")) {
                samplers.add(el.getAsJsonObject().get("name").getAsString());
            }
        }

        List<ShaderDataDescriptor.UniformEntry> uniforms = new ArrayList<>();
        if (root.has("uniforms")) {
            for (JsonElement el : root.getAsJsonArray("uniforms")) {
                JsonObject u = el.getAsJsonObject();
                String name = u.get("name").getAsString();
                String type = u.get("type").getAsString();
                int count = u.get("count").getAsInt();
                float[] defaults = readDefaults(u, count);
                uniforms.add(new ShaderDataDescriptor.UniformEntry(name, type, count, defaults));
            }
        }

        return new ShaderDataDescriptor(vertex, fragment, samplers, uniforms);
    }

    private static Identifier parseIdentifier(String raw) {
        int colon = raw.indexOf(':');
        if (colon < 0) {
            return Identifier.withDefaultNamespace(raw);
        }
        return Identifier.fromNamespaceAndPath(raw.substring(0, colon), raw.substring(colon + 1));
    }

    private static float[] readDefaults(JsonObject obj, int count) {
        float[] result = new float[count];
        if (!obj.has("values")) {
            return result;
        }
        JsonArray values = obj.getAsJsonArray("values");
        for (int i = 0; i < count && i < values.size(); i++) {
            result[i] = values.get(i).getAsFloat();
        }
        return result;
    }
}
