/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.TextVisitFactory
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package moscow.rockstar.mixin.minecraft.text;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.other.HiderUtils;
import net.minecraft.util.StringDecomposer;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={StringDecomposer.class})
public class TextVisitFactoryMixin {
    
    @ModifyVariable(method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z", at = @At("HEAD"), argsOnly = true)
    private static String patchName1(String text) {
        return patchText(text);
    }

    @ModifyVariable(method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z", at = @At("HEAD"), argsOnly = true)
    private static String patchName2(String text) {
        return patchText(text);
    }

    private static String patchText(String text) {
        var moduleManager = Rockstar.getInstance().getModuleManager();
        if (moduleManager == null) {
            return text;
        }
        HiderUtils hider = moduleManager.getModule(HiderUtils.class);
        if (hider != null && hider.isEnabled()) {
            return hider.patchName(text);
        }
        return text;
    }
}

