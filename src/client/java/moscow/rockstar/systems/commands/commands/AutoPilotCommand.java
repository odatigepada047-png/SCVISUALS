/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.c2s.play.ServerboundUseItemPacket
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.InteractionHand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3
 *  net.minecraft.world.Heightmap$Type
 */
package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.levelgen.Heightmap;

public class AutoPilotCommand
implements IMinecraft {
    private final Timer timer = new Timer();
    private Vec3 target;
    private boolean active;
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (!this.active || AutoPilotCommand.mc.player == null || AutoPilotCommand.mc.level == null) {
            return;
        }
        Vec3 currentTarget = this.target;
        if (currentTarget == null) {
            return;
        }
        if (AutoPilotCommand.mc.player.isFallFlying()) {
            Vec3 vec = currentTarget.subtract(AutoPilotCommand.mc.player.getEyePosition(1.0f)).normalize();
            float rawYaw = (float)Math.toDegrees(Math.atan2(-vec.x, vec.z));
            int highestY = (int)AutoPilotCommand.mc.player.getY();
            int highestX = (int)currentTarget.x;
            int highestZ = (int)currentTarget.z;
            int iterations = 80;
            for (int x = -iterations; x < iterations; ++x) {
                for (int z = -iterations; z < iterations; ++z) {
                    int height = AutoPilotCommand.mc.level.getHeight(Heightmap.Types.WORLD_SURFACE, (int)(AutoPilotCommand.mc.player.getX() + (double)x), (int)(AutoPilotCommand.mc.player.getZ() + (double)z)) + 5;
                    if (height <= highestY || !((double)height > AutoPilotCommand.mc.player.getY())) continue;
                    highestY = height;
                    highestX = (int)(AutoPilotCommand.mc.player.getX() + (double)x);
                    highestZ = (int)(AutoPilotCommand.mc.player.getZ() + (double)z);
                }
            }
            AutoPilotCommand.mc.options.keySprint.setDown(true);
            AutoPilotCommand.mc.options.keyUp.setDown(true);
            if (EntityUtility.getVelocity() < 1.46 && this.timer.finished(1700L)) {
                AutoPilotCommand.mc.gameMode.useItem(AutoPilotCommand.mc.player, net.minecraft.world.InteractionHand.OFF_HAND);
                this.timer.reset();
            }
            Vec3 vecHeight = new Vec3((double)highestX, (double)(highestY + 23), (double)highestZ).subtract(AutoPilotCommand.mc.player.getEyePosition(1.0f)).normalize();
            float rawPitch = (float)Math.clamp(Math.toDegrees(Math.asin(-vecHeight.y)), -89.0, 89.0);
            int i = AutoPilotCommand.mc.level.getHeight(Heightmap.Types.WORLD_SURFACE, (int)AutoPilotCommand.mc.player.getX(), (int)AutoPilotCommand.mc.player.getZ()) - 10;
            while ((double)i < AutoPilotCommand.mc.player.getY()) {
                if (!AutoPilotCommand.mc.level.getBlockState(new BlockPos((int)AutoPilotCommand.mc.player.getX(), i, (int)AutoPilotCommand.mc.player.getZ())).getFluidState().isEmpty() && AutoPilotCommand.mc.player.getY() - (double)i < 5.0) {
                    rawPitch -= 11.0f;
                    break;
                }
                ++i;
            }
            if (AutoPilotCommand.mc.player.position().distanceToSqr(currentTarget) < 20.0) {
                MessageUtility.info(Component.literal((String)Localizator.translate("commands.autopilot.stopped")));
                this.stopAutoPilot();
            }
            RotationHandler rotationHandler = Rockstar.INSTANCE.getRotationHandler();
            Rotation targetRotation = new Rotation(rawYaw, rawPitch + 13.0f);
            rotationHandler.rotate(targetRotation);
        }
    };

    private void initialize() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    public AutoPilotCommand() {
        this.initialize();
    }

    public Command command() {
        return CommandBuilder.begin("autopilot").aliases("ap", "pilot", "\u0430\u0432\u0442\u043e\u043f\u0438\u043b\u043e\u0442", "\u043f\u0438\u043b\u043e\u0442").desc("commands.autopilot.description").param("x", p -> p.optional().validator(AutoPilotCommand::verifyCoordinate)).param("y", p -> p.optional().validator(AutoPilotCommand::verifyCoordinate)).param("z", p -> p.optional().validator(AutoPilotCommand::verifyCoordinate)).handler(this::handle).build();
    }

    private static ValidationResult verifyCoordinate(String input) {
        try {
            Double.parseDouble(input);
            return ValidationResult.ok(input);
        }
        catch (NumberFormatException e) {
            return ValidationResult.error(Localizator.translate("commands.autopilot.invalid"));
        }
    }

    private void handle(CommandContext ctx) {
        String x = (String)ctx.arguments().get(0);
        String y = (String)ctx.arguments().get(1);
        String z = (String)ctx.arguments().get(2);
        if (x == null || y == null || z == null) {
            if (this.active) {
                this.stopAutoPilot();
                MessageUtility.info(Component.literal((String)Localizator.translate("commands.autopilot.stopping")));
            } else {
                MessageUtility.error(Component.literal((String)Localizator.translate("commands.autopilot.not_active")));
            }
            return;
        }
        try {
            this.target = new Vec3(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
            this.active = true;
            MessageUtility.info(Component.literal((String)Localizator.translate("commands.autopilot.start", this.target.x, this.target.y, this.target.z)));
        }
        catch (NumberFormatException e) {
            MessageUtility.error(Component.literal((String)Localizator.translate("commands.autopilot.invalid")));
        }
    }

    private void stopAutoPilot() {
        this.active = false;
        this.target = null;
        Rockstar.getInstance().getEventManager().unsubscribe(this);
        AutoPilotCommand.mc.options.keySprint.setDown(false);
        AutoPilotCommand.mc.options.keyUp.setDown(false);
    }
}

