package moscow.rockstar.systems.commands.commands;

import java.util.Map;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.waypoints.WayPointsManager;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.Utils;
import org.joml.Matrix3x2fStack;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class WaypointsCommand
implements IMinecraft,
IScaledResolution {
    public WaypointsCommand() {
    }

    public Command command() {
        return CommandBuilder.begin("waypoint").aliases("way").desc("Метки").param("action", p -> p.literal("add", "del", "clear", "self")).param("name", p -> p.optional().validator(ValidationResult::ok)).param("x", p -> p.optional().validator(this::verifyCoordinate)).param("y", p -> p.optional().validator(this::verifyCoordinate)).param("z", p -> p.optional().validator(this::verifyCoordinate)).handler(this::handle).build();
    }

    private ValidationResult verifyCoordinate(String input) {
        try {
            Integer.parseInt(input);
            return ValidationResult.ok(input);
        }
        catch (NumberFormatException e) {
            return ValidationResult.error("Не правильное число");
        }
    }

    private void handle(CommandContext ctx) {
        String action = (String)ctx.arguments().get(0);
        String name = (String)ctx.arguments().get(1);
        String x = (String)ctx.arguments().get(2);
        String y = (String)ctx.arguments().get(3);
        String z = (String)ctx.arguments().get(4);
        WayPointsManager wayPointsManager = Rockstar.getInstance().getWayPointsManager();
        switch (action.toLowerCase()) {
            case "self": {
                if (mc.player == null) {
                    MessageUtility.error(Component.literal("Игрок не найден"));
                    return;
                }
                String waypointName = name;
                if (waypointName == null || waypointName.isBlank()) {
                    waypointName = "Self";
                }
                Vec3 pos = mc.player.position();
                wayPointsManager.add(waypointName, (int) Math.round(pos.x), (int) Math.round(pos.y), (int) Math.round(pos.z));
                break;
            }
            case "add": {
                if (name == null || x == null || y == null || z == null) {
                    MessageUtility.error(Component.literal("Укажите название и координаты (.way add \"Название\" x y z)"));
                    return;
                }
                try {
                    wayPointsManager.add(name, Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
                }
                catch (NumberFormatException e) {
                    MessageUtility.error(Component.literal("Координаты должны быть числами"));
                }
                break;
            }
            case "del": {
                if (name == null) {
                    MessageUtility.error(Component.literal("Укажите название (.way del \"Название\")"));
                    return;
                }
                wayPointsManager.del(name);
                break;
            }
            case "clear": {
                wayPointsManager.clear();
            }
        }
    }
}
