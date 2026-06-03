/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.CollisionContext
 *  net.minecraft.client.Minecraft
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.util.shape.Shapes
 *  net.minecraft.world.BlockCollisions
 *  net.minecraft.world.CollisionGetter
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package moscow.rockstar.mixin.minecraft.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.game.CollisionShapeEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={BlockCollisions.class})
public abstract class BlockCollisionSpliteratorMixin {
    @WrapOperation(method={"computeNext"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/phys/shapes/CollisionContext;getCollisionShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;")})
    private VoxelShape onComputeNextCollisionBox(CollisionContext instance, BlockState blockState, CollisionGetter collisionView, BlockPos blockPos, Operation<VoxelShape> original) {
        VoxelShape shape = original.call(instance, blockState, collisionView, blockPos);
        if (Minecraft.getInstance().level == null || collisionView != Minecraft.getInstance().level) {
            return shape;
        }
        if (Rockstar.INSTANCE == null || Rockstar.getInstance().getEventManager() == null) {
            return shape;
        }
        CollisionShapeEvent event = new CollisionShapeEvent(blockState, blockPos, shape);
        Rockstar.getInstance().getEventManager().triggerEvent(event);
        return event.isCancelled() ? Shapes.empty() : event.getShape();
    }
}

