package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class ClanDisbandSubcommand implements SubCommand {
    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getDescription() {
        return "Disbands the clan you are an owner of.";
    }

    @Override
    public String getUsage() {
        return "/clan disband";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length > 1) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }

        String clanName = clansHandler.getClanNameOfMember(((Player) sender).getUniqueId());
        String error = clansHandler.disbandClan(((Player) sender).getUniqueId());

        sender.sendMessage(Objects.requireNonNullElseGet(error, () -> "Clan " + clanName + " disbanded successfully!"));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
