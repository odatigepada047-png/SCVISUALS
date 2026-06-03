/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleManager
 *  net.minecraft.particle.ParticleEffect
 *  net.minecraft.particle.ParticleTypes
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.client.render;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.Removals;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientLevel.class})
public abstract class ParticleManagerMixin {
    private Removals removalsCache;

    private Removals getRemovals() {
        if (this.removalsCache == null) {
            try {
                this.removalsCache = Rockstar.getInstance().getModuleManager().getModule(Removals.class);
            } catch (Exception e) {}
        }
        return this.removalsCache;
    }

    @Inject(method={"addDestroyBlockEffect"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAddBlockBreakParticles(BlockPos blockPos, BlockState state, CallbackInfo info) {
        Removals removals = this.getRemovals();
        if (removals != null && removals.isEnabled() && removals.getBreakParticles().isSelected()) {
            info.cancel();
        }
    }

    @Inject(method={"addBreakingBlockEffect"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAddBlockBreakingParticles(BlockPos blockPos, Direction direction, CallbackInfo info) {
        Removals removals = this.getRemovals();
        if (removals != null && removals.isEnabled() && removals.getBreakParticles().isSelected()) {
            info.cancel();
        }
    }

    @Inject(method={"addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void onAddParticle(ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        Removals removals = this.getRemovals();
        if (removals != null && removals.isEnabled() && removals.getWeather().isSelected() && parameters.getType() == ParticleTypes.RAIN) {
            ci.cancel();
        }
    }
}

