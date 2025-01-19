package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class ClanHelpSubcommand extends SubCommand {

    private Map<String, SubCommand> subCommands;

    public ClanHelpSubcommand() {
        super("clan", "help");
        maxArgs = 1;
    }
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (commonChecks(sender, args)) return true;

        if (isOnCooldown(sender, selfCooldownQuery)) return true;

        StringBuilder helpMessage = new StringBuilder();
        helpMessage.append(config.getString(configPath + "messages.header")).append("\n");
        helpMessage.append(config.getString(configPath + "messages.content")).append("\n");
        if (config.getBoolean(configPath + "show-command-list")) {
            for (SubCommand subCommand : subCommands.values()) {
                helpMessage.append(config.getString(configPath + "messages.list-item")
                        .replace("{item}", subCommand.getName())
                        .replace("{desc}", subCommand.getDescription())).append("\n");
            }
        }
        helpMessage.append(config.getString(configPath + "messages.footer"));

        handleCommandResult(sender, null, helpMessage.toString());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    public void setSubCommands(Map<String, SubCommand> subCommands) {
        this.subCommands = subCommands;
    }
}
