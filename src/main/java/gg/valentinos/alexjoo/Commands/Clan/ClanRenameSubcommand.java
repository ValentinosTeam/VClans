package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ClanRenameSubcommand extends SubCommand {

    private final int maxNameLength;
    private final int minNameLength;

    public ClanRenameSubcommand() {
        super("clan", "rename", List.of("success", "too-big", "too-small", "forbidden-formatting", "invalid-characters"));
        hasToBePlayer = true;
        minArgs = 2;
        maxNameLength = config.getInt("settings.max-clan-name-length");
        minNameLength = config.getInt("min-clan-name-length");
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        Clan clan = clanHandler.getClanByMember(uuid);

        return () -> {
            if (args.length >= 2) {
                String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                clanHandler.setClanName(clan, input);
                sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            } else { // show current prefix
                sender.sendMessage(clan.getName());
            }
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        Clan clan = clanHandler.getClanByMember(playerUUID);
        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.WARNING);
            return true;
        }

        if (args.length < 2) return false;
        String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String stripped = input.replaceAll("&[0-9a-fk-orK-OR]", "");

        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_RENAME)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }

        if (stripped.length() > maxNameLength) {
            sendFormattedPredefinedMessage(sender, "too-big", LogType.WARNING);
            return true;
        }

        if (stripped.length() < minNameLength) {
            sendFormattedPredefinedMessage(sender, "too-small", LogType.WARNING);
            return true;
        }

        if (!stripped.matches("[a-zA-Z0-9_ -]+")) {
            sendFormattedPredefinedMessage(sender, "invalid-characters", LogType.WARNING);
            return true;
        }

        if (input.matches("(?i).*(&[k-o0]).*")) {
            sendFormattedPredefinedMessage(sender, "forbidden-formatting", LogType.WARNING);
            return true;
        }

        return false;
    }

    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            return clanHandler.hasPermission(player, ClanRankPermission.CAN_RENAME);
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String name = "ERROR";
        String maxLength = String.valueOf(maxNameLength);
        String minLength = String.valueOf(minNameLength);

        if (args.length > 1) {
            name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }

        replacements.put("{name}", name);
        replacements.put("{max-length}", maxLength);
        replacements.put("{min-length}", maxLength);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("rename");
        } else if (args.length == 2) {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            if (clan == null) return List.of();
            return List.of(clan.getId());
        } else {
            return List.of();
        }
    }
}
