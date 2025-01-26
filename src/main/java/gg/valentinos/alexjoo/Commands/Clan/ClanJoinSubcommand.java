package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClanJoinSubcommand extends SubCommand {

    public ClanJoinSubcommand() {
        super("clan", "join", List.of("success", "member-joined", "clan-not-exist", "not-invited", "already-in-a-clan", "already-in-the-clan"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String clanName = args[1];

        return () -> {
            clansHandler.joinClan(player.getUniqueId(), clanName);
            player.sendMessage(messages.get("success").replace("{clan}", clanName));
            List<UUID> clanMembers = clansHandler.getClanMemberUUIDs(clanName);
            for (UUID uuid : clanMembers) {
                if (uuid.equals(player.getUniqueId()))
                    continue;
                OfflinePlayer memberPlayer = sender.getServer().getOfflinePlayer(uuid);
                if (memberPlayer.isOnline()) {
                    Objects.requireNonNull(memberPlayer.getPlayer()).sendMessage(
                        messages.get("member-joined").replace("{player}", player.getName())
                    );
                }
            }
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String clanName = args[1];
        Clan clan = clansHandler.getClans().getClanByName(clanName);
        if (clan == null) {
            sender.sendMessage(messages.get("clan-not-exist").replace("{clan}", clanName));
            return true;
        }
        if (!clan.getInvites().contains(playerUUID)) {
            sender.sendMessage(messages.get("not-invited").replace("{clan}", clanName));
            return true;
        }
        if (clan.getMembers().contains(playerUUID)) {
            sender.sendMessage(messages.get("already-in-the-clan").replace("{clan}", clanName));
            return true;
        }
        if (clansHandler.getClans().getClanByMember(playerUUID) != null) {
            sender.sendMessage(messages.get("already-in-a-clan").replace("{clan}", clanName));
            return true;
        }
        return false;
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
