package gg.valentinos.alexjoo.Commands.War;

import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Data.WarData.PeaceTreaty;
import gg.valentinos.alexjoo.Data.WarData.War;
import gg.valentinos.alexjoo.Handlers.WarHandler;
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
import static gg.valentinos.alexjoo.VClans.sendFormattedMessage;

public class WarCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public WarCommand() {
        registerSubCommand(new WarDeclareSubcommand());
        registerSubCommand(new WarPeaceSubcommand());
        registerSubCommand(new WarStartSubcommand());
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
                VClans.sendFormattedMessage(sender, "You are not in a clan.", LogType.NULL);
                return true;
            }
            WarHandler warHandler = VClans.getInstance().getWarHandler();
            War war = warHandler.getWar(playerClan);
            if (war == null) {
                VClans.sendFormattedMessage(sender, "You are not in a war.", LogType.NULL);
            } else {
                Clan enemyClan = warHandler.getWarEnemyClan(playerClan);
                sendFormattedMessage(sender, "You are in war with " + enemyClan.getName() + " and the wars state is " + war.getState() + ".", LogType.NULL);
                PeaceTreaty peaceTreaty = war.getPeaceTreaty();
                if (peaceTreaty != null) {
                    sendFormattedMessage(sender, "There is a peace treaty declared by " + peaceTreaty.getCreator().getName() + " of " + peaceTreaty.getCreatorClan().getName() + " clan", LogType.NULL);
                    if (peaceTreaty.getAmountOffered() >= 0) {
                        sendFormattedMessage(sender, "They are offering to pay your clan $" + peaceTreaty.getAmountOffered() + " to stop this war.", LogType.NULL);
                    } else if (peaceTreaty.getAmountRequested() > 0) {
                        sendFormattedMessage(sender, "They are requesting your clan to pay them $" + peaceTreaty.getAmountOffered() + " if you want to stop this war.", LogType.NULL);
                    }
                }
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
