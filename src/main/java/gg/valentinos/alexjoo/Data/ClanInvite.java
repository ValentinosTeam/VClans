package gg.valentinos.alexjoo.Data;

import java.util.UUID;

public class ClanInvite {
    private long inviteTimestamp;
    private UUID inviter;
    private long duration;

    public ClanInvite(UUID inviter, long duration) {
        this.inviter = inviter;
        this.duration = duration;
        this.inviteTimestamp = System.currentTimeMillis();
    }

    public long getInviteTimestamp() {
        return inviteTimestamp;
    }
    public void setInviteTimestamp(long inviteTimestamp) {
        this.inviteTimestamp = inviteTimestamp;
    }
    public UUID getInviter() {
        return inviter;
    }
    public void setInviter(UUID inviter) {
        this.inviter = inviter;
    }
    public long getDuration() {
        return duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - inviteTimestamp > duration;
    }

}
