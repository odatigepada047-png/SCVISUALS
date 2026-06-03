package moscow.rockstar.utility.game;

import moscow.rockstar.mixin.minecraft.client.input.InputAccessor;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.phys.Vec2;

public final class ClientInputHelper {
    private ClientInputHelper() {
    }

    private static InputAccessor asAccessor(ClientInput input) {
        return (InputAccessor) input;
    }

    public static float getMovementForward(ClientInput input) {
        return asAccessor(input).getMoveVector().y;
    }

    public static float getMovementSideways(ClientInput input) {
        return asAccessor(input).getMoveVector().x;
    }

    public static void setMovementForward(ClientInput input, float forward) {
        InputAccessor accessor = asAccessor(input);
        Vec2 vector = accessor.getMoveVector();
        accessor.setMoveVector(new Vec2(vector.x, forward));
    }

    public static void setMovementSideways(ClientInput input, float sideways) {
        InputAccessor accessor = asAccessor(input);
        Vec2 vector = accessor.getMoveVector();
        accessor.setMoveVector(new Vec2(sideways, vector.y));
    }
}
