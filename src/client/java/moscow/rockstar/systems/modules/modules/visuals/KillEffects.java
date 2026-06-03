/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  com.mojang.math.Axis
 *  net.minecraft.client.Camera
 *  net.minecraft.client.model.player.PlayerModel
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.entity.player.AvatarRenderer
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Avatar
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntitySpawnReason
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.animal.Animal
 *  net.minecraft.world.entity.monster.Creeper
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 *  org.lwjgl.opengl.GL11
 */
package moscow.rockstar.systems.modules.modules.visuals;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.shader.GlProgram;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.EntityDeathEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.ui.hud.impl.TargetHud;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.GLStateSnapshot;
import moscow.rockstar.utility.render.MeshDrawHelper;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.TextureBinder;
import net.minecraft.client.Camera;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.lwjgl.opengl.GL11;

@ModuleInfo(name="Kill Effects", category=ModuleCategory.VISUALS, desc="modules.descriptions.kill_effects")
public class KillEffects
extends BaseModule {
    private static final long EFFECT_DURATION_MS = 2000L;
    private static final long FROZEN_ENTITY_DURATION_MS = 4000L;
    private static final long TRACKED_ENTITY_TTL_MS = 15000L;
    private static final ColorRGBA TOTEM_GOLD_COLOR = new ColorRGBA(255.0f, 205.0f, 72.0f, 255.0f);
    private static final int PLAYER_MODEL_BUFFER_SIZE = 786432;
    private static final ByteBufferBuilder PLAYER_MODEL_BUFFER = new ByteBufferBuilder(786432);
    private final List<Effect> effects = new ArrayList<Effect>();
    private final List<FrozenEntityMarker> frozenMarkers = new ArrayList<FrozenEntityMarker>();
    private final BooleanSetting syncWithTheme = new BooleanSetting(this, "modules.settings.sync_with_theme").enable();
    private final ColorSetting color = new ColorSetting(this, "color", () -> this.syncWithTheme.isEnabled()).color(Colors.getAccentColor());
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.kill_effects.mode");
    private final ModeSetting.Value lightning = new ModeSetting.Value(this.mode, "modules.settings.kill_effects.mode.lightning").select();
    private final ModeSetting.Value cross = new ModeSetting.Value(this.mode, "modules.settings.kill_effects.mode.cross");
    private final ModeSetting.Value ascension = new ModeSetting.Value(this.mode, "modules.settings.kill_effects.mode.ascension");
    private final ModeSetting.Value cremation = new ModeSetting.Value(this.mode, "modules.settings.kill_effects.mode.cremation");
    private final ModeSetting.Value sonar = new ModeSetting.Value(this.mode, "modules.settings.kill_effects.mode.sonar");
    private final ModeSetting.Value position = new ModeSetting.Value(this.mode, "modules.settings.kill_effects.mode.position");
    private final BooleanSetting totemSound = new BooleanSetting(this, "modules.settings.kill_effects.totem_sound").enable();
    private final ModeSetting totemSoundMode = new ModeSetting(this, "modules.settings.kill_effects.totem_sound.mode", () -> !this.totemSound.isEnabled());
    private final ModeSetting.Value vebal = new ModeSetting.Value(this.totemSoundMode, "modules.settings.kill_effects.totem_sound.mode.vebal").select();
    private final ModeSetting.Value lopez = new ModeSetting.Value(this.totemSoundMode, "modules.settings.kill_effects.totem_sound.mode.lopez");
    private final ModeSetting.Value klyanus = new ModeSetting.Value(this.totemSoundMode, "modules.settings.kill_effects.totem_sound.mode.klyanus");
    private final Map<Integer, Long> processedEntities = new HashMap<Integer, Long>();
    private final Map<Integer, MarkerSnapshot> trackedTotemSnapshots = new HashMap<Integer, MarkerSnapshot>();
    private final EventListener<EntityDeathEvent> onEntityDeath = event -> {
        boolean recentlyTracked;
        long currentTime = System.currentTimeMillis();
        int entityId = event.getEntity().getId();
        if (this.processedEntities.containsKey(entityId) && currentTime - this.processedEntities.get(entityId) < 2000L) {
            return;
        }
        this.cleanupTrackedSnapshots(currentTime);
        MarkerSnapshot trackedSnapshot = this.trackedTotemSnapshots.get(entityId);
        boolean killedByPlayer = event.getKillerEntity() == KillEffects.mc.player;
        boolean bl = recentlyTracked = trackedSnapshot != null && currentTime - trackedSnapshot.createdAt() <= 15000L;
        if (!(recentlyTracked || killedByPlayer || this.isTarget((Entity)event.getEntity()))) {
            return;
        }
        this.processedEntities.put(entityId, currentTime);
        if (this.position.isSelected()) {
            this.addPlayerMarker((Entity)event.getEntity(), this.getColor(), 4000L);
            this.trackedTotemSnapshots.remove(entityId);
            return;
        }
        this.addSelectedEffect(event.getEntity().position(), (Entity)event.getEntity(), this.getColor(), 2000L, -1.0f);
        this.trackedTotemSnapshots.remove(entityId);
        if (this.processedEntities.size() > 100) {
            this.processedEntities.entrySet().removeIf(entry -> currentTime - (Long)entry.getValue() > 10000L);
        }
    };
    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        ClientboundEntityEventPacket packet;
        if (event.getPacket() instanceof ClientboundEntityEventPacket && (packet = (ClientboundEntityEventPacket)event.getPacket()).getEventId() == 35) {
            Entity entity = packet.getEntity((Level)KillEffects.mc.level);
            if (entity == null) {
                return;
            }
            if (this.isTarget(entity)) {
                float sonarRadius;
                MarkerSnapshot snapshot = this.captureSnapshot(entity);
                this.trackedTotemSnapshots.put(entity.getId(), snapshot);
                ColorRGBA gold = TOTEM_GOLD_COLOR;
                long duration = this.sonar.isSelected() ? 1200L : 2000L;
                float f = sonarRadius = this.sonar.isSelected() ? 30.0f : -1.0f;
                if (this.position.isSelected()) {
                    this.addPlayerMarker(entity, gold, 4000L);
                } else {
                    this.addSelectedEffect(snapshot.pos(), entity, gold, duration, sonarRadius);
                }
                if (this.totemSound.isEnabled()) {
                    this.playTotemSound(snapshot.pos());
                }
            }
        }
    };
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        long currentTime = System.currentTimeMillis();
        this.cleanupTrackedSnapshots(currentTime);
        this.processedEntities.entrySet().removeIf(entry -> currentTime - (Long)entry.getValue() > 10000L);
        this.frozenMarkers.removeIf(marker -> !marker.update(currentTime));
        if (KillEffects.mc.level == null) {
            this.clearFrozenMarkers();
        }
    };
    private final EventListener<Render3DEvent> on3DRender = event -> {
        if (RenderSystem.outputColorTextureOverride != null) {
            return;
        }
        PoseStack ms = event.pose();
        Camera camera = KillEffects.mc.gameRenderer.getMainCamera();
        ms.pushPose();
        GLStateSnapshot glState = GLStateSnapshot.capture();
        try {
            MeshData lineMesh;
            GL11.glEnable((int)3042);
            GL11.glBlendFunc((int)770, (int)1);
            GL11.glEnable((int)2929);
            GL11.glDisable((int)2884);
            GL11.glDepthMask((boolean)false);
            Identifier id = Rockstar.id("textures/bloom.png");
            TextureBinder.bind(id);
            GlProgram.usePositionTexColor();
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (Effect effect2 : this.effects) {
                effect2.render(builder, ms, camera);
            }
            MeshData builtBuffer = builder.build();
            if (builtBuffer != null) {
                MeshDrawHelper.drawBuilt(builtBuffer);
            }
            GlProgram.usePositionColor();
            BufferBuilder fillBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            BufferBuilder lineBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            for (Effect effect3 : this.effects) {
                effect3.renderSolid(fillBuilder, lineBuilder, ms, camera);
            }
            MeshData filledMesh = fillBuilder.build();
            if (filledMesh != null) {
                MeshDrawHelper.drawBuilt(filledMesh);
            }
            if ((lineMesh = lineBuilder.build()) != null) {
                MeshDrawHelper.drawBuilt(lineMesh);
            }
        }
        finally {
            TextureBinder.unbind();
            GlProgram.clearActive();
            glState.restore();
        }
        for (Effect effect4 : this.effects) {
            effect4.renderEntity(ms, event.getGameTimeDeltaPartialTick());
            if (!(effect4.animation.getRGB() >= 0.999f)) continue;
            effect4.showing = false;
        }
        ms.popPose();
        this.effects.removeIf(effect -> !effect.showing);
    };

    public ColorRGBA getColor() {
        return this.syncWithTheme.isEnabled() ? Colors.getAccentColor() : this.color.getColor();
    }

    @Override
    public void onDisable() {
        this.clearFrozenMarkers();
        this.effects.clear();
        this.processedEntities.clear();
        this.trackedTotemSnapshots.clear();
        super.onDisable();
    }

    private void addSelectedEffect(Vec3 pos, Entity sourceEntity, ColorRGBA color, long durationOverride, float sonarRadiusOverride) {
        if (this.lightning.isSelected()) {
            this.effects.add(durationOverride > 0L ? new Lightning(pos, color, durationOverride) : new Lightning(pos, color));
            this.playLightningSound(pos);
            return;
        }
        if (this.cross.isSelected()) {
            double groundY = this.findGroundY(pos);
            this.effects.add(new Cross(new Vec3(pos.x, groundY, pos.z), color));
            return;
        }
        if (this.ascension.isSelected()) {
            if (sourceEntity != null) {
                this.effects.add(new Ascension(pos, sourceEntity, color));
            }
            return;
        }
        if (this.cremation.isSelected()) {
            this.effects.add(new Cremation(pos, color));
            return;
        }
        if (this.sonar.isSelected()) {
            double groundY = this.findGroundY(pos);
            if (durationOverride > 0L && sonarRadiusOverride > 0.0f) {
                this.effects.add(new Sonar(new Vec3(pos.x, groundY, pos.z), color, durationOverride, sonarRadiusOverride));
            } else {
                this.effects.add(new Sonar(new Vec3(pos.x, groundY, pos.z), color));
            }
            return;
        }
        if (this.position.isSelected()) {
            this.addFrozenMarker(sourceEntity, durationOverride > 0L ? durationOverride : 4000L, false);
        }
    }

    private boolean isTarget(Entity entity) {
        if (entity == null) {
            return false;
        }
        LivingEntity hudTarget = this.getActiveHudTarget();
        if (hudTarget != null && hudTarget.getId() == entity.getId()) {
            return true;
        }
        Entity current = Rockstar.getInstance().getTargetManager().getCurrentTarget();
        if (current != null && current.getId() == entity.getId()) {
            return true;
        }
        if (Rockstar.getInstance().getTargetManager().getTarget().contains(entity.getName().getString())) {
            return true;
        }
        if (KillEffects.mc.crosshairPickEntity != null && KillEffects.mc.crosshairPickEntity.getId() == entity.getId()) {
            return true;
        }
        if (entity.getName().getString().equals("FakePlayer")) {
            return true;
        }
        return entity instanceof Player && entity != KillEffects.mc.player && (double)entity.distanceTo((Entity)KillEffects.mc.player) < 6.0;
    }

    private LivingEntity getActiveHudTarget() {
        TargetHud targetHud = (TargetHud)Rockstar.getInstance().getHud().getElementByName("hud.targethud");
        if (targetHud == null) {
            return null;
        }
        LivingEntity target = targetHud.getTargetEntity();
        return target != null && target.isAlive() ? target : null;
    }

    private void playTotemSound(Vec3 pos) {
        try {
            String soundName = "vebal";
            if (this.lopez.isSelected()) {
                soundName = "lopez";
            } else if (this.klyanus.isSelected()) {
                soundName = "klyanus";
            }
            Identifier soundId = Rockstar.id(soundName);
            SoundEvent soundEvent = SoundEvent.createVariableRangeEvent((Identifier)soundId);
            KillEffects.mc.level.playLocalSound(pos.x, pos.y, pos.z, soundEvent, SoundSource.PLAYERS, 1.0f, 1.0f, false);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void playLightningSound(Vec3 pos) {
        try {
            if (KillEffects.mc.level == null || KillEffects.mc.player == null) {
                return;
            }
            KillEffects.mc.level.playSound((Entity)KillEffects.mc.player, pos.x, pos.y, pos.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.0f, 1.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private double findGroundY(Vec3 pos) {
        BlockPos blockPos = BlockPos.containing((double)pos.x, (double)pos.y, (double)pos.z);
        for (int i = 0; i < 10; ++i) {
            if (!KillEffects.mc.level.getBlockState(blockPos).isAir()) {
                return (double)blockPos.getY() + 1.05;
            }
            blockPos = blockPos.below();
        }
        return (double)KillEffects.mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)pos.x, (int)pos.z) + 0.05;
    }

    private MarkerSnapshot captureSnapshot(Entity entity) {
        float f;
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            f = living.yBodyRot;
        } else {
            f = entity.getYRot();
        }
        float bodyYaw = f;
        return new MarkerSnapshot(entity.position().add(0.0, 0.02, 0.0), Math.max(0.3f, entity.getBbWidth()), Math.max(0.4f, entity.getBbHeight()), bodyYaw, ShapeType.of(entity), System.currentTimeMillis());
    }

    private void cleanupTrackedSnapshots(long currentTime) {
        this.trackedTotemSnapshots.entrySet().removeIf(entry -> currentTime - ((MarkerSnapshot)entry.getValue()).createdAt() > 15000L);
    }

    private void addFrozenMarker(Entity sourceEntity, long duration, boolean deathPose) {
        FrozenEntityMarker marker = FrozenEntityMarker.create(sourceEntity, duration, deathPose);
        if (marker != null) {
            this.frozenMarkers.add(marker);
        }
    }

    private void addPlayerMarker(Entity sourceEntity, ColorRGBA color, long duration) {
        Player player;
        PlayerMarker marker;
        if (sourceEntity instanceof Player && (marker = PlayerMarker.create(player = (Player)sourceEntity, color, duration)) != null) {
            this.effects.add(marker);
        }
    }

    private void clearFrozenMarkers() {
        for (FrozenEntityMarker marker : this.frozenMarkers) {
            marker.remove();
        }
        this.frozenMarkers.clear();
    }

    static class Lightning
    extends Effect {
        final List<Vec3> poses = new ArrayList<Vec3>();

        public Lightning(Vec3 pos, ColorRGBA color) {
            this(pos, color, 2000L);
        }

        public Lightning(Vec3 pos, ColorRGBA color, long duration) {
            super(pos, color, duration);
            Vec3 lastPos = pos;
            for (int i = 0; i < 200; ++i) {
                lastPos = lastPos.add((double)MathUtility.random(-0.4f, 0.4f), 0.25, (double)MathUtility.random(-0.4f, 0.4f));
                this.poses.add(lastPos);
            }
        }

        @Override
        void render(BufferBuilder builder, PoseStack ms, Camera camera) {
            this.animation.setEasing(Easing.BOUNCE_IN);
            this.animation.update(1.0f);
            for (Vec3 pos : this.poses) {
                float size = (float)(2.0 + 5.0 * (pos.y - this.pos.y) / 50.0);
                ms.pushPose();
                RenderUtility.prepareMatrices(ms, pos);
                ms.mulPose((Quaternionfc)camera.rotation());
                DrawUtility.drawImage(ms, builder, (double)(-size / 2.0f), (double)(-size / 2.0f), 0.0, (double)size, (double)size, this.color.withAlpha(255.0f * this.animation.getRGB() * 0.4f));
                ms.popPose();
            }
        }
    }

    static class Cross
    extends Effect {
        public Cross(Vec3 pos, ColorRGBA color) {
            super(pos, color);
        }

        @Override
        void render(BufferBuilder builder, PoseStack ms, Camera camera) {
            this.animation.setEasing(Easing.CUBIC_OUT);
            this.animation.update(1.0f);
            float alpha = 1.0f - this.animation.getRGB();
            float scale = 0.8f + this.animation.getRGB() * 0.2f;
            ms.pushPose();
            RenderUtility.prepareMatrices(ms, this.pos.add(0.0, 2.5, 0.0));
            ms.mulPose((Quaternionfc)camera.rotation());
            ms.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f));
            float verticalWidth = 0.6f * scale;
            float verticalHeight = 5.0f * scale;
            DrawUtility.drawImage(ms, builder, (double)(-verticalWidth / 2.0f), (double)(-verticalHeight / 2.0f), 0.0, (double)verticalWidth, (double)verticalHeight, this.color.withAlpha(255.0f * alpha));
            float horizontalWidth = 3.2f * scale;
            float horizontalHeight = 0.6f * scale;
            DrawUtility.drawImage(ms, builder, (double)(-horizontalWidth / 2.0f), (double)(-horizontalHeight / 2.0f - 1.0f), 0.0, (double)horizontalWidth, (double)horizontalHeight, this.color.withAlpha(255.0f * alpha));
            ms.popPose();
        }
    }

    static class Ascension
    extends Effect {
        final Entity entity;

        public Ascension(Vec3 pos, Entity entity, ColorRGBA color) {
            super(pos, color);
            this.entity = entity;
        }

        @Override
        void render(BufferBuilder builder, PoseStack ms, Camera camera) {
            this.animation.update(1.0f);
            float alpha = 1.0f - this.animation.getRGB();
            float yOffset = this.animation.getRGB() * 6.0f;
            float flap = (float)Math.sin((double)System.currentTimeMillis() / 150.0) * 15.0f;
            float wingWidth = 2.5f;
            float wingHeight = 0.8f;
            ms.pushPose();
            RenderUtility.prepareMatrices(ms, this.pos.add(0.0, (double)(yOffset + this.entity.getBbHeight() * 0.7f), 0.0));
            ms.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-this.entity.getYRot()));
            ms.pushPose();
            ms.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(30.0f + flap));
            DrawUtility.drawImage(ms, builder, 0.1, (double)(-wingHeight) / 2.0, 0.15, (double)wingWidth, (double)wingHeight, ColorRGBA.WHITE.withAlpha(255.0f * alpha * 0.7f));
            ms.popPose();
            ms.pushPose();
            ms.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(-30.0f - flap));
            DrawUtility.drawImage(ms, builder, (double)(-wingWidth) - 0.1, (double)(-wingHeight) / 2.0, 0.15, (double)wingWidth, (double)wingHeight, ColorRGBA.WHITE.withAlpha(255.0f * alpha * 0.7f));
            ms.popPose();
            ms.pushPose();
            ms.translate(0.0, (double)(this.entity.getBbHeight() * 0.35f), 0.0);
            ms.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
            float haloSize = this.entity.getBbWidth() * 1.5f;
            DrawUtility.drawImage(ms, builder, (double)(-haloSize) / 2.0, (double)(-haloSize) / 2.0, 0.0, (double)haloSize, (double)haloSize, ColorRGBA.WHITE.withAlpha(255.0f * alpha * 0.8f));
            ms.popPose();
            ms.popPose();
        }

        @Override
        void renderEntity(PoseStack ms, float tickDelta) {
            LivingEntity living;
            float alpha = 1.0f - this.animation.getRGB();
            float yOffset = this.animation.getRGB() * 6.0f;
            ms.pushPose();
            RenderUtility.prepareMatrices(ms, this.pos.add(0.0, (double)yOffset, 0.0));
            float oldPitch = this.entity.getXRot();
            float oldPrevPitch = this.entity.xRotO;
            int oldHurtTime = 0;
            int oldDeathTime = 0;
            this.entity.setXRot(45.0f);
            this.entity.xRotO = 45.0f;
            if (this.entity instanceof LivingEntity) {
                living = (LivingEntity)this.entity;
                oldHurtTime = living.hurtTime;
                oldDeathTime = living.deathTime;
                living.hurtTime = 0;
                living.deathTime = 0;
            }
            this.entity.setXRot(oldPitch);
            this.entity.xRotO = oldPrevPitch;
            if (this.entity instanceof LivingEntity) {
                living = (LivingEntity)this.entity;
                living.hurtTime = oldHurtTime;
                living.deathTime = oldDeathTime;
            }
            ms.popPose();
        }
    }

    static class Cremation
    extends Effect {
        final List<Ash> ashes = new ArrayList<Ash>();

        public Cremation(Vec3 pos, ColorRGBA color) {
            super(pos, color);
            for (int i = 0; i < 60; ++i) {
                float angle = (float)(Math.random() * Math.PI * 2.0);
                float speed = MathUtility.random(0.02f, 0.08f);
                Vec3 velocity = new Vec3(Math.cos(angle) * (double)speed, (double)MathUtility.random(0.05f, 0.15f), Math.sin(angle) * (double)speed);
                this.ashes.add(new Ash(velocity, MathUtility.random(0.5, 1.5), MathUtility.random(0.0, 360.0)));
            }
        }

        @Override
        void render(BufferBuilder builder, PoseStack ms, Camera camera) {
            this.animation.setEasing(Easing.CUBIC_OUT);
            this.animation.setDuration(2500L);
            this.animation.update(1.0f);
            ColorRGBA ashColor = new ColorRGBA(240.0f, 240.0f, 240.0f, 255.0f);
            for (Ash ash : this.ashes) {
                float yVelocity;
                float upTime = 0.3f;
                if (this.animation.getRGB() < upTime) {
                    yVelocity = (float)ash.velocity.y;
                } else {
                    float fallProgress = (this.animation.getRGB() - upTime) / (1.0f - upTime);
                    yVelocity = (float)(ash.velocity.y - (double)(fallProgress * 0.3f));
                }
                Vec3 ashPos = this.pos.add(ash.velocity.x * (double)this.animation.getRGB() * 8.0, (double)(yVelocity * this.animation.getRGB()) * 8.0, ash.velocity.z * (double)this.animation.getRGB() * 8.0);
                ms.pushPose();
                RenderUtility.prepareMatrices(ms, ashPos);
                ms.mulPose((Quaternionfc)camera.rotation());
                ms.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(ash.rotation + this.animation.getRGB() * 360.0f));
                float alpha = 1.0f - this.animation.getRGB();
                float flicker = (float)((double)0.7f + (double)0.3f * Math.sin(this.animation.getRGB() * 20.0f + ash.rotation));
                DrawUtility.drawImage(ms, builder, (double)(-ash.size / 2.0f), (double)(-ash.size / 2.0f), 0.0, (double)ash.size, (double)ash.size, ashColor.withAlpha(255.0f * alpha * flicker));
                ms.popPose();
            }
        }

        static class Ash {
            Vec3 velocity;
            float size;
            float rotation;

            Ash(Vec3 velocity, float size, float rotation) {
                this.velocity = velocity;
                this.size = size;
                this.rotation = rotation;
            }
        }
    }

    static class Sonar
    extends Effect {
        private final float maxRadius;

        public Sonar(Vec3 pos, ColorRGBA color) {
            this(pos, color, 1500L, 12.0f);
        }

        public Sonar(Vec3 pos, ColorRGBA color, long duration, float radius) {
            super(pos, color, duration);
            this.maxRadius = radius;
        }

        @Override
        void render(BufferBuilder builder, PoseStack ms, Camera camera) {
            this.animation.setEasing(Easing.CUBIC_OUT);
            this.animation.update(1.0f);
            float alpha = 1.0f - this.animation.getRGB();
            float radius = this.animation.getRGB() * this.maxRadius;
            ms.pushPose();
            RenderUtility.prepareMatrices(ms, this.pos.add(0.0, 0.1, 0.0));
            ms.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
            DrawUtility.drawImage(ms, builder, (double)(-radius), (double)(-radius), 0.0, (double)radius * 2.0, (double)radius * 2.0, this.color.withAlpha(255.0f * alpha * 0.6f));
            ms.popPose();
        }
    }

    private record MarkerSnapshot(Vec3 pos, float width, float height, float bodyYaw, ShapeType shapeType, long createdAt) {
    }

    private static enum ShapeType {
        HUMANOID,
        CHICKEN,
        QUADRUPED,
        GENERIC;


        static ShapeType of(Entity entity) {
            String className = entity.getClass().getSimpleName().toLowerCase();
            if (entity instanceof Player || className.contains("zombie") || className.contains("skeleton") || className.contains("enderman") || className.contains("illager") || className.contains("piglin")) {
                return HUMANOID;
            }
            if (className.contains("chicken")) {
                return CHICKEN;
            }
            if (entity instanceof Animal || entity instanceof Creeper) {
                return QUADRUPED;
            }
            return GENERIC;
        }
    }

    static class FrozenEntityMarker {
        private final Entity ghost;
        private final Vec3 pos;
        private final float yRot;
        private final float xRot;
        private final float bodyRot;
        private final float headRot;
        private final Pose pose;
        private final boolean deathPose;
        private final long expiresAt;

        private FrozenEntityMarker(Entity ghost, Vec3 pos, float yRot, float xRot, float bodyRot, float headRot, Pose pose, boolean deathPose, long expiresAt) {
            this.ghost = ghost;
            this.pos = pos;
            this.yRot = yRot;
            this.xRot = xRot;
            this.bodyRot = bodyRot;
            this.headRot = headRot;
            this.pose = pose;
            this.deathPose = deathPose;
            this.expiresAt = expiresAt;
        }

        static FrozenEntityMarker create(Entity sourceEntity, long duration, boolean deathPose) {
            float f;
            float bodyRot;
            Entity ghost;
            if (sourceEntity == null || KillEffects.mc.level == null) {
                return null;
            }
            try {
                if (sourceEntity instanceof Player) {
                    Player player = (Player)sourceEntity;
                    ghost = new RemotePlayer(KillEffects.mc.level, player.getGameProfile());
                } else {
                    ghost = sourceEntity.getType().create((Level)KillEffects.mc.level, EntitySpawnReason.LOAD);
                }
            }
            catch (Exception e) {
                return null;
            }
            if (ghost == null) {
                return null;
            }
            ghost.restoreFrom(sourceEntity);
            ghost.setUUID(UUID.randomUUID());
            ghost.noPhysics = true;
            ghost.blocksBuilding = false;
            ghost.setDeltaMovement(Vec3.ZERO);
            ghost.clearFire();
            ghost.setCustomNameVisible(false);
            Vec3 pos = sourceEntity.position().add(0.0, 0.02, 0.0);
            if (sourceEntity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)sourceEntity;
                bodyRot = living.yBodyRot;
            } else {
                bodyRot = sourceEntity.getYRot();
            }
            if (sourceEntity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)sourceEntity;
                f = living.yHeadRot;
            } else {
                f = sourceEntity.getYRot();
            }
            float headRot = f;
            Pose pose = sourceEntity.getPose();
            FrozenEntityMarker marker = new FrozenEntityMarker(ghost, pos, sourceEntity.getYRot(), sourceEntity.getXRot(), bodyRot, headRot, pose, deathPose, System.currentTimeMillis() + Math.max(250L, duration));
            marker.applyFrozenState();
            KillEffects.mc.level.addEntity(ghost);
            return marker;
        }

        boolean update(long currentTime) {
            if (KillEffects.mc.level == null || this.ghost == null || this.ghost.isRemoved()) {
                return false;
            }
            if (currentTime >= this.expiresAt) {
                this.remove();
                return false;
            }
            this.applyFrozenState();
            return true;
        }

        void remove() {
            if (this.ghost == null || this.ghost.level() == null) {
                return;
            }
            Level level = this.ghost.level();
            if (level instanceof ClientLevel) {
                ClientLevel clientLevel = (ClientLevel)level;
                clientLevel.removeEntity(this.ghost.getId(), Entity.RemovalReason.DISCARDED);
            } else {
                this.ghost.discard();
            }
        }

        private void applyFrozenState() {
            this.ghost.setPos(this.pos);
            this.ghost.xo = this.pos.x;
            this.ghost.yo = this.pos.y;
            this.ghost.zo = this.pos.z;
            this.ghost.xOld = this.pos.x;
            this.ghost.yOld = this.pos.y;
            this.ghost.zOld = this.pos.z;
            this.ghost.setDeltaMovement(Vec3.ZERO);
            this.ghost.setYRot(this.yRot);
            this.ghost.yRotO = this.yRot;
            this.ghost.setXRot(this.xRot);
            this.ghost.xRotO = this.xRot;
            this.ghost.setPose(this.pose);
            this.ghost.noPhysics = true;
            this.ghost.blocksBuilding = false;
            Entity entity = this.ghost;
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                living.setHealth(Math.max(1.0f, living.getHealth()));
                living.hurtTime = 0;
                living.hurtDuration = 0;
                living.deathTime = this.deathPose ? 19 : 0;
                living.swinging = false;
                living.swingTime = 0;
                living.yBodyRot = this.bodyRot;
                living.yBodyRotO = this.bodyRot;
                living.yHeadRot = this.headRot;
                living.yHeadRotO = this.headRot;
            }
            if ((entity = this.ghost) instanceof Mob) {
                Mob mob = (Mob)entity;
                mob.setNoAi(true);
                mob.setAggressive(false);
                mob.setCanPickUpLoot(false);
            }
        }
    }

    static class PlayerMarker
    extends Effect {
        private final RemotePlayer snapshot;
        private final float bodyYaw;
        private final float pitch;

        private PlayerMarker(Vec3 pos, ColorRGBA color, long duration, RemotePlayer snapshot, float bodyYaw, float pitch) {
            super(pos, color, duration);
            this.snapshot = snapshot;
            this.bodyYaw = bodyYaw;
            this.pitch = pitch;
        }

        static PlayerMarker create(Player source, ColorRGBA color, long duration) {
            float f;
            if (KillEffects.mc.level == null) {
                return null;
            }
            RemotePlayer snapshot = new RemotePlayer(KillEffects.mc.level, source.getGameProfile());
            snapshot.restoreFrom((Entity)source);
            snapshot.copyPosition((Entity)source);
            snapshot.setPose(Pose.STANDING);
            snapshot.setYRot(source.getYRot());
            snapshot.yRotO = source.getYRot();
            snapshot.setXRot(source.getXRot());
            snapshot.xRotO = source.getXRot();
            snapshot.setDeltaMovement(Vec3.ZERO);
            snapshot.hurtTime = 0;
            snapshot.deathTime = 0;
            snapshot.setHealth(Math.max(1.0f, source.getHealth()));
            Vec3 vec3 = source.position().add(0.0, 0.02, 0.0);
            long l = Math.max(250L, duration);
            if (source instanceof LivingEntity) {
                Player living = source;
                f = living.yBodyRot;
            } else {
                f = source.getYRot();
            }
            return new PlayerMarker(vec3, color, l, snapshot, f, source.getXRot());
        }

        @Override
        void render(BufferBuilder builder, PoseStack ms, Camera camera) {
        }

        @Override
        void renderEntity(PoseStack ms, float tickDelta) {
            this.animation.setEasing(Easing.CUBIC_OUT);
            this.animation.update(1.0f);
            float alpha = 1.0f - this.animation.getRGB();
            if (alpha <= 0.01f || KillEffects.mc.level == null) {
                return;
            }
            this.snapshot.setPos(this.pos);
            this.snapshot.xo = this.pos.x;
            this.snapshot.yo = this.pos.y;
            this.snapshot.zo = this.pos.z;
            this.snapshot.setPose(Pose.STANDING);
            this.snapshot.setYRot(this.bodyYaw);
            this.snapshot.yRotO = this.bodyYaw;
            this.snapshot.setXRot(this.pitch);
            this.snapshot.xRotO = this.pitch;
            this.snapshot.hurtTime = 0;
            this.snapshot.deathTime = 0;
            AvatarRenderer renderer = mc.getEntityRenderDispatcher().getPlayerRenderer((AbstractClientPlayer)this.snapshot);
            AvatarRenderState state = renderer.createRenderState();
            mc.getEntityRenderDispatcher().prepare(KillEffects.mc.gameRenderer.getMainCamera(), (Entity)this.snapshot);
            renderer.extractRenderState((Avatar)this.snapshot, state, tickDelta);
            state.deathTime = 0.0f;
            state.hasRedOverlay = false;
            state.pose = Pose.STANDING;
            state.isCrouching = false;
            state.walkAnimationPos = 0.0f;
            state.walkAnimationSpeed = 0.0f;
            state.bodyRot = this.bodyYaw;
            state.yRot = 0.0f;
            state.xRot = this.pitch;
            ms.pushPose();
            RenderUtility.prepareMatrices(ms, this.pos);
            ms.scale(state.scale, state.scale, state.scale);
            ms.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - this.bodyYaw));
            ms.scale(-1.0f, -1.0f, 1.0f);
            ms.translate(0.0f, -1.501f, 0.0f);
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate((ByteBufferBuilder)PLAYER_MODEL_BUFFER);
            int light = 0xF000F0;
            int skinColor = ColorRGBA.WHITE.withAlpha(255.0f * alpha).getRGB();
            int accentColor = this.color.withAlpha(110.0f * alpha).getRGB();
            ((PlayerModel)renderer.getModel()).resetPose();
            ((PlayerModel)renderer.getModel()).setupAnim(state);
            Identifier texture = renderer.getTextureLocation(state);
            ((PlayerModel)renderer.getModel()).renderToBuffer(ms, bufferSource.getBuffer(((PlayerModel)renderer.getModel()).renderType(texture)), light, OverlayTexture.NO_OVERLAY, skinColor);
            ((PlayerModel)renderer.getModel()).renderToBuffer(ms, bufferSource.getBuffer(((PlayerModel)renderer.getModel()).renderType(texture)), light, OverlayTexture.NO_OVERLAY, accentColor);
            bufferSource.endBatch();
            ms.popPose();
        }
    }

    static abstract class Effect {
        final Vec3 pos;
        final ColorRGBA color;
        boolean showing = true;
        final Animation animation;

        public Effect(Vec3 pos, ColorRGBA color) {
            this(pos, color, 2000L);
        }

        public Effect(Vec3 pos, ColorRGBA color, long duration) {
            this.pos = pos;
            this.color = color;
            this.animation = new Animation(duration, 0.0f, Easing.LINEAR);
        }

        abstract void render(BufferBuilder var1, PoseStack var2, Camera var3);

        void renderSolid(BufferBuilder fillBuilder, BufferBuilder lineBuilder, PoseStack ms, Camera camera) {
        }

        void renderEntity(PoseStack ms, float tickDelta) {
        }
    }

    static class PositionMarker
    extends Effect {
        private final float width;
        private final float height;
        private final float bodyYaw;
        private final ShapeType shapeType;

        public PositionMarker(Vec3 pos, ColorRGBA color, long duration, float width, float height, float bodyYaw, ShapeType shapeType) {
            super(pos, color, duration);
            this.width = Math.max(0.3f, width + 0.08f);
            this.height = Math.max(0.4f, height + 0.08f);
            this.bodyYaw = bodyYaw;
            this.shapeType = shapeType;
        }

        @Override
        void render(BufferBuilder builder, PoseStack ms, Camera camera) {
        }

        @Override
        void renderSolid(BufferBuilder fillBuilder, BufferBuilder lineBuilder, PoseStack ms, Camera camera) {
            this.animation.setEasing(Easing.CUBIC_OUT);
            this.animation.update(1.0f);
            float alpha = 1.0f - this.animation.getRGB();
            ms.pushPose();
            ms.translate(-camera.position().x, -camera.position().y, -camera.position().z);
            ms.translate(this.pos.x, this.pos.y, this.pos.z);
            ms.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-this.bodyYaw));
            switch (this.shapeType.ordinal()) {
                case 0: {
                    this.renderHumanoidShell(ms, fillBuilder, lineBuilder, alpha);
                    break;
                }
                case 1: {
                    this.renderChickenShell(ms, fillBuilder, lineBuilder, alpha);
                    break;
                }
                case 2: {
                    this.renderQuadrupedShell(ms, fillBuilder, lineBuilder, alpha);
                    break;
                }
                default: {
                    this.renderFallbackShell(ms, fillBuilder, lineBuilder, alpha);
                }
            }
            ms.popPose();
        }

        private void renderHumanoidShell(PoseStack ms, BufferBuilder fillBuilder, BufferBuilder lineBuilder, float alpha) {
            float grow = 0.03f + this.animation.getRGB() * 0.09f;
            float torsoWidth = this.width * 0.68f;
            float torsoDepth = this.width * 0.34f;
            float torsoHeight = this.height * 0.36f;
            float legHeight = this.height * 0.48f;
            float legWidth = this.width * 0.26f;
            float armWidth = this.width * 0.22f;
            float limbDepth = this.width * 0.24f;
            float armHeight = this.height * 0.4f;
            float headSize = this.width * 0.52f;
            float shoulderY = legHeight + torsoHeight - armWidth * 0.15f;
            float headY = legHeight + torsoHeight + headSize * 0.08f;
            float legGap = legWidth * 0.38f;
            float armOffset = torsoWidth / 2.0f + armWidth / 2.0f + this.width * 0.06f;
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-torsoWidth / 2.0f), (double)legHeight, (double)(-torsoDepth / 2.0f), (double)(torsoWidth / 2.0f), (double)(legHeight + torsoHeight), (double)(torsoDepth / 2.0f)).inflate((double)grow), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-headSize / 2.0f), (double)headY, (double)(-headSize / 2.0f), (double)(headSize / 2.0f), (double)(headY + headSize), (double)(headSize / 2.0f)).inflate((double)grow), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-legGap - legWidth), 0.0, (double)(-limbDepth / 2.0f), (double)(-legGap), (double)legHeight, (double)(limbDepth / 2.0f)).inflate((double)(grow * 0.8f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)legGap, 0.0, (double)(-limbDepth / 2.0f), (double)(legGap + legWidth), (double)legHeight, (double)(limbDepth / 2.0f)).inflate((double)(grow * 0.8f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-armOffset - armWidth / 2.0f), (double)(shoulderY - armHeight), (double)(-limbDepth / 2.0f), (double)(-armOffset + armWidth / 2.0f), (double)shoulderY, (double)(limbDepth / 2.0f)).inflate((double)(grow * 0.8f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(armOffset - armWidth / 2.0f), (double)(shoulderY - armHeight), (double)(-limbDepth / 2.0f), (double)(armOffset + armWidth / 2.0f), (double)shoulderY, (double)(limbDepth / 2.0f)).inflate((double)(grow * 0.8f)), alpha);
        }

        private void renderFallbackShell(PoseStack ms, BufferBuilder fillBuilder, BufferBuilder lineBuilder, float alpha) {
            double grow = 0.06 + (double)this.animation.getRGB() * 0.16;
            AABB shellBox = new AABB((double)(-this.width / 2.0f), 0.0, (double)(-this.width / 2.0f), (double)(this.width / 2.0f), (double)this.height, (double)(this.width / 2.0f)).inflate(grow, 0.02, grow);
            this.renderPart(ms, fillBuilder, lineBuilder, shellBox, alpha);
        }

        private void renderQuadrupedShell(PoseStack ms, BufferBuilder fillBuilder, BufferBuilder lineBuilder, float alpha) {
            float grow = 0.025f + this.animation.getRGB() * 0.08f;
            float legHeight = this.height * 0.45f;
            float bodyHeight = this.height * 0.32f;
            float bodyLength = this.width * 1.25f;
            float bodyWidth = this.width * 0.55f;
            float headSize = this.width * 0.38f;
            float neckLength = this.width * 0.18f;
            float legWidth = this.width * 0.18f;
            float legDepth = this.width * 0.18f;
            float legX = bodyWidth * 0.55f;
            float frontZ = bodyLength * 0.32f;
            float backZ = -bodyLength * 0.32f;
            float bodyY = legHeight + bodyHeight * 0.25f;
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-bodyWidth), (double)bodyY, (double)(-bodyLength / 2.0f), (double)bodyWidth, (double)(bodyY + bodyHeight), (double)(bodyLength / 2.0f)).inflate((double)grow), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-headSize / 2.0f), (double)(bodyY + bodyHeight * 0.3f), (double)(bodyLength / 2.0f + neckLength), (double)(headSize / 2.0f), (double)(bodyY + bodyHeight * 0.3f + headSize), (double)(bodyLength / 2.0f + neckLength + headSize)).inflate((double)(grow * 0.7f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-headSize * 0.18f), (double)(bodyY + bodyHeight * 0.22f), (double)(bodyLength / 2.0f), (double)(headSize * 0.18f), (double)(bodyY + bodyHeight * 0.55f), (double)(bodyLength / 2.0f + neckLength)).inflate((double)(grow * 0.55f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-legX - legWidth / 2.0f), 0.0, (double)(frontZ - legDepth / 2.0f), (double)(-legX + legWidth / 2.0f), (double)legHeight, (double)(frontZ + legDepth / 2.0f)).inflate((double)(grow * 0.55f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(legX - legWidth / 2.0f), 0.0, (double)(frontZ - legDepth / 2.0f), (double)(legX + legWidth / 2.0f), (double)legHeight, (double)(frontZ + legDepth / 2.0f)).inflate((double)(grow * 0.55f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-legX - legWidth / 2.0f), 0.0, (double)(backZ - legDepth / 2.0f), (double)(-legX + legWidth / 2.0f), (double)legHeight, (double)(backZ + legDepth / 2.0f)).inflate((double)(grow * 0.55f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(legX - legWidth / 2.0f), 0.0, (double)(backZ - legDepth / 2.0f), (double)(legX + legWidth / 2.0f), (double)legHeight, (double)(backZ + legDepth / 2.0f)).inflate((double)(grow * 0.55f)), alpha);
        }

        private void renderChickenShell(PoseStack ms, BufferBuilder fillBuilder, BufferBuilder lineBuilder, float alpha) {
            float grow = 0.02f + this.animation.getRGB() * 0.06f;
            float legHeight = this.height * 0.36f;
            float bodyHeight = this.height * 0.34f;
            float bodyWidth = this.width * 0.46f;
            float bodyLength = this.width * 0.62f;
            float wingWidth = this.width * 0.18f;
            float wingHeight = bodyHeight * 0.9f;
            float neckHeight = this.height * 0.18f;
            float headSize = this.width * 0.24f;
            float legGap = this.width * 0.12f;
            float bodyBottom = legHeight + this.height * 0.1f;
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-bodyWidth), (double)bodyBottom, (double)(-bodyLength / 2.0f), (double)bodyWidth, (double)(bodyBottom + bodyHeight), (double)(bodyLength / 2.0f)).inflate((double)grow), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-headSize / 2.0f), (double)(bodyBottom + bodyHeight + neckHeight * 0.3f), (double)(bodyLength * 0.18f), (double)(headSize / 2.0f), (double)(bodyBottom + bodyHeight + neckHeight * 0.3f + headSize), (double)(bodyLength * 0.18f + headSize)).inflate((double)(grow * 0.7f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-headSize * 0.18f), (double)(bodyBottom + bodyHeight * 0.8f), (double)(bodyLength * 0.08f), (double)(headSize * 0.18f), (double)(bodyBottom + bodyHeight + neckHeight), (double)(bodyLength * 0.18f)).inflate((double)(grow * 0.45f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-bodyWidth - wingWidth), (double)(bodyBottom + bodyHeight * 0.1f), (double)(-bodyLength * 0.25f), (double)(-bodyWidth), (double)(bodyBottom + bodyHeight * 0.1f + wingHeight), (double)(bodyLength * 0.25f)).inflate((double)(grow * 0.45f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)bodyWidth, (double)(bodyBottom + bodyHeight * 0.1f), (double)(-bodyLength * 0.25f), (double)(bodyWidth + wingWidth), (double)(bodyBottom + bodyHeight * 0.1f + wingHeight), (double)(bodyLength * 0.25f)).inflate((double)(grow * 0.45f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(-legGap - this.width * 0.04f), 0.0, (double)(-this.width * 0.04f), (double)(-legGap + this.width * 0.04f), (double)legHeight, (double)(this.width * 0.04f)).inflate((double)(grow * 0.35f)), alpha);
            this.renderPart(ms, fillBuilder, lineBuilder, new AABB((double)(legGap - this.width * 0.04f), 0.0, (double)(-this.width * 0.04f), (double)(legGap + this.width * 0.04f), (double)legHeight, (double)(this.width * 0.04f)).inflate((double)(grow * 0.35f)), alpha);
        }

        private void renderPart(PoseStack ms, BufferBuilder fillBuilder, BufferBuilder lineBuilder, AABB partBox, float alpha) {
            Draw3DUtility.renderFilledBox(ms, fillBuilder, partBox, this.color.withAlpha(18.0f * alpha));
            Draw3DUtility.renderOutlinedBox(ms, lineBuilder, partBox, this.color.withAlpha(235.0f * alpha));
            Draw3DUtility.renderBoxInternalDiagonals(ms, lineBuilder, partBox, this.color.withAlpha(110.0f * alpha));
        }
    }
}
