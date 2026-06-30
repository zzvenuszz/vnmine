package com.vnmine.spiritfarm;

import com.vnmine.VNMinePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * SpiritFarmConfig - Đọc cấu hình hệ thống Linh Điền từ config.yml
 */
public class SpiritFarmConfig {

    private final VNMinePlugin plugin;
    private boolean enabled;

    // Thời gian phát triển cơ bản cho mỗi giai đoạn (giây)
    private long baseGrowthTime;

    // Block linh điền: grade index -> tên hiển thị
    private final Map<Integer, FarmBlockDef> blockDefs = new HashMap<>();

    public SpiritFarmConfig(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("spirit-farming");
        if (section == null) {
            enabled = false;
            plugin.getLogger().warning("[SpiritFarm] Không tìm thấy section 'spirit-farming' trong config.yml");
            return;
        }

        enabled = section.getBoolean("enabled", true);
        baseGrowthTime = section.getLong("base-growth-time-seconds", 300); // 5 phút mặc định

        // Đọc block definitions
        ConfigurationSection blocks = section.getConfigurationSection("blocks");
        if (blocks != null) {
            blockDefs.clear();
            for (String key : blocks.getKeys(false)) {
                ConfigurationSection block = blocks.getConfigurationSection(key);
                if (block == null) continue;
                try {
                    int grade = Integer.parseInt(key);
                    String name = block.getString("name", "Linh Điền");
                    String material = block.getString("material", "FARMLAND");
                    String lore = block.getString("lore", "&7Mảnh đất linh khí");
                    double growthMultiplier = block.getDouble("growth-multiplier", 1.0);
                    int maxYield = block.getInt("max-yield", 3);
                    blockDefs.put(grade, new FarmBlockDef(name, material, lore, growthMultiplier, maxYield));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("[SpiritFarm] Sai định dạng grade: " + key);
                }
            }
        }

        plugin.getLogger().info("[SpiritFarm] Đã load " + blockDefs.size() + " loại block linh điền");
    }

    public boolean isEnabled() { return enabled; }
    public long getBaseGrowthTime() { return baseGrowthTime; }

    public FarmBlockDef getBlockDef(int grade) {
        return blockDefs.get(grade);
    }

    public int getMaxGrade() {
        int max = 0;
        for (int g : blockDefs.keySet()) {
            if (g > max) max = g;
        }
        return max;
    }

    public static class FarmBlockDef {
        public final String name;
        public final String material;
        public final String lore;
        public final double growthMultiplier;
        public final int maxYield;

        public FarmBlockDef(String name, String material, String lore, double growthMultiplier, int maxYield) {
            this.name = name;
            this.material = material;
            this.lore = lore;
            this.growthMultiplier = growthMultiplier;
            this.maxYield = maxYield;
        }
    }
}