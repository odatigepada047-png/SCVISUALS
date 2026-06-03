/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.damage.DamageSource
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.systems.event.impl.game;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

public class EntityDeathEvent
extends Event {
    private final LivingEntity entity;
    private final DamageSource source;

    public EntityDeathEvent(LivingEntity entity, DamageSource source) {
        this.entity = entity;
        this.source = source;
    }

    @Nullable
    public LivingEntity getKillerEntity() {
        return this.entity.getKillCredit();
    }

    @Generated
    public LivingEntity getEntity() {
        return this.entity;
    }

    @Generated
    public DamageSource getSource() {
        return this.source;
    }
}

