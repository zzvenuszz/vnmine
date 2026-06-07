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
 * Spawn NPC tại vị trí cấu hình, quản lý shop
 */
public class NPCManager {

    private final VNMinePlugin plugin;
    private boolean enabled;

    private final Map<String, NPCData> npcConfigs;
    private final Map<String, Entity> spawnedNPCs;

    // File lưu vị trí NPC
    private File locationFile;
    private FileConfiguration locationConfig;

    public NPCManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        this.npcConfigs = new LinkedHashMap<>();
        this.spawnedNPCs = new ConcurrentHashMap<>();
        loadConfig();
        initLocationFile();
        loadLocations();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection npcSection = config.getConfigurationSection("npc-shop");
        if (npcSection == null) {
            enabled = false;
            return;
        }

        enabled = npcSection.getBoolean("enabled", true);
        npcConfigs.clear();

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

            npcConfigs.put(npcId, data);
        }
    }

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

    private void loadLocations() {
        for (String npcId : npcConfigs.keySet()) {
            spawnNPC(npcId);
        }
        plugin.getLogger().info("Loaded " + spawnedNPCs.size() + " NPCs.");
    }

    /**
     * Spawn NPC tại vị trí đã lưu
     */
    public void spawnNPC(String npcId) {
        NPCData data = npcConfigs.get(npcId);
        if (data == null || !data.isEnabled()) return;

        // Đã spawn rồi
        if (spawnedNPCs.containsKey(npcId)) return;

        // Lấy vị trí từ file
        String path = "npcs." + npcId;
        if (!locationConfig.contains(path)) {
            plugin.getLogger().warning("No location for NPC '" + npcId + "'. Use /vnnpc create <id>");
            return;
        }

        String worldName = locationConfig.getString(path + ".world");
        double x = locationConfig.getDouble(path + ".x");
        double y = locationConfig.getDouble(path + ".y");
        double z = locationConfig.getDouble(path + ".z");
        float yaw = (float) locationConfig.getDouble(path + ".yaw", 0);

        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Location loc = new Location(world, x, y, z, yaw, 0);

        // Spawn villager
        Villager villager = (Villager) world.spawnEntity(loc, EntityType.VILLAGER);
        villager.setCustomName(ColorUtils.colorize(data.getName()));
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setCollidable(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setRemoveWhenFarAway(false);
        villager.setVillagerType(Villager.Type.PLAINS);

        // Set profession
        try {
            Villager.Profession prof = Villager.Profession.valueOf(data.getProfession().toUpperCase());
            villager.setProfession(prof);
        } catch (IllegalArgumentException e) {
            villager.setProfession(Villager.Profession.NITWIT);
        }

        // Metadata
        villager.setMetadata("vnmine_npc_id", new FixedMetadataValue(plugin, npcId));
        villager.addScoreboardTag("vnmine_npc_" + npcId);

        spawnedNPCs.put(npcId, villager);
    }

    /**
     * Despawn NPC
     */
    public void despawnNPC(String npcId) {
        Entity entity = spawnedNPCs.remove(npcId);
        if (entity != null) {
            entity.remove();
        }
    }

    /**
     * Despawn tất cả NPC
     */
    public void despawnAll() {
        for (Entity entity : spawnedNPCs.values()) {
            entity.remove();
        }
        spawnedNPCs.clear();
    }

    /**
     * Lưu vị trí NPC tại nơi player đang đứng
     */
    public void saveNPCLocation(String npcId, Player player) {
        Location loc = player.getLocation();
        String path = "npcs." + npcId;
        locationConfig.set(path + ".world", loc.getWorld().getName());
        locationConfig.set(path + ".x", loc.getX());
        locationConfig.set(path + ".y", loc.getY());
        locationConfig.set(path + ".z", loc.getZ());
        locationConfig.set(path + ".yaw", loc.getYaw());
        try {
            locationConfig.save(locationFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save NPC location!");
        }
    }

    public NPCData getNPCData(String id) { return npcConfigs.get(id); }
    public Map<String, NPCData> getNPCConfigs() { return npcConfigs; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public void reload() {
        despawnAll();
        loadConfig();
        loadLocations();
    }
}