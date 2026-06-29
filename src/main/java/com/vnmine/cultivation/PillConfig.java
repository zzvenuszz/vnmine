package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * PillConfig - Đọc flavor texts và effects cho đan dược từ cultivation.yml
 */
public class PillConfig {

    private final VNMinePlugin plugin;
    private final Map<String, PillEffect> effects = new HashMap<>();
    private final Map<String, java.util.List<String>> flavorTexts = new HashMap<>();
    private final Random random = new Random();
    private boolean effectsEnabled = true;
    private boolean flavorsEnabled = true;

    public PillConfig(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        // Đọc trực tiếp từ file cultivation.yml thay vì plugin.getConfig()
        // Vì getConfig() chỉ đọc config.yml, không bao gồm cultivation.yml
        File cultivationFile = new File(plugin.getDataFolder(), "cultivation.yml");
        if (!cultivationFile.exists()) {
            plugin.saveResource("cultivation.yml", false);
            plugin.getLogger().info("[PillConfig] Đã tạo file cultivation.yml từ resources");
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(cultivationFile);
        
        // Load pill effects
        ConfigurationSection effectSection = config.getConfigurationSection("pill-effects");
        if (effectSection == null) {
            plugin.getLogger().warning("[PillConfig] Không tìm thấy section 'pill-effects' trong cultivation.yml");
        } else {
            effects.clear();
            for (String key : effectSection.getKeys(false)) {
                ConfigurationSection pillSec = effectSection.getConfigurationSection(key);
                if (pillSec == null) continue;
                
                String loreTemplate = pillSec.getString("lore-template", "");
                int baseRecover = pillSec.getInt("base-recover", 0);
                int baseHeal = pillSec.getInt("base-heal", 0);
                int baseRegen = pillSec.getInt("base-regen", 0);
                int baseDuration = pillSec.getInt("base-duration", 0);
                int baseDmg = pillSec.getInt("base-dmg", 0);
                int baseExp = pillSec.getInt("base-exp", 0);
                boolean scaleWithGrade = pillSec.getBoolean("scale-with-grade", false);
                boolean scaleDuration = pillSec.getBoolean("scale-duration", false);
                
                effects.put(key.toUpperCase(), new PillEffect(
                    loreTemplate, baseRecover, baseHeal, baseRegen, baseDuration, baseDmg, baseExp,
                    scaleWithGrade, scaleDuration
                ));
            }
            plugin.getLogger().info("[PillConfig] Đã load " + effects.size() + " loại đan dược effects từ cultivation.yml");
        }
        
        // Load flavor texts
        ConfigurationSection flavorSection = config.getConfigurationSection("pill-flavor-texts");
        if (flavorSection == null) {
            plugin.getLogger().warning("[PillConfig] Không tìm thấy section 'pill-flavor-texts' trong cultivation.yml");
            flavorsEnabled = false;
        } else {
            flavorTexts.clear();
            int flavorCount = 0;
            for (String key : flavorSection.getKeys(false)) {
                java.util.List<String> flavors = flavorSection.getStringList(key);
                if (!flavors.isEmpty()) {
                    flavorTexts.put(key.toUpperCase(), flavors);
                    flavorCount++;
                }
            }
            plugin.getLogger().info("[PillConfig] Đã load " + flavorCount + " loại đan dược flavor texts từ cultivation.yml");
            flavorsEnabled = true;
        }
    }

    public PillEffect getEffect(String pillId) {
        if (pillId == null) return null;
        return effects.get(pillId.toUpperCase());
    }

    public String getRandomFlavor(String pillId) {
        if (!flavorsEnabled) return "";
        java.util.List<String> flavors = flavorTexts.get(pillId.toUpperCase());
        if (flavors == null || flavors.isEmpty()) return "";
        return flavors.get(random.nextInt(flavors.size()));
    }

    public boolean isEffectsEnabled() {
        return effectsEnabled;
    }

    public boolean isFlavorsEnabled() {
        return flavorsEnabled;
    }

    public void reload() {
        load();
    }

    /**
     * PillEffect - Chứa thông tin hiệu ứng của 1 loại đan dược
     */
    public static class PillEffect {
        public final String loreTemplate;
        public final int baseRecover;
        public final int baseHeal;
        public final int baseRegen;
        public final int baseDuration;
        public final int baseDmg;
        public final int baseExp;
        public final boolean scaleWithGrade;
        public final boolean scaleDuration;

        public PillEffect(String loreTemplate, int baseRecover, int baseHeal, int baseRegen, int baseDuration, int baseDmg, int baseExp, boolean scaleWithGrade, boolean scaleDuration) {
            this.loreTemplate = loreTemplate;
            this.baseRecover = baseRecover;
            this.baseHeal = baseHeal;
            this.baseRegen = baseRegen;
            this.baseDuration = baseDuration;
            this.baseDmg = baseDmg;
            this.baseExp = baseExp;
            this.scaleWithGrade = scaleWithGrade;
            this.scaleDuration = scaleDuration;
        }

        /**
         * Tạo lore với giá trị đã scale theo phẩm cấp
         * @param multiplier Tỷ lệ nhân theo phẩm cấp (1.0, 1.3, 1.6, ...)
         * @return Lore string đã replace các placeholder
         */
        public String getLore(double multiplier) {
            String lore = loreTemplate;
            
            // Tính duration multiplier (dùng giá trị riêng nếu scaleDuration, hoặc multiplier chung)
            double durationMult = scaleDuration ? multiplier : 1.0;
            
            // Heal (% HP) - scale theo grade
            if (baseHeal > 0) {
                int value = scaleWithGrade ? (int)(baseHeal * multiplier) : baseHeal;
                lore = lore.replace("{heal}", String.valueOf(value));
            }
            
            // Recover (linh lực) - scale theo grade
            if (baseRecover > 0) {
                int value = scaleWithGrade ? (int)(baseRecover * multiplier) : baseRecover;
                lore = lore.replace("{recover}", String.valueOf(value));
            }
            
            // Regen (hồi máu/giây) - scale theo grade
            if (baseRegen > 0) {
                int value = scaleWithGrade ? (int)(baseRegen * multiplier) : baseRegen;
                lore = lore.replace("{regen}", String.valueOf(value));
            }
            
            // Duration (thời gian) - scale nếu config bật
            if (baseDuration > 0) {
                int value = (int)(baseDuration * durationMult);
                lore = lore.replace("{duration}", String.valueOf(value));
            }
            
            // Dmg (sát thương %) - scale theo grade
            if (baseDmg > 0) {
                // Công thức riêng cho dmg: base + min(4, mult/2) * 5
                int bonus = Math.min(4, (int)(multiplier / 2)) * 5;
                int value = baseDmg + bonus;
                lore = lore.replace("{dmg}", String.valueOf(value));
            }
            
            // EXP - scale theo grade
            if (baseExp > 0) {
                int value = scaleWithGrade ? (int)(baseExp * multiplier) : baseExp;
                lore = lore.replace("{exp}", String.valueOf(value));
            }
            
            return lore;
        }
    }
}