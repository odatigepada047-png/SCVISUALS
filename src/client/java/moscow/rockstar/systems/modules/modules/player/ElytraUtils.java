/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.item.equipment.EquipmentType
 *  net.minecraft.screen.slot.ContainerInput
 */
package moscow.rockstar.systems.modules.modules.player;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.game.KeyUtility;
import moscow.rockstar.utility.inventory.InventoryUtility;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.ArmorSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

@ModuleInfo(name = "Elytra Utils", category = ModuleCategory.COMBAT, desc = "\u041f\u043e\u043c\u043e\u0449\u043d\u0438\u043a \u0441 \u044d\u043b\u0438\u0442\u0440\u0430\u043c\u0438")
public class ElytraUtils
        extends BaseModule {
    private final BindSetting swapKey = new BindSetting(this,
            "\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0441\u0432\u0430\u043f\u0430");
    private final BooleanSetting autoFlySpeed = new BooleanSetting((SettingsContainer) this, "Auto /fly + speed",
            "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043f\u0438\u0448\u0435\u0442 /fly \u0438 /speed 10 \u043f\u0440\u0438 \u043d\u0430\u0434\u0435\u0432\u0430\u043d\u0438\u0438 \u044d\u043b\u0438\u0442\u0440");
    private int ticksToStop = 3;
    private int currentTick = 0;
    private boolean isSwapping = false;
    private boolean keysReset = false;
    private int chatSendDelay = -1;

    private boolean isInventoryFull() {
        if (ElytraUtils.mc.player == null) {
            return false;
        }
        for (int i = 0; i < 36; i++) {
            if (ElytraUtils.mc.player.getInventory().getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private final EventListener<ClientPlayerTickEvent> onUpdate = event -> {
        if (this.chatSendDelay > 0) {
            this.chatSendDelay--;
            if (this.chatSendDelay == 0) {
                this.chatSendDelay = -1;
                if (this.autoFlySpeed.isEnabled() && ElytraUtils.mc.player != null) {
                    ElytraUtils.mc.player.connection.sendCommand("fly");
                    ElytraUtils.mc.player.connection.sendCommand("speed 10");
                }
            }
        }

        // Обработка свапа с задержкой
        if (this.isSwapping) {
            this.currentTick++;

            // Останавливаем движение на протяжении всего процесса (4 тика)
            if (this.currentTick <= 4) {
                ElytraUtils.mc.options.keyUp.setDown(false);
                ElytraUtils.mc.options.keyDown.setDown(false);
                ElytraUtils.mc.options.keyLeft.setDown(false);
                ElytraUtils.mc.options.keyRight.setDown(false);
                ElytraUtils.mc.options.keySprint.setDown(false);
                ElytraUtils.mc.options.keyJump.setDown(false);
                ElytraUtils.mc.player.setSprinting(false);

                this.keysReset = true;
            }
            
            // Свап на 3-м тике для максимальной уверенности, что движение на сервере остановлено
            if (this.currentTick == 3) {
                this.performSwap();
            }
            
            // Завершаем и возвращаем клавиши
            if (this.currentTick >= 4) {
                if (this.keysReset) {
                    ElytraUtils.mc.options.keyUp.setDown(KeyUtility.isMappingPressed(ElytraUtils.mc.options.keyUp));
                    ElytraUtils.mc.options.keyDown.setDown(KeyUtility.isMappingPressed(ElytraUtils.mc.options.keyDown));
                    ElytraUtils.mc.options.keyLeft.setDown(KeyUtility.isMappingPressed(ElytraUtils.mc.options.keyLeft));
                    ElytraUtils.mc.options.keyRight.setDown(KeyUtility.isMappingPressed(ElytraUtils.mc.options.keyRight));
                    ElytraUtils.mc.options.keyJump.setDown(KeyUtility.isMappingPressed(ElytraUtils.mc.options.keyJump));

                    boolean sprintPressed = KeyUtility.isMappingPressed(ElytraUtils.mc.options.keySprint);
                    ElytraUtils.mc.options.keySprint.setDown(sprintPressed);
                    if (sprintPressed) {
                        ElytraUtils.mc.player.setSprinting(true);
                    }
                    this.keysReset = false;
                }
                this.isSwapping = false;
                this.currentTick = 0;
            }
        }
    };
    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (this.swapKey.isKey(event.getKey()) && event.getAction() == 1 && ElytraUtils.mc.screen == null) {
            this.swapElytraChestplate();
        }
    };
    private final EventListener<MouseEvent> onMouseButtonPress = event -> {
        if (this.swapKey.isKey(event.getButton()) && event.getAction() == 1 && ElytraUtils.mc.screen == null) {
            this.swapElytraChestplate();
        }
    };

    private void swapElytraChestplate() {
        // Всегда используем задержку для безопасности
        this.isSwapping = true;
        this.currentTick = 0;
    }

    private void performSwap() {
        ArmorSlot chestplateSlot = InventoryUtility.getChestplateSlot();
        SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar());
        ItemSlot elytraItemSlot = slotsToSearch
                .findItem(itemStack -> itemStack.getItem() == Items.ELYTRA && !itemStack.nextDamageWillBreak());
        ItemSlot chestplateItemSlot = slotsToSearch.findItem(itemStack -> {
            net.minecraft.world.item.equipment.Equippable equippable = itemStack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
            return equippable != null && equippable.slot() == net.minecraft.world.entity.EquipmentSlot.CHEST;
        });

        boolean isElytraEquipped = chestplateSlot.item() == Items.ELYTRA;
        boolean hasItemEquipped = !chestplateSlot.isEmpty();

        if (this.isInventoryFull()) {
            if (!isElytraEquipped && elytraItemSlot != null) {
                InventoryUtility.moveItem(elytraItemSlot.getIdForServer(), chestplateSlot.getIdForServer(), hasItemEquipped);
                if (this.autoFlySpeed.isEnabled()) {
                    this.chatSendDelay = 2;
                }
            } else if (isElytraEquipped && chestplateItemSlot != null) {
                InventoryUtility.moveItem(chestplateItemSlot.getIdForServer(), chestplateSlot.getIdForServer(), hasItemEquipped);
            }
            return;
        }

        if (!isElytraEquipped && elytraItemSlot != null) {
            if (hasItemEquipped) {
                InventoryUtility.quickMove(chestplateSlot.getIdForServer());
            }
            InventoryUtility.quickMove(elytraItemSlot.getIdForServer());
            if (this.autoFlySpeed.isEnabled()) {
                this.chatSendDelay = 2;
            }
        } else if (isElytraEquipped && chestplateItemSlot != null) {
            InventoryUtility.quickMove(chestplateSlot.getIdForServer());
            InventoryUtility.quickMove(chestplateItemSlot.getIdForServer());
        }
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    private static class SwapTask {
        int stage;
        final ItemSlot from;
        final ItemSlot chest;

        SwapTask(ItemSlot from, ItemSlot chest) {
            this.from = from;
            this.chest = chest;
        }
    }
}
