package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;

@ModuleInfo(name = "Freelook", category = ModuleCategory.PLAYER, desc = "modules.descriptions.freelook")
public class Freelook extends BaseModule {
    
    private final BindSetting button = new BindSetting(this, "Кнопка");
    
    private boolean isActive = false;
    private float cameraYaw = 0.0f;
    private float cameraPitch = 0.0f;

    private float savedYaw = 0.0f;
    private float savedPitch = 0.0f;
    private CameraType savedPerspective = CameraType.FIRST_PERSON;
    
    private final EventListener<KeyPressEvent> onKeyPress = event -> {
        if (this.button.isKey(event.getKey())) {
            if (event.getAction() == 1) { // Press
                if (mc.screen == null) {
                    this.startFreelook();
                }
            } else if (event.getAction() == 0) { // Release
                this.stopFreelook();
            }
        }
    };
    
    private final EventListener<MouseEvent> onMouseEvent = event -> {
        if (this.button.isKey(event.getButton())) {
            if (event.getAction() == 1) { // Press
                if (mc.screen == null) {
                    this.startFreelook();
                }
            } else if (event.getAction() == 0) { // Release
                this.stopFreelook();
            }
        }
    };
    
    private void startFreelook() {
        if (mc.player == null || this.isActive) {
            return;
        }
        
        this.isActive = true;
        
        // Сохраняем текущие углы игрока
        this.savedYaw = mc.player.getYRot();
        this.savedPitch = mc.player.getXRot();
        
        // Инициализируем камеру текущими углами
        this.cameraYaw = this.savedYaw;
        this.cameraPitch = this.savedPitch;

        // Сохраняем и переключаем перспективу
        this.savedPerspective = mc.options.getCameraType();
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
    }
    
    private void stopFreelook() {
        if (!this.isActive) {
            return;
        }
        
        this.isActive = false;
        
        // Восстанавливаем перспективу
        mc.options.setCameraType(this.savedPerspective);
        
        // Восстанавливаем углы игрока
        if (mc.player != null) {
            mc.player.setYRot(this.savedYaw);
            mc.player.setXRot(this.savedPitch);
            mc.player.yRotO = this.savedYaw;
            mc.player.xRotO = this.savedPitch;
        }
    }
    
    public void updateRotation(double dx, double dy, double timeDelta, SmoothDouble smoothTurnX, SmoothDouble smoothTurnY) {
        if (!this.isActive) {
            return;
        }

        double gcd = RotationMath.getGcd();
        double deltaYaw;
        double deltaPitch;
        if (mc.options.smoothCamera) {
            deltaYaw = smoothTurnX.getNewDeltaValue(dx * gcd, timeDelta * gcd);
            deltaPitch = smoothTurnY.getNewDeltaValue(dy * gcd, timeDelta * gcd);
        } else {
            deltaYaw = dx * gcd;
            deltaPitch = dy * gcd;
        }

        if (mc.options.invertMouseX().get()) {
            deltaYaw = -deltaYaw;
        }
        if (mc.options.invertMouseY().get()) {
            deltaPitch = -deltaPitch;
        }

        Rotation rotation = RotationMath.correctRotation(new Rotation(this.cameraYaw + (float) deltaYaw, this.cameraPitch + (float) deltaPitch));
        this.cameraYaw = rotation.getYRot();
        this.cameraPitch = Mth.clamp(rotation.getXRot(), -90.0f, 90.0f);
    }
    
    public boolean isFreelookActive() {
        return this.isActive && isEnabled();
    }
    
    public float getCameraYaw(float tickDelta) {
        return this.cameraYaw;
    }

    public float getCameraPitch(float tickDelta) {
        return this.cameraPitch;
    }
    
    public float getSavedYaw() {
        return this.savedYaw;
    }
    
    public float getSavedPitch() {
        return this.savedPitch;
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        if (this.isActive) {
            this.stopFreelook();
        }
    }
}
