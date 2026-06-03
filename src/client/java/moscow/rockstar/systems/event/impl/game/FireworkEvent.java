/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.projectile.FireworkRocketEntity
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.systems.event.impl.game;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;

public class FireworkEvent
extends Event {
    private final LivingEntity entity;
    private Vec3 velocity;
    private final FireworkRocketEntity rocketEntity;

    @Generated
    public LivingEntity getEntity() {
        return this.entity;
    }

    @Generated
    public Vec3 getVelocity() {
        return this.velocity;
    }

    @Generated
    public FireworkRocketEntity getRocketEntity() {
        return this.rocketEntity;
    }

    @Generated
    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    @Generated
    public FireworkEvent(LivingEntity entity, Vec3 velocity, FireworkRocketEntity rocketEntity) {
        this.entity = entity;
        this.velocity = velocity;
        this.rocketEntity = rocketEntity;
    }
}

