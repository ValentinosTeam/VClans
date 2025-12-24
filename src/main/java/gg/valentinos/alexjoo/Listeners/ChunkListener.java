package gg.valentinos.alexjoo.Listeners;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanChunk;
import gg.valentinos.alexjoo.Data.WarData.War;
import gg.valentinos.alexjoo.Data.WarData.WarState;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
import gg.valentinos.alexjoo.Handlers.ClanHandler;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static gg.valentinos.alexjoo.VClans.Log;

public class ChunkListener implements Listener {
    private final ChunkHandler chunkHandler;
    private final ClanHandler clanHandler;
    private static final TextColor RED = TextColor.color(255, 70, 70);
    private static final TextColor GREEN = TextColor.color(70, 255, 70);

    private enum WarBypassRule {
        NONE, // war doesn't change any requirements
        SOFT, // means that the requirements to block the event are soft, (being in a war means no need to block event)
        HARD // means that requirements are harder, ie has to be in war AND in an occupied chunk
    }

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
            chunkHandler.updateChunkRadar(player, toChunk);
            enterExitNotification(player, fromChunk, toChunk);
        }

    }

    private void enterExitNotification(Player player, Chunk fromChunk, Chunk toChunk) {
        //TODO: make use of the clan Name not Id
        Clan fromClan = chunkHandler.getClanByChunk(fromChunk);
        Clan toClan = chunkHandler.getClanByChunk(toChunk);
        TextColor toClanColor = GREEN;
        TextColor fromClanColor = RED;
        if (toClan != null) {
            toClanColor = TextColor.color(toClan.getColor().get(0), toClan.getColor().get(1), toClan.getColor().get(2));
        }
        if (fromClan != null) {
            fromClanColor = TextColor.color(fromClan.getColor().get(0), fromClan.getColor().get(1), fromClan.getColor().get(2));
        }
        Clan playerClan = clanHandler.getClanByMember(player.getUniqueId());
        LegacyComponentSerializer amp = LegacyComponentSerializer.legacyAmpersand();
        Component title = null;
        Component subtitle = null;

        if (fromClan == null && toClan != null) { // from wilderness to clan
            Log("to clan: " + toClan.getName());
            title = amp.deserialize(toClan.getName());
            if (playerClan != null && playerClan.getId().equals(toClan.getId())) {
                subtitle = Component.text("Entered your territory").color(toClanColor);
            } else {
                subtitle = Component.text("Entered territory").color(toClanColor);
            }
        } else if (fromClan != null && toClan == null) { // from clan to wilderness
            Log("from clan: " + fromClan.getName());
            title = Component.text("");
            subtitle = Component.text("Left territory").color(fromClanColor);
        } else if (!Objects.equals(fromClan, toClan)) { // from clan to clan that are different
            Log("from: " + fromClan.getName() + " to: " + toClan.getName());
            title = amp.deserialize(toClan.getName());
            if (playerClan != null && playerClan.getId().equals(toClan.getId())) {
                subtitle = Component.text("Entered your territory").color(toClanColor);
            } else {
                subtitle = Component.text("Entered territory").color(toClanColor);
            }
        }

        if (title != null) {
            player.showTitle(Title.title(
                    title,
                    subtitle,
                    Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(1), Duration.ofMillis(250))
            ));
        }
    }

    // ========= CANCELABLE EVENTS (events to cancel if unauthorized player interacts with a clans chunk) =========

    // Player related events
    private boolean shouldBlockPlayerInteraction(Chunk chunk, Player player, WarBypassRule warBypassRule) {
        if (player.isOp()) return false;
        // helper method for all the player related events
        Clan chunkClan = clanHandler.getClanByChunkLocation(chunk);
        if (chunkClan != null && !chunkClan.isPlayerMember(player.getUniqueId())) {
//                player.sendMessage(Component.text("You cannot interact with this territory").color(RED));
            if (warBypassRule == WarBypassRule.NONE) return true;
            else {
                if (isInActiveWarWith(player, chunk)) {
                    if (warBypassRule == WarBypassRule.SOFT) return false;
                    else if (warBypassRule == WarBypassRule.HARD) {
                        if (isChunkCompromised(chunk)) return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // forbid placing blocks in clan chunk if player not in that clan
        Player player = event.getPlayer();
        Block block = event.getBlock();
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player, WarBypassRule.HARD));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // forbid breaking blocks in clan chunk if player not in that clan
        Player player = event.getPlayer();
        Block block = event.getBlock();
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player, WarBypassRule.HARD));
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        // forbid players placing hanging entities like frames and pictures in a clan chunk they are not a part of
        Player player = event.getPlayer();
        Block block = event.getBlock().getRelative(event.getBlockFace()); // where the entity will be placed
        if (player != null) event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player, WarBypassRule.HARD));
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        // forbid players emptying buckets with water/lava/snow powder source blocks in chunks they are not a part of
        Player player = event.getPlayer();
        Block block = event.getBlock();
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player, WarBypassRule.HARD));
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        // forbid players filling buckets with water/lava/snow powder source blocks in chunks they are not a part of
        Player player = event.getPlayer();
        Block block = event.getBlock();
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player, WarBypassRule.HARD));
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        // forbid players placing fire in territories they are not a part of and forbid fire spread from outside the clans territory to the inside
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (player != null) {
            event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player, WarBypassRule.HARD));
        } else {
            Chunk chunk = block.getChunk();
            if (chunkHandler.isChunkClaimed(chunk)) {
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
        event.setCancelled(shouldBlockPlayerInteraction(hookedEntity.getChunk(), player, WarBypassRule.SOFT));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // forbid players on interacting (right-clicking) with blocks (doors, tnt, chests, beds) inside the territory they are not a part of
        Block block = event.getClickedBlock();
        if (block == null) return;
        Player player = event.getPlayer();
        event.setCancelled(shouldBlockPlayerInteraction(block.getChunk(), player, WarBypassRule.HARD));
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // forbid players on interacting (right-clicking) with entities (frames, end crystals, boats) inside the territory they are not a part of
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        event.setCancelled(shouldBlockPlayerInteraction(entity.getChunk(), player, WarBypassRule.SOFT));
    }
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        // forbid players on interacting with armor stands (apparently they are slightly different from other entities)
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        event.setCancelled(shouldBlockPlayerInteraction(entity.getChunk(), player, WarBypassRule.HARD));
    }

    // entity events
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity target = event.getEntity();
        Chunk chunk = target.getLocation().getChunk();
        Clan chunkClan = chunkHandler.getClanByChunk(chunk);
        if (chunkClan == null) return;

        if (target instanceof Player targetPlayer) {
            // dont protect ppl who dont belong to the clan
            Clan clan = clanHandler.getClanByMember(targetPlayer.getUniqueId());
            if (clan == null || !clan.getId().equals(chunkClan.getId())) {
                return;
            }
        }

        DamageCause cause = event.getCause();

        // Explosion protection
        if (cause == DamageCause.ENTITY_EXPLOSION || cause == DamageCause.BLOCK_EXPLOSION) {
            event.setCancelled(true);
        }

        // the rest of this is not needed as it is handled in onEntityDamageByEntity();
