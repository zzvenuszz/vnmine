package com.vnmine.permission;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    private String playerName;
    private String primaryGroup;
    private List<String> permissions;
    private String prefix;
    private String suffix;

    public PlayerData(String playerName) {
        this.playerName = playerName;
        this.primaryGroup = "default";
        this.permissions = new ArrayList<>();
        this.prefix = "";
        this.suffix = "";
    }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getPrimaryGroup() { return primaryGroup; }
    public void setPrimaryGroup(String primaryGroup) { this.primaryGroup = primaryGroup; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public void addPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }
    public void removePermission(String permission) {
        permissions.remove(permission);
    }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
}