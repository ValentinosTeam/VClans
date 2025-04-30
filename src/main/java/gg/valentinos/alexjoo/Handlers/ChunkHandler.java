package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.ClanChunk;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.VClans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static gg.valentinos.alexjoo.VClans.Log;

public class ChunkHandler {

    private final String worldName;
    private final ClanHandler clanHandler;
    private HashMap<ChunkPos, ClanChunk> chunks = new HashMap<>();
    private final int maxChunkAmount;

    private record ChunkPos(int x, int z){}

    public ChunkHandler() {
        this.clanHandler = VClans.getInstance().getClanHandler();
        this.worldName = VClans.getInstance().getConfig().getString("chunks.world-name");
        for (Clan clan : clanHandler.getClans()) {
            if (clan.getChunks() == null) {
                clan.setChunks(new HashSet<>());
            }
            for (ClanChunk chunk : clan.getChunks()) {
                chunks.put(new ChunkPos(chunk.getX(), chunk.getZ()), chunk);
            }
        }
        this.maxChunkAmount = VClans.getInstance().getConfig().getInt("settings.max-chunks");
    }

    public void claimChunk(int x, int z, String clanName) {
        Clan clan = clanHandler.getClanByName(clanName);
        ClanChunk newChunk = new ClanChunk(x, z, worldName, clanName);
        addChunk(newChunk, clan);
    }

    public void unclaimChunk(int x, int z, String clanName) {
        Clan clan = clanHandler.getClanByName(clanName);
        ClanChunk chunkToRemove = chunks.get(new ChunkPos(x, z));
        removeChunk(chunkToRemove, clan);
    }

    public String getChunkInfo(int x, int z) {
        ClanChunk chunk = getChunk(x, z);
        // TODO: make this configurable.
        if (chunk == null) {
            return "Chunk Unclaimed";
        }
        return "Chunk Info:" + "\n" +
                "X: " + chunk.getX() + "\n" +
                "Z: " + chunk.getZ() + "\n" +
                "World: " + chunk.getWorld() + "\n" +
                "Clan Name: " + chunk.getClanName() + "\n";
    }

    public int getMaxChunkAmount() {
        return maxChunkAmount;
    }
    public String getWorldName() {
        return worldName;
    }
    public boolean isChunkClaimedByClan(int x, int z, String clanName) {
        ClanChunk chunk = getChunk(x, z);
        return chunk != null && chunk.getClanName().equals(clanName);
    }
    public boolean unclaimWillSplit(int x, int z, String clanName) {
        Clan clan = clanHandler.getClanByName(clanName);
        if (clan == null){
            Log("Clan not found", LogType.SEVERE);
            return false;
        }
        for (ClanChunk chunk : getAdjacentChunks(x, z)) {
            if (chunk != null && getAdjacentChunks(chunk.getX(), chunk.getZ()).size() <= 1){
                return true;
            }
        }
        return false;
    }
    public boolean isChunkAdjacentToClan(int x, int z, String clanName) {
        for (ClanChunk chunk : getAdjacentChunks(x, z)) {
            if (chunk != null && chunk.getClanName().equals(clanName)){
                return true;
            }
        }
        return false;
    }
    public boolean isChunkAdjacentToEnemyClan(int x, int z, String clanName) {
        for (ClanChunk chunk : getAdjacentChunks(x, z)) {
            if (chunk != null && !chunk.getClanName().equals(clanName)){
                return true;
            }
        }
        return false;
    }

    public List<ClanChunk> getAdjacentChunks(int x, int z) {
        List<ClanChunk> adjacentChunks = new ArrayList<>();
        int[][] offsets = {
                {1, 0}, // right
                {-1, 0}, // left
                {0, 1}, // up
                {0, -1} // down
        };
        for (int[] offset : offsets) {
            int newX = x + offset[0];
            int newZ = z + offset[1];
            ClanChunk chunk = getChunk(newX, newZ);
            adjacentChunks.add(chunk);
        }
        return adjacentChunks;
    }
    private void addChunk(ClanChunk chunk, Clan clan) {
        chunks.put(new ChunkPos(chunk.getX(), chunk.getZ()), chunk);
        clan.addChunk(chunk);
        clanHandler.saveClans();
    }
    private void removeChunk(ClanChunk chunk, Clan clan) {
        chunks.remove(new ChunkPos(chunk.getX(), chunk.getZ()));
        clan.removeChunk(chunk);
        clanHandler.saveClans();
    }
    private ClanChunk getChunk(int x, int z){
        return chunks.get(new ChunkPos(x, z));
    }
}
