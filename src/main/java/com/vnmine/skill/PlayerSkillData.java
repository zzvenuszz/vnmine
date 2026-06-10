package com.vnmine.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlayerSkillData - Dữ liệu skill bar và độ thành thục của người chơi
 * Lưu skill bar slots (0-8), proficiency của từng skill, cooldown tracking
 */
public class PlayerSkillData {

    private final UUID playerUUID;
    private final String playerName;

    // Skill Bar: 9 slots, lưu skill_id
    private final String[] skillBarSlots;

    // Proficiency: skill_id → số lần đã sử dụng
    private final Map<String, Integer> proficiencyMap;

    // Cooldown tracking: skill_id → thời gian kết thúc cooldown (millis)
    private final Map<String, Long> cooldownMap;

    // Bypass cooldown (cho phép admin bỏ qua cooldown)
    private boolean cooldownBypass;

    public PlayerSkillData(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.skillBarSlots = new String[9];
        this.proficiencyMap = new HashMap<>();
        this.cooldownMap = new HashMap<>();
        this.cooldownBypass = false;
    }

    // ==================== SKILL BAR ====================

    /**
     * Gán skill vào slot trong Skill Bar
     */
    public void setSkillBarSlot(int slot, String skillId) {
        if (slot >= 0 && slot < 9) {
            skillBarSlots[slot] = skillId;
        }
    }

    /**
     * Lấy skill_id từ slot
     */
    public String getSkillBarSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            return skillBarSlots[slot];
        }
        return null;
    }

    /**
     * Lấy toàn bộ skill bar
     */
    public String[] getSkillBarSlots() {
        return skillBarSlots.clone();
    }

    /**
     * Xóa skill khỏi slot
     */
    public void clearSkillBarSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            skillBarSlots[slot] = null;
        }
    }

    /**
     * Xóa toàn bộ skill bar
     */
    public void clearSkillBar() {
        for (int i = 0; i < 9; i++) {
            skillBarSlots[i] = null;
        }
    }

    // ==================== PROFICIENCY ====================

    /**
     * Lấy số lần đã sử dụng skill
     */
    public int getSkillUsageCount(String skillId) {
        return proficiencyMap.getOrDefault(skillId, 0);
    }

    /**
     * Tăng số lần sử dụng skill lên 1
     */
    public void incrementSkillUsage(String skillId) {
        proficiencyMap.put(skillId, proficiencyMap.getOrDefault(skillId, 0) + 1);
    }

    /**
     * Lấy cấp độ thành thục dựa vào số lần sử dụng
     */
    public ProficiencyLevel getProficiencyLevel(String skillId) {
        int usage = getSkillUsageCount(skillId);
        return ProficiencyLevel.fromUsage(usage);
    }

    /**
     * Lấy hệ số sức mạnh của skill dựa vào độ thành thục
     */
    public double getProficiencyMultiplier(String skillId) {
        return getProficiencyLevel(skillId).multiplier;
    }

    /**
     * Lấy giảm cooldown dựa vào độ thành thục (giây)
     */
    public int getCooldownReduction(String skillId) {
        ProficiencyLevel level = getProficiencyLevel(skillId);
        return level.cooldownReduction;
    }

    /**
     * Lấy tăng thời gian hiệu lực dựa vào độ thành thục
     */
    public double getDurationMultiplier(String skillId) {
        ProficiencyLevel level = getProficiencyLevel(skillId);
        return level.durationMultiplier;
    }

    /**
     * Set số lần sử dụng (cho load data)
     */
    public void setSkillUsage(String skillId, int count) {
        proficiencyMap.put(skillId, count);
    }

    /**
     * Lấy toàn bộ proficiency map
     */
    public Map<String, Integer> getProficiencyMap() {
        return proficiencyMap;
    }

    // ==================== COOLDOWN ====================

    /**
     * Kiểm tra skill có đang cooldown không
     */
    public boolean isOnCooldown(String skillId) {
        if (!cooldownMap.containsKey(skillId)) return false;
        long remaining = cooldownMap.get(skillId) - System.currentTimeMillis();
        return remaining > 0;
    }

    /**
     * Lấy thời gian cooldown còn lại (giây)
     */
    public long getCooldownRemaining(String skillId) {
        if (!cooldownMap.containsKey(skillId)) return 0;
        long remaining = cooldownMap.get(skillId) - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    /**
     * Set cooldown cho skill (cooldownSeconds là cooldown gốc từ config)
     * Proficiency sẽ giảm cooldown
     */
    public void setCooldown(String skillId, int cooldownSeconds) {
        int reduction = getCooldownReduction(skillId);
        int actualCooldown = Math.max(1, cooldownSeconds - reduction);
        cooldownMap.put(skillId, System.currentTimeMillis() + (actualCooldown * 1000L));
    }

    /**
     * Lấy thời gian cooldown thực tế sau khi giảm (giây)
     */
    public int getEffectiveCooldown(String skillId, int baseCooldown) {
        return Math.max(1, baseCooldown - getCooldownReduction(skillId));
    }

    /**
     * Lấy toàn bộ cooldown map (cho save)
     */
    public Map<String, Long> getCooldownMap() {
        return cooldownMap;
    }

    // ==================== COOLDOWN BYPASS ====================

    /**
     * Kiểm tra cooldown bypass
     */
    public boolean isCooldownBypass() {
        return cooldownBypass;
    }

    /**
     * Set cooldown bypass
     */
    public void setCooldownBypass(boolean bypass) {
        this.cooldownBypass = bypass;
    }

    // ==================== PROFICIENCY LEVEL ENUM ====================

    public enum ProficiencyLevel {
        NHAP_MON(0, 1.0, 0, 1.0),
        TIEU_THANH(50, 1.5, 1, 1.1),
        DAI_THANH(200, 2.0, 2, 1.2),
        VIEN_MAN(500, 3.0, 3, 1.3),
        XUAT_THAN_NHAP_HOA(1000, 4.5, 4, 1.4),
        DANG_PHONG_TAO_CUC(2000, 7.0, 5, 1.5);

        public final int requiredUsage;
        public final double multiplier;
        public final int cooldownReduction; // giây
        public final double durationMultiplier;

        ProficiencyLevel(int requiredUsage, double multiplier, int cooldownReduction, double durationMultiplier) {
            this.requiredUsage = requiredUsage;
            this.multiplier = multiplier;
            this.cooldownReduction = cooldownReduction;
            this.durationMultiplier = durationMultiplier;
        }

        /**
         * Lấy cấp độ thành thục dựa vào số lần sử dụng
         */
        public static ProficiencyLevel fromUsage(int usage) {
            ProficiencyLevel best = NHAP_MON;
            for (ProficiencyLevel level : values()) {
                if (usage >= level.requiredUsage) {
                    best = level;
                }
            }
            return best;
        }

        /**
         * Lấy tên hiển thị
         */
        public String getDisplayName() {
            switch (this) {
                case NHAP_MON: return "&7Nhập Môn";
                case TIEU_THANH: return "&aTiểu Thành";
                case DAI_THANH: return "&bĐại Thành";
                case VIEN_MAN: return "&dViên Mãn";
                case XUAT_THAN_NHAP_HOA: return "&6Xuất Thần Nhập Hóa";
                case DANG_PHONG_TAO_CUC: return "&c&lĐăng Phong Tạo Cực";
                default: return "&7Nhập Môn";
            }
        }
    }
}