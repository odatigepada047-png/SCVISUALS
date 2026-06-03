package moscow.rockstar.mixin.accessors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LoadingOverlay.class})
public interface SplashOverlayAccessor {
    @Accessor(value="reload")
    public ReloadInstance getReload();

    @Accessor(value="minecraft")
    public Minecraft getClient();

    @Accessor(value="fadeOutStart")
    public long getReloadCompleteTime();

    @Accessor(value="fadeOutStart")
    public void setReloadCompleteTime(long var1);
}
