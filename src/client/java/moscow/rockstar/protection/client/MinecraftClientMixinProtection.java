/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.protection.client;

import moscow.rockstar.Rockstar;
import net.minecraft.client.Minecraft;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class MinecraftClientMixinProtection {
    @VMProtect(type=VMProtectType.MUTATION)
    public static void init() {
        Rockstar.INSTANCE.initialize();
    }

    @VMProtect(type=VMProtectType.MUTATION)
    public static void shutdown() {
        Rockstar.INSTANCE.shutdown();
    }

    public static void updateTitle(Minecraft instance, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (Rockstar.INSTANCE.isPanic()) {
            return;
        }
        instance.getWindow().setTitle("%s %s (%s)".formatted("Sound Cloud Visuals", "2.0", "Beta"));
        ci.cancel();
    }
}


