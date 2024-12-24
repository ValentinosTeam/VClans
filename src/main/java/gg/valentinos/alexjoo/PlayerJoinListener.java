package gg.valentinos.alexjoo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Get the player who joined
        String playerName = event.getPlayer().getName();

        // Send a message to the player who joined
        event.getPlayer().sendMessage("Hello " + playerName + "!");
    }
}
