package gg.valentinos.alexjoo.Commands;

import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ConfirmCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length > 0){
            return false;
        }
        if (sender instanceof Player player){
            VClans.getInstance().getConfirmationHandler().executeConfirmation(player);
        }
        else{
            sender.sendMessage("Only players can use this command.");
        }
        return true;
    }
}
