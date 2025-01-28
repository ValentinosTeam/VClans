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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClanPromoteSubcommand extends SubCommand {
    public ClanPromoteSubcommand() {
        super("clan", "promote", List.of("success", "promoted", "cant-promote-yourself", "target-not-in-clan", "already-owner", "promoted-notification"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String targetName = args[1];
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);

        return () -> {
            clansHandler.promotePlayer(player.getUniqueId(), targetName);
            sendFormattedMessage(sender, messages.get("success"), LogType.FINE);
            if (target.getPlayer() != null && target.isOnline())
                sendFormattedMessage(target.getPlayer(), messages.get("promoted"), LogType.NULL);
            List<UUID> members = clansHandler.getClanMembersUUIDs(player.getUniqueId());
            for (UUID member : members) {
                Player p = Bukkit.getPlayer(member);
                if (p != null && p.isOnline()){
                    if (p.equals(player))
                        continue;
                    sendFormattedMessage(p, messages.get("promoted-notification"), LogType.NULL);
                }
            }
            cooldownHandler.createCooldown(player.getUniqueId(), selfCooldownQuery, cooldownDuration);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String targetName = args[1];
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore()) {
            sender.sendMessage(VClans.getInstance().getDefaultMessage("never-joined"));
            return true;
        } else if (target.equals(player)) {
            sender.sendMessage(messages.get("cant-promote-yourself"));
            return true;
        }

        Clan clan = clansHandler.getClans().getClanByMember(playerUUID);
        String error = clansHandler.getPlayerIsOwnerErrorKey(playerUUID, clan);
        if (error != null) {
            sender.sendMessage(VClans.getInstance().getDefaultMessage(error));
            return true;
        }
        if (clan.getOwners().contains(target.getUniqueId())) {
            sender.sendMessage(messages.get("already-owner").replace("{name}", targetName));
            return true;
        }
        if (!clan.getMembers().contains(target.getUniqueId())) {
            sender.sendMessage(messages.get("target-not-in-clan").replace("{name}", targetName));
            return true;
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String playerName = "ERROR";
        String targetName = "ERROR";
        String clanName = "ERROR";

        if (sender instanceof Player player){
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
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("promote");
        } else if (args.length == 2) {
            List<UUID> memberUUIDs = clansHandler.getClanMemberUUIDs(clansHandler.getClans().getClanNameOfMember(player.getUniqueId()));
            List<String> players = new ArrayList<>();
            for (UUID memberUUID : memberUUIDs) {
                Player p = player.getServer().getPlayer(memberUUID);
                if (p != null && (!p.equals(player) || !p.isOnline()))
                    players.add(p.getName());
            }
            return players;
        } else {
            return List.of();
        }
    }

}
