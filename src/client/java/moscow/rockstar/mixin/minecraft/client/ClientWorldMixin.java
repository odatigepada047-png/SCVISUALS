/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
//  *  net.minecraft.client.renderer.DimensionEffects
//  *  net.minecraft.client.renderer.DimensionEffects$End
 *  net.minecraft.client.world.ClientLevel
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.Ambience;
// import net.minecraft.client.renderer.DimensionEffects;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = { ClientLevel.class })
public class ClientWorldMixin {
//     @Unique
//     private final DimensionEffects endSky = new DimensionEffects.End();

//     @Inject(method = { "getDimensionEffects" }, at = { @At(value = "HEAD") }, cancellable = true)
//     private void onGetSkyProperties(CallbackInfoReturnable<DimensionEffects> info) {
//         if (Rockstar.getInstance().getModuleManager().getModule(Ambience.class).isEnabled()
//                 && Rockstar.getInstance().getModuleManager().getModule(Ambience.class).getEndSky().isEnabled()) {
//             info.setReturnValue(this.endSky);
//         }
//     }
}
