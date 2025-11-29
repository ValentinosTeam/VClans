package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanDisbandSubcommand extends SubCommand {

    public ClanDisbandSubcommand() {
        super("clan", "disband", List.of("success", "disband-notification", "no-permission"));
        hasToBePlayer = true;
        maxArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        return () -> {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            List<UUID> memberUUIDs = clan.getMemberUUIDs();
            for (UUID memberUUID : memberUUIDs) {
                Player member = VClans.getInstance().getServer().getPlayer(memberUUID);
                if (member != null && member.isOnline()) {
                    sendFormattedMessage(member, messages.get("disband-notification"), LogType.FINE);
                    VClans.getInstance().getVaultHandler().removePlayerPrefix(member);
                }
            }
            sendFormattedMessage(player, messages.get("success"), LogType.FINE);
            cooldownHandler.createCooldown(player.getUniqueId(), selfCooldownQuery, cooldownDuration);
            clanHandler.disbandClan(player.getUniqueId());
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Clan clan = clanHandler.getClanByMember(playerUUID);
        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.WARNING);
            return true;
        }
        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_DISBAND)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        if (warHandler.isInWar(clan)) {
            sendFormattedPredefinedMessage(sender, "is-in-war", LogType.WARNING);
            return true;
        }
        return false;
    }

    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            return clanHandler.hasPermission(player, ClanRankPermission.CAN_DISBAND);
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String playerName = "ERROR";
        String clanName = "ERROR";
        if (sender instanceof Player player) {
            playerName = player.getName();
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            if (clan != null)
                clanName = clan.getName();
        }
        replacements.put("{clan-name}", clanName);
        replacements.put("{player-name}", playerName);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
