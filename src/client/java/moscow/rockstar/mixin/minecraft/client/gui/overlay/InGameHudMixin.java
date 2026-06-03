package moscow.rockstar.mixin.minecraft.client.gui.overlay;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.GuiDrawContextHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.scores.Objective;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Gui.class})
public class InGameHudMixin
implements IMinecraft {
    private static final int SATURATION_TINT = 0xFFFFFFFF;
    private static final int SATURATION_Y_OFFSET = 10;
    private static final Identifier FOOD_FULL_ICON = Identifier.withDefaultNamespace("hud/food_full");
    private static final Identifier FOOD_HALF_ICON = Identifier.withDefaultNamespace("hud/food_half");

    @Inject(method={"displayScoreboardSidebar"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderScoreboardSidebarHook(GuiGraphicsExtractor context, Objective objective, CallbackInfo ci) {
        Removals removals;
        if (ServerUtility.isFT() || ServerUtility.isST()) {
            try {
                String title = objective.getDisplayName().getString();
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?:\\u0410\\u043d\\u0430\\u0440\\u0445\\u0438\\u044f|anarchy|an)[#\\s-]*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(title);
                if (matcher.find()) {
                    ServerUtility.ftAn = Integer.parseInt(matcher.group(1));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }           
        }
        if ((removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class)).isEnabled() && removals.getScoreboard().isSelected()) {
            ci.cancel();
        }
    }

    @Inject(method={"extractPortalOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderPortalOverlayHook(GuiGraphicsExtractor context, float nauseaStrength, CallbackInfo ci) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && removals.getPortal().isSelected()) {
            ci.cancel();
        }
    }

    @ModifyArgs(method={"extractCameraOverlays"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/Gui;extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V", ordinal=0))
    private void onRenderPumpkinOverlay(Args args) {
        Removals removals = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
        if (removals.isEnabled() && removals.getPumpkin().isSelected()) {
            args.set(2, (Object)Float.valueOf(0.0f));
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="RETURN")})
    public void onExtractRenderStateReturn(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        GuiDrawContextHolder.capture(context, tickCounter.getGameTimeDeltaPartialTick(false));
        this.renderSaturationBar(context);
    }

    @ModifyArgs(method={"extractRenderState"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/client/renderer/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"), require=0)
    private void moveAirBubblesWhenSaturationVisible(Args args) {
        if (!this.shouldRenderSaturation()) {
            return;
        }
        Object spriteArg = args.get(1);
        if (!(spriteArg instanceof Identifier sprite)) {
            return;
        }
        String path = sprite.getPath();
        if (!path.startsWith("hud/air")) {
            return;
        }
        int y = (Integer)args.get(3);
        args.set(3, y - 10);
    }

    private boolean shouldRenderSaturation() {
        return mc.player != null && !mc.options.hideGui && mc.player.getFoodData().getSaturationLevel() >= 0.001f;
    }

    private void renderSaturationBar(GuiGraphicsExtractor context) {
        if (!this.shouldRenderSaturation()) {
            return;
        }

        float saturation = mc.player.getFoodData().getSaturationLevel();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int barY = screenHeight - 39 - SATURATION_Y_OFFSET;

        for (int i = 0; i < 10; ++i) {
            int iconX = screenWidth / 2 + 91 - i * 8 - 9;
            float iconSaturation = saturation - i * 2;
            if (iconSaturation >= 2.0f) {
                context.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_FULL_ICON, iconX, barY, 9, 9, SATURATION_TINT);
            } else if (iconSaturation > 0.0f) {
                context.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_HALF_ICON, iconX, barY, 9, 9, SATURATION_TINT);
            }
        }
    }
}
