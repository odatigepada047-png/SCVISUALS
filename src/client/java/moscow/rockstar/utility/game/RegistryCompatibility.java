package moscow.rockstar.utility.game;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

public final class RegistryCompatibility {
    public static final ThreadLocal<Boolean> ROCKSTAR_BUILTIN_FALLBACK = ThreadLocal.withInitial(() -> false);
    private static RegistryAccess.Frozen builtinAccess;

    private RegistryCompatibility() {
    }

    public static RegistryAccess.Frozen getBuiltinAccess() {
        if (builtinAccess == null) {
            builtinAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).freeze();
        }
        return builtinAccess;
    }

    public static <T> Holder.Reference<T> getOrThrowBuiltin(ResourceKey<T> key) {
        // Must not call HolderGetter.Provider.getOrThrow — it is overwritten by HolderGetterProviderMixin
        // and would recurse back into this method.
        return getBuiltinAccess()
            .lookup(key.registryKey())
            .flatMap(getter -> getter.get(key))
            .orElseThrow(() -> new IllegalStateException("Missing builtin element " + key));
    }
}
