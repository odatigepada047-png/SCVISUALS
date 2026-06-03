/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import net.minecraft.client.renderer.*;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

@ModuleInfo(name = "Ambience", category = ModuleCategory.VISUALS, desc = "modules.descriptions.ambience")
public class Ambience
        extends BaseModule {
    public final BooleanSetting endSky = new BooleanSetting(this, "modules.settings.ambience.end_sky");
    public final ModeSetting skyMode = new ModeSetting(this, "modules.settings.ambience.sky_mode");
    private final ModeSetting.Value defaultSky = new ModeSetting.Value(this.skyMode, "Default");
    private final ModeSetting.Value galaxy = new ModeSetting.Value(this.skyMode, "Galaxy");
    private final ModeSetting.Value space = new ModeSetting.Value(this.skyMode, "Space");
    private final BooleanSetting customTime = new BooleanSetting(this, "modules.settings.ambience.custom_time");
    private final SliderSetting time = new SliderSetting((SettingsContainer) this, "modules.settings.ambience.time",
            () -> !this.customTime.isEnabled()).step(1000.0f).min(0.0f).max(24000.0f).currentValue(12000.0f);
    public final BooleanSetting bright = new BooleanSetting(this, "modules.settings.ambience.bright").enable();
    private static final Identifier END_SKY = Identifier.fromNamespaceAndPath("minecraft", "textures/environment/end_sky.png");
    private long oldTime;
    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        if (event.getPacket() instanceof ClientboundSetTimePacket && this.customTime.isEnabled()) {
            event.cancel();
        }
    };

    private final EventListener<Render3DEvent> onRender3D = event -> {
        if (!this.isEnabled()) return;

        float r = -1, g = -1, b = -1;
        if (this.endSky.isEnabled()) {
            r = g = b = 1.0f;
        } else if (this.galaxy.isSelected()) {
            r = 0.7f; g = 0.2f; b = 0.8f;
        } else if (this.space.isSelected()) {
            r = 0.1f; g = 0.1f; b = 0.3f;
        }

        if (r != -1) {
            renderCustomSky(event, r, g, b);
        }
    };

    private void renderCustomSky(Render3DEvent event, float r, float g, float b) {
        // Custom sky mesh rendering disabled pending 26.1 render pipeline migration
    }

    @Override
    public void tick() {
        if (Ambience.mc.level == null) {
            return;
        }
        if (this.customTime.isEnabled()) {
            Ambience.mc.level.getLevelData().setGameTime((long) this.time.getCurrentValue());
        }
        super.tick();
    }

    @Override
    public void onEnable() {
        if (!EntityUtility.isInGame() || Ambience.mc.level == null) {
            return;
        }
        this.oldTime = Ambience.mc.level.getGameTime();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (!EntityUtility.isInGame() || Ambience.mc.level == null) {
            return;
        }
        Ambience.mc.level.getLevelData().setGameTime(this.oldTime);
        super.onDisable();
    }

    @Generated
    public BooleanSetting getEndSky() {
        return this.endSky;
    }

    @Generated
    public BooleanSetting getCustomTime() {
        return this.customTime;
    }

    @Generated
    public SliderSetting getTime() {
        return this.time;
    }

    @Generated
    public BooleanSetting getBright() {
        return this.bright;
    }

    @Generated
    public long getOldTime() {
        return this.oldTime;
    }

    @Generated
    public ModeSetting.Value getSkyGalaxy() {
        return this.galaxy;
    }

    @Generated
    public ModeSetting.Value getSkySpace() {
        return this.space;
    }

    @Generated
    public EventListener<ReceivePacketEvent> getOnReceivePacket() {
        return this.onReceivePacket;
    }
}
