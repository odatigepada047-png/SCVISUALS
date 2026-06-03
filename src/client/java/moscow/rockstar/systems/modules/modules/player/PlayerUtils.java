/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.gui.screens.DeathScreen
 *  net.minecraft.entity.player.Player
 *  net.minecraft.entity.projectile.FishingHook
 *  net.minecraft.item.FishingRodItem
 *  net.minecraft.util.InteractionHand
 */
package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.InternalAttackEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.mixin.minecraft.client.IMinecraftClient;
import moscow.rockstar.utility.interfaces.ILivingEntity;


@ModuleInfo(name="Player Utils", category=ModuleCategory.PLAYER, desc="\u0423\u0442\u0438\u043b\u0438\u0442\u044b \u0434\u043b\u044f \u0438\u0433\u0440\u043e\u043a\u0430")
public class PlayerUtils
extends BaseModule {
    private final BooleanSetting antiAfk = new BooleanSetting((SettingsContainer)this, "Anti AFK", "\u041d\u0435 \u043f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0441\u0435\u0440\u0432\u0435\u0440\u0443 \u043a\u0438\u043a\u043d\u0443\u0442\u044c \u0432\u0430\u0441, \u043f\u043e\u043a\u0430 \u0432\u044b AFK");
    private final BooleanSetting autoRespawn = new BooleanSetting((SettingsContainer)this, "Auto Respawn", "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0432\u043e\u0437\u0440\u043e\u0436\u0434\u0430\u0435\u0442 \u043f\u0440\u0438 \u0441\u043c\u0435\u0440\u0442\u0438");
    private final BooleanSetting autoFish = new BooleanSetting((SettingsContainer)this, "Auto Fish", "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043b\u043e\u0432\u0438\u0442 \u0440\u044b\u0431\u0443");
    private final BooleanSetting noDelay = new BooleanSetting((SettingsContainer)this, "No Delay", "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0443 \u043f\u0440\u0438 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0438 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d\u043d\u044b\u0445 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0439", () -> Rockstar.getInstance().getHwid().equals("CA97CD48-6342-5915-A7C0-047C1618D58A"));
    private final SelectSetting noDelayModes = new SelectSetting((SettingsContainer)this, "\u0420\u0435\u0436\u0438\u043c\u044b", () -> !this.noDelay.isEnabled());
    private final SelectSetting.Value blockPlacement = new SelectSetting.Value(this.noDelayModes, "\u041f\u043e\u0441\u0442\u0430\u0432\u043a\u0430 \u0431\u043b\u043e\u043a\u043e\u0432");
    private final SelectSetting.Value xpBottle = new SelectSetting.Value(this.noDelayModes, "\u041f\u0443\u0437\u044b\u0440\u044c\u043a\u0438 \u043e\u043f\u044b\u0442\u0430");
    private final SelectSetting.Value jumps = new SelectSetting.Value(this.noDelayModes, "\u041f\u0440\u044b\u0436\u043a\u0438");
    private final BooleanSetting noFriendDamage = new BooleanSetting((SettingsContainer)this, "\u041d\u0435 \u0431\u0438\u0442\u044c \u0434\u0440\u0443\u0437\u0435\u0439", "\u041d\u0435 \u043f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0431\u0438\u0442\u044c \u0434\u0440\u0443\u0437\u0435\u0439");
    private final ModeSetting antiAFKMode = new ModeSetting((SettingsContainer)this, "\u0420\u0435\u0436\u0438\u043c\u044b", () -> !this.antiAfk.isEnabled());
    private final ModeSetting.Value chat = new ModeSetting.Value(this.antiAFKMode, "\u041f\u0438\u0441\u0430\u0442\u044c \u0432 \u0447\u0430\u0442");
    private final ModeSetting.Value jump = new ModeSetting.Value(this.antiAFKMode, "\u041f\u0440\u044b\u0433\u0430\u0442\u044c");
    private final ModeSetting.Value swing = new ModeSetting.Value(this.antiAFKMode, "\u0412\u0437\u043c\u0430\u0445 \u0440\u0443\u043a\u043e\u0439");
    private final SliderSetting delay = new SliderSetting(this, "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430", "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u0434\u043b\u044f \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0439", () -> !this.antiAfk.isEnabled()).min(5.0f).max(60.0f).step(5.0f).currentValue(50.0f);
    private final Timer timerAFK = new Timer();
    private final Timer fishTimer = new Timer();
    private boolean hookFlag;
    private boolean thrown;
    private boolean activeAFK;
    private final EventListener<InternalAttackEvent> onAttackEvent = event -> {
        if (this.noFriendDamage.isEnabled() && event.getEntity() instanceof Player && Rockstar.getInstance().getFriendManager().isFriend(event.getEntity().getName().getString())) {
            event.cancel();
        }
    };

    @Override
    public void tick() {
        if (this.antiAfk.isEnabled()) {
            if (this.timerAFK.finished(10000L)) {
                this.activeAFK = true;
            }
            if (EntityUtility.isPlayerMoving()) {
                this.activeAFK = false;
                this.timerAFK.reset();
            }
            if (this.activeAFK && (float)PlayerUtils.mc.player.tickCount % this.delay.getCurrentValue() == 5.0f) {
                if (this.chat.isSelected()) {
                    PlayerUtils.mc.player.connection.sendChat("\u0412\u0441\u0435\u043c \u043f\u0440\u0438\u0432\u0435\u0442 " + Math.random() + " !");
                } else if (this.jump.isSelected() && PlayerUtils.mc.player.onGround()) {
                    PlayerUtils.mc.player.jumpFromGround();
                } else if (this.swing.isSelected()) {
                    PlayerUtils.mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
        if (this.autoRespawn.isEnabled() && PlayerUtils.mc.screen instanceof DeathScreen && PlayerUtils.mc.player != null) {
            PlayerUtils.mc.player.respawn();
            mc.setScreen(null);
        }
        if (this.autoFish.isEnabled() && PlayerUtils.mc.player.getMainHandItem().getItem() instanceof FishingRodItem) {
            if (PlayerUtils.mc.player.fishing != null) {
                this.thrown = true;
                this.fishTimer.reset();
                if (!this.hookFlag && PlayerUtils.mc.player.fishing.getHookedIn() != null) {
                    this.throwRod();
                    this.hookFlag = true;
                    this.fishTimer.reset();
                }
            } else if (this.hookFlag && this.fishTimer.finished(600L)) {
                this.throwRod();
                this.hookFlag = false;
                this.thrown = false;
                this.fishTimer.reset();
            } else if (!this.hookFlag && this.thrown && this.fishTimer.finished(3000L)) {
                this.throwRod();
                this.thrown = false;
                this.fishTimer.reset();
            }
        }
        if (this.noDelay.isEnabled()) {
            if (this.blockPlacement.isSelected() && PlayerUtils.mc.options.keyUse.isDown() && PlayerUtils.mc.player.getMainHandItem().getItem() instanceof BlockItem) {
                ((IMinecraftClient)mc).setUseCooldown(0);
            }
            if (this.xpBottle.isSelected() && PlayerUtils.mc.options.keyUse.isDown() && PlayerUtils.mc.player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)) {
                ((IMinecraftClient)mc).setUseCooldown(0);
            }
            if (this.jumps.isSelected() && PlayerUtils.mc.options.keyJump.isDown()) {
                ((ILivingEntity)PlayerUtils.mc.player).setJumpDelayTimer(0);
            }
        }
        super.tick();
    }

    private void throwRod() {
        PlayerUtils.mc.gameMode.useItem((Player)PlayerUtils.mc.player, InteractionHand.MAIN_HAND);
        PlayerUtils.mc.player.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void onDisable() {
        this.hookFlag = false;
    }
}

