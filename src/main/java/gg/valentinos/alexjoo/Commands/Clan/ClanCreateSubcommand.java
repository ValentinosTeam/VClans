package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClanCreateSubcommand extends SubCommand {

    public ClanCreateSubcommand() {
        super("clan", "create", List.of("success", "already-exists", "too-long", "too-short", "invalid-characters", "already-in-clan"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String clanName = args[1];
        UUID playerUUID = player.getUniqueId();
        return () -> {
            clansHandler.createClan(playerUUID, clanName);
            sender.sendMessage(messages.get("success").replace("{name}", clanName));
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String clanName = args[1];
        UUID playerUUID = player.getUniqueId();
        if (clansHandler.getClans().clanExists(clanName)) {
            player.sendMessage(messages.get("already-exists"));
            return true;
        }
        if (clanName.length() > 16) {
            player.sendMessage(messages.get("too-long"));
            return true;
        }
        if (clanName.length() < 3) {
            player.sendMessage(messages.get("too-short"));
            return true;
        }
        if (clansHandler.getClans().isPlayerInClan(playerUUID)) {
            player.sendMessage(messages.get("already-in-clan"));
            return true;
        }
        if (!clanName.matches("[a-zA-Z0-9]+")) {
            player.sendMessage(messages.get("invalid-characters"));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }


}
