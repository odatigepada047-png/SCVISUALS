/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.TrapdoorBlock
 *  net.minecraft.client.gui.hud.BossBarHud
 *  net.minecraft.client.gui.hud.ClientBossBar
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.Player
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.AABB
 *  net.minecraft.util.Mth
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockGetter
 *  net.minecraft.world.ClipContext
 *  net.minecraft.world.ClipContext$FluidHandling
 *  net.minecraft.world.ClipContext$ShapeType
 *  net.minecraft.world.Level
 */
package moscow.rockstar.utility.math;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.utility.game.prediction.ElytraPredictionSystem;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.calculator.ExpressionBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;

public final class MathUtility
implements IMinecraft {
    private static final int TABLE_SIZE = 65536;
    private static final double TWO_PI = Math.PI * 2;
    private static final double[] TRIG_TABLE = new double[65536];

    public static double sin(double radians) {
        int index = (int)(radians * 10430.378350470453) & 0xFFFF;
        return TRIG_TABLE[index];
    }

    public static double cos(double radians) {
        int index = (int)(radians * 10430.378350470453 + 16384.0) & 0xFFFF;
        return TRIG_TABLE[index];
    }

    public static float random(double min, double max) {
        return (float)(min + (max - min) * Math.random());
    }

    public static double cubicBezier(double t, double p0, double p1, double p2, double p3) {
        return Math.pow(1.0 - t, 3.0) * p0 + 3.0 * t * Math.pow(1.0 - t, 2.0) * p1 + 3.0 * Math.pow(t, 2.0) * (1.0 - t) * p2 + Math.pow(t, 3.0) * p3;
    }

    public static boolean canSeen(Vec3 targetVec) {
        return MathUtility.mc.level.clip(new ClipContext(MathUtility.mc.player.getEyePosition(1.0f), targetVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)MathUtility.mc.player)).getType() == HitResult.Type.MISS;
    }

    public static boolean canShoot(Vec3 targetVec) {
        Vec3 start = MathUtility.mc.player.getEyePosition(1.0f);
        Vec3 direction = targetVec.subtract(start);
        double distance = direction.length();
        direction = direction.normalize();
        HashSet<BlockPos> checkedBlocks = new HashSet<BlockPos>();
        int solidBlocks = 0;
        double step = 0.25;
        for (double d = 0.0; d <= distance; d += step) {
            VoxelShape collisionShape;
            Vec3 currentPos = start.add(direction.scale(d));
            BlockPos blockPos = BlockPos.containing(currentPos);
            if (checkedBlocks.contains(blockPos)) continue;
            checkedBlocks.add(blockPos);
            BlockState blockState = MathUtility.mc.level.getBlockState(blockPos);
            if (blockState.isAir()) continue;
            Block block = blockState.getBlock();
            if (blockState.is(Blocks.GLASS) || blockState.is(Blocks.GLASS_PANE) || blockState.getBlock() instanceof TrapDoorBlock || (collisionShape = blockState.getCollisionShape((BlockGetter)MathUtility.mc.level, blockPos)).isEmpty()) continue;
            ++solidBlocks;
        }
        AtomicBoolean snipe = new AtomicBoolean(false);
        BossHealthOverlay boss = MathUtility.mc.gui.getBossOverlay();
        if (boss != null) {
            Class<BossHealthOverlay> bossbarklass = BossHealthOverlay.class;
            try {
                Field field = bossbarklass.getDeclaredField("events");
                field.setAccessible(true);
                Map<UUID, LerpingBossEvent> bossBars = (Map<UUID, LerpingBossEvent>)field.get(boss);
                for (UUID uuid : bossBars.keySet()) {
                    LerpingBossEvent clientBossBar = bossBars.get(uuid);
                    List<Component> siblings = clientBossBar.getName().getSiblings();
                    siblings.stream().allMatch(text -> {
                        if (text.getString().contains("\ub8f3\ua223\ua203\ub8f2\ua223\ua205")) {
                            snipe.set(true);
                        }
                        return true;
                    });
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return solidBlocks <= (snipe.get() ? 3 : (MathUtility.mc.player.getInventory().getSelectedSlot() == 0 ? 2 : 1));
    }

    public static int levenshtein(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[] dp = new int[m + 1];
        for (int j = 0; j <= m; ++j) {
            dp[j] = j;
        }
        for (int i = 1; i <= n; ++i) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= m; ++j) {
                int tmp = dp[j];
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[j] = Math.min(Math.min(dp[j] + 1, dp[j - 1] + 1), prev + cost);
                prev = tmp;
            }
        }
        return dp[m];
    }

    public static float interpolate(double oldValue, double newValue, double interpolationValue) {
        return (float)(oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static HitResult rayTrace(double rayTraceDistance, float yaw, float pitch, Entity entity) {
        Vec3 startVec = MathUtility.mc.player.getEyePosition(1.0f);
        Vec3 directionVec = MathUtility.getVectorForRotation(pitch, yaw);
        Vec3 endVec = startVec.add(directionVec.x * rayTraceDistance, directionVec.y * rayTraceDistance, directionVec.z * rayTraceDistance);
        return MathUtility.mc.level.clip(new ClipContext(startVec, endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
    }

    public static boolean tracedTo(Entity shooter, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter, double distance, Entity target) {
        Level world = shooter.level();
        double d0 = distance;
        for (Entity entity1 : world.getEntities(shooter, boundingBox, filter)) {
            AABB box = entity1.getBoundingBox().inflate((double)entity1.getPickRadius());
            Optional<Vec3> optional = box.clip(startVec, endVec);
            if (box.contains(startVec)) {
                if (!(d0 >= 0.0)) continue;
                if (entity1 == target) {
                    return true;
                }
                d0 = 0.0;
                continue;
            }
            if (!optional.isPresent()) continue;
            Vec3 vec3d1 = (Vec3)optional.get();
            double d1 = startVec.distanceToSqr(vec3d1);
            if (entity1.getRootVehicle() == shooter.getRootVehicle()) {
                if (d0 != 0.0 || entity1 != target) continue;
                return true;
            }
            if (entity1 == target) {
                return true;
            }
            d0 = d1;
        }
        return false;
    }

    public static boolean canTraceWithBlock(double rayTraceDistance, float yaw, float pitch, Entity entity, Entity target, boolean checkBlocks) {
        double targetDistSq;
        double blockDistSq;
        BlockHitResult blockHit;
        Vec3 endPos;
        if (target == null || entity == null || MathUtility.mc.level == null) {
            return false;
        }
        float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 startPos = entity.getEyePosition(partialTicks);
        endPos = target.getBoundingBox().getCenter();
        if (checkBlocks && (blockHit = MathUtility.mc.level.clip(new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity))) != null && blockHit.getType() == HitResult.Type.BLOCK && (blockDistSq = blockHit.getLocation().distanceToSqr(startPos)) < (targetDistSq = endPos.distanceToSqr(startPos))) {
            return false;
        }
        Vec3 direction = MathUtility.getVectorForRotation(pitch, yaw);
        Vec3 rayEnd = startPos.add(direction.scale(rayTraceDistance));
        AABB searchBox = entity.getBoundingBox().expandTowards(direction.scale(rayTraceDistance)).inflate(1.0);
        return MathUtility.tracedTo(entity, startPos, rayEnd, searchBox, e -> !e.isSpectator() && e.isPickable(), rayTraceDistance * rayTraceDistance, target);
    }

    public static Vec3 getVectorForRotation(float pitch, float yaw) {
        float yawRadians = -yaw * ((float)Math.PI / 180) - (float)Math.PI;
        float pitchRadians = -pitch * ((float)Math.PI / 180);
        float cosYaw = Mth.cos((float)yawRadians);
        float sinYaw = Mth.sin((float)yawRadians);
        float cosPitch = -Mth.cos((float)pitchRadians);
        float sinPitch = Mth.sin((float)pitchRadians);
        return new Vec3((double)(sinYaw * cosPitch), (double)sinPitch, (double)(cosYaw * cosPitch));
    }

    public static float angleDifference(float angle1, float angle2) {
        float diff = (angle1 - angle2) % 360.0f;
        if (diff < -180.0f) {
            diff += 360.0f;
        } else if (diff > 180.0f) {
            diff -= 360.0f;
        }
        return diff;
    }

    public static String calculate(String expression) {
        if ((expression = expression.replaceAll("\\s+", "")).isEmpty()) {
            return "";
        }
        try {
            double result = new ExpressionBuilder(expression).build().evaluate();
            return String.valueOf(result);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return expression;
        }
    }

    @Generated
    private MathUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        for (int i = 0; i < 65536; ++i) {
            MathUtility.TRIG_TABLE[i] = Math.sin((double)i * (Math.PI * 2) / 65536.0);
        }
    }
}
