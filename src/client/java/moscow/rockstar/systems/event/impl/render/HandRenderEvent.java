/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.util.HumanoidArm
 */
package moscow.rockstar.systems.event.impl.render;

import lombok.Generated;
import moscow.rockstar.systems.event.EventCancellable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;

public class HandRenderEvent
extends EventCancellable {
    private final HumanoidArm arm;
    private final float swingProgress;
    private final ItemStack itemStack;
    private final float equipProgress;
    private final PoseStack matrices;

    @Generated
    public HumanoidArm getArm() {
        return this.arm;
    }

    @Generated
    public float getSwingProgress() {
        return this.swingProgress;
    }

    @Generated
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Generated
    public float getEquipProgress() {
        return this.equipProgress;
    }

    @Generated
    public PoseStack pose() {
        return this.matrices;
    }

    @Generated
    public HandRenderEvent(HumanoidArm arm, float swingProgress, ItemStack itemStack, float equipProgress, PoseStack matrices) {
        this.arm = arm;
        this.swingProgress = swingProgress;
        this.itemStack = itemStack;
        this.equipProgress = equipProgress;
        this.matrices = matrices;
    }
}

