package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.StructureGrowEvent;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class ChunkListener implements Listener {
    private final ChunkHandler chunkHandler;
    private final ClanHandler clanHandler;
    private static final TextColor RED = TextColor.color(255, 70, 70);
    private static final TextColor GREEN = TextColor.color(70, 255, 70);

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
        String fromClanName = chunkHandler.getClanNameByChunk(fromChunk.getX(), fromChunk.getZ());
        String toClanName = chunkHandler.getClanNameByChunk(toChunk.getX(), toChunk.getZ());
        TextColor color = TextColor.color(125, 125, 125);
        if (toClanName != null) {
            Clan toClan = clanHandler.getClanByName(toClanName);
            color = TextColor.color(toClan.getColor().get(0), toClan.getColor().get(1), toClan.getColor().get(2));
        }
        Clan playerClan = clanHandler.getClanByMember(player.getUniqueId());
        Component title = null;
        Component subtitle = null;

        if (fromClanName == null && toClanName != null) {
            title = Component.text(toClanName).color(color);
            if (playerClan != null && playerClan.getName().equals(toClanName)) {
                subtitle = Component.text("Entered your territory").color(GREEN);
            } else {
                subtitle = Component.text("Entered territory").color(RED);
            }
        } else if (fromClanName != null && toClanName == null) {
            title = Component.text("");
            subtitle = Component.text("Left territory");
        } else if (!Objects.equals(fromClanName, toClanName)) {
            Clan toClan = clanHandler.getClanByName(toClanName);
            title = Component.text(toClanName).color(TextColor.color(toClan.getColor().get(0), toClan.getColor().get(1), toClan.getColor().get(2)));
            if (playerClan != null && playerClan.getName().equals(toClanName)) {
                subtitle = Component.text("Entered your territory").color(GREEN);
            } else {
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

    // ========= CANCELABLE EVENTS (events to cancel if unauthorized player interacts with a clans chunk) =========

    // Player related events
    private boolean shouldBlockPlayerInteraction(Chunk chunk, Player player) {
        // helper method for all the player related events
        String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
        if (chunkClanName != null) {
            Clan chunkClan = clanHandler.getClanByName(chunkClanName);
            if (chunkClan != null && !chunkClan.isPlayerMember(player.getUniqueId())) {
                player.sendMessage(Component.text("You cannot interact with this territory").color(RED));
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // forbid placing blocks in clan chunk if player not in that clan
        Player player = event.getPlayer();
        Block block = event.getBlock();
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // forbid breaking blocks in clan chunk if player not in that clan
        Player player = event.getPlayer();
        Block block = event.getBlock();
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player));
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        // forbid players placing hanging entities like frames and pictures in a clan chunk they are not a part of
        Player player = event.getPlayer();
        Block block = event.getBlock().getRelative(event.getBlockFace()); // where the entity will be placed
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player));
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        // forbid players emptying buckets with water/lava/snow powder source blocks in chunks they are not a part of
        Player player = event.getPlayer();
        Block block = event.getBlock();
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player));
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        // forbid players filling buckets with water/lava/snow powder source blocks in chunks they are not a part of
        Player player = event.getPlayer();
        Block block = event.getBlock();
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player));
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        // forbid players placing fire in territories they are not a part of and forbid fire spread from outside the clans territory to the inside
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (player != null) {
            event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player));
        } else {
            Chunk chunk = block.getChunk();
            String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
            if (chunkClanName != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        // forbid players from reeling in entities with a fishhook if the fishhook in a territory the player is not a part of (NOTE: allows casting fishhook)
        Entity hookedEntity = event.getHook().getHookedEntity();
        if (hookedEntity == null) return;
        Player player = event.getPlayer();
        event.setCancelled(shouldBlockPlayerInteraction(hookedEntity.getChunk(), player));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // forbid players on interacting (right-clicking) with blocks (doors, tnt, chests, beds) inside the territory they are not a part of
        Block block = event.getClickedBlock();
        if (block == null) return;
        Player player = event.getPlayer();
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player));
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // forbid players on interacting (right-clicking) with entities (frames, end crystals, boats) inside the territory they are not a part of
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        event.setCancelled(shouldBlockPlayerInteraction(entity.getChunk(), player));
    }

    // entity events

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // blocks non-clan members from damaging any entity protected by the claimed chunk.

        Entity targetEntity = event.getEntity();
        // return if target is not a player
