package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.render.Utils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

@ModuleInfo(name = "TNT Timer", category = ModuleCategory.VISUALS, desc = "modules.descriptions.tnt_timer")
public class TNTTimer extends BaseModule {
    private final EventListener<HudRenderEvent> onHudRenderEvent = event -> {
        if (mc.level == null || mc.player == null) {
            return;
        }

        Font font = Fonts.MEDIUM.getFont(11.0f);
        float tickDelta = event.getGameTimeDeltaPartialTick();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof PrimedTnt tnt)) {
                continue;
            }

            float seconds = tnt.getFuse() / 20.0f;
            String text = Localizator.translate("modules.tnt_timer.format", seconds);

            Vec3 renderPos = Utils.getInterpolatedPos(tnt, tickDelta).add(0.0, 0.6, 0.0);
            Vec2 screenPos = Utils.worldToScreen(renderPos);
            if (screenPos == null) {
                continue;
            }

            float distance = (float) mc.player.position().distanceTo(renderPos);
            float scale = Mth.clamp(1.0f - distance / 20.0f, 0.5f, 1.0f);
            float w = font.width(text) + 26.0f;
            float h = font.height() + 8.0f;
            float x = screenPos.x - w / 2.0f;
            float y = screenPos.y;

            event.getContext().pose().pushMatrix();
            event.getContext().pose().translate(screenPos.x, screenPos.y);
            event.getContext().pose().scale(scale, scale);

            event.getContext().drawRect(-w / 2.0f, 1.0f, w, h, new ColorRGBA(0.0f, 0.0f, 0.0f, 100.0f));
            event.getContext().drawText(font, text, -font.width(text) / 2.0f + 8.0f, 5.0f, Colors.WHITE);
            event.getContext().drawBatchItem(net.minecraft.world.item.Items.TNT.getDefaultInstance(), (int) (-font.width(text) / 2.0f - 8.0f), 3);

            event.getContext().pose().popMatrix();
        }
    };
}
