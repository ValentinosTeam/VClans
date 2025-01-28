package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.LogType;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class ClanListSubcommand extends SubCommand {

    public ClanListSubcommand() {
        super("clan", "list", List.of("clan-list-header", "clan-list-footer", "list-item", "member-list-header", "member-list-footer"));
        maxArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        StringBuilder sb = new StringBuilder();

        if (args.length == 1) {
            sb.append(messages.get("clan-list-header")).append("\n");
            for (String clanName : clansHandler.getClanNames()) {
                sb.append(messages.get("list-item").replace("{item}", clanName)).append("\n");
            }
            sb.append(messages.get("clan-list-footer"));
        } else {
            String clanName = args[1];
            sb.append(messages.get("member-list-header").replace("{clan}", clanName)).append("\n");
            List<UUID> members = clansHandler.getClanMemberUUIDs(clanName);
            for (UUID member : members) {
                OfflinePlayer player = sender.getServer().getOfflinePlayer(member);
                if (player.getPlayer() != null)
                    sb.append(messages.get("list-item").replace("{item}", player.getPlayer().getName())).append("\n");
            }
            sb.append(messages.get("member-list-footer"));
        }
        return () -> sendFormattedMessage(sender, sb.toString(), LogType.NULL);
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
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
            return clansHandler.getClanNames();
        }
        return List.of();
    }

}
