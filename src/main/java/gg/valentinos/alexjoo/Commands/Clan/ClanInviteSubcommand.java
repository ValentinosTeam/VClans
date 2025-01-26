package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
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
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);

        return () -> {
            clansHandler.invitePlayer(player.getUniqueId(), targetName);
            sender.sendMessage(messages.get("messages.success").replace("{name}", targetName));
            if (target.getPlayer() != null && target.isOnline()) {
                target.getPlayer().sendMessage(
                    messages.get("messages.invitation")
                        .replace("{clan}", clansHandler.getClans().getClanNameOfMember(player.getUniqueId()))
                        .replace("{name}", player.getName())
                );
            }
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String targetName = args[1];
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore()) {
            sender.sendMessage(VClans.getInstance().getDefaultMessage("never-joined"));
            return true;
        } else if (target.equals(player)) {
            sender.sendMessage(messages.get("invite-self"));
            return true;
        }

        Clan clan = clansHandler.getClans().getClanByOwner(playerUUID);
        String error = clansHandler.getPlayerIsOwnerErrorKey(playerUUID, clan);
        if (error != null) {
            player.sendMessage(VClans.getInstance().getDefaultMessage(error));
            return true;
        }
        if (clan.getInvites().contains(target.getUniqueId())) {
            sender.sendMessage(messages.get("already-invited"));
            return true;
        }
        if (clansHandler.getClans().getClanByMember(target.getUniqueId()) != null) {
            sender.sendMessage(messages.get("already-in-a-clan"));
            return true;
        }
        if (clan.getMembers().contains(target.getUniqueId())) {
            sender.sendMessage(messages.get("already-in-the-clan"));
            return true;
        }
        return false;
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
