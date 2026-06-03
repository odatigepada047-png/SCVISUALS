/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.Player
 *  net.minecraft.screen.ChestMenu
 *  net.minecraft.screen.slot.ContainerInput
 */
package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;

@ModuleInfo(name="Auto Resell", category=ModuleCategory.OTHER, desc="modules.descriptions.auto_resell")
public class AutoResell
extends BaseModule {
    private final Timer openTimer = new Timer();
    private final Timer clickTimer = new Timer();
    private boolean isAutoProcess;
    private boolean auctionHandled;
    private boolean storageHandled;
    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (AutoResell.mc.player == null || AutoResell.mc.gameMode == null || !ServerUtility.isFT()) {
            return;
        }
        if (this.openTimer.finished(60000L)) {
            if (!this.isAuctionOrStorageOpen()) {
                AutoResell.mc.player.connection.sendCommand("ah " + AutoResell.mc.player.getName().getString());
                this.isAutoProcess = true;
                this.clickTimer.reset();
            }
            this.openTimer.reset();
        }
        this.handleAuctionAndStorage();
    };

    private void handleAuctionAndStorage() {
        boolean isAuctionOpen = this.isTitleContains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d\u044b");
        boolean isStorageOpen = this.isTitleContains("\u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435");
        if (this.isAutoProcess && isAuctionOpen && !this.auctionHandled && this.clickTimer.finished(300L)) {
            AutoResell.mc.gameMode.handleContainerInput(AutoResell.mc.player.containerMenu.containerId, 46, 0, ContainerInput.PICKUP, (Player)AutoResell.mc.player);
            this.auctionHandled = true;
            this.clickTimer.reset();
        }
        if (this.isAutoProcess && isStorageOpen && !this.storageHandled && this.clickTimer.finished(300L)) {
            AutoResell.mc.gameMode.handleContainerInput(AutoResell.mc.player.containerMenu.containerId, 52, 0, ContainerInput.PICKUP, (Player)AutoResell.mc.player);
            AutoResell.mc.player.closeContainer();
            this.storageHandled = true;
            this.isAutoProcess = false;
            this.clickTimer.reset();
        }
        if (!isAuctionOpen) {
            this.auctionHandled = false;
        }
        if (!isStorageOpen) {
            this.storageHandled = false;
        }
    }

    private boolean isAuctionOrStorageOpen() {
        return this.isTitleContains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d\u044b") || this.isTitleContains("\u0445\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435");
    }

    private boolean isTitleContains(String string) {
        if (AutoResell.mc.screen == null || AutoResell.mc.player == null) {
            return false;
        }
        String title = AutoResell.mc.screen.getTitle().getString().toLowerCase();
        return AutoResell.mc.player.containerMenu instanceof ChestMenu && title.contains(string.toLowerCase());
    }
}

