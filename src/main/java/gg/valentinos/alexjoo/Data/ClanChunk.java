package gg.valentinos.alexjoo.Data;

public class ClanChunk {
    private int x;
    private int z;
    private String world;
    private String clanName;

    private int minPriority = 0;
    private boolean cancelInteraction = true;
    private boolean cancelBuild = true;
    private boolean cancelBreak = true;

    public ClanChunk(int x, int z, String world, String clanName) {
        this.x = x;
        this.z = z;
        this.clanName = clanName;
        this.world = world;
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
    public String getClanName() {
        return clanName;
    }
    public void setClanName(String clanName) {
        this.clanName = clanName;
    }
    public int getMinPriority() {
        return minPriority;
    }
    public void setMinPriority(int minPriority) {
        this.minPriority = minPriority;
    }
    public boolean isCancelInteraction() {
        return cancelInteraction;
    }
    public void setCancelInteraction(boolean cancelInteraction) {
        this.cancelInteraction = cancelInteraction;
    }
    public boolean isCancelBuild() {
        return cancelBuild;
    }
    public void setCancelBuild(boolean cancelBuild) {
        this.cancelBuild = cancelBuild;
    }
    public boolean isCancelBreak() {
        return cancelBreak;
    }
    public void setCancelBreak(boolean cancelBreak) {
        this.cancelBreak = cancelBreak;
    }


}
