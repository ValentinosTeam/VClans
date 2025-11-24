package gg.valentinos.alexjoo.Data.ClanData;

import gg.valentinos.alexjoo.Data.WarData.ChunkOccupationState;
import gg.valentinos.alexjoo.VClans;

import static gg.valentinos.alexjoo.VClans.Log;

public class ClanChunk {
    private int x;
    private int z;
    private String world;
    private String clanId;
    private ChunkOccupationState occupationState;
    private int occupationProgress;
    private boolean isLost;

    public ClanChunk(int x, int z, String world, String clanId) {
        this.x = x;
        this.z = z;
        this.clanId = clanId;
        this.world = world;
        this.occupationState = ChunkOccupationState.SECURED;
        this.occupationProgress = 0;
    }

    public int getX() {
        return x;
    }
    public int getZ() {
        return z;
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setZ(int z) {
        this.z = z;
    }
    public String getWorld() {
        return world;
    }
    public void setWorld(String world) {
        this.world = world;
    }
    public String getClanId() {
        return clanId;
    }
    public void setClanId(String clanId) {
        this.clanId = clanId;
    }
    public ChunkOccupationState getOccupationState() {
        return occupationState;
    }
    public void setOccupationState(ChunkOccupationState occupationState) {
        Log("(" + x + ", " + z + ") " + clanId + " occupation: " + occupationState + " progress: " + occupationProgress);
        switch (occupationState) {
            case ChunkOccupationState.SECURED,
                 ChunkOccupationState.CONTROLLED -> {
                setIstLost(false);
                VClans.getInstance().getChunkHandler().updateChunkRadarForAll();
            }
            case ChunkOccupationState.CAPTURED -> {
                setIstLost(true);
                VClans.getInstance().getChunkHandler().updateChunkRadarForAll();
            }
        }
        this.occupationState = occupationState;
    }
    public int getOccupationProgress() {
        return occupationProgress;
    }
    public void setOccupationProgress(int occupationProgress) {
        Log("(" + x + ", " + z + ") " + clanId + " occupation: " + occupationState + " progress: " + occupationProgress);
        this.occupationProgress = occupationProgress;
    }
    public boolean getIsLost() {
        return isLost;
    }
    public void setIstLost(boolean isLost) {
        this.isLost = isLost;
    }
}
