package moscow.rockstar.utility.render;

import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;

public final class HudMatrices {
    private HudMatrices() {
    }

    /**
     * Converts {@link Matrix3x2fStack} to a 4×4 matrix matching vanilla GUI ortho space
     * (same layout as {@code GuiRenderer.draw} model-view translation on Z).
     */
    public static Matrix4f toMatrix4f(Matrix3x2fStack stack) {
        Matrix4f matrix = new Matrix4f();
        matrix.m00(stack.m00());
        matrix.m01(stack.m01());
        matrix.m10(stack.m10());
        matrix.m11(stack.m11());
        matrix.m30(stack.m20());
        matrix.m31(stack.m21());
        matrix.m22(1.0f);
        matrix.m33(1.0f);
        return matrix;
    }

    public static Matrix4f toMatrix4f(Matrix3x2fStack stack, Matrix4f dest) {
        dest.identity();
        dest.m00(stack.m00());
        dest.m01(stack.m01());
        dest.m10(stack.m10());
        dest.m11(stack.m11());
        dest.m30(stack.m20());
        dest.m31(stack.m21());
        dest.m22(1.0f);
        dest.m33(1.0f);
        return dest;
    }
}
