package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.WorldChangeEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.ContainerInput;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

@ModuleInfo(name = "Test", category = ModuleCategory.OTHER, desc = "All-in-One Dupe Tester (1.21.4)")
public class TestModule extends BaseModule {
    private final ModeSetting mode = new ModeSetting(this, "Mode");
    private final ModeSetting.Value burstDupe = new ModeSetting.Value(mode, "Burst Dupe");
    private final ModeSetting.Value cursorDesync = new ModeSetting.Value(mode, "Cursor Desync");
    private final ModeSetting.Value crafterGlitch = new ModeSetting.Value(mode, "Crafter Glitch");
    private final ModeSetting.Value shulkerForced = new ModeSetting.Value(mode, "Shulker Forced");
    private final ModeSetting.Value dimChangeLog = new ModeSetting.Value(mode, "Dimension Log");
    
    private final BooleanSetting autoSuicide = new BooleanSetting(this, "Auto Suicide").enable();
    private final BindSetting runBind = new BindSetting(this, "Run Exploit");
    
    private ResourceKey<Level> lastDimension = null;

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (lastDimension == null) {
            lastDimension = mc.level.dimension();
            return;
        }
        if (lastDimension != mc.level.dimension()) {
            String oldDim = lastDimension.identifier().toString();
            String newDim = mc.level.dimension().identifier().toString();
            lastDimension = mc.level.dimension();
            
            if (mode.is(dimChangeLog)) {
                MessageUtility.info(Component.literal("Dimension changed from " + oldDim + " to " + newDim));
                if (autoSuicide.isEnabled()) {
                    mc.player.connection.sendCommand("suicide");
                }
            }
        }
    };

    private final EventListener<WorldChangeEvent> onWorldChange = event -> {
        lastDimension = null;
    };

    private final EventListener<KeyPressEvent> onKeyPress = event -> {
        if (runBind.isKey(event.getKey()) && event.getAction() == 1) {
            runExploit();
        }
    };

    private void runExploit() {
        if (mc.player == null || mc.player.containerMenu == null) return;

        if (mode.is(burstDupe)) {
            handleBurst();
        } else if (mode.is(cursorDesync)) {
            handleDesync();
        } else if (mode.is(crafterGlitch)) {
            handleCrafter();
        } else if (mode.is(shulkerForced)) {
            handleShulker();
        }
    }

    private void handleBurst() {
        int bundle = InventoryUtility.findItemInContainer(Items.BUNDLE);
        int item = InventoryUtility.findItemInContainer(s -> !s.isEmpty() && s.getItem() != Items.BUNDLE);
        if (bundle != -1 && item != -1) {
            int sid = mc.player.containerMenu.containerId;
            mc.gameMode.handleContainerInput(sid, bundle, 0, ContainerInput.PICKUP, mc.player);
            mc.gameMode.handleContainerInput(sid, item, 1, ContainerInput.PICKUP, mc.player);
            mc.gameMode.handleContainerInput(sid, -999, 0, ContainerInput.PICKUP, mc.player);
            if (autoSuicide.isEnabled()) mc.player.connection.sendCommand("suicide");
            mc.player.connection.send(new ServerboundContainerClosePacket(sid));
        }
    }

    private void handleDesync() {
        int bundle = InventoryUtility.findItemInContainer(Items.BUNDLE);
        int item = InventoryUtility.findItemInContainer(s -> !s.isEmpty() && s.getItem() != Items.BUNDLE);
        if (bundle != -1 && item != -1) {
            int sid = mc.player.containerMenu.containerId;
            mc.gameMode.handleContainerInput(sid, bundle, 0, ContainerInput.PICKUP, mc.player);
            mc.gameMode.handleContainerInput(sid, item, 1, ContainerInput.PICKUP, mc.player);
            mc.player.connection.send(new ServerboundContainerClosePacket(sid));
            if (autoSuicide.isEnabled()) mc.player.connection.sendCommand("suicide");
        }
    }

    private void handleCrafter() {
        int bundle = InventoryUtility.findItemInContainer(Items.BUNDLE);
        if (bundle != -1) {
            int sid = mc.player.containerMenu.containerId;
            mc.gameMode.handleContainerInput(sid, 0, 0, ContainerInput.PICKUP, mc.player);
            mc.gameMode.handleContainerInput(sid, bundle, 0, ContainerInput.PICKUP, mc.player);
            mc.gameMode.handleContainerInput(sid, 0, 0, ContainerInput.PICKUP, mc.player);
        }
    }

    private void handleShulker() {
        int bundle = InventoryUtility.findItemInContainer(Items.BUNDLE);
        int shulker = InventoryUtility.findItemInContainer(s -> s.getItem().toString().contains("shulker_box"));
        if (bundle != -1 && shulker != -1) {
            int sid = mc.player.containerMenu.containerId;
            mc.gameMode.handleContainerInput(sid, bundle, 0, ContainerInput.PICKUP, mc.player);
            mc.gameMode.handleContainerInput(sid, shulker, 1, ContainerInput.PICKUP, mc.player);
            mc.gameMode.handleContainerInput(sid, bundle, 0, ContainerInput.PICKUP, mc.player);
        }
    }
}
