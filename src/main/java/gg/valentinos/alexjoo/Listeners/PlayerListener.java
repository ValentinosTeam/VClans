package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.VClans;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String clanName = VClans.getInstance().getClansHandler().getClanNameOfMember(player.getUniqueId());
        player.sendMessage("Current clan: " + (clanName == null ? "None" : clanName));
        String invitedClanNames = VClans.getInstance().getClansHandler().getInvitedClanNames(player.getUniqueId());
        if (!invitedClanNames.isEmpty())
            player.sendMessage("You have been invited to: \n" + invitedClanNames);
    }
}