//        if (!(targetEntity instanceof Player targetPlayer)) return;
        Chunk chunk = event.getEntity().getChunk();
        String chunkClan = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
        // return if target is not in a claimed chunk
        if (chunkClan == null) return;
        DamageSource damageSource = event.getDamageSource();

        Entity damagerEntity = damageSource.getCausingEntity();

        // return if the damager isn't a player
        if (damagerEntity instanceof Player damagerPlayer) {
            // return if damager is part of the clan that owns the chunk
            if (clanHandler.isPlayerInClan(damagerPlayer.getUniqueId(), chunkClan)) return;
            damagerPlayer.sendMessage(Component.text("You cannot harm this entity in a clan territory").color(RED));
            event.setCancelled(true);
        } else {
            DamageType damageType = damageSource.getDamageType();
            Chunk chunkSource = damageSource.getSourceLocation().getChunk();
            chunkClan = chunkHandler.getClanNameByChunk(chunkSource.getX(), chunkSource.getZ());

            if (damageType != DamageType.EXPLOSION && chunkClan != null) return;
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // stop blocks inside claimed chunks from being exploded by entities (ignited tnt, creeper, wither attack)
        List<Block> blocks = event.blockList();
        blocks.removeIf(block -> {
            Chunk chunk = block.getChunk();
            String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
            return chunkClanName != null; // Remove blocks in claimed chunks
        });
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        // cancel any block change caused by a player in a claimed chunk they don't belong to
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            event.setCancelled(shouldBlockPlayerInteraction(chunk, player));
        }
    }

//    @EventHandler
//    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
//        Entity remover = event.getRemover();
//        Location loc = event.getEntity().getLocation();
//        Chunk chunk = loc.getChunk();
//        String chunkClan = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
//
//        if (chunkClan != null) {
//            if (remover instanceof Player player) {
//                Log("remover is player", LogType.INFO);
//                if (!clanHandler.isPlayerInClan(player.getUniqueId(), chunkClan)) {
//                    player.sendMessage(Component.text("You cannot break this in this territory").color(RED));
//                    event.setCancelled(true);
//                }
//            } else {
//                event.setCancelled(true);
//            }
//        }
//    }

    // block events

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        // forbid liquid flow from outside into a claimed territory
        String fromClanName = chunkHandler.getClanNameByChunk(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
        String toClanName = chunkHandler.getClanNameByChunk(event.getToBlock().getChunk().getX(), event.getToBlock().getChunk().getZ());
        if (fromClanName == null && toClanName != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        // block fire or lava from destroying blocks (does not apply to ice smelting)
        Chunk chunk = event.getBlock().getChunk();
        String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
        if (chunkClanName != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        // stop blocks inside claimed chunks from being exploded by blocks (respawn anchor)
        List<Block> blocks = event.blockList();
        blocks.removeIf(block -> {
            Chunk chunk = block.getChunk();
            String chunkClanName = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
            return chunkClanName != null; // Remove blocks in claimed chunks
        });
    }

    @EventHandler
    public void onTreeGrow(StructureGrowEvent event) {
        // removes blocks that would grow from outside claimed chunk inside (sapling -> tree, mushroom -> big mushroom)
        Chunk saplingChunk = event.getLocation().getChunk();
        String clanName = chunkHandler.getClanNameByChunk(saplingChunk.getX(), saplingChunk.getZ());
        if (clanName != null) return;
        event.getBlocks().removeIf(blockState -> {
            Chunk chunk = blockState.getLocation().getChunk();
            String targetClan = chunkHandler.getClanNameByChunk(chunk.getX(), chunk.getZ());
            return targetClan != null;
        });
    }

    // piston events
    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        // block piston extend if piston outside claimed chunk affects blocks inside claimed chunk
        event.setCancelled(blockPistonEvent(event.getBlock(), event.getBlocks(), event.getDirection()));
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        // block piston retract if piston outside claimed chunk affects blocks inside claimed chunk
        event.setCancelled(blockPistonEvent(event.getBlock(), event.getBlocks(), event.getDirection()));
    }

    private boolean blockPistonEvent(Block piston, List<Block> affectedBlocks, BlockFace direction) {
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
}
