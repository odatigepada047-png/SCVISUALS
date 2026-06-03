/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package moscow.rockstar.systems.commands.commands;

import java.util.List;
import moscow.rockstar.Rockstar;
import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.ParameterBuilder;
import moscow.rockstar.systems.localization.Localizator;
import moscow.rockstar.systems.modules.Module;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.network.chat.Component;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class ToggleCommand {
    @Compile
    public Command command() {
        List<String> moduleNames = Rockstar.getInstance().getModuleManager().getModules().stream().map(module -> module.getName().replace(" ", "")).toList();
        return CommandBuilder.begin("toggle").aliases("t").desc("commands.toggle.description").param("module", p -> p.validator(ParameterBuilder.MODULE).suggests(moduleNames)).handler(context -> {
            Module module = (Module)context.arguments().getFirst();
            module.toggle();
            MessageUtility.info(Component.literal((String)Localizator.translate("commands.toggle." + (module.isEnabled() ? "enabled" : "disabled"), module.getName())));
        }).build();
    }
}

