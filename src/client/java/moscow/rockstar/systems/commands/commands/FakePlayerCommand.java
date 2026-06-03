/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.network.RemotePlayer
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.MoverType
 *  net.minecraft.entity.effect.MobEffectInstance
 *  net.minecraft.entity.effect.MobEffects
 *  net.minecraft.entity.player.Player
 *  net.minecraft.network.listener.ClientGamePacketListener
 *  net.minecraft.network.packet.s2c.play.ClientboundEntityEventPacket
 *  net.minecraft.sound.SoundSource
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.util.InteractionHand
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.systems.commands.commands;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.AttackEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public class FakePlayerCommand
implements IMinecraft {
    private RemotePlayer fakePlayer;
    private float moveForward = 0.0f;
    private float moveStrafe = 0.0f;
    private final EventListener<AttackEvent> onAttackEvent = event -> {
        if (this.fakePlayer != null && event.getEntity() == this.fakePlayer && this.fakePlayer.hurtTime == 0) {
            FakePlayerCommand.mc.level.playSound((Player)FakePlayerCommand.mc.player, this.fakePlayer.getX(), this.fakePlayer.getY(), this.fakePlayer.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 1.0f);
            if (FakePlayerCommand.mc.player.fallDistance > 0.0f) {
                FakePlayerCommand.mc.level.playSound((Player)FakePlayerCommand.mc.player, this.fakePlayer.getX(), this.fakePlayer.getY(), this.fakePlayer.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                FakePlayerCommand.mc.level.playSound((Player)FakePlayerCommand.mc.player, this.fakePlayer.getX(), this.fakePlayer.getY(), this.fakePlayer.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            this.fakePlayer.hurtClient(FakePlayerCommand.mc.level.damageSources().generic());
            this.fakePlayer.setHealth(this.fakePlayer.getHealth() + this.fakePlayer.getAbsorptionAmount() - 1.0f);
            if (this.fakePlayer.isDeadOrDying()) {
                this.fakePlayer.setHealth(10.0f);
                new ClientboundEntityEventPacket((Entity)this.fakePlayer, (byte)35).handle(FakePlayerCommand.mc.player.connection);
            }
        }
    };
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (this.fakePlayer == null || FakePlayerCommand.mc.screen != null) {
            return;
        }
        int key = event.getKey();
        int action = event.getAction();
        if (key == 265) {
            this.moveForward = action == 1 || action == 2 ? 1.0f : 0.0f;
        } else if (key == 264) {
            this.moveForward = action == 1 || action == 2 ? -1.0f : 0.0f;
        } else if (key == 263) {
            this.moveStrafe = action == 1 || action == 2 ? 1.0f : 0.0f;
        } else if (key == 262) {
            this.moveStrafe = action == 1 || action == 2 ? -1.0f : 0.0f;
        }
    };
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = event -> {
        if (this.fakePlayer == null || FakePlayerCommand.mc.player == null) {
            return;
        }
        if (this.moveForward != 0.0f || this.moveStrafe != 0.0f) {
            float yaw = FakePlayerCommand.mc.player.getYRot();
            double speed = 0.2;
            double motionX = (double)this.moveStrafe * Math.cos(Math.toRadians(yaw)) - (double)this.moveForward * Math.sin(Math.toRadians(yaw));
            double motionZ = (double)this.moveForward * Math.cos(Math.toRadians(yaw)) + (double)this.moveStrafe * Math.sin(Math.toRadians(yaw));
            Vec3 velocity = new Vec3(motionX * speed, this.fakePlayer.getDeltaMovement().y, motionZ * speed);
            this.fakePlayer.setDeltaMovement(velocity);
            this.fakePlayer.move(MoverType.SELF, velocity);
            this.fakePlayer.setSprinting(true);
        } else {
            this.fakePlayer.setSprinting(false);
            this.fakePlayer.setDeltaMovement(0.0, this.fakePlayer.getDeltaMovement().y, 0.0);
        }
    };

    public FakePlayerCommand() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    public Command command() {
        return CommandBuilder.begin("fakeplayer").aliases("fp").desc("commands.fakeplayer.description").param("action", p -> p.literal("add", "del")).handler(this::handle).build();
    }

    private void handle(CommandContext ctx) {
        String action = (String)ctx.arguments().getFirst();
        switch (action.toLowerCase()) {
            case "add": {
                this.add();
                break;
            }
            case "del": {
                this.del();
            }
        }
    }

    public void add() {
        if (this.fakePlayer != null) {
            this.fakePlayer.discard();
            this.fakePlayer = null;
        }
        this.fakePlayer = new RemotePlayer(FakePlayerCommand.mc.level, new GameProfile(UUID.fromString("66123666-6666-6666-6666-666666666600"), "FakePlayer"));
        this.fakePlayer.copyPosition(FakePlayerCommand.mc.player);
        this.fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, FakePlayerCommand.mc.player.getMainHandItem().copy());
        this.fakePlayer.setItemInHand(InteractionHand.OFF_HAND, FakePlayerCommand.mc.player.getOffhandItem().copy());
        this.fakePlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 9999, 2));
        this.fakePlayer.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 9999, 4));
        this.fakePlayer.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 9999, 1));
        FakePlayerCommand.mc.level.addEntity((Entity)this.fakePlayer);
        Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.SUCCESS, Localizator.translate("commands.fakeplayer.success"), Localizator.translate("commands.fakeplayer.added"));
    }

    public void del() {
        if (this.fakePlayer == null) {
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.ERROR, Localizator.translate("commands.fakeplayer.error"), Localizator.translate("commands.fakeplayer.not_exists"));
            return;
        }
        this.fakePlayer.discard();
        this.fakePlayer = null;
        Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.SUCCESS, Localizator.translate("commands.fakeplayer.success"), Localizator.translate("commands.fakeplayer.removed"));
    }
}
