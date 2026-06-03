/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
 *  net.minecraft.client.gui.screens.options.OptionsScreen
 *  net.minecraft.client.gui.screens.world.SelectWorldScreen
 */
package moscow.rockstar.ui.mainmenu;

import moscow.rockstar.utility.game.KeyUtility;
import moscow.rockstar.utility.render.ShaderColorHelper;
import net.minecraft.client.input.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.framework.base.CustomScreen;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.msdf.Font;
import moscow.rockstar.framework.msdf.Fonts;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.objects.gradient.impl.VerticalGradient;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.modules.other.Sounds;
import moscow.rockstar.ui.mainmenu.CustomButton;
import moscow.rockstar.utility.animation.base.Animation;
import moscow.rockstar.utility.animation.base.Easing;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.TextUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.math.MathUtility;
import moscow.rockstar.utility.render.DrawUtility;
import moscow.rockstar.utility.render.RenderUtility;
import moscow.rockstar.utility.render.obj.Rect;
import moscow.rockstar.utility.sounds.ClientSounds;
import moscow.rockstar.systems.parallax.ParallaxManager;
import moscow.rockstar.systems.parallax.ParallaxLayer;
import moscow.rockstar.utility.math.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import ru.kotopushka.compiler.sdk.annotations.Compile;
import ru.kotopushka.compiler.sdk.annotations.VMProtect;
import ru.kotopushka.compiler.sdk.enums.VMProtectType;

