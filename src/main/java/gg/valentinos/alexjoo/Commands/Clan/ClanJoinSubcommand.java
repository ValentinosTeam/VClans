package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClanJoinSubcommand extends SubCommand {

    public ClanJoinSubcommand() {
        super("clan", "join");
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (commonChecks(sender, args)) return true;

        Player player = (Player) sender;
        String clanName = args[1];

        if (isOnCooldown(sender, selfCooldownQuery)) return true;

        String error = clansHandler.joinClan(player.getUniqueId(), clanName);

        handleCommandResult(sender, error, () -> {
            player.sendMessage(config.getString(configPath + "messages.success").replace("{clan}", clanName));
            List<UUID> clanMembers = clansHandler.getClanMemberUUIDs(clanName);
            for (UUID uuid : clanMembers) {
                if (uuid.equals(player.getUniqueId()))
                    continue;
                OfflinePlayer memberPlayer = sender.getServer().getOfflinePlayer(uuid);
                if (memberPlayer.isOnline()) {
                    Objects.requireNonNull(memberPlayer.getPlayer()).sendMessage(
                            config.getString(configPath + "messages.member-joined")
                                  .replace("{player}", player.getName())
                    );
                }
            }
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
