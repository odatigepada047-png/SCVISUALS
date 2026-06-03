/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyboardHandler
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.Screen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={KeyboardHandler.class})
public class KeyboardMixin
implements IMinecraft {
    @Inject(method={"keyPress"}, at={@At(value="HEAD")})
    private void triggerKeyEvent(long window, int action, KeyEvent event, CallbackInfo ci) {
        int key = event.key();
        if (key == -1) {
            return;
        }
        Rockstar.getInstance().getEventManager().triggerEvent(new KeyPressEvent(action, key));
        if (KeyboardMixin.mc.screen != null) {
            return;
        }
        if (key == 46 && action == 1) {
            mc.setScreen((Screen)new ChatScreen("", false));
        }
    }
}

