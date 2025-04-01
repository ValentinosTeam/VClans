package gg.valentinos.alexjoo.Data;

import gg.valentinos.alexjoo.VClans;

import java.util.*;
import java.util.stream.Collectors;

public class Clan {
    private String name;
    private int maxSize;
    private final UUID owner;
    private HashMap<String, ClanRank> ranks;
    private HashMap<UUID, ClanMember> members;
    private HashMap<UUID, ClanInvite> invites;

    public Clan(String name, UUID owner, int maxSize) {
        this.name = name;
        this.members = new HashMap<>();
        this.invites = new HashMap<>();
        this.ranks = new HashMap<>();
        this.maxSize = maxSize;
        this.owner = owner;
    }

    public HashMap<UUID, ClanMember> getMembers() {
        return members;
    }
    public void setMembers(HashMap<UUID, ClanMember> members) {
        this.members = members;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public HashMap<UUID, ClanInvite> getInvites() {
        return invites;
    }
    public void setInvites(HashMap<UUID, ClanInvite> invites) {
        this.invites = invites;
    }
    public HashMap<String, ClanRank> getRanks() {
        return ranks;
    }
    public void setRanks(HashMap<String, ClanRank> ranks) {
        this.ranks = ranks;
    }
    public int getMaxSize() {
        return maxSize;
    }
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    // Member logic
    public void addMember(UUID uuid, String rank) {
        ClanMember member = new ClanMember(rank);
        members.put(uuid, member);
    }
    public void addOwnerMember(UUID uuid) {
        invites.remove(uuid);
        addMember(uuid, "owner");
    }
    public void addDefaultMember(UUID uuid) {
        invites.remove(uuid);
        addMember(uuid, "default");
    }
    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }
    public List<UUID> getMemberUUIDs() {
        return List.copyOf(members.keySet());
    }
    public boolean isPlayerMember(UUID uuid) {
        return members.containsKey(uuid);
    }
    public boolean isPlayerOwner(UUID uuid) {
        return owner.equals(uuid);
    }
    public boolean isFull() {
        return members.size() >= maxSize;
    }
    public List<UUID> getMembersSortedByPriority() {
        return members.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> -ranks.get(entry.getValue().getRankId()).getPriority()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Invite logic
    public void inviteMember(UUID inviterUUID, UUID inviteeUUID) {
        //TODO: make sure to read the config here for the invite duration
        ClanInvite invite = new ClanInvite(inviterUUID, 30);
        invites.put(inviteeUUID, invite);
    }
    public boolean isMemberInvited(UUID uuid) {
        if (invites.containsKey(uuid)) {
            if (isInviteExpired(uuid)) {
                invites.remove(uuid);
                return false;
            }
            return true;
        }
        return false;
    }
    public boolean isInviteExpired(UUID uuid) {
        ClanInvite invite = invites.get(uuid);
        VClans.getInstance().getLogger().info("invite exists: " + (invite != null));
        if (invite == null) return true;
        if (invite.isExpired()) {
            invites.remove(uuid);
            return true;
        }
        return false;
    }

    // Rank logic
    public void createRank(String id, String title) {
        ranks.put(id, new ClanRank(title, id));
    }
    public ClanRank getRankById(String id) {
        return ranks.get(id);
    }
    public ClanRank getRank(UUID playerUUID) {
        return ranks.get(members.get(playerUUID).getRankId());
    }
    public String getMemberRankTitle(UUID playerUUID) {
        return ranks.get(members.get(playerUUID).getRankId()).getTitle();
    }
    public void removeRank(String name) {
        ranks.remove(name);
    }
    public void setRank(UUID playerUUID, String rankId) {
        ClanMember member = members.get(playerUUID);
        if (member != null && ranks.containsKey(rankId)) {
            member.setRankId(rankId);
        }
    }

}
