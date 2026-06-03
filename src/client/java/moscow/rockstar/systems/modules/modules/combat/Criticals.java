/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ServerboundMovePlayerPacket$Full
 */
package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.InternalAttackEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationHandler;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

@ModuleInfo(name="Criticals", category=ModuleCategory.COMBAT)
public class Criticals
extends BaseModule {
    private int airTicks;
    private final EventListener<InternalAttackEvent> onAttack = event -> {
        if (Criticals.mc.player.isInWater()) {
            return;
        }
        if (!Criticals.mc.player.onGround() && Criticals.mc.player.fallDistance == 0.0f) {
            RotationHandler rotationHandler = Rockstar.getInstance().getRotationHandler();
            Rotation rot = rotationHandler.isIdling() ? rotationHandler.getPlayerRotation() : rotationHandler.getCurrentRotation();
            rot = new Rotation(rot.getYRot() + MathUtility.random(-1.0, 1.0), rot.getXRot() + MathUtility.random(-1.0, 1.0));
            rot = RotationMath.correctRotation(rot);
            Criticals.mc.player.fallDistance = MathUtility.random(1.0E-5f, 1.0E-4f);
            Criticals.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.PosRot(Criticals.mc.player.getX(), Criticals.mc.player.getY() - (double)Criticals.mc.player.fallDistance, Criticals.mc.player.getZ(), rot.getYRot(), rot.getXRot(), Criticals.mc.player.onGround(), Criticals.mc.player.horizontalCollision));
        }
    };

    @Override
    public void tick() {
        this.airTicks = Criticals.mc.player.onGround() ? 0 : ++this.airTicks;
    }

    public boolean canCritical() {
        return this.isEnabled() && Criticals.mc.player.fallDistance <= 0.0f && !Criticals.mc.player.onGround();
    }
}

