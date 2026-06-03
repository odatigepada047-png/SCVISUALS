package moscow.rockstar.systems.parallax;

/**
 * Configuration model for parallax effect settings.
 * Contains global parameters that affect all parallax layers.
 */
public class ParallaxConfig {
    public static final float DEFAULT_INTENSITY = 1.0f;
    public static final long DEFAULT_SMOOTHING = 800L;
    public static final float DEFAULT_MAX_OFFSET = 30.0f;

    private boolean enabled;
    private float intensity;
    private long smoothingDuration;
    private float maxOffsetPixels;

    /**
     * Creates a new parallax configuration with specified parameters.
     *
     * @param enabled Whether the parallax effect is enabled
     * @param intensity Global intensity multiplier (0.0 to 2.0)
     * @param smoothingDuration Animation duration in milliseconds (must be positive)
     * @param maxOffsetPixels Maximum offset in pixels (must be positive)
     * @throws IllegalArgumentException if validation fails
     */
    public ParallaxConfig(boolean enabled, float intensity, long smoothingDuration, float maxOffsetPixels) {
        validateIntensity(intensity);
        validateSmoothingDuration(smoothingDuration);
        validateMaxOffset(maxOffsetPixels);

        this.enabled = enabled;
        this.intensity = intensity;
        this.smoothingDuration = smoothingDuration;
        this.maxOffsetPixels = maxOffsetPixels;
    }

    /**
     * Creates a new parallax configuration with default values.
     */
    public ParallaxConfig() {
        this(true, DEFAULT_INTENSITY, DEFAULT_SMOOTHING, DEFAULT_MAX_OFFSET);
    }

    private void validateIntensity(float intensity) {
        if (intensity < 0.0f || intensity > 2.0f) {
            throw new IllegalArgumentException("Intensity must be between 0.0 and 2.0, got: " + intensity);
        }
    }

    private void validateSmoothingDuration(long duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Smoothing duration must be positive, got: " + duration);
        }
    }

    private void validateMaxOffset(float maxOffset) {
        if (maxOffset <= 0) {
            throw new IllegalArgumentException("Max offset must be positive, got: " + maxOffset);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        validateIntensity(intensity);
        this.intensity = intensity;
    }

    public long getSmoothingDuration() {
        return smoothingDuration;
    }

    public void setSmoothingDuration(long smoothingDuration) {
        validateSmoothingDuration(smoothingDuration);
        this.smoothingDuration = smoothingDuration;
    }

    public float getMaxOffsetPixels() {
        return maxOffsetPixels;
    }

    public void setMaxOffsetPixels(float maxOffsetPixels) {
        validateMaxOffset(maxOffsetPixels);
        this.maxOffsetPixels = maxOffsetPixels;
    }
}
