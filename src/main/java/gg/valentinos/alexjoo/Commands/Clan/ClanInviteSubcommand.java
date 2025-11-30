package gg.valentinos.alexjoo.Commands.Clan;

import gg.valentinos.alexjoo.Commands.CommandAction;
import gg.valentinos.alexjoo.Commands.SubCommand;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanRankPermission;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Utility.Decorator;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.key.Key;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClanInviteSubcommand extends SubCommand {

    public ClanInviteSubcommand() {
        super("clan", "invite", List.of("success", "invitation", "invite-self", "already-in-a-clan", "already-in-the-clan", "already-invited", "no-permission", "clan-full"));
        hasToBePlayer = true;
        requiredArgs = 2;
    }

    @Override
    public CommandAction getAction(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String targetName = args[1];

        return () -> {
            sendFormattedMessage(sender, messages.get("success"), LogType.FINE);
            OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);
            if (target.getPlayer() != null && target.isOnline()) {
                sendFormattedMessage(target.getPlayer(), messages.get("invitation"), LogType.INFO);
                Decorator.PlaySound(target.getPlayer(), Key.key("minecraft:item.book.page_turn"), 1f);
            }
            cooldownHandler.createCooldown(player.getUniqueId(), targetCooldownQuery, cooldownDuration);
            clanHandler.invitePlayer(player.getUniqueId(), targetName);
        };
    }

    @Override
    protected boolean hasSpecificErrors(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        String targetName = args[1];
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore()) {
            sendFormattedPredefinedMessage(sender, "never-joined", LogType.WARNING);
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            sendFormattedPredefinedMessage(sender, "invite-self", LogType.WARNING);
            return true;
        }

        Clan clan = clanHandler.getClanByMember(playerUUID);
        if (clan == null) {
            sendFormattedPredefinedMessage(sender, "not-in-clan", LogType.WARNING);
            return true;
        }
        if (!clanHandler.hasPermission(player, ClanRankPermission.CAN_INVITE)) {
            sendFormattedPredefinedMessage(sender, "no-permission", LogType.WARNING);
            return true;
        }
        if (clan.isMemberInvited(target.getUniqueId())) {
            sendFormattedPredefinedMessage(sender, "already-invited", LogType.WARNING);
            return true;
        }
        if (clan.isPlayerMember(target.getUniqueId())) {
            sendFormattedPredefinedMessage(sender, "already-in-the-clan", LogType.WARNING);
            return true;
        }
        if (clanHandler.isPlayerInAClan(target.getUniqueId())) {
            sendFormattedPredefinedMessage(sender, "already-in-a-clan", LogType.WARNING);
            return true;
        }
        if (clanHandler.clanIsFull(clan)) {
            sendFormattedPredefinedMessage(sender, "clan-full", LogType.WARNING);
            return true;
        }
        return false;
    }

    @Override
    public boolean suggestCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            return clanHandler.hasPermission(player, ClanRankPermission.CAN_INVITE);
        }
        return false;
    }

    @Override
    protected void loadReplacementValues(CommandSender sender, String[] args) {
        String playerName = "ERROR";
        String clanName = "ERROR";
        String clanId = "ERROR";
        String targetName = "ERROR";

        if (sender instanceof Player player) {
            playerName = player.getName();
            Clan clan = clanHandler.getClanByMember(player.getUniqueId());
            if (clan != null) {
                clanName = clan.getName();
                clanId = clan.getId();
            }
        }
        if (args.length > 1) {
            targetName = args[1];
        }

        replacements.put("{target-name}", targetName);
        replacements.put("{player-name}", playerName);
        replacements.put("{clan-name}", clanName);
        replacements.put("{clan-id}", clanId);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("invite");
        } else if (args.length == 2) {
            List<UUID> excludePlayers = new ArrayList<>();
            for (Clan clan : clanHandler.getClans())
                excludePlayers.addAll(clan.getMemberUUIDs());
            excludePlayers.add(((Player) sender).getUniqueId());
            return sender.getServer().getOnlinePlayers().stream()
                    .map(Player::getUniqueId)
                    .filter(uuid -> !excludePlayers.contains(uuid))
                    .map(uuid -> Objects.requireNonNull(VClans.getInstance().getServer().getPlayer(uuid)).getName())
                    .toList();
        } else {
            return List.of();
        }
    }

}
