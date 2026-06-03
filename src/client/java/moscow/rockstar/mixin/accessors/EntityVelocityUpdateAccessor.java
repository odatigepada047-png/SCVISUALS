package moscow.rockstar.mixin.accessors;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientboundSetEntityMotionPacket.class})
public interface EntityVelocityUpdateAccessor {
    @Mutable
    @Accessor(value="movement")
    public void setMovement(Vec3 var1);
}
