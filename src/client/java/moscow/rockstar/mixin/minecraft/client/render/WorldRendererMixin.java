package moscow.rockstar.mixin.minecraft.client.render;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.profiling.Profiler;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {LevelRenderer.class})
public class WorldRendererMixin implements IMinecraft {
    // Moved to GameRendererMixin.java to run after doEntityOutline() to fix black screens
}


