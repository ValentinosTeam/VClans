package gg.valentinos.alexjoo.Data;

public class ClanMember {
    private String rankId;
    private long joinDate;
    private String name;

    public ClanMember(String name, String rank){
        this.rankId = rank;
        this.joinDate = System.currentTimeMillis();
        this.name = name;
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
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }


}
