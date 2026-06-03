/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  lombok.Generated
 *  net.minecraft.component.DataComponents
 *  net.minecraft.component.type.ItemEnchantmentsComponent
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.ResourceKey
 *  net.minecraft.registry.entry.Holder
 */
package moscow.rockstar.utility.inventory;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Set;
import lombok.Generated;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;

public final class EnchantmentUtility {
    public static void getEnchantments(ItemStack itemStack, Object2IntMap<Holder<Enchantment>> enchantments) {
        enchantments.clear();
        if (!itemStack.isEmpty()) {
            Set<Object2IntMap.Entry<Holder<Enchantment>>> itemEnchantments = itemStack.getItem() == Items.ENCHANTED_BOOK ? ((ItemEnchantments)itemStack.get(DataComponents.STORED_ENCHANTMENTS)).entrySet() : itemStack.getEnchantments().entrySet();
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments) {
                enchantments.put(entry.getKey(), entry.getIntValue());
            }
        }
    }

    @SafeVarargs
    public static boolean hasEnchantments(ItemStack itemStack, ResourceKey<Enchantment> ... enchantments) {
        if (itemStack.isEmpty()) {
            return false;
        }
        Object2IntArrayMap itemEnchantments = new Object2IntArrayMap();
        EnchantmentUtility.getEnchantments(itemStack, (Object2IntMap<Holder<Enchantment>>)itemEnchantments);
        for (ResourceKey<Enchantment> enchantment : enchantments) {
            if (EnchantmentUtility.hasEnchantment((Object2IntMap<Holder<Enchantment>>)itemEnchantments, enchantment)) continue;
            return false;
        }
        return true;
    }

    public static int getEnchantmentLevel(ItemStack itemStack, ResourceKey<Enchantment> enchantment) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        Object2IntArrayMap itemEnchantments = new Object2IntArrayMap();
        EnchantmentUtility.getEnchantments(itemStack, (Object2IntMap<Holder<Enchantment>>)itemEnchantments);
        return EnchantmentUtility.getEnchantmentLevel((Object2IntMap<Holder<Enchantment>>)itemEnchantments, enchantment);
    }

    public static int getEnchantmentLevel(Object2IntMap<Holder<Enchantment>> itemEnchantments, ResourceKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : Object2IntMaps.fastIterable(itemEnchantments)) {
            if (!entry.getKey().is(enchantment)) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    private static boolean hasEnchantment(Object2IntMap<Holder<Enchantment>> itemEnchantments, ResourceKey<Enchantment> enchantmentKey) {
        for (Holder enchantment : itemEnchantments.keySet()) {
            if (!enchantment.is(enchantmentKey)) continue;
            return true;
        }
        return false;
    }

    @Generated
    private EnchantmentUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
