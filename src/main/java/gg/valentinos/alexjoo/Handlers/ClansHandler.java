package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.Clans;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class ClansHandler {

    private Clans clans;
    private static final Logger logger = VClans.getInstance().getLogger();
    private static final FileConfiguration config = VClans.getInstance().getConfig();

    public ClansHandler() {
        loadClans();
    }

    public Clans getClans() {
        return clans;
    }

    public void createClan(UUID player, String name) {
        ArrayList<UUID> members = new ArrayList<>();
        ArrayList<UUID> owners = new ArrayList<>();
        owners.add(player);
        Clan clan = new Clan(name, members, owners);
        clans.addClan(clan);
        saveClans();
        logger.fine("Player " + Bukkit.getPlayer(player).getName() + " has successfully created a clan with name " + name);
    }

    public void disbandClan(UUID player) {
        // This method will disband a clan return true if successful, false if not
        Clan clan = clans.getClanByMember(player);
        clans.getClans().remove(clan);
        saveClans();
        logger.fine("Player " + Bukkit.getPlayer(player).getName() + " has successfully disbanded the clan " + clan.getName());
    }

    public void invitePlayer(UUID playerUUID, String targetName) {
        // This method will invite a player to a clan return true if successful, false if not
        Player player = Bukkit.getPlayer(playerUUID);
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);
        Clan clan = clans.getClanByOwner(playerUUID);
        clan.inviteMember(target.getUniqueId());
        saveClans();
        logger.fine("Player " + player.getName() + " has invited player " + targetName + " to the clan.");
    }

    public void joinClan(UUID player, String clanName) {
        Clan clan = clans.getClanByName(clanName);
        clan.addMember(player);
        saveClans();
        logger.fine("Player " + Bukkit.getPlayer(player).getName() + " has successfully joined the clan " + clanName);
    }

    public void leaveClan(UUID player) {
        Clan clan = clans.getClanByMember(player);
        clan.removeMember(player);
    }

    public void stepDownPlayer(UUID player) {
        Clan clan = clans.getClanByMember(player);
        clan.stepDownOwner(player);
    }

    public void promotePlayer(UUID playerUUID, String targetName){
        Player player = Bukkit.getPlayer(playerUUID);
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);
        Clan clan = clans.getClanByOwner(playerUUID);
        clan.addOwner(target.getUniqueId());
    }

    public void kickPlayer(UUID playerUUID, String targetName) {
        // This method will kick a player from a clan return true if successful, false if not
        Player player = Bukkit.getPlayer(playerUUID);
        OfflinePlayer target = player.getServer().getOfflinePlayer(targetName);
        Clan clan = clans.getClanByMember(playerUUID);
        clan.removeMember(target.getUniqueId());
    }

    public void loadClans() {
        clans = new Clans();
        List<Clan> clansList = JsonUtils.deserializeClans("clans.json");
        if (clansList == null) {
            logger.warning("Clans file is empty. Creating new clans.json file.");
            saveClans();
            return;
        }
        clans.setClans(clansList);
    }

    public void saveClans() {
        JsonUtils.toJsonFile(clans.getClans(), "clans.json");
    }

    public List<String> getClanNames() {
        // This method will return the list of clan names
        List<String> names = new ArrayList<>();
        for (Clan clan : clans.getClans()) {
            names.add(clan.getName());
        }
        return names;
    }
    public List<String> getInvitingClanNames(UUID playerUUID) {
        // This method will return the list of clan names that have invited the player
        List<String> names = new ArrayList<>();
        for (Clan clan : clans.getClans()) {
            if (clan.getInvites().contains(playerUUID)) {
                names.add(clan.getName());
            }
        }
        return names;
    }
    public List<UUID> getClanMembersUUIDs(Clan clan){
        List<UUID> members = new ArrayList<>();
        members.addAll(clan.getMembers());
        members.addAll(clan.getOwners());
        return members;
    }
    public List<UUID> getClanMembersUUIDs(UUID playerUUID){
        Clan clan = clans.getClanByMember(playerUUID);
        if (clan == null)
            return List.of();

        return getClanMembersUUIDs(clan);
    }
    public List<UUID> getClanMemberUUIDs(String name) {
        Clan clan = clans.getClanByName(name);
        if (clan == null)
            return List.of();

        return getClanMembersUUIDs(clan);
    }

    public String getPlayerIsOwnerErrorKey(UUID player, Clan clan) {
        if (clan == null) {
            return "not-in-clan";
        }
        if (!clan.isOwner(player)) {
            return "not-owner";
        }
        return null;
    }

}
