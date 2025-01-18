package gg.valentinos.alexjoo.Handlers;

import com.google.gson.reflect.TypeToken;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.Clans;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClansHandler {

    private Clans clans;

    public ClansHandler() {
        loadClans();
    }

    public String createClan(UUID player, String name) {
        // This method will create a clan return null if successful, error message if not
        VClans.getInstance().getLogger().info("Player " + Bukkit.getPlayer(player).getName() + " is trying to create a clan with name " + name);
        if (clans.clanExists(name)) {
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
        if (clans.getClanByMember(player) != null) {
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
        Clan clan = new Clan(name, members, owners, new ArrayList<>());
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
        Clan clan = clans.getClanByMember(player);
        String error = isPlayerOwner(player, clan);
        if (error != null) {
            return error;
        }

        return deleteClan(player, clan);
    }

    public String invitePlayer(UUID player, UUID target) {
        // This method will invite a player to a clan return true if successful, false if not
        VClans.getInstance().getLogger().info("Player " + Bukkit.getPlayer(player).getName() + " is trying to invite player " + Bukkit.getPlayer(target).getName() + " to the clan.");
        Clan clan = clans.getClanByOwner(player);
        if (clan == null) {
            VClans.getInstance().getLogger().warning("Player does not own a clan to invite players.");
            return "You do not own a clan to invite players.";
        }
        if (player.equals(target)) {
            VClans.getInstance().getLogger().warning("Player is trying to kick themselves.");
            return "You cannot kick yourself.";
        }
        if (clan.getMembers().contains(target)) {
            VClans.getInstance().getLogger().warning("Player is already in the clan.");
            return "Player is already in your clan.";
        }
        if (clan.getInvites().contains(target)) {
            VClans.getInstance().getLogger().warning("Player is already invited to the clan.");
            return "Player is already invited to the clan.";
        }
        if (clans.getClanByMember(target) != null) {
            VClans.getInstance().getLogger().warning("Player is already in a clan.");
            return "Player is already in a clan.";
        }
        clan.inviteMember(target);
        saveClans();
        return null;
    }

    public String joinClan(UUID player, String name) {
        // This method will join a player to a clan return true if successful, false if not
        VClans.getInstance().getLogger().info("Player " + Bukkit.getPlayer(player).getName() + " is trying to join a clan with name " + name);
        Clan clan = clans.getClanByName(name);
        if (clan == null) {
            VClans.getInstance().getLogger().warning("Clan with name " + name + " does not exist.");
            return "Clan with name " + name + " does not exist.";
        }
        if (clan.getMembers().contains(player)) {
            VClans.getInstance().getLogger().warning("Player is already in the clan.");
            return "You are already in the clan.";
        }
        if (!clan.getInvites().contains(player)) {
            VClans.getInstance().getLogger().warning("Player is not invited to the clan.");
            return "You are not invited to the clan.";
        }
        if (clans.getClanByMember(player) != null) {
            VClans.getInstance().getLogger().warning("Player is already in a clan.");
            return "You are already in a clan.";
        }
        clan.addMember(player);
        saveClans();
        return null;
    }

    public String leaveClan(UUID player) {
        // This method will leave a player from a clan return true if successful, false if not
        VClans.getInstance().getLogger().info("Player " + Bukkit.getPlayer(player).getName() + " is trying to leave the clan.");
        Clan clan = clans.getClanByMember(player);
        if (clan == null) {
            VClans.getInstance().getLogger().warning("Player is not in a clan.");
            return "You are not in a clan.";
        }
        if (clan.isOwner(player)) {
            VClans.getInstance().getLogger().warning("Player is the owner of the clan.");
            return "You are the owner of the clan. Disband the clan instead.";
        }
        clan.removeMember(player);
        return null;
    }

    public String stepDownPlayer(UUID player) {
        // This method will step down a player from a clan return true if successful, false if not
        VClans.getInstance().getLogger().info("Player " + Bukkit.getPlayer(player).getName() + " is trying to step down from the clan.");
        Clan clan = clans.getClanByMember(player);
        String error = isPlayerOwner(player, clan);
        if (error != null) {
            return error;
        }
        if (clan.getOwners().size() == 1) {
            VClans.getInstance().getLogger().warning("Player is the only owner of the clan.");
            return "You are the only owner of the clan. Disband the clan instead.";
        }
        clan.stepDownOwner(player);
        return null;
    }

    public String kickPlayer(UUID player, UUID target) {
        // This method will kick a player from a clan return true if successful, false if not
        VClans.getInstance().getLogger().info("Player " + Bukkit.getPlayer(player).getName() + " is trying to kick player " + Bukkit.getPlayer(target).getName() + " from the clan.");
        Clan clan = clans.getClanByMember(player);
        String error = isPlayerOwner(player, clan);
        if (error != null) {
            return error;
        }
        if (player.equals(target)) {
            VClans.getInstance().getLogger().warning("Player is trying to kick themselves.");
            return "You cannot kick yourself.";
        }
        if (!clan.getMembers().contains(target)) {
            VClans.getInstance().getLogger().warning("Player is not in the clan.");
            return "Player is not in the clan.";
        }
        clan.removeMember(target);
        return null;
    }

    public void loadClans() {
        clans = new Clans();
        List<Clan> clansList = JsonUtils.deserializeClans("clans.json");
        if (clansList == null) {
            VClans.getInstance().getLogger().warning("Clans file is empty. Creating new clans.json file.");
            saveClans();
            return;
        }
        clans.setClans(clansList);
    }

    public void saveClans() {
        JsonUtils.toJsonFile(clans.getClans(), "clans.json");
    }

    public String getInvitedClanNames(UUID uniqueId) {
        // This method will return the list of clans the player is invited to
        StringBuilder sb = new StringBuilder();
        for (Clan clan : clans.getClans()) {
            if (clan.getInvites().contains(uniqueId)) {
                sb.append(clan.getName()).append("\n");
            }
        }
        return sb.toString();
    }

    public List<UUID> getMembersOfPlayerClan(UUID player) {
        Clan clan = clans.getClanByMember(player);
        if (clan == null)
            return List.of();
        return clan.getMembers();
    }

    public String getClanNameOfMember(UUID uniqueId) {
        // This method will return the clan the player is in
        Clan clan = clans.getClanByMember(uniqueId);
        if (clan == null) {
            return null;
        }
        return clan.getName();
    }

    public String getClanList() {
        // This method will return the list of clans
        StringBuilder sb = new StringBuilder();
        for (Clan clan : clans.getClans()) {
            sb.append(" - ").append(clan.getName()).append("\n");
        }
        return sb.toString();
    }

    public String getClanMembersList(String clanName) {
        Clan clan = clans.getClanByName(clanName);
        if (clan == null) {
            return "Clan does not exist.";
        }
        StringBuilder sb = new StringBuilder();
        for (UUID owner : clan.getOwners()) {
            sb.append(" - ").append(Bukkit.getOfflinePlayer(owner).getName()).append(" [Owner]\n");
        }
        for (UUID member : clan.getMembers()) {
            sb.append(" - ").append(Bukkit.getOfflinePlayer(member).getName()).append(" [Member] \n");
        }
        return sb.toString();
    }

    public List<UUID> getClanMemberUUIDs(String name) {
        Clan clan = clans.getClanByName(name);
        if (clan == null)
            return List.of();
        List<UUID> members = new ArrayList<>();
        members.addAll(clan.getMembers());
        members.addAll(clan.getOwners());

        return members;
    }

    private String isPlayerOwner(UUID player, Clan clan) {
        if (clan == null) {
            VClans.getInstance().getLogger().warning("Player is not in a clan.");
            return "You are not in a clan.";
        }
        if (!clan.isOwner(player)) {
            VClans.getInstance().getLogger().warning("Player is not the owner of the clan.");
            return "You are not the owner of the clan.";
        }
        return null;
    }

}
