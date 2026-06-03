package moscow.rockstar.mixin.minecraft.core;

import java.util.Optional;
import moscow.rockstar.utility.game.RegistryCompatibility;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HolderGetter.Provider.class)
public interface HolderGetterProviderMixin {
    @Shadow
    <T> Optional<Holder.Reference<T>> get(ResourceKey<T> key);

    @Overwrite
    default <T> Holder.Reference<T> getOrThrow(ResourceKey<T> key) {
        Optional<Holder.Reference<T>> holder = this.get(key);
        if (holder.isPresent()) {
            return holder.get();
        }

        if (RegistryCompatibility.ROCKSTAR_BUILTIN_FALLBACK.get()) {
            throw new IllegalStateException("Missing element " + key);
        }

        try {
            RegistryCompatibility.ROCKSTAR_BUILTIN_FALLBACK.set(true);
            return RegistryCompatibility.getOrThrowBuiltin(key);
        } catch (RuntimeException ignored) {
            try {
                Holder.Reference<T> ref = Holder.Reference.createStandAlone(new net.minecraft.core.HolderOwner() {}, key);
                try {
                    java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                    unsafeField.setAccessible(true);
                    sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
                    
                    String registryName = key.registry().toString();
                    if (registryName.equals("minecraft:trim_material")) {
                        Class<?> targetClass = Class.forName("net.minecraft.item.equipment.trim.ArmorTrimMaterial");
                        Object dummyValue = unsafe.allocateInstance(targetClass);
                        java.lang.reflect.Method bindMethod = Holder.Reference.class.getDeclaredMethod("bindValue", Object.class);
                        bindMethod.setAccessible(true);
                        bindMethod.invoke(ref, dummyValue);
                    } else if (registryName.equals("minecraft:trim_pattern")) {
                        Class<?> targetClass = Class.forName("net.minecraft.item.equipment.trim.ArmorTrimPattern");
                        Object dummyValue = unsafe.allocateInstance(targetClass);
                        java.lang.reflect.Method bindMethod = Holder.Reference.class.getDeclaredMethod("bindValue", Object.class);
                        bindMethod.setAccessible(true);
                        bindMethod.invoke(ref, dummyValue);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                return ref;
            } catch (Throwable t) {
                throw new IllegalStateException("Missing element " + key, t);
            }
        } finally {
            RegistryCompatibility.ROCKSTAR_BUILTIN_FALLBACK.set(false);
        }
    }
}
