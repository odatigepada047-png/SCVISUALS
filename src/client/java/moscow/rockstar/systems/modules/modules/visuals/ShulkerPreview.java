package moscow.rockstar.systems.modules.modules.visuals;

import java.util.List;
import moscow.rockstar.framework.base.CustomDrawContext;
import moscow.rockstar.mixin.accessors.HandledScreenAccessor;
import moscow.rockstar.systems.event.EventListener;
import moscow.rockstar.systems.event.impl.render.ScreenRenderEvent;
import moscow.rockstar.systems.modules.api.ModuleCategory;
import moscow.rockstar.systems.modules.api.ModuleInfo;
import moscow.rockstar.systems.modules.impl.BaseModule;
import moscow.rockstar.systems.setting.settings.BooleanSetting;
import moscow.rockstar.systems.setting.settings.SliderSetting;
import moscow.rockstar.systems.setting.settings.ColorSetting;
import moscow.rockstar.utility.colors.ColorRGBA;
import moscow.rockstar.utility.game.ItemUtility;
import moscow.rockstar.utility.gui.GuiUtility;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

@ModuleInfo(name = "Shulker Preview", category = ModuleCategory.VISUALS, desc = "modules.descriptions.shulker_preview")
public class ShulkerPreview extends BaseModule {
    public final BooleanSetting scale = new BooleanSetting(this, "modules.settings.shulker_preview.scale").enable();
    public final SliderSetting scaleValue = new SliderSetting(this, "modules.settings.shulker_preview.scale_value", () -> !scale.isEnabled()).min(0.1f).max(2.0f).step(0.1f).currentValue(1.2f);
    public final ColorSetting color = new ColorSetting(this, "modules.settings.shulker_preview.color").color(new ColorRGBA(255.0f, 255.0f, 255.0f, 255.0f));

    private final EventListener<moscow.rockstar.systems.event.impl.player.ClientPlayerTickEvent> onTick = event -> {
        if (!isEnabled() || mc.player == null || mc.level == null) return;
        if (mc.player.containerMenu instanceof net.minecraft.world.inventory.ShulkerBoxMenu) {
            if (mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                if (mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof ShulkerBoxBlockEntity shulker) {
                    for (int i = 0; i < 27; i++) {
                        shulker.setItem(i, mc.player.containerMenu.slots.get(i).getItem());
                    }
                }
            }
        }
    };

    private final EventListener<ScreenRenderEvent> onScreen = event -> {
        if (!isEnabled() || !(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        HandledScreenAccessor accessor = (HandledScreenAccessor) containerScreen;
        double mx = moscow.rockstar.utility.gui.GuiUtility.getMouse().x;
        double my = moscow.rockstar.utility.gui.GuiUtility.getMouse().y;
        Slot hovered = null;
        for (Slot slot : containerScreen.getMenu().slots) {
            if (slot == null || !slot.isActive()) continue;
            int sx = accessor.getX() + slot.x;
            int sy = accessor.getY() + slot.y;
            if (GuiUtility.isHovered(sx, sy, 16.0, 16.0, mx, my)) {
                hovered = slot;
                break;
            }
        }
        if (hovered == null || !hovered.hasItem()) return;

        List<ItemStack> content = getShulkerItems(hovered.getItem());
        if (content.isEmpty()) return;

        renderPreview(event.getContext(), content, (float)mx + 12.0f, (float)my + 12.0f);
    };

    public List<ItemStack> getHoveredShulkerItems() {
        if (!isEnabled() || mc.player == null) return List.of();
        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
            return List.of();
        }

        try {
            HandledScreenAccessor accessor = (HandledScreenAccessor) containerScreen;
            double mx = moscow.rockstar.utility.gui.GuiUtility.getMouse().x;
            double my = moscow.rockstar.utility.gui.GuiUtility.getMouse().y;
            Slot hovered = null;
            for (Slot slot : containerScreen.getMenu().slots) {
                if (slot == null || !slot.isActive()) continue;
                int sx = accessor.getX() + slot.x;
                int sy = accessor.getY() + slot.y;
                if (GuiUtility.isHovered(sx, sy, 16.0, 16.0, mx, my)) {
                    hovered = slot;
                    break;
                }
            }
            if (hovered == null || !hovered.hasItem()) return List.of();
            return getShulkerItems(hovered.getItem());
        } catch (Throwable t) {
            return List.of();
        }
    }

    public boolean shouldHighlight(ShulkerBoxBlockEntity shulker) {
        if (!isEnabled()) return false;
        for (int i = 0; i < shulker.getContainerSize(); i++) {
            if (!shulker.getItem(i).isEmpty()) return true;
        }
        return false;
    }

    public boolean shouldHighlight(ItemStack stack) {
        return isEnabled() && !getShulkerItems(stack).isEmpty();
    }

    private List<ItemStack> getShulkerItems(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem bi) || !(bi.getBlock() instanceof ShulkerBoxBlock)) {
            return List.of();
        }
        List<ItemStack> out = ItemUtility.getItemsInShulker(stack);
        if (out.size() > 27) {
            return out.subList(0, 27);
        }
        return out;
    }

    private void renderPreview(CustomDrawContext ctx, List<ItemStack> items, float x, float y) {
        float w = 9 * 18 + 8;
        float h = 3 * 18 + 8;
        if (x + w > sr.getGuiScaledWidth() - 4.0f) x -= (w + 24.0f);
        if (y + h > sr.getGuiScaledHeight() - 4.0f) y = sr.getGuiScaledHeight() - h - 4.0f;

        float s = scale.isEnabled() ? scaleValue.getCurrentValue() : 1.0f;
        if (s != 1.0f) {
            moscow.rockstar.utility.render.RenderUtility.scale(ctx.pose(), x, y, s);
        }

        ctx.drawRect(x, y, w, h, new ColorRGBA(20, 20, 20, 170));
        for (int i = 0; i < Math.min(27, items.size()); i++) {
            int col = i % 9;
            int row = i / 9;
            int ix = (int) (x + 4 + col * 18);
            int iy = (int) (y + 4 + row * 18);
            ctx.drawRect(ix, iy, 16, 16, new ColorRGBA(40, 40, 40, 130));
            ctx.drawBatchItem(items.get(i), ix, iy);
        }

        if (s != 1.0f) {
            moscow.rockstar.utility.render.RenderUtility.end(ctx.pose());
        }
    }
}

