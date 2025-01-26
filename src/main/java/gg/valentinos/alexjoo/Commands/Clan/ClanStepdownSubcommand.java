package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClanStepdownSubcommand extends SubCommand {

    public ClanStepdownSubcommand() {
        super("clan", "stepdown", List.of("success", "only-owner", "stepdown-notification"));
        hasToBePlayer = true;
        maxArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        return () ->{
            clansHandler.stepDownPlayer(player.getUniqueId());
            sender.sendMessage(messages.get("success"));
            List<UUID> members = clansHandler.getClanMembersUUIDs(player.getUniqueId());
            for (UUID member : members) {
                Player p = Bukkit.getPlayer(member);
                if (p != null && p.isOnline())
                    p.sendMessage(messages.get("stepdown-notification").replace("{name}", player.getName()));
            }
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Clan clan = clansHandler.getClans().getClanByMember(playerUUID);
        String error = clansHandler.getPlayerIsOwnerErrorKey(playerUUID, clan);
        if (error != null) {
            sender.sendMessage(VClans.getInstance().getDefaultMessage(error));
            return true;
        }
        if (clan.getOwners().size() == 1) {
            sender.sendMessage(messages.get("only-owner"));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
