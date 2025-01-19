package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class ClanListSubcommand extends SubCommand {

    public ClanListSubcommand() {
        super("clan", "list");
        maxArgs = 2;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (commonChecks(sender, args)) return true;

        if (isOnCooldown(sender, selfCooldownQuery)) return true;

        if (args.length == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(config.getString(configPath + "messages.clan-list-header")).append("\n");
            for (String clanName : clansHandler.getClanList()) {
                sb.append(config.getString(configPath + "messages.list-item").replace("{item}", clanName)).append("\n");
            }
            sb.append(config.getString(configPath + "messages.clan-list-footer"));
            sender.sendMessage(sb.toString());
        } else {
            String clanName = args[1];
            StringBuilder sb = new StringBuilder();
            sb.append(config.getString(configPath + "messages.member-list-header").replace("{clan}", clanName)).append("\n");
            List<UUID> members = clansHandler.getClanMemberUUIDs(clanName);
            for (UUID member : members) {
                OfflinePlayer player = sender.getServer().getOfflinePlayer(member);
                sb.append(config.getString(configPath + "messages.list-item").replace("{item}", player.getName())).append("\n");
            }
            sb.append(config.getString(configPath + "messages.member-list-footer"));
            sender.sendMessage(sb.toString());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("list");
        } else if (args.length == 2) {
            return clansHandler.getClanList();
        }
        return List.of();
    }
}
