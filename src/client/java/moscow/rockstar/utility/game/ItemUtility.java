/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.component.DataComponents
 *  net.minecraft.component.type.BundleContentsComponent
 *  net.minecraft.component.type.ItemContainerContents
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.registry.DynamicRegistryManager
 *  net.minecraft.registry.HolderLookup$WrapperLookup
 *  net.minecraft.resource.Resource
 *  net.minecraft.resource.ResourceManager
 *  net.minecraft.util.Identifier
 */
package moscow.rockstar.utility.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Generated;
import moscow.rockstar.utility.game.DonateItem;
import moscow.rockstar.utility.game.server.ServerUtility;
import moscow.rockstar.utility.interfaces.IMinecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;

public final class ItemUtility
implements IMinecraft {
    public static List<ItemStack> getItemsInShulker(ItemStack s) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        ItemContainerContents container = (ItemContainerContents)s.get(DataComponents.CONTAINER);
        if (container == null) {
            BundleContents container1 = (BundleContents)s.get(DataComponents.BUNDLE_CONTENTS);
            if (container1 != null) {
                container1.itemCopyStream().forEach(items::add);
                return items;
            }
            CompoundTag nbt = ItemUtility.getNBT(s);
            if (nbt != null && mc.level != null) {
                nbt.getCompound("BlockEntityTag").ifPresent(blockEntityTag -> {
                    blockEntityTag.getList("Items").ifPresent(list -> {
                        net.minecraft.resources.RegistryOps<net.minecraft.nbt.Tag> ops = 
                            net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE, mc.level.registryAccess());
                        for (int i = 0; i < list.size(); i++) {
                            list.getCompound(i).ifPresent(itemTag -> {
                                ItemStack.CODEC.parse(ops, itemTag).result().ifPresent(items::add);
                            });
                        }
                    });
                });
            }
            return items;
        }
        container.nonEmptyItemCopyStream().forEach(items::add);
        return items;
    }

    public static CompoundTag getNBT(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag();
        }
        return null;
    }

    public static boolean checkDonItem(ItemStack itemStack, String startWith) {
        CompoundTag customData = ItemUtility.getNBT(itemStack);
        if (customData == null) {
            return false;
        }
        if (customData.contains("don-item")) {
            String donItemName = customData.getStringOr("don-item", "");
            return donItemName.contains(startWith);
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static String findHashedModel(String hashedId) {
        ResourceManager resourceManager = mc.getResourceManager();
        Identifier modelPath = Identifier.fromNamespaceAndPath((String)"minecraft", (String)("models/item/" + hashedId.replace("minecraft:", "") + ".json"));
        Optional resource = resourceManager.getResource(modelPath);
        if (!resource.isPresent()) {
            return null;
        }
        try (BufferedReader reader = ((Resource)resource.get()).openAsReader()) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            System.err.println("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u0438 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u043e\u0439 \u043c\u043e\u0434\u0435\u043b\u0438: " + e.getMessage());
            return null;
        }
    }

    public static boolean isDonItem(ItemStack itemStack) {
        CompoundTag customData = ItemUtility.getNBT(itemStack);
        if (customData == null) {
            return false;
        }
        return customData.contains("don-item");
    }

    public static String donNBT(ItemStack itemStack) {
        CompoundTag customData = ItemUtility.getNBT(itemStack);
        if (customData == null) {
            return "";
        }
        CompoundTag sphereEffect = customData.getCompoundOrEmpty("sphereEffect");
        if (customData.contains("don-item")) {
            return customData.getStringOr("don-item", "");
        }
        if (customData.contains("spooky-item")) {
            return customData.getStringOr("spooky-item", "");
        }
        if (ServerUtility.is("holyworld") && customData.contains("sphereEffect") && itemStack.getItem() == Items.TOTEM_OF_UNDYING && sphereEffect.contains("rank")) {
            if (sphereEffect.getStringOr("rank", "").equals("ETERNITY")) {
                return sphereEffect.getStringOr("name", "");
            }
            return sphereEffect.getStringOr("rank", "");
        }
        return "";
    }

    public static DonateItem getDonateItem(ItemStack stack) {
        for (DonateItem item : DonateItem.values()) {
            for (String key : item.getNbt()) {
                if (!ItemUtility.donNBT(stack).equals(key)) continue;
                return item;
            }
        }
        return null;
    }

    public static int totemFactor(ItemStack stack) {
        if (stack.isEnchanted()) {
            for (DonateItem item : DonateItem.values()) {
                for (String key : item.getNbt()) {
                    if (!ItemUtility.donNBT(stack).equals(key)) continue;
                    return 12 - item.getTotem();
                }
            }
            return 0;
        }
        return -1;
    }

    public static int bestFactor(ItemStack stack) {
        if (stack.isEnchanted() || ItemUtility.isDonItem(stack)) {
            for (DonateItem item : DonateItem.values()) {
                for (String key : item.getNbt()) {
                    if (!ItemUtility.donNBT(stack).equals(key)) continue;
                    return 15 - item.getFactor();
                }
            }
            return 16;
        }
        return 17;
    }

    @Generated
    private ItemUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
