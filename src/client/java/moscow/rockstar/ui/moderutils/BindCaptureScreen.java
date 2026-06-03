package moscow.rockstar.ui.moderutils;

import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.colors.Colors;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.interfaces.IScaledResolution;
import net.minecraft.client.input.KeyEvent;

public class BindCaptureScreen extends CustomScreen implements IScaledResolution, IMinecraft {
    private final BindSetting setting;
    private final ModerUtilsScreen parent;

    public BindCaptureScreen(BindSetting setting, ModerUtilsScreen parent) {
        this.setting = setting;
        this.parent = parent;
    }

    @Override
    public void render(UIContext context) {
        // Draw dark semi-transparent background
        float width = IScaledResolution.sr.getGuiScaledWidth();
        float height = IScaledResolution.sr.getGuiScaledHeight();
        
        context.drawRect(0, 0, width, height, ColorRGBA.BLACK.withAlpha(150.0f));

        // Draw center box
        float boxW = 220.0f;
        float boxH = 70.0f;
        float x = (width - boxW) / 2.0f;
        float y = (height - boxH) / 2.0f;

        context.drawShadow(x, y, boxW, boxH, 20.0f, BorderRadius.all(6.0f), ColorRGBA.BLACK.withAlpha(200.0f));
        context.drawRoundedRect(x, y, boxW, boxH, BorderRadius.all(6.0f), Colors.getAdditionalColor().withAlpha(240.0f));
        context.drawRoundedBorder(x, y, boxW, boxH, 1.0f, BorderRadius.all(6.0f), Colors.getOutlineColor());

        context.drawCenteredText(
            Fonts.BOLD.getFont(10.0f),
            "Нажмите любую клавишу для бинда",
            width / 2.0f,
            y + 22.0f,
            Colors.getTextColor()
        );

        context.drawCenteredText(
            Fonts.REGULAR.getFont(8.0f),
            "ESC / DELETE - сбросить, ПКМ - отмена",
            width / 2.0f,
            y + 42.0f,
            Colors.getTextColor().mulAlpha(0.6f)
        );
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (button == MouseButton.RIGHT) {
            mc.setScreen(parent);
            return;
        }
        
        setting.setKey(button.getButtonIndex());
        mc.setScreen(parent);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 256 || keyCode == 261) { // ESC or DELETE
            setting.setKey(-1);
        } else {
            setting.setKey(keyCode);
        }
        mc.setScreen(parent);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
