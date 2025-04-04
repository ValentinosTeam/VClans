package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.Data.ClanMember;
import gg.valentinos.alexjoo.Data.ClanRank;
import gg.valentinos.alexjoo.Data.Clans;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

public class ClanHandler {

    private Clans clans;
    private static final Logger logger = VClans.getInstance().getLogger();
    private final int defaultMaxSize;

    public ClanHandler() {
        loadClans();
        defaultMaxSize = VClans.getInstance().getConfig().getInt("settings.max-clan-size");
    }

    public Clans getClans() {
        return clans;
    }

    // the following functions do not check for permissions. Do that in the commands themselves
    public void createClan(UUID playerUUID, String name) {
        Clan clan = createDefaultClan(name, playerUUID);
        clans.addClan(clan);
        saveClans();
        logger.fine("Player " + Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getName() + " has successfully created a clan with name " + name);
    }
    public void disbandClan(UUID playerUUID) {
        Clan clan = getClanByMember(playerUUID);
        clans.getClans().remove(clan);
        saveClans();
        logger.fine("Player " + Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getName() + " has successfully disbanded the clan " + clan.getName());
    }
    public void invitePlayer(UUID playerUUID, String targetName) {
        Player player = Bukkit.getPlayer(playerUUID);
        OfflinePlayer target = VClans.getInstance().getServer().getOfflinePlayer(targetName);
        Clan clan = getClanByMember(playerUUID);
        clan.inviteMember(playerUUID, target.getUniqueId());
        saveClans();
        logger.fine("Player " + Objects.requireNonNull(player).getName() + " has invited player " + targetName + " to the clan.");
    }
    public void joinClan(UUID playerUUID, String clanName) {
        Clan clan = getClanByName(clanName);
        clan.addDefaultMember(playerUUID);
        saveClans();
        logger.fine("Player " + Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getName() + " has successfully joined the clan " + clanName);
    }
    public void leaveClan(UUID playerUUID) {
        Clan clan = getClanByMember(playerUUID);
        clan.removeMember(playerUUID);
        saveClans();
        logger.fine("Player " + Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getName() + " has successfully left the clan " + clan.getName());
    }
    public void kickPlayer(UUID playerUUID, String targetName) {
        Player player = Bukkit.getPlayer(playerUUID);
        OfflinePlayer target = VClans.getInstance().getServer().getOfflinePlayer(targetName);
        Clan clan = getClanByMember(playerUUID);
        clan.removeMember(target.getUniqueId());
        saveClans();
        logger.fine("Player " + Objects.requireNonNull(player).getName() + " has kicked player " + targetName + " from the clan.");
    }
    public void assignRank(UUID targetUUID, String rankName){
        Clan clan = getClanByMember(targetUUID);
        ClanMember member = clan.getMembers().get(targetUUID);
        member.setRankId(rankName);
        saveClans();
    }
    public void addRank(Clan clan, ClanRank newRank) {
        ClanRank rank = new ClanRank(newRank.getTitle(), newRank.getId());
        rank.setPriority(newRank.getPriority());
        HashMap<String, Boolean> permissions = newRank.getPermissions();
        rank.setPermissions(permissions);
        clan.addRank(rank);
        saveClans();
    }
    public void removeRank(Clan clan, String rankId) {
        clan.removeRank(rankId);
        for (ClanMember member : clan.getMembers().values()) {
            if (member.getRankId().equals(rankId)) {
                member.setRankId("default");
            }
        }
        saveClans();
    }

    // saves and loads the clans
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

    // various getters
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
        for (Clan clan : clans) {
            if (clan.isMemberInvited(playerUUID))
                names.add(clan.getName());
        }
        return names;
    }
    public List<UUID> getClanMembersUUIDs(UUID playerUUID){
        Clan clan = getClanByMember(playerUUID);
        if (clan == null)
            return List.of();
        return clan.getMemberUUIDs();
    }
    public List<UUID> getClanMemberUUIDs(String name) {
        Clan clan = getClanByName(name);
        if (clan == null)
            return List.of();
        return clan.getMemberUUIDs();
    }

    public String getClanNameOfMember(UUID playerUUID) {
        Clan clan = getClanByMember(playerUUID);
        if (clan == null)
            return "";
        return clan.getName();
    }
    public String getMemberRankTitle(UUID playerUUID) {
        Clan clan = getClanByMember(playerUUID);
        if (clan == null)
            return "";
        return clan.getMemberRankTitle(playerUUID);
    }

    public boolean isPlayerInAClan(UUID playerUUID) {
        return clans.getClans().stream().anyMatch(c -> c.getMembers().containsKey(playerUUID));
    }
    public boolean clanExists(String name) {
        return clans.getClans().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name));
    }

    public Clan getClanByName(String name) {
        return clans.getClans().stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
    public Clan getClanByMember(UUID memberUUID) {
        return clans.getClans().stream().filter(c -> c.getMembers().containsKey(memberUUID)).findFirst().orElse(null);
    }
    private Clan createDefaultClan(String name, UUID ownerUUID) {
        Clan clan = new Clan(name, ownerUUID, defaultMaxSize);
        clan.addOwnerMember(ownerUUID);
        //TODO: make sure to read the config here for default titles
        clan.createRank("owner", "leader");
        ClanRank ownerRank = clan.getRankById("owner");
        HashMap <String, Boolean> permissions = ownerRank.getPermissions();
        permissions.put("canDisband", true);
        permissions.put("canInvite", true);
        permissions.put("canKick", true);
        permissions.put("canEditRank", true);
        permissions.put("canCreateRank", true);
        permissions.put("canDeleteRank", true);
        permissions.put("canChangeRank", true);
        ownerRank.setPermissions(permissions);
        ownerRank.setPriority(99);
        clan.createRank("default", "member");
        return clan;
    }
}
