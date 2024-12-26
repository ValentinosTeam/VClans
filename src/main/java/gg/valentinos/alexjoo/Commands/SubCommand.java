package gg.valentinos.alexjoo.Commands;

import gg.valentinos.alexjoo.Handlers.ClansHandler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {
    ClansHandler clansHandler = VClans.getInstance().getClansHandler();

    String getName(); // Name of the subcommand (e.g., "create", "delete")

    String getDescription(); // Optional, for help messages

    String getUsage(); // Optional, for usage instructions

    boolean execute(CommandSender sender, String[] args); // The logic for the subcommand

    List<String> onTabComplete(CommandSender sender, String[] args);

}
