/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 *  net.minecraft.util.math.Vec2
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.ui.hud;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.ChatRenderEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.window.ChatClickEvent;
import moscow.rockstar.systems.event.impl.window.ChatKeyPressEvent;
import moscow.rockstar.systems.event.impl.window.ChatReleaseEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.ui.components.animated.AnimatedText;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.hud.Grid;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.hud.HudHistoryManager;
import moscow.rockstar.ui.hud.HudList;
import moscow.rockstar.ui.hud.impl.ArmorHud;
import moscow.rockstar.ui.hud.impl.Cooldowns;
import moscow.rockstar.ui.hud.impl.Effects;
import moscow.rockstar.ui.hud.impl.KeyBinds;
import moscow.rockstar.ui.hud.impl.TargetHud;
import moscow.rockstar.ui.hud.impl.TrapTimeHud;
import moscow.rockstar.ui.hud.impl.island.DynamicIsland;
import moscow.rockstar.ui.hud.inline.impl.PlayerElement;
import moscow.rockstar.ui.hud.inline.impl.WorldElement;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.cursor.CursorType;
import moscow.rockstar.utility.game.cursor.CursorUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.UiOverlayRenderer;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;

public class Hud
implements IMinecraft,
IScaledResolution {
    private final List<HudElement> elements = new ArrayList<HudElement>();
    private final List<Popup> popups = new ArrayList<Popup>();
    public DynamicIsland island;
    private final HudHistoryManager historyManager = new HudHistoryManager();
    private final Grid grid = new Grid();
    private String desc = "";
    private AnimatedText descText;
    private final Timer timer = new Timer();
    private final moscow.rockstar.utility.animation.base.Animation hintAnimation = new moscow.rockstar.utility.animation.base.Animation(300L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final EventListener<HudRenderEvent> onHud = event -> {
        if (!UiOverlayRenderer.shouldRenderIngameHud()) {
            return;
        }
        UIContext context = UIContext.of(event.getContext(), Hud.mc.screen == null ? -1 : (int)GuiUtility.getMouse().x, Hud.mc.screen == null ? -1 : (int)GuiUtility.getMouse().y, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        if (this.descText == null) {
            this.descText = new AnimatedText(Fonts.REGULAR.getFont(10.0f), 10.0f, 300L, Easing.BAKEK).centered();
        }
        this.desc = "";
        this.grid.draw(context);
        this.grid.update();
        for (HudElement element : this.elements) {
            float prevAlpha = moscow.rockstar.utility.render.ShaderColorHelper.getAlpha();
            int prevScissorStackSize = moscow.rockstar.utility.render.ScissorUtility.getStackSize();
            try {
                element.render(context);
                if (element.isShowing() && element.getSelecting().getValue() > 0.0f) {
                    float anim = element.getAnimation().getValue() * element.getVisible().getValue();
                    float scale = 0.5f + anim * 0.5f - 0.05f * element.getSelecting().getValue();
                    element.getLoadingAnim().setDuration(1500L);
                    element.getLoadingAnim().update(1.0f);
                    if (element.getLoadingAnim().getValue() == 1.0f) {
                        element.getLoadingAnim().setValue(0.0f);
                    }
                    RenderUtility.scale(context.pose(), element.x + element.getWidth() / 2.0f, element.y + element.getHeight() / 2.0f, scale);
                    context.drawLoadingRect(element.x, element.y, element.getWidth(), element instanceof HudList ? Math.max(20.0f, element.getHeight()) : element.getHeight(), element.getLoadingAnim().getValue() * 2.2f - 0.5f, BorderRadius.all(element instanceof DynamicIsland ? 7.0f : 6.0f), ColorRGBA.WHITE.withAlpha(100.0f * element.getSelecting().getRGB()));
                    RenderUtility.end(context.pose());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                moscow.rockstar.utility.render.ShaderColorHelper.setShaderColor(1.0f, 1.0f, 1.0f, prevAlpha);
                while (moscow.rockstar.utility.render.ScissorUtility.getStackSize() > prevScissorStackSize) {
                    moscow.rockstar.utility.render.ScissorUtility.pop();
                }
            }
        }
        this.descText.pos(sr.getGuiScaledWidth() / 2.0f, 30.0f);
        if (!this.desc.contains(".description")) {
            this.descText.update(this.desc);
            this.descText.render(context);
        }

        // Подсказка про Shift
        boolean isDraggingAny = this.elements.stream().anyMatch(HudElement::isDragging);
        this.hintAnimation.update(isDraggingAny ? 1.0f : 0.0f);
        if (this.hintAnimation.getRGB() > 0.0f) {
            String hint = "Зажмите Shift для свободного перемещения";
            float hintX = sr.getGuiScaledWidth() / 2.0f;
            float hintY = sr.getGuiScaledHeight() - 65.0f;
            float alpha = this.hintAnimation.getRGB();
            
            context.drawText(Fonts.MEDIUM.getFont(8.0f), hint, hintX - Fonts.MEDIUM.getFont(8.0f).width(hint) / 2.0f, hintY, ColorRGBA.WHITE.withAlpha(255.0f * alpha));
        }
        if (!(Hud.mc.screen instanceof ChatScreen)) {
            if (!this.popups.isEmpty()) {
                this.popups.clear();
            }
            CursorUtility.set(CursorType.DEFAULT);
            for (HudElement element : this.elements) {
                element.setSelect(false);
                if (element.isDragging()) {
                    element.setDragging(false);
                    if (element.x != element.getStartDragX() || element.y != element.getStartDragY()) {
                        Rockstar.getInstance().getHud().getHistoryManager().registerMove(element, element.getStartDragX(), element.getStartDragY(), element.x, element.y);
                    }
                    Rockstar.getInstance().getFileManager().writeFile("client");
                }
            }
        }
        this.popups.removeIf(popup -> popup.getAnimation().getValue() == 0.0f && !popup.isShowing());
    };
    private final EventListener<ChatRenderEvent> onPostHud = event -> {
        if (!UiOverlayRenderer.shouldRenderIngameHud()) {
            return;
        }
        UIContext context = UIContext.of(event.getContext(), Hud.mc.screen == null ? -1 : (int)GuiUtility.getMouse().x, Hud.mc.screen == null ? -1 : (int)GuiUtility.getMouse().y, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        context.pose().pushMatrix();
        context.pose().translate(0.0f, 0.0f);
        for (Popup popup : this.popups) {
            System.out.println("[HUD Debug] Rendering popup " + popup.hashCode() + " at x=" + popup.getX() + " y=" + popup.getY() + " height=" + popup.getHeight() + " showing=" + popup.isShowing() + " animation=" + popup.getAnimation().getValue());
            if (popup.getY() + popup.getHeight() > sr.getGuiScaledHeight()) {
                popup.setY(sr.getGuiScaledHeight() - 10.0f - popup.getHeight());
            }
            popup.render(context);
        }
        context.pose().popMatrix();
    };
    private final EventListener<ChatKeyPressEvent> onKeyPress = event -> {
        int modifiers = event.getModifiers();
        int keyCode = event.getKeyCode();
        if (keyCode == 90 && (modifiers & 2) != 0) {
            Rockstar.getInstance().getHud().getHistoryManager().undo();
            return;
        }
        if (keyCode == 89 && (modifiers & 2) != 0) {
            Rockstar.getInstance().getHud().getHistoryManager().redo();
            return;
        }
    };
    private final EventListener<ChatClickEvent> onClick = event -> {
        System.out.println("[HUD Debug] onClick at x=" + event.getX() + " y=" + event.getY() + " button=" + event.getButton() + " active_popups=" + this.popups.size() + " disabled_elements_count=" + this.disabledElements().size());
        // Nametags module removed - entity list no longer available
        for (Popup popup : this.popups) {
            popup.onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
            if (popup.isHovered(event.getX(), event.getY())) {
                System.out.println("[HUD Debug] Click was inside popup " + popup.hashCode());
                return;
            }
            System.out.println("[HUD Debug] Click was outside popup " + popup.hashCode() + ", closing it");
            popup.setShowing(false);
        }
        for (HudElement element : this.elements) {
            element.onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
            if ((!element.isHovered(event.getX(), event.getY()) || !element.isShowing()) && !element.isDragging()) continue;
            System.out.println("[HUD Debug] Click handled by element: " + element.getName() + " (showing=" + element.isShowing() + " dragging=" + element.isDragging() + ")");
            return;
        }
        if (event.getButton() == 1 && !this.disabledElements().isEmpty()) {
            Popup popup = new Popup(event.getX(), event.getY(), 90.0f, 6.0f).title(Localizator.translate("whatadd")).separator();
            System.out.println("[HUD Debug] Creating add-hud popup " + popup.hashCode() + " at x=" + event.getX() + " y=" + event.getY() + " with " + this.disabledElements().size() + " elements");
            for (HudElement element : this.disabledElements()) {
                popup.button(Localizator.translate(element.getName()), element.getIcon(), popup1 -> {
                    System.out.println("[HUD Debug] Add HUD button clicked: " + element.getName() + " at x=" + event.getX() + " y=" + event.getY());
                    element.pos(event.getX(), event.getY());
                    element.setShowing(true);
                    popup1.setShowing(false);
                    Rockstar.getInstance().getFileManager().writeFile("client");
                });
            }
            this.popups.add(popup);
        } else if (event.getButton() == 1 && this.disabledElements().isEmpty() && this.timer.finished(600L)) {
            System.out.println("[HUD Debug] Right-click empty space but disabledElements is empty");
            Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.ERROR, "\u042d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u0432 \u043d\u0435\u0442", "\u042d\u043b\u0435\u043c\u0435\u043d\u0442\u044b \u0437\u0430\u043a\u043e\u043d\u0447\u0438\u043b\u0438\u0441\u044c, \u0434\u043e\u0431\u0430\u0432\u043b\u044f\u0442\u044c \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0435\u0447\u0435\u0433\u043e");
            this.timer.reset();
        }
    };
    private final EventListener<ChatReleaseEvent> onRelease = event -> {
        for (Popup popup : this.popups) {
            popup.onMouseReleased(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
            if (!popup.isHovered(event.getX(), event.getY())) continue;
            return;
        }
        for (HudElement element : this.elements) {
            element.onMouseReleased(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
        }
    };

    @CompileBytecode
    private void initialize() {
        Rockstar.getInstance().getEventManager().subscribe(this);
        this.island = new DynamicIsland();
        this.elements.addAll(List.of(new Effects(), new KeyBinds(), new Cooldowns(), new TargetHud(), new ArmorHud(), this.island, new WorldElement(), new PlayerElement(), new TrapTimeHud()));
    }

    public Hud() {
        this.initialize();
    }

    public List<HudElement> enabledElements() {
        return this.elements.stream().filter(HudElement::isShowing).toList();
    }

    public List<HudElement> disabledElements() {
        return this.elements.stream().filter(element -> !element.isShowing()).toList();
    }

    public <T extends HudElement> T getElementByName(String name) {
        return (T)((HudElement)this.elements.stream().filter(element -> element.getName().equalsIgnoreCase(name)).findFirst().orElse(null));
    }

    private boolean handleClick(ChatClickEvent event, Entity entity, Vec2 screenPos) {
        // Nametags module removed
        return false;
    }

    @Generated
    public List<HudElement> getElements() {
        return this.elements;
    }

    @Generated
    public List<Popup> getPopups() {
        return this.popups;
    }

    @Generated
    public DynamicIsland getIsland() {
        return this.island;
    }

    @Generated
    public HudHistoryManager getHistoryManager() {
        return this.historyManager;
    }

    @Generated
    public Grid getGrid() {
        return this.grid;
    }

    @Generated
    public String getDesc() {
        return this.desc;
    }

    @Generated
    public AnimatedText getDescText() {
        return this.descText;
    }

    @Generated
    public Timer getTimer() {
        return this.timer;
    }

    @Generated
    public EventListener<HudRenderEvent> getOnHud() {
        return this.onHud;
    }

    @Generated
    public EventListener<ChatRenderEvent> getOnPostHud() {
        return this.onPostHud;
    }

    @Generated
    public EventListener<ChatKeyPressEvent> getOnKeyPress() {
        return this.onKeyPress;
    }

    @Generated
    public EventListener<ChatClickEvent> getOnClick() {
        return this.onClick;
    }

    @Generated
    public EventListener<ChatReleaseEvent> getOnRelease() {
        return this.onRelease;
    }

    @Generated
    public void setDesc(String desc) {
        this.desc = desc;
    }
}

