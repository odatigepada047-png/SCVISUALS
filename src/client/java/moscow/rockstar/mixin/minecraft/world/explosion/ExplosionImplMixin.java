/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.Level
 *  net.minecraft.world.explosion.ExplosionImpl
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package moscow.rockstar.mixin.minecraft.world.explosion;

import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.mixin.accessors.ExplosionImplAccessor;
import moscow.rockstar.systems.event.impl.game.AncientDebrisEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerExplosion.class})
public abstract class ExplosionImplMixin
implements IMinecraft {
    @Inject(method={"hurtEntities"}, at={@At(value="TAIL")})
    private void onAfterDamageEntities(CallbackInfo ci) {
        ServerExplosion self = (ServerExplosion)(Object)this;
        List<BlockPos> affectedBlocks = ((ExplosionImplAccessor)self).invokeGetBlocksToDestroy();
        List<BlockPos> debris = affectedBlocks.stream().filter(pos -> self.level().getBlockState(pos).is(Blocks.ANCIENT_DEBRIS)).toList();
        if (!debris.isEmpty() && self.level().dimension() == Level.NETHER) {
            Rockstar.getInstance().getEventManager().triggerEvent(new AncientDebrisEvent(debris, self.center()));
        }
    }
}
