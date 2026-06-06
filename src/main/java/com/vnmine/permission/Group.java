package com.vnmine.permission;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String name;
    private int weight;
    private String prefix;
    private String suffix;
    private boolean isDefault;
    private List<String> parents;
    private List<String> permissions;

    public Group(String name) {
        this.name = name;
        this.weight = 0;
        this.prefix = "";
        this.suffix = "";
        this.isDefault = false;
        this.parents = new ArrayList<>();
        this.permissions = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public List<String> getParents() { return parents; }
    public void setParents(List<String> parents) { this.parents = parents; }
    public void addParent(String parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
        }
    }
    public void removeParent(String parent) {
        parents.remove(parent);
    }

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
}