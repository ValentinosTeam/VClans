package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.VClans;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PlayerListener implements Listener {

    private final boolean showCurrentClanOnJoin;

    public PlayerListener() {
        showCurrentClanOnJoin = VClans.getInstance().getConfig().getBoolean("show-current-clan-on-join");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (showCurrentClanOnJoin){
            String clanName = VClans.getInstance().getClansHandler().getClanNameOfMember(player.getUniqueId());
            player.sendMessage("Current clan: " + (clanName == null ? "None" : clanName));
        }
        List<String> invitedClanNames = VClans.getInstance().getClansHandler().getInvitingClanNames(player.getUniqueId());
        StringBuilder sb = new StringBuilder();
        for (String invitedClanName : invitedClanNames) {
            sb.append(invitedClanName).append(", ");
        }
        if (!invitedClanNames.isEmpty())
            player.sendMessage("You have been invited to: \n" + sb);
    }

}
