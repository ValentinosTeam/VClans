package gg.valentinos.alexjoo.Data.WarData;

import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.Data.ClanData.ClanChunk;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ChunkOccupationTask implements Consumer<BukkitTask> {

    private final War war;
    private final Clan defenderClan;
    private final Clan attackerClan;
    private final ClanChunk chunk;

    private final int MAX_DEFENCE_HP;

    private final int PROGRESS_INCREMENT;

    public ChunkOccupationTask(War war, Clan defenderClan, Clan attackerClan, ClanChunk chunk) {
        this.war = war;
        this.defenderClan = defenderClan;
        this.attackerClan = attackerClan;
        this.chunk = chunk;

        this.MAX_DEFENCE_HP = VClans.getInstance().getWarHandler().MAX_DEFENCE_HP;

        PROGRESS_INCREMENT = 1; //TODO: make configurable
    }

    @Override
    public void accept(BukkitTask bukkitTask) {
        if (war.getState() != WarState.IN_PROGRESS) {
            bukkitTask.cancel();
            return;
        }
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
                Map<String, List<Player>> playersInChunk = getPlayersInChunk(chunk);
                //                if only attackers present, change to capturing
                if (playersInChunk.containsKey(attackerClan.getId()) && !playersInChunk.containsKey(defenderClan.getId())) {
                    chunk.setOccupationState(ChunkOccupationState.CAPTURING);
                }
                //                if only defenders present, do nothing
                //                if both present, do nothing
            }
            case CAPTURING -> {
                // if capturing, then check who is present in the chunk.
                Map<String, List<Player>> playersInChunk = getPlayersInChunk(chunk);
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
                Map<String, List<Player>> playersInChunk = getPlayersInChunk(chunk);
                //                if only defenders present, change to liberating
                if (playersInChunk.containsKey(defenderClan.getId()) && !playersInChunk.containsKey(attackerClan.getId())) {
                    chunk.setOccupationState(ChunkOccupationState.LIBERATING);
                }
                //                if only attackers present, do nothing
                //                if both present, do nothing
            }
            case CONTESTED -> {
                // if contested, then check who is present in the chunk.
                Map<String, List<Player>> playersInChunk = getPlayersInChunk(chunk);
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
                Map<String, List<Player>> playersInChunk = getPlayersInChunk(chunk);
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
