/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.network.PlayerInfo
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.Player
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.utility.game.prediction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class ElytraPredictionSystem {
    private static final int MAX_HISTORY_SIZE = 15;
    private static final double GRAVITY = -0.08;
    private static final double AIR_RESISTANCE_X = 0.99;
    private static final double AIR_RESISTANCE_Y = 0.98;
    private static final double AIR_RESISTANCE_Z = 0.99;
    private static final double PITCH_INFLUENCE = 0.06;
    private static final double DIRECTION_CORRECTION = 0.1;
    private static final long DATA_CLEANUP_INTERVAL = 30000L;
    private static final Map<UUID, List<MovementData>> movementHistory = new ConcurrentHashMap<UUID, List<MovementData>>();
    private static final Map<UUID, PlayerPredictionStats> playerStats = new ConcurrentHashMap<UUID, PlayerPredictionStats>();
    private static final Map<UUID, Integer> customPredictionTicks = new ConcurrentHashMap<UUID, Integer>();
    private static long lastCleanupTime = System.currentTimeMillis();

    public static Vec3 predictPlayerPosition(Player target) {
        if (target == null) {
            return Vec3.ZERO;
        }
        ElytraPredictionSystem.updateEntityTracking(target);
        if (!ElytraPredictionSystem.isLeaving(target)) {
            return target.position();
        }
        int predictionTicks = ElytraPredictionSystem.calculateOptimalPredictionTicks(target);
        return ElytraPredictionSystem.simulateElytraFlight(target, predictionTicks);
    }

    public static void updateEntityTracking(Player entity) {
        if (entity == null) {
            return;
        }
        UUID uuid = entity.getUUID();
        long currentTime = System.currentTimeMillis();
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        double distanceToClient = entity.distanceTo((Entity)client.player);
        MovementData data = new MovementData(entity.position(), entity.getDeltaMovement(), entity.getXRot(), entity.getYRot(), entity.isFallFlying(), currentTime, distanceToClient);
        List<MovementData> history = movementHistory.computeIfAbsent(uuid, k -> new ArrayList<>());
        history.add(data);
        if (history.size() > 15) {
            history.remove(0);
        }
        ElytraPredictionSystem.updatePlayerStats(uuid, data);
        ElytraPredictionSystem.cleanupOldData(currentTime);
    }

    public static boolean isLeaving(Player target) {
        if (!target.isFallFlying()) {
            return false;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return false;
        }
        UUID uuid = target.getUUID();
        List<MovementData> history = movementHistory.get(uuid);
        if (history == null || history.size() < 3) {
            return false;
        }
        boolean isDistanceIncreasing = ElytraPredictionSystem.isDistanceIncreasing(history);
        boolean isVelocityDirectedAway = ElytraPredictionSystem.isVelocityDirectedAway(target, (Player)client.player);
        boolean hasSignificantSpeed = ElytraPredictionSystem.hasSignificantSpeed(target);
        int positiveChecks = 0;
        if (isDistanceIncreasing) {
            ++positiveChecks;
        }
        if (isVelocityDirectedAway) {
            ++positiveChecks;
        }
        if (hasSignificantSpeed) {
            ++positiveChecks;
        }
        return positiveChecks >= 2;
    }

    private static boolean isDistanceIncreasing(List<MovementData> history) {
        if (history.size() < 3) {
            return false;
        }
        int pointsToAnalyze = Math.min(5, history.size());
        List<MovementData> recentHistory = history.subList(history.size() - pointsToAnalyze, history.size());
        int increasingCount = 0;
        for (int i = 1; i < recentHistory.size(); ++i) {
            if (!(recentHistory.get((int)i).distanceToClient > recentHistory.get((int)(i - 1)).distanceToClient)) continue;
            ++increasingCount;
        }
        return increasingCount >= (recentHistory.size() - 1) / 2;
    }

    private static boolean isVelocityDirectedAway(Player target, Player client) {
        Vec3 velocityDirection;
        Vec3 targetPos = target.position();
        Vec3 clientPos = client.position();
        Vec3 targetVelocity = target.getDeltaMovement();
        Vec3 directionToTarget = targetPos.subtract(clientPos).normalize();
        double dotProduct = directionToTarget.dot(velocityDirection = targetVelocity.normalize());
        return dotProduct > 0.3;
    }

    private static boolean hasSignificantSpeed(Player target) {
        double speed = target.getDeltaMovement().length();
        return speed > 0.8;
    }

    private static Vec3 simulateElytraFlight(Player player, int ticksAhead) {
        Vec3 position = player.position();
        Vec3 velocity = player.getDeltaMovement();
        float pitch = player.getXRot();
        float yaw = player.getYRot();
        boolean isFlying = player.isFallFlying();
        for (int tick = 0; tick < ticksAhead; ++tick) {
            if (isFlying) {
                position = ElytraPredictionSystem.simulateElytraTick(position, velocity, pitch, yaw);
                velocity = ElytraPredictionSystem.updateElytraVelocity(velocity, pitch, yaw);
                continue;
            }
            velocity = velocity.add(0.0, -0.08, 0.0).scale(0.98);
            position = position.add(velocity);
        }
        return position;
    }

    private static Vec3 simulateElytraTick(Vec3 position, Vec3 velocity, float pitch, float yaw) {
        return position.add(velocity);
    }

    private static Vec3 updateElytraVelocity(Vec3 velocity, float pitch, float yaw) {
        double yawAcceleration;
        double motionX = velocity.x;
        double motionY = velocity.y;
        double motionZ = velocity.z;
        float pitchRad = (float)Math.toRadians(pitch);
        float yawRad = (float)Math.toRadians(yaw);
        Vec3 lookDirection = new Vec3(-Math.sin(yawRad) * Math.cos(pitchRad), -Math.sin(pitchRad), Math.cos(yawRad) * Math.cos(pitchRad));
        double horizontalVelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        double lookHorizontal = Math.sqrt(lookDirection.x * lookDirection.x + lookDirection.z * lookDirection.z);
        float cosPitch = (float)Math.cos(pitchRad);
        float cosPitchSq = cosPitch * cosPitch;
        if ((motionY += -0.08 + (double)cosPitchSq * 0.06) < 0.0 && lookHorizontal > 0.0) {
            yawAcceleration = motionY * -0.1 * (double)cosPitchSq;
            motionY += yawAcceleration;
            motionX += lookDirection.x * yawAcceleration / lookHorizontal;
            motionZ += lookDirection.z * yawAcceleration / lookHorizontal;
        }
        if (pitch < 0.0f && lookHorizontal > 0.0) {
            yawAcceleration = horizontalVelocity * -Math.sin(pitchRad) * 0.04;
            motionY += yawAcceleration * 3.2;
            motionX -= lookDirection.x * yawAcceleration / lookHorizontal;
            motionZ -= lookDirection.z * yawAcceleration / lookHorizontal;
        }
        if (lookHorizontal > 0.0) {
            motionX += (lookDirection.x / lookHorizontal * horizontalVelocity - motionX) * 0.1;
            motionZ += (lookDirection.z / lookHorizontal * horizontalVelocity - motionZ) * 0.1;
        }
        return new Vec3(motionX *= 0.99, motionY *= 0.98, motionZ *= 0.99);
    }

    private static int calculateOptimalPredictionTicks(Player target) {
        UUID uuid = target.getUUID();
        if (customPredictionTicks.containsKey(uuid)) {
            return customPredictionTicks.get(uuid);
        }
        int baseTicks = ElytraPredictionSystem.calculateNetworkDelay(target);
        List<MovementData> history = movementHistory.get(uuid);
        if (history == null || history.size() < 3) {
            return baseTicks;
        }
        double velocityVariance = ElytraPredictionSystem.calculateVelocityVariance(history);
        double directionVariance = ElytraPredictionSystem.calculateDirectionVariance(history);
        if (target.isFallFlying()) {
            double speed = target.getDeltaMovement().length();
            if (speed > 2.0) {
                baseTicks += Math.min(4, (int)(speed * 1.2));
            }
            if (directionVariance > 30.0) {
                baseTicks += 2;
            }
        }
        return Math.max(1, Math.min(15, baseTicks));
    }

    private static int calculateNetworkDelay(Player player) {
        Minecraft client = Minecraft.getInstance();
        int ping = 100;
        if (client.getConnection() != null) {
            try {
                PlayerInfo playerListEntry = client.getConnection().getPlayerInfo(player.getUUID());
                if (playerListEntry != null) {
                    ping = playerListEntry.getLatency();
                }
            }
            catch (Exception playerListEntry) {
                // empty catch block
            }
        }
        int networkTicks = Math.max(1, ping / 50);
        int serverProcessingTicks = 2;
        return networkTicks + serverProcessingTicks;
    }

    private static double calculateVelocityVariance(List<MovementData> history) {
        if (history.size() < 2) {
            return 0.0;
        }
        double[] speeds = history.stream().mapToDouble(data -> data.velocity.length()).toArray();
        double mean = Arrays.stream(speeds).average().orElse(0.0);
        double variance = Arrays.stream(speeds).map(speed -> Math.pow(speed - mean, 2.0)).average().orElse(0.0);
        return Math.sqrt(variance);
    }

    private static double calculateDirectionVariance(List<MovementData> history) {
        if (history.size() < 2) {
            return 0.0;
        }
        double totalVariance = 0.0;
        for (int i = 1; i < history.size(); ++i) {
            MovementData prev = history.get(i - 1);
            MovementData curr = history.get(i);
            double yawDiff = Math.abs(curr.yaw - prev.yaw);
            double pitchDiff = Math.abs(curr.pitch - prev.pitch);
            if (yawDiff > 180.0) {
                yawDiff = 360.0 - yawDiff;
            }
            totalVariance += Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
        }
        return totalVariance / (double)(history.size() - 1);
    }

    private static void updatePlayerStats(UUID uuid, MovementData data) {
        PlayerPredictionStats stats = playerStats.computeIfAbsent(uuid, k -> new PlayerPredictionStats());
        stats.update(data);
    }

    private static void cleanupOldData(long currentTime) {
        if (currentTime - lastCleanupTime < 30000L) {
            return;
        }
        lastCleanupTime = currentTime;
        movementHistory.entrySet().removeIf(entry -> {
            List<MovementData> history = entry.getValue();
            history.removeIf(data -> currentTime - data.timestamp > 30000L);
            return history.isEmpty();
        });
        playerStats.entrySet().removeIf(entry -> currentTime - ((PlayerPredictionStats)entry.getValue()).lastUpdate > 30000L);
    }

    @Generated
    private ElytraPredictionSystem() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static class MovementData {
        public final Vec3 position;
        public final Vec3 velocity;
        public final float pitch;
        public final float yaw;
        public final boolean isFallFlying;
        public final long timestamp;
        public final double distanceToClient;

        public MovementData(Vec3 position, Vec3 velocity, float pitch, float yaw, boolean isFallFlying, long timestamp, double distanceToClient) {
            this.position = position;
            this.velocity = velocity;
            this.pitch = pitch;
            this.yaw = yaw;
            this.isFallFlying = isFallFlying;
            this.timestamp = timestamp;
            this.distanceToClient = distanceToClient;
        }
    }

    private static class PlayerPredictionStats {
        private double averageSpeed = 0.0;
        private int sampleCount = 0;
        private long lastUpdate = System.currentTimeMillis();

        private PlayerPredictionStats() {
        }

        public void update(MovementData data) {
            double speed = data.velocity.length();
            this.averageSpeed = (this.averageSpeed * (double)this.sampleCount + speed) / (double)(this.sampleCount + 1);
            ++this.sampleCount;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
}
