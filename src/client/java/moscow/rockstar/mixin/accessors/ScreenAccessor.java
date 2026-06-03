/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Renderable
 *  net.minecraft.client.gui.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package moscow.rockstar.mixin.accessors;

import java.util.List;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Screen.class})
public interface ScreenAccessor {
    @Accessor(value="children")
    public List<GuiEventListener> getChildren();

    @Invoker(value="addRenderableWidget")
    <T extends GuiEventListener & Renderable & net.minecraft.client.gui.narration.NarratableEntry> T invokeAddDrawableChild(T widget);
}

