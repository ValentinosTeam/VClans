package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanCreateSubcommand extends SubCommand {

    public ClanCreateSubcommand() {
        super("clan", "create");
        if (config.getBoolean(configPath + "inverse"))
            targetCooldownQuery = "clan-disband";
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (commonChecks(sender, args)) return true;

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String clanName = args[1];

        if (isOnCooldown(sender, selfCooldownQuery)) return true;

        String errorMessage = clansHandler.createClan(playerUUID, clanName);

        handleCommandResult(sender, errorMessage, config.getString(configPath+"messages.success").replace("{name}", clanName));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
