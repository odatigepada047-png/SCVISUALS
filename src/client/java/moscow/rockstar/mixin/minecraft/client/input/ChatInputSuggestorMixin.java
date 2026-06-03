package moscow.rockstar.mixin.minecraft.client.input;

import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import moscow.rockstar.Rockstar;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CommandSuggestions.class})
public abstract class ChatInputSuggestorMixin {
    @Shadow
    @Final
    EditBox input;
    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    @Nullable
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    public abstract void showSuggestions(boolean var1);

    @Inject(method={"updateCommandInfo"}, at={@At(value="INVOKE", target="Lcom/mojang/brigadier/StringReader;canRead()Z", remap=false)}, cancellable=true)
    private void injectAutoCompletion(CallbackInfo ci) {
        String prefix;
        String text = this.input.getValue();
        if (text.startsWith(prefix = Rockstar.getInstance().getCommandManager().getPrefix())) {
            this.pendingSuggestions = Rockstar.getInstance().getCommandManager().autoComplete(text, this.input.getCursorPosition());
            this.pendingSuggestions.thenRun(() -> {
                try {
                    if (this.pendingSuggestions.isDone() && !this.pendingSuggestions.get().isEmpty() && this.suggestions == null) {
                        this.showSuggestions(false);
                        ci.cancel();
                    }
                }
                catch (InterruptedException | ExecutionException exception) {
                    // empty catch block
                }
            });
        }
    }
}
