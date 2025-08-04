package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

import static gg.valentinos.alexjoo.VClans.sendFormattedMessage;

public class PlayerListener implements Listener {

    private final boolean showCurrentClanOnJoin;

    public PlayerListener() {
        showCurrentClanOnJoin = VClans.getInstance().getConfig().getBoolean("show-current-clan-on-join");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (showCurrentClanOnJoin) {
            Clan clan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());

            sendFormattedMessage(player, "Current clan: " + (clan == null ? "None" : clan.getName()), LogType.INFO);
        }

        Clan clan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
        if (clan != null) {
            VClans.getInstance().getVaultHandler().setPlayerPrefix(player, clan.getPrefix());
        } else {
            VClans.getInstance().getVaultHandler().removePlayerPrefix(player);
        }

        List<String> invitedClanNames = VClans.getInstance().getClanHandler().getInvitingClanNames(player.getUniqueId());
        StringBuilder sb = new StringBuilder();
        for (String invitedClanName : invitedClanNames) {
            sb.append(invitedClanName).append(", ");
        }
        if (!invitedClanNames.isEmpty())
            player.sendMessage("You have been invited to: \n" + sb);
    }

}
