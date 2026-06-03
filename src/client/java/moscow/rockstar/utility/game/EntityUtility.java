/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.Player
 *  net.minecraft.item.AxeItem
 *  net.minecraft.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.item.MaceItem
 *  net.minecraft.item.SwordItem
 *  net.minecraft.item.TridentItem
 *  net.minecraft.scoreboard.ReadOnlyScoreInfo
 *  net.minecraft.scoreboard.ScoreHolder
 *  net.minecraft.scoreboard.DisplaySlot
 *  net.minecraft.scoreboard.Objective
 *  net.minecraft.scoreboard.number.NumberFormat
 *  net.minecraft.scoreboard.number.StyledFormat
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.AABB
 *  net.minecraft.util.math.Position
 *  net.minecraft.world.Level
 */
package moscow.rockstar.utility.game;

import lombok.Generated;
import moscow.rockstar.utility.game.ClientInputHelper;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
// import net.minecraft.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;

public final class EntityUtility
implements IMinecraft {
    private static float timer = 1.0f;

    public static void resetTimer() {
        timer = 1.0f;
    }

    public static Block getBlock() {
        return EntityUtility.getBlock(0.0, 0.0, 0.0);
    }

    public static Block getBlock(double x, double y, double z) {
        return !EntityUtility.isInGame() ? Blocks.AIR : EntityUtility.mc.level.getBlockState(BlockPos.containing(EntityUtility.mc.player.position().add(x, y, z))).getBlock();
    }

    public static boolean collideWith(LivingEntity entity) {
        return EntityUtility.collideWith(entity, 0.0f);
    }

    public static boolean collideWith(LivingEntity entity, float grow) {
        AABB box = EntityUtility.mc.player.getBoundingBox();
        AABB targetbox = entity.getBoundingBox().inflate((double)grow, 0.0, (double)grow);
        return box.maxX > targetbox.minX && box.maxY > targetbox.minY && box.maxZ > targetbox.minZ && box.minX < targetbox.maxX && box.minY < targetbox.maxY && box.minZ < targetbox.maxZ;
    }

    public static void setSpeed(double speed) {
        ClientInput input = EntityUtility.mc.player.input;
        double forward = ClientInputHelper.getMovementForward(input);
        double strafe = ClientInputHelper.getMovementSideways(input);
        float yaw = EntityUtility.mc.player.getYRot();
        if (forward == 0.0 && strafe == 0.0) {
            EntityUtility.mc.player.setDeltaMovement(0.0, EntityUtility.mc.player.getDeltaMovement().y, 0.0);
            return;
        }
        if (forward != 0.0) {
            if (strafe > 0.0) {
                yaw += (float)(forward > 0.0 ? -45 : 45);
            } else if (strafe < 0.0) {
                yaw += (float)(forward > 0.0 ? 45 : -45);
            }
            strafe = 0.0;
            forward = forward > 0.0 ? 1.0 : -1.0;
        }
        double motionX = forward * speed * Math.cos(Math.toRadians((double)yaw + 90.0)) + strafe * speed * Math.sin(Math.toRadians((double)yaw + 90.0));
        double motionZ = forward * speed * Math.sin(Math.toRadians((double)yaw + 90.0)) - strafe * speed * Math.cos(Math.toRadians((double)yaw + 90.0));
        EntityUtility.mc.player.setDeltaMovement(motionX, EntityUtility.mc.player.getDeltaMovement().y, motionZ);
    }

    public static boolean isPlayerMoving() {
        if (EntityUtility.mc.player == null || EntityUtility.mc.level == null || EntityUtility.mc.player.input == null) {
            return false;
        }
        ClientInput input = EntityUtility.mc.player.input;
        return ClientInputHelper.getMovementForward(input) != 0.0f || ClientInputHelper.getMovementSideways(input) != 0.0f;
    }

    public static Block getBlockBelow(Entity entity) {
        if (entity == null) {
            return null;
        }
        BlockPos pos = entity.blockPosition().below();
        return EntityUtility.getBlockAt(pos, entity.level());
    }

    public static Block getBlockAbove(Entity entity) {
        if (entity == null) {
            return null;
        }
        BlockPos pos = entity.blockPosition().offset(0, Math.round(entity.getBbHeight()), 0).above();
        return EntityUtility.getBlockAt(pos, entity.level());
    }

    public static Block getBlockBelowPlayer() {
        if (EntityUtility.mc.player == null || EntityUtility.mc.level == null) {
            return null;
        }
        BlockPos pos = EntityUtility.mc.player.blockPosition().below().above();
        return EntityUtility.getBlockAt(pos, (Level)EntityUtility.mc.level);
    }

    public static Block getBlockAbovePlayer() {
        if (EntityUtility.mc.player == null || EntityUtility.mc.level == null) {
            return null;
        }
        BlockPos pos = EntityUtility.mc.player.blockPosition().above();
        return EntityUtility.getBlockAt(pos, (Level)EntityUtility.mc.level);
    }

    public static Block getBlockStandingOn(Entity entity) {
        if (entity == null) {
            return null;
        }
        BlockPos pos = entity.blockPosition();
        return EntityUtility.getBlockAt(pos, entity.level());
    }

    public static double getVelocity() {
        return Math.hypot(EntityUtility.mc.player.getDeltaMovement().x, EntityUtility.mc.player.getDeltaMovement().z);
    }

    public static Block getBlockStandingOnPlayer() {
        if (EntityUtility.mc.player == null || EntityUtility.mc.level == null) {
            return null;
        }
        BlockPos pos = EntityUtility.mc.player.blockPosition();
        return EntityUtility.getBlockAt(pos, (Level)EntityUtility.mc.level);
    }

    public static Block getBlockAt(BlockPos pos, Level world) {
        return world.getBlockState(pos).getBlock();
    }

    public static double direction(float rotationYaw, double moveForward, double moveStrafing) {
        if (moveForward < 0.0) {
            rotationYaw += 180.0f;
        }
        float forward = 1.0f;
        if (moveForward < 0.0) {
            forward = -0.5f;
        } else if (moveForward > 0.0) {
            forward = 0.5f;
        }
        if (moveStrafing > 0.0) {
            rotationYaw -= 90.0f * forward;
        }
        if (moveStrafing < 0.0) {
            rotationYaw += 90.0f * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static boolean isInGame() {
        return EntityUtility.mc.player != null && EntityUtility.mc.level != null;
    }

    public static float getHealth(Player ent) {
        if (ent == null) {
            return 0.0f;
        }
        if (!ServerUtility.isServerForHPFix()) {
            return ent.getHealth() + ent.getAbsorptionAmount();
        }
        Objective scoreBoard = ent.level().getScoreboard().getDisplayObjective(DisplaySlot.BELOW_NAME);
        if (scoreBoard != null) {
            ReadOnlyScoreInfo score = ent.level().getScoreboard().getPlayerScoreInfo((ScoreHolder)ent, scoreBoard);
            if (score != null) {
                return (float) score.value();
            }
        }
        return ent.getMaxHealth();
    }

    public static boolean isHoldingWeapon() {
        if (EntityUtility.mc.player == null) {
            return false;
        }
        ItemStack heldStack = EntityUtility.mc.player.getMainHandItem();
        Item heldItem = heldStack.getItem();
        if (heldStack.isEmpty()) {
            return false;
        }
        return heldStack.is(item -> item.is(ItemTags.SWORDS)) || heldItem instanceof AxeItem || heldItem instanceof TridentItem || heldItem instanceof MaceItem;
    }

    @Generated
    private EntityUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setTimer(float timer) {
        EntityUtility.timer = timer;
    }

    @Generated
    public static float getTimer() {
        return timer;
    }
}

