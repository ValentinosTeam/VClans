package gg.valentinos.alexjoo.Data.ClanData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Clans implements Iterable<Clan> {
    private List<Clan> clans;

    public Clans() {
        setClans(new ArrayList<>());
    }

    public List<Clan> getClans() {
        return clans;
    }
    public void setClans(List<Clan> clans) {
        this.clans = clans;
    }

    public void addClan(Clan clan) {
        clans.add(clan);
    }

    @Override
    public @NotNull Iterator<Clan> iterator() {
        return clans.iterator();
    }
}
