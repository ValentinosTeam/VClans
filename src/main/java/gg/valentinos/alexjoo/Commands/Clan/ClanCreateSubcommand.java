package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanCreateSubcommand implements SubCommand {
    private final static String cooldownQuery = "ClanCreate";
    private final static long cooldownDuration = 300;

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Creates a new clan.";
    }

    @Override
    public String getUsage() {
        return "/clan create <name>";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: " + getUsage());
            return true;
        }
        UUID playerUUID = ((Player) sender).getUniqueId();

        if (cooldownHandler.isOnCooldown(playerUUID, cooldownQuery)) {
            String timeLeft = cooldownHandler.getTimeLeft(playerUUID, cooldownQuery);
            sender.sendMessage("This command is on cooldown. " + timeLeft + " left.");
            return true;
        }

        String clanName = args[1];
        String error = clansHandler.createClan(((Player) sender).getUniqueId(), clanName);

        if (error == null) {
            cooldownHandler.createCooldown(playerUUID, cooldownQuery, cooldownDuration);
            sender.sendMessage("Clan " + clanName + " created successfully!");
        } else {
            sender.sendMessage(error);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
