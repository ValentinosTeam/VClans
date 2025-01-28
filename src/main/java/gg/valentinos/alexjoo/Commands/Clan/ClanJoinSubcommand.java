package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanJoinSubcommand extends SubCommand {

    public ClanJoinSubcommand() {
        super("clan", "join", List.of("success", "joined-notification", "clan-not-exist", "not-invited", "already-in-a-clan", "already-in-the-clan"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String clanName = args[1];

        return () -> {
            clansHandler.joinClan(player.getUniqueId(), clanName);
            sendFormattedMessage(sender, messages.get("success"), LogType.FINE);
            List<UUID> clanMembers = clansHandler.getClanMemberUUIDs(clanName);
            for (UUID uuid : clanMembers) {
                Player memberPlayer = Bukkit.getPlayer(uuid);
                if (memberPlayer != null && memberPlayer.isOnline()) {
                    if (memberPlayer.equals(player))
                        continue;
                    sendFormattedMessage(memberPlayer.getPlayer(), messages.get("joined-notification"), LogType.NULL);
                }
            }
            cooldownHandler.createCooldown(player.getUniqueId(), selfCooldownQuery, cooldownDuration);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String clanName = args[1];
        Clan clan = clansHandler.getClans().getClanByName(clanName);
        if (clan == null) {
            sendFormattedMessage(sender, messages.get("clan-not-exist"), LogType.WARNING);
            return true;
        }
        if (!clan.getInvites().contains(playerUUID)) {
            sendFormattedMessage(sender, messages.get("not-invited"), LogType.WARNING);
            return true;
        }
        if (clan.getMembers().contains(playerUUID)) {
            sendFormattedMessage(sender, messages.get("already-in-the-clan"), LogType.WARNING);
            return true;
        }
        if (clansHandler.getClans().getClanByMember(playerUUID) != null) {
            sendFormattedMessage(sender, messages.get("already-in-a-clan"), LogType.WARNING);
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
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("join");
        } else if (args.length == 2) {
            return clansHandler.getInvitingClanNames(player.getUniqueId());
        }
        else {
            return List.of();
        }
    }
}
