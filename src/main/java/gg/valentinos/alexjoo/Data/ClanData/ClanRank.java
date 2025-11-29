package gg.valentinos.alexjoo.Data.ClanData;

import java.util.HashMap;

public class ClanRank {
    private String title;
    private int priority;
    private String id;
    private HashMap<ClanRankPermission, Boolean> permissions;

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
    public HashMap<ClanRankPermission, Boolean> getPermissions() {
        return permissions;
    }
    public HashMap<ClanRankPermission, Boolean> copyPermissions() {
        HashMap<ClanRankPermission, Boolean> newPermissions = new HashMap<>();
        for (ClanRankPermission key : permissions.keySet()) {
            newPermissions.put(key, permissions.get(key));
        }
        return newPermissions;
    }
    public void setPermissions(HashMap<ClanRankPermission, Boolean> permissions) {
        this.permissions = permissions;
    }
    public static HashMap<ClanRankPermission, Boolean> createDefaultPermissions() {
        HashMap<ClanRankPermission, Boolean> newPermissions = new HashMap<>();

        for (ClanRankPermission perm : ClanRankPermission.values()) {
            newPermissions.put(perm, false);
        }

        return newPermissions;
    }
}
