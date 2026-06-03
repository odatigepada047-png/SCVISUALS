package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.lang.reflect.Field;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.shared.PredicateValue;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.PotionUtility;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.GLStateSnapshot;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.client.player.AbstractClientPlayer;
import com.mojang.math.Axis;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.utility.render.MeshDrawHelper;
import moscow.rockstar.utility.render.TextureBinder;
import org.lwjgl.opengl.GL11;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import net.minecraft.util.Mth;

@ModuleInfo(name="Prediction", category=ModuleCategory.VISUALS)
public class Prediction extends BaseModule {
    private static final double MIN_SEGMENT_DISTANCE_SQR = 0.0025;
    private static final double MIN_CAMERA_DISTANCE_SQR = 1.44;
    private static final double DUPLICATE_START_DISTANCE_SQR = 0.04;
    private static final double DUPLICATE_END_DISTANCE_SQR = 0.25;
    private static final double HUD_DUPLICATE_DISTANCE_SQR = 0.36;
    private static final double LANDED_DUPLICATE_DISTANCE_SQR = 0.16;
    private static Field inGroundField;
    private static Field inGroundTimeField;
    private final List<Predicted> predicted = new ArrayList<>();
    private final List<Landed> landed = new ArrayList<>();
    private final List<Vec3> smoothedLandedPositions = new ArrayList<>();
    private final SelectSetting entities = new SelectSetting(this, "modules.settings.prediction.entities");
    private final ModeSetting renderMode = new ModeSetting(this, "modules.settings.prediction.render_mode");
    private final ModeSetting.Value defaultMode = new ModeSetting.Value(this.renderMode, "modules.settings.prediction.render_mode.default");
    private final ModeSetting.Value glowMode = new ModeSetting.Value(this.renderMode, "modules.settings.prediction.render_mode.glow").select();
    private final BooleanSetting syncWithTheme = new BooleanSetting(this, "modules.settings.sync_with_theme").enable();
    private final ColorSetting color = new ColorSetting(this, "color", () -> this.syncWithTheme.isEnabled()).color(Colors.getAccentColor());
    private final BooleanSetting inHand = new BooleanSetting(this, "modules.settings.prediction.hand").enable();
    private final BooleanSetting walls = new BooleanSetting(this, "modules.settings.prediction.walls").enable();
    private final BooleanSetting hud = new BooleanSetting(this, "modules.settings.prediction.hud");

