package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class ClanHelpSubcommand extends SubCommand {

    private Map<String, SubCommand> subCommands;
    private final boolean showCommandList;

    public ClanHelpSubcommand() {
        super("clan", "help", List.of("header", "content", "list-item", "footer"));
        maxArgs = 1;
        showCommandList = config.getBoolean(configPath + "show-command-list");
    }
    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        return () ->{
            StringBuilder helpMessage = new StringBuilder();
            helpMessage.append(messages.get("header")).append("\n");
            helpMessage.append(messages.get("content")).append("\n");
            if (showCommandList) {
                for (SubCommand subCommand : subCommands.values()) {
                    helpMessage.append(messages.get("list-item")
                            .replace("{item}", subCommand.getName())
                            .replace("{desc}", subCommand.getDescription())).append("\n");
                }
            }
            helpMessage.append(messages.get("footer"));
            sendFormattedMessage(sender, helpMessage.toString());
//            cooldownHandler.createCooldown(sender, selfCooldownQuery, cooldownDuration);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    public void setSubCommands(Map<String, SubCommand> subCommands) {
        this.subCommands = subCommands;
    }
}
