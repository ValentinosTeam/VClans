package gg.valentinos.alexjoo.Commands.War;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Data.WarData.PeaceTreaty;
import gg.valentinos.alexjoo.Data.WarData.War;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static gg.valentinos.alexjoo.VClans.Log;

public class WarPeaceSubcommand extends SubCommand {

    public WarPeaceSubcommand() {
        super("war", "peace", List.of("success", "not-in-war", "no-peace-treaty", "cant-accept-own-treaty", "treaty-creator-cant-pay", "acceptor-cant-pay", "peace-treaty-accepted", "peace-treaty-declined", "peace-treaty-offer", "peace-treaty-request", "invalid-amount"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan playerClan = clanHandler.getClanByMember(player.getUniqueId());
        Clan otherClan = warHandler.getWarEnemyClan(playerClan);
        War war = warHandler.getWar(playerClan);

        String arg = args[1].toLowerCase();

        if (arg.equals("accept")) {
            return () -> {
                warHandler.acceptPeaceTreaty(player, war);
                for (Player clanMember : playerClan.getOnlinePlayers()) {
                    sendFormattedPredefinedMessage(clanMember, "peace-treaty-accepted", LogType.NULL);
                }
                for (Player clanMember : otherClan.getOnlinePlayers()) {
                    sendFormattedPredefinedMessage(clanMember, "peace-treaty-accepted", LogType.NULL);
                }
            };
        } else if (arg.equals("decline")) {
            return () -> {
                warHandler.declinePeaceTreaty(war);
                for (Player clanMember : playerClan.getOnlinePlayers()) {
                    sendFormattedPredefinedMessage(clanMember, "peace-treaty-declined", LogType.NULL);
                }
                for (Player clanMember : otherClan.getOnlinePlayers()) {
                    sendFormattedPredefinedMessage(clanMember, "peace-treaty-declined", LogType.NULL);
                }
            };
        } else {
            return () -> {
                try {
                    int amount = Integer.parseInt(arg);
                    warHandler.createPeaceTreaty(player, war, amount);
                    List<Player> players = playerClan.getOnlinePlayers();
                    players.addAll(otherClan.getOnlinePlayers());
                    if (amount >= 0) {
                        for (Player clanMember : players) {
                            sendFormattedPredefinedMessage(clanMember, "peace-treaty-request", LogType.NULL);
                        }
                    } else {
                        for (Player clanMember : players) {
                            sendFormattedPredefinedMessage(clanMember, "peace-treaty-offer", LogType.NULL);
                        }
                    }
                } catch (NumberFormatException e) {
                    Log("Something really wrong went during the execution of this command!\n" + sender + Arrays.toString(args), LogType.SEVERE);
                }
            };
        }
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("peace");
        } else if (args.length == 2) {
            if (!(sender instanceof Player player)) return List.of();
            {
                Clan clan = clanHandler.getClanByMember(player.getUniqueId());
                if (clan == null) return List.of();
                War war = warHandler.getWar(clan);
                if (war == null) return List.of();
                if (war.getPeaceTreaty() == null) {
                    return List.of("1000", "-1000", "0");
                } else {
                    Clan receivingClan = war.getPeaceTreaty().getTargetClan();
                    if (receivingClan.getId().equals(clan.getId())) {
                        return List.of("accept", "decline");
                    }
                }
            }
        }
        return List.of();
    }
    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());

        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.WARNING);
            return true;
        }
        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_OFFER_PEACE)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        War war = warHandler.getWar(clan);
        if (war == null) {
            sendFormattedPredefinedMessage(sender, "not-in-war", LogType.WARNING);
            return true;
        }
        String arg = args[1].toLowerCase();
        PeaceTreaty peaceTreaty = war.getPeaceTreaty();
        if (arg.equals("accept") || arg.equals("decline")) {
            if (peaceTreaty == null) {
                sendFormattedPredefinedMessage(sender, "no-peace-treaty", LogType.WARNING);
                return true;
            }
            if (arg.equals("accept")) {
                if (Objects.equals(peaceTreaty.getCreatorClan().getId(), clan.getId())) {
                    sendFormattedPredefinedMessage(sender, "cant-accept-own-treaty", LogType.WARNING);
                    return true;
                }
                if (!peaceTreaty.canCreatorPay()) {
                    sendFormattedPredefinedMessage(sender, "treaty-creator-cant-pay", LogType.WARNING);
                    return true;
                }
                if (!peaceTreaty.canTargetPay(player)) {
                    sendFormattedPredefinedMessage(sender, "acceptor-cant-pay", LogType.WARNING);
                    return true;
                }
            }
            return false;
        }

        boolean isInt;
        try {
            Integer.parseInt(arg);
            isInt = true;
        } catch (NumberFormatException e) {
            isInt = false;
        }
        if (!isInt) {
            sendFormattedPredefinedMessage(sender, "invalid-amount", LogType.WARNING);
            return true;
        }
        return false;
    }
    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            if (clan == null) return false;
            War war = warHandler.getWar(clan);
            if (war == null) return false;
            return clanHandler.hasPermission(player, ClanRankPermission.CAN_OFFER_PEACE);
        }
        return false;
    }
    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String creatorName = "ERROR";
        String amountString = "ERROR";

        if (sender instanceof Player player) {
            if (args.length > 1) {
                String arg = args[1].toLowerCase();
                if (arg.equals("accept") || arg.equals("decline")) {
                    Clan clan = clanHandler.getClanByMember(player.getUniqueId());
                    if (clan == null) return;
                    War war = warHandler.getWar(clan);
                    if (war == null) return;
                    PeaceTreaty peaceTreaty = war.getPeaceTreaty();
                    if (peaceTreaty == null) return;
                    creatorName = peaceTreaty.getCreator().getName();
                    int amount = peaceTreaty.getAmountOffered();
                    if (amount == 0) amount = peaceTreaty.getAmountRequested();
                    amountString = String.valueOf(amount);
                } else {
                    int amount = 0;
                    try {
                        amount = Integer.parseInt(arg);
                        if (amount >= 0) {
                            amountString = "" + amount;
                        } else {
                            amountString = "" + -amount;
                        }
                        creatorName = player.getName();
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        replacements.put("{creator}", creatorName);
        replacements.put("{amount}", amountString);
    }
}
