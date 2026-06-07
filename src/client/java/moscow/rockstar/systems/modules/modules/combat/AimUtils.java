package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.Render3DEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.rotations.Rotation;
import moscow.rockstar.utility.rotations.RotationMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@ModuleInfo(name = "Aim Utils", category = ModuleCategory.COMBAT)
public class AimUtils extends BaseModule {

    private final SelectSetting targets = new SelectSetting(this, "modules.settings.aim_utils.targets");
    private final SelectSetting.Value playersArmored = new SelectSetting.Value(this.targets, "modules.settings.aim_utils.targets.players_armored").select();
    private final SelectSetting.Value playersNaked = new SelectSetting.Value(this.targets, "modules.settings.aim_utils.targets.players_naked").select();
    private final SelectSetting.Value animals = new SelectSetting.Value(this.targets, "modules.settings.aim_utils.targets.animals");
    private final SelectSetting.Value mobs = new SelectSetting.Value(this.targets, "modules.settings.aim_utils.targets.mobs");
    private final SelectSetting.Value invisibles = new SelectSetting.Value(this.targets, "modules.settings.aim_utils.targets.invisibles").select();

    private final SliderSetting fov = new SliderSetting(this, "FOV")
            .min(1.0f).max(180.0f).step(1.0f).currentValue(60.0f);
    
    private final SliderSetting range = new SliderSetting(this, "Range")
            .min(1.0f).max(100.0f).step(1.0f).currentValue(60.0f);

    private final SliderSetting yawSpeed = new SliderSetting(this, "Yaw Speed")
            .min(1.0f).max(360.0f).step(1.0f).currentValue(60.0f);

    private final SliderSetting pitchSpeed = new SliderSetting(this, "Pitch Speed")
            .min(1.0f).max(360.0f).step(1.0f).currentValue(60.0f);

    private final SliderSetting yawSmoothness = new SliderSetting(this, "Yaw Smoothness")
            .min(1.0f).max(30.0f).step(1.0f).currentValue(5.0f);

    private final SliderSetting pitchSmoothness = new SliderSetting(this, "Pitch Smoothness")
            .min(1.0f).max(30.0f).step(1.0f).currentValue(5.0f);

    private final BooleanSetting pitchAim = new BooleanSetting(this, "Pitch Aiming")
            .enabled(true);

    private long lastTime = System.currentTimeMillis();

    private final EventListener<Render3DEvent> onRender = event -> {
        if (AimUtils.mc.player == null || AimUtils.mc.level == null) {
            return;
        }

        long now = System.currentTimeMillis();
        float deltaTime = Math.min(2.0f, (now - this.lastTime) / 50.0f);
        this.lastTime = now;

        LivingEntity target = this.getBestTarget();
        if (target == null) {
            return;
        }

        Vec3 targetPoint = RotationMath.getNearestPoint(target);
        Rotation targetRot = RotationMath.getRotationTo(targetPoint);

        float currentYaw = AimUtils.mc.player.getYRot();
        float currentPitch = AimUtils.mc.player.getXRot();

        float yawDiff = RotationMath.getAngleDifference(currentYaw, targetRot.getYRot());
        float yawStep;
        if (this.yawSmoothness.getCurrentValue() <= 1.0f) {
            yawStep = yawDiff;
        } else {
            yawStep = (yawDiff / this.yawSmoothness.getCurrentValue()) * deltaTime;
        }
        float maxYawSpeed = this.yawSpeed.getCurrentValue() * deltaTime;
        if (Math.abs(yawStep) > maxYawSpeed) {
            yawStep = Math.signum(yawStep) * maxYawSpeed;
        }
        float newYaw = currentYaw + yawStep;

        float newPitch = currentPitch;
        if (this.pitchAim.isEnabled()) {
            float pitchDiff = targetRot.getXRot() - currentPitch;
            float pitchStep;
            if (this.pitchSmoothness.getCurrentValue() <= 1.0f) {
                pitchStep = pitchDiff;
            } else {
                pitchStep = (pitchDiff / this.pitchSmoothness.getCurrentValue()) * deltaTime;
            }
            float maxPitchSpeed = this.pitchSpeed.getCurrentValue() * deltaTime;
            if (Math.abs(pitchStep) > maxPitchSpeed) {
                pitchStep = Math.signum(pitchStep) * maxPitchSpeed;
            }
            newPitch = currentPitch + pitchStep;
            newPitch = Math.max(-90.0f, Math.min(90.0f, newPitch));
        }

        Rotation corrected = RotationMath.correctRotation(new Rotation(newYaw, newPitch));
        AimUtils.mc.player.setYRot(corrected.getYRot());
        AimUtils.mc.player.yRotO = corrected.getYRot();
        if (this.pitchAim.isEnabled()) {
            AimUtils.mc.player.setXRot(corrected.getXRot());
            AimUtils.mc.player.xRotO = corrected.getXRot();
        }
    };

    @Override
    public void onEnable() {
        this.lastTime = System.currentTimeMillis();
        super.onEnable();
    }

    private LivingEntity getBestTarget() {
        LivingEntity best = null;
        double minFov = Double.MAX_VALUE;

        for (Entity entity : AimUtils.mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity) || entity == AimUtils.mc.player) {
                continue;
            }
            LivingEntity target = (LivingEntity) entity;
            if (!target.isAlive() || target.isDeadOrDying() || target instanceof ArmorStand) {
                continue;
            }

            // Target type filtering
            if (target.isInvisible() && !this.invisibles.isSelected()) {
                continue;
            }

            if (target instanceof Player) {
                boolean hasArmor = false;
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (slot.isArmor()) {
                        ItemStack armorStack = target.getItemBySlot(slot);
                        if (armorStack != null && !armorStack.isEmpty()) {
                            hasArmor = true;
                            break;
                        }
                    }
                }
                if (hasArmor) {
                    if (!this.playersArmored.isSelected()) {
                        continue;
                    }
                } else {
                    if (!this.playersNaked.isSelected()) {
                        continue;
                    }
                }
            } else if (target instanceof Animal) {
                if (!this.animals.isSelected()) {
                    continue;
                }
            } else if (target instanceof Mob) {
                if (!this.mobs.isSelected()) {
                    continue;
                }
            } else {
                if (!this.mobs.isSelected()) {
                    continue;
                }
            }

            double dist = AimUtils.mc.player.distanceTo(target);
            if (dist > this.range.getCurrentValue()) {
                continue;
            }

            if (Rockstar.getInstance().getFriendManager().isFriend(target.getName().getString())) {
                continue;
            }

            Vec3 targetPoint = RotationMath.getNearestPoint(target);
            Rotation targetRot = RotationMath.getRotationTo(targetPoint);

            float yawDiff = RotationMath.getAngleDifference(AimUtils.mc.player.getYRot(), targetRot.getYRot());
            float pitchDiff = targetRot.getXRot() - AimUtils.mc.player.getXRot();
            double fovDiff = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

            if (fovDiff <= this.fov.getCurrentValue() && fovDiff < minFov) {
                if (AimUtils.mc.player.hasLineOfSight(target)) {
                    minFov = fovDiff;
                    best = target;
                }
            }
        }
        return best;
    }
}
