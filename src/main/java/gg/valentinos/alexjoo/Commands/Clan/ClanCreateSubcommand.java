package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanCreateSubcommand extends SubCommand {
    private final int clanNameMaxLength;
    private final int clanNameMinLength;

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
            sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            cooldownHandler.createCooldown(playerUUID, selfCooldownQuery, cooldownDuration);
            clanHandler.createClan(playerUUID, clanName);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String clanName = args[1];
        UUID playerUUID = player.getUniqueId();
        if (clanHandler.clanExists(clanName)) {
            sendFormattedPredefinedMessage(sender, "already-exists", LogType.WARNING);
            return true;
        }
        if (clanName.length() > clanNameMaxLength) {
            sendFormattedPredefinedMessage(sender, "too-long", LogType.WARNING);
            return true;
        }
        if (clanName.length() < clanNameMinLength) {
            sendFormattedPredefinedMessage(sender, "too-short", LogType.WARNING);
            return true;
        }
        if (clanHandler.isPlayerInAClan(playerUUID)) {
            sendFormattedPredefinedMessage(sender, "already-in-clan", LogType.WARNING);
            return true;
        }
        if (!clanName.matches("[a-zA-Z0-9_-]+")) {
            sendFormattedPredefinedMessage(sender, "invalid-characters", LogType.WARNING);
            return true;
        }
        return false;
    }
    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            return !clanHandler.isPlayerInAClan(player.getUniqueId());
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
