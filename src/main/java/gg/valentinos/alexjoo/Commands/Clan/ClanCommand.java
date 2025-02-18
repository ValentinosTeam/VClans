package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public ClanCommand() {
        ClanHelpSubcommand clanHelpSubcommand = new ClanHelpSubcommand();
        registerSubCommand(clanHelpSubcommand);
        registerSubCommand(new ClanCreateSubcommand());
        registerSubCommand(new ClanDisbandSubcommand());
        registerSubCommand(new ClanListSubcommand());
        registerSubCommand(new ClanInviteSubcommand());
        registerSubCommand(new ClanJoinSubcommand());
        registerSubCommand(new ClanLeaveSubcommand());
        registerSubCommand(new ClanKickSubcommand());
        //TODO: rank create, rank delete, rank setperm, rank setname, rank settitle, rank setPriority
        //TODO: clan setname


        clanHelpSubcommand.setSubCommands(subCommands);
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        //TODO: make all messages configurable
        if (args.length == 0) {
            if (sender instanceof Player player){
                String clanName = VClans.getInstance().getClansHandler().getClanNameOfMember(player.getUniqueId());
                sender.sendMessage(clanName == null ? "Clanless..." : "Your clan: " + clanName);
            }
            sender.sendMessage("Use '/clan help' for help.");
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            sender.sendMessage("Unknown subcommand. Use /clan for help.");
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
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
