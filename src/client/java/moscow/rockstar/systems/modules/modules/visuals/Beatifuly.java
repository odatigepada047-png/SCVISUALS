package moscow.rockstar.systems.modules.modules.visuals;

import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.window.ChatClickEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.multiplayer.chat.GuiMessage;

import java.util.Map;
import java.util.WeakHashMap;

@ModuleInfo(name = "Beatifuly", category = ModuleCategory.VISUALS, desc = "Плавные анимации чата, инвентаря, F5 и быстрое копирование команд")
public class Beatifuly extends BaseModule implements IMinecraft {

    public final BooleanSetting chatAnimation = new BooleanSetting(this, "Плавный чат").enable();
    public final BooleanSetting chatCopy = new BooleanSetting(this, "Копирование сообщений").enable();
    public final BooleanSetting containerAnimation = new BooleanSetting(this, "Плавный инвентарь").enable();
    public final BooleanSetting smoothF5 = new BooleanSetting(this, "Плавный F5").enable();

    private static Beatifuly instance;

    public Beatifuly() {
        instance = this;
    }

    public static Beatifuly getInstance() {
        return instance;
    }

    public static boolean isChatAnimEnabled() {
        return instance != null && instance.isEnabled() && instance.chatAnimation.isEnabled();
    }

    public static boolean isContainerAnimEnabled() {
        return instance != null && instance.isEnabled() && instance.containerAnimation.isEnabled();
    }

    public static boolean isSmoothF5Enabled() {
        return instance != null && instance.isEnabled() && instance.smoothF5.isEnabled();
    }

    // --- Chat Animation Cache ---
    private static final Map<GuiMessage.Line, Long> messageTimeMap = new WeakHashMap<>();

    public static float[] getChatAnimationOffset(GuiMessage.Line line) {
        long now = System.currentTimeMillis();
        long firstSeen = messageTimeMap.computeIfAbsent(line, l -> now);
        long elapsed = now - firstSeen;
        long animDuration = 250L; // 250 ms animation
        if (elapsed < animDuration) {
            float t = (float) elapsed / animDuration;
            // Cubic ease out: f(t) = 1 - (1-t)^3
            float ease = 1.0f - (float) Math.pow(1.0f - t, 3);
            float yOffset = -9.0f * (1.0f - ease); // slide up 9 pixels (line height)
            float alphaMult = t; // fade in
            return new float[]{ yOffset, alphaMult };
        }
        return new float[]{ 0.0f, 1.0f };
    }

    // --- Chat Double Click Listener ---
    private final EventListener<ChatClickEvent> onChatClick = event -> {
        if (!chatCopy.isEnabled() || !event.isDoubled()) return;
        if (!(mc.screen instanceof ChatScreen chatScreen)) return;

        // Perform ClickableStyleFinder to locate the sequence
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int guiTicks = mc.gui.getGuiTicks();
        ChatComponent.DisplayMode displayMode = chatScreen.displayMode;

        CustomStyleFinder finder = new CustomStyleFinder(mc.font, (int) event.getX(), (int) event.getY());
        finder.includeInsertions(chatScreen.insertionClickMode());

        mc.gui.getChat().captureClickableText(finder, screenHeight, guiTicks, displayMode);

        if (finder.matchedSequence != null) {
            // Find the GuiMessage.Line matching this sequence in trimmedMessages
            java.util.List<GuiMessage.Line> lines = mc.gui.getChat().trimmedMessages;
            if (lines != null) {
                for (GuiMessage.Line line : lines) {
                    if (line != null && line.content == finder.matchedSequence) {
                        GuiMessage parent = line.parent;
                        if (parent != null) {
                            String rawText = parent.content().getString();
                            handleCopyCommand(rawText);
                            break;
                        }
                    }
                }
            }
        }
    };

    private void handleCopyCommand(String messageText) {
        if (messageText == null || messageText.isEmpty()) return;

        // Try to find the slash command in the message
        int slashIndex = messageText.indexOf('/');
        String copiedText;
        if (slashIndex != -1) {
            copiedText = messageText.substring(slashIndex).trim();
        } else {
            copiedText = messageText.trim();
        }

        mc.keyboardHandler.setClipboard(copiedText);

        if (mc.player != null) {
            mc.player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("§6[Beautiful] §aСкопировано в буфер обмена: §f" + copiedText)
            );
        }
    }

    // Subclass of ClickableStyleFinder to capture the FormattedCharSequence
    public static class CustomStyleFinder extends ActiveTextCollector.ClickableStyleFinder {
        public FormattedCharSequence matchedSequence = null;

        public CustomStyleFinder(Font font, int x, int y) {
            super(font, x, y);
        }

        @Override
        public void accept(net.minecraft.client.gui.TextAlignment alignment, int x, int y, ActiveTextCollector.Parameters params, FormattedCharSequence sequence) {
            Style before = this.result();
            super.accept(alignment, x, y, params, sequence);
            Style after = this.result();
            if (before == null && after != null) {
                this.matchedSequence = sequence;
            }
        }
    }
}
