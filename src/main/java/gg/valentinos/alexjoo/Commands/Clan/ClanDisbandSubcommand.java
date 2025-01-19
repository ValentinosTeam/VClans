package gg.valentinos.alexjoo.Commands.Clan;

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
    public boolean execute(CommandSender sender, String[] args) {
        if (commonChecks(sender, args)) return true;

        String clanName = clansHandler.getClanNameOfMember(((Player) sender).getUniqueId());
        VClans.getInstance().getLogger().info("Clan name: " + clanName);
        Player player = (Player) sender;
        VClans.getInstance().getLogger().info("Player: " + player.getName());

        if (isOnCooldown(sender, selfCooldownQuery)) return true;
        VClans.getInstance().getLogger().info("Not on cooldown");

        String errorMessage = clansHandler.disbandClan(player.getUniqueId());

        VClans.getInstance().getLogger().info("Error message: " + errorMessage);

        handleCommandResult(sender, errorMessage, config.getString(configPath+"messages.success").replace("{clan}", clanName));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
