package gg.valentinos.alexjoo.Data;

import com.google.gson.annotations.Expose;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.HashMap;

public class ClanRank {
    private String title;
    private int priority;
    private String id;

    private boolean canDisband;
    private boolean canInvite;
    private boolean canKick;
    private boolean canEditRank;
    private boolean canCreateRank;
    private boolean canDeleteRank;
    private boolean canChangeRank;

    @Expose(serialize = false, deserialize = false)
    private HashMap<String, Boolean> permissions = new HashMap<>();

    public ClanRank(String title, String id) {
        this.title = title;
        this.id = id;
    }

    public boolean canDisband() {
        return canDisband;
    }
    public void setCanDisband(boolean canDisband) {
        this.canDisband = canDisband;
        permissions.put("canDisband", canDisband);
    }
    public boolean canInvite() {
        return canInvite;
    }
    public void setCanInvite(boolean canInvite) {
        this.canInvite = canInvite;
        permissions.put("canInvite", canInvite);
    }
    public boolean canKick() {
        return canKick;
    }
    public void setCanKick(boolean canKick) {
        this.canKick = canKick;
        permissions.put("canKick", canKick);
    }
    public boolean canEditRank() {
        return canEditRank;
    }
    public void setCanEditRank(boolean canEditRank) {
        this.canEditRank = canEditRank;
        permissions.put("canEditRank", canEditRank);
    }
    public boolean canCreateRank() {
        return canCreateRank;
    }
    public void setCanCreateRank(boolean canCreateRank) {
        this.canCreateRank = canCreateRank;
        permissions.put("canCreateRank", canCreateRank);
    }
    public boolean canDeleteRank() {
        return canDeleteRank;
    }
    public void setCanDeleteRank(boolean canDeleteRank) {
        this.canDeleteRank = canDeleteRank;
        permissions.put("canDeleteRank", canDeleteRank);
    }
    public boolean canChangeRank() {
        return canChangeRank;
    }
    public void setCanChangeRank(boolean canChangeRank) {
        this.canChangeRank = canChangeRank;
        permissions.put("canChangeRank", canChangeRank);
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public HashMap<String, Boolean> getPermissions() {
        return permissions;
    }
}
