package gg.valentinos.alexjoo.Commands.War;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class WarDeclareSubcommand extends SubCommand {
    public WarDeclareSubcommand() {
        super("war", "declare", List.of("success", "initiator-clan-notification", "clan-in-war", "target-clan-notification", "declare-self"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }
    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String targetClanId = args[1];

        return () -> {
            Clan playerClan = clanHandler.getClanByMember(player.getUniqueId());
            Clan targetClan = clanHandler.joinClan(player.getUniqueId(), targetClanId);
            warHandler.declareWar(playerClan, targetClan);
            for (Player onlinePlayer : playerClan.getOnlinePlayers()) {
                if (onlinePlayer == player) continue;
                sendFormattedPredefinedMessage(onlinePlayer, "initiator-clan-notification", LogType.INFO);
            }
            for (Player onlinePlayer : targetClan.getOnlinePlayers()) {
                sendFormattedPredefinedMessage(onlinePlayer, "target-clan-notification", LogType.INFO);
            }
            sendFormattedPredefinedMessage(player, "success", LogType.INFO);
        };
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("declare");
        } else if (args.length == 2) {
            if (!(sender instanceof Player player)) return List.of();
            Clan playerClan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
            if (playerClan == null) return List.of();
            return VClans.getInstance().getClanHandler().getClans().getClans().stream()
                    .filter(clan -> VClans.getInstance().getWarHandler().inWar(clan) == null)
                    .filter(clan -> clan != playerClan)
                    .map(Clan::getId).toList();
        } else {
            return List.of();
        }
    }
    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Clan clan = clanHandler.getClanByMember(player.getUniqueId());
        String targetClanId = args[1];
        Clan targetClan = clanHandler.getClanById(targetClanId);
        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.WARNING);
            return true;
        }
        if (targetClan == null) {
            sendFormattedPredefinedMessage(sender, "clan-not-found", LogType.WARNING);
            return true;
        }
        String clanName = clan.getId();
        HashMap<String, Boolean> permissions = clan.getRank(player.getUniqueId()).getPermissions();
        if (!permissions.get("canDeclareWar")) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        if (Objects.equals(targetClanId, clan.getId())) {
            sendFormattedPredefinedMessage(sender, "declare-self", LogType.WARNING);
            return true;
        }
        if (warHandler.inWar(clan) != null) {
            sendFormattedPredefinedMessage(sender, "clan-in-war", LogType.WARNING);
            return true;
        }
        if (warHandler.getWarCooldown(clan) > 0) {
            sendFormattedMessage(sender, "Your clan is on a war cooldown. Please wait before declaring another war.", LogType.WARNING);
            return true;
        }
        if (warHandler.getWarCooldown(targetClan) > 0) {
            sendFormattedMessage(sender, "The target clan is on a war cooldown. You cannot declare war on them at this time.", LogType.WARNING);
            return true;
        }
        return false;
    }
    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            return clan != null && clan.getRank(player.getUniqueId()).getPermissions().get("canDeclareWar");
        }
        return false;
    }
    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String clanName = "ERROR";
        String targetClanName = "ERROR";
        String playerName = "ERROR";
        String initiatorClanName = "ERROR";

        String targetClanId = args[1];
        if (sender instanceof Player player) {
            playerName = player.getName();
            Clan playerClan = clanHandler.getClanByMember(player.getUniqueId());
            Clan targetClan = clanHandler.getClanById(targetClanId);
            if (playerClan != null) {
                initiatorClanName = playerClan.getId();
                if (warHandler.inWar(playerClan) != null) {
                    clanName = warHandler.inWar(playerClan).getId();
                }
            }
            if (targetClan != null) {
                targetClanName = targetClan.getId();
                if (warHandler.inWar(targetClan) != null) {
                    clanName = warHandler.inWar(targetClan).getId();
                }
            }
        }
        replacements.put("{clan-name}", clanName);
        replacements.put("{target-clan}", targetClanName);
        replacements.put("{player-name}", playerName);
        replacements.put("{initiator-clan}", initiatorClanName);
    }
}
