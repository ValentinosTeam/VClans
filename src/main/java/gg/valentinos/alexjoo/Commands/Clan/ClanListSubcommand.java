package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class ClanListSubcommand extends SubCommand {

    public ClanListSubcommand() {
        super("clan", "list", List.of("clan-list-header", "clan-list-item", "clan-list-footer", "member-list-header", "member-list-item", "member-list-footer", "clan-not-found"));
        maxArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        StringBuilder sb = new StringBuilder();

        if (args.length == 1) {
            sb.append(messages.get("clan-list-header")).append("\n");
            for (String clanName : clanHandler.getClanNames())
                sb.append(messages.get("clan-list-item").replace("{clan-name}", clanName)).append("\n");
            sb.append(messages.get("clan-list-footer"));
        } else {
            String clanName = args[1];
            Clan clan = clanHandler.getClanByName(clanName);
            sb.append(messages.get("member-list-header").replace("{clan}", clanName)).append("\n");
            List<UUID> members = clanHandler.getClanMemberUUIDs(clanName);
            for (UUID member : members) {
                OfflinePlayer player = sender.getServer().getOfflinePlayer(member);
                if (player.getName() != null)
                    sb.append(messages.get("member-list-item")
                            .replace("{player-name}", player.getName())
                            .replace("{rank}", clanHandler.getMemberRankTitle(player.getUniqueId()))
                            .replace("{priority}", clan.getRank(player.getUniqueId()).getPriority() + ""))
                            .append("\n");
            }
            sb.append(messages.get("member-list-footer"));
        }
        return () -> sendFormattedMessage(sender, sb.toString(), LogType.FINE);
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        if (args.length == 2 && !clanHandler.clanExists(args[1])) {
            sendFormattedMessage(sender, messages.get("clan-not-found"), LogType.WARNING);
            return true;
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("list");
        } else if (args.length == 2) {
            return clanHandler.getClanNames();
        }
        return List.of();
    }

}
