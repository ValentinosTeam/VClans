package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanRankSubcommand extends SubCommand {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public ClanRankSubcommand() {
        super("clan", "rank", List.of());
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {

    }
}
