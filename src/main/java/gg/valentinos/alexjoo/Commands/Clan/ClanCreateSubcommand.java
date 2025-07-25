package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanCreateSubcommand extends SubCommand {
    private final static int ID_MIN_LENGTH = 4;
    private final static int ID_MAX_LENGTH = 10;

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
            sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            cooldownHandler.createCooldown(playerUUID, selfCooldownQuery, cooldownDuration);
            Clan clan = clanHandler.createClan(playerUUID, clanName);
            VClans.getInstance().getVaultHandler().setPlayerPrefix(player, clan.getPrefix());
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String clanId = args[1];
        UUID playerUUID = player.getUniqueId();
        if (clanHandler.clanExists(clanId)) {
            sendFormattedPredefinedMessage(sender, "already-exists", LogType.WARNING);
            return true;
        }
        if (clanId.length() < ID_MIN_LENGTH) {
            sendFormattedPredefinedMessage(sender, "too-short", LogType.WARNING);
            return true;
        }
        if (clanId.length() > ID_MAX_LENGTH) {
            sendFormattedPredefinedMessage(sender, "too-long", LogType.WARNING);
            return true;
        }
        if (clanHandler.isPlayerInAClan(playerUUID)) {
            sendFormattedPredefinedMessage(sender, "already-in-clan", LogType.WARNING);
            return true;
        }
        if (!clanId.matches("[a-z_-]+")) {
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
        String clanId = "ERROR";

        if (sender instanceof Player player) {
            playerName = player.getName();
        }
        if (args.length > 1) {
            clanId = args[1];
        }
        replacements.put("{clan-id}", clanId);
        replacements.put("{player-name}", playerName);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }


}
