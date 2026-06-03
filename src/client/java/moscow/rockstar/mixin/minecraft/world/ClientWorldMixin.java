package moscow.rockstar.mixin.minecraft.world;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={ClientLevel.class})
public abstract class ClientWorldMixin {
    // XRay block-update hook removed; method renamed in 26.1
}
