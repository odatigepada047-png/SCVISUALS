package moscow.rockstar.systems.commands.commands;

import moscow.rockstar.systems.commands.Command;
import moscow.rockstar.systems.commands.CommandBuilder;
import moscow.rockstar.systems.commands.CommandContext;
import moscow.rockstar.systems.commands.ValidationResult;
import moscow.rockstar.systems.party.PartyManager;
import moscow.rockstar.utility.game.MessageUtility;
import net.minecraft.network.chat.Component;
import ru.kotopushka.compiler.sdk.annotations.Compile;

public class PartyCommand {
    @Compile
    public Command command() {
        return CommandBuilder.begin("party", b -> b.desc("commands.party.description")
            .param("action", p -> p.literal("create", "join", "leave", "info"))
            .param("code", p -> p.optional().validator(ValidationResult::ok))
            .handler(this::handle))
            .build();
    }

    @Compile
    private void handle(CommandContext ctx) {
        if (ctx.arguments().isEmpty()) {
            this.showHelp();
            return;
        }
        
        String action = (String) ctx.arguments().get(0);
        String code = ctx.arguments().size() > 1 ? (String) ctx.arguments().get(1) : null;
        
        PartyManager pm = PartyManager.getInstance();
        
        switch (action.toLowerCase()) {
            case "create":
                pm.createParty();
                break;
            case "join":
                if (code == null) {
                    MessageUtility.error(Component.literal("§cИспользование: .party join <код>"));
                    return;
                }
                pm.joinParty(code);
                break;
            case "leave":
                pm.leaveParty();
                break;
            case "info":
                pm.showInfo();
                break;
            default:
                this.showHelp();
                break;
        }
    }

    @Compile
    private void showHelp() {
        MessageUtility.info(Component.literal("§6[Party] Команды:"));
        MessageUtility.info(Component.literal("§f.party create §7- создать группу"));
        MessageUtility.info(Component.literal("§f.party join <код> §7- войти в группу"));
        MessageUtility.info(Component.literal("§f.party leave §7- покинуть группу"));
        MessageUtility.info(Component.literal("§f.party info §7- инфо о тиммейтах (HP, XYZ, дист)"));
    }
}
