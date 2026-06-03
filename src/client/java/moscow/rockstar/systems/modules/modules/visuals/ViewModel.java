/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.util.HumanoidArm
 *  net.minecraft.util.math.Axis
 */
package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HandRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;
import com.mojang.math.Axis;

@ModuleInfo(name="View Model", category=ModuleCategory.VISUALS, desc="modules.descriptions.view_model")
public class ViewModel
extends BaseModule {
    private final SliderSetting mainTranslateX = new SliderSetting(this, "modules.settings.view_model.main_translate_x").min(-2.0f).max(2.0f).currentValue(0.0f).step(0.05f);
    private final SliderSetting mainTranslateY = new SliderSetting(this, "modules.settings.view_model.main_translate_y").min(-2.0f).max(2.0f).currentValue(0.0f).step(0.05f);
    private final SliderSetting mainTranslateZ = new SliderSetting(this, "modules.settings.view_model.main_translate_z").min(-2.0f).max(2.0f).currentValue(0.0f).step(0.05f);
    private final SliderSetting mainRotateX = new SliderSetting(this, "modules.settings.view_model.main_rotate_x").min(-180.0f).max(180.0f).currentValue(0.0f).step(1.0f);
    private final SliderSetting mainRotateY = new SliderSetting(this, "modules.settings.view_model.main_rotate_y").min(-180.0f).max(180.0f).currentValue(0.0f).step(1.0f);
    private final SliderSetting mainRotateZ = new SliderSetting(this, "modules.settings.view_model.main_rotate_z").min(-180.0f).max(180.0f).currentValue(0.0f).step(1.0f);
    private final SliderSetting offTranslateX = new SliderSetting(this, "modules.settings.view_model.off_translate_x").min(-2.0f).max(2.0f).currentValue(0.0f).step(0.05f);
    private final SliderSetting offTranslateY = new SliderSetting(this, "modules.settings.view_model.off_translate_y").min(-2.0f).max(2.0f).currentValue(0.0f).step(0.05f);
    private final SliderSetting offTranslateZ = new SliderSetting(this, "modules.settings.view_model.off_translate_z").min(-2.0f).max(2.0f).currentValue(0.0f).step(0.05f);
    private final SliderSetting offRotateX = new SliderSetting(this, "modules.settings.view_model.off_rotate_x").min(-180.0f).max(180.0f).currentValue(0.0f).step(1.0f);
    private final SliderSetting offRotateY = new SliderSetting(this, "modules.settings.view_model.off_rotate_y").min(-180.0f).max(180.0f).currentValue(0.0f).step(1.0f);
    private final SliderSetting offRotateZ = new SliderSetting(this, "modules.settings.view_model.off_rotate_z").min(-180.0f).max(180.0f).currentValue(0.0f).step(1.0f);
    private final EventListener<HandRenderEvent> onHandRender = event -> {
        PoseStack matrices = event.pose();
        boolean isMain = event.getArm() == HumanoidArm.RIGHT;
        float translateX = isMain ? this.mainTranslateX.getCurrentValue() : this.offTranslateX.getCurrentValue();
        float translateY = isMain ? this.mainTranslateY.getCurrentValue() : this.offTranslateY.getCurrentValue();
        float translateZ = isMain ? this.mainTranslateZ.getCurrentValue() : this.offTranslateZ.getCurrentValue();
        float rotateX = isMain ? this.mainRotateX.getCurrentValue() : this.offRotateX.getCurrentValue();
        float rotateY = isMain ? this.mainRotateY.getCurrentValue() : this.offRotateY.getCurrentValue();
        float rotateZ = isMain ? this.mainRotateZ.getCurrentValue() : this.offRotateZ.getCurrentValue();
        float direction = isMain ? 1.0f : -1.0f;
        matrices.translate(translateX * direction, translateY, translateZ);
        matrices.mulPose(Axis.XP.rotationDegrees(rotateX));
        matrices.mulPose(Axis.YP.rotationDegrees(rotateY));
        matrices.mulPose(Axis.ZP.rotationDegrees(rotateZ));
    };
}



