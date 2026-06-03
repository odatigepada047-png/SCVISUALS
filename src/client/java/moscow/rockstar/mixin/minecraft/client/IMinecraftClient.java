package moscow.rockstar.mixin.minecraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Minecraft.class})
public interface IMinecraftClient {
    @Invoker(value="startUseItem")
    void idoItemUse();

    @Accessor(value="rightClickDelay")
    void setUseCooldown(int value);

    @Accessor(value="user")
    void setUser(User user);
}
