package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClanInviteSubcommand implements SubCommand {
    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Invite an online player to your clan.";
    }

    @Override
    public String getUsage() {
        return "/clan invite <player>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }

        OfflinePlayer target = player.getServer().getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            player.sendMessage("Player " + args[1] + " has never joined this server before.");
            return true;
        } else if (target.equals(player)) {
            player.sendMessage("You can't invite yourself to your clan.");
            return true;
        }

        String error = clansHandler.invitePlayer(player.getUniqueId(), target.getUniqueId());
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("Invited " + target.getName() + " to your clan.");
            if (target.isOnline()) {
                target.getPlayer().sendMessage("You have been invited to join " +
                        clansHandler.getClanNameOfMember(player.getUniqueId()) +
                        ". Use /clan join <clan name> to join.");
            }
        }
        return true;
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
