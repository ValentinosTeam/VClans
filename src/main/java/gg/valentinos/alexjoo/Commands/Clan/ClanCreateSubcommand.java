package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanCreateSubcommand extends SubCommand {
    private int clanNameMaxLength;
    private int clanNameMinLength;

    public ClanCreateSubcommand() {
        super("clan", "create", List.of("success", "already-exists", "too-long", "too-short", "invalid-characters", "already-in-clan"));
        hasToBePlayer = true;
        requiredArgs = 2;
        clanNameMaxLength = config.getInt("settings.max-clan-name-length");
        clanNameMinLength = config.getInt("settings.min-clan-name-length");
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String clanName = args[1];
        UUID playerUUID = player.getUniqueId();

        return () -> {
            clanHandler.createClan(playerUUID, clanName);
            sendFormattedMessage(sender, messages.get("success"), LogType.FINE);
            cooldownHandler.createCooldown(playerUUID, selfCooldownQuery, cooldownDuration);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String clanName = args[1];
        UUID playerUUID = player.getUniqueId();
        if (clanHandler.clanExists(clanName)) {
            sendFormattedMessage(sender, messages.get("already-exists"), LogType.WARNING);
            return true;
        }
        if (clanName.length() > clanNameMaxLength) {
            sendFormattedMessage(sender, messages.get("too-long"), LogType.WARNING);
            return true;
        }
        if (clanName.length() < clanNameMinLength) {
            sendFormattedMessage(sender, messages.get("too-short"), LogType.WARNING);
            return true;
        }
        if (clanHandler.isPlayerInAClan(playerUUID)) {
            sendFormattedMessage(sender, messages.get("already-in-clan"), LogType.WARNING);
            return true;
        }
        if (!clanName.matches("[a-zA-Z0-9_-]+")) {
            sendFormattedMessage(sender, messages.get("invalid-characters"), LogType.WARNING);
            return true;
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String playerName = "ERROR";
        String clanName = "ERROR";
        if (sender instanceof Player player) {
            playerName = player.getName();
        }
        if (args.length > 1) {
            clanName = args[1];
        }
        replacements.put("{clan-name}", clanName);
        replacements.put("{player-name}", playerName);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }


}
