package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanStepdownSubcommand extends SubCommand {

    public ClanStepdownSubcommand() {
        super("clan", "stepdown", List.of("success", "only-owner", "stepdown-notification"));
        hasToBePlayer = true;
        maxArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        return () ->{
            clansHandler.stepDownPlayer(player.getUniqueId());
            sendFormattedMessage(sender, messages.get("success"), LogType.FINE);
            List<UUID> members = clansHandler.getClanMembersUUIDs(player.getUniqueId());
            for (UUID member : members) {
                Player p = Bukkit.getPlayer(member);
                if (p != null && p.isOnline() && !p.equals(player))
                    sendFormattedMessage(p, messages.get("stepdown-notification"), LogType.NULL);
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
            sender.sendMessage(VClans.getInstance().getDefaultMessage(error));
            return true;
        }
        if (clan.getOwners().size() == 1) {
            sender.sendMessage(messages.get("only-owner"));
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

        replacements.put("{player-name}", playerName);
        replacements.put("{clan-name}", clanName);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
