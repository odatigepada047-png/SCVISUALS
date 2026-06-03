/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.gui.screens.Screen
 */
package moscow.rockstar.systems.event.impl.game;

import lombok.Generated;
import moscow.rockstar.systems.event.Event;
import net.minecraft.client.gui.screens.Screen;

public class CloseScreenEvent
extends Event {
    private final Screen screen;

    @Generated
    public Screen getScreen() {
        return this.screen;
    }

    @Generated
    public CloseScreenEvent(Screen screen) {
        this.screen = screen;
    }
}

