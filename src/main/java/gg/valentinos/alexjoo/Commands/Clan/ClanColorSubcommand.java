package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanColorSubcommand extends SubCommand {

    public ClanColorSubcommand() {
        super("clan", "color", List.of("success", "invalid-input"));
        hasToBePlayer = true;
        requiredArgs = 4;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());
        UUID playerUUID = player.getUniqueId();

        int r = Integer.parseInt(args[1]);
        int g = Integer.parseInt(args[2]);
        int b = Integer.parseInt(args[3]);
        return () -> {
            sendFormattedPredefinedMessage(player, "success", LogType.FINE);
            cooldownHandler.createCooldown(playerUUID, selfCooldownQuery, cooldownDuration);
            clanHandler.setClanColor(clan, r, g, b);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Clan clan = clanHandler.getClanByMember(playerUUID);
        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan");
            return true;
        }
        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_COLOR)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        if (!isInputValid(args)) {
            sendFormattedPredefinedMessage(sender, "invalid-input", LogType.WARNING);
            return true;
        }
        return false;
    }

    @Override
    public boolean suggestCommand(CommandSender sender) {
        Player player = (Player) sender;
        return clanHandler.hasPermission(player, ClanRankPermission.CAN_COLOR);
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length >= 2 && args.length <= 4) {
            int random = (int) (Math.random() * 256);
            return List.of(String.valueOf(random));
        }
        return List.of();
    }

    private boolean isInputValid(String[] args) {
        if (args.length != 4) return false;
        try {
            int r = Integer.parseInt(args[1]);
            int g = Integer.parseInt(args[2]);
            int b = Integer.parseInt(args[3]);

            return isInRange(r) && isInRange(g) && isInRange(b);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private boolean isInRange(int value) {
        return value >= 0 && value <= 255;
    }
}
