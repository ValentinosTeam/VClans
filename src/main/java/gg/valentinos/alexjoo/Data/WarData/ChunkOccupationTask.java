package gg.valentinos.alexjoo.Data.WarData;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanChunk;
import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChunkOccupationTask implements Consumer<BukkitTask> {

    private record PlayerDiff(List<Player> left, List<Player> joined) {
    }

    private final War war;
    private final Clan defenderClan;
    private final Clan attackerClan;
    private final ClanChunk chunk;

    private final int MAX_DEFENCE_HP;
    private final int PROGRESS_INCREMENT;

    private Map<String, List<Player>> playersInChunk;
    private BossBar bossBar;

    public ChunkOccupationTask(War war, Clan defenderClan, Clan attackerClan, ClanChunk chunk) {
        this.war = war;
        this.defenderClan = defenderClan;
        this.attackerClan = attackerClan;
        this.chunk = chunk;

        this.MAX_DEFENCE_HP = VClans.getInstance().getWarHandler().MAX_DEFENCE_HP;

        PROGRESS_INCREMENT = 1; //TODO: make configurable

        this.playersInChunk = new HashMap<>();

        Component name = Component.text(chunk.getOccupationState().name());
        float progress = (float) chunk.getOccupationProgress() / MAX_DEFENCE_HP;
        this.bossBar = BossBar.bossBar(name, progress, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_20);
    }

    @Override
    public void accept(BukkitTask bukkitTask) {
        if (war.getState() != WarState.IN_PROGRESS) {
            bukkitTask.cancel();
            return;
        }

        Map<String, List<Player>> previousPlayersInChunk = copyPlayersMap(playersInChunk);
        playersInChunk = getPlayersInChunk(chunk);
        PlayerDiff playerDiff = getChunkDiff(previousPlayersInChunk, playersInChunk);

        handleBossBar(playerDiff);
        handleChunkOccupation();
    }

    private void handleChunkOccupation() {
        switch (chunk.getOccupationState()) {
            case SECURED -> {
                // if secured, then if there is a chunk that is not part of the clan or is captured by the attacker clan, change to controlled
                if (!VClans.getInstance().getChunkHandler().chunkShouldBeSecured(chunk)) {
                    chunk.setOccupationState(ChunkOccupationState.CONTROLLED);
                    chunk.setOccupationProgress(MAX_DEFENCE_HP);
                }
            }
            case CONTROLLED -> {
                // if controlled, then check who is present in the chunk.
                //                if only attackers present, change to capturing
                if (playersInChunk.containsKey(attackerClan.getId()) && !playersInChunk.containsKey(defenderClan.getId())) {
                    chunk.setOccupationState(ChunkOccupationState.CAPTURING);
                }
                //                if only defenders present, do nothing
                //                if both present, do nothing
            }
            case CAPTURING -> {
                // if capturing, then check who is present in the chunk.
                //                if only attackers present, increase capture progress
                if (playersInChunk.containsKey(attackerClan.getId()) && !playersInChunk.containsKey(defenderClan.getId())) {
                    int newProgress = chunk.getOccupationProgress() - PROGRESS_INCREMENT; // Example progress increment
                    if (newProgress <= 0) {
                        chunk.setOccupationState(ChunkOccupationState.CAPTURED);
                        chunk.setOccupationProgress(0);
                    } else {
                        chunk.setOccupationProgress(newProgress);
                    }
                }
                //                if only defenders present, change to liberating
                else if (playersInChunk.containsKey(defenderClan.getId()) && !playersInChunk.containsKey(attackerClan.getId())) {
                    chunk.setOccupationState(ChunkOccupationState.LIBERATING);
                }
                //                if both present, change to contested
                else if (playersInChunk.containsKey(attackerClan.getId()) && playersInChunk.containsKey(defenderClan.getId())) {
                    chunk.setOccupationState(ChunkOccupationState.CONTESTED);
                }
            }
            case CAPTURED -> {
                // if captured, then check who is present in the chunk.
                //                if only defenders present, change to liberating
                if (playersInChunk.containsKey(defenderClan.getId()) && !playersInChunk.containsKey(attackerClan.getId())) {
                    chunk.setOccupationState(ChunkOccupationState.LIBERATING);
                }
                //                if only attackers present, do nothing
                //                if both present, do nothing
            }
            case CONTESTED -> {
                // if contested, then check who is present in the chunk.
                //                if only attackers present, change to capturing
                if (playersInChunk.containsKey(attackerClan.getId()) && !playersInChunk.containsKey(defenderClan.getId())) {
                    chunk.setOccupationState(ChunkOccupationState.CAPTURING);
                }
                //                if only defenders present, change to liberating
                else if (playersInChunk.containsKey(defenderClan.getId()) && !playersInChunk.containsKey(attackerClan.getId())) {
                    chunk.setOccupationState(ChunkOccupationState.LIBERATING);
                }
                //                if both present, do nothing
            }
            case LIBERATING -> {
                // if liberating, then check who is present in the chunk.
                //                if only defenders present, increase liberate progress
                if (playersInChunk.containsKey(defenderClan.getId()) && !playersInChunk.containsKey(attackerClan.getId())) {
                    int newProgress = chunk.getOccupationProgress() + PROGRESS_INCREMENT; // Example progress increment
                    if (newProgress >= MAX_DEFENCE_HP) {
                        if (VClans.getInstance().getChunkHandler().chunkShouldBeSecured(chunk)) {
                            chunk.setOccupationState(ChunkOccupationState.SECURED);
                        } else {
                            chunk.setOccupationState(ChunkOccupationState.CONTROLLED);
                        }
                        chunk.setOccupationProgress(MAX_DEFENCE_HP);
                    } else {
                        chunk.setOccupationProgress(newProgress);
                    }
                }
                //                if only attackers present, change to capturing
                else if (playersInChunk.containsKey(attackerClan.getId()) && !playersInChunk.containsKey(defenderClan.getId())) {
                    chunk.setOccupationState(ChunkOccupationState.CAPTURING);
                }
                //                if both present, change to contested
                else if (playersInChunk.containsKey(attackerClan.getId()) && playersInChunk.containsKey(defenderClan.getId())) {
                    chunk.setOccupationState(ChunkOccupationState.CONTESTED);
                }
            }

        }
    }
    private void handleBossBar(PlayerDiff playerDiff) {

        // Update bar for players who haven't left
        switch (chunk.getOccupationState()) {
            case SECURED -> {
                bossBar.name(Component.text("Secured"));
                bossBar.color(BossBar.Color.BLUE);
            }
            case CONTROLLED -> {
                bossBar.name(Component.text("Controlled"));
                bossBar.color(BossBar.Color.BLUE);
            }
            case CAPTURING -> {
                bossBar.name(Component.text("Capturing"));
                bossBar.color(BossBar.Color.RED);
            }
            case CAPTURED -> {
                bossBar.name(Component.text("Captured"));
                bossBar.color(BossBar.Color.RED);
            }
            case LIBERATING -> {
                bossBar.name(Component.text("Liberating"));
                bossBar.color(BossBar.Color.BLUE);
            }
            case CONTESTED -> {
                bossBar.name(Component.text("Contested"));
                bossBar.color(BossBar.Color.PURPLE);
            }
        }
        bossBar.progress((float) chunk.getOccupationProgress() / MAX_DEFENCE_HP);
        // Hide boss bar for ppl who left
        Audience left = Audience.audience(playerDiff.left);
        left.hideBossBar(bossBar);
        // Create bar for players who just joined
        Audience joined = Audience.audience(playerDiff.joined);
        joined.showBossBar(bossBar);
    }

    private static PlayerDiff getChunkDiff(Map<String, List<Player>> previous, Map<String, List<Player>> current) {
        List<Player> previousList = new ArrayList<>();
        for (List<Player> list : previous.values()) previousList.addAll(list);
        List<Player> currentList = new ArrayList<>();
        for (List<Player> list : current.values()) currentList.addAll(list);

        Set<UUID> previousUUIDs = previousList.stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());
        Set<UUID> currentUUIDs = currentList.stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());

        List<Player> left = new ArrayList<>();
        for (Player p : previousList) {
            if (!currentUUIDs.contains(p.getUniqueId())) {
                left.add(p);
            }
        }
        List<Player> joined = new ArrayList<>();
        for (Player p : currentList) {
            if (!previousUUIDs.contains(p.getUniqueId())) {
                joined.add(p);
            }
        }

        return new PlayerDiff(left, joined);
    }
    private static Map<String, List<Player>> copyPlayersMap(Map<String, List<Player>> source) {
        Map<String, List<Player>> copy = new HashMap<>();
        for (Map.Entry<String, List<Player>> entry : source.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
    private static Map<String, List<Player>> getPlayersInChunk(ClanChunk chunk) {
        Map<String, List<Player>> playersInChunk = new HashMap<>();
        List<Player> allPlayers = VClans.getInstance().getChunkHandler().getPlayersInChunk(chunk);
        for (Player player : allPlayers) {
            Clan playerClan = VClans.getInstance().getClanHandler().getClanByMember(player.getUniqueId());
            if (playerClan != null) {
                String clanId = playerClan.getId();
                if (!playersInChunk.containsKey(clanId)) {
                    playersInChunk.put(clanId, new ArrayList<>());
                }
                playersInChunk.get(clanId).add(player);
            }
        }
        return playersInChunk;
    }

}
