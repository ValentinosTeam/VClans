package gg.valentinos.alexjoo.Data;

import java.util.List;

public class Clans {
    private List<Clan> clans;

    public List<Clan> getClans() {
        return clans;
    }

    public void setClans(List<Clan> clans) {
        this.clans = clans;
    }

    public void addClan(Clan clan) {
        clans.add(clan);
    }
}
