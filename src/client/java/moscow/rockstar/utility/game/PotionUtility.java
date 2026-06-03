/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.component.DataComponents
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.effect.MobEffect
 *  net.minecraft.entity.effect.MobEffectInstance
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.item.PotionItem
 *  net.minecraft.registry.entry.Holder
 */
package moscow.rockstar.utility.game;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.core.Holder;

public final class PotionUtility {
    public static boolean hasEffect(ItemStack stack, Holder<MobEffect> effectType) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (!(stack.getItem() instanceof PotionItem)) {
            return false;
        }
        PotionContents potionContents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) {
            return false;
        }
        for (MobEffectInstance effect : potionContents.getAllEffects()) {
            if (!effect.is(effectType)) continue;
            return true;
        }
        return false;
    }

    public static List<MobEffectInstance> effects(ItemStack stack) {
        ArrayList<MobEffectInstance> effects = new ArrayList<MobEffectInstance>();
        if (stack == null || stack.isEmpty()) {
            return effects;
        }
        if (!(stack.getItem() instanceof PotionItem)) {
            return effects;
        }
        PotionContents potionContents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) {
            return effects;
        }
        potionContents.getAllEffects().forEach(effects::add);
        return effects;
    }

    @Generated
    private PotionUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

