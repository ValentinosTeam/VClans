package gg.valentinos.alexjoo.Data;

public class Cooldown {
    private String name;
    private long timestamp;

    public Cooldown(String name, long duration) {
        this.name = name;
        this.timestamp = System.currentTimeMillis() + duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > timestamp;
    }


}
