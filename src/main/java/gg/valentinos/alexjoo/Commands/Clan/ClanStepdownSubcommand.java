package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class ClanStepdownSubcommand extends SubCommand {

    public ClanStepdownSubcommand() {
        super("clan", "stepdown");
        hasToBePlayer = true;
        maxArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        return () ->{
            String error = clansHandler.stepDownPlayer(player.getUniqueId());
            handleCommandResult(sender, error, config.getString(configPath + "messages.success"));
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
