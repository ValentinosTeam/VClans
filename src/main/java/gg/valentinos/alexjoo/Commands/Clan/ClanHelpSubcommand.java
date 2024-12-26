package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ClanHelpSubcommand implements SubCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Shows the help message.";
    }

    @Override
    public String getUsage() {
        return "/clan help";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length > 1) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }

        String helpMessage = """
                Thank you for using Valentinos Clans plugin made by Alex_Joo!
                To view the list of available commands, use /clan.
                
                Show some explanation about what this plugin does here.
                """;

        sender.sendMessage(helpMessage);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
