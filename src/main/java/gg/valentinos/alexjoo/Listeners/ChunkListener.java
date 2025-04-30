package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ChunkListener implements Listener {
    private final ChunkHandler chunkHandler;

    public ChunkListener() {
        this.chunkHandler = VClans.getInstance().getChunkHandler();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return; // Same chunk, do nothing
        }
        Chunk toChunk = event.getTo().getChunk();

        Player player = event.getPlayer();
        chunkHandler.updateChunkRadar(player, toChunk.getX(), toChunk.getZ());
    }
}
