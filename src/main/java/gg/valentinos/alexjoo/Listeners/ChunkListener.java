package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.util.Objects;

public class ChunkListener implements Listener {
    private final ChunkHandler chunkHandler;
    private final ClanHandler clanHandler;

    public ChunkListener() {
        this.chunkHandler = VClans.getInstance().getChunkHandler();
        this.clanHandler = VClans.getInstance().getClanHandler();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();
        if (!fromChunk.equals(toChunk)) { // update the chunk radar and notify if player entered or left a chunk
            Player player = event.getPlayer();
            chunkHandler.updateChunkRadar(player, toChunk.getX(), toChunk.getZ());
            enterExitNotification(player, fromChunk, toChunk);
        }

    }

    private void enterExitNotification(Player player, Chunk fromChunk, Chunk toChunk) {
        String fromClan = chunkHandler.getClanNameByChunk(fromChunk.getX(), fromChunk.getZ());
        String toClan = chunkHandler.getClanNameByChunk(toChunk.getX(), toChunk.getZ());
        if(fromClan == null && toClan != null) {
            player.showTitle(Title.title(
                    Component.text(toClan),
                    Component.text("Entered territory").color(TextColor.color(255, 70, 70)),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500))
            ));
        }
        else if(fromClan != null && toClan == null) {
            player.showTitle(Title.title(
                    Component.text(fromClan),
                    Component.text("Left territory").color(TextColor.color(255, 70, 70)),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500))
            ));
        }
        else if(!Objects.equals(fromClan, toClan)) {
            player.showTitle(Title.title(
                    Component.text(toClan),
                    Component.text("Entered territory").color(TextColor.color(255, 70, 70)),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500))
            ));
        }
    }
}
