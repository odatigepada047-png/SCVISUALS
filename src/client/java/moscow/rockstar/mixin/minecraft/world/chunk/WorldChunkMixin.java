/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.Level
 *  net.minecraft.world.chunk.WorldChunk
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.world.chunk;

import com.llamalad7.mixinextras.sugar.Local;
import moscow.rockstar.utility.game.WorldUtility;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelChunk.class})
public abstract class WorldChunkMixin {
    @Shadow
    public abstract Level getLevel();

    @Inject(method={"removeBlockEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/entity/BlockEntity;setRemoved()V")})
    private void onRemoveBlockEntity(BlockPos pos, CallbackInfo ci, @Local @Nullable BlockEntity removed) {
        if (removed != null) {
            WorldUtility.blockEntities.remove(removed);
        }
    }
}

