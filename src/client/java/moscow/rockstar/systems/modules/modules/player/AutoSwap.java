package moscow.rockstar.systems.modules.modules.player;

import java.util.Comparator;
import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.event.impl.window.MouseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.systems.setting.settings.BindSetting;
import moscow.rockstar.systems.setting.settings.ModeSetting;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.game.KeyUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import net.minecraft.world.inventory.ContainerInput;
import moscow.rockstar.utility.inventory.ItemSlot;
import moscow.rockstar.utility.inventory.group.SlotGroup;
import moscow.rockstar.utility.inventory.group.SlotGroups;
import moscow.rockstar.utility.inventory.slots.HotbarSlot;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.InteractionHand;

@ModuleInfo(name = "Auto Swap", category = ModuleCategory.COMBAT)
public class AutoSwap extends BaseModule {
    private final BindSetting button = new BindSetting(this, "modules.settings.auto_swap.button");
    private final ModeSetting itemMode = new ModeSetting(this, "modules.settings.auto_swap.item");
    private final ModeSetting.Value swapTal = new ModeSetting.Value(this.itemMode, "modules.settings.auto_swap.item.talisman").select();
    private final ModeSetting swapToMode = new ModeSetting(this, "modules.settings.auto_swap.swap_to");
    private final ModeSetting.Value swapToTal = new ModeSetting.Value(this.swapToMode, "modules.settings.auto_swap.swap_to.talisman").select();
    private final moscow.rockstar.systems.setting.settings.BooleanSetting ignoreVihr = new moscow.rockstar.systems.setting.settings.BooleanSetting(this, "modules.settings.auto_swap.ignore_vihr").enable();
    private final Timer timer = new Timer();

    private int ticksToStop = 3;
    private int currentTick = 0;
    private boolean isSwapping = false;
    private boolean keysReset = false;

    private final EventListener<KeyPressEvent> onKeyPressEvent = event -> {
        if (event.getAction() != 1) {
            return;
        }
        if (this.button.isKey(event.getKey())) {
            this.startSwap();
        }
    };

    private final EventListener<MouseEvent> onMouseEvent = event -> {
        if (this.button.isKey(event.getButton())) {
            this.startSwap();
        }
    };

