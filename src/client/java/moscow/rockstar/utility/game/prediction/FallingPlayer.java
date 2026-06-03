/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.LocalPlayer
 *  net.minecraft.util.Mth
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.utility.game.prediction;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FallingPlayer {
    private final LocalPlayer player;
    private double x;
    private double y;
    private double z;
    private double motionX;
    private double motionY;
    private double motionZ;
    private final float yaw;
    private int simulatedTicks;

    public FallingPlayer(LocalPlayer player, double x, double y, double z, double motionX, double motionY, double motionZ, float yaw) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.yaw = yaw;
        this.simulatedTicks = 0;
    }

    public static FallingPlayer fromPlayer(LocalPlayer player) {
        return new FallingPlayer(player, player.position().x, player.position().y, player.position().z, player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z, player.getYRot());
    }

    public boolean findFall(float fallDist) {
        Vec3 rotationVec = this.player.getViewVector(0.0f);
        double tempMotionX = this.motionX;
        double tempMotionY = this.motionY;
        double tempMotionZ = this.motionZ;
        double d = 0.08;
        float n = Mth.cos((float)(this.player.getXRot() * ((float)Math.PI / 180)));
        n = (float)((double)(n * n) * Math.min(rotationVec.length() / 0.4, 1.0));
        Vec3 vec3d = new Vec3(tempMotionX, tempMotionY, tempMotionZ).add(0.0, d * (-1.0 + (double)n * 0.75), 0.0);
        tempMotionY = vec3d.y * (double)0.98f;
        return tempMotionY < (double)fallDist;
    }

    public boolean findFall(float fallDist, int ticks) {
        Vec3 rotationVec = this.player.getViewVector(0.0f);
        double tempMotionX = this.motionX;
        double tempMotionY = this.motionY;
        double tempMotionZ = this.motionZ;
        double d = 0.08;
        float n = Mth.cos((float)(this.player.getXRot() * ((float)Math.PI / 180)));
        n = (float)((double)(n * n) * Math.min(rotationVec.length() / 0.4, 1.0));
        for (int i = 0; i < ticks; ++i) {
            Vec3 vec3d = new Vec3(tempMotionX, tempMotionY, tempMotionZ).add(0.0, d * (-1.0 + (double)n * 0.75), 0.0);
            tempMotionY = vec3d.y * (double)0.98f;
            if (!(tempMotionY >= (double)fallDist)) continue;
            return false;
        }
        return true;
    }
}

