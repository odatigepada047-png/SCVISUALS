package moscow.rockstar.utility.math;

/**
 * Simple 2D vector for storing offset coordinates.
 * Used primarily for parallax effect calculations.
 */
public class Vector2f {
    public float x;
    public float y;

    /**
     * Creates a new 2D vector with specified coordinates.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets both coordinates of this vector.
     *
     * @param x The new x coordinate
     * @param y The new y coordinate
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
