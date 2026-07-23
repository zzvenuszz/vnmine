package com.vnmine.skill;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * ArtifactSkillManager - Quản lý kỹ năng pháp bảo (artifact skills)
 * Hệ thống skill độc lập cho pháp bảo, có thể gán cho nhiều artifact
 */
public class ArtifactSkillManager {

    private final VNMinePlugin plugin;
    private final Map<String, ArtifactSkillConfig> artifactSkills;

    public ArtifactSkillManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        this.artifactSkills = new LinkedHashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("artifact-skills");
        if (section == null) return;

        artifactSkills.clear();
        for (String skillId : section.getKeys(false)) {
            ConfigurationSection skillSection = section.getConfigurationSection(skillId);
            if (skillSection == null) continue;

            ArtifactSkillConfig cfg = new ArtifactSkillConfig();
            cfg.id = skillId;
            cfg.name = ColorUtils.colorize(skillSection.getString("name", "&f" + skillId));
            cfg.executor = skillSection.getString("executor", skillId);
            cfg.manaCost = skillSection.getInt("mana-cost", 0);
            cfg.cooldownSeconds = skillSection.getInt("cooldown-seconds", 0);
            cfg.icon = skillSection.getString("icon", "STONE");

            artifactSkills.put(skillId.toUpperCase(), cfg);
        }

        plugin.getLogger().info("Loaded " + artifactSkills.size() + " artifact skills.");
    }

    /**
     * Cast artifact skill
     */
    public void castArtifactSkill(Player player, String skillId, PlayerCultivationData data) {
        ArtifactSkillConfig skill = artifactSkills.get(skillId.toUpperCase());
        if (skill == null) {
            MessageUtils.send(player, "&cKỹ năng pháp bảo không tồn tại: " + skillId);
            return;
        }

        // Kiểm tra mana
        if (skill.manaCost > 0) {
            if (!plugin.getCultivationManager().consumeMana(player, skill.manaCost)) {
                MessageUtils.send(player, "&cKhông đủ linh lực! (Cần &b" + skill.manaCost + " &clinh lực)");
                return;
            }
        }

        MessageUtils.send(player, "&d✦ Thi triển pháp bảo: &e" + ColorUtils.stripColor(skill.name));
        MessageUtils.playSound(player, Sound.ENTITY_BLAZE_SHOOT);

        // Thực thi skill
        switch (skill.executor.toUpperCase()) {
            case "FLYING_SWORD_ACTIVATE":
                activateFlyingSword(player);
                break;
            case "FLYING_SWORD_ATTACK":
                flyingSwordAttack(player);
                break;
            default:
                MessageUtils.send(player, "&e✦ " + ColorUtils.stripColor(skill.name));
        }
    }

    /**
     * Kiếm Phi Hành - Kích hoạt
     */
    private void activateFlyingSword(Player player) {
        MessageUtils.send(player, "&b✦ Thiên Ngự Kiếm! ✦");
        // TODO: Implement flying sword session
    }

    /**
     * Kiếm Phi Hành - Tấn công
     */
    private void flyingSwordAttack(Player player) {
        MessageUtils.send(player, "&b✦ Kiếm Phi Hành: Tấn công!");
        // TODO: Implement flying sword attack
    }

    /**
     * Lấy artifact skill config
     */
    public ArtifactSkillConfig getSkill(String skillId) {
        return artifactSkills.get(skillId.toUpperCase());
    }

    /**
     * Lấy tất cả artifact skills
     */
    public Collection<ArtifactSkillConfig> getSkills() {
        return artifactSkills.values();
    }

    /**
     * Kiểm tra skill có tồn tại không
     */
    public boolean hasSkill(String skillId) {
        return artifactSkills.containsKey(skillId.toUpperCase());
    }

    /**
     * Reload config
     */
    public void reload() {
        loadConfig();
    }

    /**
     * Artifact Skill Config
     */
    public static class ArtifactSkillConfig {
        public String id;
        public String name;
        public String executor;
        public int manaCost;
        public int cooldownSeconds;
        public String icon;
    }
}