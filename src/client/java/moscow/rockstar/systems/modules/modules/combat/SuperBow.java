package moscow.rockstar.systems.modules.modules.combat;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.item.BowItem;

@ModuleInfo(name = "SuperBow", category = ModuleCategory.COMBAT)
public class SuperBow extends BaseModule {

    private final ModeSetting mode = new ModeSetting(this, this.getSettingName("mode"));
    private final ModeSetting.Value release = new ModeSetting.Value(this.mode, this.getSettingName("release"));
    private final ModeSetting.Value spam    = new ModeSetting.Value(this.mode, this.getSettingName("spam"));
    private final ModeSetting.Value instant = new ModeSetting.Value(this.mode, this.getSettingName("instant"));

    private final SliderSetting packets = new SliderSetting(this, this.getSettingName("packets"))
            .step(1.0f).min(1.0f).max(20.0f).currentValue(5.0f);

    private boolean wasUsing = false;

    private final EventListener<SendPacketEvent> onSendPacket = event -> {
        if (!this.mode.is(this.release)) return;
        if (SuperBow.mc.player == null) return;

        Packet<?> p = event.getPacket();
        if (!(p instanceof ServerboundPlayerActionPacket action)) return;
        if (action.getAction() != ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) return;

        double x = SuperBow.mc.player.getX();
        double y = SuperBow.mc.player.getY();
        double z = SuperBow.mc.player.getZ();
        float yaw = SuperBow.mc.player.getYRot();
        float pitch = SuperBow.mc.player.getXRot();
        boolean onGround = SuperBow.mc.player.onGround();
        boolean horizontal = SuperBow.mc.player.horizontalCollision;

        SuperBow.mc.player.connection.send((Packet) new ServerboundMovePlayerPacket.PosRot(
                x, y + 10.0, z, yaw, pitch, onGround, horizontal));
        SuperBow.mc.player.connection.send((Packet) new ServerboundMovePlayerPacket.PosRot(
                x, y - 10.0, z, yaw, pitch, onGround, horizontal));
    };

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (SuperBow.mc.player == null || SuperBow.mc.level == null) return;
        if (!(SuperBow.mc.player.getUseItem().getItem() instanceof BowItem)) {
            this.wasUsing = false;
            return;
        }
        if (!SuperBow.mc.player.isUsingItem()) {
            this.wasUsing = false;
            return;
        }

        double x = SuperBow.mc.player.getX();
        double y = SuperBow.mc.player.getY();
        double z = SuperBow.mc.player.getZ();

        if (this.mode.is(this.spam)) {
            int count = (int) this.packets.getCurrentValue();
            for (int i = 0; i < count; i++) {
                SuperBow.mc.player.connection.send((Packet) new ServerboundMovePlayerPacket.Pos(
                        x, y + 0.000000001, z, true, false));
                SuperBow.mc.player.connection.send((Packet) new ServerboundMovePlayerPacket.Pos(
                        x, y + 0.000000001, z, false, false));
            }
            return;
        }

        if (this.mode.is(this.instant)) {
            int burst = this.wasUsing ? 8 : 30;
            for (int i = 0; i < burst; i++) {
                SuperBow.mc.player.connection.send((Packet) new ServerboundMovePlayerPacket.Pos(
                        x, y + 0.000000001, z, true, false));
                SuperBow.mc.player.connection.send((Packet) new ServerboundMovePlayerPacket.Pos(
                        x, y + 0.000000001, z, false, false));
            }
            this.wasUsing = true;
        }
    };
}
