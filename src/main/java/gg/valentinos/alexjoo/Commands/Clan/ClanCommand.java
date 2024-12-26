package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public ClanCommand() {
        registerSubCommand(new ClanCreateSubcommand());
        registerSubCommand(new ClanDisbandSubcommand());
        registerSubCommand(new ClanListSubcommand());
        registerSubCommand(new ClanHelpSubcommand());
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Available commands:");
            for (SubCommand subCommand : subCommands.values()) {
                sender.sendMessage("- " + subCommand.getName() + ": " + subCommand.getDescription());
            }
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            sender.sendMessage("Unknown subcommand. Use /clan for help.");
            return true;
        }

        return subCommand.execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(subCommandName -> subCommandName.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length > 1) {
            // Delegate tab completion to the specific subcommand (if any)
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());

            if (subCommand != null) {
                return subCommand.onTabComplete(sender, args);
            }
        }

        return Collections.emptyList();
    }
}
