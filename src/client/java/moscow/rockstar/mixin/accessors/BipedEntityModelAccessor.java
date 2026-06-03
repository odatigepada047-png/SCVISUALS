/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.ModelPart
 *  net.minecraft.client.renderer.entity.model.HumanoidModel
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package moscow.rockstar.mixin.accessors;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={HumanoidModel.class})
public interface BipedEntityModelAccessor {
    @Accessor(value="head")
    public ModelPart rockstar$getHead();

    @Accessor(value="hat")
    public ModelPart rockstar$getHat();

    @Accessor(value="body")
    public ModelPart rockstar$getBody();

    @Accessor(value="rightArm")
    public ModelPart rockstar$getRightArm();

    @Accessor(value="leftArm")
    public ModelPart rockstar$getLeftArm();

    @Accessor(value="rightLeg")
    public ModelPart rockstar$getRightLeg();

    @Accessor(value="leftLeg")
    public ModelPart rockstar$getLeftLeg();
}

