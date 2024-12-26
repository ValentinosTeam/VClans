package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.Clans;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class ClansHandler {

    private Clans clans;

    public ClansHandler() {
        loadClans();
    }

    public String createClan(UUID player, String name) {
        // This method will create a clan return null if successful, error message if not
        VClans.getInstance().getLogger().info("Player " + Bukkit.getPlayer(player).getName() + " is trying to create a clan with name " + name);
        if (clans.getClans().stream().anyMatch(clan -> clan.getName().equalsIgnoreCase(name))) {
            VClans.getInstance().getLogger().warning("Clan with name " + name + " already exists.");
            return "Clan with name " + name + " already exists.";
        }
        if (name.length() > 16) {
            VClans.getInstance().getLogger().warning("Clan name is too long.");
            return "Clan name is too long.";
        }
        if (name.length() < 3) {
            VClans.getInstance().getLogger().warning("Clan name is too short.");
            return "Clan name is too short.";
        }
        if (clans.getClans().stream().anyMatch(clan -> clan.getOwners().contains(player))) {
            VClans.getInstance().getLogger().warning("Player already owns a clan.");
            return "You already own a clan.";
        }
        if (clans.getClans().stream().anyMatch(clan -> clan.getMembers().contains(player))) {
            VClans.getInstance().getLogger().warning("Player is already in a clan.");
            return "You already in a clan.";
        }
        if (!name.matches("^[a-zA-Z0-9_]*$")) {
            VClans.getInstance().getLogger().warning("Clan name contains invalid characters.");
            return "Clan name contains invalid characters.";
        }

        // checks done, create the clan
        ArrayList<UUID> members = new ArrayList<>();
        ArrayList<UUID> owners = new ArrayList<>();
        owners.add(player);
        Clan clan = new Clan(name, members, owners);
        clans.addClan(clan);
        saveClans();
        VClans.getInstance().getLogger().fine("Player " + Bukkit.getPlayer(player).getName() + " has successfully created a clan with name " + name);
        return null;
    }

    public String deleteClan(UUID player, Clan clan) {
        // This method will delete a clan return true if successful, false if not

        // TODO: Check if player is an admin. if true then allow delete clan
        // Check if the player is the owner of the clan
        if (!clan.getOwners().contains(player)) {
            VClans.getInstance().getLogger().warning("Player " + Bukkit.getPlayer(player).getName() + " is not the owner of the clan.");
            return "You are not the owner of the clan.";
        }

        clans.getClans().remove(clan);
        saveClans();
        return null;
    }

    public String disbandClan(UUID player) {
        // This method will disband a clan return true if successful, false if not
        VClans.getInstance().getLogger().info("Player " + Bukkit.getPlayer(player).getName() + " is trying to disband a clan.");
        Clan clan = clans.getClans().stream().filter(c -> c.getOwners().contains(player)).findFirst().orElse(null);
        if (clan == null) {
            VClans.getInstance().getLogger().warning("Player does not own a clan to disband.");
            return "You do not own a clan to disband.";
        }
        return deleteClan(player, clan);
    }

    private void addPlayer(String clanName, UUID playerId) {
        // This method will add a player to a clan
//        saveClans();
    }

    public void loadClans() {
        clans = JsonUtils.fromJsonFile("clans.json", Clans.class);
        if (clans == null)
            clans = new Clans();
        clans.setClans(new ArrayList<>());
    }

    public void saveClans() {
        JsonUtils.toJsonFile(clans, "clans.json");
    }

    public String getClanName(@NotNull UUID uniqueId) {
        // This method will return the clan name of the player
        Clan clan = clans.getClans().stream().filter(c -> c.getOwners().contains(uniqueId)).findFirst().orElse(null);
        if (clan == null) {
            clan = clans.getClans().stream().filter(c -> c.getMembers().contains(uniqueId)).findFirst().orElse(null);
        }
        return clan == null ? null : clan.getName();
    }

    public String getClanList() {
        // This method will return the list of clans
        StringBuilder sb = new StringBuilder();
        for (Clan clan : clans.getClans()) {
            sb.append(clan.getName()).append("\n");
        }
        return sb.toString();
    }
}
