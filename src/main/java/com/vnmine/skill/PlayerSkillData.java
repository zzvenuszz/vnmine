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

    // Proficiency: skill_id → số điểm thành thục (points)
    private final Map<String, Integer> proficiencyPointsMap;

    // Proficiency: skill_id → số lần đã sử dụng (legacy, kept for backward compatibility)
    private final Map<String, Integer> proficiencyUsageMap;

    // Cooldown tracking: skill_id → thời gian kết thúc cooldown (millis)
    private final Map<String, Long> cooldownMap;

    // Bypass cooldown (cho phép admin bỏ qua cooldown)
    private boolean cooldownBypass;

    public PlayerSkillData(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.skillBarSlots = new String[9];
        this.proficiencyPointsMap = new HashMap<>();
        this.proficiencyUsageMap = new HashMap<>();
        this.cooldownMap = new HashMap<>();
        this.cooldownBypass = false;
    }

    // ==================== SKILL BAR ====================

    public void setSkillBarSlot(int slot, String skillId) {
        if (slot >= 0 && slot < 9) {
            skillBarSlots[slot] = skillId;
        }
    }

    public String getSkillBarSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            return skillBarSlots[slot];
        }
        return null;
    }

    public String[] getSkillBarSlots() {
        return skillBarSlots.clone();
    }

    public void clearSkillBarSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            skillBarSlots[slot] = null;
        }
    }

    public void clearSkillBar() {
        for (int i = 0; i < 9; i++) {
            skillBarSlots[i] = null;
        }
    }

    // ==================== PROFICIENCY ====================

    public int getSkillUsageCount(String skillId) {
        return proficiencyUsageMap.getOrDefault(skillId, 0);
    }

    public void incrementSkillUsage(String skillId) {
        proficiencyUsageMap.put(skillId, proficiencyUsageMap.getOrDefault(skillId, 0) + 1);
    }

    /**
     * Lấy số điểm thành thục hiện tại
     */
    public int getProficiencyPoints(String skillId) {
        return proficiencyPointsMap.getOrDefault(skillId, 0);
    }

    /**
     * Thêm điểm thành thục
     */
    public void addProficiencyPoints(String skillId, int points) {
        proficiencyPointsMap.put(skillId, getProficiencyPoints(skillId) + points);
    }

    /**
     * Reset điểm thành thục về 0 (khi học skill mới hoặc nâng cấp)
     */
    public void resetProficiencyPoints(String skillId) {
        proficiencyPointsMap.put(skillId, 0);
    }

    /**
     * Set số điểm thành thục (cho load/upgrade)
     */
    public void setProficiencyPoints(String skillId, int points) {
        proficiencyPointsMap.put(skillId, points);
    }

    /**
     * Lấy bậc thành thục dựa trên điểm thành thục từ config
     */
    public ProficiencyLevel getProficiencyLevel(String skillId) {
        int points = getProficiencyPoints(skillId);
        return ProficiencyLevel.fromPoints(points);
    }

    public double getProficiencyMultiplier(String skillId) {
        return getProficiencyLevel(skillId).multiplier;
    }

    public int getCooldownReduction(String skillId) {
        ProficiencyLevel level = getProficiencyLevel(skillId);
        return level.cooldownReduction;
    }

    public double getDurationMultiplier(String skillId) {
        ProficiencyLevel level = getProficiencyLevel(skillId);
        return level.durationMultiplier;
    }

    public void setSkillUsage(String skillId, int count) {
        proficiencyUsageMap.put(skillId, count);
    }

    public Map<String, Integer> getProficiencyUsageMap() {
        return proficiencyUsageMap;
    }

    public Map<String, Integer> getProficiencyPointsMap() {
        return proficiencyPointsMap;
    }

    // ==================== KHỐNG HỎA THUẬT EFFECTS ====================

    /**
     * Lấy tỉ lệ giảm thời gian luyện đan từ Khống Hỏa Thuật
     * @return 0.0 - 0.75 (giảm từ 0% đến 75%)
     */
    public double getAlchemyTimeReduction() {
        ProficiencyLevel level = getProficiencyLevel("FIRE_CONTROL");
        switch (level) {
            case NHAP_MON: return 0.0;
            case TIEU_THANH: return 0.15;
            case DAI_THANH: return 0.30;
            case VIEN_MAN: return 0.45;
            case XUAT_THAN_NHAP_HOA: return 0.60;
            case DANG_PHONG_TAO_CUC: return 0.75;
            default: return 0.0;
        }
    }

    /**
     * Lấy bonus phẩm cấp đan dược từ Khống Hỏa Thuật
     * @return 0 - 5 (cộng vào grade index)
     */
    public int getAlchemyGradeBonus() {
        ProficiencyLevel level = getProficiencyLevel("FIRE_CONTROL");
        switch (level) {
            case NHAP_MON: return 0;
            case TIEU_THANH: return 1;
            case DAI_THANH: return 2;
            case VIEN_MAN: return 3;
            case XUAT_THAN_NHAP_HOA: return 4;
            case DANG_PHONG_TAO_CUC: return 5;
            default: return 0;
        }
    }

    /**
     * Lấy tên hiển thị proficiency cho Khống Hỏa Thuật
     */
    public String getFireControlProficiencyName() {
        return getProficiencyLevel("FIRE_CONTROL").getDisplayName();
    }

    // ==================== LUYỆN KHÍ THUẬT EFFECTS ====================

    /**
     * Lấy tỉ lệ giảm tiêu hao linh lực pháp bảo từ Luyện Khí Thuật
     * @return 0.0 - 0.50 (giảm từ 0% đến 50% mana cost)
     */
    public double getForgeManaReduction() {
        ProficiencyLevel level = getProficiencyLevel("FORGE_MASTERY");
        switch (level) {
            case NHAP_MON: return 0.0;
            case TIEU_THANH: return 0.10;
            case DAI_THANH: return 0.20;
            case VIEN_MAN: return 0.30;
            case XUAT_THAN_NHAP_HOA: return 0.40;
            case DANG_PHONG_TAO_CUC: return 0.50;
            default: return 0.0;
        }
    }

    /**
     * Lấy bonus phẩm cấp pháp khí từ Luyện Khí Thuật
     * @return 0 - 5 (cộng vào grade index)
     */
    public int getForgeGradeBonus() {
        ProficiencyLevel level = getProficiencyLevel("FORGE_MASTERY");
        switch (level) {
            case NHAP_MON: return 0;
            case TIEU_THANH: return 1;
            case DAI_THANH: return 2;
            case VIEN_MAN: return 3;
            case XUAT_THAN_NHAP_HOA: return 4;
            case DANG_PHONG_TAO_CUC: return 5;
            default: return 0;
        }
    }

    /**
     * Lấy tên hiển thị proficiency cho Luyện Khí Thuật
     */
    public String getForgeMasteryProficiencyName() {
        return getProficiencyLevel("FORGE_MASTERY").getDisplayName();
    }

    // ==================== COOLDOWN ====================

    public boolean isOnCooldown(String skillId) {
        if (!cooldownMap.containsKey(skillId)) return false;
        long remaining = cooldownMap.get(skillId) - System.currentTimeMillis();
        return remaining > 0;
    }

    public long getCooldownRemaining(String skillId) {
        if (!cooldownMap.containsKey(skillId)) return 0;
        long remaining = cooldownMap.get(skillId) - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    public void setCooldown(String skillId, int cooldownSeconds) {
        int reduction = getCooldownReduction(skillId);
        int actualCooldown = Math.max(1, cooldownSeconds - reduction);
        cooldownMap.put(skillId, System.currentTimeMillis() + (actualCooldown * 1000L));
    }

    public int getEffectiveCooldown(String skillId, int baseCooldown) {
        return Math.max(1, baseCooldown - getCooldownReduction(skillId));
    }

    public Map<String, Long> getCooldownMap() {
        return cooldownMap;
    }

    // ==================== COOLDOWN BYPASS ====================

    public boolean isCooldownBypass() {
        return cooldownBypass;
    }

    public void setCooldownBypass(boolean bypass) {
        this.cooldownBypass = bypass;
    }

    // ==================== PROFICIENCY LEVEL ENUM ====================

    /**
     * Cấu hình cho từng bậc thành thục từ YAML
     */
    public static class ProficiencyConfig {
        public int requiredPoints;  // Điểm cần để lên bậc này
        public double damageMultiplier;
        public int cooldownReduction;
        public double durationMultiplier;
        public String displayName;

        public ProficiencyConfig(int requiredPoints, double damageMultiplier, int cooldownReduction, double durationMultiplier, String displayName) {
            this.requiredPoints = requiredPoints;
            this.damageMultiplier = damageMultiplier;
            this.cooldownReduction = cooldownReduction;
            this.durationMultiplier = durationMultiplier;
            this.displayName = displayName;
        }
    }

    public enum ProficiencyLevel {
        NHAP_MON(0, 1.0, 0, 1.0, "&7Nhập Môn"),
        TIEU_THANH(100, 1.5, 1, 1.1, "&aTiểu Thành"),
        DAI_THANH(200, 2.0, 2, 1.2, "&bĐại Thành"),
        VIEN_MAN(300, 3.0, 3, 1.3, "&dViên Mãn"),
        XUAT_THAN_NHAP_HOA(400, 4.5, 4, 1.4, "&6Xuất Thần Nhập Hóa"),
        DANG_PHONG_TAO_CUC(500, 7.0, 5, 1.5, "&c&lĐăng Phong Tạo Cực");

        public final int requiredPoints;
        public final double multiplier;
        public final int cooldownReduction;
        public final double durationMultiplier;
        public final String displayName;

        ProficiencyLevel(int requiredPoints, double multiplier, int cooldownReduction, double durationMultiplier, String displayName) {
            this.requiredPoints = requiredPoints;
            this.multiplier = multiplier;
            this.cooldownReduction = cooldownReduction;
            this.durationMultiplier = durationMultiplier;
            this.displayName = displayName;
        }

        /**
         * Lấy bậc thành thục dựa trên số điểm (points system)
         */
        public static ProficiencyLevel fromPoints(int points) {
            ProficiencyLevel best = NHAP_MON;
            for (ProficiencyLevel level : values()) {
                if (points >= level.requiredPoints) {
                    best = level;
                }
            }
            return best;
        }

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