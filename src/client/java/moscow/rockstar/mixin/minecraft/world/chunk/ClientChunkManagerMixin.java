/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.world.ClientChunkCache
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.packet.s2c.play.ChunkData$BlockEntityVisitor
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.Heightmap$Type
 *  net.minecraft.world.chunk.WorldChunk
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package moscow.rockstar.mixin.minecraft.world.chunk;

import java.util.function.Consumer;
import java.util.Map;
import moscow.rockstar.utility.chunkanimator.ChunkAnimator;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientChunkCache.class})
public class ClientChunkManagerMixin {
    @Inject(method={"replaceWithPacketData"}, at={@At(value="TAIL")})
    private void onChunkLoad(int x, int z, FriendlyByteBuf buf, Map<?, ?> nbt, Consumer<?> consumer, CallbackInfoReturnable<LevelChunk> cir) {
        ChunkPos pos = new ChunkPos(x, z);
        LevelChunk chunk = (LevelChunk)cir.getReturnValue();
        if (chunk != null) {
            float surfaceY = this.getSurfaceY(chunk);
            ChunkAnimator.startAnimation(pos, surfaceY);
        }
    }

    private float getSurfaceY(LevelChunk chunk) {
        int totalHeight = 0;
        int count = 0;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                totalHeight += height;
                ++count;
            }
        }
        return count > 0 ? (float)totalHeight / (float)count : 64.0f;
    }
}

