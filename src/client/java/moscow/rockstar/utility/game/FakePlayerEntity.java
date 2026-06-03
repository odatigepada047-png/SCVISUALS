/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.network.RemotePlayer
 *  net.minecraft.client.world.ClientLevel
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.Entity$RemovalReason
 */
package moscow.rockstar.utility.game;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public class FakePlayerEntity
extends RemotePlayer {
    public FakePlayerEntity(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    public void spawn() {
        this.unsetRemoved();
        if (this.level() instanceof ClientLevel clientLevel) {
            clientLevel.addEntity((Entity)this);
        }
    }

    public void remove() {
        if (this.level() instanceof ClientLevel clientLevel) {
            clientLevel.removeEntity(this.getId(), Entity.RemovalReason.DISCARDED);
        }
    }

    public void takeKnockback(double strength, double x, double z) {
    }
}
