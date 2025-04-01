package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.ClanRank;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.GUIs.RankGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

import static gg.valentinos.alexjoo.VClans.SendMessage;

public class ClanRankSubcommand extends SubCommand {

    public ClanRankSubcommand() {
        super("clan", "rank", List.of("success", "target-not-in-clan", "rank-not-found", "rank-priority-low", "rank-changed"));
        hasToBePlayer = true;
        maxArgs = 3;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());
        return () -> {
            if (args.length == 1) {
                RankGui rankGui = new RankGui();
                rankGui.openInventory(player);
            }
            else if (args.length == 2) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                ClanRank rank = clan.getRank(target.getUniqueId());

                Component component = Component.text("Rank information for " + target.getName() + ":").append(Component.newline());
                component = component.append(rank.getRankInfo());
                SendMessage(player, component, LogType.NULL);
            }
            else if (args.length == 3) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                clanHandler.assignRank(target.getUniqueId(), args[2]);
                sendFormattedPredefinedMessage(sender, "success", LogType.INFO);
                if (target.isOnline()) {
                    Player targetPlayer = (Player) target;
                    sendFormattedPredefinedMessage(targetPlayer, "rank-changed", LogType.INFO);
                }

            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());
        if (clan == null) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of("rank");
        }
        else if (args.length == 2) {
            List<UUID> memberUUIDs = clan.getMemberUUIDs();
            List<String> memberNames = new ArrayList<>();
            for (UUID uuid : memberUUIDs) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
                if (uuid != player.getUniqueId()) {
                    memberNames.add(target.getName());
                }
            }
            return memberNames;
        }
        else if (args.length == 3) {
            return new ArrayList<>(clan.getRanks().keySet());
        }
        return List.of();
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Clan clan = clanHandler.getClanByMember(playerUUID);
        if (args.length == 1) {     //clan rank - opens gui
            if (clan == null) {
                sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.WARNING);
                return true;
            }
            return false;
        }
        else if (args.length > 1 && args.length <= maxArgs) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (!clan.isPlayerMember(target.getUniqueId())) {
                // the target is not in this clan
                sendFormattedPredefinedMessage(sender, "target-not-in-clan", LogType.WARNING);
                return true;
            }
            if (args.length == 3) {    //clan rank <player> <rank> - assigns rank
                ClanRank rank = clan.getRankById(args[2]);
                if (rank == null) {
                    // the rank does not exist
                    sendFormattedPredefinedMessage(sender, "rank-not-found", LogType.WARNING);
                    return true;
                }
                if (!clan.getRank(playerUUID).canChangeRank()) {
                    // the player does not have permission to change ranks
                    sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
                    return true;
                }
                if (clan.getRank(playerUUID).getPriority() <= rank.getPriority()) {
                    // the players rank is lower than the rank he is trying to assign
                    sendFormattedPredefinedMessage(sender, "rank-priority-low", LogType.WARNING);
                    return true;
                }
                if (clan.getRank(playerUUID).getPriority() <= clan.getRank(target.getUniqueId()).getPriority()) {
                    // the players rank is lower than the targets rank
                    sendFormattedPredefinedMessage(sender, "rank-priority-low", LogType.WARNING);
                    return true;
                }
                return false;
            }
            return false;
        }
        return true;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String targetName = "ERROR";
        String rankName = "ERROR";
        if (args.length > 1){
            targetName = args[1];
        }
        if (args.length > 2){
            rankName = args[2];
        }
        replacements.put("target-name", targetName);
        replacements.put("rank", rankName);
    }
}
