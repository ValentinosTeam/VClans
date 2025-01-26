package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
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
        super("clan", "kick", List.of("success", "cant-kick-yourself", "target-not-in-clan", "cant-kick-owner", "player-kicked-notification", "player-kicked"));
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
            sender.sendMessage(messages.get("success").replace("{name}", targetName));
            List<UUID> members = clansHandler.getClanMembersUUIDs(player.getUniqueId());
            for (UUID member : members) {
                Player p = Bukkit.getPlayer(member);
                if (p != null && p.isOnline()) {
                    p.sendMessage(messages.get("player-kicked")
                        .replace("{name}", player.getName())
                        .replace("{target}", targetName));
                }
            }
            if (target.getPlayer() != null && target.isOnline()) {
                target.getPlayer().sendMessage(
                    messages.get("messages.invitation")
                        .replace("{clan}", clansHandler.getClans().getClanNameOfMember(player.getUniqueId()))
                        .replace("{name}", player.getName())
                );
            }
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
            sender.sendMessage(VClans.getInstance().getDefaultMessage(error));
            return true;
        }
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(messages.get("never-joined").replace("{name}", targetName));
            return true;
        }
        if (playerUUID.equals(target.getUniqueId())) {
            sender.sendMessage(messages.get("cant-kick-yourself").replace("{name}", targetName));
            return true;
        }
        if (!clan.getMembers().contains(target.getUniqueId())) {
            sender.sendMessage(messages.get("target-not-in-clan").replace("{name}", targetName));
            return true;
        }
        if (clan.isOwner(target.getUniqueId())) {
            sender.sendMessage(messages.get("cant-kick-owner").replace("{name}", targetName));
            return true;
        }
        return false;
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
