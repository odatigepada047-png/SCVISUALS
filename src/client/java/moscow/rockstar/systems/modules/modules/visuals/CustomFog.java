/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.block.enums.FogType
 *  net.minecraft.client.renderer.Camera
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.MobEffects
 */
package moscow.rockstar.systems.modules.modules.visuals;

import lombok.Generated;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.RangeSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import net.minecraft.world.level.material.FogType;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;

@ModuleInfo(name="Custom Fog", category=ModuleCategory.VISUALS)
public class CustomFog
extends BaseModule {
    private final RangeSetting distance = new RangeSetting(this, "modules.settings.custom_fog.distance").min(1.0f).max(500.0f).step(1.0f).firstValue(10.0f).secondValue(200.0f);
    private final BooleanSetting syncWithTheme = new BooleanSetting(this, "modules.settings.sync_with_theme");
    private final ColorSetting fogColor = new ColorSetting(this, "modules.settings.custom_fog.color", () -> this.syncWithTheme.isEnabled()).color(new ColorRGBA(151, 71, 255, 100)).alpha(true);

    public ColorRGBA getColor() {
        return this.syncWithTheme.isEnabled() ? Colors.getAccentColor() : this.fogColor.getColor();
    }

    public boolean shouldModifyFog(Camera camera) {
        if (!this.isEnabled() || CustomFog.mc.level == null || CustomFog.mc.player == null) {
            return false;
        }
        Entity entity = camera.entity();
        if (camera.getFluidInCamera() == FogType.WATER) {
            return false;
        }
        if (camera.getFluidInCamera() == FogType.LAVA) {
            return false;
        }
        if (camera.getFluidInCamera() == FogType.POWDER_SNOW) {
            return false;
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (livingEntity.hasEffect(MobEffects.BLINDNESS)) {
                return false;
            }
            if (livingEntity.hasEffect(MobEffects.NIGHT_VISION)) {
                return false;
            }
        }
        return true;
    }

    @Generated
    public RangeSetting getDistance() {
        return this.distance;
    }

    @Generated
    public ColorSetting getFogColor() {
        return this.fogColor;
    }
}


