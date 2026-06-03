/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.client.network.AbstractClientPlayer
 *  net.minecraft.client.renderer.BufferBuilder
 *  net.minecraft.client.renderer.BufferRenderer
 *  net.minecraft.client.renderer.MeshData
 *  net.minecraft.client.renderer.Camera
 *  net.minecraft.client.util.math.PoseStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.Vec3
 */
package moscow.rockstar.systems.modules.modules.visuals;

import lombok.Generated;
import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.render.CrystalRenderer;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.render.MeshDrawHelper;
import moscow.rockstar.framework.shader.GlProgram;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.player.AbstractClientPlayer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;



@ModuleInfo(name="Friend Markers", desc="\u0412\u044b\u0434\u0435\u043b\u044f\u0435\u0442 \u0434\u0440\u0443\u0437\u0435\u0439", category=ModuleCategory.VISUALS)
public class FriendMarkers
extends BaseModule {
    private static final float SIMS_SCALE = 0.12f;
    private final ModeSetting setting = new ModeSetting(this, "modules.settings.friends_markers.setting");
    private final ModeSetting.Value heads = new ModeSetting.Value(this.setting, "modules.settings.friends_markers.heads");
    private final ModeSetting.Value sims = new ModeSetting.Value(this.setting, "Sims");
    private final java.util.Map<java.util.UUID, Vec3> crystalPositions = new java.util.HashMap<>();

    private final EventListener<Render3DEvent> onRender3D = event -> {
        if (RenderSystem.outputColorTextureOverride != null) {
            return;
        }
        if (!this.sims.isSelected() || FriendMarkers.mc.level == null) {
            return;
        }
        moscow.rockstar.utility.render.ShaderColorHelper.reset();
        RenderUtility.setupRender3D(true);
        PoseStack ms = event.pose();
        ColorRGBA color = new ColorRGBA(52.0f, 199.0f, 89.0f);
        BufferBuilder builder = CrystalRenderer.createBuffer();
        float tickDelta = event.getGameTimeDeltaPartialTick();

        this.crystalPositions.keySet().removeIf(uuid -> FriendMarkers.mc.level.getPlayerByUUID(uuid) == null);

        for (AbstractClientPlayer player : FriendMarkers.mc.level.players()) {
            if (!player.isAlive() || !Rockstar.getInstance().getFriendManager().isFriend(player.getName().getString())) continue;

            java.util.UUID uuid = player.getUUID();
            Vec3 targetPos = Utils.getInterpolatedPos((Entity)player, tickDelta);
            Vec3 currentPos = this.crystalPositions.get(uuid);
            if (currentPos == null || currentPos.distanceTo(targetPos) > 10.0) {
                currentPos = targetPos;
            } else {
                double lerpFactor = 0.15;
                currentPos = new Vec3(
                    currentPos.x + (targetPos.x - currentPos.x) * lerpFactor,
                    currentPos.y + (targetPos.y - currentPos.y) * lerpFactor,
                    currentPos.z + (targetPos.z - currentPos.z) * lerpFactor
                );
            }
            this.crystalPositions.put(uuid, currentPos);

            ms.pushPose();
            RenderUtility.prepareMatrices(ms, currentPos);
            this.renderSimsCrystal(ms, builder, player, tickDelta, color);
            ms.popPose();
        }
        MeshData built = builder.build();
        if (built != null) {
            GlProgram.usePositionColor();
            MeshDrawHelper.drawBuilt(built);
        }
        RenderUtility.endRender3D();
    };

    @Override
    public void onDisable() {
        this.crystalPositions.clear();
        super.onDisable();
    }

    @Generated
    public ModeSetting.Value getHeads() {
        return this.heads;
    }

    private void renderSimsCrystal(PoseStack ms, BufferBuilder builder, AbstractClientPlayer player, float tickDelta, ColorRGBA color) {
        CrystalRenderer.render(ms, builder, 0.0f, player.getBbHeight() + 0.4f, 0.0f, SIMS_SCALE, color.withAlpha(255.0f));
    }
}

