package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.GUIs.RankGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClanRankSubcommand extends SubCommand {

    public ClanRankSubcommand() {
        super("clan", "rank", List.of());
        hasToBePlayer = true;
        requiredArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        RankGui rankGui = new RankGui();
        return () -> {
            rankGui.openInventory(player);
        };
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
