package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClanJoinSubcommand implements SubCommand {
    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Joins you to a clan if you were invited.";
    }

    @Override
    public String getUsage() {
        return "/clan join <clan>";
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

        String error = clansHandler.joinClan(player.getUniqueId(), args[1]);
        if (error != null) {
            player.sendMessage(error);
        } else {
            player.sendMessage("Joined clan " + args[1] + " successfully!");
            List<UUID> clanMembers = clansHandler.getClanMemberUUIDs(args[1]);
            for (UUID uuid : clanMembers) {
                if (uuid.equals(player.getUniqueId())) {
                    continue;
                }
                OfflinePlayer memberPlayer = sender.getServer().getOfflinePlayer(uuid);
                if (memberPlayer.isOnline()) {
                    Objects.requireNonNull(memberPlayer.getPlayer()).sendMessage(player.getName() + " has joined the clan!");
                }
            }
        }
        player.sendMessage(Objects.requireNonNullElseGet(error, () -> "Joined clan " + args[1] + " successfully!"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
