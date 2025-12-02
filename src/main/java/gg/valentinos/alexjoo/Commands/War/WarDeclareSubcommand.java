package gg.valentinos.alexjoo.Commands.War;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Data.WarData.War;
import gg.valentinos.alexjoo.Utility.Decorator;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class WarDeclareSubcommand extends SubCommand {
    public WarDeclareSubcommand() {
        super("war", "declare", List.of("success", "initiator-clan-notification", "clan-in-war", "target-clan-notification", "declare-self", "clan-on-cooldown", "target-on-cooldown", "clan-not-found", "tier-mismatch", "no-chunks", "tier-too-low"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }
    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String targetClanId = args[1];

        return () -> {
            Clan playerClan = clanHandler.getClanByMember(player.getUniqueId());
            Clan targetClan = clanHandler.getClanById(targetClanId);
            warHandler.declareWar(playerClan, targetClan);
            for (Player onlinePlayer : playerClan.getOnlinePlayers()) {
                Decorator.PlaySound(onlinePlayer, Key.key("minecraft:item.goat_horn.sound.2"), 1);
                Decorator.Broadcast(onlinePlayer, Component.text("War Declared!").color(TextColor.color(255, 0, 0)), "Your clan declared war on " + targetClan.getName(), 5);
                if (onlinePlayer == player) continue;
                sendFormattedPredefinedMessage(onlinePlayer, "initiator-clan-notification", LogType.NULL);
            }
            for (Player onlinePlayer : targetClan.getOnlinePlayers()) {
                sendFormattedPredefinedMessage(onlinePlayer, "target-clan-notification", LogType.NULL);
                Decorator.PlaySound(onlinePlayer, Key.key("minecraft:item.goat_horn.sound.2"), 1);
                Decorator.Broadcast(onlinePlayer, Component.text("War Declared!").color(TextColor.color(255, 0, 0)), playerClan.getName() + " declared war on your clan", 5);
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
                    .filter(clan -> VClans.getInstance().getWarHandler().getWarEnemyClan(clan) == null)
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
        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_DECLARE_WAR)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        if (Objects.equals(targetClanId, clan.getId())) {
            sendFormattedPredefinedMessage(sender, "declare-self", LogType.WARNING);
            return true;
        }
        if (warHandler.getWarEnemyClan(clan) != null) {
            sendFormattedPredefinedMessage(sender, "clan-in-war", LogType.WARNING);
            return true;
        }
        if (warHandler.getWarCooldown(clan) > 0) {
            sendFormattedPredefinedMessage(sender, "clan-on-cooldown", LogType.WARNING);
            return true;
        }
        if (warHandler.getWarCooldown(targetClan) > 0) {
            sendFormattedPredefinedMessage(sender, "target-on-cooldown", LogType.WARNING);
            return true;
        }
        if (targetClan.getTier() < warHandler.MIN_CLAN_TIER || clan.getTier() < warHandler.MIN_CLAN_TIER) {
            sendFormattedPredefinedMessage(sender, "tier-too-low", LogType.WARNING);
            return true;
        }
        if (targetClan.getTier() < clan.getTier() - 1) {
            sendFormattedPredefinedMessage(sender, "tier-mismatch", LogType.WARNING);
            return true;
        }
        if (targetClan.getChunks().isEmpty()) {
            sendFormattedPredefinedMessage(sender, "no-chunks", LogType.WARNING);
            return true;
        }
        if (clan.getChunks().isEmpty()) {
            sendFormattedPredefinedMessage(sender, "no-chunks", LogType.WARNING);
            return true;
        }

        return false;
    }
    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            if (clan == null) return false;
            War war = warHandler.getWar(clan);
            if (war != null) return false;
            return clanHandler.hasPermission(player, ClanRankPermission.CAN_DECLARE_WAR);
        }
        return false;
    }
    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String clanName = "ERROR";
        String targetClanName = "ERROR";
        String playerName = "ERROR";
        String initiatorClanName = "ERROR";
        String clanCooldown = "ERROR";
        String targetCooldown = "ERROR";
        String noChunkClan = "ERROR";
        String minTier = warHandler.MIN_CLAN_TIER + "";

        if (sender instanceof Player player) {
            playerName = player.getName();
            Clan playerClan = clanHandler.getClanByMember(player.getUniqueId());
            if (playerClan != null) {
                initiatorClanName = playerClan.getId();
                if (warHandler.getWarEnemyClan(playerClan) != null) {
                    clanName = warHandler.getWarEnemyClan(playerClan).getId();
                }
                clanCooldown = "" + warHandler.getWarCooldown(playerClan);
                if (playerClan.getChunks().isEmpty()) {
                    noChunkClan = playerClan.getName();
                }
            }
        }
        if (args.length == requiredArgs) {
            String targetClanId = args[1];
            Clan targetClan = clanHandler.getClanById(targetClanId);
            if (targetClan != null) {
                targetClanName = targetClan.getId();
                if (warHandler.getWarEnemyClan(targetClan) != null) {
                    clanName = warHandler.getWarEnemyClan(targetClan).getId();
                }
                targetCooldown = "" + warHandler.getWarCooldown(targetClan);
                if (targetClan.getChunks().isEmpty()) {
                    noChunkClan = targetClan.getName();
                }
            }
        }
        replacements.put("{clan-name}", clanName);
        replacements.put("{target-clan}", targetClanName);
        replacements.put("{player-name}", playerName);
        replacements.put("{initiator-clan}", initiatorClanName);
        replacements.put("{clan-cooldown}", clanCooldown);
        replacements.put("{target-cooldown}", targetCooldown);
        replacements.put("{no-chunk-clan}", noChunkClan);
        replacements.put("{min-tier}", minTier);
    }
}
