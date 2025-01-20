package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClanDisbandSubcommand extends SubCommand {

    public ClanDisbandSubcommand() {
        super("clan", "disband");
        if (config.getBoolean(configPath + "inverse"))
            targetCooldownQuery = "clan-create";
        hasToBePlayer = true;
        maxArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {

        String clanName = clansHandler.getClanNameOfMember(((Player) sender).getUniqueId());
        Player player = (Player) sender;

        return () -> {
            String errorMessage = clansHandler.disbandClan(player.getUniqueId());
            handleCommandResult(sender, errorMessage, config.getString(configPath+"messages.success").replace("{clan}", clanName));
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
