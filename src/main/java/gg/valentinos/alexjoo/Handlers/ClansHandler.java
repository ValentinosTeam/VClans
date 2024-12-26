package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.Clans;
import gg.valentinos.alexjoo.Utility.JsonUtils;

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

    private void saveClans() {
        JsonUtils.toJsonFile(clans, "clans.json");
    }


}
