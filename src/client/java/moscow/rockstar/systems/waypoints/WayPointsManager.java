package moscow.rockstar.systems.waypoints;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class WayPointsManager {
    private final Map<String, Vec3> waypoints = new HashMap<String, Vec3>();

    public void add(String name, int x, int y, int z) {
        Vec3 pos = new Vec3((double)x, (double)y, (double)z);
        if (this.waypoints.containsKey(name)) {
            this.waypoints.put(name, pos);
            MessageUtility.info(Component.literal(Localizator.translate("modules.waypoints.overwritten", name, x, y, z)));
            return;
        }
        this.waypoints.put(name, pos);
        MessageUtility.info(Component.literal(Localizator.translate("modules.waypoints.added", name, x, y, z)));
    }

    public void add(String name, int x, int y, int z, boolean temp) {
        add(name, x, y, z);
    }

    public static Vec2 projectWorldPositionToGui(Vec3 worldCoords) {
        Matrix4f modelView = (Matrix4f) moscow.rockstar.utility.render.Utils.getCachedLevelModelView();
        Matrix4f projection = (Matrix4f) moscow.rockstar.utility.render.Utils.getCachedLevelProjection();
        Vec3 cameraPos = moscow.rockstar.utility.render.Utils.getCachedCameraPos();
        if (modelView == null || projection == null || cameraPos == null) {
            return null;
        }

        double x = worldCoords.x - cameraPos.x;
        double y = worldCoords.y - cameraPos.y;
        double z = worldCoords.z - cameraPos.z;

        Vector4f pos = new Vector4f((float) x, (float) y, (float) z, 1.0f);
        pos.mul(modelView);
        pos.mul(projection);

        if (pos.w <= 0.0f) {
            return null;
        }

        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;

        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        float screenX = (ndcX + 1.0f) * 0.5f * width;
        float screenY = (1.0f - ndcY) * 0.5f * height;

        return new Vec2(screenX, screenY);
    }

    public void del(String name) {
        if (this.waypoints.remove(name) != null) {
            MessageUtility.info(Component.literal(Localizator.translate("modules.waypoints.deleted", name)));
        } else {
            MessageUtility.info(Component.literal(Localizator.translate("modules.waypoints.not_found", name)));
        }
    }

    public void clear() {
        this.waypoints.clear();
        MessageUtility.info(Component.literal(Localizator.translate("modules.waypoints.cleared")));
    }

    public boolean contains(String name) {
        return this.waypoints.containsKey(name);
    }

    public Set<Map.Entry<String, Vec3>> getEntries() {
        return this.waypoints.entrySet();
    }
}
