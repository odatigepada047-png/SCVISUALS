package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.ui.hud.impl.Cooldowns;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCooldowns.class)
public class ItemCooldownManagerMixin {

    @Inject(method = "addCooldown(Lnet/minecraft/world/item/ItemStack;I)V", at = @At("HEAD"))
    private void onSet(ItemStack stack, int duration, CallbackInfo ci) {
        if (stack != null) {
            Cooldowns.updateRemoteDuration(stack.getItem(), duration);
        }
    }
}
