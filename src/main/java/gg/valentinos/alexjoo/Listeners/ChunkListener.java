package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.VClans;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static gg.valentinos.alexjoo.VClans.Log;

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
                player.sendMessage(Component.text("You cannot place blocks in this territory").color(RED));
                event.setCancelled(true);
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
                player.sendMessage(Component.text("You cannot break blocks in this territory").color(RED));
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        // triggers when anything explodes (tnt, creeper, etc)
        List<Block> blocks = event.blockList();
        blocks.removeIf(block -> {
            Chunk chunk = block.getChunk();
            String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
            return chunkClanName != null; // Remove blocks in claimed chunks
        });
    }
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event){
        // triggers only when enderman picks up or places a block in a claimed chunk
        Chunk chunk = event.getBlock().getChunk();
        String clanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
        if (clanName != null){
            if (event.getBlock().getType() == Material.FARMLAND && event.getTo() == Material.DIRT) {
                if (event.getEntity() instanceof Player player) {
                    if (!clanHandler.isPlayerInClan(player.getUniqueId(), clanName)) {
                        player.sendMessage(Component.text("You cannot trample farmland in this territory").color(RED));
                        event.setCancelled(true);
                    }
                }
            }
            if (event.getEntity().getType() == EntityType.ENDERMAN) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event){
        String fromClanName = chunkHandler.getClanNameByChunk(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
        String toClanName = chunkHandler.getClanNameByChunk(event.getToBlock().getChunk().getX(), event.getToBlock().getChunk().getZ());
        if (fromClanName == null && toClanName != null) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockBurn(BlockBurnEvent event){
        // triggers when a block is burned by fire or lava (DOES NOT DISABLE MELTING ICE)
        Chunk chunk = event.getBlock().getChunk();
        String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
        if (chunkClanName != null) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event){
        // will block piston extend if piston outside claimed chunk affects blocks inside claimed chunk
        event.setCancelled(blockPistonEvent(event.getBlock(), event.getBlocks(), event.getDirection()));
    }
    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event){
        // will block piston retract if piston outside claimed chunk affects blocks inside claimed chunk
        event.setCancelled(blockPistonEvent(event.getBlock(), event.getBlocks(), event.getDirection()));
    }
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event){
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
        if (chunkClanName != null) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event){
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Player player = event.getPlayer();
        String chunkClan = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());

        if (chunkClan != null) {
            if (player != null) {
                if (!clanHandler.isPlayerInClan(player.getUniqueId(), chunkClan)) {
                    player.sendMessage(Component.text("You cannot place this in this territory").color(RED));
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onHangingHangingBreakByEntity(HangingBreakByEntityEvent event){
        Entity remover = event.getRemover();
        Location loc = event.getEntity().getLocation();
        Chunk chunk = loc.getChunk();
        String chunkClan = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());

        if (chunkClan != null) {
            if (remover instanceof Player player) {
                Log("remover is player", LogType.INFO);
                if (!clanHandler.isPlayerInClan(player.getUniqueId(), chunkClan)) {
                    player.sendMessage(Component.text("You cannot break this in this territory").color(RED));
                    event.setCancelled(true);
                }
            }
            else{
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        event.setCancelled(blockBucketEvent(block, player));
    }
    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlockClicked();
        event.setCancelled(blockBucketEvent(block, player));
    }

    private boolean blockBucketEvent(Block block, Player player){
        Chunk chunk = block.getChunk();
        String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
        if (chunkClanName != null) {
            if (!clanHandler.isPlayerInClan(player.getUniqueId(), chunkClanName)) {
                player.sendMessage(Component.text("You cannot use buckets in this territory").color(RED));
                return true;
            }
        }
        return false;
    }
    private boolean blockPistonEvent(Block piston, List<Block> affectedBlocks, BlockFace direction){
        if (chunkHandler.getClanNameByChunk(piston.getChunk().getX(), piston.getChunk().getZ()) == null) {
            Block pistonHead = piston.getRelative(direction);
            if (chunkHandler.getClanNameByChunk(pistonHead.getChunk().getX(), pistonHead.getChunk().getZ()) != null) {
                return true;
            }
            for (Block block : affectedBlocks) {
                if (chunkHandler.getClanNameByChunk(block.getChunk().getX(), block.getChunk().getZ()) != null) {
                    return true;
                } else {
                    Block relativeBlock = block.getRelative(direction);
                    if (chunkHandler.getClanNameByChunk(relativeBlock.getChunk().getX(), relativeBlock.getChunk().getZ()) != null) {
                        return true;
                    }
                }
            }
        }
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
