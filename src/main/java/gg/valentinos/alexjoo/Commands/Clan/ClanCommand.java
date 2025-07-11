package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
        registerSubCommand(new ClanRankSubcommand());
        registerSubCommand(new ClanColorSubcommand());

        clanHelpSubcommand.setSubCommands(subCommands);
    }

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                String clanName = VClans.getInstance().getClanHandler().getClanNameOfMember(player.getUniqueId());
                if (clanName != null) {
                    VClans.sendFormattedMessage(sender, "Your current clan: " + clanName, LogType.FINE);
                }
            }
            VClans.sendFormattedMessage(sender, "Use '/clan help' for help.", LogType.FINE);
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            VClans.sendFormattedMessage(sender, "Unknown subcommand. Use '/clan help' for help.", LogType.FINE);
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                String commandName = entry.getKey();
                SubCommand subCommand = entry.getValue();
                if (subCommand.suggestCommand(sender)) {
                    suggestions.add(commandName);
                }
            }
            return suggestions;
        } else if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.onTabComplete(sender, args);
            }
        }

        return Collections.emptyList();
    }
}
