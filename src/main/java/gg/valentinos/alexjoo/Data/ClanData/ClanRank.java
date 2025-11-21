package gg.valentinos.alexjoo.Data.ClanData;

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
    public HashMap<String, Boolean> copyPermissions() {
        HashMap<String, Boolean> newPermissions = new HashMap<>();
        for (String key : permissions.keySet()) {
            newPermissions.put(key, permissions.get(key));
        }
        return newPermissions;
    }
    public void setPermissions(HashMap<String, Boolean> permissions) {
        this.permissions = permissions;
    }
    public static HashMap<String, Boolean> createDefaultPermissions() {
        HashMap<String, Boolean> newPermissions = new HashMap<>();
        newPermissions.put("canDisband", false);
        newPermissions.put("canInvite", false);
        newPermissions.put("canKick", false);
        newPermissions.put("canEditRank", false);
        newPermissions.put("canCreateRank", false);
        newPermissions.put("canDeleteRank", false);
        newPermissions.put("canChangeRank", false);
        newPermissions.put("canClaimChunks", false);
        newPermissions.put("canUnclaimChunks", false);
        newPermissions.put("canColor", false);
        newPermissions.put("canUpgrade", false);
        newPermissions.put("canSetPrefix", false);
        newPermissions.put("canRename", false);
        newPermissions.put("canDeclareWar", false);
        return newPermissions;
    }
}
