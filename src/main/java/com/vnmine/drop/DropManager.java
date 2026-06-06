package com.vnmine.drop;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class DropManager {
    private final JavaPlugin plugin;
    private boolean enabled;
    private boolean replaceEnabled;
    private boolean breakEnabled;
    private boolean explodeEnabled;
    private List<DropRule> rules;

    public DropManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.rules = new ArrayList<>();
    }

    public void load() {
        rules.clear();
        FileConfiguration config = plugin.getConfig();

        if (!config.contains("block-drop-settings")) {
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("block-drop-settings");
        if (section == null) return;

        enabled = section.getBoolean("enabled", true);
        replaceEnabled = section.getBoolean("replace-enabled", true);
        breakEnabled = section.getBoolean("break-enabled", true);
        explodeEnabled = section.getBoolean("explode-enabled", true);

        // Load rules
        List<Map<?, ?>> ruleMaps = section.getMapList("rules");
        for (Map<?, ?> ruleMap : ruleMaps) {
            DropRule rule = parseRule(ruleMap);
            if (rule != null) {
                rules.add(rule);
            }
        }

        plugin.getLogger().info("§aBlock drop settings loaded: " + rules.size() + " rules");
    }

    @SuppressWarnings("unchecked")
    private DropRule parseRule(Map<?, ?> ruleMap) {
        String blockName = (String) ruleMap.get("block");
        if (blockName == null) return null;

        Material blockMaterial = Material.getMaterial(blockName.toUpperCase());
        if (blockMaterial == null) {
            plugin.getLogger().warning("§cUnknown block material: " + blockName);
            return null;
        }

        DropRule rule = new DropRule(blockMaterial);

        // Parse each tool-specific configuration
        for (Map.Entry<?, ?> entry : ruleMap.entrySet()) {
            String key = (String) entry.getKey();
            if (key.equals("block")) continue;

            if (entry.getValue() instanceof Map) {
                Map<String, Object> toolConfig = (Map<String, Object>) entry.getValue();
                ToolConfig tc = parseToolConfig(toolConfig);
                if (tc != null) {
                    rule.getToolConfigs().put(key, tc);
                }
            }
        }

        return rule;
    }

    @SuppressWarnings("unchecked")
    private ToolConfig parseToolConfig(Map<String, Object> toolMap) {
        ToolConfig tc = new ToolConfig();

        // Drop-replace
        if (toolMap.containsKey("drop-replace")) {
            Map<String, Object> drMap = (Map<String, Object>) toolMap.get("drop-replace");
            tc.dropReplaceEnabled = (Boolean) drMap.getOrDefault("enabled", true);
            String replaceBlockStr = (String) drMap.get("replace-block");
            if (replaceBlockStr != null) {
                tc.replaceBlock = Material.getMaterial(replaceBlockStr.toUpperCase());
            }
            Object chanceObj = drMap.get("chance");
            if (chanceObj instanceof Number) {
                tc.dropReplaceChance = ((Number) chanceObj).doubleValue();
            }
        }

        // Tool-break
        if (toolMap.containsKey("tool-break")) {
            Map<String, Object> tbMap = (Map<String, Object>) toolMap.get("tool-break");
            tc.toolBreakEnabled = (Boolean) tbMap.getOrDefault("enabled", true);
            Object chanceObj = tbMap.get("chance");
            if (chanceObj instanceof Number) {
                tc.toolBreakChance = ((Number) chanceObj).doubleValue();
            }
            tc.toolBreakMessage = (String) tbMap.getOrDefault("message", "&cCúp của bạn đã bị gãy!");
        }

        // Explode
        if (toolMap.containsKey("explode")) {
            Map<String, Object> exMap = (Map<String, Object>) toolMap.get("explode");
            tc.explodeEnabled = (Boolean) exMap.getOrDefault("enabled", true);
            Object powerObj = exMap.get("power");
            if (powerObj instanceof Number) {
                tc.explodePower = ((Number) powerObj).floatValue();
            }
            tc.explodeBreakBlocks = (Boolean) exMap.getOrDefault("break-blocks", true);
            tc.explodeSetFire = (Boolean) exMap.getOrDefault("set-fire", false);
            tc.explodeDamagePlayer = (Boolean) exMap.getOrDefault("damage-player", true);
            Object explodeChance = exMap.get("chance");
            if (explodeChance instanceof Number) {
                tc.explodeChance = ((Number) explodeChance).doubleValue();
            }
            tc.explodeMessage = (String) exMap.getOrDefault("message", "&cKhối phát nổ!");
        }

        return tc;
    }

    public DropRule getRule(Material block) {
        for (DropRule rule : rules) {
            if (rule.getBlock() == block) {
                return rule;
            }
        }
        return null;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isReplaceEnabled() { return replaceEnabled; }
    public void setReplaceEnabled(boolean replaceEnabled) { this.replaceEnabled = replaceEnabled; }

    public boolean isBreakEnabled() { return breakEnabled; }
    public void setBreakEnabled(boolean breakEnabled) { this.breakEnabled = breakEnabled; }

    public boolean isExplodeEnabled() { return explodeEnabled; }
    public void setExplodeEnabled(boolean explodeEnabled) { this.explodeEnabled = explodeEnabled; }

    public List<DropRule> getRules() { return rules; }

    // Inner classes for drop rule data
    public static class DropRule {
        private final Material block;
        private final Map<String, ToolConfig> toolConfigs;

        public DropRule(Material block) {
            this.block = block;
            this.toolConfigs = new LinkedHashMap<>();
        }

        public Material getBlock() { return block; }
        public Map<String, ToolConfig> getToolConfigs() { return toolConfigs; }

        public ToolConfig getToolConfig(String toolType) {
            return toolConfigs.get(toolType);
        }
    }

    public static class ToolConfig {
        private boolean dropReplaceEnabled = true;
        private Material replaceBlock = Material.COAL;
        private double dropReplaceChance = 30.0;

        private boolean toolBreakEnabled = true;
        private double toolBreakChance = 5.0;
        private String toolBreakMessage = "&cCúp của bạn đã bị gãy!";

        private boolean explodeEnabled = true;
        private float explodePower = 3.0f;
        private boolean explodeBreakBlocks = true;
        private boolean explodeSetFire = false;
        private boolean explodeDamagePlayer = true;
        private double explodeChance = 10.0;
        private String explodeMessage = "&cKhối phát nổ!";

        public boolean isDropReplaceEnabled() { return dropReplaceEnabled; }
        public Material getReplaceBlock() { return replaceBlock; }
        public double getDropReplaceChance() { return dropReplaceChance; }

        public boolean isToolBreakEnabled() { return toolBreakEnabled; }
        public double getToolBreakChance() { return toolBreakChance; }
        public String getToolBreakMessage() { return toolBreakMessage; }

        public boolean isExplodeEnabled() { return explodeEnabled; }
        public float getExplodePower() { return explodePower; }
        public boolean isExplodeBreakBlocks() { return explodeBreakBlocks; }
        public boolean isExplodeSetFire() { return explodeSetFire; }
        public boolean isExplodeDamagePlayer() { return explodeDamagePlayer; }
        public double getExplodeChance() { return explodeChance; }
        public String getExplodeMessage() { return explodeMessage; }
    }
}