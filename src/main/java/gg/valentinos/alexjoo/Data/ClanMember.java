package gg.valentinos.alexjoo.Data;

public class ClanMember {
    private String rankName;
    private long joinDate;

    public ClanMember(String rank){
        this.rankName = rank;
        this.joinDate = System.currentTimeMillis();
    }

    public String getRankName() {
        return rankName;
    }
    public void setRankName(String rankName) {
        this.rankName = rankName;
    }
    public long getJoinDate() {
        return joinDate;
    }
    public void setJoinDate(long joinDate) {
        this.joinDate = joinDate;
    }

}
