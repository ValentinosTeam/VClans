package gg.valentinos.alexjoo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Get the player who joined
        String playerName = event.getPlayer().getName();

        // Send a message to the player who joined
        event.getPlayer().sendMessage("Hello WOLRD" + playerName + "!");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Get the player who moved
        String playerName = event.getPlayer().getName();

        // Send a message to the player who moved
        event.getPlayer().sendMessage("You moved, " + playerName + "!");
    }
}
