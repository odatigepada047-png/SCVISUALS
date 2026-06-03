/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl$PackConfirmScreen
 *  net.minecraft.entity.player.Player
 *  net.minecraft.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.common.ServerboundResourcePackPacket
 *  net.minecraft.network.protocol.common.ServerboundResourcePackPacket$Action
 *  net.minecraft.network.packet.s2c.play.ClientboundSystemChatPacket
 *  net.minecraft.network.packet.s2c.play.ClientboundOpenScreenPacket
 *  net.minecraft.screen.ChestMenu
 *  net.minecraft.screen.slot.ContainerInput
 *  net.minecraft.util.InteractionHand
 */
package moscow.rockstar.systems.modules.modules.other;

import java.util.function.BooleanSupplier;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventIntegration;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.network.SendPacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.player.InputEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.utility.game.EntityUtility;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.time.Timer;
import moscow.rockstar.utility.sounds.ClientSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.InteractionHand;

@ModuleInfo(name = "Assist", category = ModuleCategory.OTHER, desc = "\u041f\u043e\u043c\u043e\u0449\u043d\u0438\u043a \u0434\u043b\u044f \u0440\u0430\u0437\u043d\u044b\u0445 \u0441\u0435\u0440\u0432\u0435\u0440\u043e\u0432")
public class Assist
        extends BaseModule {
    private final BooleanSupplier rwCondition = () -> ServerUtility.isHW() || ServerUtility.isPastaFT()
            || ServerUtility.isCM() || ServerUtility.isSaturn() || ServerUtility.isIntave();
    private final BooleanSupplier ftCondition = () -> ServerUtility.isHW() || ServerUtility.isRW()
            || ServerUtility.isCM() || ServerUtility.isSaturn() || ServerUtility.isIntave();
    private final BooleanSupplier hwCondition = () -> ServerUtility.isPastaFT() || ServerUtility.isRW()
            || ServerUtility.isCM() || ServerUtility.isSaturn() || ServerUtility.isIntave();
    private final BooleanSetting spoof = new BooleanSetting(this, "\u0421\u043f\u0443\u0444 \u0440\u043f",
            "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0437\u0430\u0439\u0442\u0438 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440 \u0431\u0435\u0437 \u0441\u043a\u0430\u0447\u0438\u0432\u0430\u043d\u0438\u044f \u0440\u0435\u0441\u0443\u0440\u0441 \u043f\u0430\u043a\u0430",
            this.rwCondition);
    private final BooleanSetting closeMenu = new BooleanSetting(this,
            "\u0417\u0430\u043a\u0440\u044b\u0432\u0430\u0442\u044c \u043c\u0435\u043d\u044e",
            "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0437\u0430\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u043c\u0435\u043d\u044e \u043f\u0440\u0438 \u0437\u0430\u0445\u043e\u0434\u0435 \u043d\u0430 \u0433\u0440\u0438\u0444",
            this.rwCondition).enable();
    private final BooleanSetting autoFix = new BooleanSetting((SettingsContainer) this,
            "\u0410\u0432\u0442\u043e\u043f\u043e\u0447\u0438\u043d\u043a\u0430", this.rwCondition);
    private final BooleanSetting warnArmor = new BooleanSetting((SettingsContainer) this,
            "\u041f\u043e\u043b\u043e\u043c\u043a\u0430 \u0431\u0440\u043e\u043d\u0438",
            "\u041f\u0440\u0435\u0434\u0443\u043f\u0440\u0435\u0436\u0434\u0430\u0435\u0442 \u0435\u0441\u043b\u0438 \u0431\u0440\u043e\u043d\u044f \u043f\u043e\u043b\u043e\u043c\u0430\u043d\u0430");

    private final BooleanSetting autoPiona = new BooleanSetting(this,
            "\u0410\u0432\u0442\u043e \u043f\u0438\u043e\u043d\u0430",
            "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043f\u0440\u043e\u043f\u0438\u0441\u044b\u0432\u0430\u0435\u0442 /piona \u043f\u0440\u0438 \u0437\u0430\u0445\u043e\u0434\u0435 \u043d\u0430 Funtime",
            ServerUtility::isRW).enable();
    private final BindSetting dezorentKey = new BindSetting(this,
            "\u0414\u0435\u0437\u043e\u0440\u0438\u0435\u043d\u0442\u0430\u0446\u0438\u044f", this.ftCondition);
    private final BindSetting trapkaKey = new BindSetting(this, "\u0422\u0440\u0430\u043f\u043a\u0430",
            this.ftCondition);
    private final BindSetting smerchKey = new BindSetting(this,
            "\u041e\u0433\u043d\u0435\u043d\u043d\u044b\u0439 \u0441\u043c\u0435\u0440\u0447", this.ftCondition);
    private final BindSetting plastKey = new BindSetting(this, "\u041f\u043b\u0430\u0441\u0442", this.ftCondition);
    private final BindSetting auraKey = new BindSetting(this, "\u0411\u043e\u0436\u044c\u044f \u0430\u0443\u0440\u0430",
            this.ftCondition);
    private final BindSetting pilbKey = new BindSetting(this, "\u042f\u0432\u043d\u0430\u044f \u043f\u044b\u043b\u044c",
            this.ftCondition);
    private final BooleanSetting autoZako = new BooleanSetting((SettingsContainer) this,
            "\u0410\u0432\u0442\u043e /zako", this.hwCondition);
    private final BooleanSetting autoSell = new BooleanSetting((SettingsContainer) this, "AutoSell", "Автоматически подтверждает продажу", this.hwCondition);
    private final BooleanSetting autoStop = new BooleanSetting((SettingsContainer) this,
            "\u0410\u0432\u0442\u043e-\u0441\u0442\u043e\u043f", this.hwCondition);
    private final BindSetting stanKey = new BindSetting(this, "\u0421\u0442\u0430\u043d", this.hwCondition);
    private final BindSetting snowKey = new BindSetting(this, "\u041a\u043e\u043c \u0441\u043d\u0435\u0433\u0430",
            this.hwCondition);
    private final BindSetting bombKey = new BindSetting(this,
            "\u0412\u0437\u0440\u044b\u0432\u043d\u0430\u044f \u0448\u0442\u0443\u0447\u043a\u0430", this.hwCondition);
    private final BindSetting hwTrapKey = new BindSetting(this, "\u0422\u0440\u0430\u043f\u043a\u0430",
            this.hwCondition);
    private final BindSetting boomTrapKey = new BindSetting(this,
            "\u0412\u0437\u0440\u044b\u0432\u043d\u0430\u044f \u0442\u0440\u0430\u043f\u043a\u0430", this.hwCondition);
    private final BindSetting goolKey = new BindSetting(this,
            "\u041f\u0440\u043e\u0449\u0430\u043b\u044c\u043d\u044b\u0439 \u0433\u0443\u043b", this.hwCondition);
    private final BindSetting backpackKey = new BindSetting(this, "\u0420\u044e\u043a\u0437\u0430\u043a",
            this.hwCondition);
    private final BooleanSetting autoWaypoint = new BooleanSetting((SettingsContainer) this,
            "\u0410\u0432\u0442\u043e Waypoint",
            "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0441\u043e\u0437\u0434\u0430\u0435\u0442 waypoint \u0434\u043b\u044f \u0438\u0432\u0435\u043d\u0442\u043e\u0432");
    private final Timer timer = new Timer();
    private final Timer timerStop = new Timer();
    private boolean stopHandle;
    private boolean zakoCommandSent = true;
    private boolean pionaCommandSent = true;
    private boolean visible;
    private final EventListener<MouseEvent> onMouseEvent = event -> this.handleButtonPress(event.getButton());
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (event.getAction() != 1) {
            return;
        }
        this.handleButtonPress(event.getKey());
    };
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
        String title;
        ClientboundSystemChatPacket packet;
        ClientboundOpenScreenPacket screenPacket;
        String message;
        Packet<?> patt0$temp;
        if (this.autoPiona.isEnabled() && (patt0$temp = event.getPacket()) instanceof ClientboundSystemChatPacket
                && ((message = (packet = (ClientboundSystemChatPacket) patt0$temp).content().getString().toLowerCase())
                        .contains(
                                "10,000 \u0431\u044b\u043b\u043e \u043d\u0430\u0447\u0438\u0441\u043b\u0435\u043d\u043e \u0432\u0430\u043c")
                        || message.contains(
                                "\u043f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u0435 \u0442\u0435\u043a\u0441\u0442 \u0435\u0449\u0435 \u0440\u0430\u0437"))) {
            this.pionaCommandSent = false;
            this.timer.reset();
        }
        if (this.autoZako.isEnabled() && (patt0$temp = event.getPacket()) instanceof ClientboundSystemChatPacket) {
            packet = (ClientboundSystemChatPacket) patt0$temp;
            message = packet.content().getString();
            if (message.contains(
                    "\u0412\u044b \u0443\u0436\u0435 \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u043b\u0438 \u044d\u0442\u043e\u0442 \u043f\u0440\u043e\u043c\u043e\u043a\u043e\u0434")) {
                this.zakoCommandSent = false;
                this.timer.reset();
            } else if (message.contains(
                    "\u041f\u0440\u044f\u043c\u043e \u0441\u0435\u0439\u0447\u0430\u0441 \u0438\u0434\u0435\u0442 \u043d\u0430\u0431\u043e\u0440")) {
                this.zakoCommandSent = true;
            }
        }
        if (this.autoStop.isEnabled() && (patt0$temp = event.getPacket()) instanceof ClientboundSystemChatPacket
                && (message = (packet = (ClientboundSystemChatPacket) patt0$temp).content().getString()).contains(
                        "\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u043f\u0440\u0438\u043d\u044f\u0442\u0430")) {
            this.stopHandle = true;
        }
        if (this.autoWaypoint.isEnabled() && (patt0$temp = event.getPacket()) instanceof ClientboundSystemChatPacket) {
            packet = (ClientboundSystemChatPacket) patt0$temp;
            message = packet.content().getString();
            this.parseEventMessage(message);
        }
        if (this.autoSell.isEnabled() && (patt0$temp = event.getPacket()) instanceof ClientboundSystemChatPacket) {
            packet = (ClientboundSystemChatPacket) patt0$temp;
            message = packet.content().getString();
            if (message.contains("Введите /ah sell auto confirm")) {
                Assist.mc.player.connection.sendCommand("ah sell auto confirm");
            }
        }
        if (this.closeMenu.isEnabled() && (patt0$temp = event.getPacket()) instanceof ClientboundOpenScreenPacket
                && ((title = (screenPacket = (ClientboundOpenScreenPacket) patt0$temp).getTitle().getString()).contains(
                        "\u041c\u0435\u043d\u044e") || title.contains("\ua201\ua000\ua202\ua301\ua202\ua001"))) {
            Assist.mc.player.closeContainer();
            event.cancel();
        }
    };

    private final EventListener<InputEvent> inputEvent = event -> {
        if (this.autoStop.isEnabled() && this.stopHandle && !this.timerStop.finished(3200L)) {
            event.setForward(0.0f);
            event.setJump(false);
            event.setStrafe(0.0f);
            this.timerStop.reset();
        }
    };
    private Timer shootTimer = new Timer();
    private Timer chargeTimer = new Timer();
    private ItemSlot previousSlot = null;
    private boolean isCharging = false;
    private boolean needsSlotSwapBack = false;
    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
    };

    @Override
    public void tick() {
        float currentDamage;
        float maxDamage;
        if (Assist.mc.player == null || Assist.mc.level == null || Assist.mc.gameMode == null) {
            return;
        }
        if (this.warnArmor.isEnabled()) {
            float armorPoint = 1.0f;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!slot.isArmor()) continue;
                ItemStack stack = Assist.mc.player.getItemBySlot(slot);
                if (stack.isEmpty())
                    continue;
                maxDamage = stack.getMaxDamage();
                currentDamage = maxDamage - (float) stack.getDamageValue();
                armorPoint = currentDamage / maxDamage;
            }
            if ((double) armorPoint < 0.36) {
                if (this.visible) {
                    Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.INFO,
                            "\u041f\u043e\u043b\u043e\u043c\u043a\u0430",
                            "\u0412\u0430\u0448\u0430 \u0431\u0440\u043e\u043d\u044f \u043d\u0430 \u0433\u0440\u0430\u043d\u0438 \u043f\u043e\u043b\u043e\u043c\u043a\u0438");
                    this.visible = false;
                }
            } else {
                this.visible = true;
            }
        }

        if (this.autoFix.isEnabled() && !ServerUtility.hasCT) {
            Inventory inventory = Assist.mc.player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); ++i) {
                ItemStack stack;
                stack = inventory.getItem(i);
                if (stack.isEmpty() || !stack.isDamageableItem()
                        || !((currentDamage = (maxDamage = (float) stack.getMaxDamage()) - (float) stack.getDamageValue())
                                / maxDamage > 0.5f)
                        || Assist.mc.player.tickCount % 25 != 0)
                    continue;
                Assist.mc.player.connection.sendCommand("fix all");
                break;
            }
        }
        if (this.spoof.isEnabled() && this.spoof.isVisible() && Assist.mc.player.tickCount > 20
                && Assist.mc.screen != null && Assist.mc.screen.getClass().getSimpleName().equals("PackConfirmScreen")) {
            Assist.mc.player.connection
                    .send((Packet) new ServerboundResourcePackPacket(Assist.mc.player.getUUID(),
                            ServerboundResourcePackPacket.Action.ACCEPTED));
            Assist.mc.player.connection
                    .send((Packet) new ServerboundResourcePackPacket(Assist.mc.player.getUUID(),
                            ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
            Assist.mc.player.closeContainer();
        }
        if (this.autoPiona.isEnabled()) {
            if (Assist.mc.player.containerMenu instanceof ChestMenu
                    && Assist.mc.screen.getTitle().getString()
                            .contains("\u0412\u0430\u043c \u043f\u043e\u0434\u0430\u0440\u043e\u043a")) {
                Assist.mc.gameMode.handleContainerInput(Assist.mc.player.containerMenu.containerId, 13, 0,
                        ContainerInput.PICKUP, (Player) Assist.mc.player);
            }
            if (this.timer.finished(1000L) && !this.pionaCommandSent) {
                this.pionaCommandSent = true;
                Assist.mc.player.connection.sendCommand("piona");
            }
        }
        if (this.autoZako.isEnabled() && this.timer.finished(500L) && this.zakoCommandSent) {
            this.zakoCommandSent = false;
            Assist.mc.player.connection.sendCommand("zako");
        }
        super.tick();
    }

    private void handleButtonPress(int button) {
        if (this.dezorentKey.isKey(button)) {
            useItemIfInHotbar(Items.ENDER_EYE);
        } else if (this.trapkaKey.isKey(button)) {
            useItemIfInHotbar(Items.NETHERITE_SCRAP);
        } else if (this.smerchKey.isKey(button)) {
            useItemIfInHotbar(Items.FIRE_CHARGE);
        } else if (this.stanKey.isKey(button)) {
            useItemIfInHotbar(Items.NETHER_STAR);
        } else if (this.plastKey.isKey(button)) {
            useItemIfInHotbar(Items.DRIED_KELP);
        } else if (this.auraKey.isKey(button)) {
            useItemIfInHotbar(Items.PHANTOM_MEMBRANE);
        } else if (this.pilbKey.isKey(button)) {
            useItemIfInHotbar(Items.SUGAR);
        } else if (this.snowKey.isKey(button)) {
            useItemIfInHotbar(Items.SNOWBALL);
        } else if (this.bombKey.isKey(button)) {
            useItemIfInHotbar(Items.FIRE_CHARGE);
        } else if (this.hwTrapKey.isKey(button)) {
            useItemIfInHotbar(Items.POPPED_CHORUS_FRUIT);
        } else if (this.boomTrapKey.isKey(button)) {
            useItemIfInHotbar(Items.PRISMARINE_SHARD);
        } else if (this.goolKey.isKey(button)) {
            useItemIfInHotbar(Items.FIREWORK_STAR);
        } else if (this.backpackKey.isKey(button)) {
            useItemIfInHotbar(Items.MAGENTA_SHULKER_BOX);
        }
    }

    private void useItemIfInHotbar(net.minecraft.world.item.Item item) {
        if (SlotGroups.hotbar().findItem(item) != null) {
            EventIntegration.SWAP_INTEGRATION.useItem(item);
        }
    }

    private void attemptShoot() {
        ItemStack crossbowStack;
        SlotGroup<ItemSlot> group = SlotGroups.hotbar().and(SlotGroups.inventory());
        ItemSlot crossbowSlot = group.findItem(Items.CROSSBOW);
        if (crossbowSlot == null) {
            return;
        }
        if (this.previousSlot == null) {
            this.previousSlot = InventoryUtility.getCurrentHotbarSlot();
        }
        if (crossbowSlot instanceof HotbarSlot) {
            HotbarSlot hotbarSlot = (HotbarSlot) crossbowSlot;
            if (InventoryUtility.getCurrentHotbarSlot().item() != Items.CROSSBOW) {
                InventoryUtility.selectHotbarSlot(hotbarSlot);
            }
        }
        if (!this.isCrossbowCharged(crossbowStack = InventoryUtility.getCurrentHotbarSlot().itemStack())) {
            this.startCharging();
            return;
        }
        this.shoot();
        this.startCharging();
    }

    private boolean isCrossbowCharged(ItemStack crossbow) {
        if (crossbow.isEmpty() || crossbow.getItem() != Items.CROSSBOW) {
            return false;
        }
        CompoundTag nbt = ItemUtility.getNBT(crossbow);
        return nbt != null && nbt.getBoolean("Charged").orElse(false);
    }

    private void startCharging() {
        if (this.isCharging) {
            return;
        }
        this.isCharging = true;
        this.chargeTimer.reset();
        Minecraft.getInstance().options.keyUse.setDown(true);
    }

    private void finishCharging() {
        if (!this.isCharging) {
            return;
        }
        this.isCharging = false;
        Minecraft.getInstance().options.keyUse.setDown(false);
        this.needsSlotSwapBack = true;
    }

    private void shoot() {
        if (Assist.mc.gameMode != null) {
            Assist.mc.gameMode.useItem((Player) Assist.mc.player, InteractionHand.MAIN_HAND);
        }
    }

    private void parseEventMessage(String message) {


        String lowerMessage = message.toLowerCase();
        boolean isAirdrop = lowerMessage.contains("\u0430\u0438\u0440\u0434\u0440\u043e\u043f"); // аирдроп
        boolean isAltar = lowerMessage
                .contains("\u0430\u043b\u044c\u0442\u0430\u0440\u044c \u043d\u0435\u0436\u0438\u0442\u0438"); // алтарь
                                                                                                              // нежити
        boolean isGeyser = lowerMessage.contains("\u0433\u0435\u0439\u0437\u0435\u0440"); // гейзер
        boolean isLegacyEvent = message.contains("[\u0418\u0432\u0435\u043d\u0442\u044b]")
                || message.contains("\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b:");

        if (!isAirdrop && !isAltar && !isGeyser && !isLegacyEvent) {
            return;
        }

        try {
            String eventType = "point"; // Дефолтная иконка для неизвестных ивентов
            String eventName = "";
            if (isAirdrop) {
                eventType = "airdrop";
                eventName = "\u0410\u0438\u0440\u0434\u0440\u043e\u043f"; // Аирдроп
            } else if (isAltar) {
                eventType = "altar";
                eventName = "\u0410\u043b\u0442\u0430\u0440\u044c \u043d\u0435\u0436\u0438\u0442\u0438"; // Алтарь нежити
            } else if (isGeyser) {
                eventType = "geyser";
                eventName = "\u0413\u0435\u0439\u0437\u0435\u0440"; // Гейзер
            } else if (message.contains("\u041c\u0430\u044f\u043a \u0443\u0431\u0438\u0439\u0446\u0430")
                    || message.contains("\u041c\u0430\u044f\u043a")) {
                eventType = "beacon";
                eventName = "\u041c\u0430\u044f\u043a";
            } else if (message.contains("\u0412\u0443\u043b\u043a\u0430\u043d")) {
                eventType = "volcano";
                eventName = "\u0412\u0443\u043b\u043a\u0430\u043d";
            } else if (message.contains(
                    "\u041c\u0435\u0442\u0435\u043e\u0440\u0438\u0442\u043d\u044b\u0439 \u0434\u043e\u0436\u0434\u044c")
                    || message.contains(
                            "\u041c\u0435\u0442\u0438\u043e\u0440\u0438\u0442\u043d\u044b\u0439 \u0434\u043e\u0436\u0434\u044c")) {
                eventType = "meteor";
                eventName = "\u041c\u0435\u0442\u0435\u043e\u0440\u0438\u0442";
            } else if (message.contains("\u0413\u043e\u0440\u044f\u0449\u0438\u0439 \u0447\u0435\u0440\u0435\u043f")) {
                eventType = "point";
                eventName = "\u0413\u043e\u0440\u044f\u0449\u0438\u0439 \u0447\u0435\u0440\u0435\u043f";
            } else {
                int eventStart = message.indexOf("]") + 1;
                int eventEnd = message.indexOf(":", eventStart);
                if (eventStart > 0 && eventEnd > eventStart) {
                    eventName = message.substring(eventStart, eventEnd).trim();
                }
            }

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(-?\\d+)\\s+(\\d+)\\s+(-?\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));

                String waypointName = this.getEventIcon(eventType) + " " + eventName;
                Rockstar.getInstance().getWayPointsManager().add(waypointName, x, y, z, true);
                ClientSounds.APPLEPAY.play(1.0f, 1.0f);
            }
        } catch (Exception e) {
        }
    }

    private String getEventIcon(String eventType) {
        switch (eventType) {
            case "beacon":
                return "\u0045";
            case "geyser":
            case "volcano":
                return "\u0041";
            case "meteor":
                return "\u0043";
            case "airdrop":
                return "\u0044";
            case "altar":
                return "\u0046";
            case "point":
            default:
                return "\u0042";
        }
    }

    private String getEventName(String eventType) {
        switch (eventType) {
            case "airdrop":
                return "\u0410\u0438\u0440\u0434\u0440\u043e\u043f";
            case "altar":
                return "\u0410\u043b\u044c\u0442\u0430\u0440\u044c \u041d\u0435\u0436\u0438\u0442\u0438";
            case "beacon":
                return "\u041c\u0430\u044f\u043a";
            case "volcano":
                return "\u0412\u0443\u043b\u043a\u0430\u043d";
            case "meteor":
                return "\u041c\u0435\u0442\u0435\u043e\u0440\u0438\u0442";
            default:
                return "\u0418\u0432\u0435\u043d\u0442";
        }
    }

    public void stop() {
        ItemSlot itemSlot;
        if (this.isCharging) {
            Minecraft.getInstance().options.keyUse.setDown(false);
            this.isCharging = false;
        }
        if (this.previousSlot != null && (itemSlot = this.previousSlot) instanceof HotbarSlot) {
            HotbarSlot hotbarSlot = (HotbarSlot) itemSlot;
            InventoryUtility.selectHotbarSlot(hotbarSlot);
            this.previousSlot = null;
        }
        this.needsSlotSwapBack = false;
    }
}
