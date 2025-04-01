package gg.valentinos.alexjoo.Data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class ClanRank {
    private String title;
    private String id;
    private int priority;

    private boolean canDisband;
    private boolean canInvite;
    private boolean canKick;
    private boolean canEditRank;
    private boolean canCreateRank;
    private boolean canDeleteRank;
    private boolean canChangeRank;

    public ClanRank(String title, String id) {
        this.title = title;
        this.id = id;
    }

    public boolean canDisband() {
        return canDisband;
    }
    public void setCanDisband(boolean canDisband) {
        this.canDisband = canDisband;
    }
    public boolean canInvite() {
        return canInvite;
    }
    public void setCanInvite(boolean canInvite) {
        this.canInvite = canInvite;
    }
    public boolean canKick() {
        return canKick;
    }
    public void setCanKick(boolean canKick) {
        this.canKick = canKick;
    }
    public boolean canEditRank() {
        return canEditRank;
    }
    public void setCanEditRank(boolean canEditRank) {
        this.canEditRank = canEditRank;
    }
    public boolean canCreateRank() {
        return canCreateRank;
    }
    public void setCanCreateRank(boolean canCreateRank) {
        this.canCreateRank = canCreateRank;
    }
    public boolean canDeleteRank() {
        return canDeleteRank;
    }
    public void setCanDeleteRank(boolean canDeleteRank) {
        this.canDeleteRank = canDeleteRank;
    }
    public boolean canChangeRank() {
        return canChangeRank;
    }
    public void setCanChangeRank(boolean canChangeRank) {
        this.canChangeRank = canChangeRank;
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
    public String getId() {
        return id;
    }

    public Component getRankInfo(){
        Component component = Component.text("Rank: " + title).append(Component.newline());
        component = component.append(Component.text("Rank ID: " + id)).append(Component.newline());
        component = component.append(Component.text("Priority: " + priority)).append(Component.newline());
        Component temp = Component.text(canDisband);
        if (!canDisband)
            temp = temp.color(TextColor.color(255,0,0));
        else
            temp = temp.color(TextColor.color(0,255,0));
        component = component.append(Component.text("Can Disband: ")).append(temp).append(Component.newline());
        temp = Component.text(canInvite);
        if (!canInvite)
            temp = temp.color(TextColor.color(255,0,0));
        else
            temp = temp.color(TextColor.color(0,255,0));
        component = component.append(Component.text("Can Invite: ")).append(temp).append(Component.newline());
        temp = Component.text(canKick);
        if (!canKick)
            temp = temp.color(TextColor.color(255,0,0));
        else
            temp = temp.color(TextColor.color(0,255,0));
        component = component.append(Component.text("Can Kick: ")).append(temp).append(Component.newline());
        temp = Component.text(canEditRank);
        if (!canEditRank)
            temp = temp.color(TextColor.color(255,0,0));
        else
            temp = temp.color(TextColor.color(0,255,0));
        component = component.append(Component.text("Can Edit Rank: ")).append(temp).append(Component.newline());
        temp = Component.text(canCreateRank);
        if (!canCreateRank)
            temp = temp.color(TextColor.color(255,0,0));
        else
            temp = temp.color(TextColor.color(0,255,0));
        component = component.append(Component.text("Can Create Rank: ")).append(temp).append(Component.newline());
        temp = Component.text(canDeleteRank);
        if (!canDeleteRank)
            temp = temp.color(TextColor.color(255,0,0));
        else
            temp = temp.color(TextColor.color(0,255,0));
        component = component.append(Component.text("Can Delete Rank: ")).append(temp).append(Component.newline());
        temp = Component.text(canChangeRank);
        if (!canChangeRank)
            temp = temp.color(TextColor.color(255,0,0));
        else
            temp = temp.color(TextColor.color(0,255,0));
        component = component.append(Component.text("Can Change Rank: ")).append(temp).append(Component.newline());
//        Log(component.toString(), LogType.NULL);
        return component;
    }


}
