package gg.valentinos.alexjoo.Commands.War;

import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Data.WarData.PeaceTreaty;
import gg.valentinos.alexjoo.Data.WarData.War;
import gg.valentinos.alexjoo.Data.WarData.WarState;
import gg.valentinos.alexjoo.Handlers.WarHandler;
import gg.valentinos.alexjoo.Utility.Decorator;
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
                VClans.sendFormattedMessage(sender, "&cYou are not in a clan.", LogType.NULL);
                return true;
            }

            WarHandler warHandler = VClans.getInstance().getWarHandler();
            War war = warHandler.getWar(playerClan);

            if (war == null) {
                VClans.sendFormattedMessage(sender, "&7Your clan is &cnot &7currently in a war.", LogType.NULL);
                return true;
            }

            Clan enemyClan = warHandler.getWarEnemyClan(playerClan);
            PeaceTreaty peaceTreaty = war.getPeaceTreaty();
            StringBuilder builder = new StringBuilder();

            builder.append("&7War status for&r: [").append(playerClan.getPrefix()).append("&r] ").append(playerClan.getName()).append("&r\n");
            builder.append("&7Enemy clan&r: [").append(enemyClan.getPrefix()).append("&r] ").append(enemyClan.getName()).append("&r\n");

            int secondsLeft = warHandler.getTimeLeftInSeconds(war);
            if (war.getState() == WarState.DECLARED) {
                builder.append("&7State&r: &bdeclared&r\n");
                builder.append("&7Time before war starts&r: &r").append(Decorator.formatSeconds(secondsLeft)).append("&r\n");
            } else if (war.getState() == WarState.IN_PROGRESS) {
                builder.append("&7State&r: &cin progress&r\n");
                builder.append("&7Time before war ends&r: &r").append(Decorator.formatSeconds(secondsLeft)).append("&r\n");
            }

            if (peaceTreaty != null) {
                builder.append("\n&7Peace treaty&r:\n");
                builder.append(" &7- Creator&r: [")
                        .append(peaceTreaty.getCreatorClan().getPrefix()).append("&r] ")
                        .append(peaceTreaty.getCreator().getName())
                        .append(" &7of&r ")
                        .append(peaceTreaty.getCreatorClan().getName())
                        .append("&r\n");

                if (peaceTreaty.getAmountOffered() > 0) {
                    builder.append(" &7- Offers &r: &e$")
                            .append(peaceTreaty.getAmountOffered())
                            .append(" &7paid to &r")
                            .append(peaceTreaty.getTargetClan().getName())
                            .append("&r\n");
                }

                if (peaceTreaty.getAmountRequested() > 0) {
                    builder.append(" &7- Demands&r: &e$")
                            .append(peaceTreaty.getAmountRequested())
                            .append(" &r")
                            .append(peaceTreaty.getTargetClan().getName())
                            .append("&7 must pay for.\n");
                }
            }
            VClans.sendFormattedMessage(sender, builder.toString(), LogType.NULL);
            VClans.sendFormattedMessage(sender, "Use '/war help' for help.", LogType.FINE);
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
