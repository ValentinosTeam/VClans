package gg.valentinos.alexjoo.Data;

import java.util.List;
import java.util.UUID;

public class Clan {
    private String name;
    private List<UUID> members;
    private List<UUID> owners;
    private List<UUID> invites;

    public Clan(String name, List<UUID> members, List<UUID> owners, List<UUID> invites) {
        this.name = name;
        this.members = members;
        this.owners = owners;
        this.invites = invites;
    }

    public Clan(String name, List<UUID> members, List<UUID> owners) {
        this.name = name;
        this.members = members;
        this.owners = owners;
        this.invites = List.of();
    }


    public List<UUID> getMembers() {
        return members;
    }

    public void setMembers(List<UUID> members) {
        this.members = members;
    }

    public List<UUID> getOwners() {
        return owners;
    }

    public void setOwners(List<UUID> owners) {
        this.owners = owners;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UUID> getInvites() {
        return invites;
    }

    public void setInvites(List<UUID> invites) {
        this.invites = invites;
    }

    public boolean isOwner(UUID uuid) {
        return owners.contains(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public void addMember(UUID uuid) {
        invites.remove(uuid);
        members.add(uuid);
    }

    public void inviteMember(UUID uuid) {
        invites.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public void addOwner(UUID uuid) {
        owners.add(uuid);
    }

    public void removeOwner(UUID uuid) {
        owners.remove(uuid);
    }

    public void stepDownOwner(UUID uuid) {
        owners.remove(uuid);
        members.add(uuid);
    }

    public boolean isMemberOrOwner(UUID uuid) {
        return isMember(uuid) || isOwner(uuid);
    }


}
