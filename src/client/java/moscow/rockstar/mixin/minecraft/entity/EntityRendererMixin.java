package moscow.rockstar.mixin.minecraft.entity;

import moscow.rockstar.Rockstar;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityRenderer.class})
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method={"getNameTag"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderLabel(T entity, CallbackInfoReturnable<Component> cir) {
        // Nametags module removed
    }
}

