package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class ClanListSubcommand implements SubCommand {
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Lists every clan on the server or the members of a specific clan.";
    }

    @Override
    public String getUsage() {
        return "/clan list [<clan name>]";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length > 2) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }

        if (args.length == 1) {
            sender.sendMessage("Clans:");
            sender.sendMessage(clansHandler.getClanList());
        } else {
            sender.sendMessage("Members of " + args[1] + ":");
            List<UUID> members = clansHandler.getClanMemberUUIDs(args[1]);
            for (UUID member : members) {
                OfflinePlayer player = sender.getServer().getOfflinePlayer(member);
                sender.sendMessage(clansHandler.getClanMembersList(args[1]));
            }
        }


        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
