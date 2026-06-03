package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.party.PartyManager;
import moscow.rockstar.systems.party.MemberInfo;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.RenderUtility;
import org.joml.Matrix3x2fStack;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

@ModuleInfo(name="Party System", category=ModuleCategory.OTHER, desc="Система групп и стрелочек тиммейтов")
public class PartySystem extends BaseModule {
    
    private int tickCounter = 0;

    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (mc.player == null || mc.level == null) return;
        
        this.tickCounter++;
        if (this.tickCounter >= 20) {
            this.tickCounter = 0;
            PartyManager.getInstance().sendPositionUpdate();
        }
    };

    private final EventListener<HudRenderEvent> onHudRender = event -> {
        if (mc.player == null || mc.level == null) return;
        
        CustomDrawContext context = event.getContext();
        Matrix3x2fStack ms = context.pose();
        float midX = mc.getWindow().getGuiScaledWidth() / 2.0f;
        float midY = mc.getWindow().getGuiScaledHeight() / 2.0f;
        
        Font font = Fonts.MEDIUM.getFont(6.0f);
        
        String myServer = "singleplayer";
        if (mc.getCurrentServer() != null) {
            net.minecraft.client.multiplayer.ServerData si = mc.getCurrentServer();
            myServer = si.ip != null ? si.ip : (si.name != null ? si.name : "?");
        }
        int myAnarchy = PartyManager.getInstance().getAnarchyFromScoreboard();
        
        // Рендер стрелочек для тиммейтов
        for (Map.Entry<String, MemberInfo> entry : PartyManager.getInstance().members.entrySet()) {
            String name = entry.getKey();
            MemberInfo info = entry.getValue();
            if (info.isStale()) continue;
            
            if (info.server == null) continue;
            if (!myServer.replace("\"", "").equalsIgnoreCase(info.server.replace("\"", ""))) continue;
            if (myAnarchy != info.anarchy) continue;
            
            Vec3 pos = new Vec3(info.x, info.y, info.z).subtract(mc.gameRenderer.getMainCamera().position());
            float cameraYaw = mc.player.getYRot();
            double cos = Mth.cos(cameraYaw * ((float)Math.PI / 180.0f));
            double sin = Mth.sin(cameraYaw * ((float)Math.PI / 180.0f));
            double rotY = -(pos.z * cos - pos.x * sin);
            double rotX = -(pos.x * cos + pos.z * sin);
            float angle = (float)(Math.atan2(rotY, rotX) * 180.0 / Math.PI - 90.0);
            
            ms.pushMatrix();
            ms.translate(midX, midY);
            RenderUtility.rotate(ms, 0.0f, 0.0f, angle);
            
            // Рисуем стрелочку
            context.drawTexture(moscow.rockstar.Rockstar.id("textures/arrow.png"), -10.0f, 40.0f, 20.0f, 20.0f, Colors.getAccentColor());
            
            RenderUtility.end(ms);
            
            // Рисуем текст (имя и дистанция)
            float rad = (float) Math.toRadians(angle);
            float textX = (float) (-Math.sin(rad) * 30.0);
            float textY = (float) (Math.cos(rad) * 30.0);
            
            double dist = new Vec3(info.x, info.y, info.z).distanceTo(mc.player.position());
            String text = String.format("%s (%.0fm)", name, dist);
            float width = font.width(text);
            
            context.drawText(font, text, textX - width / 2.0f, textY + 2.0f, Colors.WHITE);
            
            ms.popMatrix();
        }
    };
}
