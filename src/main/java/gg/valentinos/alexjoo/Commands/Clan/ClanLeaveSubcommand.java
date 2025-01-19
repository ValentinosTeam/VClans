package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class ClanLeaveSubcommand extends SubCommand {

    public ClanLeaveSubcommand() {
        super("clan", "leave");
        hasToBePlayer = true;
        maxArgs = 1;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (commonChecks(sender, args)) return true;

        Player player = (Player) sender;

        if (isOnCooldown(sender, selfCooldownQuery)) return true;

        String error = clansHandler.leaveClan(player.getUniqueId());

        handleCommandResult(sender, error, config.getString(configPath + "messages.success"));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
