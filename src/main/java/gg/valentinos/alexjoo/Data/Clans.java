package gg.valentinos.alexjoo.Data;

import java.util.List;
import java.util.UUID;

public class Clans {
    private List<Clan> clans;

    public List<Clan> getClans() {
        return clans;
    }

    public void setClans(List<Clan> clans) {
        this.clans = clans;
    }

    public void addClan(Clan clan) {
        if (clans.contains(clan)) {
            return;
        }
        if (clans.stream().anyMatch(c -> c.getName().equals(clan.getName()))) {
            return;
        }
        if (clans.stream().anyMatch(c -> c.getOwners().equals(clan.getOwners()))) {
            return;
        }
        if (clans.stream().anyMatch(c -> c.getMembers().equals(clan.getMembers()))) {
            return;
        }
        clans.add(clan);
    }

    public Clan getClanByName(String name) {
        return clans.stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Clan getClanByOwner(UUID owner) {
        return clans.stream().filter(c -> c.getOwners().contains(owner)).findFirst().orElse(null);
    }

    public Clan getClanByMember(UUID member) {
        // This includes owners
        Clan clan = clans.stream().filter(c -> c.getMembers().contains(member)).findFirst().orElse(null);
        if (clan == null) {
            clan = getClanByOwner(member);
        }
        return clan;
    }

    public boolean clanExists(String name) {
        return clans.stream().anyMatch(c -> c.getName().equalsIgnoreCase(name));
    }
}
