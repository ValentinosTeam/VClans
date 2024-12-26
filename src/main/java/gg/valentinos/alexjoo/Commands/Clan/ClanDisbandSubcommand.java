package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

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

        String clanName = VClans.getInstance().getClansHandler().getClanName(((Player) sender).getUniqueId());
        String error = VClans.getInstance().getClansHandler().disbandClan(((Player) sender).getUniqueId());

        if (error == null) {
            sender.sendMessage("Clan " + clanName + " disbanded successfully!");
        } else {
            sender.sendMessage(error);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
