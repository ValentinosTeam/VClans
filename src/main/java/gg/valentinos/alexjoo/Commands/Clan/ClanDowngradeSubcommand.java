package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ClanTierHandler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanDowngradeSubcommand extends SubCommand {
    public ClanDowngradeSubcommand() {
        super("clan", "downgrade", List.of("success", "min-tier"));
        hasToBePlayer = true;
        requiredArgs = 1;
        successSound = Key.key("minecraft:block.grindstone.use");
        successVolume = 1f;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());

        return () -> {
            sendFormattedPredefinedMessage(sender, "success", LogType.FINE);
            VClans.getInstance().getClanHandler().downgradeClan(clan);
        };
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("downgrade");
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
        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_DOWNGRADE)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.INFO);
            return true;
        }
        if (clan.getTier() == 0) {
            sendFormattedPredefinedMessage(sender, "min-tier", LogType.INFO);
            return true;
        }
        ClanTierHandler clanTierHandler = VClans.getInstance().getClanTierHandler();
        if (clan.getMembers().size() > clanTierHandler.getPlayerLimit(clan.getTier() - 1)) {
            sendFormattedPredefinedMessage(sender, "need-to-kick", LogType.INFO);
            return true;
        }
        if (clan.getChunks().size() > clanTierHandler.getChunkLimit(clan.getTier() - 1)) {
            sendFormattedPredefinedMessage(sender, "need-to-unclaim", LogType.INFO);
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
            return clanHandler.hasPermission(player, ClanRankPermission.CAN_DOWNGRADE);
        }
        return false;
    }
    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String tierLabel = "ERROR";
        String playerAmount = "ERROR";
        String chunkAmount = "ERROR";

        if (sender instanceof Player player) {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());

            if (clan != null && clan.getTier() > 0) {
                ClanTierHandler clanTierHandler = VClans.getInstance().getClanTierHandler();
                tierLabel = String.valueOf(clanTierHandler.getLabel(clan.getTier() - 1));
                playerAmount = String.valueOf(clanTierHandler.getPlayerLimit(clan.getTier() - 1));
                chunkAmount = String.valueOf(clanTierHandler.getChunkLimit(clan.getTier() - 1));
            }
        }

        replacements.put("{tier-label}", tierLabel);
        replacements.put("{player-amount}", playerAmount);
        replacements.put("{chunk-amount}", chunkAmount);
    }
}