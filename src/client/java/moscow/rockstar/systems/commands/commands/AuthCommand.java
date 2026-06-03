/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package moscow.rockstar.systems.commands.commands;

import java.util.Map;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.modules.other.AutoAuth;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.network.chat.Component;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class AuthCommand {
    @Compile
    public Command command() {
        return CommandBuilder.begin("auth", b -> b.aliases("autoAuth", "\u043f\u0430\u0440\u043e\u043b\u0438", "passwords").desc("commands.auth.description").handler(this::handle)).build();
    }

    @Compile
    private void handle(CommandContext ctx) {
        Map<String, String> map = Rockstar.getInstance().getModuleManager().getModule(AutoAuth.class).listPassword();
        int counter = 1;
        if (map.isEmpty()) {
            MessageUtility.error(Component.literal((String)Localizator.translate("commands.auth.empty")));
            return;
        }
        MessageUtility.info(Component.literal((String)Localizator.translate("commands.auth.passwords")));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String nickname = entry.getKey();
            String password = entry.getValue();
            MessageUtility.info(Component.literal((String)(counter++ + ") \u041d\u0438\u043a: " + nickname + " | \u041f\u0430\u0440\u043e\u043b\u044c: " + password)));
        }
    }
}

