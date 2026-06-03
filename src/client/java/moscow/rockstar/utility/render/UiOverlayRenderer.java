package moscow.rockstar.utility.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.ui.menu.MenuScreen;
import moscow.rockstar.ui.mainmenu.CustomTitleScreen;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.systems.event.impl.render.ChatRenderEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.PostHudRenderEvent;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.event.impl.render.ScreenRenderEvent;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.batching.Batching;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public final class UiOverlayRenderer implements IMinecraft {
    public static boolean blurRequestedThisFrame = false;
    private static boolean hadBlurRequestsLastFrame = false;
    private static Removals removalsCache;

    private static Removals getRemovals() {
        if (removalsCache == null) {
            try {
                removalsCache = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
            } catch (Exception e) {}
        }
        return removalsCache;
    }

    private UiOverlayRenderer() {
    }

    /** In-world HUD only; title/menu island is rendered by {@link CustomTitleScreen} explicitly. */
    public static boolean shouldRenderIngameHud() {
        Screen screen = mc.screen;
        if (screen instanceof CustomScreen) {
            return false;
        }
        if (!EntityUtility.isInGame()) {
            if (screen == null) {
                return false;
            }
            String name = screen.getClass().getSimpleName().toLowerCase();
            return screen instanceof ConnectScreen || name.contains("loading") || name.contains("connect");
        }
        return true;
    }

    private static boolean isMenuScreenOpen() {
        return mc.screen instanceof MenuScreen;
    }

    public static void flushDraws(GpuBufferSlice fogBuffer) {
        GuiGraphicsExtractor context = GuiDrawContextHolder.getExtractor();
        if (context == null) {
            return;
        }

        float delta = GuiDrawContextHolder.getTickDelta();
        ShaderColorHelper.reset();
        Batching.clearActive();
        UiRenderMatrices.begin();
        boolean prevDepthOverride = MeshDrawHelper.disableDepthOverride;
        MeshDrawHelper.disableDepthOverride = true;
        try {
            CustomRenderTarget.clearActiveBinding();
            if (DrawUtility.blurProgram != null && mc.level != null) {
                if (isMenuScreenOpen() || hadBlurRequestsLastFrame) {
                    DrawUtility.blurProgram.draw(isMenuScreenOpen());
                }
            }

            CustomDrawContext customContext = CustomDrawContext.of(context);
            Screen screen = mc.screen;

            Rockstar.getInstance().getEventManager().triggerEvent(new PreHudRenderEvent(customContext, delta));

            Removals removals = getRemovals();
            if (removals.isEnabled() && removals.getWaterBlur().isSelected() && mc.player != null && mc.player.isUnderWater()) {
                customContext.drawBlurredRect(
                        0.0f,
                        0.0f,
                        mc.getWindow().getGuiScaledWidth(),
                        mc.getWindow().getGuiScaledHeight(),
                        35.0f,
                        BorderRadius.all(0.0f),
                        new ColorRGBA(100.0f, 165.0f, 225.0f, 70.0f)
                );
            }

            if (shouldRenderIngameHud()) {
                Rockstar.getInstance().getEventManager().triggerEvent(new HudRenderEvent(customContext, delta));
            }

            if (shouldRenderIngameHud()) {
                Rockstar.getInstance().getEventManager().triggerEvent(new PostHudRenderEvent(customContext, delta));
            }

            if (screen instanceof ChatScreen) {
                Rockstar.getInstance().getEventManager().triggerEvent(new ChatRenderEvent(customContext, delta));
            }


            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                Rockstar.getInstance().getEventManager().triggerEvent(new ScreenRenderEvent(customContext, delta));
            }

            if (screen instanceof CustomScreen customScreen) {
                customScreen.renderCustomUi(
                        context,
                        GuiDrawContextHolder.getMouseX(),
                        GuiDrawContextHolder.getMouseY(),
                        delta
                );
            }
        } finally {
            MeshDrawHelper.disableDepthOverride = prevDepthOverride;
            hadBlurRequestsLastFrame = blurRequestedThisFrame;
            blurRequestedThisFrame = false;
            Batching.clearActive();
            ShaderColorHelper.reset();
            CustomRenderTarget.clearActiveBinding();
            UiRenderMatrices.end();
            GuiDrawContextHolder.clear();
            MeshDrawHelper.cleanupBuffers();
            moscow.rockstar.utility.render.ScissorUtility.clear();
        }
    }
}
