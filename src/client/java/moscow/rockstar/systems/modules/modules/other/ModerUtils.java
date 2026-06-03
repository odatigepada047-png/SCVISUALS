package moscow.rockstar.systems.modules.modules.other;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.network.ReceivePacketEvent;
import moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent;
import moscow.rockstar.systems.event.impl.window.KeyPressEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BindSetting;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

@ModuleInfo(name = "Moder Utils", category = ModuleCategory.OTHER, enabledByDefault = true, desc = "Модераторские утилиты для автоматического обхода проверок и наказаний.")
public class ModerUtils extends BaseModule {
    public final BindSetting lockTargetBind = new BindSetting(this, "Lock Target ESP");
    public final BindSetting openBind = new BindSetting(this, "Open Menu");

    // Target ESP Lock Feature (дополнение)
    public final moscow.rockstar.systems.setting.settings.BooleanSetting enableTargetLock =
        new moscow.rockstar.systems.setting.settings.BooleanSetting(this, "Enable Target Lock").enable();

    // Just Push binds
    public final BindSetting pushSlime = new BindSetting(this, "Push 2.2 (Слизь)", () -> !enableTargetLock.isEnabled());
    public final BindSetting pushMagma = new BindSetting(this, "Push 2.2.2 (Магма)", () -> !enableTargetLock.isEnabled());
    public final BindSetting pushPuffer = new BindSetting(this, "Push 2.2.3 (Иглобрюх)", () -> !enableTargetLock.isEnabled());
    public final BindSetting pushGhast = new BindSetting(this, "Push 2.8 (Слеза)", () -> !enableTargetLock.isEnabled());

    // SAC Push bind
    public final BindSetting sacPunish = new BindSetting(this, "SAC Punish", () -> !enableTargetLock.isEnabled());

    // Dupe IP bind
    public final BindSetting dupeIpBind = new BindSetting(this, "Dupe IP", () -> !enableTargetLock.isEnabled());
    
    private String lastPunishTarget = null;
    private String retryTarget = null;
    private int retryTicks = -1;

    // Just push state
    private String activePushTarget = null;
    private String pendingPushReason = null;
    private String retryPushTarget = null;
    private String retryPushReason = null;
    private int retryPushTicks = -1;
    
    // Locked target for ESP
    private Player lockedTarget = null;

    public ModerUtils() {
        super();
    }
    
    public Player getLockedTarget() {
        return this.lockedTarget;
    }
    
    public void setLockedTarget(Player target) {
        this.lockedTarget = target;
    }

    public void setLastPunishTarget(String name) {
        this.lastPunishTarget = name;
    }

    public String getLastPunishTarget() {
        return this.lastPunishTarget;
    }

