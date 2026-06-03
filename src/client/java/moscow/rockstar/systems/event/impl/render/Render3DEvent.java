/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.renderer.Camera
 *  net.minecraft.client.util.math.PoseStack
 *  org.joml.Matrix4f
 */
package moscow.rockstar.systems.event.impl.render;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

public class Render3DEvent
extends Event {
    private final PoseStack matrices;
    private final Matrix4f positionMatrix;
    private final Matrix4f projectionMatrix;
    private final Camera camera;
    private final float tickDelta;

    @Generated
    public PoseStack pose() {
        return this.matrices;
    }

    @Generated
    public Matrix4f getPositionMatrix() {
        return this.positionMatrix;
    }

    @Generated
    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    @Generated
    public Camera getMainCamera() {
        return this.camera;
    }

    @Generated
    public float getGameTimeDeltaPartialTick() {
        return this.tickDelta;
    }

    @Generated
    public Render3DEvent(PoseStack matrices, Matrix4f positionMatrix, Matrix4f projectionMatrix, Camera camera, float tickDelta) {
        this.matrices = matrices;
        this.positionMatrix = positionMatrix;
        this.projectionMatrix = projectionMatrix;
        this.camera = camera;
        this.tickDelta = tickDelta;
    }
}

