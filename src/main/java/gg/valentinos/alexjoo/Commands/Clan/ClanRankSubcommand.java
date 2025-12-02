package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRank;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.GUIs.RankGui;
import gg.valentinos.alexjoo.Utility.Decorator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            } else if (args.length == 2) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                ClanRank rank = clan.getRank(target.getUniqueId());

                Component component = Component.text("Rank information for " + target.getName() + ":").append(Component.newline());
                component = component.append(clan.getRankInfo(rank));
                SendMessage(player, component, LogType.NULL);
            } else if (args.length == 3) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                clanHandler.assignRank(target.getUniqueId(), args[2]);
                sendFormattedPredefinedMessage(sender, "success", LogType.INFO);
                if (target.isOnline()) {
                    Player targetPlayer = (Player) target;
                    sendFormattedPredefinedMessage(targetPlayer, "rank-changed", LogType.INFO);
                    Decorator.PlaySound(targetPlayer, Key.key("minecraft:block.note_block.pling"), 1f);
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
        } else if (args.length == 2) {
            List<UUID> memberUUIDs = clan.getMemberUUIDs();
            List<String> memberNames = new ArrayList<>();
            for (UUID uuid : memberUUIDs) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
                if (uuid != player.getUniqueId()) {
                    memberNames.add(target.getName());
                }
            }
            return memberNames;
        } else if (args.length == 3) {
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
        } else if (args.length > 1 && args.length <= maxArgs) {
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
                if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_CHANGE_RANK)) {
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
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            return clan != null;
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String playerName = "ERROR";
        String targetName = "ERROR";
        String rankName = "ERROR";
        if (sender instanceof Player player) {
            playerName = player.getName();
        }
        if (args.length > 1) {
            targetName = args[1];
        }
        if (args.length > 2) {
            rankName = args[2];
        }
        replacements.put("{player-name}", playerName);
        replacements.put("{target-name}", targetName);
        replacements.put("{rank}", rankName);
    }
}