    public void executePush(String playerName, String reason) {
        this.activePushTarget = playerName;
        this.pendingPushReason = reason;
        
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.sendCommand("push " + playerName);
        }
    }

    private final EventListener<KeyPressEvent> onKeyPress = event -> {
        if (event.getAction() != 1 || mc.screen != null) return;
        
        // Lock Target ESP bind (ПРИОРИТЕТ 1 - ВЫШЕ ВСЕХ) - работает ТОЛЬКО если дополнение включено
        if (enableTargetLock.isEnabled() && lockTargetBind.isKey(event.getKey())) {
            if (mc.hitResult instanceof EntityHitResult) {
                EntityHitResult hit = (EntityHitResult) mc.hitResult;
                if (hit.getEntity() instanceof Player) {
                    Player target = (Player) hit.getEntity();
                    // Фиксируем таргет - ESP останется на нём пока не наведёмся на другого
                    this.lockedTarget = target;
                    return;
                }
            }
            // Если не навелись ни на кого - убираем фиксацию
            this.lockedTarget = null;
            return;
        }
        
        // Open menu bind - проверяем ЛКМ (кнопка 0) или custom bind
        boolean isLeftClick = event.getKey() == org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
        boolean isCustomBind = openBind.isKey(event.getKey());
        
        if (isLeftClick || isCustomBind) {
            String targetPlayer = null;
            if (mc.hitResult instanceof EntityHitResult) {
                EntityHitResult hit = (EntityHitResult) mc.hitResult;
                if (hit.getEntity() instanceof Player) {
                    Player target = (Player) hit.getEntity();
                    if (mc.player.distanceTo(target) <= 3.0) {
                        targetPlayer = target.getName().getString();
                    }
                }
            }
            mc.setScreen(new moscow.rockstar.ui.moderutils.ModerUtilsScreen(this, targetPlayer));
            return;
        }
        
        // SAC Punish bind - работает ТОЛЬКО если дополнение включено И есть зафиксированный таргет
        if (enableTargetLock.isEnabled() && sacPunish.isKey(event.getKey())) {
            // Банит ТОЛЬКО зафиксированного игрока
            if (this.lockedTarget != null && this.lockedTarget.isAlive() && !this.lockedTarget.isRemoved()) {
                String name = this.lockedTarget.getName().getString();
                this.lastPunishTarget = name;
                if (mc.player.connection != null) {
                    mc.player.connection.sendCommand("sac punish " + name);
                }
            }
            return;
        }
        
        // Dupe IP bind - отправляет /dupeip <ник зафиксированного таргета>
        if (enableTargetLock.isEnabled() && dupeIpBind.isKey(event.getKey())) {
            if (this.lockedTarget != null && this.lockedTarget.isAlive() && !this.lockedTarget.isRemoved()) {
                String name = this.lockedTarget.getName().getString();
                if (mc.player.connection != null) {
                    mc.player.connection.sendCommand("dupeip " + name);
                }
            }
            return;
        }
        
        // Just Push binds - работают ТОЛЬКО если дополнение включено И есть зафиксированный таргет
        if (enableTargetLock.isEnabled()) {
            String reason = null;
            if (pushSlime.isKey(event.getKey())) {
                reason = "2.2";
            } else if (pushMagma.isKey(event.getKey())) {
                reason = "2.2.2";
            } else if (pushPuffer.isKey(event.getKey())) {
                reason = "2.2.3";
            } else if (pushGhast.isKey(event.getKey())) {
                reason = "2.8";
            }
            
            if (reason != null) {
                // Пушит ТОЛЬКО зафиксированного игрока
                if (this.lockedTarget != null && this.lockedTarget.isAlive() && !this.lockedTarget.isRemoved()) {
                    executePush(this.lockedTarget.getName().getString(), reason);
                }
            }
        }
    };
    
    private Player getTargetPlayer() {
        // Если дополнение выключено - работает как раньше (по наведению)
        if (!enableTargetLock.isEnabled()) {
            if (mc.hitResult instanceof EntityHitResult) {
                EntityHitResult hit = (EntityHitResult) mc.hitResult;
                if (hit.getEntity() instanceof Player) {
                    return (Player) hit.getEntity();
                }
            }
            return null;
        }
        
        // Если дополнение включено - возвращает ТОЛЬКО зафиксированного
        if (this.lockedTarget != null && this.lockedTarget.isAlive() && !this.lockedTarget.isRemoved()) {
            return this.lockedTarget;
        }
        
        return null;
    }

    private final EventListener<ReceivePacketEvent> onReceivePacket = event -> {
        if (mc.player == null) return;
        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientboundSystemChatPacket) {
            ClientboundSystemChatPacket systemChatPacket = (ClientboundSystemChatPacket) packet;
            String message = systemChatPacket.content().getString();
            
            // Handle SAC Punish restricted places warning
            if (message.contains("Вы не можете использовать /sac punish") && message.contains("в этом месте")) {
                int start = message.indexOf("/sac punish ");
                if (start != -1) {
                    start += "/sac punish ".length();
                    int end = message.indexOf(" в этом месте", start);
                    if (end != -1) {
                        String parsedTarget = message.substring(start, end).trim();
                        if (!parsedTarget.isEmpty()) {
                            lastPunishTarget = parsedTarget;
                        }
                    }
                }
                handleBypass();
            }
            
            // Handle Spec check failure for Push command
            if (message.contains("Выйдите со слежки")) {
                if (activePushTarget != null && pendingPushReason != null) {
                    mc.player.connection.sendCommand("spec");
                    retryPushTarget = activePushTarget;
                    retryPushReason = pendingPushReason;
                    retryPushTicks = 10; // Wait 10 ticks
                }
            }
        }
    };

    private final EventListener<ClientPlayerTickEvent> onTick = event -> {
        // 1. Handle SAC Punish retry ticks
        if (retryTarget != null && retryTicks >= 0) {
            if (retryTicks == 0) {
                if (mc.player != null && mc.player.connection != null) {
                    mc.player.connection.sendCommand("sac punish " + retryTarget);
                }
                retryTarget = null;
                retryTicks = -1;
            } else {
                retryTicks--;
            }
        }
        
        // 2. Handle Just Push retry ticks
        if (retryPushTarget != null && retryPushTicks >= 0) {
            if (retryPushTicks == 0) {
                if (mc.player != null && mc.player.connection != null) {
                    mc.player.connection.sendCommand("push " + retryPushTarget);
                    pendingPushReason = retryPushReason;
                    activePushTarget = retryPushTarget;
                }
                retryPushTarget = null;
                retryPushReason = null;
                retryPushTicks = -1;
            } else {
                retryPushTicks--;
            }
        }

        // 3. Auto click item in Push GUI
        if (pendingPushReason != null && mc.player != null) {
            if (mc.player.containerMenu instanceof ChestMenu && mc.screen instanceof ContainerScreen) {
                ChestMenu handler = (ChestMenu) mc.player.containerMenu;
                net.minecraft.world.item.Item targetItem = getTargetItemForReason(pendingPushReason);
                if (targetItem != null) {
                    int targetSlot = -1;
                    for (int i = 0; i < handler.getRowCount() * 9; i++) {
                        net.minecraft.world.item.ItemStack stack = handler.getSlot(i).getItem();
                        if (stack.getItem() == targetItem) {
                            targetSlot = i;
                            break;
                        }
                    }
                    if (targetSlot != -1) {
                        mc.gameMode.handleContainerInput(handler.containerId, targetSlot, 0, ContainerInput.PICKUP, mc.player);
                        mc.player.closeContainer();
                        mc.setScreen(null);
                        pendingPushReason = null;
                        activePushTarget = null;
                    }
                }
            }
        }
    };

    private net.minecraft.world.item.Item getTargetItemForReason(String reason) {
        if (reason == null) return null;
        switch (reason) {
            case "2.2":
                return net.minecraft.world.item.Items.SLIME_BALL;
            case "2.2.2":
                return net.minecraft.world.item.Items.MAGMA_CREAM;
            case "2.2.3":
                return net.minecraft.world.item.Items.PUFFERFISH;
            case "2.8":
                return net.minecraft.world.item.Items.GHAST_TEAR;
            default:
                return null;
        }
    }

    private void handleBypass() {
        if (mc.player == null || lastPunishTarget == null) return;

        BossHealthOverlay boss = mc.gui.getBossOverlay();
        if (boss == null) return;

        try {
            Field field = BossHealthOverlay.class.getDeclaredField("events");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<UUID, LerpingBossEvent> bossBars = (Map<UUID, LerpingBossEvent>) field.get(boss);

            for (LerpingBossEvent bar : bossBars.values()) {
                String barText = bar.getName().getString();
                String lowerBarText = barText.toLowerCase();

                // 1. Слежка за ареной смерти -> /specarena
                if (lowerBarText.contains("арен") && lowerBarText.contains("смерт")) {
                    mc.player.connection.sendCommand("specarena");
                    retryTarget = lastPunishTarget;
                    retryTicks = 10; // Wait 10 ticks (500ms)
                    return;
                }

                // 2. Слежка за пвп ареной -> /specpvp
                if (lowerBarText.contains("пвп") && lowerBarText.contains("арен")) {
                    mc.player.connection.sendCommand("specpvp");
                    retryTarget = lastPunishTarget;
                    retryTicks = 10; // Wait 10 ticks (500ms)
                    return;
                }

                // 3. name игрока из таба и идет время -> /spec name
                if (mc.getConnection() != null) {
                    for (PlayerInfo entry : mc.getConnection().getListedOnlinePlayers()) {
                        String tabName = entry.getProfile().name();
                        if (tabName.equalsIgnoreCase(mc.player.getName().getString())) continue;
                        if (tabName.length() >= 3 && barText.contains(tabName)) {
                            if (lowerBarText.contains("слеж") || lowerBarText.contains("наблюд") || 
                                lowerBarText.contains(":") || lowerBarText.contains("сек") || 
                                lowerBarText.contains("мин") || lowerBarText.matches(".*\\d+.*")) {
                                mc.player.connection.sendCommand("spec " + tabName);
                                retryTarget = lastPunishTarget;
                                retryTicks = 10; // Wait 10 ticks (500ms)
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
