package gg.valentinos.alexjoo.Data;

import com.google.gson.annotations.Expose;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.HashMap;

public class ClanRank {
    private String title;
    private int priority;
    private String id;
    private HashMap<String, Boolean> permissions;

    public ClanRank(String title, String id) {
        this.title = title;
        this.id = id;
        this.permissions = createDefaultPermissions();
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
    public void setPermissions(HashMap<String, Boolean> permissions) {
        this.permissions = permissions;
    }
    public static HashMap<String, Boolean> createDefaultPermissions() {
        HashMap<String, Boolean> permissions = new HashMap<>();
        permissions.put("canDisband", false);
        permissions.put("canInvite", false);
        permissions.put("canKick", false);
        permissions.put("canEditRank", false);
        permissions.put("canCreateRank", false);
        permissions.put("canDeleteRank", false);
        permissions.put("canChangeRank", false);
        return permissions;
    }
}
