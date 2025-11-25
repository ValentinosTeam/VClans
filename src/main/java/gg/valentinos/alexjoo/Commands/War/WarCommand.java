package gg.valentinos.alexjoo.Commands.War;

import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static gg.valentinos.alexjoo.VClans.Log;

public class WarCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public WarCommand() {
        registerSubCommand(new WarDeclareSubcommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Log("This command can only be used by players.", LogType.FINE);
            return true;
        }
        if (args.length == 0) {
            // send info about current war status
            Clan playerClan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
            if (playerClan == null) {
                VClans.sendFormattedMessage(sender, "You are not in a clan.", LogType.WARNING);
                return true;
            }
            Clan enemyClan = VClans.getInstance().getWarHandler().getWarEnemyClan(playerClan);
            if (enemyClan != null) {
                VClans.sendFormattedMessage(sender, "You are currently in a war with " + enemyClan.getName(), LogType.INFO);
            } else {
                VClans.sendFormattedMessage(sender, "You are not currently in a war.", LogType.INFO);
            }
            int timeSinceLastWar = VClans.getInstance().getWarHandler().getWarCooldown(playerClan);
            if (timeSinceLastWar > 0) {
                VClans.sendFormattedMessage(sender, "Your clan is on war cooldown for another " + timeSinceLastWar + " seconds.", LogType.INFO);
            }
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            VClans.sendFormattedMessage(sender, "Unknown subcommand. Use '/war help' for help.", LogType.FINE);
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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

    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }
}
