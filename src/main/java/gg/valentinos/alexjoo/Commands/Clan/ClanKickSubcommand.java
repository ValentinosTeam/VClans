package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClanKickSubcommand implements SubCommand {
    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public String getDescription() {
        return "Kicks a member from your clan.";
    }

    @Override
    public String getUsage() {
        return "/clan kick <player>";
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
        }
        String error = clansHandler.kickPlayer(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(Objects.requireNonNullElseGet(error, () -> "Kicked " + target.getName() + " from your clan."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("kick");
        } else if (args.length == 2) {
            return clansHandler.getMembersOfPlayerClan(((Player) sender).getUniqueId())
                    .stream()
                    .map(Bukkit::getOfflinePlayer)
                    .map(OfflinePlayer::getName)
                    .filter(Objects::nonNull)
                    .filter(name -> !name.equals(sender.getName()))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }
}
