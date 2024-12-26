package gg.valentinos.alexjoo.Data;

import java.util.List;
import java.util.UUID;

public class Clan {
    private String name;
    private List<UUID> members;
    private List<UUID> owners;

    public Clan(String name, List<UUID> members, List<UUID> owners) {
        this.name = name;
        this.members = members;
        this.owners = owners;
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
}
