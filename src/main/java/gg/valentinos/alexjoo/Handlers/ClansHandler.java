package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.Clans;
import gg.valentinos.alexjoo.Utility.JsonUtils;

import java.util.List;
import java.util.UUID;

public class ClansHandler {

    private Clans clans;

    private void createClan(String name) {
        // This method will create a clan
    }

    private void deleteClan(String name) {
        // This method will delete a clan
    }

    private void addPlayer(String clanName, UUID playerId) {
        // This method will add a player to a clan
    }

    public void loadClans() {
        clans = JsonUtils.fromJsonFile("clans.json", Clans.class);
        if (clans == null)
            clans = new Clans();

    }

    public void saveClans() {
        tempClans();
        JsonUtils.toJsonFile(clans, "clans.json");
    }

    public void tempClans() {
        List<UUID> members = List.of(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> owners = List.of(UUID.randomUUID());
        Clan clan = new Clan("Test", members, owners);
        clans = new Clans();
        clans.getClans().add(clan);
    }
}
