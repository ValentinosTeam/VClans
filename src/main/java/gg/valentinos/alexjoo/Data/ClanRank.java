package gg.valentinos.alexjoo.Data;

public class ClanRank {
    private String title;
    private int priority;

    private boolean canDisband;
    private boolean canInvite;
    private boolean canKick;
    private boolean canChangeRank;

    public ClanRank(String title) {
        this.title = title;
    }

    public boolean canDisband() {
        return canDisband;
    }
    public void setCanDisband(boolean canDisband) {
        this.canDisband = canDisband;
    }
    public boolean canInvite() {
        return canInvite;
    }
    public void setCanInvite(boolean canInvite) {
        this.canInvite = canInvite;
    }
    public boolean canKick() {
        return canKick;
    }
    public void setCanKick(boolean canKick) {
        this.canKick = canKick;
    }
    public boolean canChangeRank() {
        return canChangeRank;
    }
    public void setCanChangeRank(boolean canChangeRank) {
        this.canChangeRank = canChangeRank;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }


}
