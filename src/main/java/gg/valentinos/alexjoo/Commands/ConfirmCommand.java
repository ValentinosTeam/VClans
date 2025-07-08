package gg.valentinos.alexjoo.Commands;

import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfirmCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length > 0) {
            return false;
        }
        if (sender instanceof Player player) {
            VClans.Log("Player " + player.getName() + " is executing confirmation command.", LogType.INFO);
            VClans.getInstance().getConfirmationHandler().executeConfirmation(player);
        } else {
            VClans.sendFormattedMessage(sender, "Only players can use this command.", LogType.WARNING);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