public class CustomTitleScreen
extends CustomScreen
implements IMinecraft {
    private static boolean once;
    private static final List<CustomButton> buttons;
    private boolean active;
    private final Animation activeAnimation = new Animation(1000L, 0.0f, Easing.FIGMA_EASE_IN_OUT);
    private final ColorRGBA dateColor = new ColorRGBA(171.0f, 254.0f, 255.0f);
    private final ColorRGBA timeColor = new ColorRGBA(203.0f, 254.0f, 255.0f);
    private float gyroX = 0.0f;
    private float gyroY = 0.0f;
    private double lastMouseY = -1.0;

    @Compile
    @VMProtect(type=VMProtectType.MUTATION)
    protected void init() {
        String basePath = "image/mainmenu/icons/";
        if (!once) {
            if (Rockstar.getInstance().getModuleManager().getModule(Sounds.class).isEnabled()) {
                ClientSounds.WELCOME.play(Rockstar.getInstance().getModuleManager().getModule(Sounds.class).getVolume().getCurrentValue());
            }
            buttons.add(new CustomButton(basePath + "single.png", 12.0f, () -> mc.setScreen((Screen)new SelectWorldScreen((Screen)this))));
            buttons.add(new CustomButton(basePath + "multi.png", 12.0f, () -> mc.setScreen((Screen)new JoinMultiplayerScreen((Screen)this))));
            buttons.add(new CustomButton(basePath + "settings.png", 12.0f, () -> mc.setScreen((Screen)new OptionsScreen((Screen)this, CustomTitleScreen.mc.options, false))));
            buttons.add(new CustomButton(basePath + "quit.png", 14.0f, () -> ((Minecraft)mc).stop()));
            once = true;
        }
        super.init();
    }

    @Override
    public void render(UIContext context) {
        Font timeFont = Fonts.ROUND_BOLD.getFont(65.0f);
        Font dateFont = Fonts.MEDIUM.getFont(16.0f);
        Font unlockFont = Fonts.REGULAR.getFont(10.0f);
        float textAlpha = 255.0f * (0.5f + 0.5f * this.activeAnimation.getRGB());
        float timeOffset = MathUtility.interpolate((float)this.height / 2.0f - 20.0f, 80.0, this.activeAnimation.getRGB());
        this.activeAnimation.update(this.active);
        
        // Эффект гироскопа: плавно интерполируем положение к позиции мыши
        if (this.width > 0 && this.height > 0) {
            float targetGyroX = (float) (context.getMouseX() - this.width / 2.0) / (this.width / 2.0f) * 15.0f;
            float targetGyroY = (float) (context.getMouseY() - this.height / 2.0) / (this.height / 2.0f) * 15.0f;
            
            // Защита от NaN и резких прыжков
            if (Float.isNaN(targetGyroX)) targetGyroX = 0.0f;
            if (Float.isNaN(targetGyroY)) targetGyroY = 0.0f;
            
            float speed = 0.1f * context.getDelta();
            this.gyroX = MathUtility.interpolate(this.gyroX, Math.clamp(targetGyroX, -15.0f, 15.0f), speed);
            this.gyroY = MathUtility.interpolate(this.gyroY, Math.clamp(targetGyroY, -15.0f, 15.0f), speed);
        }
        
        // Финальная проверка перед отрисовкой
        if (Float.isNaN(this.gyroX)) this.gyroX = 0.0f;
        if (Float.isNaN(this.gyroY)) this.gyroY = 0.0f;

//         context.drawRoundedRect(0.0f, 0.0f, (float)this.width, (float)this.height, BorderRadius.ZERO, new VerticalGradient(new ColorRGBA(26.0f, 34.0f, 56.0f), new ColorRGBA(5.0f, 3.0f, 12.0f)));

        // Отрисовка фона с небольшим зумом и смещением
        float zoom = 1.05f;
        RenderUtility.scale(context.pose(), (float)this.width / 2.0f, (float)this.height / 2.0f, zoom);
        
        ShaderColorHelper.reset();
        context.drawTexture(Rockstar.id("image/mainmenu/background.png"), -this.gyroX, -this.gyroY, (float)this.width, (float)this.height);
        
        RenderUtility.end(context.pose());
        
        // Отрисовка времени с эффектом растяжения (iOS Style)
        // Если active = false (заблокировано), растягиваем цифры вниз
        float stretchFactor = 1.0f + (1.0f - this.activeAnimation.getRGB()) * 0.6f;
        
        context.drawCenteredText(dateFont, TextUtility.getFormattedDate(), (float)this.width / 2.0f, timeOffset - 23.0f, ColorRGBA.WHITE.withAlpha(textAlpha));
        
        RenderUtility.scale(context.pose(), (float)this.width / 2.0f, timeOffset, 1.0f, stretchFactor);
        context.drawCenteredText(timeFont, TextUtility.getCurrentTime(), (float)this.width / 2.0f, timeOffset, ColorRGBA.WHITE.withAlpha(textAlpha));
        RenderUtility.end(context.pose());

//         context.drawRoundedRect((float)this.width / 2.0f - 36.0f, (float)(this.height - 5) - 3.0f * this.activeAnimation.getRGB(), 72.0f, 3.0f, BorderRadius.all(1.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.activeAnimation.getRGB()));
        context.drawCenteredText(unlockFont, Localizator.translate("mainmenu.next"), (float)this.width / 2.0f, (float)(this.height - 15) + 3.0f * this.activeAnimation.getRGB(), ColorRGBA.WHITE.withAlpha(155.0f * (1.0f - this.activeAnimation.getRGB())));
        float offset = 0.0f;
        for (CustomButton button : buttons) {
            button.getActiveAnim().update((float)(buttons.size() - buttons.indexOf(button)) > (1.0f - this.activeAnimation.getRGB()) * (float)buttons.size() + 0.5f);
            button.set((float)this.width / 2.0f - 69.0f + offset, (this.height > 500 ? (float)this.height / 2.0f : (float)this.height / 1.25f) - 5.0f - 10.0f * button.getActiveAnim().getValue(), 30.0f, 30.0f);
            offset += button.getWidth() + 6.0f;
            button.draw(context);
        }
        if (this.shouldShowIsland()) {
            Rockstar.getInstance().getHud().getIsland().render(context);
        }
    }

    @Override
    @Compile
    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.shouldShowIsland() && Rockstar.getInstance().getHud().getIsland().handleClick((float)mouseX, (float)mouseY, button.getButtonIndex())) {
            return;
        }
        this.lastMouseY = mouseY;
        if (this.active) {
            for (CustomButton customButton : buttons) {
                if (!customButton.hovered(mouseX, mouseY) || customButton.getActiveAnim().getValue() != 1.0f) continue;
                customButton.click(mouseX, mouseY, button.getButtonIndex());
                return;
            }
        }
        super.onMouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
        this.lastMouseY = -1.0;
        super.onMouseReleased(mouseX, mouseY, button);
    }

    @Compile
    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (!this.active) {
            this.active = true;
            return true;
        }
        if (keyCode == 69) {
            Rockstar.getInstance().getThemeManager().switchTheme();
        }
        if (KeyUtility.hasControlDown() && keyCode == 82) {
            Minecraft.getInstance().setScreen((Screen)new JoinMultiplayerScreen((Screen)this));
        }
        if (KeyUtility.hasControlDown() && keyCode == 84) {
            Minecraft.getInstance().setScreen((Screen)new SelectWorldScreen((Screen)this));
        }
        return super.keyPressed(event);
    }

    private boolean shouldShowIsland() {
        return Rockstar.getInstance().getMusicTracker().haveActiveSession();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (this.lastMouseY != -1.0) {
            double diff = mouseY - this.lastMouseY;
            if (Math.abs(diff) > 20.0) {
                if (diff < -20.0) { // Смахивание вверх
                    this.active = true;
                } else if (diff > 20.0 && this.active) { // Смахивание вниз
                    this.active = false;
                }
                this.lastMouseY = -1.0;
            }
        }
        super.mouseMoved(mouseX, mouseY);
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    static {
        buttons = new ArrayList<CustomButton>();
    }
}
