/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.renderer.Camera
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.Mth
 *  net.minecraft.util.math.Vec2
 *  net.minecraft.util.math.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 */
package moscow.rockstar.utility.render;

import lombok.Generated;
import moscow.rockstar.systems.waypoints.WayPointsManager;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public final class Utils
implements IMinecraft {
    private static Matrix4f modelViewMatrix;
    private static Matrix4f projectionMatrix;
    private static Vec3 cameraPos;

    public static void onRender(Matrix4f modelView, Matrix4f projection) {
        modelViewMatrix = new Matrix4f((Matrix4fc)modelView);
        projectionMatrix = new Matrix4f((Matrix4fc)projection);
    }

    public static void onRender(Matrix4f modelView, Matrix4f projection, Vec3 camPos) {
        modelViewMatrix = new Matrix4f((Matrix4fc)modelView);
        projectionMatrix = new Matrix4f((Matrix4fc)projection);
        cameraPos = camPos;
    }

    @Nullable
    public static Matrix4fc getCachedLevelModelView() {
        return modelViewMatrix;
    }

    @Nullable
    public static Matrix4fc getCachedLevelProjection() {
        return projectionMatrix;
    }

    @Nullable
    public static Vec3 getCachedCameraPos() {
        return cameraPos;
    }

    /**
     * Scaled GUI pixel coordinates for a world-space point — see {@link WayPointsManager#projectWorldPositionToGui(Vec3)}.
     */
    public static Vec2 worldToScreen(Vec3 worldCoords) {
        return WayPointsManager.projectWorldPositionToGui(worldCoords);
    }

    public static Vec3 getInterpolatedPos(Entity entity, float tickDelta) {
        return new Vec3(Mth.lerp((double)tickDelta, (double)entity.xo, (double)entity.getX()), Mth.lerp((double)tickDelta, (double)entity.yo, (double)entity.getY()), Mth.lerp((double)tickDelta, (double)entity.zo, (double)entity.getZ()));
    }

    public static Vec3 getInterpolatedPos(Vec3 prev, Vec3 pos, float tickDelta) {
        return new Vec3(Mth.lerp((double)tickDelta, (double)prev.x, (double)pos.x), Mth.lerp((double)tickDelta, (double)prev.y, (double)pos.y), Mth.lerp((double)tickDelta, (double)prev.z, (double)pos.z));
    }

    @Generated
    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

