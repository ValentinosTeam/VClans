package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class ClanLeaveSubcommand implements SubCommand {
    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Makes you leave the clan you are in.";
    }

    @Override
    public String getUsage() {
        return "/clan leave";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length > 1) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }

        String error = clansHandler.leaveClan(player.getUniqueId());
        player.sendMessage(Objects.requireNonNullElse(error, "You left your clan successfully!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
