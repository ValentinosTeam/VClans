package gg.valentinos.alexjoo.Commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    String getName(); // Name of the subcommand (e.g., "create", "delete")

    String getDescription(); // Optional, for help messages

    String getUsage(); // Optional, for usage instructions

    boolean execute(CommandSender sender, String[] args); // The logic for the subcommand

    List<String> onTabComplete(CommandSender sender, String[] args);

}
