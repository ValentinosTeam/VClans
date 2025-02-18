package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClanInviteSubcommand extends SubCommand {

    public ClanInviteSubcommand() {
        super("clan", "invite", List.of("success", "invitation", "invite-self", "already-in-a-clan", "already-in-the-clan", "already-invited", "no-permission", "clan-full"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String targetName = args[1];

        return () -> {
            clanHandler.invitePlayer(player.getUniqueId(), targetName);
            sendFormattedMessage(sender, messages.get("success"), LogType.FINE);
            OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);
            if (target.getPlayer() != null && target.isOnline())
                sendFormattedMessage(target.getPlayer(), messages.get("invitation"), LogType.INFO);
            cooldownHandler.createCooldown(player.getUniqueId(), selfCooldownQuery, cooldownDuration);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String targetName = args[1];
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore()) {
            sendFormattedMessage(sender, VClans.getInstance().getDefaultMessage("never-joined"), LogType.WARNING);
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            sendFormattedMessage(sender, messages.get("invite-self"), LogType.WARNING);
            return true;
        }

        Clan clan = clanHandler.getClanByMember(playerUUID);
        if (clan == null) {
            sendFormattedMessage(sender, VClans.getInstance().getDefaultMessage("not-in-a-clan"), LogType.WARNING);
            return true;
        }
        if (!clan.getRank(playerUUID).canInvite()) {
            sendFormattedMessage(sender, messages.get("no-permission"), LogType.WARNING);
            return true;
        }
        if (clan.isMemberInvited(target.getUniqueId())) {
            sendFormattedMessage(sender, messages.get("already-invited"), LogType.WARNING);
            return true;
        }
        if (clanHandler.isPlayerInAClan(target.getUniqueId())) {
            sendFormattedMessage(sender, messages.get("already-in-a-clan"), LogType.WARNING);
            return true;
        }
        if (clan.isPlayerMember(target.getUniqueId())) {
            sendFormattedMessage(sender, messages.get("already-in-the-clan"), LogType.WARNING);
            return true;
        }
        if (clan.isFull()) {
            sendFormattedMessage(sender, VClans.getInstance().getDefaultMessage("clan-full"), LogType.WARNING);
            return true;
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String playerName = "ERROR";
        String clanName = "ERROR";
        String targetName = "ERROR";

        if (sender instanceof Player player){
            playerName = player.getName();
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            if (clan != null)
                clanName = clan.getName();
        }
        if (args.length > 1) {
            targetName = args[1];
        }

        replacements.put("{target-name}", targetName);
        replacements.put("{player-name}", playerName);
        replacements.put("{clan-name}", clanName);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("invite");
        } else if (args.length == 2) {
            List<UUID> excludePlayers = new ArrayList<>();
            for (Clan clan : clanHandler.getClans())
                excludePlayers.addAll(clan.getMemberUUIDs());
            return sender.getServer().getOnlinePlayers().stream()
                    .map(Player::getUniqueId)
                    .filter(uuid -> !excludePlayers.contains(uuid))
                    .map(uuid -> VClans.getInstance().getServer().getPlayer(uuid).getName())
                    .toList();
        } else {
            return List.of();
        }
    }

}
