package com.vnmine.world;

import org.bukkit.World;
import org.bukkit.WorldType;

import java.util.HashMap;
import java.util.Map;

public class WorldConfig {
    private String worldName;
    private boolean generateIfNotExists;
    private String seed;
    private WorldType type;
    private World.Environment environment;
    private Map<String, Double> oreRates;
    private Map<String, Double> structureRates;
    private Map<String, Double> mobRates;

    public WorldConfig(String worldName) {
        this.worldName = worldName;
        this.generateIfNotExists = true;
        this.seed = "";
        this.type = WorldType.NORMAL;
        this.environment = World.Environment.NORMAL;
        this.oreRates = new HashMap<>();
        this.structureRates = new HashMap<>();
        this.mobRates = new HashMap<>();
    }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public boolean isGenerateIfNotExists() { return generateIfNotExists; }
    public void setGenerateIfNotExists(boolean generateIfNotExists) { this.generateIfNotExists = generateIfNotExists; }

    public String getSeed() { return seed; }
    public void setSeed(String seed) { this.seed = seed; }

    public WorldType getType() { return type; }
    public void setType(WorldType type) { this.type = type; }

    public World.Environment getEnvironment() { return environment; }
    public void setEnvironment(World.Environment environment) { this.environment = environment; }

    public Map<String, Double> getOreRates() { return oreRates; }
    public void setOreRates(Map<String, Double> oreRates) { this.oreRates = oreRates; }

    public Map<String, Double> getStructureRates() { return structureRates; }
    public void setStructureRates(Map<String, Double> structureRates) { this.structureRates = structureRates; }

    public Map<String, Double> getMobRates() { return mobRates; }
    public void setMobRates(Map<String, Double> mobRates) { this.mobRates = mobRates; }

    public double getOreRate(String ore) {
        return oreRates.getOrDefault(ore, 1.0);
    }

    public double getStructureRate(String structure) {
        return structureRates.getOrDefault(structure, 1.0);
    }

    public double getMobRate(String mob) {
        return mobRates.getOrDefault(mob, 1.0);
    }
}