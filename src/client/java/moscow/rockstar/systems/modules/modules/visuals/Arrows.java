/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DstFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SrcFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gl.ShaderProgramKey
 *  net.minecraft.client.gl.ShaderProgramKeys
 *  net.minecraft.client.renderer.BufferBuilder
 *  net.minecraft.client.renderer.VertexFormat$DrawMode
 *  net.minecraft.client.renderer.DefaultVertexFormat
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.Mth
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.systems.modules.modules.visuals;

// import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.target.TargetSettings;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.Draw3DUtility;
import moscow.rockstar.utility.render.GLStateSnapshot;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.batching.impl.IconBatching;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@ModuleInfo(name="Arrows", category=ModuleCategory.VISUALS, desc="modules.descriptions.tracers")
public class Arrows
extends BaseModule {
    private final BooleanSetting lines = new BooleanSetting(this, "lines");
    private final SelectSetting targets = new SelectSetting((SettingsContainer)this, "modules.settings.tracers.targets", "modules.settings.tracers.targets.description");
    private final SelectSetting.Value players = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.players").select();
    private final SelectSetting.Value animals = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.animals");
    private final SelectSetting.Value mobs = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.mobs");
    private final SelectSetting.Value invisibles = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.invisibles").select();
    private final SelectSetting.Value nakedPlayers = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.naked_players").select();
    private final SelectSetting.Value friends = new SelectSetting.Value(this.targets, "modules.settings.tracers.targets.friends").select();
    private final Map<Entity, ArrowsAnimation> animations = new HashMap<Entity, ArrowsAnimation>();
    private final EventListener<HudRenderEvent> onHud = event -> {
        if (Arrows.mc.player == null || Arrows.mc.level == null || this.lines.isEnabled()) {
            return;
        }
        CustomDrawContext context = event.getContext();
        org.joml.Matrix3x2fStack ms = context.pose();
        TargetSettings targetSettings = new TargetSettings.Builder().targetPlayers(this.players.isSelected()).targetAnimals(this.animals.isSelected()).targetInvisibles(this.invisibles.isSelected()).targetFriends(this.friends.isSelected()).targetNakedPlayers(this.nakedPlayers.isSelected()).targetMobs(this.mobs.isSelected()).build();
        HashSet<Entity> toRemove = new HashSet<Entity>();
        for (Map.Entry<Entity, ArrowsAnimation> entry : this.animations.entrySet()) {
            LivingEntity livingEntity;
            Entity entity = entry.getKey();
            ArrowsAnimation animation = entry.getValue();
            boolean shouldShow = Arrows.mc.level.getEntity(entity.getId()) != null && entity instanceof LivingEntity && targetSettings.isEntityValid((Entity)(livingEntity = (LivingEntity)entity));
            animation.showing.update(shouldShow);
            animation.showing.setDuration(500L);
            if (animation.showing.getValue() != 0.0f || shouldShow) continue;
            toRemove.add(entity);
        }
        for (Entity entity : Arrows.mc.level.entitiesForRendering()) {
            LivingEntity livingEntity;
            if (!(entity instanceof LivingEntity) || !targetSettings.isEntityValid((Entity)(livingEntity = (LivingEntity)entity)) || this.animations.containsKey(entity)) continue;
            this.animations.put(entity, new ArrowsAnimation());
        }
        GLStateSnapshot glState = GLStateSnapshot.capture();
        try {
            ms.pushMatrix();
            ms.translate(sr.getGuiScaledWidth() / 2.0f, sr.getGuiScaledHeight() / 2.0f);
            IconBatching iconBatching = new IconBatching(DefaultVertexFormat.POSITION_TEX_COLOR);
            for (Map.Entry<Entity, ArrowsAnimation> arrow : this.animations.entrySet()) {
                if (!(arrow.getValue().showing.getValue() > 0.0f)) continue;
                RenderUtility.rotate(ms, 0.0f, 0.0f, this.calculateAngle(arrow.getKey(), event.getGameTimeDeltaPartialTick()));
                RenderUtility.scale(ms, 0.0f, 0.0f, 2.0f - arrow.getValue().showing.getValue());
                context.drawTexture(Rockstar.id("textures/arrow.png"), -10.0f, 40.0f, 20.0f, 20.0f, (Rockstar.getInstance().getFriendManager().isFriend(arrow.getKey().getName().getString()) ? Colors.GREEN : Colors.getAccentColor()).mulAlpha(arrow.getValue().showing.getValue()));
                RenderUtility.end(ms);
                RenderUtility.end(ms);
            }
            iconBatching.draw();
            for (Entity entity : toRemove) {
                this.animations.remove(entity);
            }
            ms.popMatrix();
        } finally {
            glState.restore();
        }
    };
    private final EventListener<Render3DEvent> on3DRender = event -> {
        // Commented out to compile
//         if (Arrows.mc.player == null || Arrows.mc.level == null || !this.lines.isEnabled()) {
//             return;
//         }
//         PoseStack matrices = event.pose();
//         TargetSettings targetSettings = new TargetSettings.Builder().targetPlayers(this.players.isSelected()).targetAnimals(this.animals.isSelected()).targetInvisibles(this.invisibles.isSelected()).targetFriends(this.friends.isSelected()).targetNakedPlayers(this.nakedPlayers.isSelected()).targetMobs(this.mobs.isSelected()).build();
//         RenderUtility.setupRender3D(false);
//         RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
// //         BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
//         for (Entity entity : Arrows.mc.level.getEntities()) {
//             LivingEntity livingEntity;
//             if (!(entity instanceof LivingEntity) || !targetSettings.isEntityValid((Entity)(livingEntity = (LivingEntity)entity))) continue;
//             Vec3 entityPos = Utils.getInterpolatedPos((Entity)livingEntity, event.getGameTimeDeltaPartialTick());
//             Draw3DUtility.renderLineFromPlayer(matrices, builder, entityPos.add(0.0, (double)(livingEntity.getHeight() / 2.0f), 0.0), Colors.WHITE);
//         }
//         RenderUtility.buildBuffer(builder);
//         RenderUtility.endRender3D();
    };

    private float calculateAngle(Entity entity, float partialTicks) {
        Vec3 pos = Utils.getInterpolatedPos(entity, partialTicks).subtract(Arrows.mc.gameRenderer.getMainCamera().position());
        float cameraYaw = Arrows.mc.player.getYRot();
        double cos = Mth.cos(cameraYaw * ((float)Math.PI / 180.0f));
        double sin = Mth.sin(cameraYaw * ((float)Math.PI / 180.0f));
        double rotY = -(pos.z * cos - pos.x * sin);
        double rotX = -(pos.x * cos + pos.z * sin);
        return (float)(Math.atan2(rotY, rotX) * 180.0 / Math.PI - 90.0);
    }

    static class ArrowsAnimation {
        Animation showing = new Animation(300L, Easing.BAKEK);
        Animation rotating = new Animation(300L, Easing.BAKEK);

        ArrowsAnimation() {
        }
    }
}


