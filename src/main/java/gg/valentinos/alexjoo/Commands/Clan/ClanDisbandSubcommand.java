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

public class ClanDisbandSubcommand extends SubCommand {

    public ClanDisbandSubcommand() {
        super("clan", "disband", List.of("success", "not-in-clan", "not-leader", "not-only-owner"));
        hasToBePlayer = true;
        maxArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        return () -> {
            List<UUID> members = clansHandler.getClans().getClanByOwner(player.getUniqueId()).getMembers();
            clansHandler.disbandClan(player.getUniqueId());
            sendFormattedMessage(sender, messages.get("success"), LogType.FINE);
            for (UUID member : members) {
                Player memberPlayer = VClans.getInstance().getServer().getPlayer(member);
                if (memberPlayer != null) {
                    sendFormattedMessage(memberPlayer, messages.get("disband-notification"), LogType.NULL);
                }
            }
            cooldownHandler.createCooldown(player.getUniqueId(), selfCooldownQuery, cooldownDuration);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Clan clan = clansHandler.getClans().getClanByMember(playerUUID);
        String error = clansHandler.getPlayerIsOwnerErrorKey(playerUUID, clan);
        if (error != null) {
            sendFormattedMessage(sender, VClans.getInstance().getDefaultMessage(error), LogType.WARNING);
            return true;
        }
        if (clan.getOwners().size() > 1){
            sendFormattedMessage(sender, messages.get("not-only-owner"), LogType.WARNING);
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
            Clan clan = clansHandler.getClans().getClanByOwner(player.getUniqueId());
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
