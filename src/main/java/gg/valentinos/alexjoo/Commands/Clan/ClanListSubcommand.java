package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ClanListSubcommand implements SubCommand {
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Lists every clan on the server.";
    }

    @Override
    public String getUsage() {
        return "/clan list";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length > 1) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }

        sender.sendMessage("Clans:");
        sender.sendMessage(VClans.getInstance().getClansHandler().getClanList());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
