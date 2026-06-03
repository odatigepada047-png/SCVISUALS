package moscow.rockstar.utility.render;

/**
 * Replaces removed {@code RenderSystem#setShaderColor} / {@code getShaderColor} for UI alpha stacking.
 */
public final class ShaderColorHelper {
    private static final float[] COLOR = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

    private ShaderColorHelper() {
    }

    public static void setShaderColor(float r, float g, float b, float a) {
        COLOR[0] = r;
        COLOR[1] = g;
        COLOR[2] = b;
        COLOR[3] = a;
    }

    public static float[] getShaderColor() {
        return COLOR;
    }

    public static float getAlpha() {
        return COLOR[3];
    }

    public static void reset() {
        setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
