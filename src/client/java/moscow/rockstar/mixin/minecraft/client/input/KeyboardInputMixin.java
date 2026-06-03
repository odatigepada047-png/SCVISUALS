/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.client.input.ClientInput
 *  net.minecraft.client.input.KeyboardInput
 *  net.minecraft.util.Input
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.client.input;

import moscow.rockstar.Rockstar;
import moscow.rockstar.mixin.minecraft.client.input.InputAccessor;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.utility.game.ClientInputHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value=EnvType.CLIENT)
@Mixin(value={KeyboardInput.class})
public abstract class KeyboardInputMixin {
    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void onTick(CallbackInfo ci) {
        ClientInput input = (ClientInput)(Object)this;
        InputAccessor accessor = (InputAccessor)input;
        Input keys = accessor.getInput();
        float movementForward = ClientInputHelper.getMovementForward(input);
        float movementSideways = ClientInputHelper.getMovementSideways(input);
        boolean jumping = accessor.getInput().jump();
        boolean sneaking = accessor.getInput().shift();
        boolean sprint = accessor.getInput().sprint();
        InputEvent event = new InputEvent(movementForward, movementSideways, jumping, sneaking, sprint);
        Rockstar.getInstance().getEventManager().triggerEvent(event);
        ClientInputHelper.setMovementForward(input, event.getForward());
        ClientInputHelper.setMovementSideways(input, event.getStrafe());
        boolean forwardKey = event.getForward() > 0.0f;
        boolean backwardKey = event.getForward() < 0.0f;
        boolean leftKey = event.getStrafe() > 0.0f;
        boolean rightKey = event.getStrafe() < 0.0f;
        accessor.setInput(new Input(forwardKey, backwardKey, leftKey, rightKey, event.isJump(), event.isSneak(), event.isSprint()));
    }
}
