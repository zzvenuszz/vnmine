package com.vnmine.permission;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class PermissionManager {
    private final JavaPlugin plugin;
    private boolean enabled;
    private Map<String, Group> groups;
    private Map<String, PlayerData> playerDataMap;

    public PermissionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.groups = new LinkedHashMap<>();
        this.playerDataMap = new HashMap<>();
    }

    public void load() {
        groups.clear();
        playerDataMap.clear();

        FileConfiguration config = plugin.getConfig();

        if (!config.contains("permission-system")) {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            config = plugin.getConfig();
        }

        enabled = config.getBoolean("permission-system.enabled", true);

        // Load groups
        ConfigurationSection groupsSection = config.getConfigurationSection("permission-system.groups");
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
                if (groupSection == null) continue;

                Group group = new Group(groupName);
                group.setWeight(groupSection.getInt("weight", 0));
                group.setPrefix(groupSection.getString("prefix", ""));
                group.setSuffix(groupSection.getString("suffix", ""));
                group.setDefault(groupSection.getBoolean("default", false));
                group.setParents(groupSection.getStringList("parents"));
                group.setPermissions(groupSection.getStringList("permissions"));
                groups.put(groupName, group);
            }
        }

        // Ensure default group exists
        if (groups.values().stream().noneMatch(Group::isDefault)) {
            Group defaultGroup = new Group("default");
            defaultGroup.setDefault(true);
            groups.put("default", defaultGroup);
        }

        // Load player data
        ConfigurationSection playersSection = config.getConfigurationSection("permission-system.players");
        if (playersSection != null) {
            for (String playerName : playersSection.getKeys(false)) {
                ConfigurationSection playerSection = playersSection.getConfigurationSection(playerName);
                if (playerSection == null) continue;

                PlayerData data = new PlayerData(playerName);
                data.setPrimaryGroup(playerSection.getString("group", "default"));
                data.setPermissions(playerSection.getStringList("permissions"));
                data.setPrefix(playerSection.getString("prefix", ""));
                data.setSuffix(playerSection.getString("suffix", ""));
                playerDataMap.put(playerName.toLowerCase(), data);
            }
        }

        plugin.getLogger().info("§aPermission system loaded: " + groups.size() + " groups, " + playerDataMap.size() + " players");
    }

    public boolean hasPermission(Player player, String node) {
        if (!enabled) {
            // Fallback to Bukkit permission
            return player.hasPermission(node);
        }

        // Check per-player permissions first (highest priority)
        PlayerData data = getPlayerData(player.getName());
        if (data != null) {
            if (checkNodeInList(data.getPermissions(), node)) {
                return true;
            }
            // Check negative permission
            if (checkNodeInList(data.getPermissions(), "-" + node)) {
                return false;
            }
        }

        // Get all permissions from player's groups
        Set<String> allPermissions = getAllPlayerPermissions(player.getName());

        // Check negative permissions first
        if (checkNodeInList(allPermissions, "-" + node)) {
            return false;
        }

        // Check positive permissions
        if (checkNodeInList(allPermissions, node)) {
            return true;
        }

        // Fallback to Bukkit permission
        return player.hasPermission(node);
    }

    private boolean checkNodeInList(Collection<String> permissions, String node) {
        for (String perm : permissions) {
            if (perm.trim().isEmpty()) continue;

            String cleaned = perm.trim();
            boolean negative = cleaned.startsWith("-");
            if (negative) {
                cleaned = cleaned.substring(1);
            }

            // Check exact match
            if (cleaned.equals(node)) {
                return !negative;
            }

            // Check wildcard match: vnmine.* matches vnmine.command.tps
            if (cleaned.endsWith(".*")) {
                String prefix = cleaned.substring(0, cleaned.length() - 2);
                if (node.startsWith(prefix + ".") || node.equals(prefix)) {
                    return !negative;
                }
            }

            // Check if node itself is wildcard and matches
            if (node.endsWith(".*") && cleaned.startsWith(node.substring(0, node.length() - 2))) {
                return !negative;
            }
        }
        return false;
    }

    private Set<String> getAllPlayerPermissions(String playerName) {
        Set<String> permissions = new HashSet<>();
        List<String> groupNames = getPlayerGroups(playerName);

        for (String groupName : groupNames) {
            Group group = groups.get(groupName);
            if (group != null) {
                permissions.addAll(group.getPermissions());
            }
        }

        return permissions;
    }

    public List<String> getPlayerGroups(String playerName) {
        List<String> result = new ArrayList<>();
        PlayerData data = getPlayerData(playerName);
        String primaryGroupName = (data != null) ? data.getPrimaryGroup() : "default";

        Group primaryGroup = groups.get(primaryGroupName);
        if (primaryGroup == null) {
            // Fallback to default
            primaryGroup = groups.values().stream()
                    .filter(Group::isDefault)
                    .findFirst()
                    .orElse(null);
            if (primaryGroup == null) return result;
            primaryGroupName = primaryGroup.getName();
        }

        // Collect all groups including parents
        Set<String> visited = new HashSet<>();
        collectGroups(primaryGroupName, result, visited);

        return result;
    }

    private void collectGroups(String groupName, List<String> result, Set<String> visited) {
        if (groupName == null || visited.contains(groupName)) return;
        visited.add(groupName);

        Group group = groups.get(groupName);
        if (group == null) return;

        // Add parent groups first (they have lower priority)
        for (String parent : group.getParents()) {
            collectGroups(parent, result, visited);
        }

        // Add current group
        if (!result.contains(groupName)) {
            result.add(groupName);
        }
    }

    public PlayerData getPlayerData(String playerName) {
        return playerDataMap.get(playerName.toLowerCase());
    }

    public PlayerData getOrCreatePlayerData(String playerName) {
        String key = playerName.toLowerCase();
        if (!playerDataMap.containsKey(key)) {
            playerDataMap.put(key, new PlayerData(playerName));
        }
        return playerDataMap.get(key);
    }

    public Group getGroup(String name) {
        return groups.get(name);
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public Group getDefaultGroup() {
        return groups.values().stream()
                .filter(Group::isDefault)
                .findFirst()
                .orElse(null);
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public void addGroup(Group group) {
        groups.put(group.getName(), group);
    }

    public void removeGroup(String name) {
        groups.remove(name);
    }

    public void saveToConfig() {
        FileConfiguration config = plugin.getConfig();
        config.set("permission-system.enabled", enabled);

        // Save groups
        for (Group group : groups.values()) {
            String path = "permission-system.groups." + group.getName();
            config.set(path + ".weight", group.getWeight());
            config.set(path + ".prefix", group.getPrefix());
            config.set(path + ".suffix", group.getSuffix());
            config.set(path + ".default", group.isDefault());
            config.set(path + ".parents", group.getParents());
            config.set(path + ".permissions", group.getPermissions());
        }

        // Save player data
        for (PlayerData data : playerDataMap.values()) {
            String path = "permission-system.players." + data.getPlayerName();
            config.set(path + ".group", data.getPrimaryGroup());
            config.set(path + ".permissions", data.getPermissions());
            config.set(path + ".prefix", data.getPrefix());
            config.set(path + ".suffix", data.getSuffix());
        }

        plugin.saveConfig();
        plugin.reloadConfig();
    }
}