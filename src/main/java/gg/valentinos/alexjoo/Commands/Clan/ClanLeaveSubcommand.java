package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClanLeaveSubcommand extends SubCommand {

    public ClanLeaveSubcommand() {
        super("clan", "leave", List.of("success", "owner-cant-leave"));
        hasToBePlayer = true;
        maxArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        return () -> {
            clansHandler.leaveClan(player.getUniqueId());
            sender.sendMessage(messages.get("success"));
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Clan clan = clansHandler.getClans().getClanByMember(playerUUID);
        if (clan == null) {
            sender.sendMessage(VClans.getInstance().getDefaultMessage("not-in-clan"));
            return true;
        }
        if (clan.isOwner(playerUUID)) {
            sender.sendMessage(messages.get("owner-cant-leave"));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
