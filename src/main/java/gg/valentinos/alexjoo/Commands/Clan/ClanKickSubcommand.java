package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClanKickSubcommand extends SubCommand {

    public ClanKickSubcommand() {
        super("clan", "kick", List.of("success", "cant-kick-yourself", "target-not-in-clan", "cant-kick-owner", "kicked-notification", "kicked"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String targetName = args[1];
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);

        return () -> {
            clansHandler.kickPlayer(player.getUniqueId(), targetName);
            sendFormattedMessage(sender, messages.get("success"), LogType.FINE);
            List<UUID> members = clansHandler.getClanMembersUUIDs(player.getUniqueId());
            for (UUID member : members) {
                Player p = Bukkit.getPlayer(member);
                if (p != null && p.isOnline()){
                    if (p.equals(player))
                        continue;
                    sendFormattedMessage(p, messages.get("kicked-notification"), LogType.NULL);
                }
            }
            if (target.getPlayer() != null && target.isOnline())
                sendFormattedMessage(target.getPlayer(), messages.get("kicked"), LogType.NULL);
            cooldownHandler.createCooldown(player.getUniqueId(), selfCooldownQuery, cooldownDuration);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String targetName = args[1];
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);
        Clan clan = clansHandler.getClans().getClanByMember(playerUUID);
        String error = clansHandler.getPlayerIsOwnerErrorKey(playerUUID, clan);
        if (error != null) {
            sendFormattedMessage(sender, VClans.getInstance().getDefaultMessage(error), LogType.WARNING);
            return true;
        }
        if (!target.hasPlayedBefore()) {
            sendFormattedMessage(sender, VClans.getInstance().getDefaultMessage("never-joined"), LogType.WARNING);
            return true;
        }
        if (playerUUID.equals(target.getUniqueId())) {
            sendFormattedMessage(sender, messages.get("cant-kick-yourself"), LogType.WARNING);
            return true;
        }
        if (!clan.getMembers().contains(target.getUniqueId())) {
            sendFormattedMessage(sender, messages.get("target-not-in-clan"), LogType.WARNING);
            return true;
        }
        if (clan.isOwner(target.getUniqueId())) {
            sendFormattedMessage(sender, messages.get("cant-kick-owner"), LogType.WARNING);
            return true;
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String playerName = "ERROR";
        String targetName = "ERROR";
        String clanName = "ERROR";
        if (sender instanceof Player player) {
            playerName = player.getName();
            Clan clan = clansHandler.getClans().getClanByOwner(player.getUniqueId());
            if (clan != null)
                clanName = clan.getName();
        }
        if (args.length > 1) {
            targetName = args[1];
        }
        replacements.put("{target-name}", targetName);
        replacements.put("{player-name}", playerName);
        replacements.put("{clan-name}", clanName);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("kick");
        } else if (args.length == 2) {
            return clansHandler.getClanMembersUUIDs(((Player) sender).getUniqueId())
                    .stream()
                    .map(Bukkit::getOfflinePlayer)
                    .map(OfflinePlayer::getName)
                    .filter(Objects::nonNull)
                    .filter(name -> !name.equals(sender.getName()))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

}
