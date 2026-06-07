/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.ingame.AbstractContainerScreen
 *  com.mojang.blaze3d.platform.InputConstants
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.screen.slot.ContainerInput
 *  net.minecraft.util.collection.NonNullList
 *  org.lwjgl.glfw.GLFW
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.gui.screen;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.event.impl.render.ScreenRenderEvent;
import moscow.rockstar.utility.render.GuiDrawContextHolder;
import moscow.rockstar.systems.event.impl.window.ContainerClickEvent;
import moscow.rockstar.systems.event.impl.window.ContainerReleaseEvent;
import moscow.rockstar.systems.modules.modules.player.InvUtils;
import moscow.rockstar.systems.modules.modules.visuals.Beatifuly;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.core.NonNullList;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractContainerScreen.class})
public abstract class HandledScreenMixin
implements IMinecraft {
    @Unique
    private final Timer timer = new Timer();

    @Unique
    private long rockstar$openTime = 0L;

    @Shadow
    protected abstract boolean isHovering(Slot var1, double var2, double var4);

    @Shadow
    protected abstract void slotClicked(Slot var1, int var2, int var3, ContainerInput var4);

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.rockstar$openTime = System.currentTimeMillis();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onRenderHead(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Beatifuly.isContainerAnimEnabled()) {
            long elapsed = System.currentTimeMillis() - this.rockstar$openTime;
            float t = Math.min(1.0f, elapsed / 250.0f);
            float ease = t * (2.0f - t);
            float scale = 0.8f + 0.2f * ease;
            float centerX = (float) context.guiWidth() / 2.0f;
            float centerY = (float) context.guiHeight() / 2.0f;
            context.pose().pushMatrix();
            context.pose().translate(centerX, centerY);
            context.pose().scale(scale, scale);
            context.pose().translate(-centerX, -centerY);
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void onRenderReturn(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (Beatifuly.isContainerAnimEnabled()) {
            context.pose().popMatrix();
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GuiDrawContextHolder.capture(context, delta);
        try {
            if (HandledScreenMixin.mc.player != null && HandledScreenMixin.mc.player.containerMenu != null) {
                NonNullList<Slot> slots = HandledScreenMixin.mc.player.containerMenu.slots;
                InvUtils invUtils = Rockstar.getInstance().getModuleManager().getModule(InvUtils.class);
                if (invUtils != null && invUtils.isEnabled()) {
                    for (Slot slot : slots) {
                        if (slot != null && this.isHovering(slot, mouseX, mouseY) && slot.isActive()) {
                            if (invUtils.getScroller().isSelected() && this.timer.finished((long)invUtils.getScrollDelay().getCurrentValue()) && moscow.rockstar.utility.game.KeyUtility.isKeyPressed(340) && GLFW.glfwGetMouseButton(mc.getWindow().handle(), 0) == 1) {
                                this.slotClicked(slot, slot.index, 0, ContainerInput.QUICK_MOVE);
                                this.timer.reset();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")})
    private void onMouseClick(MouseButtonEvent event, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        Rockstar.getInstance().getEventManager().triggerEvent(new ContainerClickEvent((float)event.x(), (float)event.y(), event.button()));
    }

    @Inject(method={"mouseReleased"}, at={@At(value="HEAD")})
    public void mouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        Rockstar.getInstance().getEventManager().triggerEvent(new ContainerReleaseEvent((float)event.x(), (float)event.y(), event.button()));
    }
}
