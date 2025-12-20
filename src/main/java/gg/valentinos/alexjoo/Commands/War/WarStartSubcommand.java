package gg.valentinos.alexjoo.Commands.War;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Data.WarData.War;
import gg.valentinos.alexjoo.Data.WarData.WarState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WarStartSubcommand extends SubCommand {

    public WarStartSubcommand() {
        super("war", "start", List.of("not-in-war", "not-in-grace", "skip-too-late", "not-defender", "grace-got-shortened"));
        hasToBePlayer = true;
        requiredArgs = 1;
    }
    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan playerClan = clanHandler.getClanByMember(player.getUniqueId());
        Clan otherClan = warHandler.getWarEnemyClan(playerClan);
        War war = warHandler.getWar(playerClan);

        return () -> {
            warHandler.skipForwardGrace(war);
            for (Player clanMember : playerClan.getOnlinePlayers()) {
                sendFormattedPredefinedMessage(clanMember, "grace-got-shortened", LogType.NULL);
            }
            for (Player clanMember : otherClan.getOnlinePlayers()) {
                sendFormattedPredefinedMessage(clanMember, "grace-got-shortened", LogType.NULL);
            }
        };
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("start");
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
        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_DECLARE_WAR)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        War war = warHandler.getWar(clan);
        if (war == null) {
            sendFormattedPredefinedMessage(sender, "not-in-war", LogType.WARNING);
            return true;
        }
        if (war.getState() != WarState.DECLARED) {
            sendFormattedPredefinedMessage(sender, "not-in-grace", LogType.WARNING);
            return true;
        }
        if (warHandler.getTimeLeftInSeconds(war) <= warHandler.GRACE_SKIP_TILL) {
            sendFormattedPredefinedMessage(sender, "skip-too-late", LogType.WARNING);
            return true;
        }
        if (!war.getTargetClanId().equals(clan.getId())) {
            sendFormattedPredefinedMessage(sender, "not-defender", LogType.WARNING);
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
            if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_DECLARE_WAR)) return false;
            if (war.getState() != WarState.DECLARED) return false;
            if (!war.getTargetClanId().equals(clan.getId())) return false;
            return true;
        }
        return false;
    }
    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String durationString = "ERROR";
        String starterClan = "ERROR";

        durationString = warHandler.GRACE_SKIP_TILL + "";
        if (sender instanceof Player player) {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            if (clan == null) return;
            starterClan = clan.getName();
        }

        replacements.put("{duration}", durationString);
        replacements.put("{clan-name}", starterClan);
    }
}
