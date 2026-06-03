package moscow.rockstar.mixin.minecraft.client.gui;

import moscow.rockstar.systems.modules.modules.other.HiderUtils;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.client.gui.Gui$1DisplayEntry")
public class GuiDisplayEntryMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static Component modifyName(Component name) {
        return HiderUtils.patchComponent(name);
    }
}
