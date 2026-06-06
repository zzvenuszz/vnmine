package com.vnmine.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class WorldManager {
    private final JavaPlugin plugin;
    private boolean enabled;
    private Map<String, WorldConfig> worldConfigs;

    public WorldManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.worldConfigs = new HashMap<>();
    }

    public void load() {
        worldConfigs.clear();
        FileConfiguration config = plugin.getConfig();

        if (!config.contains("world-settings")) {
            return;
        }

        enabled = config.getBoolean("world-settings.enabled", true);

        ConfigurationSection worldsSection = config.getConfigurationSection("world-settings.worlds");
        if (worldsSection == null) return;

        for (String worldName : worldsSection.getKeys(false)) {
            ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
            if (worldSection == null) continue;

            WorldConfig wc = new WorldConfig(worldName);
            wc.setGenerateIfNotExists(worldSection.getBoolean("generate-if-not-exists", true));
            wc.setSeed(worldSection.getString("seed", ""));

            // Parse world type
            String typeStr = worldSection.getString("type", "NORMAL");
            try {
                wc.setType(WorldType.valueOf(typeStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                wc.setType(WorldType.NORMAL);
            }

            // Parse environment
            String envStr = worldSection.getString("environment", "NORMAL");
            try {
                wc.setEnvironment(World.Environment.valueOf(envStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                wc.setEnvironment(World.Environment.NORMAL);
            }

            // Load ore rates
            ConfigurationSection oreSection = worldSection.getConfigurationSection("ore-rates");
            if (oreSection != null) {
                for (String key : oreSection.getKeys(false)) {
                    wc.getOreRates().put(key, oreSection.getDouble(key, 1.0));
                }
            }

            // Load structure rates
            ConfigurationSection structureSection = worldSection.getConfigurationSection("structure-rates");
            if (structureSection != null) {
                for (String key : structureSection.getKeys(false)) {
                    wc.getStructureRates().put(key, structureSection.getDouble(key, 1.0));
                }
            }

            // Load mob rates
            ConfigurationSection mobSection = worldSection.getConfigurationSection("mob-rates");
            if (mobSection != null) {
                for (String key : mobSection.getKeys(false)) {
                    wc.getMobRates().put(key, mobSection.getDouble(key, 1.0));
                }
            }

            worldConfigs.put(worldName, wc);
        }

        // Tự động tạo các world nếu chưa tồn tại
        if (enabled) {
            generateWorlds();
        }

        plugin.getLogger().info("§aWorld settings loaded: " + worldConfigs.size() + " worlds configured");
    }

    public boolean generateWorlds() {
        boolean anyGenerated = false;
        for (WorldConfig wc : worldConfigs.values()) {
            if (generateWorld(wc)) {
                anyGenerated = true;
            }
        }
        return anyGenerated;
    }

    private boolean generateWorld(WorldConfig wc) {
        if (!wc.isGenerateIfNotExists()) return false;

        // Check if world exists
        World world = Bukkit.getWorld(wc.getWorldName());
        if (world != null) {
            return false; // World already loaded
        }

        // Check if world folder exists
        String worldContainer = plugin.getServer().getWorldContainer().getAbsolutePath();
        File worldFolder = new File(worldContainer, wc.getWorldName());
        if (worldFolder.exists() && worldFolder.isDirectory()) {
            // World data exists but not loaded - load it
            world = createWorld(wc);
            if (world != null) {
                plugin.getLogger().info("§aLoaded existing world: " + wc.getWorldName());
                return true;
            }
            return false;
        }

        // World does not exist, create new one
        world = createWorld(wc);
        if (world != null) {
            plugin.getLogger().info("§aCreated new world: " + wc.getWorldName());
            applyWorldSettings(wc, world);
            return true;
        }

        return false;
    }

    private World createWorld(WorldConfig wc) {
        try {
            WorldCreator creator = new WorldCreator(wc.getWorldName());
            creator.type(wc.getType());
            creator.environment(wc.getEnvironment());

            if (wc.getSeed() != null && !wc.getSeed().isEmpty()) {
                try {
                    long seed = Long.parseLong(wc.getSeed());
                    creator.seed(seed);
                } catch (NumberFormatException e) {
                    creator.seed(wc.getSeed().hashCode());
                }
            }

            // Generate structures
            creator.generateStructures(true);

            return Bukkit.createWorld(creator);
        } catch (Exception e) {
            plugin.getLogger().warning("§cCould not create world '" + wc.getWorldName() + "': " + e.getMessage());
            return null;
        }
    }

    private void applyWorldSettings(WorldConfig wc, World world) {
        // Apply mob rates by adjusting spawn limits
        double hostileRate = wc.getMobRate("hostiles");
        double passiveRate = wc.getMobRate("passive");
        double ambientRate = wc.getMobRate("ambient");
        double waterRate = wc.getMobRate("water");

        // Adjust monster spawn limit
        int baseMonsterLimit = world.getMonsterSpawnLimit();
        if (hostileRate != 1.0) {
            world.setMonsterSpawnLimit((int) Math.max(1, baseMonsterLimit * hostileRate));
        }

        // Adjust animal spawn limit
        int baseAnimalLimit = world.getAnimalSpawnLimit();
        if (passiveRate != 1.0) {
            world.setAnimalSpawnLimit((int) Math.max(1, baseAnimalLimit * passiveRate));
        }

        // Adjust ambient spawn limit
        int baseAmbientLimit = world.getAmbientSpawnLimit();
        if (ambientRate != 1.0) {
            world.setAmbientSpawnLimit((int) Math.max(1, baseAmbientLimit * ambientRate));
        }

        // Adjust water spawn limit
        int baseWaterLimit = world.getWaterAnimalSpawnLimit();
        if (waterRate != 1.0) {
            world.setWaterAnimalSpawnLimit((int) Math.max(1, baseWaterLimit * waterRate));
        }

        plugin.getLogger().info("§aApplied world settings to: " + wc.getWorldName());
    }

    public boolean generateWorld(String worldName) {
        WorldConfig wc = worldConfigs.get(worldName);
        if (wc == null) {
            wc = new WorldConfig(worldName);
        }
        return generateWorld(wc);
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public WorldConfig getWorldConfig(String worldName) {
        return worldConfigs.get(worldName);
    }

    public Map<String, WorldConfig> getWorldConfigs() {
        return worldConfigs;
    }
}