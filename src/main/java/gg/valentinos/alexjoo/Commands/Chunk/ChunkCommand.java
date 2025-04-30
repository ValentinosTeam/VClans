package gg.valentinos.alexjoo.Commands.Chunk;

import gg.valentinos.alexjoo.Commands.SubCommand;
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

public class ChunkCommand  implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public ChunkCommand() {
        registerSubCommand(new ChunkClaimSubcommand());
        registerSubCommand(new ChunkUnclaimSubcommand());
        registerSubCommand(new ChunkRadarSubcommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)){
            Log("This command can only be used by players.", LogType.FINE);
            return true;
        }
        if (args.length == 0) {
            // send info about current chunk
            VClans.sendFormattedMessage(sender, VClans.getInstance().getChunkHandler().getChunkInfo(player.getChunk().getX(), player.getChunk().getZ()), LogType.FINE);
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
        if (subCommand == null) {
            VClans.sendFormattedMessage(sender, "Unknown subcommand. Use '/chunk help' for help.", LogType.FINE);
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()){
                String commandName = entry.getKey();
                SubCommand subCommand = entry.getValue();
                if (subCommand.suggestCommand(sender)){
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
