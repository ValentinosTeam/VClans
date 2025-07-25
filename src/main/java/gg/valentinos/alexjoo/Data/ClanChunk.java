package gg.valentinos.alexjoo.Data;

public class ClanChunk {
    private int x;
    private int z;
    private String world;
    private String clanId;

    public ClanChunk(int x, int z, String world, String clanId) {
        this.x = x;
        this.z = z;
        this.clanId = clanId;
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
    public String getClanId() {
        return clanId;
    }
    public void setClanId(String clanId) {
        this.clanId = clanId;
    }
}