//        DamageSource source = event.getDamageSource();
//        Entity damager = source.getCausingEntity();
//        if (damager instanceof Player player) {
//            if (clanHandler.isPlayerInClan(player.getUniqueId(), chunkClan.getId())) return;
//            if (isInWarWith(player, chunk)) return;
//            event.setCancelled(true);
//        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // stop blocks inside claimed chunks from being exploded by entities (ignited tnt, creeper, wither attack)
        List<Block> blocks = event.blockList();

        blocks.removeIf(block -> {
            Chunk chunk = block.getChunk();
            if (event.getEntityType() == EntityType.END_CRYSTAL && isWarTerritory(chunk)) return false;
            return chunkHandler.isChunkClaimed(chunk); // Remove blocks in claimed chunks
        });
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        // cancel any block change caused by a player in a claimed chunk they don't belong to
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            event.setCancelled(shouldBlockPlayerInteraction(chunk, player, WarBypassRule.HARD));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player player = null;

        if (event.getDamager() instanceof Player) {
            player = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }
        }

        if (player == null) return;

        Entity entity = event.getEntity();

        if (shouldBlockPlayerInteraction(entity.getChunk(), player, WarBypassRule.SOFT)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        // Triggered when a vehicle (boat, minecart) is *damaged* by anything (player, arrow    , explosion)
        if (!(event.getAttacker() instanceof Player player)) return;

        Entity vehicle = event.getVehicle();
        if (shouldBlockPlayerInteraction(vehicle.getChunk(), player, WarBypassRule.HARD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        // Triggered when a vehicle is *broken* (final blow)
        if (!(event.getAttacker() instanceof Player player)) return;

        Entity vehicle = event.getVehicle();
        if (shouldBlockPlayerInteraction(vehicle.getChunk(), player, WarBypassRule.HARD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        Chunk chunk = event.getEntity().getChunk();
        String chunkClanId = chunkHandler.getClanIdByChunk(chunk);

        if (!chunkHandler.isChunkClaimed(chunk)) return;

        if (remover instanceof Player player) {
            if (!clanHandler.isPlayerInClan(player.getUniqueId(), chunkClanId)) {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    // block events

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        // forbid liquid flow from outside into a claimed territory
        Chunk fromChunk = event.getBlock().getChunk();
        Chunk toChunk = event.getToBlock().getChunk();
        if (!chunkHandler.isChunkClaimed(fromChunk) && chunkHandler.isChunkClaimed(toChunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        // block fire or lava from destroying blocks (does not apply to ice smelting)
        Chunk chunk = event.getBlock().getChunk();
        if (chunkHandler.isChunkClaimed(chunk)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        // stop blocks inside claimed chunks from being exploded by blocks (respawn anchor)
        List<Block> blocks = event.blockList();
        blocks.removeIf(block -> {
            Chunk chunk = block.getChunk();
            return chunkHandler.isChunkClaimed(chunk); // Remove blocks in claimed chunks
        });
    }

    @EventHandler
    public void onTreeGrow(StructureGrowEvent event) {
        // removes blocks that would grow from outside claimed chunk inside (sapling -> tree, mushroom -> big mushroom)
        Chunk saplingChunk = event.getLocation().getChunk();
        if (chunkHandler.isChunkClaimed(saplingChunk)) return;
        event.getBlocks().removeIf(blockState -> {
            Chunk chunk = blockState.getLocation().getChunk();
            String targetClan = chunkHandler.getClanIdByChunk(chunk);
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
        if (chunkHandler.getClanIdByChunk(piston.getChunk()) == null) {
            Block pistonHead = piston.getRelative(direction);
            if (chunkHandler.getClanIdByChunk(pistonHead.getChunk()) != null) {
                return true;
            }
            for (Block block : affectedBlocks) {
                if (chunkHandler.getClanIdByChunk(block.getChunk()) != null) {
                    return true;
                } else {
                    Block relativeBlock = block.getRelative(direction);
                    if (chunkHandler.getClanIdByChunk(relativeBlock.getChunk()) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private static boolean isInActiveWarWith(Player player, Chunk chunk) {
        Clan chunkClan = VClans.getInstance().getChunkHandler().getClanByChunk(chunk);
        if (chunkClan == null) return false;
        War war = VClans.getInstance().getWarHandler().getWar(chunkClan);
        if (war == null) return false;
        return VClans.getInstance().getWarHandler().isInActiveWarWith(player, chunkClan);
    }
    private static boolean isWarTerritory(Chunk chunk) {
        Clan chunkClan = VClans.getInstance().getChunkHandler().getClanByChunk(chunk);
        if (chunkClan == null) return false;
        War war = VClans.getInstance().getWarHandler().getWar(chunkClan);
        if (war == null) return false;
        return true;
    }
    private static boolean isChunkCompromised(Chunk chunk) {
        Clan clan = VClans.getInstance().getChunkHandler().getClanByChunk(chunk);
        War war = VClans.getInstance().getWarHandler().getWar(clan);
        if (war == null) return false;
        if (war.getState() != WarState.IN_PROGRESS) return false;
        ClanChunk clanChunk = clan.getChunkByLocation(chunk.getX(), chunk.getZ());
        if (clanChunk == null) return false;
        return clanChunk.getIsLost();
    }
}
