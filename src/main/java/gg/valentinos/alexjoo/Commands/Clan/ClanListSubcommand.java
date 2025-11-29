package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
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
            for (Clan clan : clanHandler.getClans())
                sb.append(messages.get("clan-list-item")
                                .replace("{clan-name}", clan.getName())
                                .replace("{clan-id}", clan.getId())
                                .replace("{clan-prefix}", clan.getPrefix()))
                        .append("\n");
            sb.append(messages.get("clan-list-footer"));
        } else {
            String clanId = args[1];
            Clan clan = clanHandler.getClanById(clanId);
            sb.append(messages.get("member-list-header").replace("{clan-name}", clanId)).append("\n");
            //List<UUID> members = clanHandler.getClanMemberUUIDs(clanName);
            List<UUID> members = clan.getMembersSortedByPriority();
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
    public boolean suggestCommand(CommandSender sender) {
        return true;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String clanId = "ERROR";

        if (args.length == 2) {
            clanId = args[1];
        }

        replacements.put("{clan-id}", clanId);

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("list");
        } else if (args.length == 2) {
            return clanHandler.getClans().getClans().stream().map(Clan::getId).toList();
        }
        return List.of();
    }

}
