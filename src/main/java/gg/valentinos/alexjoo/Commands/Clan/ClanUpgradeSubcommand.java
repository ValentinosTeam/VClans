package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ClanUpgradeSubcommand extends SubCommand {
    public ClanUpgradeSubcommand() {
        super("clan", "upgrade", List.of("success", "max-tier", "cant-afford"));
        hasToBePlayer = true;
        requiredArgs = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());

        return () -> {
            sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            VClans.getInstance().getClanHandler().upgradeClan(clan);
        };
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("upgrade");
        } else {
            return List.of();
        }
    }
    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        Clan clan = clanHandler.getClanByMember(playerUUID);
        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.INFO);
            return true;
        }
        HashMap<String, Boolean> permissions = clan.getRank(playerUUID).getPermissions();
        if (!permissions.get("canUpgrade")) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.INFO);
            return true;
        }
        if (VClans.getInstance().getClanTierHandler().getHighestTierNumber() <= clan.getTier()) {
            sendFormattedPredefinedMessage(sender, "max-tier", LogType.INFO);
            return true;
        }
        if (!VClans.getInstance().getClanTierHandler().canAffordUpgrade(player, clan.getTier() + 1)) {
            sendFormattedPredefinedMessage(sender, "cant-afford", LogType.WARNING);
            return true;
        }

        return false;
    }
    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            return clan != null && clan.getRank(player.getUniqueId()).getPermissions().get("canUpgrade");
        }
        return false;
    }
    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String tierLabel = "ERROR";
        String tierPrice = "ERROR";

        if (sender instanceof Player player) {

            Clan clan = clanHandler.getClanByMember(player.getUniqueId());

            if (clan != null) {
                tierPrice = String.valueOf(VClans.getInstance().getClanTierHandler().getPrice(clan.getTier() + 1));
                tierLabel = String.valueOf(VClans.getInstance().getClanTierHandler().getLabel(clan.getTier() + 1));
            }
        }

        replacements.put("{tier-label}", tierLabel);
        replacements.put("{tier-price}", tierPrice);
    }
}
