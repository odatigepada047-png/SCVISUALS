package moscow.rockstar.systems.parallax;

/**
 * Represents a single parallax layer with its own depth factor and smoothing.
 * Each layer moves at a different speed based on its depth factor to create a 3D illusion.
 */
public class ParallaxLayer {
    private static final float MIN_SMOOTHING_MS = 1.0f;
    private static final float MAX_DELTA_SECONDS = 0.1f;

    private final float depthFactor;
    private final float maxOffset;
    private final float smoothingDurationMs;

    private float targetOffsetX;
    private float targetOffsetY;
    private float currentOffsetX;
    private float currentOffsetY;
    private long lastUpdateNanoTime;

    /**
     * Creates a new parallax layer.
     *
     * @param depthFactor Multiplier for parallax intensity (0.0 = no movement, 1.0 = full movement)
     * @param maxOffset Maximum pixel offset allowed
     * @param smoothingDuration Animation duration in milliseconds
     */
    public ParallaxLayer(float depthFactor, float maxOffset, long smoothingDuration) {
        this.depthFactor = depthFactor;
        this.maxOffset = maxOffset;
        this.smoothingDurationMs = Math.max((float)smoothingDuration, MIN_SMOOTHING_MS);
        this.targetOffsetX = 0.0f;
        this.targetOffsetY = 0.0f;
        this.currentOffsetX = 0.0f;
        this.currentOffsetY = 0.0f;
        this.lastUpdateNanoTime = -1L;
    }

    /**
     * Updates target offset based on normalized mouse position.
     *
     * @param normalizedX MouseHandler X position normalized to -1.0 to 1.0
     * @param normalizedY MouseHandler Y position normalized to -1.0 to 1.0
     */
    public void updateTarget(float normalizedX, float normalizedY) {
        // Calculate target offsets based on normalized position, depth factor, and max offset
        this.targetOffsetX = normalizedX * depthFactor * maxOffset;
        this.targetOffsetY = normalizedY * depthFactor * maxOffset;
    }

    /**
     * Updates the current offset towards target offset.
     * This method should be called every frame.
     *
     * @param delta Frame delta time (kept for compatibility, not used)
     */
    public void update(float delta) {
        long now = System.nanoTime();
        if (lastUpdateNanoTime < 0L) {
            lastUpdateNanoTime = now;
            return;
        }

        float deltaSeconds = (float)(now - lastUpdateNanoTime) / 1_000_000_000.0f;
        lastUpdateNanoTime = now;

        if (deltaSeconds <= 0.0f) {
            return;
        }

        deltaSeconds = Math.min(deltaSeconds, MAX_DELTA_SECONDS);

        // Exponential damping: avoids visible "restarts" when target changes often.
        float smoothingSeconds = smoothingDurationMs / 1000.0f;
        float lambda = 4.6051702f / smoothingSeconds;
        float blend = 1.0f - (float)Math.exp(-lambda * deltaSeconds);

        currentOffsetX += (targetOffsetX - currentOffsetX) * blend;
        currentOffsetY += (targetOffsetY - currentOffsetY) * blend;
    }

    /**
     * Gets current X offset.
     *
     * @return Current X offset in pixels
     */
    public float getOffsetX() {
        return currentOffsetX;
    }

    /**
     * Gets current Y offset.
     *
     * @return Current Y offset in pixels
     */
    public float getOffsetY() {
        return currentOffsetY;
    }

    /**
     * Resets layer to center position.
     */
    public void reset() {
        targetOffsetX = 0.0f;
        targetOffsetY = 0.0f;
        currentOffsetX = 0.0f;
        currentOffsetY = 0.0f;
        lastUpdateNanoTime = -1L;
    }

    /**
     * Gets the depth factor of this layer.
     *
     * @return Depth factor (0.0 to 1.0)
     */
    public float getDepthFactor() {
        return depthFactor;
    }

    /**
     * Gets the maximum offset of this layer.
     *
     * @return Maximum offset in pixels
     */
    public float getMaxOffset() {
        return maxOffset;
    }
}