    private final EventListener<HudRenderEvent> onRender2D = event -> {
        CustomDrawContext context = event.getContext();
        Matrix3x2fStack ms = context.getMatrices();
        List<Predicted> hudPredictions = this.collectHudPredictions();
        for (Predicted predict : hudPredictions) {
            Vec2 screenPos = Utils.worldToScreen(predict.vectors.getLast());
            if (screenPos == null) continue;
            float x = screenPos.x;
            float y = screenPos.y;
            Font font = Fonts.MEDIUM.getFont(13.0f);
            float height = font.height() + 6.0f;
            float yOff = -height;
            String name = predict.entity.getName().getString().replace("Брошенный эндер-жемчуг", "Эндер-жемчуг");
            if (predict.entity instanceof AbstractThrownPotion potion) {
                name = potion.getItem().getHoverName().getString();
            }
            name = name.replace("] ", "").replace("[", "") + String.format(" (%s сек)", TextUtility.formatNumber((float)predict.ticks / 20.0f));
            ItemStack stack;
            if (predict.entity instanceof ThrowableItemProjectile item) {
                stack = item.getItem();
            } else if (predict.entity instanceof AbstractArrow item) {
                stack = item.getPickupItemStackOrigin();
            } else if (predict.entity instanceof ItemEntity item) {
                stack = item.getItem();
            } else {
                stack = Items.ARROW.getDefaultInstance();
            }
            float distance = (float)predict.vectors.getLast().distanceTo(Prediction.mc.player.getEyePosition(1.0f));
            float scale = Mth.clamp((1.0f - distance / 20.0f), 0.5f, 1.0f);
            ms.pushMatrix();
            ms.translate(x, y);
            ms.scale(scale, scale);
            float firstWidth = font.width(name) + 20.0f;
            context.drawRect(-firstWidth / 2.0f, yOff, firstWidth, height, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
            context.drawItem(stack, -firstWidth / 2.0f, yOff, 1.0f);
            context.drawText(font, name, -firstWidth / 2.0f + 17.0f, yOff + 3.0f, Colors.WHITE);
            yOff += height;
            if (predict.entity instanceof Projectile projectile && projectile.getOwner() instanceof AbstractClientPlayer player) {
                String owner = "От " + (projectile.getOwner() == Prediction.mc.player ? "Вас" : projectile.getOwner().getName().getString());
                float secondWidth = font.width(owner) + 22.0f;
                context.drawRect(-secondWidth / 2.0f, yOff, secondWidth, height, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
                context.drawHead(player, -secondWidth / 2.0f, yOff, height, BorderRadius.ZERO, Colors.WHITE);
                context.drawText(font, owner, -secondWidth / 2.0f + 19.0f, yOff + 3.0f, Colors.WHITE);
                yOff += height;
            }
            if (predict.entity instanceof AbstractThrownPotion potion) {
                for (MobEffectInstance effect : PotionUtility.effects(potion.getItem())) {
                    String potionName = effect.getEffect().value().getDisplayName().getString();
                    int amplifier = effect.getAmplifier();
                    int duration = effect.getDuration();
                    String potionLevel = amplifier > 0 ? " " + (amplifier + 1) : "";
                    String potionTime = this.formatDuration(duration);
                    String fullPotionText = potionName + potionLevel + " (" + potionTime + ")";
                    float potionWidth = font.width(fullPotionText) + 6.0f;
                    context.drawRect(-potionWidth / 2.0f, yOff + 5.0f, potionWidth, height, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
                    context.drawText(font, fullPotionText, -potionWidth / 2.0f + 3.0f, yOff + 8.0f, ColorRGBA.fromInt(effect.getEffect().value().getColor()).withAlpha(255.0f));
                    yOff += height;
                }
            }
            ms.popMatrix();
        }
        if (this.hud.isEnabled()) {
            Font font = Fonts.MEDIUM.getFont(10.0f);
            float yOff = 0.0f;
            for (Predicted predict : hudPredictions) {
                if (predict.collidedEntity != Prediction.mc.player || predict.entity instanceof ThrownEnderpearl) continue;
                String name = predict.entity.getName().getString().replace("Брошенный эндер-жемчуг", "Эндер-жемчуг") + String.format(" (%s сек)", TextUtility.formatNumber((float)predict.ticks / 20.0f));
                context.drawCenteredText(font, "В вас летит " + name, sr.getGuiScaledWidth() / 2.0f, sr.getGuiScaledHeight() / 2.0f + 20.0f + yOff, Colors.WHITE);
                yOff += font.height() + 3.0f;
            }
        }
    };

    private final EventListener<Render3DEvent> onRender3D = event -> {
        if (RenderSystem.outputColorTextureOverride != null) {
            return;
        }
        moscow.rockstar.utility.render.ShaderColorHelper.reset();
        moscow.rockstar.utility.render.TextureBinder.unbind();
        moscow.rockstar.framework.shader.GlProgram.clearActive();
        PoseStack ms = event.pose();
        Camera camera = Prediction.mc.gameRenderer.getMainCamera();
        ms.pushPose();
        RenderUtility.setupRender3D(true);
        RenderUtility.prepareMatrices(ms);
        List<Predicted> renderablePredictions = this.collectRenderablePredictions(camera.position(), event.getGameTimeDeltaPartialTick());

        GLStateSnapshot glState = GLStateSnapshot.capture();

        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDepthMask(false);

            if (this.walls.isEnabled()) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }

            if (this.defaultMode.isSelected()) {
                GlProgram.usePositionColor();
                BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
                for (Predicted pred : renderablePredictions) {
                    this.drawPredictionPath(ms, builder, pred, camera.position(), event.getGameTimeDeltaPartialTick());
                }
                MeshData mesh = builder.build();
                if (mesh != null) {
                    MeshDrawHelper.drawBuilt(mesh);
                }
            } else {
                Identifier id = Rockstar.id("textures/bloom.png");
                TextureBinder.bind(id);
                GlProgram.usePositionTexColor();
                BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                for (Predicted pred : renderablePredictions) {
                    Vec3 prevPos = pred.vectors.getFirst();
                    Vec3 entityPos = Utils.getInterpolatedPos(pred.entity, event.getGameTimeDeltaPartialTick());
                    if (entityPos.distanceTo(Prediction.mc.player.getEyePosition(1.0f)) > 2.0) {
                        for (int i = 0; i < 10; ++i) {
                            float t = (float)i / 10.0f;
                            Vec3 interpolatedPos = entityPos.add(prevPos.subtract(entityPos).scale(t));
                            this.drawGlow(ms, interpolatedPos, buffer, (float)prevPos.distanceTo(entityPos) / 3.0f, 1.0f);
                            this.drawGlow(ms, interpolatedPos, buffer, (float)prevPos.distanceTo(entityPos) * 2.0f, 0.05f);
                        }
                    }
                    for (Vec3 pos : pred.vectors) {
                        if (pos.distanceTo(Prediction.mc.player.getEyePosition(1.0f)) > 2.0) {
                            for (int i = 0; i < 10; ++i) {
                                float t = (float)i / 10.0f;
                                Vec3 interpolatedPos = prevPos.add(pos.subtract(prevPos).scale(t));
                                this.drawGlow(ms, interpolatedPos, buffer, (float)pos.distanceTo(prevPos) / 3.0f, 1.0f);
                                this.drawGlow(ms, interpolatedPos, buffer, (float)pos.distanceTo(prevPos) * 2.0f, 0.05f);
                            }
                        }
                        prevPos = pos;
                    }
                    float size = 9.0f;
                    if (!(pred.entity instanceof AbstractThrownPotion)) continue;
                    ms.pushPose();
                    Vec3 last = pred.vectors.getLast();
                    ms.translate(last.x, last.y, last.z);
                    ms.mulPose(Axis.XN.rotationDegrees(-90.0f));
                    DrawUtility.drawImage(ms, buffer, -size / 2.0f, -size / 2.0f, 0.0, size, size, this.getRenderColor());
                    ms.popPose();
                }
                MeshData mesh = buffer.build();
                if (mesh != null) {
                    MeshDrawHelper.drawBuilt(mesh);
                }
            }

            this.drawImpactOrbs(ms, camera, event.getGameTimeDeltaPartialTick());

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            GlProgram.usePositionColor();
            BufferBuilder quadsBuffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (Landed land : this.landed) {
                if (land.collidedEntity == null) continue;
                Draw3DUtility.renderFilledBox(ms, quadsBuffer, land.collidedEntity.getBoundingBox(), this.getRenderColor().mulAlpha(0.5f));
            }
            MeshData quadsMesh = quadsBuffer.build();
            if (quadsMesh != null) {
                MeshDrawHelper.drawBuilt(quadsMesh);
            }

            BufferBuilder linesBuffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH);
            for (Landed land : this.landed) {
                if (land.collidedEntity == null) continue;
                this.drawBoxLines(ms, linesBuffer, land.collidedEntity.getBoundingBox(), this.getRenderColor());
            }
            MeshData linesMesh = linesBuffer.build();
            if (linesMesh != null) {
                MeshDrawHelper.drawBuilt(linesMesh);
            }
        } finally {
            TextureBinder.unbind();
            GlProgram.clearActive();
            glState.restore();
            RenderUtility.endRender3D();
        }
        ms.popPose();
    };

