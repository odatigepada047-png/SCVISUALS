/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.consume.ItemUseAnimation
 *  net.minecraft.util.HumanoidArm
 *  org.joml.Quaternionf
 */
package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HandRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingAnimScreen;
import moscow.rockstar.systems.modules.constructions.swinganim.SwingTransformations;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ButtonSetting;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionf;

@ModuleInfo(name="Swing Animation", category=ModuleCategory.VISUALS, desc="\u0418\u0437\u043c\u0435\u043d\u044f\u0435\u0442 \u0430\u043d\u0438\u043c\u0430\u0446\u0438\u0438 \u0440\u0443\u043a \u043f\u0440\u0438 \u0432\u0437\u043c\u0430\u0445\u0435")
public class SwingAnimation
extends BaseModule {
    private final BooleanSetting onlyAura = new BooleanSetting(this, "swing.only_aura");
    private final ButtonSetting button = new ButtonSetting(this, "swing.open_menu").action(() -> mc.setScreen((Screen)new SwingAnimScreen()));
    private final EventListener<HandRenderEvent> onHandRender = event -> {
        // Получаем ведущую руку игрока
        HumanoidArm mainHand = mc.player != null ? mc.player.getMainArm() : HumanoidArm.RIGHT;
        HumanoidArm renderArm = event.getArm();

        // Применяем анимацию только к ведущей руке
        boolean isMainHand = renderArm == mainHand;
        if (this.shouldApplyAnimation(event.getItemStack()) && isMainHand) {
            PoseStack matrices = event.pose();
            float swingProgress = event.getSwingProgress();
            float equipProgress = event.getEquipProgress();
            SwingTransformations trans = Rockstar.getInstance().getSwingManager().transformations(swingProgress);

            // Если ведущая рука левая — зеркалим анимацию
            boolean mirror = mainHand == HumanoidArm.LEFT;
            float anchorX = mirror ? -trans.anchorX() : trans.anchorX();
            float moveX   = mirror ? -trans.moveX()   : trans.moveX();
            float rotateY = mirror ? -trans.rotateY() : trans.rotateY();
            float rotateZ = mirror ? -trans.rotateZ() : trans.rotateZ();

            matrices.translate(anchorX, trans.anchorY(), trans.anchorZ());
            matrices.translate(moveX, trans.moveY(), trans.moveZ());
            matrices.mulPose(new Quaternionf().rotationXYZ(
                    (float) Math.toRadians(trans.rotateX()),
                    (float) Math.toRadians(rotateY),
                    (float) Math.toRadians(rotateZ)));
            matrices.translate(-anchorX, -trans.anchorY(), -trans.anchorZ());
            event.cancel();
        }
    };

    public boolean shouldApplyAnimation(ItemStack itemStack) {
        Entity target = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        Item item = itemStack.getItem();
        if (this.onlyAura.isEnabled() && target == null) {
            return false;
        }
        return item != Items.AIR && item != Items.FILLED_MAP && item != Items.CROSSBOW && item != Items.BOW && item != Items.TRIDENT && itemStack.getUseAnimation() != ItemUseAnimation.DRINK && itemStack.getUseAnimation() != ItemUseAnimation.EAT;
    }
}



