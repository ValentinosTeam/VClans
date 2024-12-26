package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class ClanStepdownSubcommand implements SubCommand {
    @Override
    public String getName() {
        return "stepdown";
    }

    @Override
    public String getDescription() {
        return "Step down from your clan leadership.";
    }

    @Override
    public String getUsage() {
        return "/clan stepdown";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length > 1) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }

        String error = clansHandler.stepDownPlayer(player.getUniqueId());
        player.sendMessage(Objects.requireNonNullElseGet(error, () -> "You stepped down from your clan leadership."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
