package gg.valentinos.alexjoo.Data;

public class Cooldown {
    private String query;
    private long timestamp;

    public Cooldown(String query, long duration) {
        this.query = query;
        this.timestamp = System.currentTimeMillis() + (duration * 1000);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public String getTimeLeft() {
        long currentTime = System.currentTimeMillis();
        long remainingTime = timestamp - currentTime;

        if (remainingTime <= 0) {
            return "00:00:00:00"; // Time has expired
        }

        long days = remainingTime / (24 * 60 * 60 * 1000);
        long hours = (remainingTime % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (remainingTime % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (remainingTime % (60 * 1000)) / 1000;

        return String.format("%02d:", days) +
                String.format("%02d:", hours) +
                String.format("%02d:", minutes) +
                String.format("%02d", seconds);
    }


}
