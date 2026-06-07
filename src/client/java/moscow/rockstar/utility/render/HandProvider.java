package moscow.rockstar.utility.render;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.modules.modules.visuals.CustomHand;
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

public class HandProvider implements SubmitNodeCollector, OrderedSubmitNodeCollector {
    private final SubmitNodeCollector original;
    private OrderedSubmitNodeCollector originalOrdered;
    private final float red, green, blue, alpha;
    private CustomHand customHandCache;

    private CustomHand getCustomHand() {
        if (customHandCache == null) {
            try {
                customHandCache = Rockstar.getInstance().getModuleManager().getModule(CustomHand.class);
            } catch (Exception e) {}
        }
        return customHandCache;
    }

    public HandProvider(SubmitNodeCollector original, float red, float green, float blue, float alpha) {
        this.original = original;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    private int multiplyColor(int color) {
        CustomHand customHand = getCustomHand();
        if (customHand == null || !customHand.isEnabled()) return color;

        int originalA = (color >> 24) & 0xFF;
        int originalR = (color >> 16) & 0xFF;
        int originalG = (color >> 8) & 0xFF;
        int originalB = color & 0xFF;

        int a = (int) (originalA * alpha);

        int r, g, b;
        if (customHand.isEffectGlass()) {
            // Glass effect: blend 80% accent color and 20% original texture color to look highly translucent and glowy
            r = (int) (originalR * 0.2f + (red * 255.0f) * 0.8f);
            g = (int) (originalG * 0.2f + (green * 255.0f) * 0.8f);
            b = (int) (originalB * 0.2f + (blue * 255.0f) * 0.8f);
        } else if (customHand.isEffectBlur()) {
            // Blur effect: blend 60% accent color and 40% original texture color for frosted appearance
            r = (int) (originalR * 0.4f + (red * 255.0f) * 0.6f);
            g = (int) (originalG * 0.4f + (green * 255.0f) * 0.6f);
            b = (int) (originalB * 0.4f + (blue * 255.0f) * 0.6f);
        } else if (customHand.isEffectFlare()) {
            // Flare effect: pulsating warm fire color with a flicker.
            // Pulse uses System.nanoTime so it animates without needing an extra tick wiring.
            float t = (float) ((System.nanoTime() / 1_000_000L) % 100000L) / 1000.0f;
            float pulse = 0.5f + 0.5f * (float) Math.sin(t * 6.2831853f * 1.3f);
            float flicker = 0.85f + 0.15f * (float) Math.sin(t * 53.0f);
            float intensity = pulse * flicker;

            // Warm fire palette: bright orange-red core, yellow highlight
            float fireR = 1.00f;
            float fireG = 0.45f + 0.25f * intensity;
            float fireB = 0.10f * intensity;

            float mix = 0.55f + 0.30f * intensity;
            float inv = 1.0f - mix;
            r = (int) (originalR * inv + fireR * 255.0f * mix);
            g = (int) (originalG * inv + fireG * 255.0f * mix);
            b = (int) (originalB * inv + fireB * 255.0f * mix);
        } else if (customHand.isEffectGlow()) {
            // Glow effect: keep original texture mostly intact but add an additive accent-color glow
            // so the hand reads as "lit from within / rim-glowing" with the accent color.
            float boost = 0.55f;
            r = (int) Math.min(255, originalR + (red * 255.0f) * boost);
            g = (int) Math.min(255, originalG + (green * 255.0f) * boost);
            b = (int) Math.min(255, originalB + (blue * 255.0f) * boost);
        } else {
            // Default: pure multiplication
            r = (int) (originalR * red);
            g = (int) (originalG * green);
            b = (int) (originalB * blue);
        }

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        a = Math.max(0, Math.min(255, a));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        this.originalOrdered = original.order(order);
        return this;
    }

    private RenderType getTranslucentLayer(RenderType layer) {
        if (layer == null) return null;
        try {
            net.minecraft.resources.Identifier textureId = null;
            try {
                moscow.rockstar.mixin.accessors.RenderTypeAccessor typeAcc = (moscow.rockstar.mixin.accessors.RenderTypeAccessor) layer;
                net.minecraft.client.renderer.rendertype.RenderSetup setup = typeAcc.getState();
                java.util.Map<String, ?> textures = ((moscow.rockstar.mixin.accessors.RenderSetupAccessor) (Object) setup).getTextures();
                if (textures != null && !textures.isEmpty()) {
                    Object binding = textures.values().iterator().next();
                    if (binding != null) {
                        java.lang.reflect.Method locationMethod = binding.getClass().getMethod("location");
                        textureId = (net.minecraft.resources.Identifier) locationMethod.invoke(binding);
                    }
                }
            } catch (Throwable t) {
                // Ignore and try fallback
            }

            if (textureId != null) {
                String name = layer.toString().toLowerCase();
                if (name.contains("item")) {
                    return net.minecraft.client.renderer.rendertype.RenderTypes.itemTranslucent(textureId);
                } else {
                    return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(textureId);
                }
            }
        } catch (Throwable t) {
            // Ignore
        }
        return layer;
    }

    @Override
    public void submitItem(PoseStack matrices, ItemDisplayContext displayContext, int light, int overlay, int color, int[] layers, List<BakedQuad> elements, ItemStackRenderState.FoilType foil) {
        CustomHand customHand = getCustomHand();
        ItemStackRenderState.FoilType finalFoil = foil;
        if (customHand != null && customHand.isEnabled() && customHand.isEffectGlass()) {
            finalFoil = ItemStackRenderState.FoilType.STANDARD;
        }

        int[] newLayers = layers;
        if (layers != null && layers.length > 0) {
            newLayers = new int[layers.length];
            for (int i = 0; i < layers.length; i++) {
                newLayers[i] = multiplyColor(layers[i]);
            }
        }

        List<BakedQuad> newElements = elements;
        if (elements != null && !elements.isEmpty()) {
            newElements = new java.util.ArrayList<>(elements.size());
            for (BakedQuad quad : elements) {
                BakedQuad.MaterialInfo mat = quad.materialInfo();
                if (mat != null) {
                    RenderType originalType = mat.itemRenderType();
                    RenderType newType = getTranslucentLayer(originalType);
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
            originalOrdered.submitItem(matrices, displayContext, light, overlay, multiplyColor(color), newLayers, newElements, finalFoil);
        } else {
            original.submitItem(matrices, displayContext, light, overlay, multiplyColor(color), newLayers, newElements, finalFoil);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, int light, int overlay, int color, net.minecraft.client.resources.model.sprite.SpriteId spriteId, net.minecraft.client.resources.model.sprite.SpriteGetter spriteGetter, int id, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, light, overlay, multiplyColor(color), spriteId, spriteGetter, id, crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, light, overlay, multiplyColor(color), spriteId, spriteGetter, id, crumblingOverlay);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, RenderType layer, int light, int overlay, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        RenderType transLayer = getTranslucentLayer(layer);
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, transLayer, light, overlay, multiplyColor(color), crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, transLayer, light, overlay, multiplyColor(color), crumblingOverlay);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, RenderType layer, int light, int overlay, int color, TextureAtlasSprite sprite, int id, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        RenderType transLayer = getTranslucentLayer(layer);
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, transLayer, light, overlay, multiplyColor(color), sprite, id, crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, transLayer, light, overlay, multiplyColor(color), sprite, id, crumblingOverlay);
        }
    }

