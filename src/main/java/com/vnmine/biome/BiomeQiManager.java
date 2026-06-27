package com.vnmine.biome;

import com.vnmine.VNMinePlugin;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;

public class BiomeQiManager {
    private final VNMinePlugin plugin;
    private final Map<Biome, Double> regenBonus = new HashMap<>();
    private final Map<Biome, Double> drainPenalty = new HashMap<>();

    public BiomeQiManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        regenBonus.clear();
        drainPenalty.clear();
        var section = plugin.getConfig().getConfigurationSection("cultivation.biome-qi");
        if (section == null || !section.getBoolean("enabled", true)) return;

        var regenSection = section.getConfigurationSection("mana-regen-bonus");
        if (regenSection != null) {
            for (String key : regenSection.getKeys(false)) {
                try {
                    Biome biome = Biome.valueOf(key);
                    regenBonus.put(biome, regenSection.getDouble(key));
                } catch (IllegalArgumentException | NullPointerException ignored) {
                }
            }
        }

        var drainSection = section.getConfigurationSection("mana-drain-penalty");
        if (drainSection != null) {
            for (String key : drainSection.getKeys(false)) {
                try {
                    Biome biome = Biome.valueOf(key);
                    drainPenalty.put(biome, drainSection.getDouble(key));
                } catch (IllegalArgumentException | NullPointerException ignored) {
                }
            }
        }
    }

    public double getRegenBonus(Biome biome) {
        return regenBonus.getOrDefault(biome, 1.0);
    }

    public double getDrainPenalty(Biome biome) {
        return drainPenalty.getOrDefault(biome, 1.0);
    }
}
