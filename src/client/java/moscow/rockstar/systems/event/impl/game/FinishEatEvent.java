/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package moscow.rockstar.systems.event.impl.game;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FinishEatEvent
extends Event {
    private final Player user;
    private final ItemStack stack;

    @Generated
    public Player getUser() {
        return this.user;
    }

    @Generated
    public ItemStack getStack() {
        return this.stack;
    }

    @Generated
    public FinishEatEvent(Player user, ItemStack stack) {
        this.user = user;
        this.stack = stack;
    }
}