    @Override
    public void submitModel(Model model, Object state, PoseStack matrices, net.minecraft.resources.Identifier id, int light, int overlay, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (originalOrdered != null) {
            originalOrdered.submitModel(model, state, matrices, id, light, overlay, multiplyColor(color), crumblingOverlay);
        } else {
            original.submitModel(model, state, matrices, id, light, overlay, multiplyColor(color), crumblingOverlay);
        }
    }

    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType layer, int light, int overlay, TextureAtlasSprite sprite, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        RenderType transLayer = getTranslucentLayer(layer);
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, transLayer, light, overlay, sprite, multiplyColor(color), crumblingOverlay);
        } else {
            original.submitModelPart(part, matrices, transLayer, light, overlay, sprite, multiplyColor(color), crumblingOverlay);
        }
    }

    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType layer, int light, int overlay, TextureAtlasSprite sprite, boolean x, boolean y, int color, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int id) {
        RenderType transLayer = getTranslucentLayer(layer);
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, transLayer, light, overlay, sprite, x, y, multiplyColor(color), crumblingOverlay, id);
        } else {
            original.submitModelPart(part, matrices, transLayer, light, overlay, sprite, x, y, multiplyColor(color), crumblingOverlay, id);
        }
    }

    @Override
    public void submitBlockModel(PoseStack matrices, RenderType layer, List<BlockStateModelPart> quads, int[] light, int overlay, int color, int alpha) {
        RenderType transLayer = getTranslucentLayer(layer);
        if (originalOrdered != null) {
            originalOrdered.submitBlockModel(matrices, transLayer, quads, light, overlay, multiplyColor(color), alpha);
        } else {
            original.submitBlockModel(matrices, transLayer, quads, light, overlay, multiplyColor(color), alpha);
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
        RenderType transLayer = getTranslucentLayer(layer);
        if (originalOrdered != null) {
            originalOrdered.submitCustomGeometry(matrices, transLayer, custom);
        } else {
            original.submitCustomGeometry(matrices, transLayer, custom);
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
        RenderType transLayer = getTranslucentLayer(layer);
        int packedColor = multiplyColor(0xFFFFFFFF);
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, transLayer, light, overlay, sprite, x, y, packedColor, null, 0);
        } else {
            original.submitModelPart(part, matrices, transLayer, light, overlay, sprite, x, y, packedColor, null, 0);
        }
    }

    @Override
    public void submitModelPart(ModelPart part, PoseStack matrices, RenderType layer, int light, int overlay, TextureAtlasSprite sprite) {
        RenderType transLayer = getTranslucentLayer(layer);
        int packedColor = multiplyColor(0xFFFFFFFF);
        if (originalOrdered != null) {
            originalOrdered.submitModelPart(part, matrices, transLayer, light, overlay, sprite, packedColor, null);
        } else {
            original.submitModelPart(part, matrices, transLayer, light, overlay, sprite, packedColor, null);
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
            originalOrdered.submitNameTag(matrices, pos, multiplyColor(color), text, shadow, light, scale, cameraState);
        } else {
            original.submitNameTag(matrices, pos, multiplyColor(color), text, shadow, light, scale, cameraState);
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
            originalOrdered.submitShadow(matrices, opacity * alpha, pieces);
        } else {
            original.submitShadow(matrices, opacity * alpha, pieces);
        }
    }

    @Override
    public void submitText(PoseStack matrices, float x, float y, FormattedCharSequence text, boolean shadow, Font.DisplayMode displayMode, int color, int light, int overlay, int opacity) {
        if (originalOrdered != null) {
            originalOrdered.submitText(matrices, x, y, text, shadow, displayMode, multiplyColor(color), light, overlay, (int)(opacity * alpha));
        } else {
            original.submitText(matrices, x, y, text, shadow, displayMode, multiplyColor(color), light, overlay, (int)(opacity * alpha));
        }
    }
}
