package moscow.rockstar.mixin.minecraft.client.gui.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import moscow.rockstar.utility.render.UiOverlayRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @Shadow
    private net.minecraft.client.renderer.state.gui.GuiRenderState renderState;

    @Shadow
    private net.minecraft.client.gui.render.GuiItemAtlas itemAtlas;

    @org.spongepowered.asm.mixin.Unique
    private static final java.util.Set<java.lang.Object> rockstar$accumulatedIdentities = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    @org.spongepowered.asm.mixin.Unique
    private static final java.util.List<net.minecraft.client.renderer.item.TrackingItemStackRenderState> rockstar$cachedFallbackStates = new java.util.ArrayList<>();

    @org.spongepowered.asm.mixin.Unique
    private static boolean rockstar$fallbackInitialized = false;

    @org.spongepowered.asm.mixin.Unique
    private final java.util.List<net.minecraft.client.renderer.item.TrackingItemStackRenderState> rockstar$tempFrameStates = new java.util.ArrayList<>();

    /**
     * Must run after {@code draw()} finishes — injecting at {@code draw} RETURN still runs while vanilla
     * may hold an open {@link com.mojang.blaze3d.systems.RenderPass}, which breaks DynamicTransforms UBO writes
     * and causes flicker / vertex garbage.
     */
    @Inject(method = "render", at = @At("RETURN"))
    private void rockstar$flushDeferredDraws(GpuBufferSlice fogBuffer, CallbackInfo ci) {
        moscow.rockstar.utility.render.GuiDrawContextHolder.captureGuiRenderer((net.minecraft.client.gui.render.GuiRenderer)(Object)this);
        UiOverlayRenderer.flushDraws(fogBuffer);
    }



    @Inject(method = "prepareItemElements", at = @At("HEAD"))
    private void rockstar$injectCustomItemIdentities(CallbackInfo ci) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        
        java.util.Set<java.lang.Object> identities = ((moscow.rockstar.mixin.accessors.GuiRenderStateAccessor) this.renderState).rockstar$getItemModelIdentities();
        if (identities == null) return;
        
        // 1. Initialize fallback cache once
        if (!rockstar$fallbackInitialized) {
            rockstar$fallbackInitialized = true;
            net.minecraft.world.item.Item[] fallbackItems = {
                net.minecraft.world.item.Items.DRIED_KELP,
                net.minecraft.world.item.Items.NETHERITE_SCRAP,
                net.minecraft.world.item.Items.ENDER_PEARL,
                net.minecraft.world.item.Items.GOLDEN_APPLE,
                net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE,
                net.minecraft.world.item.Items.SNOWBALL,
                net.minecraft.world.item.Items.ENDER_EYE,
                net.minecraft.world.item.Items.SUGAR,
                net.minecraft.world.item.Items.PHANTOM_MEMBRANE,
                
                // Netherite Set
                net.minecraft.world.item.Items.NETHERITE_HELMET,
                net.minecraft.world.item.Items.NETHERITE_CHESTPLATE,
                net.minecraft.world.item.Items.NETHERITE_LEGGINGS,
                net.minecraft.world.item.Items.NETHERITE_BOOTS,
                net.minecraft.world.item.Items.NETHERITE_SWORD,
                net.minecraft.world.item.Items.NETHERITE_AXE,
                net.minecraft.world.item.Items.NETHERITE_PICKAXE,
                
                // Diamond Set
                net.minecraft.world.item.Items.DIAMOND_HELMET,
                net.minecraft.world.item.Items.DIAMOND_CHESTPLATE,
                net.minecraft.world.item.Items.DIAMOND_LEGGINGS,
                net.minecraft.world.item.Items.DIAMOND_BOOTS,
                net.minecraft.world.item.Items.DIAMOND_SWORD,
                net.minecraft.world.item.Items.DIAMOND_AXE,
                
                // Iron Set
                net.minecraft.world.item.Items.IRON_HELMET,
                net.minecraft.world.item.Items.IRON_CHESTPLATE,
                net.minecraft.world.item.Items.IRON_LEGGINGS,
                net.minecraft.world.item.Items.IRON_BOOTS,
                net.minecraft.world.item.Items.IRON_SWORD,
                net.minecraft.world.item.Items.IRON_AXE,
                
                // Gold Set
                net.minecraft.world.item.Items.GOLDEN_HELMET,
                net.minecraft.world.item.Items.GOLDEN_CHESTPLATE,
                net.minecraft.world.item.Items.GOLDEN_LEGGINGS,
                net.minecraft.world.item.Items.GOLDEN_BOOTS,
                net.minecraft.world.item.Items.GOLDEN_SWORD,
                
                // Chainmail Set
                net.minecraft.world.item.Items.CHAINMAIL_HELMET,
                net.minecraft.world.item.Items.CHAINMAIL_CHESTPLATE,
                net.minecraft.world.item.Items.CHAINMAIL_LEGGINGS,
                net.minecraft.world.item.Items.CHAINMAIL_BOOTS,
                
                // Leather Set
                net.minecraft.world.item.Items.LEATHER_HELMET,
                net.minecraft.world.item.Items.LEATHER_CHESTPLATE,
                net.minecraft.world.item.Items.LEATHER_LEGGINGS,
                net.minecraft.world.item.Items.LEATHER_BOOTS,
                
                // Other weapons/utility
                net.minecraft.world.item.Items.SHIELD,
                net.minecraft.world.item.Items.TOTEM_OF_UNDYING,
                net.minecraft.world.item.Items.BOW,
                net.minecraft.world.item.Items.CROSSBOW,
                net.minecraft.world.item.Items.ELYTRA,
                net.minecraft.world.item.Items.ARROW,
                net.minecraft.world.item.Items.TRIDENT
            };
            for (net.minecraft.world.item.Item item : fallbackItems) {
                try {
                    net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item);
                    net.minecraft.client.renderer.item.TrackingItemStackRenderState state = new net.minecraft.client.renderer.item.TrackingItemStackRenderState();
                    net.minecraft.client.Minecraft.getInstance().getItemModelResolver().updateForLiving(
                        state, stack, net.minecraft.world.item.ItemDisplayContext.GUI, mc.player
                    );
                    java.lang.Object identity = state.getModelIdentity();
                    if (identity != null) {
                        rockstar$accumulatedIdentities.add(identity);
                        rockstar$cachedFallbackStates.add(state);
                    }
                } catch (Throwable t) {
                    // Ignore
                }
            }
        }
        
        // Clear temp dynamic frame states
        rockstar$tempFrameStates.clear();
        
        // 2. Resolve dynamic items (player, target, prediction, cooldowns)
        // 2a. Player items
        try {
            for (net.minecraft.world.item.ItemStack item : moscow.rockstar.utility.game.ClientEntityUtility.getArmorItems(mc.player)) {
                if (item != null && !item.isEmpty()) {
                    rockstar$resolveDynamicItem(identities, item, mc.player);
                }
            }
            if (mc.player.getMainHandItem() != null && !mc.player.getMainHandItem().isEmpty()) {
                rockstar$resolveDynamicItem(identities, mc.player.getMainHandItem(), mc.player);
            }
            if (mc.player.getOffhandItem() != null && !mc.player.getOffhandItem().isEmpty()) {
                rockstar$resolveDynamicItem(identities, mc.player.getOffhandItem(), mc.player);
            }
            if (mc.player.isUsingItem() && mc.player.getActiveItem() != null && !mc.player.getActiveItem().isEmpty()) {
                rockstar$resolveDynamicItem(identities, mc.player.getActiveItem(), mc.player);
            }
        } catch (Throwable t) {
            // Ignore
        }
        
        // 2b. Target items (armor and hands of the target)
        try {
            net.minecraft.world.entity.Entity t = moscow.rockstar.Rockstar.getInstance().getTargetManager().getCurrentTarget();
            net.minecraft.world.entity.LivingEntity target = null;
            if (t instanceof net.minecraft.world.entity.LivingEntity && t.isAlive()) {
                target = (net.minecraft.world.entity.LivingEntity) t;
            } else {
                net.minecraft.world.entity.Entity crosshairEntity = moscow.rockstar.utility.game.ClientEntityUtility.getCrosshairEntity();
                if (crosshairEntity instanceof net.minecraft.world.entity.LivingEntity livingCrosshair && livingCrosshair.isAlive()) {
                    target = livingCrosshair;
                }
            }
            if (target == null) {
                moscow.rockstar.ui.hud.impl.TargetHud targetHud = moscow.rockstar.Rockstar.getInstance().getHud().getElementByName("hud.targethud");
                if (targetHud != null) {
                    target = targetHud.getTargetEntity();
                }
            }
            if (target != null) {
                for (net.minecraft.world.item.ItemStack item : moscow.rockstar.utility.game.ClientEntityUtility.getArmorItems(target)) {
                    if (item != null && !item.isEmpty()) {
                        rockstar$resolveDynamicItem(identities, item, mc.player);
                    }
                }
                if (target.getMainHandItem() != null && !target.getMainHandItem().isEmpty()) {
                    rockstar$resolveDynamicItem(identities, target.getMainHandItem(), mc.player);
                }
                if (target.getOffhandItem() != null && !target.getOffhandItem().isEmpty()) {
                    rockstar$resolveDynamicItem(identities, target.getOffhandItem(), mc.player);
                }
                if (target.isUsingItem() && target.getActiveItem() != null && !target.getActiveItem().isEmpty()) {
                    rockstar$resolveDynamicItem(identities, target.getActiveItem(), mc.player);
                }
            }
        } catch (Throwable t) {
            // Ignore
        }
        
        // 2c. Prediction items (items from current Prediction module targets)
        try {
            moscow.rockstar.systems.modules.modules.visuals.Prediction prediction = 
                moscow.rockstar.Rockstar.getInstance().getModuleManager().getModule(moscow.rockstar.systems.modules.modules.visuals.Prediction.class);
            if (prediction != null && prediction.isEnabled()) {
                for (var pred : prediction.getPredicted()) {
                    net.minecraft.world.item.ItemStack stack = null;
                    if (pred.entity() instanceof net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile item) {
                        stack = item.getItem();
                    } else if (pred.entity() instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow item) {
                        stack = item.getPickupItemStackOrigin();
                    } else if (pred.entity() instanceof net.minecraft.world.entity.item.ItemEntity item) {
                        stack = item.getItem();
                    }
                    if (stack != null && !stack.isEmpty()) {
                        rockstar$resolveDynamicItem(identities, stack, mc.player);
                    }
                }
            }
        } catch (Throwable t) {
            // Ignore
        }
        
        // 2d. Cooldown items from Cooldowns HUD
        try {
            moscow.rockstar.ui.hud.impl.Cooldowns cooldownsHud = moscow.rockstar.Rockstar.getInstance().getHud().getElementByName("hud.cooldowns");
            if (cooldownsHud != null) {
                for (net.minecraft.world.item.Item item : cooldownsHud.getActiveCooldownItems()) {
                    rockstar$resolveDynamicItem(identities, new net.minecraft.world.item.ItemStack(item), mc.player);
                }
            }
        } catch (Throwable t) {
            // Ignore
        }
        
        // Ensure all previously accumulated identities are retained to keep the atlas stable and prevent flickering
        synchronized (rockstar$accumulatedIdentities) {
            identities.addAll(rockstar$accumulatedIdentities);
        }
    }

    @Inject(method = "prepareItemElements", at = @At("RETURN"))
    private void rockstar$populateCustomItemSlots(CallbackInfo ci) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null || this.itemAtlas == null) return;
        
        // 1. Populate cached fallback item states
        for (net.minecraft.client.renderer.item.TrackingItemStackRenderState state : rockstar$cachedFallbackStates) {
            try {
                this.itemAtlas.getOrUpdate(state);
            } catch (Throwable t) {
                // Ignore
            }
        }
        
        // 2. Populate dynamic items resolved during this frame
        for (net.minecraft.client.renderer.item.TrackingItemStackRenderState state : rockstar$tempFrameStates) {
            try {
                this.itemAtlas.getOrUpdate(state);
            } catch (Throwable t) {
                // Ignore
            }
        }
        
        // Clear frame states
        rockstar$tempFrameStates.clear();
    }
    
    @org.spongepowered.asm.mixin.Unique
    private void rockstar$resolveDynamicItem(java.util.Set<java.lang.Object> identities, net.minecraft.world.item.ItemStack stack, net.minecraft.client.player.LocalPlayer player) {
        if (stack == null || stack.isEmpty() || player == null) return;
        try {
            net.minecraft.client.renderer.item.TrackingItemStackRenderState state = new net.minecraft.client.renderer.item.TrackingItemStackRenderState();
            net.minecraft.client.Minecraft.getInstance().getItemModelResolver().updateForLiving(
                state, stack, net.minecraft.world.item.ItemDisplayContext.GUI, player
            );
            java.lang.Object identity = state.getModelIdentity();
            if (identity != null) {
                identities.add(identity);
                rockstar$accumulatedIdentities.add(identity);
                rockstar$tempFrameStates.add(state);
            }
        } catch (Throwable t) {
            // Ignore
        }
    }
}
