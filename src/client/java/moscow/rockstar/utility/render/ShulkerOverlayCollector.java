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
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionf;
import java.util.List;
import java.util.Map;

public class ShulkerOverlayCollector implements SubmitNodeCollector, OrderedSubmitNodeCollector {
    private final SubmitNodeCollector original;
    private OrderedSubmitNodeCollector originalOrdered;
    private final int forcedOverlay;
    private final int highlightColor;
    private final boolean useChams;

    public ShulkerOverlayCollector(SubmitNodeCollector original, int highlightColor, boolean useChams) {
        this.original = original;
        this.forcedOverlay = OverlayTexture.pack(OverlayTexture.u(0.0f), OverlayTexture.v(true));
        this.highlightColor = highlightColor;
        this.useChams = useChams;
    }

    private RenderType getChamsLayer(RenderType layer) {
        if (layer == null) return null;
        try {
            net.minecraft.resources.Identifier textureId = null;
            try {
                moscow.rockstar.mixin.accessors.RenderTypeAccessor typeAcc = (moscow.rockstar.mixin.accessors.RenderTypeAccessor) layer;
                net.minecraft.client.renderer.rendertype.RenderSetup setup = typeAcc.getState();
                Map<String, ?> textures = ((moscow.rockstar.mixin.accessors.RenderSetupAccessor) (Object) setup).getTextures();
                if (textures != null && !textures.isEmpty()) {
                    Object binding = textures.values().iterator().next();
                    if (binding != null) {
                        try {
                            java.lang.reflect.Method locationMethod = binding.getClass().getMethod("location");
                            textureId = (net.minecraft.resources.Identifier) locationMethod.invoke(binding);
                        } catch (Throwable t) {
                            System.err.println("[ShulkerOverlayCollector] binding class: " + binding.getClass().getName());
                            for (java.lang.reflect.Method m : binding.getClass().getDeclaredMethods()) {
                                System.err.println("  Method: " + m.getName() + " -> " + m.getReturnType().getName());
                            }
                            throw t;
                        }
                    }
                }
            } catch (Throwable t) {
                System.err.println("[ShulkerOverlayCollector] Failed to extract texture from layer: " + layer);
                t.printStackTrace();
            }

            if (textureId == null) {
                String layerStr = layer.toString().toLowerCase();
                if (layerStr.contains("shulker") || layerStr.contains("cutout")) {
                    textureId = net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/shulker_boxes.png");
                }
            }

            if (textureId != null) {
                return moscow.rockstar.systems.modules.modules.visuals.TargetESP.getChamsRenderType(textureId);
            }
        } catch (Throwable t) {
            // Ignore
        }
        return layer;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        this.originalOrdered = original.order(order);
        return this;
    }

