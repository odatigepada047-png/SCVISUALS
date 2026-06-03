package moscow.rockstar.mixin.minecraft.client.gui.components;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.other.HiderUtils;

@Mixin(EditBox.class)
public class EditBoxMixin {
    @ModifyVariable(method = "applyFormat", at = @At("HEAD"), argsOnly = true)
    private String modifyFormatText(String text) {
        HiderUtils hider = Rockstar.getInstance().getModuleManager().getModule(HiderUtils.class);
        if (hider != null && hider.isEnabled()) {
            return hider.patchName(text);
        }
        return text;
    }
}
