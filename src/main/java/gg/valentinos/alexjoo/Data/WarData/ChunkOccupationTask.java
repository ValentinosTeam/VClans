package gg.valentinos.alexjoo.Data.WarData;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanChunk;
import gg.valentinos.alexjoo.Handlers.ChunkHandler;
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

    private enum Team {
        ATTACKERS,
        DEFENDERS,
        BOTH,
        NEITHER
    }

    private record OccupationAdvantage(Team team, int strength) {
    }

    private final War war;
    private final Clan defenderClan;
    private final Clan attackerClan;
    private final ClanChunk chunk;
    private final ChunkHandler chunkHandler;

    private final int CHUNK_HEALTH_POINTS;
    private final int CHUNK_OCCUPATION_DAMAGE;

    private Map<String, List<Player>> playersInChunk;
    private BossBar bossBar;

    public ChunkOccupationTask(War war, Clan defenderClan, Clan attackerClan, ClanChunk chunk) {
        this.war = war;
        this.defenderClan = defenderClan;
        this.attackerClan = attackerClan;
        this.chunk = chunk;
        this.chunkHandler = VClans.getInstance().getChunkHandler();

        this.CHUNK_HEALTH_POINTS = VClans.getInstance().getWarHandler().CHUNK_HEALTH_POINTS;
        this.CHUNK_OCCUPATION_DAMAGE = VClans.getInstance().getWarHandler().CHUNK_OCCUPATION_DAMAGE;


        this.playersInChunk = new HashMap<>();

        Component name = Component.text(chunk.getOccupationState().name());
        float progress = (float) chunk.getOccupationProgress() / CHUNK_HEALTH_POINTS;
        this.bossBar = BossBar.bossBar(name, progress, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_20);
    }

    @Override
    public void accept(BukkitTask bukkitTask) {
        Map<String, List<Player>> previousPlayersInChunk = copyPlayersMap(playersInChunk);
        playersInChunk = getPlayersInChunk(chunk);
        PlayerDiff playerDiff = getChunkDiff(previousPlayersInChunk, playersInChunk);

        if (war.getState() == WarState.ENDED) {
            stopTask();
            bukkitTask.cancel();
            return;
        }

        handleChunkOccupation();
        handleBossBar(playerDiff);
    }

    private void handleChunkOccupation() {
        if (chunk.getOccupationState() != ChunkOccupationState.SECURED && chunkHandler.chunkShouldBeSecured(chunk)) {
            chunk.setOccupationState(ChunkOccupationState.SECURED);
            chunk.setOccupationProgress(CHUNK_HEALTH_POINTS);
        } else {
            OccupationAdvantage advantage = getOccupationAdvantage();
            Team team = advantage.team;
            int strength = advantage.strength;
            switch (chunk.getOccupationState()) {
                case SECURED -> {
                    if (!chunkHandler.chunkShouldBeSecured(chunk)) {
                        chunk.setOccupationState(ChunkOccupationState.CONTROLLED);
                        chunk.setOccupationProgress(CHUNK_HEALTH_POINTS);
                    }
                }
                case CONTROLLED -> {
                    if (team == Team.ATTACKERS) {
                        chunk.setOccupationState(ChunkOccupationState.CAPTURING);
                    }
                }
                case CAPTURING -> {
                    if (team == Team.ATTACKERS) {
                        int newProgress = chunk.getOccupationProgress() - CHUNK_OCCUPATION_DAMAGE * strength;
                        if (newProgress <= 0) {
                            chunk.setOccupationState(ChunkOccupationState.CAPTURED);
                            war.checkWarEndCondition();
                            chunk.setOccupationProgress(0);
                        } else {
                            chunk.setOccupationProgress(newProgress);
                        }
                    } else if (team == Team.DEFENDERS) {
                        chunk.setOccupationState(ChunkOccupationState.LIBERATING);
                    } else if (team == Team.BOTH) {
                        chunk.setOccupationState(ChunkOccupationState.CONTESTED);
                    }
                }
                case CAPTURED -> {
                    if (team == Team.DEFENDERS) {
                        chunk.setOccupationState(ChunkOccupationState.LIBERATING);
                    }
                }
                case CONTESTED -> {
                    if (team == Team.ATTACKERS) {
                        chunk.setOccupationState(ChunkOccupationState.CAPTURING);
                    } else if (team == Team.DEFENDERS) {
                        chunk.setOccupationState(ChunkOccupationState.LIBERATING);
                    }
                }
                case LIBERATING -> {
                    if (team == Team.DEFENDERS) {
                        int newProgress = chunk.getOccupationProgress() + CHUNK_OCCUPATION_DAMAGE * strength;
                        if (newProgress >= CHUNK_HEALTH_POINTS) {
                            if (chunkHandler.chunkShouldBeSecured(chunk)) {
                                chunk.setOccupationState(ChunkOccupationState.SECURED);
                            } else {
                                chunk.setOccupationState(ChunkOccupationState.CONTROLLED);
                            }
                            chunk.setOccupationProgress(CHUNK_HEALTH_POINTS);
                        } else {
                            chunk.setOccupationProgress(newProgress);
                        }
                    } else if (team == Team.ATTACKERS) {
                        chunk.setOccupationState(ChunkOccupationState.CAPTURING);
                    } else if (team == Team.BOTH) {
                        chunk.setOccupationState(ChunkOccupationState.CONTESTED);
                    }
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
        bossBar.progress((float) chunk.getOccupationProgress() / CHUNK_HEALTH_POINTS);
        // Hide boss bar for ppl who left
        Audience left = Audience.audience(playerDiff.left);
        left.hideBossBar(bossBar);
        // Create bar for players who just joined
        Audience joined = Audience.audience(playerDiff.joined);
        joined.showBossBar(bossBar);
    }
    private void stopTask() {
        List<Player> players = new ArrayList<>();
        players.addAll(defenderClan.getOnlinePlayers());
        players.addAll(attackerClan.getOnlinePlayers());
        Audience playerAudience = Audience.audience(players);
        playerAudience.hideBossBar(bossBar);
        bossBar = null;
    }

    //    private Team getOccupatingTeam() {
//        int defendersAmount = 0;
//        if (playersInChunk.containsKey(defenderClan.getId())) defendersAmount = playersInChunk.get(defenderClan.getId()).size();
//        int attackerAmount = 0;
//        if (playersInChunk.containsKey(attackerClan.getId())) attackerAmount = playersInChunk.get(attackerClan.getId()).size();
//
//        if (defendersAmount == 0 && attackerAmount > 0) return Team.ATTACKERS;
//        if (defendersAmount > 0 && attackerAmount == 0) return Team.DEFENDERS;
//        if (defendersAmount > 0 && attackerAmount > 0) return Team.BOTH;
//        return Team.NEITHER;
//    }
    private OccupationAdvantage getOccupationAdvantage() {
        int defendersAmount = 0;
        if (playersInChunk.containsKey(defenderClan.getId())) defendersAmount = playersInChunk.get(defenderClan.getId()).size();
        int attackerAmount = 0;
        if (playersInChunk.containsKey(attackerClan.getId())) attackerAmount = playersInChunk.get(attackerClan.getId()).size();

        int strength = Math.abs(defendersAmount - attackerAmount);

        Team team;
        if (attackerAmount > defendersAmount) team = Team.ATTACKERS;
        else if (defendersAmount > attackerAmount) team = Team.DEFENDERS;
        else if (attackerAmount == 0 && defendersAmount == 0) team = Team.NEITHER;
        else team = Team.BOTH;

        return new OccupationAdvantage(team, strength);
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
