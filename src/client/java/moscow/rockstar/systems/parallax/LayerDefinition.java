package moscow.rockstar.systems.parallax;

/**
 * Metadata definition for a parallax layer.
 * Contains information about texture, depth, and rendering order.
 */
public class LayerDefinition {
    private final String textureId;
    private final float depthFactor;
    private final int renderOrder;

    /**
     * Creates a new layer definition.
     *
     * @param textureId Resource identifier for layer texture (must not be null or empty)
     * @param depthFactor Depth multiplier (0.0 = background, 1.0 = foreground, must be in range [0.0, 1.0])
     * @param renderOrder Z-order for rendering (lower = back, must be non-negative)
     * @throws IllegalArgumentException if validation fails
     */
    public LayerDefinition(String textureId, float depthFactor, int renderOrder) {
        validateTextureId(textureId);
        validateDepthFactor(depthFactor);
        validateRenderOrder(renderOrder);

        this.textureId = textureId;
        this.depthFactor = depthFactor;
        this.renderOrder = renderOrder;
    }

    private void validateTextureId(String textureId) {
        if (textureId == null || textureId.isEmpty()) {
            throw new IllegalArgumentException("Texture ID must not be null or empty");
        }
    }

    private void validateDepthFactor(float depthFactor) {
        if (depthFactor < 0.0f || depthFactor > 1.0f) {
            throw new IllegalArgumentException("Depth factor must be between 0.0 and 1.0, got: " + depthFactor);
        }
    }

    private void validateRenderOrder(int renderOrder) {
        if (renderOrder < 0) {
            throw new IllegalArgumentException("Render order must be non-negative, got: " + renderOrder);
        }
    }

    public String getTextureId() {
        return textureId;
    }

    public float getDepthFactor() {
        return depthFactor;
    }

    public int getRenderOrder() {
        return renderOrder;
    }
}
