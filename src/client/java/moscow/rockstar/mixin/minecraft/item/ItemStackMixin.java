/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.item;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.FinishEatEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemStack.class})
public abstract class ItemStackMixin {
    @Inject(method={"finishUsingItem"}, at={@At(value="TAIL")})
    private void onFinishUsing(Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof Player) {
            Player player = (Player)user;
            Rockstar.getInstance().getEventManager().triggerEvent(new FinishEatEvent(player, (ItemStack)(Object)this));
        }
    }
}
