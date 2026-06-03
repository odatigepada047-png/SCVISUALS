/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.util.Mth
 *  net.minecraft.util.math.Vec2
 */
package moscow.rockstar.systems.modules.constructions.swinganim;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingPhase;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingSettings;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingTransformations;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPreset;
import moscow.rockstar.systems.modules.constructions.swinganim.presets.SwingPresetManager;
import moscow.rockstar.systems.setting.settings.BezierSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.math.MathUtility;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import ru.kotopushka.compiler.sdk.annotations.CompileBytecode;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class SwingManager {
    private final List<SwingPreset> presets = new ArrayList<SwingPreset>();
    private String current = "autosave";
    private final SwingSettings sharedSettings = new SwingSettings();
    private final SwingPhase startPhase = new SwingPhase();
    private final SwingPhase endPhase = new SwingPhase();
    private final BezierSetting bezier = new BezierSetting(this.sharedSettings, "animation").start(0.5f, 1.0f).end(0.5f, 0.0f);
    private final BooleanSetting back = new BooleanSetting(this.sharedSettings, "swing.back").enable();
    private final SliderSetting speed = new SliderSetting(this.sharedSettings, "swing.wing_speed").step(1.0f).min(1.0f).max(5.0f).currentValue(2.0f);

    @VMProtect(type=VMProtectType.MUTATION)
    @CompileBytecode
    private void initialize() {
        this.presets.add(new SwingPreset("swings.block_hit", new Vec2(0.5f, 1.0f), new Vec2(0.5f, 0.0f), true, 2.0f, new SwingTransformations(0.0f, -0.05f, -0.7f, 1.0500001f, -0.7f, -1.1f, -120.0f, -135.0f, -60.0f), new SwingTransformations(0.0f, -0.05f, -0.7f, 1.0500001f, -0.7f, -1.1f, -120.0f, -180.0f, -60.0f)));
        this.presets.add(new SwingPreset("swings.bonk", new Vec2(0.40131578f, 0.53543305f), new Vec2(0.0f, -0.24409449f), true, 2.0f, new SwingTransformations(0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), new SwingTransformations(0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, -45.0f, 0.0f, 0.0f)));
        this.presets.add(new SwingPreset("swings.rotate_360", new Vec2(0.43421054f, 0.61417323f), new Vec2(0.04605263f, -0.26771653f), false, 2.0f, new SwingTransformations(0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), new SwingTransformations(0.0f, -0.4f, -0.65000004f, 0.0f, 0.0f, 0.0f, -360.0f, 0.0f, 0.0f)));
        this.presets.add(new SwingPreset("swings.from_me", new Vec2(0.42105263f, 0.87401575f), new Vec2(0.3881579f, -0.4566929f), true, 2.0f, new SwingTransformations(0.0f, 0.0f, -1.1f, 0.2f, 0.0f, -0.1f, -135.0f, 45.0f, 60.0f), new SwingTransformations(0.0f, 0.0f, -1.1f, 0.2f, 0.0f, -0.3f, -180.0f, 45.0f, 60.0f)));
        // Слэш — диагональный удар сверху-справа вниз-влево с дугой
        this.presets.add(new SwingPreset("swings.slash", new Vec2(0.6f, 1.0f), new Vec2(0.3f, 0.0f), true, 2.5f, new SwingTransformations(0.0f, -0.3f, -0.7f, 0.35f, 0.25f, 0.0f, -30.0f, 55.0f, -25.0f), new SwingTransformations(0.0f, -0.3f, -0.7f, -0.15f, -0.25f, 0.0f, -95.0f, -50.0f, 20.0f)));
        // Апперкот — резкий удар снизу вверх с наклоном
        this.presets.add(new SwingPreset("swings.uppercut", new Vec2(0.5f, 1.0f), new Vec2(0.5f, 0.0f), true, 2.0f, new SwingTransformations(0.0f, -0.5f, -0.65f, 0.15f, -0.35f, 0.0f, 40.0f, 15.0f, 18.0f), new SwingTransformations(0.0f, -0.5f, -0.65f, 0.05f, 0.3f, 0.0f, -110.0f, -10.0f, -15.0f)));
    }

    public SwingManager() {
        this.initialize();
    }

    public SwingTransformations transformations(float progress) {
        progress = this.bezier.easing().ease(progress, 0.0f, 1.0f, 1.0f);
        if (this.back.isEnabled()) {
            progress = Mth.sin((float)(Mth.sqrt((float)progress) * (float)Math.PI));
        }
        return new SwingTransformations(this.get(this.startPhase.getAnchorX(), this.endPhase.getAnchorX(), progress), this.get(this.startPhase.getAnchorY(), this.endPhase.getAnchorY(), progress), this.get(this.startPhase.getAnchorZ(), this.endPhase.getAnchorZ(), progress), this.get(this.startPhase.getMoveX(), this.endPhase.getMoveX(), progress), this.get(this.startPhase.getMoveY(), this.endPhase.getMoveY(), progress), this.get(this.startPhase.getMoveZ(), this.endPhase.getMoveZ(), progress), this.get(this.startPhase.getRotateX(), this.endPhase.getRotateX(), progress), this.get(this.startPhase.getRotateY(), this.endPhase.getRotateY(), progress), this.get(this.startPhase.getRotateZ(), this.endPhase.getRotateZ(), progress));
    }

    private float get(SliderSetting start, SliderSetting end, float progress) {
        return MathUtility.interpolate(start.getCurrentValue(), end.getCurrentValue(), progress);
    }

    public String getCurrent() {
        SwingPresetManager manager = Rockstar.getInstance().getSwingPresetManager();
        return manager.getCurrent() != null ? manager.getCurrent().getFileName() : this.current;
    }

    @Generated
    public List<SwingPreset> getPresets() {
        return this.presets;
    }

    @Generated
    public SwingSettings getSharedSettings() {
        return this.sharedSettings;
    }

    @Generated
    public SwingPhase getStartPhase() {
        return this.startPhase;
    }

    @Generated
    public SwingPhase getEndPhase() {
        return this.endPhase;
    }

    @Generated
    public BezierSetting getBezier() {
        return this.bezier;
    }

    @Generated
    public BooleanSetting getBack() {
        return this.back;
    }

    @Generated
    public SliderSetting getSpeed() {
        return this.speed;
    }

    @Generated
    public void setCurrent(String current) {
        this.current = current;
    }
}

