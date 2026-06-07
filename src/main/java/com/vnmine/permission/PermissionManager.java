package com.vnmine.permission;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PermissionManager - Hệ thống phân quyền giống LuckPerms
 * Load/Save từ file permissions.yml riêng
 * Hỗ trợ group hierarchy, wildcard, inheritance
 */
public class PermissionManager {
    private final JavaPlugin plugin;
    private boolean enabled;
    private Map<String, Group> groups;
    private Map<String, PlayerData> playerDataMap;
    private File permFile;
    private FileConfiguration permConfig;

    public PermissionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.groups = new LinkedHashMap<>();
        this.playerDataMap = new HashMap<>();
    }

    public void load() {
        groups.clear();
        playerDataMap.clear();

        // Load from permissions.yml instead of config.yml
        permFile = new File(plugin.getDataFolder(), "permissions.yml");
        if (!permFile.exists()) {
            plugin.saveResource("permissions.yml", false);
        }
        permConfig = YamlConfiguration.loadConfiguration(permFile);

        enabled = permConfig.getBoolean("enabled", true);

        // Load groups
        ConfigurationSection groupsSection = permConfig.getConfigurationSection("groups");
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
        ConfigurationSection playersSection = permConfig.getConfigurationSection("players");
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

        plugin.getLogger().info("§aPermission system loaded from permissions.yml: " + groups.size() + " groups, " + playerDataMap.size() + " players");
    }

    public boolean hasPermission(Player player, String node) {
        if (!enabled) {
            // Fallback to Bukkit permission
            return player.hasPermission(node) || player.isOp();
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

        // Check if player has "*" (all permissions)
        if (checkNodeInList(allPermissions, "*")) {
            return true;
        }

        // Fallback to Bukkit permission
        return player.hasPermission(node) || player.isOp();
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

        // Sort by weight descending
        result.sort((a, b) -> {
            Group ga = groups.get(a);
            Group gb = groups.get(b);
            int wa = (ga != null) ? ga.getWeight() : 0;
            int wb = (gb != null) ? gb.getWeight() : 0;
            return Integer.compare(wb, wa);
        });

        return result;
    }

    private void collectGroups(String groupName, List<String> result, Set<String> visited) {
        if (groupName == null || visited.contains(groupName)) return;
        visited.add(groupName);

        Group group = groups.get(groupName);
        if (group == null) return;

        // Add current group first, then parents
        if (!result.contains(groupName)) {
            result.add(groupName);
        }

        // Add parent groups
        for (String parent : group.getParents()) {
            collectGroups(parent, result, visited);
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
        if (permConfig == null) {
            permFile = new File(plugin.getDataFolder(), "permissions.yml");
            permConfig = YamlConfiguration.loadConfiguration(permFile);
        }

        permConfig.set("enabled", enabled);

        // Clear and re-save groups
        permConfig.set("groups", null);
        for (Group group : groups.values()) {
            String path = "groups." + group.getName();
            permConfig.set(path + ".weight", group.getWeight());
            permConfig.set(path + ".prefix", group.getPrefix());
            permConfig.set(path + ".suffix", group.getSuffix());
            permConfig.set(path + ".default", group.isDefault());
            permConfig.set(path + ".parents", group.getParents());
            permConfig.set(path + ".permissions", group.getPermissions());
        }

        // Clear and re-save players
        permConfig.set("players", null);
        for (PlayerData data : playerDataMap.values()) {
            String path = "players." + data.getPlayerName();
            permConfig.set(path + ".group", data.getPrimaryGroup());
            permConfig.set(path + ".permissions", data.getPermissions());
            permConfig.set(path + ".prefix", data.getPrefix());
            permConfig.set(path + ".suffix", data.getSuffix());
        }

        try {
            permConfig.save(permFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save permissions.yml: " + e.getMessage());
        }
    }
}