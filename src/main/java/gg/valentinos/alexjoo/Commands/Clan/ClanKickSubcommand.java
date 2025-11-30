package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Utility.Decorator;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.key.Key;
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
        super("clan", "kick", List.of("success", "cant-kick-yourself", "target-not-in-clan", "insufficient-rank", "kicked-notification", "kicked", "no-permission"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String targetName = args[1];
        Player target = player.getServer().getPlayer(targetName);

        return () -> {
            sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            List<UUID> members = clanHandler.getClanMembersUUIDs(player.getUniqueId());
            for (UUID member : members) {
                Player p = Bukkit.getPlayer(member);
                if (p != null && p.isOnline()) {
                    if (p.equals(player) || p.equals(target))
                        continue;
                    sendFormattedPredefinedMessage(p, "kicked-notification");
                }
            }
            if (target != null && target.isOnline()) {
                sendFormattedPredefinedMessage(target.getPlayer(), "kicked");
                Decorator.PlaySound(target, Key.key("minecraft:block.iron_door.close"), 1f);
            }
            cooldownHandler.createCooldown(player.getUniqueId(), targetCooldownQuery, cooldownDuration);
            clanHandler.kickPlayer(player.getUniqueId(), targetName);
            VClans.getInstance().getVaultHandler().removePlayerPrefix(target);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String targetName = args[1];
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);
        Clan clan = clanHandler.getClanByMember(playerUUID);
        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.WARNING);
            return true;
        }
        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_KICK)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        if (!target.hasPlayedBefore()) {
            sendFormattedPredefinedMessage(sender, "never-joined", LogType.WARNING);
            return true;
        }
        if (playerUUID.equals(target.getUniqueId())) {
            sendFormattedPredefinedMessage(sender, "cant-kick-yourself", LogType.WARNING);
            return true;
        }
        if (!clan.isPlayerMember(target.getUniqueId())) {
            sendFormattedPredefinedMessage(sender, "target-not-in-clan", LogType.WARNING);
            return true;
        }
        if (clan.getRank(playerUUID).getPriority() <= clan.getRank(target.getUniqueId()).getPriority()) {
            sendFormattedPredefinedMessage(sender, "cant-kick-owner", LogType.WARNING);
            return true;
        }
        return false;
    }

    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            return clanHandler.hasPermission(player, ClanRankPermission.CAN_KICK);
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
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
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
            return clanHandler.getClanMembersUUIDs(((Player) sender).getUniqueId())
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
