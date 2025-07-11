package gg.valentinos.alexjoo.Data;

import gg.valentinos.alexjoo.VClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class Clan {
    private String name;
    private int maxSize;
    private final UUID owner;
    private HashMap<String, ClanRank> ranks;
    private HashMap<UUID, ClanMember> members;
    private HashMap<UUID, ClanInvite> invites;
    private HashSet<ClanChunk> chunks;
    private List<Integer> color;

    public Clan(String name, UUID owner, int maxSize) {
        this.name = name;
        this.members = new HashMap<>();
        this.invites = new HashMap<>();
        this.ranks = new HashMap<>();
        this.chunks = new HashSet<>();
        this.maxSize = maxSize;
        this.owner = owner;
        this.color = List.of(211, 211, 211);
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
    public HashSet<ClanChunk> getChunks() {
        return chunks;
    }
    public void setChunks(HashSet<ClanChunk> chunks) {
        this.chunks = chunks;
    }
    public int getMaxSize() {
        return maxSize;
    }
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
    public List<Integer> getColor() {
        return color;
    }
    public void setColor(List<Integer> color) {
        this.color = color;
    }

    // Member logic
    public void addMember(UUID uuid, String rank) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        ClanMember member = new ClanMember(player.getName(), rank);
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
    public List<UUID> getMemberUUIDsFromRank(String rank) {
        List<UUID> uuids = new ArrayList<>();
        members.forEach((uuid, member) -> {
            if (member.getRankId().equals(rank)) {
                uuids.add(uuid);
            }
        });
        return uuids;
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
    public void addRank(ClanRank rank) {
        ranks.put(rank.getId(), rank);
    }
    public void removeRank(String name) {
        ranks.remove(name);
        //TODO: set rank to default for all members with this rank
    }
    public void setRank(UUID playerUUID, String rankId) {
        ClanMember member = members.get(playerUUID);
        if (member != null && ranks.containsKey(rankId)) {
            member.setRankId(rankId);
        }
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
    public Component getRankInfo(ClanRank rank) {
        Component component = Component.text("Rank: " + rank.getTitle()).append(Component.newline());
        component = component.append(Component.text("Rank ID: " + rank.getId())).append(Component.newline());
        component = component.append(Component.text("Priority: " + rank.getPriority())).append(Component.newline());

        HashMap<String, Boolean> permissions = rank.getPermissions();
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            String permission = entry.getKey();
            boolean value = entry.getValue();
            Component temp = Component.text(value);
            if (!value)
                temp = temp.color(TextColor.color(255, 0, 0));
            else
                temp = temp.color(TextColor.color(0, 255, 0));
            component = component.append(Component.text(permission + ": ")).append(temp).append(Component.newline());
        }

        List<UUID> uuids = getMemberUUIDsFromRank(rank.getId());
        StringBuilder sb = new StringBuilder();
        sb.append("Members: ");
        for (UUID uuid : uuids) {
            sb.append(Bukkit.getOfflinePlayer(uuid).getName()).append(", ");
        }
        int lastCommaIndex = sb.lastIndexOf(",");
        if (lastCommaIndex != -1)
            sb.setCharAt(lastCommaIndex, '.');
        component = component.append(Component.text(sb.toString()));

        return component;
    }

    // Chunk logic
    public void addChunk(ClanChunk chunk) {
        chunks.add(chunk);
    }
    public void removeChunk(ClanChunk chunk) {
        chunks.remove(chunk);
    }
    public ClanChunk getChunkByLocation(int x, int z) {
        for (ClanChunk chunk : chunks) {
            if (chunk.getX() == x && chunk.getZ() == z) {
                return chunk;
            }
        }
        return null;
    }

    // Color logic
    public void setColor(int r, int g, int b) {
        this.color = List.of(r, g, b);
    }
    public Component getColoredName() {
        return Component.text(name).color(TextColor.color(color.get(0), color.get(1), color.get(2)));
    }
}
