package gg.valentinos.alexjoo.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJump(PlayerMoveEvent event) {
        if (event.getFrom().getY() < event.getTo().getY()) {
            event.getPlayer().sendMessage("You jumped!");
        }
    }
}
