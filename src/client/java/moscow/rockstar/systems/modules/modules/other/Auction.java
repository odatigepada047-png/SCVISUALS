/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.ingame.AbstractContainerScreen
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.player.Player
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.item.PickaxeItem
 *  net.minecraft.item.PotionItem
 *  net.minecraft.item.tooltip.TooltipType
 *  net.minecraft.registry.ResourceKey
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.Level
 */
package moscow.rockstar.systems.modules.modules.other;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.framework.base.CustomComponent;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.framework.base.UIContext;
import moscow.rockstar.framework.objects.BorderRadius;
import moscow.rockstar.framework.objects.MouseButton;
import moscow.rockstar.framework.objects.gradient.impl.VerticalGradient;
import moscow.rockstar.mixin.accessors.HandledScreenAccessor;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.HudRenderEvent;
import moscow.rockstar.systems.event.impl.render.ScreenRenderEvent;
import moscow.rockstar.systems.event.impl.window.ContainerClickEvent;
import moscow.rockstar.systems.event.impl.window.ContainerReleaseEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.Setting;
import moscow.rockstar.systems.setting.SettingsContainer;
import moscow.rockstar.systems.setting.settings.SelectSetting;
import moscow.rockstar.ui.components.popup.Popup;
import moscow.rockstar.ui.menu.dropdown.components.settings.impl.SelectSettingComponent;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.gui.GuiUtility;
import moscow.rockstar.utility.inventory.EnchantmentUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