    public Prediction() {
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.pearls", entity -> entity instanceof ThrownEnderpearl).select();
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.tridents", entity -> entity instanceof ThrownTrident).select();
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.snowballs", entity -> entity instanceof Snowball).select();
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.arrows", entity -> entity instanceof Arrow).select();
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.potions", entity -> entity instanceof AbstractThrownPotion).select();
        new PredicateValue<Entity>(this.entities, "modules.settings.prediction.entities.items", entity -> entity instanceof ItemEntity);
    }

    private ColorRGBA getRenderColor() {
        return (this.syncWithTheme.isEnabled() ? Colors.getAccentColor() : this.color.getColor()).withAlpha(127.0f);
    }

    @Override
    public void tick() {
        this.predicted.clear();
        this.landed.clear();
        ArrayList<Arrow> projectiles = new ArrayList<>();
        if (this.inHand.isEnabled()) {
            ItemStack handStack = Prediction.mc.player.getMainHandItem();
            Projectile inHand = null;
            if (handStack.getItem() instanceof EnderpearlItem) {
                inHand = new ThrownEnderpearl(Prediction.mc.level, Prediction.mc.player, handStack);
            } else if (handStack.getItem() instanceof TridentItem && Prediction.mc.player.isUsingItem()) {
                inHand = new ThrownTrident(Prediction.mc.level, Prediction.mc.player, handStack);
            } else if (handStack.getItem() instanceof BowItem && Prediction.mc.player.isUsingItem()) {
                ItemStack arrowStack = new ItemStack(Items.ARROW);
                inHand = new Arrow(Prediction.mc.level, Prediction.mc.player, arrowStack, handStack);
            } else if (handStack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(handStack)) {
                boolean hasMultishot = EnchantmentUtility.getEnchantmentLevel(handStack, (ResourceKey<Enchantment>)Enchantments.MULTISHOT) > 0;
                ItemStack arrowStack = new ItemStack(Items.ARROW);
                if (hasMultishot) {
                    for (int i = 0; i < 3; ++i) {
                        Arrow arrow = new Arrow(Prediction.mc.level, Prediction.mc.player, arrowStack, handStack);
                        projectiles.add(arrow);
                    }
                } else {
                    inHand = new Arrow(Prediction.mc.level, Prediction.mc.player, arrowStack, handStack);
                }
            }
            if (inHand != null) {
                Projectile projectile = inHand;
                float speed = 1.5f;
                if (inHand instanceof ThrownTrident) {
                    speed = 2.5f;
                } else if (inHand instanceof Arrow) {
                    speed = 3.0f;
                }
                this.setVelocity(projectile, Prediction.mc.player, Prediction.mc.player.getXRot(), Prediction.mc.player.getYRot(), 0.0f, speed, 1.0f);
                this.predict(projectile, true);
            }
        }
        if (!projectiles.isEmpty()) {
            float speed = 3.15f;
            float spreadAngle = 10.0f;
            for (int i = 0; i < projectiles.size(); ++i) {
                Projectile projectile = projectiles.get(i);
                float yawOffset = 0.0f;
                if (i == 0) {
                    yawOffset = -spreadAngle;
                } else if (i == 2) {
                    yawOffset = spreadAngle;
                }
                this.setVelocity(projectile, Prediction.mc.player, Prediction.mc.player.getXRot(), Prediction.mc.player.getYRot() + yawOffset, 0.0f, speed, 1.0f);
                this.predict(projectile, true);
            }
        }
        for (Entity entity : Prediction.mc.level.entitiesForRendering()) {
            this.predict(entity, false);
        }
    }

