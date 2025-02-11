package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanInviteSubcommand extends SubCommand {

    public ClanInviteSubcommand() {
        super("clan", "invite", List.of("success", "invitation", "invite-self", "not-owner", "already-in-a-clan", "already-in-the-clan", "already-invited"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String targetName = args[1];

        return () -> {
            clansHandler.invitePlayer(player.getUniqueId(), targetName);
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
        if (target.equals(player)) {
            sendFormattedMessage(sender, messages.get("invite-self"), LogType.WARNING);
            return true;
        }

        Clan clan = clansHandler.getClans().getClanByOwner(playerUUID);
        String error = clansHandler.getPlayerIsOwnerErrorKey(playerUUID, clan);
        if (error != null) {
            sendFormattedMessage(sender, VClans.getInstance().getDefaultMessage(error), LogType.WARNING);
            return true;
        }
        if (clan.getInvites().contains(target.getUniqueId())) {
            sendFormattedMessage(sender, messages.get("already-invited"), LogType.WARNING);
            return true;
        }
        if (clansHandler.getClans().getClanByMember(target.getUniqueId()) != null) {
            sendFormattedMessage(sender, messages.get("already-in-a-clan"), LogType.WARNING);
            return true;
        }
        if (clan.getMembers().contains(target.getUniqueId())) {
            sendFormattedMessage(sender, messages.get("already-in-the-clan"), LogType.WARNING);
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
            Clan clan = clansHandler.getClans().getClanByOwner(player.getUniqueId());
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
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName).filter(name -> !name.equals(sender.getName())).toList();
        } else {
            return List.of();
        }
    }

}
