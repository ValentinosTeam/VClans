package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.VClans;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;
import java.util.Objects;

public class ChunkListener implements Listener {
    private final ChunkHandler chunkHandler;
    private final ClanHandler clanHandler;
    private final TextColor RED = TextColor.color(255, 70, 70);
    private final TextColor GREEN = TextColor.color(70, 255, 70);

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

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
        if (chunkClanName != null) {
            Clan chunkClan = clanHandler.getClanByName(chunkClanName);
            if (chunkClan != null && !chunkClan.isPlayerMember(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You cannot place blocks in this territory.").color(RED));
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();
        String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
        if (chunkClanName != null) {
            Clan chunkClan = clanHandler.getClanByName(chunkClanName);
            if (chunkClan != null && !chunkClan.isPlayerMember(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You cannot break blocks in this territory.").color(RED));
            }
        }
    }

    @EventHandler
    public void onBlockBreakBlock(BlockBreakBlockEvent event) {

    }


    private boolean canPlayerInteractWithBlock(Player player, BlockEvent event, String message){
        return false;
    }

    private void enterExitNotification(Player player, Chunk fromChunk, Chunk toChunk) {
        String fromClanName = chunkHandler.getClanNameByChunk(fromChunk.getX(), fromChunk.getZ());
        String toClanName = chunkHandler.getClanNameByChunk(toChunk.getX(), toChunk.getZ());
        Clan playerClan = clanHandler.getClanByMember(player.getUniqueId());
        Component title = null;
        Component subtitle = null;

        if(fromClanName == null && toClanName != null) {
            title = Component.text(toClanName);
            if (playerClan != null && playerClan.getName().equals(toClanName)) {
                subtitle = Component.text("Entered your territory").color(GREEN);
            }
            else{
                subtitle = Component.text("Entered territory").color(RED);
            }
        }
        else if(fromClanName != null && toClanName == null) {
            title = Component.text("");
            subtitle = Component.text("Left territory").color(RED);
        }
        else if(!Objects.equals(fromClanName, toClanName)) {
            title = Component.text(toClanName);
            if (playerClan != null && playerClan.getName().equals(toClanName)) {
                subtitle = Component.text("Entered your territory").color(GREEN);
            }
            else{
                subtitle = Component.text("Entered territory").color(RED);
            }
        }

        if (title != null) {
            player.showTitle(Title.title(
                title,
                subtitle,
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500))
            ));
        }
    }
}
