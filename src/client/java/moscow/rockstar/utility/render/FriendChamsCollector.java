package moscow.rockstar.utility.render;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import org.joml.Quaternionf;
import java.util.List;

public class FriendChamsCollector implements SubmitNodeCollector, OrderedSubmitNodeCollector {
    private final SubmitNodeCollector original;
    private OrderedSubmitNodeCollector originalOrdered;

    public FriendChamsCollector(SubmitNodeCollector original) {
        this.original = original;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        this.originalOrdered = original.order(order);
        return this;
    }

    @Override
    public void submitItem(PoseStack matrices, ItemDisplayContext displayContext, int light, int overlay, int color, int[] layers, List<BakedQuad> elements, ItemStackRenderState.FoilType foil) {
        if (originalOrdered != null) {
            originalOrdered.submitItem(matrices, displayContext, light, overlay, color, layers, elements, foil);
        } else {
            original.submitItem(matrices, displayContext, light, overlay, color, layers, elements, foil);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, int light, int overlay, int color, net.minecraft.client.resources.model.sprite.SpriteId spriteId, net.minecraft.client.resources.model.sprite.SpriteGetter spriteGetter, int id, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, light, overlay, color, spriteId, spriteGetter, id, crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, light, overlay, color, spriteId, spriteGetter, id, crumblingOverlay);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, RenderType layer, int light, int overlay, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, layer, light, overlay, color, crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, layer, light, overlay, color, crumblingOverlay);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, RenderType layer, int light, int overlay, int color, TextureAtlasSprite sprite, int id, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, layer, light, overlay, color, sprite, id, crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, layer, light, overlay, color, sprite, id, crumblingOverlay);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, net.minecraft.resources.Identifier id, int light, int overlay, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, id, light, overlay, color, crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, id, light, overlay, color, crumblingOverlay);
        }
    }

    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType layer, int light, int overlay, TextureAtlasSprite sprite, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, layer, light, overlay, sprite, color, crumblingOverlay);
        } else {
            original.submitModelPart(part, matrices, layer, light, overlay, sprite, color, crumblingOverlay);
        }
    }

    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType layer, int light, int overlay, TextureAtlasSprite sprite, boolean x, boolean y, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int id) {
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, layer, light, overlay, sprite, x, y, color, crumblingOverlay, id);
        } else {
            original.submitModelPart(part, matrices, layer, light, overlay, sprite, x, y, color, crumblingOverlay, id);
        }
    }

    @Override
    public void submitBlockModel(PoseStack matrices, RenderType layer, List<BlockStateModelPart> quads, int[] light, int overlay, int color, int alpha) {
        if (originalOrdered != null) {
            originalOrdered.submitBlockModel(matrices, layer, quads, light, overlay, color, alpha);
        } else {
            original.submitBlockModel(matrices, layer, quads, light, overlay, color, alpha);
        }
    }

    @Override
    public void submitBreakingBlockModel(PoseStack matrices, BlockStateModel model, long time, int light) {
        if (originalOrdered != null) {
            originalOrdered.submitBreakingBlockModel(matrices, model, time, light);
        } else {
            original.submitBreakingBlockModel(matrices, model, time, light);
        }
    }

    @Override
    public void submitCustomGeometry(PoseStack matrices, RenderType layer, SubmitNodeCollector.CustomGeometryRenderer custom) {
        if (originalOrdered != null) {
            originalOrdered.submitCustomGeometry(matrices, layer, custom);
        } else {
            original.submitCustomGeometry(matrices, layer, custom);
        }
    }

    @Override
    public void submitFlame(PoseStack matrices, EntityRenderState state, Quaternionf rotation) {
        if (originalOrdered != null) {
            originalOrdered.submitFlame(matrices, state, rotation);
        } else {
            original.submitFlame(matrices, state, rotation);
        }
    }

    @Override
    public void submitLeash(PoseStack matrices, EntityRenderState.LeashState leash) {
        if (originalOrdered != null) {
            originalOrdered.submitLeash(matrices, leash);
        } else {
            original.submitLeash(matrices, leash);
        }
    }

    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType layer, int light, int overlay, TextureAtlasSprite sprite, boolean x, boolean y) {
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, layer, light, overlay, sprite, x, y, 0xFFFFFFFF, null, 0);
        } else {
            original.submitModelPart(part, matrices, layer, light, overlay, sprite, x, y, 0xFFFFFFFF, null, 0);
        }
    }

    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType layer, int light, int overlay, TextureAtlasSprite sprite) {
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, layer, light, overlay, sprite, 0xFFFFFFFF, null);
        } else {
            original.submitModelPart(part, matrices, layer, light, overlay, sprite, 0xFFFFFFFF, null);
        }
    }

    @Override
    public void submitMovingBlock(PoseStack matrices, MovingBlockRenderState state) {
        if (originalOrdered != null) {
            originalOrdered.submitMovingBlock(matrices, state);
        } else {
            original.submitMovingBlock(matrices, state);
        }
    }

    @Override
    public void submitNameTag(PoseStack matrices, Vec3 pos, int color, Component text, boolean shadow, int light, double scale, CameraRenderState cameraState) {
        if (originalOrdered != null) {
            originalOrdered.submitNameTag(matrices, pos, color, text, shadow, light, scale, cameraState);
        } else {
            original.submitNameTag(matrices, pos, color, text, shadow, light, scale, cameraState);
        }
    }

    @Override
    public void submitParticleGroup(SubmitNodeCollector.ParticleGroupRenderer renderer) {
        if (originalOrdered != null) {
            originalOrdered.submitParticleGroup(renderer);
        } else {
            original.submitParticleGroup(renderer);
        }
    }

    @Override
    public void submitShadow(PoseStack matrices, float opacity, List<net.minecraft.client.renderer.entity.state.EntityRenderState.ShadowPiece> pieces) {
        if (originalOrdered != null) {
            originalOrdered.submitShadow(matrices, opacity, pieces);
        } else {
            original.submitShadow(matrices, opacity, pieces);
        }
    }

    @Override
    public void submitText(PoseStack matrices, float x, float y, FormattedCharSequence text, boolean shadow, Font.DisplayMode displayMode, int color, int light, int overlay, int opacity) {
        // Force Font.DisplayMode.SEE_THROUGH so nicknames render through walls
        if (originalOrdered != null) {
            originalOrdered.submitText(matrices, x, y, text, shadow, Font.DisplayMode.SEE_THROUGH, color, light, overlay, opacity);
        } else {
            original.submitText(matrices, x, y, text, shadow, Font.DisplayMode.SEE_THROUGH, color, light, overlay, opacity);
        }
    }
}
