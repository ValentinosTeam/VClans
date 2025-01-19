package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ClanInviteSubcommand extends SubCommand {

    public ClanInviteSubcommand() {
        super("clan", "invite");
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (commonChecks(sender, args)) return true;

        Player player = (Player) sender;
        String targetName = args[1];
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);

        if (isOnCooldown(sender, selfCooldownQuery)) return true;

        String error = clansHandler.invitePlayer(player.getUniqueId(), targetName);

        handleCommandResult(sender, error, () -> {
            sender.sendMessage(config.getString(configPath + "messages.success").replace("{name}", targetName));
            if (error == null && target.isOnline()) {
                target.getPlayer().sendMessage(
                        config.getString(configPath + "messages.invitation")
                                .replace("{clan}", clansHandler.getClanNameOfMember(player.getUniqueId()))
                                .replace("{name}", player.getName())
                );
            }
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("invite");
        } else if (args.length == 2) {
            return sender.getServer().getOnlinePlayers().stream().map(Player::getName).filter(name -> !name.equals(sender.getName())).toList();
        } else {
            return List.of();
        }
    }
}
