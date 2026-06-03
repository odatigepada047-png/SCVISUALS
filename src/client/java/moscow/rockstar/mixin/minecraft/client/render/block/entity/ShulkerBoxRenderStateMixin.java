package moscow.rockstar.mixin.minecraft.client.render.block.entity;

import moscow.rockstar.utility.interfaces.IShulkerRenderState;
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ShulkerBoxRenderState.class)
public class ShulkerBoxRenderStateMixin implements IShulkerRenderState {
    @Unique
    private boolean rockstar$highlight;
    @Unique
    private float rockstar$scale = 1.0f;
    @Unique
    private int rockstar$color = -1;
    @Unique
    private boolean rockstar$useChams;

    @Override
    public boolean rockstar$shouldHighlight() {
        return this.rockstar$highlight;
    }

    @Override
    public void rockstar$setHighlight(boolean highlight) {
        this.rockstar$highlight = highlight;
    }

    @Override
    public float rockstar$getScale() {
        return this.rockstar$scale;
    }

    @Override
    public void rockstar$setScale(float scale) {
        this.rockstar$scale = scale;
    }

    @Override
    public int rockstar$getColor() {
        return this.rockstar$color;
    }

    @Override
    public void rockstar$setColor(int color) {
        this.rockstar$color = color;
    }

    @Override
    public boolean rockstar$shouldUseChams() {
        return this.rockstar$useChams;
    }

    @Override
    public void rockstar$setUseChams(boolean useChams) {
        this.rockstar$useChams = useChams;
    }
}
