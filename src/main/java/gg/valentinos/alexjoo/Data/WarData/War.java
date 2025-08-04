package gg.valentinos.alexjoo.Data.WarData;

import gg.valentinos.alexjoo.Data.ClanData.Clan;

public class War {
    private String initiatorClanId;
    private String targetClanId;
    private long declarationTime;
    private WarState state;

    public War(Clan initiatorClan, Clan targetClan) {
        this.initiatorClanId = initiatorClan.getId();
        this.targetClanId = targetClan.getId();
        this.declarationTime = System.currentTimeMillis();
        this.state = WarState.DECLARED;
    }

    public String getInitiatorClanId() {
        return initiatorClanId;
    }
    public void setInitiatorClanId(String initiatorClanId) {
        this.initiatorClanId = initiatorClanId;
    }
    public String getTargetClanId() {
        return targetClanId;
    }
    public void setTargetClanId(String targetClanId) {
        this.targetClanId = targetClanId;
    }
    public long getDeclarationTime() {
        return declarationTime;
    }
    public void setDeclarationTime(long declarationTime) {
        this.declarationTime = declarationTime;
    }
    public WarState getState() {
        return state;
    }
    public void setState(WarState state) {
        this.state = state;
    }

}
