/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.Minecraft
 */
package moscow.rockstar.systems.modules;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.systems.modules.exception.UnknownModuleException;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.modules.modules.movement.AutoSprint;
import moscow.rockstar.systems.modules.modules.other.Assist;
import moscow.rockstar.systems.modules.modules.other.Auction;
import moscow.rockstar.systems.modules.modules.other.AutoAccept;
import moscow.rockstar.systems.modules.modules.other.AutoAuth;
import moscow.rockstar.systems.modules.modules.other.AutoJoin;
import moscow.rockstar.systems.modules.modules.other.AutoResell;
import moscow.rockstar.systems.modules.modules.other.ItemPickup;
import moscow.rockstar.systems.modules.modules.other.HiderUtils;
import moscow.rockstar.systems.modules.modules.other.PhantomItems;
import moscow.rockstar.systems.modules.modules.other.RussianRoulette;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.systems.modules.modules.player.AutoEat;
import moscow.rockstar.systems.modules.modules.player.AutoInvisible;
import moscow.rockstar.systems.modules.modules.player.AutoSwap;
import moscow.rockstar.systems.modules.modules.player.ElytraUtils;
import moscow.rockstar.systems.modules.modules.player.InvUtils;
import moscow.rockstar.systems.modules.modules.player.MiddleClick;
import moscow.rockstar.systems.modules.modules.player.PlayerUtils;
import moscow.rockstar.systems.modules.modules.visuals.Ambience;
import moscow.rockstar.systems.modules.modules.visuals.Animations;
import moscow.rockstar.systems.modules.modules.visuals.AspectRatio;
import moscow.rockstar.systems.modules.modules.visuals.CustomCrosshair;
import moscow.rockstar.systems.modules.modules.visuals.CustomFog;
import moscow.rockstar.systems.modules.modules.visuals.CustomHand;
import moscow.rockstar.systems.modules.modules.visuals.FriendMarkers;
import moscow.rockstar.systems.modules.modules.visuals.Interface;
import moscow.rockstar.systems.modules.modules.visuals.KillEffects;
import moscow.rockstar.systems.modules.modules.visuals.MenuModule;
import moscow.rockstar.systems.modules.modules.visuals.ObjectInfo;
import moscow.rockstar.systems.modules.modules.player.Freelook;
import moscow.rockstar.systems.modules.modules.visuals.Prediction;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import moscow.rockstar.systems.modules.modules.visuals.ShulkerPreview;
import moscow.rockstar.systems.modules.modules.visuals.SwingAnimation;
import moscow.rockstar.systems.modules.modules.visuals.TNTTimer;
import moscow.rockstar.systems.modules.modules.visuals.TargetESP;
import moscow.rockstar.systems.modules.modules.visuals.World;
import moscow.rockstar.systems.modules.modules.visuals.Waypoints;
import moscow.rockstar.systems.modules.modules.other.PartySystem;
import moscow.rockstar.systems.modules.modules.other.ModerUtils;
import net.minecraft.client.Minecraft;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<Module>();
    private final java.util.Map<Class<? extends Module>, Module> moduleClassMap = new java.util.HashMap<>();
    private final EventListener<ClientPlayerTickEvent> tickListener;
    private final EventListener<HudRenderEvent> moduleWidgetRenderer;
    private final EventListener<KeyPressEvent> onKeyPress = event -> {
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        for (Module module : this.getModules()) {
            if (module.getKey() != event.getKey() || module.getKey() == -1 || event.getAction() != 1) continue;
            module.toggle();
        }
    };
    private final EventListener<MouseEvent> onMouseButtonPress = event -> {
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        for (Module module : this.getModules()) {
            if (module.getKey() != event.getButton() || module.getKey() == -1 || event.getAction() != 1) continue;
            module.toggle();
        }
    };

    public ModuleManager(EventListener<ClientPlayerTickEvent> tickListener, EventListener<HudRenderEvent> moduleWidgetRenderer) {
        this.tickListener = tickListener;
        this.moduleWidgetRenderer = moduleWidgetRenderer;
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    @CompileBytecode
    public void registerModules() {
        this.register(new AutoSprint());
        this.register(new MenuModule());
        this.register(new Removals());
        this.register(new Ambience());
        this.register(new Animations());
        this.register(new SwingAnimation());
        this.register(new CustomHand());
        this.register(new FriendMarkers());
        this.register(new TNTTimer());
        this.register(new Interface());
        this.register(new TargetESP());
        this.register(new CustomFog());
        this.register(new World());
        this.register(new KillEffects());
        this.register(new Prediction());
        this.register(new AspectRatio());
        this.register(new CustomCrosshair());
        this.register(new Freelook());
        this.register(new AutoInvisible());
        this.register(new MiddleClick());
        this.register(new InvUtils());
        this.register(new AutoEat());
        this.register(new PlayerUtils());
        this.register(new ItemPickup());
        this.register(new ObjectInfo());
        this.register(new HiderUtils());
        this.register(new ElytraUtils());
        this.register(new AutoResell());
        this.register(new Auction());
        this.register(new AutoAccept());
        this.register(new AutoSwap());
        this.register(new RussianRoulette());
        this.register(new AutoAuth());
        this.register(new AutoJoin());
        this.register(new Assist());
        this.register(new Sounds());
        this.register(new ShulkerPreview());
        this.register(new Waypoints());
        this.register(new PartySystem());
        this.register(new ModerUtils());
    }

    @CompileBytecode
    public void enableModules() {
        for (Module module : this.modules) {
            if (!module.getInfo().enabledByDefault()) continue;
            module.enable();
        }
    }

    public void register(BaseModule module) {
        this.modules.add(module);
        this.moduleClassMap.put(module.getClass(), module);
    }

    public <T extends Module> T getModule(String name) {
        return (T)this.modules.stream().filter(module -> module.getName().replace(" ", "").equalsIgnoreCase(name) || module.getName().equalsIgnoreCase(name)).findFirst().orElseThrow(() -> new UnknownModuleException(name));
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        Module m = this.moduleClassMap.get(clazz);
        if (m != null) {
            return (T) m;
        }
        return (T)this.modules.stream().filter(module -> module.getClass().equals(clazz)).findFirst().orElseThrow(() -> new UnknownModuleException(clazz.getSimpleName()));
    }

    @Generated
    public List<Module> getModules() {
        return this.modules;
    }

    @Generated
    public EventListener<ClientPlayerTickEvent> getTickListener() {
        return this.tickListener;
    }

    @Generated
    public EventListener<HudRenderEvent> getModuleWidgetRenderer() {
        return this.moduleWidgetRenderer;
    }

    @Generated
    public EventListener<KeyPressEvent> getOnKeyPress() {
        return this.onKeyPress;
    }

    @Generated
    public EventListener<MouseEvent> getOnMouseButtonPress() {
        return this.onMouseButtonPress;
    }
}
