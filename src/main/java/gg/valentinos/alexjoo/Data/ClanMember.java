package gg.valentinos.alexjoo.Data;

public class ClanMember {
    private String rankId;
    private long joinDate;

    public ClanMember(String rank){
        this.rankId = rank;
        this.joinDate = System.currentTimeMillis();
    }

    public String getRankId() {
        return rankId;
    }
    public void setRankId(String rankId) {
        this.rankId = rankId;
    }
    public long getJoinDate() {
        return joinDate;
    }
    public void setJoinDate(long joinDate) {
        this.joinDate = joinDate;
    }

}
