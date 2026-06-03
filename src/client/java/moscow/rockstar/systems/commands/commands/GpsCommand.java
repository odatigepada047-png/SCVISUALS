package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import moscow.rockstar.utility.render.RenderUtility;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;

public class GpsCommand implements IMinecraft, IScaledResolution {
    private Integer targetX = null;
    private Integer targetZ = null;
    private boolean isActive = false;
    private final Animation animation = new Animation(500, Easing.BAKEK);

    private final EventListener<HudRenderEvent> onHudRenderEvent = event -> {
        animation.update(this.isActive && this.targetX != null && this.targetZ != null);
        
        float animValue = animation.getRGB();
        if (animValue <= 0.001f || GpsCommand.mc.player == null) {
            return;
        }

        org.joml.Matrix3x2fStack matrices = event.getContext().pose();
        int screenWidth = GpsCommand.mc.getWindow().getGuiScaledWidth();
        int screenHeight = GpsCommand.mc.getWindow().getGuiScaledHeight();

        // Вычисляем расстояние и угол
        double playerX = GpsCommand.mc.player.getX();
        double playerZ = GpsCommand.mc.player.getZ();
        double dx = this.targetX - playerX;
        double dz = this.targetZ - playerZ;
        double distance = Math.sqrt(dx * dx + dz * dz);

        double angleToTarget = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double playerYaw = Mth.wrapDegrees(GpsCommand.mc.player.getYRot());
        double relativeAngle = Mth.wrapDegrees(angleToTarget - playerYaw);

        // Позиция над прицелом
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2 - 110;

        matrices.pushMatrix();
        matrices.translate((float) centerX, (float) centerY);

        // Вращение на месте
        matrices.rotate((float) Math.toRadians(relativeAngle));

        float scaleAnim = 2.0f - animValue;
        float arrowSize = 18f;

        matrices.scale(animValue, animValue);
        matrices.scale(scaleAnim, scaleAnim);

        // Рисуем стрелочку акцентом
        Identifier arrowTexture = Rockstar.id("textures/arrow.png");
        event.getContext().drawTexture(arrowTexture, -arrowSize / 2.0f, -arrowSize / 2.0f, arrowSize, arrowSize, 
                Colors.getAccentColor().mulAlpha(animValue));

        matrices.popMatrix();

        // Расстояние крупным шрифтом
        if (animValue > 0.5f) {
            String distanceText = String.format("%.0fm", distance);
            float textSize = 10.0f; // Увеличенный шрифт
            int textWidth = (int) Fonts.MEDIUM.getFont(textSize).width(distanceText);
            int textX = centerX - textWidth / 2;
            int textY = centerY + 12;

            event.getContext().drawText(Fonts.MEDIUM.getFont(textSize), distanceText, textX, textY, 
                    ColorRGBA.WHITE.mulAlpha(animValue));
        }
    };

    public GpsCommand() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    public Command command() {
        return CommandBuilder.begin("gps")
                .desc("Навигация к координатам")
                .param("action", p -> p.literal("set", "off"))
                .param("x", p -> p.optional().validator(this::verifyCoordinate))
                .param("z", p -> p.optional().validator(this::verifyCoordinate))
                .handler(this::handle)
                .build();
    }

    private ValidationResult verifyCoordinate(String input) {
        try {
            Integer.parseInt(input);
            return ValidationResult.ok(input);
        } catch (NumberFormatException e) {
            return ValidationResult.error("Неправильное число");
        }
    }

    private void handle(CommandContext ctx) {
        String action = (String) ctx.arguments().get(0);
        String x = (String) ctx.arguments().get(1);
        String z = (String) ctx.arguments().get(2);

        switch (action.toLowerCase()) {
            case "set": {
                if (x == null || z == null) {
                    MessageUtility.error(Component.literal("Укажите координаты (.gps set x z)"));
                    return;
                }
                try {
                    this.targetX = Integer.parseInt(x);
                    this.targetZ = Integer.parseInt(z);
                    this.isActive = true;
                    MessageUtility.info(Component.literal("GPS установлен на " + this.targetX + " " + this.targetZ));
                } catch (NumberFormatException e) {
                    MessageUtility.error(Component.literal("Координаты должны быть числами"));
                }
                break;
            }
            case "off": {
                this.targetX = null;
                this.targetZ = null;
                this.isActive = false;
                MessageUtility.info(Component.literal("GPS отключен"));
                break;
            }
        }
    }
}