@ModuleInfo(name="Auction", category=ModuleCategory.OTHER)
public class Auction
extends BaseModule {
    private final List<AuctionItem> pageItems = new ArrayList<AuctionItem>();
    private double averageEffectivePrice = 0.0;
    private double minEffectivePrice = Double.MAX_VALUE;
    private String title = "";
    private boolean isAuctionCached = false;
    private String lastTitle = "";
    private int scanTicks = 0;
    private static final BorderRadius RADIUS_1 = BorderRadius.all(1.0f);
    private int lastInventoryHash = 0;

    private boolean titleContains(String... keywords) {
        String normalizedTitle = normalize(this.title);
        for (String kw : keywords) {
            if (normalizedTitle.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSearchActive() {
        return this.titleContains("\u043f\u043e\u0438\u0441\u043a", "search");
    }

    private final SelectSetting armor = new SelectSetting((SettingsContainer)this, "modules.settings.auction.armor", () -> this.isSearchActive() && !this.titleContains("\u0448\u043b\u0435\u043c", "\u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a", "\u043f\u043e\u043d\u043e\u0436\u0438", "\u0431\u043e\u0442\u0438\u043d\u043a\u0438", "\u0431\u0440\u043e\u043d\u044f", "helmet", "chestplate", "leggings", "boots", "armor"));
    private final SelectSetting.Value noSpike = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_spike").select();
    private final SelectSetting.Value noProt5 = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_prot5").select();
    private final SelectSetting.Value noDurability = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_durability");
    private final SelectSetting.Value noRepair = new SelectSetting.Value(this.armor, "modules.settings.auction.armor.no_repair");
    private final SelectSetting pickaxe = new SelectSetting((SettingsContainer)this, "modules.settings.auction.pickaxe", () -> this.isSearchActive() && !this.titleContains("\u043a\u0438\u0440\u043a\u0430", "pickaxe"));
    private final SelectSetting.Value noSilkTouch = new SelectSetting.Value(this.pickaxe, "modules.settings.auction.pickaxe.silk_touch");
    private final SelectSetting potions = new SelectSetting((SettingsContainer)this, "modules.settings.auction.potions", () -> this.isSearchActive() && !this.titleContains("\u0441\u0438\u043b\u044b", "\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438", "\u0437\u0435\u043b\u044c\u0435", "potion", "strength", "speed"));
    private final SelectSetting.Value noLevel3 = new SelectSetting.Value(this.potions, "modules.settings.auction.potions.no_level3");
    private final SelectSetting.Value noCombined = new SelectSetting.Value(this.potions, "modules.settings.auction.potions.no_combined");
    private final Popup popup = new Popup(0.0f, 0.0f);
    private final EventListener<HudRenderEvent> onHud = event -> {
        Screen tempScreen = Auction.mc.screen;
        if (!(tempScreen instanceof AbstractContainerScreen) || !this.isAuctionCached) {
            this.popup.setShowing(false);
            this.title = "";
            if (this.popup.getAnimation().getValue() > 0.0f) {
                this.drawPopup(event.getContext());
            }
            return;
        }
        this.popup.setShowing(true);
        this.drawPopup(event.getContext());
    };
    private final EventListener<ScreenRenderEvent> onScreen = event -> {
        Screen tempScreen = Auction.mc.screen;
        if (!(tempScreen instanceof AbstractContainerScreen)) {
            return;
        }
        AbstractContainerScreen screen = (AbstractContainerScreen)tempScreen;
        HandledScreenAccessor accessor = (HandledScreenAccessor)screen;

        if (!this.isAuctionCached) {
            return;
        }

        try {
            if (!this.pageItems.isEmpty()) {
                for (AuctionItem item : this.pageItems) {
                    if (item.effectivePrice > this.averageEffectivePrice) {
                        continue;
                    }
                    Slot slotToHighlight = screen.getMenu().getSlot(item.slotId);
                    if (slotToHighlight == null || !slotToHighlight.isActive() || !slotToHighlight.hasItem()) {
                        continue;
                    }

                    int x = accessor.getX() + slotToHighlight.x;
                    int y = accessor.getY() + slotToHighlight.y;
                    ColorRGBA color = this.calculateHighlightColor(item.effectivePrice);

                    this.drawSlotHighlight(event.getContext(), (float)x, (float)y, color);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            this.reset();
        }
    };
    private final EventListener<ContainerClickEvent> onClick = event -> this.popup.onMouseClicked(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));
    private final EventListener<ContainerReleaseEvent> onRelease = event -> this.popup.onMouseReleased(event.getX(), event.getY(), MouseButton.fromButtonIndex(event.getButton()));

    public Auction() {
        for (Setting setting : this.getSettings()) {
            if (!(setting instanceof SelectSetting)) continue;
            SelectSetting selectSetting = (SelectSetting)setting;
            this.popup.add(new SelectSettingComponent(selectSetting, (CustomComponent)this.popup));
        }
    }

    private int computeInventoryHash(AbstractContainerScreen<?> screen) {
        int hash = 0;
        int containerSize = screen.getMenu().slots.size() - 36;
        for (int i = 0; i < containerSize; ++i) {
            Slot slot = screen.getMenu().getSlot(i);
            if (slot != null && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                hash = hash * 31 + stack.getItem().hashCode();
                hash = hash * 31 + stack.getCount();
                hash = hash * 31 + stack.getDamageValue();
            }
        }
        return hash;
    }

    @Override
    public void tick() {
        Screen screen = Auction.mc.screen;
        if (!(screen instanceof AbstractContainerScreen)) {
            this.reset();
            return;
        }
        AbstractContainerScreen screen2 = (AbstractContainerScreen)screen;
        this.title = screen2.getTitle().getString();
        boolean nextCached = this.isAuction(screen2);
        this.isAuctionCached = nextCached;
        if (!this.isAuctionCached) {
            this.reset();
            return;
        }
        int currentHash = this.computeInventoryHash(screen2);
        if (!this.title.equals(this.lastTitle) || currentHash != this.lastInventoryHash) {
            this.lastTitle = this.title;
            this.lastInventoryHash = currentHash;
            this.scanAndAnalyzePage(screen2);
        }
        super.tick();
    }

    private void drawPopup(CustomDrawContext orig) {
        UIContext context = UIContext.of(orig, Auction.mc.screen == null ? -1 : (int)GuiUtility.getMouse().x, Auction.mc.screen == null ? -1 : (int)GuiUtility.getMouse().y, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        this.popup.setWidth(120.0f);
        this.popup.pos(10.0f, sr.getGuiScaledHeight() / 2.0f - this.popup.getHeight() / 2.0f);
        this.popup.render(context);
    }

    private ColorRGBA calculateHighlightColor(double effectivePrice) {
        double range = this.averageEffectivePrice - this.minEffectivePrice;
        double factor = range > 0.0 ? (effectivePrice - this.minEffectivePrice) / range : 0.0;
        factor = Math.max(0.0, Math.min(1.0, factor));
        int red = (int)(60.0 + 195.0 * factor);
        return new ColorRGBA(factor < (double)0.001f ? (float)red : 255.0f, 255.0f, 60.0f, (float)(250.0 * (1.0 - factor)));
    }

    private void drawSlotHighlight(CustomDrawContext context, float x, float y, ColorRGBA color) {
        float w = 16.0f;
        float h = 16.0f;

        context.drawRoundedRect(x, y, w, h, RADIUS_1, new VerticalGradient(color.withAlpha(0.0f), color.withAlpha(0.8f * color.getAlpha())));
    }

    private boolean isAuction(AbstractContainerScreen<?> screen) {
        if (screen == null) {
            return false;
        }
        String title = screen.getTitle().getString();
        String normalized = normalize(title);
        return normalized.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") || normalized.contains("\u043f\u043e\u0438\u0441\u043a") || normalized.contains("\u0431\u0438\u0440\u0436\u0430")
            || normalized.contains("auction") || normalized.contains("search") || normalized.contains("market") || normalized.contains("ah");
    }

    private String normalize(String str) {
        if (str == null) return "";
        return str.toLowerCase()
            .replace('a', '\u0430') // Eng 'a' -> Rus 'а'
            .replace('c', '\u0441') // Eng 'c' -> Rus 'с'
            .replace('e', '\u0435') // Eng 'e' -> Rus 'е'
            .replace('o', '\u043e') // Eng 'o' -> Rus 'о'
            .replace('p', '\u0440') // Eng 'p' -> Rus 'р'
            .replace('x', '\u0445') // Eng 'x' -> Rus 'х'
            .replace('y', '\u0443'); // Eng 'y' -> Rus 'у'
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    private boolean shouldHideItem(ItemStack stack, List<Component> tooltip) {
        Item item = stack.getItem();
        net.minecraft.world.item.equipment.Equippable equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot().isArmor()) {
            if (this.noSpike.isSelected() && EnchantmentUtility.hasEnchantments(stack, Enchantments.THORNS)) {
                return true;
            }
            if (this.noProt5.isSelected() && EnchantmentUtility.getEnchantmentLevel(stack, (ResourceKey<Enchantment>)Enchantments.PROTECTION) < 5) {
                return true;
            }
            if (this.noDurability.isSelected() && stack.getMaxDamage() > 0 && stack.isDamaged()) {
                return true;
            }
            if (this.noRepair.isSelected() && !EnchantmentUtility.hasEnchantments(stack, Enchantments.MENDING)) {
                return true;
            }
        }
        if (item.toString().contains("pickaxe") && this.noSilkTouch.isSelected() && !EnchantmentUtility.hasEnchantments(stack, Enchantments.SILK_TOUCH)) {
            return true;
        }
        if (item instanceof PotionItem) {
            List<String> tooltipStrings = tooltip.stream().map(text -> text.getString().toLowerCase()).toList();
            if (this.noLevel3.isSelected() && !this.hasLevel3Potion(tooltipStrings)) {
                return true;
            }
            if (this.noCombined.isSelected() && !this.isCombinedPotion(tooltipStrings)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasLevel3Potion(List<String> tooltip) {
        for (String line : tooltip) {
            if (!line.contains("\u0441\u0438\u043b\u0430") && !line.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c") || !line.contains("iii") && !line.contains("3") && !line.contains("\u0443\u0441\u0438\u043b\u0435\u043d\u043d")) continue;
            return true;
        }
        return false;
    }

    private boolean isCombinedPotion(List<String> tooltip) {
        boolean hasStrength = tooltip.stream().anyMatch(line -> line.contains("\u0441\u0438\u043b\u0430"));
        boolean hasSpeed = tooltip.stream().anyMatch(line -> line.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c"));
        return hasStrength && hasSpeed;
    }

    private void scanAndAnalyzePage(AbstractContainerScreen<?> screen) {
        this.pageItems.clear();
        this.minEffectivePrice = Double.MAX_VALUE;
        double totalEffectivePrice = 0.0;
        int pricedItemCount = 0;
        int containerSize = screen.getMenu().slots.size() - 36;
        for (int i = 0; i < containerSize; ++i) {
            List<Component> tooltip;
            Slot slot = screen.getMenu().getSlot(i);
            if (slot == null || !slot.hasItem()) continue;
            ItemStack stack = slot.getItem();
            if (this.shouldHideItem(stack, tooltip = stack.getTooltipLines(Item.TooltipContext.of(Auction.mc.level), (Player)Auction.mc.player, Auction.mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL))) continue;
            long totalPrice = -1L;
            for (Component lineText : tooltip) {
                String line = lineText.getString();
                String normalizedLine = normalize(line);
                String lowerLine = line.toLowerCase();
                boolean isPriceLine = lowerLine.contains("цена") ||
                                      lowerLine.contains("курс") ||
                                      lowerLine.contains("стоимост") ||
                                      lowerLine.contains("купить") ||
                                      lowerLine.contains("price") ||
                                      lowerLine.contains("cost") ||
                                      lowerLine.contains("buy") ||
                                      normalizedLine.contains("\u0446\u0435\u043d\u0430") ||
                                      normalizedLine.contains("\u043a\u0443\u0440\u0441") ||
                                      normalizedLine.contains("\u0441\u0442\u043e\u0438\u043c\u043e\u0441\u0442") ||
                                      normalizedLine.contains("\u043a\u0443\u043f\u0438\u0442");
                if (!isPriceLine) continue;
                try {
                    String cleanLine = line.toLowerCase()
                        .replaceAll("\\d+\\s*(\u0448\u0442|\u0448\u0442\\.|pc|pcs|item|items)", "")
                        .replaceAll("\\(\\d+\\)", "");
                    String priceString = cleanLine.replaceAll("[^\\d]", "");
                    if (priceString.isEmpty()) continue;
                    totalPrice = Long.parseLong(priceString);
                    break;
                }
                catch (NumberFormatException e) {}
            }
            if (totalPrice == -1L) continue;
            int count = stack.getCount();
            int maxDurability = stack.getMaxDamage();
            int currentDurability = maxDurability - stack.getDamageValue();
            double pricePerUnit = (double)totalPrice / (double)count;
            double durabilityFactor = 1.0;
            if (maxDurability > 0) {
                durabilityFactor = (double)currentDurability / (double)maxDurability;
                durabilityFactor = Math.max(0.1, durabilityFactor);
            }
            double effectivePrice = pricePerUnit / durabilityFactor;
            this.pageItems.add(new AuctionItem(i, totalPrice, count, maxDurability, currentDurability, effectivePrice));
            totalEffectivePrice += effectivePrice;
            ++pricedItemCount;
            if (!(effectivePrice < this.minEffectivePrice)) continue;
            this.minEffectivePrice = effectivePrice;
        }
        if (pricedItemCount > 0) {
            this.averageEffectivePrice = totalEffectivePrice / (double)pricedItemCount;
        } else {
            this.reset();
        }
    }

    private void reset() {
        this.pageItems.clear();
        this.averageEffectivePrice = 0.0;
        this.minEffectivePrice = Double.MAX_VALUE;
        this.isAuctionCached = false;
        this.lastTitle = "";
        this.scanTicks = 0;
        this.lastInventoryHash = 0;
    }

    private record AuctionItem(int slotId, long totalPrice, int count, int maxDurability, int currentDurability, double effectivePrice) {
    }
}
