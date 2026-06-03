package moscow.rockstar.systems.parallax;

import moscow.rockstar.utility.math.Vector2f;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages multiple parallax layers, calculates offsets based on mouse position,
 * and coordinates smooth transitions.
 */
public class ParallaxManager {
    private final ParallaxConfig config;
    private final List<ParallaxLayer> layers;
    
    private float normalizedMouseX;
    private float normalizedMouseY;
    private float previousMouseX;
    private float previousMouseY;
    private boolean enabled;

    /**
     * Creates a new parallax manager with specified configuration.
     *
     * @param config Configuration for the parallax system
     */
    public ParallaxManager(ParallaxConfig config) {
        this.config = config;
        this.layers = new ArrayList<>();
        this.normalizedMouseX = 0.0f;
        this.normalizedMouseY = 0.0f;
        this.previousMouseX = -1.0f;
        this.previousMouseY = -1.0f;
        this.enabled = config.isEnabled();
    }

    /**
     * Creates a new parallax manager with default configuration.
     */
    public ParallaxManager() {
        this(new ParallaxConfig());
    }

    /**
     * Adds a parallax layer with specified depth factor.
     *
     * @param layer The parallax layer to add
     */
    public void addLayer(ParallaxLayer layer) {
        layers.add(layer);
        
        // Initialize layer with current mouse position
        layer.updateTarget(normalizedMouseX, normalizedMouseY);
        
        // Log warning if too many layers
        if (layers.size() > 10) {
            System.err.println("[ParallaxManager] Warning: More than 10 layers added. This may impact performance.");
        }
    }

    /**
     * Updates mouse position for parallax calculations.
     *
     * @param mouseX Current mouse X coordinate
     * @param mouseY Current mouse Y coordinate
     * @param screenWidth Width of the screen
     * @param screenHeight Height of the screen
     */
    public void updateMousePosition(float mouseX, float mouseY, int screenWidth, int screenHeight) {
        // Validate screen dimensions
        if (screenWidth <= 0 || screenHeight <= 0) {
            if (enabled) {
                System.err.println("[ParallaxManager] Warning: Invalid screen dimensions (" + 
                    screenWidth + "x" + screenHeight + "). Disabling parallax effect.");
                enabled = false;
            }
            return;
        }
        
        // Re-enable if dimensions are now valid
        if (!enabled && config.isEnabled()) {
            enabled = true;
        }
        
        if (!enabled) {
            return;
        }
        
        // Skip if mouse hasn't moved (optimization)
        if (mouseX == previousMouseX && mouseY == previousMouseY) {
            return;
        }
        
        previousMouseX = mouseX;
        previousMouseY = mouseY;
        
        // Calculate screen center
        float centerX = screenWidth / 2.0f;
        float centerY = screenHeight / 2.0f;
        
        // Calculate normalized coordinates relative to center
        normalizedMouseX = (mouseX - centerX) / centerX;
        normalizedMouseY = (mouseY - centerY) / centerY;
        
        // Clamp to [-1.0, 1.0] range
        normalizedMouseX = Math.max(-1.0f, Math.min(1.0f, normalizedMouseX));
        normalizedMouseY = Math.max(-1.0f, Math.min(1.0f, normalizedMouseY));
        
        // Apply intensity multiplier
        float intensity = config.getIntensity();
        float adjustedX = normalizedMouseX * intensity;
        float adjustedY = normalizedMouseY * intensity;
        
        // Update all layers with new normalized coordinates
        for (ParallaxLayer layer : layers) {
            layer.updateTarget(adjustedX, adjustedY);
        }
    }

    /**
     * Updates all layer animations.
     *
     * @param delta Frame delta time
     */
    public void update(float delta) {
        if (!enabled) {
            return;
        }
        
        for (ParallaxLayer layer : layers) {
            layer.update(delta);
        }
    }

    /**
     * Gets the current offset for a specific layer.
     *
     * @param layerIndex Index of the layer
     * @return Vector2f containing x and y offsets
     */
    public Vector2f getLayerOffset(int layerIndex) {
        if (layerIndex < 0 || layerIndex >= layers.size()) {
            return new Vector2f(0.0f, 0.0f);
        }
        
        ParallaxLayer layer = layers.get(layerIndex);
        return new Vector2f(layer.getOffsetX(), layer.getOffsetY());
    }

    /**
     * Resets all layers to center position.
     */
    public void reset() {
        normalizedMouseX = 0.0f;
        normalizedMouseY = 0.0f;
        previousMouseX = -1.0f;
        previousMouseY = -1.0f;
        
        for (ParallaxLayer layer : layers) {
            layer.reset();
        }
    }

    /**
     * Enables or disables the parallax effect.
     *
     * @param enabled True to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        config.setEnabled(enabled);
    }

    /**
     * Checks if the parallax effect is enabled.
     *
     * @return True if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the global intensity multiplier.
     *
     * @param intensity Intensity value (0.0 to 2.0)
     */
    public void setIntensity(float intensity) {
        config.setIntensity(intensity);
        
        // Re-update all layers with new intensity
        float adjustedX = normalizedMouseX * intensity;
        float adjustedY = normalizedMouseY * intensity;
        
        for (ParallaxLayer layer : layers) {
            layer.updateTarget(adjustedX, adjustedY);
        }
    }

    /**
     * Gets the number of layers in this manager.
     *
     * @return Number of layers
     */
    public int getLayerCount() {
        return layers.size();
    }
}
