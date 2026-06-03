package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.utility.game.MessageUtility;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import moscow.rockstar.utility.time.Timer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class ReHubCommand
implements IMinecraft {
    private boolean processing;
    private final Timer timer = new Timer();
    private int targetAnarchy = -1;
    private int state = 0; // 0: idle, 1: wait for hub, 2: open compass, 3: click head, 4: click stand, 5: click anarchy

    private final EventListener<ClientPlayerTickEvent> onUpdateEvent = event -> {
        if (!this.processing || ReHubCommand.mc.level == null || ReHubCommand.mc.player == null) {
            return;
        }
        
        if (ServerUtility.isHW()) {
            this.handleHolyWorldRehub();
            return;
        }
        
        if ((ServerUtility.isFT() || ServerUtility.isFT()) && ReHubCommand.mc.level.getDifficulty() == Difficulty.EASY && this.timer.finished(1000L)) {
            ReHubCommand.mc.player.connection.sendCommand("an" + ServerUtility.ftAn);
            this.timer.reset();
            this.processing = false;
        }
    };

    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = event -> {
        if (!this.processing || !ServerUtility.isHW()) return;
        
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundSystemChatPacket) {
            ClientboundSystemChatPacket chatPacket = (ClientboundSystemChatPacket) packet;
            String message = chatPacket.content().getString();
            // Если мы уже в лобби, то пропускаем ожидание хаба
            if (message.contains("Ты уже подключен к лобби") && this.state == 1) {
                this.state = 2; // Переходим к открытию компаса
                this.timer.reset();
            }
        }
    };

    private void handleHolyWorldRehub() {
        if (this.state == 1) { // Wait for hub
            if (this.timer.finished(2000L)) {
                this.state = 2; // Open compass
                this.timer.reset();
            }
        }
        
        if (this.state == 2) { // Open compass
            int compassSlot = -1;
            for (int i = 0; i < 9; i++) {
                net.minecraft.world.item.ItemStack stack = ReHubCommand.mc.player.getInventory().getItem(i);
                if (stack.getItem() == Items.COMPASS) {
                    compassSlot = i;
                    break;
                }
            }
            
            if (compassSlot != -1) {
                ReHubCommand.mc.player.getInventory().setSelectedSlot(compassSlot);
                ReHubCommand.mc.gameMode.useItem(ReHubCommand.mc.player, InteractionHand.MAIN_HAND);
                this.state = 3; // Wait for GUI 1
                this.timer.reset();
            } else {
                MessageUtility.error(Component.literal("Compass not found in hotbar!"));
                this.processing = false;
                this.state = 0;
            }
        }
        
        if (this.state == 3) { // Click 2nd head in first GUI
            if (ReHubCommand.mc.screen instanceof ContainerScreen) {
                ChestMenu handler = (ChestMenu) ReHubCommand.mc.player.containerMenu;
                int headCount = 0;
                int targetSlot = -1;
                for (int i = 0; i < handler.getRowCount() * 9; i++) {
                    net.minecraft.world.item.ItemStack stack = handler.getSlot(i).getItem();
                    if (stack.getItem() == Items.PLAYER_HEAD) {
                        headCount++;
                        if (headCount == 2) {
                            targetSlot = i;
                            break;
                        }
                    }
                }
                
                if (targetSlot != -1) {
                    ReHubCommand.mc.gameMode.handleContainerInput(handler.containerId, targetSlot, 0, ContainerInput.PICKUP, ReHubCommand.mc.player);
                    this.state = 4; // Wait for GUI 2 (stands)
                    this.timer.reset();
                }
            }
        }
        
        if (this.state == 4) { // Click stand in 2nd GUI
            if (ReHubCommand.mc.screen instanceof ContainerScreen) {
                ContainerScreen screen = (ContainerScreen) ReHubCommand.mc.screen;
                String title = screen.getTitle().getString();
                
                // Проверяем, что это нужное меню
                if (!title.contains("Выбор Лайт анархии")) {
                    return;
                }
                
                ChestMenu handler = (ChestMenu) ReHubCommand.mc.player.containerMenu;
                int targetSlot = -1;
                
                int requiredCount = 1;
                if (this.targetAnarchy >= 1 && this.targetAnarchy <= 15) requiredCount = 1;
                else if (this.targetAnarchy >= 16 && this.targetAnarchy <= 31) requiredCount = 2;
                else if (this.targetAnarchy >= 32 && this.targetAnarchy <= 47) requiredCount = 3;
                else if (this.targetAnarchy >= 48 && this.targetAnarchy <= 63) requiredCount = 16;
                
                System.out.println("[RCT Debug] GUI 2 opened. Searching for count: " + requiredCount);
                for (int i = 0; i < handler.getRowCount() * 9; i++) {
                    net.minecraft.world.item.ItemStack stack = handler.getSlot(i).getItem();
                    System.out.println("[RCT Debug] Slot " + i + ": " + stack.getItem() + " x" + stack.getCount());
                    if (stack.getCount() == requiredCount) {
                        targetSlot = i;
                        break;
                    }
                }
                
                if (targetSlot != -1) {
                    ReHubCommand.mc.gameMode.handleContainerInput(handler.containerId, targetSlot, 0, ContainerInput.PICKUP, ReHubCommand.mc.player);
                    this.state = 5; // Wait for GUI 3 (heads)
                    this.timer.reset();
                }
            }
        }
        
        if (this.state == 5) { // Click anarchy in 3rd GUI
            if (ReHubCommand.mc.screen instanceof ContainerScreen) {
                ChestMenu handler = (ChestMenu) ReHubCommand.mc.player.containerMenu;
                int targetSlot = -1;
                for (int i = 0; i < handler.getRowCount() * 9; i++) {
                    net.minecraft.world.item.ItemStack stack = handler.getSlot(i).getItem();
                    if (stack.getItem() == Items.PLAYER_HEAD && stack.getCount() == this.targetAnarchy) {
                        targetSlot = i;
                        break;
                    }
                }
                
                if (targetSlot != -1) {
                    ReHubCommand.mc.gameMode.handleContainerInput(handler.containerId, targetSlot, 0, ContainerInput.PICKUP, ReHubCommand.mc.player);
                    this.processing = false;
                    this.state = 0;
                }
            }
        }
    }

    public ReHubCommand() {
        Rockstar.getInstance().getEventManager().subscribe(this);
    }

    public Command command() {
        return CommandBuilder.begin("rct", b -> b.aliases("reconnect").desc("commands.rehub.description").param("anarchy", p -> p.optional().validator(ValidationResult::ok)).handler(this::handle)).build();
    }

    private void handle(CommandContext ctx) {
        if (ReHubCommand.mc.player == null || ReHubCommand.mc.level == null) {
            return;
        }
        if (ServerUtility.hasCT) {
            MessageUtility.error(Component.literal((String)Localizator.translate("commands_rehub.ct")));
            return;
        }
        
        String anarchyArg = null;
        if (!ctx.arguments().isEmpty()) {
            anarchyArg = (String) ctx.arguments().get(0);
        }
        
        if (anarchyArg != null) {
            try {
                this.targetAnarchy = Integer.parseInt(anarchyArg);
            } catch (NumberFormatException e) {
                MessageUtility.error(Component.literal("Invalid anarchy number!"));
                return;
            }
        } else {
            this.targetAnarchy = this.getAnarchyFromScoreboard();
            if (this.targetAnarchy == -1) {
                MessageUtility.error(Component.literal("Could not detect current anarchy from scoreboard!"));
                return;
            }
        }
        
        this.timer.reset();
        ReHubCommand.mc.player.connection.sendCommand("hub");
        this.processing = true;
        this.state = 1; // Wait for hub
    }

    private int getAnarchyFromScoreboard() {
        if (ReHubCommand.mc.level == null) return -1;
        Scoreboard scoreboard = ReHubCommand.mc.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) {
            System.out.println("[RCT Debug] No sidebar objective found!");
            return -1;
        }
        
        String title = objective.getDisplayName().getString();
        System.out.println("[RCT Debug] Scoreboard Title: " + title);
        
        // Очищаем от кодов цветов
        title = title.replaceAll("§.", "");
        
        // Ищем "Лайт #" или "лайт #" и число после него
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)лайт\\s*#\\s*(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            int result = Integer.parseInt(matcher.group(1));
            System.out.println("[RCT Debug] Detected anarchy: " + result);
            return result;
        }
        
        return -1;
    }
}
