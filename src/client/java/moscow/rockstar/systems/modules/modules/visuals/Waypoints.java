package moscow.rockstar.systems.modules.modules.visuals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.PreHudRenderEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.render.Utils;
import moscow.rockstar.utility.sounds.ClientSounds;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix3x2fStack;

@ModuleInfo(name="Waypoints", category=ModuleCategory.VISUALS, enabledByDefault=true)
public class Waypoints extends BaseModule {
    private final BindSetting createKey = new BindSetting(this, "modules.settings.waypoints.create_key").key(86);
    private final BooleanSetting showWaypoints = new BooleanSetting(this, "modules.settings.waypoints.show_waypoints").enabled(true);
    private final BooleanSetting showTempWaypoints = new BooleanSetting(this, "modules.settings.waypoints.show_temp_waypoints").enabled(true);
    private final Map<String, Waypoint> waypoints = new HashMap<String, Waypoint>();
    
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (this.createKey.isKey(event.getKey()) && event.getAction() == 1 && Waypoints.mc.screen == null && Waypoints.mc.player != null && Waypoints.mc.level != null) {
            Vec3 pos;
            Vec3 start = Waypoints.mc.player.getEyePosition(1.0f);
            Vec3 direction = Waypoints.mc.player.getViewVector(1.0f);
            Vec3 end = start.add(direction.scale(200.0));
            Player targetPlayer = null;
            double closestDistance = Double.MAX_VALUE;
            for (Player player : Waypoints.mc.level.players()) {
                double distance;
                AABB hitbox;
                Vec3 hit;
                if (player == Waypoints.mc.player || (hit = (hitbox = player.getBoundingBox().inflate(0.3)).clip(start, end).orElse(null)) == null || !((distance = start.distanceTo(hit)) < closestDistance) || !(distance <= 200.0)) continue;
                closestDistance = distance;
                targetPlayer = player;
            }
            if (targetPlayer != null) {
                UUID playerUUID = targetPlayer.getUUID();
                if (this.waypoints.values().stream().anyMatch(w -> playerUUID.equals(w.playerUUID))) {
                    return;
                }
                String name = targetPlayer.getName().getString();
                pos = targetPlayer.position();
                this.add(name, pos.x, pos.y, pos.z, true, playerUUID);
                return;
            }
            HitResult raycastResult = Waypoints.mc.player.pick(200.0, 1.0f, false);
            if (raycastResult.getType() == HitResult.Type.BLOCK && raycastResult instanceof BlockHitResult) {
                BlockHitResult blockHit = (BlockHitResult)raycastResult;
                pos = blockHit.getLocation();
                String baseName = Localizator.translate("modules.waypoints.base_name");
                if (baseName == null || baseName.isBlank()) {
                    baseName = "Waypoint";
                }
                String name = baseName;
                int counter = 1;
                while (this.waypoints.containsKey(name)) {
                    name = baseName + " " + counter++;
                }
                this.add(name, pos.x, pos.y, pos.z, true, null);
            }
        }
    };

    private final EventListener<PreHudRenderEvent> onHudRenderEvent = event -> {
        Matrix3x2fStack matrices = event.getContext().getMatrices();
        float tickDelta = event.getGameTimeDeltaPartialTick();
        long currentTime = System.currentTimeMillis();
        this.waypoints.entrySet().removeIf(entry -> {
            Waypoint waypoint = entry.getValue();
            if (waypoint.temp && currentTime - waypoint.creationTime > 5000L) {
                return true;
            }
            if (waypoint.playerUUID != null) {
                Player player = Waypoints.mc.level.getPlayerByUUID(waypoint.playerUUID);
                if (player != null) {
                    Vec3 targetPos = Utils.getInterpolatedPos(player, tickDelta);
                    float alpha = 0.2f * tickDelta;
                    waypoint.pos = waypoint.pos.lerp(targetPos, (double)Mth.clamp(alpha, 0.0f, 1.0f));
                } else {
                    MessageUtility.info(Component.literal(Localizator.translate("modules.waypoints.player_removed", waypoint.name)));
                    return true;
                }
            }
            return false;
        });

        if (this.showTempWaypoints.isEnabled()) {
            for (Waypoint waypoint : this.waypoints.values()) {
                Vec3 renderPos = waypoint.pos;
                Vec3 renderPosAdjusted = renderPos.add(0.0, 0.5, 0.0);
                Vec2 screenPos = Utils.worldToScreen(renderPosAdjusted);
                if (screenPos == null) continue;
                float distance = (float)Waypoints.mc.player.position().distanceTo(renderPos);
                float scale = Mth.clamp((1.0f - distance / 20.0f), 0.5f, 1.0f);
                matrices.pushMatrix();
                matrices.translate(screenPos.x, screenPos.y);
                matrices.scale(scale, scale);
                String text = waypoint.name + " (" + String.format("%.1f", distance) + "m)";
                int width = (int)Fonts.MEDIUM.getFont(11.0f).width(text);
                int x = -width / 2;
                int iconSize = 32;
                event.getContext().drawRoundedRect((float)(x - 3), 2.0f, (float)(width + 8), Fonts.MEDIUM.getFont(11.0f).height() + 6.0f, BorderRadius.all(3.0f), new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
                event.getContext().drawText(Fonts.MEDIUM.getFont(11.0f), text, x, 5.0f, ColorRGBA.WHITE);

                matrices.popMatrix();
            }
        }

        if (this.showWaypoints.isEnabled()) {
            moscow.rockstar.framework.msdf.Font font = Fonts.MEDIUM.getFont(11.0f);
            for (Map.Entry<String, Vec3> entry : Rockstar.getInstance().getWayPointsManager().getEntries()) {
                String name = entry.getKey();
                Vec3 pos = entry.getValue();
                Vec3 renderPos = pos.add(0.5, 0.5, 0.5);
                Vec3 renderPosAdjusted = renderPos.add(0.0, 0.5, 0.0);
                Vec2 screenPos = Utils.worldToScreen(renderPosAdjusted);
                if (screenPos == null) continue;
                float distance = (float)Waypoints.mc.player.position().distanceTo(renderPos);
                float scale = Mth.clamp((1.0f - distance / 20.0f), 0.5f, 1.0f);
                
                matrices.pushMatrix();
                matrices.translate(screenPos.x, screenPos.y);
                matrices.scale(scale, scale);

                String icon = "";
                String displayName = name;
                if (name.length() > 2 && name.charAt(1) == ' ' && name.charAt(0) >= 'A' && name.charAt(0) <= 'F') {
                    icon = String.valueOf(name.charAt(0));
                    displayName = name.substring(2);
                } else if (name.length() > 0 && name.charAt(0) >= 'A' && name.charAt(0) <= 'F') {
                    icon = String.valueOf(name.charAt(0));
                    displayName = name;
                }

                String distanceText = displayName + " (" + String.format("%.1f", distance) + "m)";
                float textWidth = font.width(distanceText);
                float textHeight = font.height();
                float x = -textWidth / 2.0f;

                event.getContext().drawRoundedRect(x - 4.0f, 2.0f, textWidth + 8.0f, textHeight + 6.0f, BorderRadius.all(3.0f), new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));

                if (!icon.isEmpty()) {
                    int index = icon.charAt(0) - 'A';
                    if (index >= 0 && index <= 5) {
                        moscow.rockstar.framework.msdf.MsdfRenderer.renderText(
                                moscow.rockstar.framework.msdf.Fonts.WAYPOINT_ICONS,
                                icon,
                                24.0f,
                                ColorRGBA.WHITE.getRGB(),
                                moscow.rockstar.utility.render.HudMatrices.toMatrix4f(matrices),
                                -11.0f,
                                -23.0f,
                                0.0f);
                    }
                }

                event.getContext().drawText(font, distanceText, x + 1.0f, 5.0f, ColorRGBA.WHITE);
                matrices.popMatrix();
            }
        }
    };

    private void add(String name, double x, double y, double z, boolean isTemp, UUID playerUUID) {
        Vec3 pos = new Vec3(x, y, z);
        if (this.waypoints.containsKey(name)) {
            MessageUtility.error(Component.literal(Localizator.translate("modules.waypoints.exists", name)));
            return;
        }
        this.waypoints.put(name, new Waypoint(name, pos, isTemp, System.currentTimeMillis(), playerUUID));
        ClientSounds.MODULE.play(0.5f);
    }

    public void deleteWaypoint(String name) {
        Rockstar.getInstance().getWayPointsManager().del(name);
    }

    public void addFromParty(String name, double x, double y, double z, String owner) {
        String finalName = owner == null || owner.isBlank() ? name : name + " (" + owner + ")";
        Rockstar.getInstance().getWayPointsManager().add(finalName, (int) x, (int) y, (int) z);
    }

    private static class Waypoint {
        public String name;
        public Vec3 pos;
        public boolean temp;
        public long creationTime;
        public UUID playerUUID;

        public Waypoint(String name, Vec3 pos, boolean temp, long creationTime, UUID playerUUID) {
            this.name = name;
            this.pos = pos;
            this.temp = temp;
            this.creationTime = creationTime;
            this.playerUUID = playerUUID;
        }
    }
}
