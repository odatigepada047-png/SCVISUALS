package moscow.rockstar.systems.modules.modules.other;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.game.PickupEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.notifications.NotificationType;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@ModuleInfo(name="Item Pickup", category=ModuleCategory.OTHER, enabledByDefault=true, desc="Уведомляет вас при поднятии донатного предмета")
public class ItemPickup
extends BaseModule {
    
    public static class PickedItem {
        public final ItemStack stack;
        public final int count;
        public final boolean insideShulker;
        public final String shulkerName;

        public PickedItem(ItemStack stack, int count, boolean insideShulker, String shulkerName) {
            this.stack = stack;
            this.count = count;
            this.insideShulker = insideShulker;
            this.shulkerName = shulkerName;
        }
    }

    private final List<PickedItem> pickedQueue = new ArrayList<>();

    private final EventListener<PickupEvent> onPickupEvent = event -> {
        ItemStack stack = event.getItemStack();
        int count = event.getCount();
        
        // Add the main picked item to queue
        pickedQueue.add(new PickedItem(stack, count, false, ""));
        
        // If it contains inner items (shulker box / bundle), add them to queue as insideShulker
        List<ItemStack> shulkerItems = ItemUtility.getItemsInShulker(stack);
        if (!shulkerItems.isEmpty()) {
            String shulkerName = stack.getHoverName().getString();
            for (ItemStack innerStack : shulkerItems) {
                pickedQueue.add(new PickedItem(innerStack, innerStack.getCount(), true, shulkerName));
            }
        }
    };

    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        if (!ServerUtility.isST()) {
            return;
        }
        if (event.getPacket() instanceof ClientboundEntityEventPacket) {
            ClientboundEntityEventPacket packet = (ClientboundEntityEventPacket) event.getPacket();
            if (packet.getEventId() == 35) { // Totem pop
                Entity entity = packet.getEntity((Level) ItemPickup.mc.level);
                if (entity instanceof Player && entity != ItemPickup.mc.player) {
                    Player player = (Player) entity;
                    ItemStack offHand = player.getOffhandItem();
                    ItemStack mainHand = player.getMainHandItem();
                    
                    ItemStack popped = ItemStack.EMPTY;
                    if (offHand.getItem() == Items.TOTEM_OF_UNDYING || offHand.getItem() == Items.PLAYER_HEAD) {
                        popped = offHand;
                    } else if (mainHand.getItem() == Items.TOTEM_OF_UNDYING || mainHand.getItem() == Items.PLAYER_HEAD) {
                        popped = mainHand;
                    }
                    
                    String totemType = "Обычный";
                    if (!popped.isEmpty()) {
                        if (popped.getItem() == Items.TOTEM_OF_UNDYING) {
                            if (popped.isEnchanted()) {
                                totemType = "Зачарованный";
                            }
                        } else if (popped.getItem() == Items.PLAYER_HEAD) {
                            String displayName = popped.getHoverName().getString();
                            if (displayName.contains("Сфера") || popped.isEnchanted()) {
                                totemType = "Зачарованный (Сфера)";
                            } else {
                                totemType = "Обычный (Голова)";
                            }
                        }
                    }
                    
                    String textMsg = "Игрок " + player.getName().getString() + " снёс " + totemType + " тотем!";
                    ItemPickup.mc.player.sendSystemMessage(Component.literal("§c[PVPLogs] §f" + textMsg));
                    Rockstar.getInstance().getNotificationManager().addNotificationOther(
                        NotificationType.INFO, 
                        "Снос тотема", 
                        textMsg
                    );
                }
            }
        }
    };

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        if (pickedQueue.isEmpty() || ItemPickup.mc.player == null) {
            return;
        }
        
        List<PickedItem> armorList = new ArrayList<>();
        List<PickedItem> sphereList = new ArrayList<>();
        List<PickedItem> totemList = new ArrayList<>();
        List<PickedItem> trapList = new ArrayList<>();
        List<PickedItem> yavList = new ArrayList<>();
        List<PickedItem> desList = new ArrayList<>();
        List<PickedItem> shulkerList = new ArrayList<>();
        List<PickedItem> donList = new ArrayList<>();
        
        Map<String, List<PickedItem>> shulkerContents = new java.util.HashMap<>();
        
        for (PickedItem item : pickedQueue) {
            ItemStack stack = item.stack;
            if (stack.isEmpty()) continue;
            
            if (item.insideShulker) {
                if (isSpecialNetheriteArmor(stack) || 
                    isSpecialPlayerHead(stack) || 
                    isTotem(stack) || 
                    isSpecialNetheriteScrap(stack) || 
                    isSpecialSugar(stack) || 
                    isSpecialEyeOfEnder(stack) ||
                    isDonateItem(stack)) {
                    shulkerContents.computeIfAbsent(item.shulkerName, k -> new ArrayList<>()).add(item);
                }
                continue;
            }
            
            if (isSpecialNetheriteArmor(stack)) {
                armorList.add(item);
            } else if (isSpecialPlayerHead(stack)) {
                sphereList.add(item);
            } else if (isTotem(stack)) {
                totemList.add(item);
            } else if (isSpecialNetheriteScrap(stack)) {
                trapList.add(item);
            } else if (isSpecialSugar(stack)) {
                yavList.add(item);
            } else if (isSpecialEyeOfEnder(stack)) {
                desList.add(item);
            } else if (isShulkerBox(stack)) {
                shulkerList.add(item);
            } else if (isDonateItem(stack)) {
                donList.add(item);
            }
        }
        
        // Merge duplicates for cleaner printing
        armorList = mergeDuplicates(armorList);
        sphereList = mergeDuplicates(sphereList);
        totemList = mergeDuplicates(totemList);
        trapList = mergeDuplicates(trapList);
        yavList = mergeDuplicates(yavList);
        desList = mergeDuplicates(desList);
        donList = mergeDuplicates(donList);
        shulkerList = mergeDuplicates(shulkerList);
        
        // 1. Process Armor
        if (!armorList.isEmpty()) {
            StringBuilder desc = new StringBuilder();
            for (PickedItem item : armorList) {
                String displayName = item.stack.getHoverName().getString();
                ItemPickup.mc.player.sendSystemMessage(Component.literal("§eВы подняли §f" + displayName + (item.count > 1 ? " x" + item.count : "")));
                if (desc.length() > 0) desc.append(", ");
                desc.append(displayName).append(item.count > 1 ? " x" + item.count : "");
            }
            Rockstar.getInstance().getNotificationManager().addNotificationOther(
                NotificationType.INFO, 
                "Броня Крушителя", 
                "Вы подняли: " + desc.toString()
            );
        }
        
        // 2. Process Spheres
        if (!sphereList.isEmpty()) {
            StringBuilder desc = new StringBuilder();
            for (PickedItem item : sphereList) {
                String displayName = item.stack.getHoverName().getString();
                ItemPickup.mc.player.sendSystemMessage(Component.literal("§eВы подняли §f" + displayName + (item.count > 1 ? " x" + item.count : "")));
                if (desc.length() > 0) desc.append(", ");
                desc.append(displayName).append(item.count > 1 ? " x" + item.count : "");
            }
            Rockstar.getInstance().getNotificationManager().addNotificationOther(
                NotificationType.INFO, 
                "Сферы", 
                "Вы подняли: " + desc.toString()
            );
        }
        
        // 3. Process Totems
        if (!totemList.isEmpty()) {
            int total = 0;
            for (PickedItem item : totemList) {
                String displayName = item.stack.getHoverName().getString();
                ItemPickup.mc.player.sendSystemMessage(Component.literal("§eВы подняли §f" + displayName + (item.count > 1 ? " x" + item.count : "")));
                total += item.count;
            }
            Rockstar.getInstance().getNotificationManager().addNotificationOther(
                NotificationType.INFO, 
                "Тотемы", 
                "Вы подняли зачарованные тотемы: " + total + " шт."
            );
        }
        
        // 4. Process Trapki
        if (!trapList.isEmpty()) {
            StringBuilder desc = new StringBuilder();
            for (PickedItem item : trapList) {
                String displayName = item.stack.getHoverName().getString();
                ItemPickup.mc.player.sendSystemMessage(Component.literal("§eВы подняли §f" + displayName + (item.count > 1 ? " x" + item.count : "")));
                if (desc.length() > 0) desc.append(", ");
                desc.append(displayName).append(item.count > 1 ? " x" + item.count : "");
            }
            Rockstar.getInstance().getNotificationManager().addNotificationOther(
                NotificationType.INFO, 
                "Трапки", 
                "Вы подняли: " + desc.toString()
            );
        }
        
        // 5. Process Yavki
        if (!yavList.isEmpty()) {
            StringBuilder desc = new StringBuilder();
            for (PickedItem item : yavList) {
                String displayName = item.stack.getHoverName().getString();
                ItemPickup.mc.player.sendSystemMessage(Component.literal("§eВы подняли §f" + displayName + (item.count > 1 ? " x" + item.count : "")));
                if (desc.length() > 0) desc.append(", ");
                desc.append(displayName).append(item.count > 1 ? " x" + item.count : "");
            }
            Rockstar.getInstance().getNotificationManager().addNotificationOther(
                NotificationType.INFO, 
                "Явки", 
                "Вы подняли: " + desc.toString()
            );
        }
        
        // 6. Process Desorientations
        if (!desList.isEmpty()) {
            StringBuilder desc = new StringBuilder();
            for (PickedItem item : desList) {
                String displayName = item.stack.getHoverName().getString();
                ItemPickup.mc.player.sendSystemMessage(Component.literal("§eВы подняли §f" + displayName + (item.count > 1 ? " x" + item.count : "")));
                if (desc.length() > 0) desc.append(", ");
                desc.append(displayName).append(item.count > 1 ? " x" + item.count : "");
            }
            Rockstar.getInstance().getNotificationManager().addNotificationOther(
                NotificationType.INFO, 
                "Дезориентации", 
                "Вы подняли: " + desc.toString()
            );
        }
        
        // 7. Process Donate Items
        if (!donList.isEmpty()) {
            StringBuilder desc = new StringBuilder();
            for (PickedItem item : donList) {
                String displayName = item.stack.getHoverName().getString();
                ItemPickup.mc.player.sendSystemMessage(Component.literal("§eВы подняли §f" + displayName + (item.count > 1 ? " x" + item.count : "")));
                if (desc.length() > 0) desc.append(", ");
                desc.append(displayName).append(item.count > 1 ? " x" + item.count : "");
            }
            Rockstar.getInstance().getNotificationManager().addNotificationOther(
                NotificationType.INFO, 
                "Донатные предметы", 
                "Вы подняли: " + desc.toString()
            );
        }
        
        // 8. Process Shulkers
        if (!shulkerList.isEmpty()) {
            for (PickedItem shulkerItem : shulkerList) {
                String shulkerName = shulkerItem.stack.getHoverName().getString();
                
                List<PickedItem> contents = shulkerContents.get(shulkerName);
                if (contents != null && !contents.isEmpty()) {
                    ItemPickup.mc.player.sendSystemMessage(Component.literal("§eВы подняли §f" + shulkerName));
                    contents = mergeDuplicates(contents);
                    StringBuilder chatContents = new StringBuilder();
                    StringBuilder notifContents = new StringBuilder();
                    for (PickedItem inner : contents) {
                        String innerName = inner.stack.getHoverName().getString();
                        if (chatContents.length() > 0) {
                            chatContents.append("§e, §f");
                            notifContents.append(", ");
                        }
                        chatContents.append(innerName).append(inner.count > 1 ? " x" + inner.count : "");
                        notifContents.append(innerName).append(inner.count > 1 ? " x" + inner.count : "");
                    }
                    ItemPickup.mc.player.sendSystemMessage(Component.literal("  §e-> Содержимое: §f" + chatContents.toString()));
                    Rockstar.getInstance().getNotificationManager().addNotificationOther(
                        NotificationType.INFO, 
                        "Содержимое шалкера", 
                        shulkerName + " содержит: " + notifContents.toString()
                    );
                }
            }
        }
        
        // 9. Process any shulker contents where the shulker itself wasn't in direct shulkerList
        for (Map.Entry<String, List<PickedItem>> entry : shulkerContents.entrySet()) {
            String shulkerName = entry.getKey();
            boolean processed = shulkerList.stream().anyMatch(s -> s.stack.getHoverName().getString().equals(shulkerName));
            if (!processed) {
                List<PickedItem> contents = entry.getValue();
                contents = mergeDuplicates(contents);
                StringBuilder chatContents = new StringBuilder();
                StringBuilder notifContents = new StringBuilder();
                for (PickedItem inner : contents) {
                    String innerName = inner.stack.getHoverName().getString();
                    if (chatContents.length() > 0) {
                        chatContents.append("§e, §f");
                        notifContents.append(", ");
                    }
                    chatContents.append(innerName).append(inner.count > 1 ? " x" + inner.count : "");
                    notifContents.append(innerName).append(inner.count > 1 ? " x" + inner.count : "");
                }
                ItemPickup.mc.player.sendSystemMessage(Component.literal("§eВ контейнере §f" + shulkerName + " §eнайдено:"));
                ItemPickup.mc.player.sendSystemMessage(Component.literal("  §e-> Содержимое: §f" + chatContents.toString()));
                Rockstar.getInstance().getNotificationManager().addNotificationOther(
                    NotificationType.INFO, 
                    "Содержимое " + shulkerName, 
                    notifContents.toString()
                );
            }
        }
        
        pickedQueue.clear();
    };

    private List<PickedItem> mergeDuplicates(List<PickedItem> items) {
        List<PickedItem> merged = new ArrayList<>();
        for (PickedItem item : items) {
            PickedItem found = null;
            for (PickedItem m : merged) {
                if (m.stack.getItem() == item.stack.getItem() && 
                    m.stack.getHoverName().getString().equals(item.stack.getHoverName().getString())) {
                    found = m;
                    break;
                }
            }
            if (found != null) {
                int index = merged.indexOf(found);
                merged.set(index, new PickedItem(found.stack, found.count + item.count, found.insideShulker, found.shulkerName));
            } else {
                merged.add(new PickedItem(item.stack, item.count, item.insideShulker, item.shulkerName));
            }
        }
        return merged;
    }

    private boolean isSpecialNetheriteArmor(ItemStack stack) {
        if (stack.getItem() == Items.NETHERITE_HELMET ||
            stack.getItem() == Items.NETHERITE_CHESTPLATE ||
            stack.getItem() == Items.NETHERITE_LEGGINGS ||
            stack.getItem() == Items.NETHERITE_BOOTS) {
            return stack.getHoverName().getString().contains("Крушителя");
        }
        return false;
    }

    private boolean isSpecialPlayerHead(ItemStack stack) {
        if (stack.getItem() == Items.PLAYER_HEAD) {
            return stack.getHoverName().getString().contains("Сфера");
        }
        return false;
    }

    private boolean isTotem(ItemStack stack) {
        return stack.getItem() == Items.TOTEM_OF_UNDYING && stack.isEnchanted();
    }

    private boolean isSpecialNetheriteScrap(ItemStack stack) {
        if (stack.getItem() == Items.NETHERITE_SCRAP) {
            return stack.getHoverName().getString().contains("Трапка");
        }
        return false;
    }

    private boolean isSpecialSugar(ItemStack stack) {
        if (stack.getItem() == Items.SUGAR) {
            return stack.getHoverName().getString().contains("Явка");
        }
        return false;
    }

    private boolean isSpecialEyeOfEnder(ItemStack stack) {
        if (stack.getItem() == Items.ENDER_EYE) {
            return stack.getHoverName().getString().contains("Дезориентация");
        }
        return false;
    }

    private boolean isShulkerBox(ItemStack stack) {
        return stack.getItem().getDescriptionId().contains("shulker_box");
    }

    private boolean isDonateItem(ItemStack stack) {
        return ItemUtility.isDonItem(stack);
    }
}
