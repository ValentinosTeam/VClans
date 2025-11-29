package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.ClanData.*;
import gg.valentinos.alexjoo.Data.LogType;
import gg.valentinos.alexjoo.Utility.JsonUtils;
import gg.valentinos.alexjoo.VClans;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

import static gg.valentinos.alexjoo.VClans.Log;
import static gg.valentinos.alexjoo.VClans.WORLD_NAME;

public class ClanHandler {

    private Clans clans;

    public ClanHandler() {
        loadClans();
    }

    public Clans getClans() {
        return clans;
    }

    // the following functions do not check for permissions. Do that in the commands themselves
    public Clan createClan(UUID playerUUID, String name) {
        Clan clan = createDefaultClan(name, playerUUID);
        clans.addClan(clan);
        saveClans();
        Log("Player " + Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getName() + " has successfully created a clan with name " + name);
        return clan;
    }
    public void disbandClan(Clan clan) {
        VClans.getInstance().getChunkHandler().unclaimChunks(clan.getId());
        clans.getClans().remove(clan);
        saveClans();
        BlueMapHandler blueMapHandler = VClans.getInstance().getBlueMapHandler();
        if (blueMapHandler != null) {
            blueMapHandler.removeClanTerritory(clan);
        }
    }
    public void disbandClan(UUID playerUUID) {
        Clan clan = getClanByMember(playerUUID);
        disbandClan(clan);
        Log("Player " + Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getName() + " has successfully disbanded the clan " + clan.getId());
    }
    public void invitePlayer(UUID playerUUID, String targetName) {
        Player player = Bukkit.getPlayer(playerUUID);
        OfflinePlayer target = VClans.getInstance().getServer().getOfflinePlayer(targetName);
        Clan clan = getClanByMember(playerUUID);
        clan.inviteMember(playerUUID, target.getUniqueId());
        saveClans();
        Log("Player " + Objects.requireNonNull(player).getName() + " has invited player " + targetName + " to the clan.");
    }
    public Clan joinClan(UUID playerUUID, String clanName) {
        Clan clan = getClanById(clanName);
        clan.addDefaultMember(playerUUID);
        saveClans();
        Log("Player " + Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getName() + " has successfully joined the clan " + clanName);
        return clan;
    }
    public void leaveClan(UUID playerUUID) {
        Clan clan = getClanByMember(playerUUID);
        clan.removeMember(playerUUID);
        saveClans();
        Log("Player " + Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).getName() + " has successfully left the clan " + clan.getId());
    }
    public void kickPlayer(UUID playerUUID, String targetName) {
        Player player = Bukkit.getPlayer(playerUUID);
        OfflinePlayer target = VClans.getInstance().getServer().getOfflinePlayer(targetName);
        Clan clan = getClanByMember(playerUUID);
        clan.removeMember(target.getUniqueId());
        saveClans();
        Log("Player " + Objects.requireNonNull(player).getName() + " has kicked player " + targetName + " from the clan.");
    }
    public void assignRank(UUID targetUUID, String rankName) {
        Clan clan = getClanByMember(targetUUID);
        ClanMember member = clan.getMembers().get(targetUUID);
        member.setRankId(rankName);
        saveClans();
    }
    public void addRank(Clan clan, ClanRank newRank) {
        ClanRank rank = new ClanRank(newRank.getTitle(), newRank.getId());
        rank.setPriority(newRank.getPriority());
        HashMap<ClanRankPermission, Boolean> permissions = newRank.getPermissions();
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
    public void setClanColor(Clan clan, int r, int g, int b) {
        clan.setColor(r, g, b);
        saveClans();
        BlueMapHandler blueMapHandler = VClans.getInstance().getBlueMapHandler();
        if (blueMapHandler != null) {
            blueMapHandler.drawClanTerritory(clan);
        }
        VClans.getInstance().getChunkHandler().updateChunkRadarForAll();
    }
    public void upgradeClan(Clan clan) {
        clan.setTier(clan.getTier() + 1);

        saveClans();
    }
    public void downgradeClan(Clan clan) {
        if (clan.getTier() == 0) return;
        clan.setTier(clan.getTier() - 1);
        saveClans();
    }
    public void setClanPrefix(Clan clan, String prefix) {
        clan.setPrefix(prefix);
        saveClans();
        for (UUID memberUUID : clan.getMemberUUIDs()) {
            Player player = Bukkit.getPlayer(memberUUID);
            VClans.getInstance().getVaultHandler().setPlayerPrefix(player, clan.getPrefix());
        }
    }
    public void setClanName(Clan clan, String name) {
        clan.setName(name);
        saveClans();
    }

    // saves and loads the clans
    public void loadClans() {
        clans = new Clans();
        List<Clan> clansList = JsonUtils.deserializeClans("clans.json");
        if (clansList == null) {
            Log("Clans file is empty. Creating new clans.json file.", LogType.WARNING);
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
                names.add(clan.getId());
        }
        return names;
    }
    public List<UUID> getClanMembersUUIDs(UUID playerUUID) {
        Clan clan = getClanByMember(playerUUID);
        if (clan == null)
            return List.of();
        return clan.getMemberUUIDs();
    }
    public List<UUID> getClanMemberUUIDs(String name) {
        Clan clan = getClanById(name);
        if (clan == null)
            return List.of();
        return clan.getMemberUUIDs();
    }

    public String getClanNameOfMember(UUID playerUUID) {
        Clan clan = getClanByMember(playerUUID);
        if (clan == null)
            return null;
        return clan.getId();
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
        return clans.getClans().stream().anyMatch(c -> c.getId().equalsIgnoreCase(name));
    }
    public boolean isPlayerInClan(UUID playerUUID, String clanName) {
        Clan clan = getClanByMember(playerUUID);
        return clan != null && clan.getId().equals(clanName);
    }
    public boolean clanIsFull(Clan clan) {
        return clan.getMembers().size() >= VClans.getInstance().getClanTierHandler().getPlayerLimit(clan.getTier());
    }
    public boolean hasPermission(Player player, ClanRankPermission permission) {
        Clan clan = getClanByMember(player.getUniqueId());
        if (clan == null) return false;
        HashMap<ClanRankPermission, Boolean> permissions = clan.getRank(player.getUniqueId()).getPermissions();
        if (permissions == null) return false;
        if (!permissions.containsKey(permission)) return false;
        return permissions.get(permission);
    }

    public Clan getClanById(String id) {
        return clans.getClans().stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }
    public Clan getClanByMember(UUID memberUUID) {
        return clans.getClans().stream().filter(c -> c.getMembers().containsKey(memberUUID)).findFirst().orElse(null);
    }
    public Clan getClanByChunkLocation(int x, int z) {
        for (Clan clan : clans.getClans()) {
            if (clan.getChunks() == null || clan.getChunks().isEmpty()) {
                continue;
            }
            for (ClanChunk chunk : clan.getChunks()) {
                if (chunk.getX() == x && chunk.getZ() == z) {
                    return clan;
                }
            }
        }
        return null;
    }
    public Clan getClanByChunkLocation(Chunk chunk) {
        for (Clan clan : clans.getClans()) {
            if (clan.getChunks() == null || clan.getChunks().isEmpty()) {
                continue;
            }
            for (ClanChunk clanChunk : clan.getChunks()) {
                if (clanChunk.getX() == chunk.getX() && clanChunk.getZ() == chunk.getZ() && chunk.getWorld().getName().equals(WORLD_NAME)) {
                    return clan;
                }
            }
        }
        return null;
    }
    private Clan createDefaultClan(String name, UUID ownerUUID) {
        Clan clan = new Clan(name, ownerUUID);
        clan.addOwnerMember(ownerUUID);
        //TODO: make sure to read the config here for default titles
        clan.createRank("owner", "leader");
        ClanRank ownerRank = clan.getRankById("owner");
        HashMap<ClanRankPermission, Boolean> permissions = ownerRank.getPermissions();
        permissions.replaceAll((k, v) -> true);
        ownerRank.setPermissions(permissions);
        ownerRank.setPriority(99);
        clan.createRank("default", "member");
        return clan;
    }
}