    @Override
    public void submitItem(PoseStack matrices, ItemDisplayContext displayContext, int light, int overlay, int color, int[] layers, List<BakedQuad> elements, ItemStackRenderState.FoilType foil) {
        int[] newLayers = layers;
        if (layers != null && layers.length > 0) {
            newLayers = new int[layers.length];
            for (int i = 0; i < layers.length; i++) {
                newLayers[i] = highlightColor;
            }
        }

        List<BakedQuad> newElements = elements;
        if (useChams && elements != null && !elements.isEmpty()) {
            newElements = new java.util.ArrayList<>(elements.size());
            for (BakedQuad quad : elements) {
                BakedQuad.MaterialInfo mat = quad.materialInfo();
                if (mat != null) {
                    RenderType originalType = mat.itemRenderType();
                    RenderType newType = getChamsLayer(originalType);
                    if (newType != originalType) {
                        BakedQuad.MaterialInfo newMat = new BakedQuad.MaterialInfo(
                            mat.sprite(),
                            mat.layer(),
                            newType,
                            mat.tintIndex(),
                            mat.shade(),
                            mat.lightEmission()
                        );
                        BakedQuad newQuad = new BakedQuad(
                            quad.position0(),
                            quad.position1(),
                            quad.position2(),
                            quad.position3(),
                            quad.packedUV0(),
                            quad.packedUV1(),
                            quad.packedUV2(),
                            quad.packedUV3(),
                            quad.direction(),
                            newMat
                        );
                        newElements.add(newQuad);
                        continue;
                    }
                }
                newElements.add(quad);
            }
        }

        if (originalOrdered != null) {
            originalOrdered.submitItem(matrices, displayContext, light, forcedOverlay, highlightColor, newLayers, newElements, foil);
        } else {
            original.submitItem(matrices, displayContext, light, forcedOverlay, highlightColor, newLayers, newElements, foil);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, int light, int overlay, int color, net.minecraft.client.resources.model.sprite.SpriteId spriteId, net.minecraft.client.resources.model.sprite.SpriteGetter spriteGetter, int id, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, light, forcedOverlay, highlightColor, spriteId, spriteGetter, id, crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, light, forcedOverlay, highlightColor, spriteId, spriteGetter, id, crumblingOverlay);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, RenderType layer, int light, int overlay, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        RenderType finalLayer = useChams ? getChamsLayer(layer) : layer;
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, finalLayer, light, forcedOverlay, highlightColor, crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, finalLayer, light, forcedOverlay, highlightColor, crumblingOverlay);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, RenderType layer, int light, int overlay, int color, TextureAtlasSprite sprite, int id, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        RenderType finalLayer = useChams ? getChamsLayer(layer) : layer;
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, finalLayer, light, forcedOverlay, highlightColor, sprite, id, crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, finalLayer, light, forcedOverlay, highlightColor, sprite, id, crumblingOverlay);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, net.minecraft.resources.Identifier id, int light, int overlay, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (useChams) {
            RenderType chamsLayer = moscow.rockstar.systems.modules.modules.visuals.TargetESP.getChamsRenderType(id);
            if (originalOrdered != null) {
                originalOrdered.submitModel(model, state, matrices, chamsLayer, light, forcedOverlay, highlightColor, crumblingOverlay);
            } else {
                original.submitModel(model, state, matrices, chamsLayer, light, forcedOverlay, highlightColor, crumblingOverlay);
            }
        } else {
            if (originalOrdered != null) {
                originalOrdered.submitModel(model, state, matrices, id, light, forcedOverlay, highlightColor, crumblingOverlay);
            } else {
                original.submitModel(model, state, matrices, id, light, forcedOverlay, highlightColor, crumblingOverlay);
            }
        }
    }

    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType layer, int light, int overlay, TextureAtlasSprite sprite, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        RenderType finalLayer = useChams ? getChamsLayer(layer) : layer;
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, finalLayer, light, forcedOverlay, sprite, highlightColor, crumblingOverlay);
        } else {
            original.submitModelPart(part, matrices, finalLayer, light, forcedOverlay, sprite, highlightColor, crumblingOverlay);
        }
    }

    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType layer, int light, int overlay, TextureAtlasSprite sprite, boolean x, boolean y, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int id) {
        RenderType finalLayer = useChams ? getChamsLayer(layer) : layer;
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, finalLayer, light, forcedOverlay, sprite, x, y, highlightColor, crumblingOverlay, id);
        } else {
            original.submitModelPart(part, matrices, finalLayer, light, forcedOverlay, sprite, x, y, highlightColor, crumblingOverlay, id);
        }
    }

    @Override
    public void submitBlockModel(PoseStack matrices, RenderType layer, List<BlockStateModelPart> quads, int[] light, int overlay, int color, int alpha) {
        RenderType finalLayer = useChams ? getChamsLayer(layer) : layer;
        if (originalOrdered != null) {
            originalOrdered.submitBlockModel(matrices, finalLayer, quads, light, forcedOverlay, highlightColor, alpha);
        } else {
            original.submitBlockModel(matrices, finalLayer, quads, light, forcedOverlay, highlightColor, alpha);
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
        RenderType finalLayer = useChams ? getChamsLayer(layer) : layer;
        if (originalOrdered != null) {
            originalOrdered.submitCustomGeometry(matrices, finalLayer, custom);
        } else {
            original.submitCustomGeometry(matrices, finalLayer, custom);
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
        RenderType finalLayer = useChams ? getChamsLayer(layer) : layer;
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, finalLayer, light, forcedOverlay, sprite, x, y, highlightColor, null, 0);
        } else {
            original.submitModelPart(part, matrices, finalLayer, light, forcedOverlay, sprite, x, y, highlightColor, null, 0);
        }
    }

    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType layer, int light, int overlay, TextureAtlasSprite sprite) {
        RenderType finalLayer = useChams ? getChamsLayer(layer) : layer;
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, finalLayer, light, forcedOverlay, sprite, highlightColor, null);
        } else {
            original.submitModelPart(part, matrices, finalLayer, light, forcedOverlay, sprite, highlightColor, null);
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
        if (originalOrdered != null) {
            originalOrdered.submitText(matrices, x, y, text, shadow, displayMode, color, light, forcedOverlay, opacity);
        } else {
            original.submitText(matrices, x, y, text, shadow, displayMode, color, light, forcedOverlay, opacity);
        }
    }
}
