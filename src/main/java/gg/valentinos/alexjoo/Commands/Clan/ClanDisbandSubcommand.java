package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanDisbandSubcommand extends SubCommand {

    public ClanDisbandSubcommand() {
        super("clan", "disband", List.of("success", "not-in-clan", "not-leader", "not-only-owner"));
        hasToBePlayer = true;
        maxArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        return () -> {
            String clanName = clansHandler.getClans().getClanByOwner(player.getUniqueId()).getName();
            clansHandler.disbandClan(player.getUniqueId());
            sender.sendMessage(messages.get("success").replace("{clan}", clanName));
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Clan clan = clansHandler.getClans().getClanByMember(playerUUID);
        String error = clansHandler.getPlayerIsOwnerErrorKey(playerUUID, clan);
        if (error != null) {
            player.sendMessage(VClans.getInstance().getDefaultMessage(error));
            return true;
        }
        if (clan.getOwners().size() > 1){
            player.sendMessage(messages.get("not-only-owner"));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
