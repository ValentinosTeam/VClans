package gg.valentinos.alexjoo.Data;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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

    // Invite logic
    public void inviteMember(UUID inviterUUID, UUID inviteeUUID) {
        //TODO: make sure to read the config here for the invite duration
        ClanInvite invite = new ClanInvite(inviterUUID, 30);
        invites.put(inviteeUUID, invite);
    }
    public boolean isMemberInvited(UUID uuid) {
        return invites.containsKey(uuid);
    }
    public boolean isInviteExpired(UUID uuid) {
        ClanInvite invite = invites.get(uuid);
        if (invite == null) return true;
        return invite.isExpired();
    }

    // Rank logic
    public void createRank(String name, String title) {
        ranks.put(name, new ClanRank(name, title));
    }
    public ClanRank getRank(String name) {
        return ranks.get(name);
    }
    public ClanRank getRank(UUID playerUUID) {
        return getRank(members.get(playerUUID).getRankName());
    }
    public String getMemberRankTitle(UUID playerUUID) {
        return ranks.get(members.get(playerUUID).getRankName()).getTitle();
    }
    public void removeRank(String name) {
        ranks.remove(name);
    }

}
