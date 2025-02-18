package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanLeaveSubcommand extends SubCommand {

    public ClanLeaveSubcommand() {
        super("clan", "leave", List.of("success", "leave-notification", "owner-cant-leave"));
        hasToBePlayer = true;
        maxArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        return () -> {
            clanHandler.leaveClan(player.getUniqueId());
            sendFormattedMessage(sender, messages.get("success"), LogType.FINE);
            List<UUID> members = clanHandler.getClanMembersUUIDs(player.getUniqueId());
            for (UUID member : members) {
                Player p = VClans.getInstance().getServer().getPlayer(member);
                if (p != null && p.isOnline()){
                    if (p.equals(player))
                        continue;
                    sendFormattedMessage(p, messages.get("leave-notification"), LogType.INFO);
                }
            }
            cooldownHandler.createCooldown(player.getUniqueId(), selfCooldownQuery, cooldownDuration);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Clan clan = clanHandler.getClanByMember(playerUUID);
        if (clan == null) {
            sendFormattedMessage(sender, messages.get("not-in-clan"), LogType.WARNING);
            return true;
        }
        if (clan.isPlayerOwner(playerUUID)) {
            sendFormattedMessage(sender, messages.get("owner-cant-leave"), LogType.WARNING);
            return true;
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
