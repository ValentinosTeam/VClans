package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanPrefixSubcommand extends SubCommand {

    private final int maxPrefixLength;

    public ClanPrefixSubcommand() {
        super("clan", "prefix", List.of("success", "too-big", "forbidden-formatting", "invalid-characters"));
        hasToBePlayer = true;
        maxArgs = 2;
        maxPrefixLength = config.getInt("settings.max-prefix-length");
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        Clan clan = clanHandler.getClanByMember(uuid);

        return () -> {
            if (args.length == 2) { // gave prefix input = set new prefix
                String input = args[1];
                clanHandler.setClanPrefix(clan, input);
                sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            } else { // show current prefix
                sender.sendMessage(clan.getPrefix());
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

        if (args.length != 2) return false;
        String input = args[1];
        String stripped = input.replaceAll("&[0-9a-fk-orK-OR]", "");

        if (!clan.getRank(playerUUID).getPermissions().getOrDefault("canSetPrefix", false)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }

        if (stripped.length() > maxPrefixLength) {
            sendFormattedPredefinedMessage(sender, "too-big", LogType.WARNING);
            return true;
        }

        if (!stripped.matches("[a-zA-Z]+")) {
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
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            return clan != null && clan.getRank(player.getUniqueId()).getPermissions().getOrDefault("canSetPrefix", false);
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String prefix = "ERROR";
        String maxLength = String.valueOf(maxPrefixLength);
        if (args.length > 1) {
            prefix = args[1];
        }

        replacements.put("{prefix}", prefix);
        replacements.put("{max-length}", maxLength);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("prefix");
        } else if (args.length == 2) {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            if (clan == null) return List.of();
            int length = (Math.min(clan.getId().length(), maxPrefixLength));
            return List.of(clan.getId().substring(0, length));
        } else {
            return List.of();
        }
    }
}