    private void predict(Entity entity, boolean inHand) {
        List<AbstractClientPlayer> sortedPlayers;
        if (!this.isValid(entity)) {
            return;
        }
        if (entity instanceof Projectile projectile && projectile.getOwner() == null && !(sortedPlayers = Prediction.mc.level.players()).isEmpty()) {
            Collections.sort(sortedPlayers, Comparator.comparingDouble(player -> player.distanceTo(projectile)));
            projectile.setOwner(sortedPlayers.getFirst());
        }
        ArrayList<Vec3> positions = new ArrayList<>();
        Vec3 lastPos = entity.position();
        Vec3 lastMotion = entity.getDeltaMovement();
        Entity collidedEntity = null;
        int ticks = 0;
        BlockHitResult blockHitResult = null;
        int i = 0;
        while (i < 150) {
            Vec3 motion = this.predictMotion(entity, lastMotion);
            Vec3 pos = lastPos.add(motion);
            ticks = i++;
            blockHitResult = Prediction.mc.level.clip(new ClipContext(lastPos, pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            Entity collided = this.checkEntityCollision(entity, pos);
            if (collided != null) {
                positions.add(pos);
                collidedEntity = collided;
                break;
            }
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                positions.add(blockHitResult.getLocation());
                break;
            }
            positions.add(pos);
            lastPos = pos;
            lastMotion = motion;
        }
        if (!positions.isEmpty()) {
            if (inHand) {
                this.landed.add(new Landed(entity, positions.getLast(), ticks, collidedEntity, blockHitResult));
            } else {
                this.predicted.add(new Predicted(entity, positions, ticks, collidedEntity));
            }
        }
    }

    private void drawGlow(PoseStack ms, Vec3 pos, BufferBuilder buffer, float size, float alpha) {
        ms.pushPose();
        ms.translate(pos.x, pos.y, pos.z);
        ms.mulPose(Prediction.mc.gameRenderer.getMainCamera().rotation());
        DrawUtility.drawImage(ms, buffer, -size / 2.0f, -size / 2.0f, 0.0, size, size, this.getRenderColor().withAlpha(this.getRenderColor().getAlpha() * alpha));
        ms.popPose();
    }

    private boolean isValid(Entity entity) {
        boolean valid = false;
        for (SelectSetting.Value selectedValue : this.entities.getSelectedValues()) {
            PredicateValue predicateValue = (PredicateValue)selectedValue;
            if (!predicateValue.predicated(entity)) continue;
            valid = true;
        }
        if (!valid || entity == Prediction.mc.player) {
            return false;
        }
        if (entity instanceof AbstractArrow arrow && this.isEmbeddedArrow(arrow)) {
            return false;
        }
        return Math.abs(entity.getDeltaMovement().x + entity.getDeltaMovement().z) > 0.01f || Math.abs(entity.getDeltaMovement().y) > 0.2f;
    }

    private Entity checkEntityCollision(Entity movingEntity, Vec3 predictedPos) {
        Vec3 currentPos = movingEntity.position();
        Vec3 direction = predictedPos.subtract(currentPos);
        if (direction.length() == 0.0) {
            return null;
        }
        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(movingEntity, currentPos, predictedPos, movingEntity.getBoundingBox().expandTowards(direction).inflate(0.5), entity -> Prediction.mc.player != entity && entity.isAlive() && !(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb) && entity != movingEntity, direction.length());
        return hitResult != null ? hitResult.getEntity() : null;
    }

    private void setVelocity(Projectile entity, double x, double y, double z, float power) {
        Vec3 vec3d = this.calculateVelocity(entity, x, y, z, power);
        entity.setDeltaMovement(vec3d);
        entity.hurtMarked = true;
        double d = vec3d.horizontalDistance();
        entity.setYRot((float)(Mth.atan2(vec3d.x, vec3d.z) * 57.2957763671875));
        entity.setXRot((float)(Mth.atan2(vec3d.y, d) * 57.2957763671875));
        entity.yRotO = entity.getYRot();
        entity.xRotO = entity.getXRot();
    }

    private void setVelocity(Projectile entity, Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        float f = -Mth.sin(yaw * ((float)Math.PI / 180)) * Mth.cos(pitch * ((float)Math.PI / 180));
        float g = -Mth.sin((pitch + roll) * ((float)Math.PI / 180));
        float h = Mth.cos(yaw * ((float)Math.PI / 180)) * Mth.cos(pitch * ((float)Math.PI / 180));
        this.setVelocity(entity, f, g, h, speed);
        Vec3 vec3d = shooter.getDeltaMovement();
        entity.setDeltaMovement(entity.getDeltaMovement().add(vec3d.x, shooter.onGround() ? 0.0 : vec3d.y, vec3d.z));
    }

    private Vec3 calculateVelocity(Projectile entity, double x, double y, double z, float power) {
        return new Vec3(x, y, z).normalize().scale(power);
    }

    private Vec3 predictMotion(Entity entity, Vec3 motion) {
        return motion.scale(0.99).add(0.0, -entity.getGravity(), 0.0);
    }

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, remainingSeconds);
        }
        return String.format("0:%02d", remainingSeconds);
    }

    private List<Predicted> collectRenderablePredictions(Vec3 cameraPos, float tickDelta) {
        ArrayList<Predicted> renderable = new ArrayList<>();
        for (Predicted pred : this.predicted) {
            if (pred.vectors.isEmpty()) {
                continue;
            }
            if (pred.entity instanceof AbstractArrow arrow && this.isEmbeddedArrow(arrow)) {
                continue;
            }
            Vec3 entityPos = Utils.getInterpolatedPos(pred.entity, tickDelta);
            Vec3 lastPos = pred.vectors.getLast();
            if (entityPos.distanceToSqr(cameraPos) < MIN_CAMERA_DISTANCE_SQR && lastPos.distanceToSqr(cameraPos) < MIN_CAMERA_DISTANCE_SQR) {
                continue;
            }
            if (this.isDuplicatePrediction(pred, renderable, tickDelta)) {
                continue;
            }
            renderable.add(pred);
        }
        return renderable;
    }

    private boolean isDuplicatePrediction(Predicted candidate, List<Predicted> accepted, float tickDelta) {
        Vec3 candidateStart = Utils.getInterpolatedPos(candidate.entity, tickDelta);
        Vec3 candidateEnd = candidate.vectors.getLast();
        for (Predicted existing : accepted) {
            if (existing.entity.getClass() != candidate.entity.getClass() || existing.vectors.isEmpty()) {
                continue;
            }
            Vec3 existingStart = Utils.getInterpolatedPos(existing.entity, tickDelta);
            Vec3 existingEnd = existing.vectors.getLast();
            if (candidateStart.distanceToSqr(existingStart) < DUPLICATE_START_DISTANCE_SQR &&
                candidateEnd.distanceToSqr(existingEnd) < DUPLICATE_END_DISTANCE_SQR) {
                return true;
            }
        }
        return false;
    }

    private boolean isEmbeddedArrow(AbstractArrow arrow) {
        if (arrow.onGround() && arrow.getDeltaMovement().lengthSqr() < MIN_SEGMENT_DISTANCE_SQR) {
            return true;
        }
        return this.readArrowBoolean(arrow, "inGround") || this.readArrowInt(arrow, "inGroundTime") > 0;
    }

    private boolean readArrowBoolean(AbstractArrow arrow, String fieldName) {
        try {
            if (inGroundField == null && "inGround".equals(fieldName)) {
                inGroundField = AbstractArrow.class.getDeclaredField(fieldName);
                inGroundField.setAccessible(true);
            }
            Field field = inGroundField;
            return field != null && field.getBoolean(arrow);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private int readArrowInt(AbstractArrow arrow, String fieldName) {
        try {
            if (inGroundTimeField == null && "inGroundTime".equals(fieldName)) {
                inGroundTimeField = AbstractArrow.class.getDeclaredField(fieldName);
                inGroundTimeField.setAccessible(true);
            }
            Field field = inGroundTimeField;
            return field != null ? field.getInt(arrow) : 0;
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    private void drawImpactOrbs(PoseStack ms, Camera camera, float tickDelta) {
        BufferBuilder crystalBuffer = moscow.rockstar.utility.render.CrystalRenderer.createBuffer();
        int orbCount = 0;
        for (Landed land : this.landed) {
            if (land.collidedEntity != null || land.hitResult == null) {
                continue;
            }
            Vec3 targetPos = land.hitResult.getLocation().add(0.0, 0.08, 0.0);
            if (this.isDuplicateLandedTarget(targetPos, orbCount)) {
                continue;
            }
            Vec3 smoothPos = this.getSmoothedLandedPosition(orbCount, targetPos);
            this.drawImpactOrbCrystal(ms, crystalBuffer, smoothPos, tickDelta, orbCount);
            ++orbCount;
        }
        this.trimSmoothedLandings(orbCount);

        GlProgram.usePositionColor();
        MeshData crystalMesh = crystalBuffer.build();
        if (crystalMesh != null) {
            MeshDrawHelper.drawBuilt(crystalMesh);
        }
    }

    private Vec3 getSmoothedLandedPosition(int index, Vec3 targetPos) {
        while (this.smoothedLandedPositions.size() <= index) {
            this.smoothedLandedPositions.add(targetPos);
        }
        Vec3 current = this.smoothedLandedPositions.get(index);
        Vec3 smoothed = current.lerp(targetPos, 0.014);
        this.smoothedLandedPositions.set(index, smoothed);
        return smoothed;
    }

    private void trimSmoothedLandings(int activeCount) {
        while (this.smoothedLandedPositions.size() > activeCount) {
            this.smoothedLandedPositions.remove(this.smoothedLandedPositions.size() - 1);
        }
    }

    private void drawImpactOrbCrystal(PoseStack ms, BufferBuilder buffer, Vec3 pos, float tickDelta, int index) {
        float time = (mc.level != null ? mc.level.getGameTime() : 0) + tickDelta + index * 17.0f;
        float spin = time * 2.3f;
        float tilt = 8.0f + 2.0f * Mth.sin(time * 0.05f);
        float size = 0.043f + 0.0025f * Mth.sin(time * 0.09f);
        ColorRGBA color = this.getRenderColor();
        ms.pushPose();
        ms.translate(pos.x, pos.y, pos.z);
        for (int i = 0; i < 3; ++i) {
            ms.pushPose();
            ms.mulPose(Axis.YP.rotationDegrees(spin + i * 57.0f));
            ms.mulPose(Axis.XP.rotationDegrees(tilt + i * 21.0f));
            ms.mulPose(Axis.ZP.rotationDegrees(i == 0 ? 0.0f : (i == 1 ? 90.0f : 45.0f)));
            moscow.rockstar.utility.render.CrystalRenderer.render(ms, buffer, 0.0f, 0.0f, 0.0f, size, color.withAlpha(i == 0 ? color.getAlpha() : color.getAlpha() * (170.0f / 255.0f)));
            ms.popPose();
        }
        ms.popPose();
    }

    private boolean isDuplicateLandedTarget(Vec3 targetPos, int activeCount) {
        for (int i = 0; i < activeCount && i < this.smoothedLandedPositions.size(); ++i) {
            if (this.smoothedLandedPositions.get(i).distanceToSqr(targetPos) < LANDED_DUPLICATE_DISTANCE_SQR) {
                return true;
            }
        }
        return false;
    }

    private List<Predicted> collectHudPredictions() {
        ArrayList<Predicted> result = new ArrayList<>();
        for (Predicted predict : this.predicted) {
            if (predict.vectors.isEmpty()) {
                continue;
            }
            if (this.isDuplicateHudPrediction(predict, result)) {
                continue;
            }
            result.add(predict);
        }
        return result;
    }

    private boolean isDuplicateHudPrediction(Predicted candidate, List<Predicted> accepted) {
        Vec3 candidateEnd = candidate.vectors.getLast();
        for (Predicted existing : accepted) {
            if (existing.entity.getClass() != candidate.entity.getClass() || existing.vectors.isEmpty()) {
                continue;
            }
            Vec3 existingEnd = existing.vectors.getLast();
            if (candidateEnd.distanceToSqr(existingEnd) < HUD_DUPLICATE_DISTANCE_SQR &&
                Math.abs(candidate.ticks - existing.ticks) <= 2) {
                return true;
            }
        }
        return false;
    }

    private void drawPredictionPath(PoseStack matrices, BufferBuilder builder, Predicted pred, Vec3 cameraPos, float tickDelta) {
        Vec3 startPos = Utils.getInterpolatedPos(pred.entity, tickDelta);
        Vec3 firstPos = pred.vectors.getFirst();
        if (this.shouldDrawSegment(startPos, firstPos, cameraPos)) {
            this.drawPredictionLine(matrices, builder, startPos, firstPos, this.getRenderColor());
        }
        Vec3 prevPos = firstPos;
        for (int i = 1; i < pred.vectors.size(); ++i) {
            Vec3 pos = pred.vectors.get(i);
            if (this.shouldDrawSegment(prevPos, pos, cameraPos)) {
                this.drawPredictionLine(matrices, builder, prevPos, pos, this.getRenderColor());
            }
            prevPos = pos;
        }
    }

    private boolean shouldDrawSegment(Vec3 startPos, Vec3 endPos, Vec3 cameraPos) {
        if (startPos.distanceToSqr(endPos) < MIN_SEGMENT_DISTANCE_SQR) {
            return false;
        }
        return !(startPos.distanceToSqr(cameraPos) < MIN_CAMERA_DISTANCE_SQR || endPos.distanceToSqr(cameraPos) < MIN_CAMERA_DISTANCE_SQR);
    }

    private void drawPredictionLine(PoseStack matrices, BufferBuilder builder, Vec3 startPos, Vec3 endPos, ColorRGBA color) {
        PoseStack.Pose matrixEntry = matrices.last();
        Matrix4f matrix4f = matrixEntry.pose();
        Vec3 normalized = endPos.subtract(startPos).normalize();
        if (normalized.lengthSqr() < 1.0E-6f) {
            normalized = new Vec3(0, 1, 0);
        }
        builder.addVertex(matrix4f, (float)startPos.x, (float)startPos.y, (float)startPos.z).setColor(color.getRGB()).setNormal(matrixEntry, (float)normalized.x, (float)normalized.y, (float)normalized.z).setLineWidth(1.25f);
        builder.addVertex(matrix4f, (float)endPos.x, (float)endPos.y, (float)endPos.z).setColor(color.getRGB()).setNormal(matrixEntry, (float)normalized.x, (float)normalized.y, (float)normalized.z).setLineWidth(1.25f);
    }

    private void drawBoxLines(PoseStack ms, BufferBuilder buffer, AABB box, ColorRGBA color) {
        float minX = (float)box.minX;
        float minY = (float)box.minY;
        float minZ = (float)box.minZ;
        float maxX = (float)box.maxX;
        float maxY = (float)box.maxY;
        float maxZ = (float)box.maxZ;
        
        Vec3[] vertices = {
            new Vec3(minX, minY, minZ), new Vec3(maxX, minY, minZ),
            new Vec3(maxX, minY, minZ), new Vec3(maxX, minY, maxZ),
            new Vec3(maxX, minY, maxZ), new Vec3(minX, minY, maxZ),
            new Vec3(minX, minY, maxZ), new Vec3(minX, minY, minZ),
            
            new Vec3(minX, maxY, minZ), new Vec3(maxX, maxY, minZ),
            new Vec3(maxX, maxY, minZ), new Vec3(maxX, maxY, maxZ),
            new Vec3(maxX, maxY, maxZ), new Vec3(minX, maxY, maxZ),
            new Vec3(minX, maxY, maxZ), new Vec3(minX, maxY, minZ),
            
            new Vec3(minX, minY, minZ), new Vec3(minX, maxY, minZ),
            new Vec3(maxX, minY, minZ), new Vec3(maxX, maxY, minZ),
            new Vec3(maxX, minY, maxZ), new Vec3(maxX, maxY, maxZ),
            new Vec3(minX, minY, maxZ), new Vec3(minX, maxY, maxZ)
        };
        
        PoseStack.Pose entry = ms.last();
        Matrix4f matrix = entry.pose();
        int rgb = color.getRGB();
        
        for (int i = 0; i < vertices.length; i += 2) {
            Vec3 start = vertices[i];
            Vec3 end = vertices[i + 1];
            Vec3 normalized = end.subtract(start).normalize();
            if (normalized.lengthSqr() < 1.0E-6f) {
                normalized = new Vec3(0, 1, 0);
            }
            
            buffer.addVertex(matrix, (float)start.x, (float)start.y, (float)start.z)
                  .setColor(rgb)
                  .setNormal(entry, (float)normalized.x, (float)normalized.y, (float)normalized.z)
                  .setLineWidth(1.0f);
                  
            buffer.addVertex(matrix, (float)end.x, (float)end.y, (float)end.z)
                  .setColor(rgb)
                  .setNormal(entry, (float)normalized.x, (float)normalized.y, (float)normalized.z)
                  .setLineWidth(1.0f);
        }
    }

    public List<Predicted> getPredicted() {
        return this.predicted;
    }

    public record Landed(Entity entity, Vec3 pos, int ticks, Entity collidedEntity, BlockHitResult hitResult) {
    }

    public record Predicted(Entity entity, List<Vec3> vectors, int ticks, Entity collidedEntity) {
    }
}
