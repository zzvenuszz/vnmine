package com.vnmine.npc;

import com.vnmine.VNMinePlugin;
import com.vnmine.npc.NPCData.NPCTrade;
import com.vnmine.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NPCManager - Quản lý NPC trong game
 * 
 * Cấu trúc dữ liệu:
 * - npcTemplates: template NPC từ config.yml (skill_master, artifact_master, ...)
 * - npcInstances: các NPC instance đã được tạo và spawn (npc_001, npc_002, ...)
 * - npc_locations.yml: lưu dữ liệu vĩnh viễn (vị trí, loại, tên custom, skin)
 */
public class NPCManager {

    private final VNMinePlugin plugin;
    private boolean enabled;

    // Template NPC từ config.yml (skill_master, artifact_master, ...)
    private final Map<String, NPCData> npcTemplates;

    // NPC instance đã spawn (npc_001 -> NPCInstanceData có entity)
    private final Map<String, NPCInstanceData> npcInstances;

    // File lưu vị trí NPC
    private File locationFile;
    private FileConfiguration locationConfig;

    public NPCManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        this.npcTemplates = new LinkedHashMap<>();
        this.npcInstances = new ConcurrentHashMap<>();
        loadConfig();
        initLocationFile();
        loadInstances();
    }

    // ===================== TEMPLATE CONFIG =====================

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection npcSection = config.getConfigurationSection("npc-shop");
        if (npcSection == null) {
            enabled = false;
            return;
        }

        enabled = npcSection.getBoolean("enabled", true);
        npcTemplates.clear();

        ConfigurationSection npcs = npcSection.getConfigurationSection("npcs");
        if (npcs == null) return;

        for (String npcId : npcs.getKeys(false)) {
            ConfigurationSection npcData = npcs.getConfigurationSection(npcId);
            if (npcData == null) continue;

            if (!npcData.getBoolean("enabled", true)) continue;

            NPCData data = new NPCData(npcId);
            data.setName(npcData.getString("name", npcId));
            data.setEntityType(npcData.getString("type", "VILLAGER"));
            data.setProfession(npcData.getString("profession", "NITWIT"));

            // Load trades
            ConfigurationSection tradesSection = npcData.getConfigurationSection("trades");
            if (tradesSection != null) {
                List<NPCTrade> trades = new ArrayList<>();
                for (String tradeId : tradesSection.getKeys(false)) {
                    ConfigurationSection tradeSection = tradesSection.getConfigurationSection(tradeId);
                    if (tradeSection != null) {
                        trades.add(NPCTrade.fromConfig(tradeId, tradeSection));
                    }
                }
                data.setTrades(trades);
            }

            npcTemplates.put(npcId, data);
        }
    }

    // ===================== LOCATION FILE =====================

    private void initLocationFile() {
        locationFile = new File(plugin.getDataFolder(), "npc_locations.yml");
        if (!locationFile.exists()) {
            try {
                locationFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create npc_locations.yml!");
            }
        }
        locationConfig = YamlConfiguration.loadConfiguration(locationFile);
    }

    private void loadInstances() {
        ConfigurationSection instances = locationConfig.getConfigurationSection("instances");
        if (instances == null) return;

        for (String id : instances.getKeys(false)) {
            spawnNPC(id);
        }
        plugin.getLogger().info("Loaded " + npcInstances.size() + " NPC instances.");
    }

    // ===================== SPAWN / DESPAWN =====================

    /**
     * Spawn NPC từ dữ liệu trong file
     */
    public void spawnNPC(String instanceId) {
        String path = "instances." + instanceId;
        if (!locationConfig.contains(path)) {
            plugin.getLogger().warning("No location for NPC '" + instanceId + "'. Use /vnnpc create <id> <type>");
            return;
        }

        // Đã spawn rồi
        if (npcInstances.containsKey(instanceId)) return;

        String type = locationConfig.getString(path + ".type");
        String customName = locationConfig.getString(path + ".name", null);

        // Kiểm tra template
        NPCData template = npcTemplates.get(type);
        if (template == null) {
            plugin.getLogger().warning("NPC instance '" + instanceId + "' has unknown type: " + type);
            return;
        }

        // Lấy vị trí
        String worldName = locationConfig.getString(path + ".world");
        double x = locationConfig.getDouble(path + ".x");
        double y = locationConfig.getDouble(path + ".y");
        double z = locationConfig.getDouble(path + ".z");
        float yaw = (float) locationConfig.getDouble(path + ".yaw", 0);

        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Location loc = new Location(world, x, y, z, yaw, 0);

        // Tên hiển thị: custom name nếu có, nếu không thì dùng tên từ template
        String displayName = (customName != null && !customName.isEmpty())
                ? customName
                : template.getName();

        // Spawn villager
        Villager villager = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
        villager.setCustomName(ColorUtils.colorize(displayName));
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setCollidable(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setRemoveWhenFarAway(false);
        villager.setVillagerType(Villager.Type.PLAINS);

        // Set profession từ template
        try {
            Villager.Profession prof = Villager.Profession.valueOf(template.getProfession().toUpperCase());
            villager.setProfession(prof);
        } catch (IllegalArgumentException e) {
            villager.setProfession(Villager.Profession.NITWIT);
        }

        // Metadata
        villager.setMetadata("vnmine_npc_id", new FixedMetadataValue(plugin, instanceId));
        villager.addScoreboardTag("vnmine_npc_" + instanceId);

        // Lưu instance data
        NPCInstanceData data = new NPCInstanceData(instanceId, type, displayName, loc);
        data.setEntity(villager);
        npcInstances.put(instanceId, data);
    }

    /**
     * Despawn NPC (xóa entity khỏi thế giới)
     */
    public void despawnNPC(String instanceId) {
        NPCInstanceData data = npcInstances.remove(instanceId);
        if (data != null && data.getEntity() != null) {
            data.getEntity().remove();
        }
    }

    /**
     * Despawn tất cả NPC
     */
    public void despawnAll() {
        for (NPCInstanceData data : npcInstances.values()) {
            if (data.getEntity() != null) {
                data.getEntity().remove();
            }
        }
        npcInstances.clear();
    }

    // ===================== CRUD OPERATIONS =====================

    /**
     * Tạo NPC instance mới
     * /vnnpc create <id> <type> [name]
     */
    public boolean createNPC(String instanceId, String type, Player player, String customName) {
        NPCData template = npcTemplates.get(type);
        if (template == null) return false;

        // Kiểm tra ID đã tồn tại
        if (locationConfig.contains("instances." + instanceId)) return false;

        // Tên hiển thị
        String name = (customName != null && !customName.isEmpty())
                ? customName
                : template.getName();

        // Lưu vị trí
        Location loc = player.getLocation();
        String path = "instances." + instanceId;
        locationConfig.set(path + ".type", type);
        locationConfig.set(path + ".name", name);
        locationConfig.set(path + ".world", loc.getWorld().getName());
        locationConfig.set(path + ".x", loc.getX());
        locationConfig.set(path + ".y", loc.getY());
        locationConfig.set(path + ".z", loc.getZ());
        locationConfig.set(path + ".yaw", loc.getYaw());
        locationConfig.set(path + ".pitch", 0);
        saveLocationFile();

        // Spawn
        spawnNPC(instanceId);

        return true;
    }

    /**
     * Xóa NPC vĩnh viễn (despawn + xóa data file)
     * /vnnpc remove <id>
     */
    public boolean removeNPC(String instanceId) {
        // Despawn entity
        despawnNPC(instanceId);

        // Xóa khỏi file
        String path = "instances." + instanceId;
        if (!locationConfig.contains(path)) return false;

        locationConfig.set(path, null);
        saveLocationFile();

        return true;
    }

    /**
     * Xóa tất cả NPC
     * /vnnpc removeall
     */
    public void removeAllNPCs() {
        despawnAll();
        locationConfig.set("instances", null);
        saveLocationFile();
    }

    /**
     * Xóa tất cả NPC theo loại
     * /vnnpc removeall <type>
     */
    public int removeNPCsByType(String type) {
        ConfigurationSection instances = locationConfig.getConfigurationSection("instances");
        if (instances == null) return 0;

        int count = 0;
        List<String> toRemove = new ArrayList<>();

        for (String id : instances.getKeys(false)) {
            String instanceType = locationConfig.getString("instances." + id + ".type");
            if (type.equals(instanceType)) {
                toRemove.add(id);
            }
        }

        for (String id : toRemove) {
            despawnNPC(id);
            locationConfig.set("instances." + id, null);
            count++;
        }

        if (count > 0) saveLocationFile();
        return count;
    }

    /**
     * Di chuyển NPC đến vị trí người chơi
     * /vnnpc movehere <id>
     */
    public boolean moveNPC(String instanceId, Player player) {
        String path = "instances." + instanceId;
        if (!locationConfig.contains(path)) return false;

        // Despawn cũ
        despawnNPC(instanceId);

        // Cập nhật vị trí
        Location loc = player.getLocation();
        locationConfig.set(path + ".world", loc.getWorld().getName());
        locationConfig.set(path + ".x", loc.getX());
        locationConfig.set(path + ".y", loc.getY());
        locationConfig.set(path + ".z", loc.getZ());
        locationConfig.set(path + ".yaw", loc.getYaw());
        locationConfig.set(path + ".pitch", 0);
        saveLocationFile();

        // Spawn lại
        spawnNPC(instanceId);

        return true;
    }

    /**
     * Đổi tên NPC
     * /vnnpc rename <id> <name>
     */
    public boolean renameNPC(String instanceId, String newName) {
        String path = "instances." + instanceId;
        if (!locationConfig.contains(path)) return false;

        locationConfig.set(path + ".name", newName);
        saveLocationFile();

        // Despawn cũ rồi spawn lại để cập nhật tên
        despawnNPC(instanceId);
        spawnNPC(instanceId);

        return true;
    }

    /**
     * Đổi skin NPC (lưu tên skin, spawn dạng villager - skin chỉ hiệu quả khi có ProtocolLib)
     * /vnnpc skin <id> <playerName>
     */
    public boolean setSkinNPC(String instanceId, String skinName) {
        String path = "instances." + instanceId;
        if (!locationConfig.contains(path)) return false;

        locationConfig.set(path + ".skin", skinName);
        saveLocationFile();

        // respawn
        spawnNPC(instanceId);

        return true;
    }

    // ===================== FILE HELPERS =====================

    private void saveLocationFile() {
        try {
            locationConfig.save(locationFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save npc_locations.yml!");
        }
    }

    // ===================== QUERY METHODS =====================

    /**
     * Lấy danh sách tất cả instance IDs từ file
     */
    public List<String> getAllInstanceIds() {
        ConfigurationSection instances = locationConfig.getConfigurationSection("instances");
        if (instances == null) return new ArrayList<>();
        return new ArrayList<>(instances.getKeys(false));
    }

    /**
     * Lấy danh sách instance IDs theo loại từ file
     */
    public List<String> getInstanceIdsByType(String type) {
        ConfigurationSection instances = locationConfig.getConfigurationSection("instances");
        if (instances == null) return new ArrayList<>();

        List<String> result = new ArrayList<>();
        for (String id : instances.getKeys(false)) {
            String instanceType = locationConfig.getString("instances." + id + ".type");
            if (type.equals(instanceType)) {
                result.add(id);
            }
        }
        return result;
    }

    /**
     * Lấy template NPC data từ config theo type
     */
    public NPCData getTemplate(String type) {
        return npcTemplates.get(type);
    }

    /**
     * Lấy NPCData từ instance ID (tra type → template)
     * Dùng cho NPCListener khi click NPC để mở shop
     */
    public NPCData getNPCDataFromInstance(String instanceId) {
        String type = getInstanceType(instanceId);
        if (type == null) return null;
        return npcTemplates.get(type);
    }

    public Set<String> getTemplateIds() {
        return npcTemplates.keySet();
    }

    /**
     * Lấy instance data đang spawn
     */
    public NPCInstanceData getInstanceData(String instanceId) {
        return npcInstances.get(instanceId);
    }

    /**
     * Lấy type của instance từ file
     */
    public String getInstanceType(String instanceId) {
        return locationConfig.getString("instances." + instanceId + ".type");
    }

    /**
     * Lấy thông tin instance để hiển thị
     */
    public NPCDisplayInfo getDisplayInfo(String instanceId) {
        String path = "instances." + instanceId;
        if (!locationConfig.contains(path)) return null;

        String type = locationConfig.getString(path + ".type");
        String name = locationConfig.getString(path + ".name");
        String skin = locationConfig.getString(path + ".skin", null);
        boolean isSpawned = npcInstances.containsKey(instanceId);

        return new NPCDisplayInfo(instanceId, type, name, skin, isSpawned);
    }

    // ===================== GETTERS =====================

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Map<String, NPCInstanceData> getSpawnedInstances() { return npcInstances; }
    public boolean hasTemplate(String type) { return npcTemplates.containsKey(type); }

    public void reload() {
        despawnAll();
        loadConfig();
        loadInstances();
    }

    // ===================== INNER CLASSES =====================

    /**
     * Dữ liệu của một NPC instance đã spawn
     */
    public static class NPCInstanceData {
        private final String id;
        private final String type;
        private String displayName;
        private final Location location;
        private org.bukkit.entity.Entity entity;

        public NPCInstanceData(String id, String type, String displayName, Location location) {
            this.id = id;
            this.type = type;
            this.displayName = displayName;
            this.location = location;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public Location getLocation() { return location; }
        public org.bukkit.entity.Entity getEntity() { return entity; }
        public void setEntity(org.bukkit.entity.Entity entity) { this.entity = entity; }
    }

    /**
     * Thông tin hiển thị của NPC instance
     */
    public static class NPCDisplayInfo {
        private final String id;
        private final String type;
        private final String name;
        private final String skin;
        private final boolean spawned;

        public NPCDisplayInfo(String id, String type, String name, String skin, boolean spawned) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.skin = skin;
            this.spawned = spawned;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getName() { return name; }
        public boolean hasCustomName() { return name != null && !name.isEmpty(); }
        public String getSkin() { return skin; }
        public boolean isSpawned() { return spawned; }
    }
}