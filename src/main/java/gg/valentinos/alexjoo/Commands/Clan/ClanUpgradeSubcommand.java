package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ClanTierHandler;
import gg.valentinos.alexjoo.Handlers.VaultHandler;
import gg.valentinos.alexjoo.Utility.Decorator;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanUpgradeSubcommand extends SubCommand {
    public ClanUpgradeSubcommand() {
        super("clan", "upgrade", List.of("success", "max-tier", "cant-afford", "clan-upgraded"));
        hasToBePlayer = true;
        requiredArgs = 1;
        successSound = Key.key("minecraft:block.enchantment_table.use");
        successVolume = 1;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());

        return () -> {
            sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            for (Player member : clan.getOnlinePlayers()) {
                if (member.getUniqueId().equals(player.getUniqueId())) continue;
                sendFormattedPredefinedMessage(member, "clan-upgraded", LogType.FINE);
                Decorator.PlaySound(member, successSound, successVolume);
            }
            VClans.getInstance().getClanHandler().upgradeClan(clan);
            VaultHandler vaultHandler = VClans.getInstance().getVaultHandler();
            if (vaultHandler.getEconomy() != null) {
                ClanTierHandler clanTierHandler = VClans.getInstance().getClanTierHandler();
                vaultHandler.withdrawPlayer(player, clanTierHandler.getPrice(clan.getTier()));
            }
        };
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("upgrade");
        }
        return List.of();
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
        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_UPGRADE)) {
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
        if (warHandler.isInWar(clan)) {
            sendFormattedPredefinedMessage(sender, "is-in-war", LogType.WARNING);
            return true;
        }

        return false;
    }
    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            return clanHandler.hasPermission(player, ClanRankPermission.CAN_UPGRADE);
        }
        return false;
    }
    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String tierLabel = "ERROR";
        String tierPrice = "ERROR";

        if (sender instanceof Player player) {

            Clan clan = clanHandler.getClanByMember(player.getUniqueId());

            if (clan != null && VClans.getInstance().getClanTierHandler().getHighestTierNumber() > clan.getTier()) {
                tierPrice = String.valueOf(VClans.getInstance().getClanTierHandler().getPrice(clan.getTier() + 1));
                tierLabel = String.valueOf(VClans.getInstance().getClanTierHandler().getLabel(clan.getTier() + 1));
            }
        }

        replacements.put("{tier-label}", tierLabel);
        replacements.put("{tier-price}", tierPrice);
    }
}
