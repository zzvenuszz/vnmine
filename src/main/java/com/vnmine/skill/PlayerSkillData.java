package com.vnmine.skill;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

/**
 * PlayerSkillData - Dữ liệu kỹ năng của người chơi
 * Lưu mastery từng skill, quickbar slots, cooldowns
 */
public class PlayerSkillData {

    // skill_id → masteryExp (tổng EXP mastery)
    private final Map<String, Integer> skillMastery;
    // Slot hotbar (0-8) → skill_id (null nếu trống)
    private final String[] quickbarSlots;
    // skill_id → thời gian kết thúc cooldown (millis)
    private final Map<String, Long> cooldowns;

    public PlayerSkillData() {
        this.skillMastery = new HashMap<>();
        this.quickbarSlots = new String[9];
        this.cooldowns = new HashMap<>();
    }

    // ==================== MASTERY ====================

    public int getMasteryExp(String skillId) {
        return skillMastery.getOrDefault(skillId, 0);
    }

    public void addMasteryExp(String skillId, int amount) {
        skillMastery.put(skillId, getMasteryExp(skillId) + amount);
    }

    public SkillMastery getMasteryLevel(String skillId) {
        return SkillMastery.getByExp(getMasteryExp(skillId));
    }

    public Map<String, Integer> getAllMastery() {
        return Collections.unmodifiableMap(skillMastery);
    }

    // ==================== QUICKBAR ====================

    /**
     * Lấy skill_id tại slot hotbar
     */
    public String getQuickbarSkill(int slot) {
        if (slot < 0 || slot >= 9) return null;
        return quickbarSlots[slot];
    }

    /**
     * Gán skill vào slot hotbar
     */
    public void setQuickbarSkill(int slot, String skillId) {
        if (slot < 0 || slot >= 9) return;
        quickbarSlots[slot] = skillId;
    }

    /**
     * Xóa skill khỏi slot
     */
    public void clearQuickbarSlot(int slot) {
        if (slot < 0 || slot >= 9) return;
        quickbarSlots[slot] = null;
    }

    /**
     * Lấy toàn bộ quickbar
     */
    public String[] getQuickbarSlots() {
        return Arrays.copyOf(quickbarSlots, 9);
    }

    /**
     * Tìm slot chứa skill (dùng để kiểm tra skill đã gán chưa)
     */
    public int findSkillSlot(String skillId) {
        for (int i = 0; i < 9; i++) {
            if (skillId.equals(quickbarSlots[i])) return i;
        }
        return -1;
    }

    // ==================== COOLDOWN ====================

    public boolean isOnCooldown(String skillId) {
        long endTime = cooldowns.getOrDefault(skillId, 0L);
        return System.currentTimeMillis() < endTime;
    }

    public long getCooldownRemaining(String skillId) {
        long endTime = cooldowns.getOrDefault(skillId, 0L);
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public void setCooldown(String skillId, int seconds) {
        cooldowns.put(skillId, System.currentTimeMillis() + (seconds * 1000L));
    }

    public void clearCooldown(String skillId) {
        cooldowns.remove(skillId);
    }

    // ==================== SERIALIZATION ====================

    /**
     * Lưu dữ liệu vào ConfigurationSection
     */
    public void saveToConfig(ConfigurationSection section) {
        // Lưu mastery
        ConfigurationSection masterySection = section.createSection("mastery");
        for (Map.Entry<String, Integer> entry : skillMastery.entrySet()) {
            masterySection.set(entry.getKey(), entry.getValue());
        }

        // Lưu quickbar
        ConfigurationSection quickbarSection = section.createSection("quickbar");
        for (int i = 0; i < 9; i++) {
            if (quickbarSlots[i] != null) {
                quickbarSection.set("slot-" + i, quickbarSlots[i]);
            }
        }
    }

    /**
     * Đọc dữ liệu từ ConfigurationSection
     */
    public static PlayerSkillData loadFromConfig(ConfigurationSection section) {
        PlayerSkillData data = new PlayerSkillData();

        if (section == null) return data;

        // Đọc mastery
        ConfigurationSection masterySection = section.getConfigurationSection("mastery");
        if (masterySection != null) {
            for (String key : masterySection.getKeys(false)) {
                data.skillMastery.put(key, masterySection.getInt(key, 0));
            }
        }

        // Đọc quickbar
        ConfigurationSection quickbarSection = section.getConfigurationSection("quickbar");
        if (quickbarSection != null) {
            for (String key : quickbarSection.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key.replace("slot-", ""));
                    if (slot >= 0 && slot < 9) {
                        data.quickbarSlots[slot] = quickbarSection.getString(key);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return data;
    }
}