    public AutoSwap() {
        new ModeSetting.Value(this.swapToMode, "modules.settings.auto_swap.swap_to.orb");
        new ModeSetting.Value(this.itemMode, "modules.settings.auto_swap.item.orb");
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isSwapping) {
            this.currentTick++;

            if (this.currentTick <= 3) {
                AutoSwap.mc.options.keyUp.setDown(false);
                AutoSwap.mc.options.keyDown.setDown(false);
                AutoSwap.mc.options.keyLeft.setDown(false);
                AutoSwap.mc.options.keyRight.setDown(false);
                AutoSwap.mc.options.keySprint.setDown(false);
                AutoSwap.mc.options.keyJump.setDown(false);
                AutoSwap.mc.player.setSprinting(false);

                this.keysReset = true;
            }

            if (this.currentTick == 2) {
                this.swap();
            } else if (this.currentTick >= 3) {
                if (this.keysReset) {
                    AutoSwap.mc.options.keyUp.setDown(KeyUtility.isMappingPressed(AutoSwap.mc.options.keyUp));
                    AutoSwap.mc.options.keyDown.setDown(KeyUtility.isMappingPressed(AutoSwap.mc.options.keyDown));
                    AutoSwap.mc.options.keyLeft.setDown(KeyUtility.isMappingPressed(AutoSwap.mc.options.keyLeft));
                    AutoSwap.mc.options.keyRight.setDown(KeyUtility.isMappingPressed(AutoSwap.mc.options.keyRight));
                    AutoSwap.mc.options.keyJump.setDown(KeyUtility.isMappingPressed(AutoSwap.mc.options.keyJump));

                    boolean sprintPressed = KeyUtility.isMappingPressed(AutoSwap.mc.options.keySprint);
                    AutoSwap.mc.options.keySprint.setDown(sprintPressed);
                    if (sprintPressed) {
                        AutoSwap.mc.player.setSprinting(true);
                    }
                    this.keysReset = false;
                }
                this.isSwapping = false;
                this.currentTick = 0;
            }
        }
    }

    private void startSwap() {
        if (AutoSwap.mc.screen != null) {
            return;
        }

        SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar()).and(SlotGroups.offhand());
        List<ItemSlot> slots = this.filterVihrIfNeeded(
                slotsToSearch.findItems(this.swapTal.isSelected() ? Items.TOTEM_OF_UNDYING : Items.PLAYER_HEAD),
                this.swapTal.isSelected());
        List<ItemSlot> slots1 = this.filterVihrIfNeeded(
                slotsToSearch.findItems(this.swapToTal.isSelected() ? Items.TOTEM_OF_UNDYING : Items.PLAYER_HEAD),
                this.swapToTal.isSelected());

        ItemSlot slot = slots.stream().min(Comparator.comparingInt(stack -> ItemUtility.bestFactor(stack.itemStack()) - (stack.getIdForServer() == 45 ? 99 : 0))).orElse(null);
        ItemSlot slot1 = slots1.stream().filter(candidate -> slot != candidate)
                .min(Comparator.comparingInt(stack -> ItemUtility.bestFactor(stack.itemStack()) - (stack.getIdForServer() == 45 ? 99 : 0))).orElse(null);

        if (slot == null || slot1 == null) {
            return;
        }

        this.isSwapping = true;
        this.currentTick = 0;
    }

    private void swap() {
        if (AutoSwap.mc.screen != null) {
            return;
        }
        SlotGroup<ItemSlot> slotsToSearch = SlotGroups.inventory().and(SlotGroups.hotbar()).and(SlotGroups.offhand());
        List<ItemSlot> slots = this.filterVihrIfNeeded(
                slotsToSearch.findItems(this.swapTal.isSelected() ? Items.TOTEM_OF_UNDYING : Items.PLAYER_HEAD),
                this.swapTal.isSelected());
        List<ItemSlot> slots1 = this.filterVihrIfNeeded(
                slotsToSearch.findItems(this.swapToTal.isSelected() ? Items.TOTEM_OF_UNDYING : Items.PLAYER_HEAD),
                this.swapToTal.isSelected());

        ItemSlot slot = slots.stream().min(Comparator.comparingInt(stack -> ItemUtility.bestFactor(stack.itemStack()) - (stack.getIdForServer() == 45 ? 99 : 0))).orElse(null);
        ItemSlot slot1 = slots1.stream().filter(candidate -> slot != candidate)
                .min(Comparator.comparingInt(stack -> ItemUtility.bestFactor(stack.itemStack()) - (stack.getIdForServer() == 45 ? 99 : 0))).orElse(null);

        if (slot == null || slot1 == null) {
            return;
        }

        int containerId = AutoSwap.mc.player.containerMenu.containerId;
        if (AutoSwap.mc.player.getOffhandItem().getItem() == slot.item()) {
            AutoSwap.mc.gameMode.handleContainerInput(containerId, slot1.getIdForServer(), 40, ContainerInput.SWAP,
                    AutoSwap.mc.player);
        } else {
            AutoSwap.mc.gameMode.handleContainerInput(containerId, slot.getIdForServer(), 40, ContainerInput.SWAP,
                    AutoSwap.mc.player);
        }

        this.timer.reset();
        Rockstar.getInstance().getNotificationManager().addNotificationOther(NotificationType.SUCCESS, this.getName(),
                AutoSwap.mc.player.getOffhandItem().getHoverName().getString().replace("[", "").replace("] ", "")
                        .replace("xxx ", "").replace(" xxx", "").replace("123 ", "").replace(" 123", ""));
    }

    private List<ItemSlot> filterVihrIfNeeded(List<ItemSlot> slots, boolean isTotem) {
        if (!isTotem || !this.ignoreVihr.isEnabled()) {
            return slots;
        }

        return slots.stream()
                .filter(slot -> {
                    String displayName = slot.itemStack().getHoverName().getString();
                    boolean isVihr = displayName.contains("Вихря") || displayName.contains("Вихрь");

                    if (isVihr) {
                        boolean isOffhand = slot.getIdForServer() == 45;
                        boolean isMainHand = slot instanceof HotbarSlot
                                && ((HotbarSlot) slot).getSlotId() == AutoSwap.mc.player.getInventory().getSelectedSlot();
                        return isOffhand || isMainHand;
                    }

                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
