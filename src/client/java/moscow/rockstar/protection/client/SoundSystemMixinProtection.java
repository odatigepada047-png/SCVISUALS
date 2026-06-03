/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.sound.SoundEvents
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.protection.client;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.sounds.SoundEngine;

public class SoundSystemMixinProtection {
    private static boolean matches(SoundInstance sound, Identifier eventId) {
        return sound.getIdentifier().equals(eventId);
    }

    public static void playSound(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && removals.getBeacon().isSelected() && (matches(sound, SoundEvents.BEACON_ACTIVATE.location()) || matches(sound, SoundEvents.BEACON_AMBIENT.location()) || matches(sound, SoundEvents.BEACON_POWER_SELECT.location()) || matches(sound, SoundEvents.BEACON_DEACTIVATE.location()))) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
            return;
        }
        if (removals.isEnabled() && removals.getWeatherSound().isSelected() && (matches(sound, SoundEvents.WEATHER_RAIN.location()) || matches(sound, SoundEvents.WEATHER_RAIN_ABOVE.location()) || matches(sound, SoundEvents.LIGHTNING_BOLT_THUNDER.location()))) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
            return;
        }
        if (removals.isEnabled() && removals.getPhantoms().isSelected() && (matches(sound, SoundEvents.PARROT_IMITATE_PHANTOM.location()) || matches(sound, SoundEvents.PHANTOM_AMBIENT.location()) || matches(sound, SoundEvents.PHANTOM_BITE.location()) || matches(sound, SoundEvents.PHANTOM_FLAP.location()) || matches(sound, SoundEvents.PHANTOM_DEATH.location()) || matches(sound, SoundEvents.PHANTOM_HURT.location()) || matches(sound, SoundEvents.PHANTOM_SWOOP.location()))) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }
}
