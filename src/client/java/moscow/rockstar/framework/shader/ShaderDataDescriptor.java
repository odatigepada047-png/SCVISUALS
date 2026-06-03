package moscow.rockstar.framework.shader;

import java.util.List;
import net.minecraft.resources.Identifier;

/**
 * Parsed {@code data.json} descriptor for a Rockstar shader program.
 */
public record ShaderDataDescriptor(
        Identifier vertex,
        Identifier fragment,
        List<String> samplers,
        List<UniformEntry> uniforms
) {
    public record UniformEntry(String name, String type, int count, float[] defaults) {
    }
}
