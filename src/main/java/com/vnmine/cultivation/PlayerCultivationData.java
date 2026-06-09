package com.vnmine.cultivation;

import com.vnmine.skill.PlayerSkillData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlayerCultivationData - Dữ liệu tu luyện của người chơi
 * Lưu level, exp, linh lực, kỹ năng đã học...
 */
public class PlayerCultivationData {

    private final UUID playerUUID;
    private final String playerName;

    // Tu luyện
    private int level;
    private double experience;
    private double maxExperience; // Exp cần cho level tiếp theo

    // Linh lực (Spirit Power / Mana)
    private int mana;
    private int maxMana;

    // Kỹ năng đã học (skill_id → true)
    private Map<String, Boolean> learnedSkills;

    // Kỹ năng Passive đang active
    private Map<String, Boolean> activePassiveSkills;

    // Thống kê
    private int mobsKilled;
    private int elitesKilled;
    private int bossesKilled;
    private int pillsCrafted;
    private int herbsHarvested;

    // Trạng thái
    private long lastCombatTime;
    private long lastManaRegenTime;

    // Trạng thái độ kiếp
    private boolean waitingForTribulation; // Đang bị chặn ở threshold level
    private boolean tribulationInProgress; // Đang trong quá trình độ kiếp

    // Skill data (bar, proficiency, cooldown)
    private PlayerSkillData skillData;

    public PlayerCultivationData(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.level = 1;
        this.experience = 0;
        this.maxExperience = 100; // Level 1 → 2 cần 100 exp
        this.mana = 100;
        this.maxMana = 100;
        this.learnedSkills = new HashMap<>();
        this.activePassiveSkills = new HashMap<>();
        this.lastCombatTime = 0;
        this.lastManaRegenTime = System.currentTimeMillis();
        this.skillData = new PlayerSkillData(playerUUID, playerName);
    }

    // ==================== GETTERS & SETTERS ====================

    public UUID getPlayerUUID() { return playerUUID; }
    public String getPlayerName() { return playerName; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, Math.min(level, 100)); }

    public double getExperience() { return experience; }
    public void setExperience(double experience) { this.experience = Math.max(0, experience); }

    public double getMaxExperience() { return maxExperience; }
    public void setMaxExperience(double maxExperience) { this.maxExperience = Math.max(1, maxExperience); }

    public int getMana() { return mana; }
    public void setMana(int mana) { this.mana = Math.max(0, Math.min(mana, getMaxMana())); }

    public int getMaxMana() { return maxMana; }
    public void setMaxMana(int maxMana) { this.maxMana = Math.max(1, maxMana); }

    public Map<String, Boolean> getLearnedSkills() { return learnedSkills; }
    public void setLearnedSkills(Map<String, Boolean> learnedSkills) { this.learnedSkills = learnedSkills; }

    public Map<String, Boolean> getActivePassiveSkills() { return activePassiveSkills; }
    public void setActivePassiveSkills(Map<String, Boolean> activePassiveSkills) { this.activePassiveSkills = activePassiveSkills; }

    public int getMobsKilled() { return mobsKilled; }
    public void setMobsKilled(int mobsKilled) { this.mobsKilled = mobsKilled; }

    public int getElitesKilled() { return elitesKilled; }
    public void setElitesKilled(int elitesKilled) { this.elitesKilled = elitesKilled; }

    public int getBossesKilled() { return bossesKilled; }
    public void setBossesKilled(int bossesKilled) { this.bossesKilled = bossesKilled; }

    public int getPillsCrafted() { return pillsCrafted; }
    public void setPillsCrafted(int pillsCrafted) { this.pillsCrafted = pillsCrafted; }

    public int getHerbsHarvested() { return herbsHarvested; }
    public void setHerbsHarvested(int herbsHarvested) { this.herbsHarvested = herbsHarvested; }

    public long getLastCombatTime() { return lastCombatTime; }
    public void setLastCombatTime(long lastCombatTime) { this.lastCombatTime = lastCombatTime; }
    public void updateCombatTime() { this.lastCombatTime = System.currentTimeMillis(); }

    public long getLastManaRegenTime() { return lastManaRegenTime; }
    public void setLastManaRegenTime(long lastManaRegenTime) { this.lastManaRegenTime = lastManaRegenTime; }

    // ==================== SKILL DATA ====================

    /**
     * Lấy PlayerSkillData
     */
    public PlayerSkillData getSkillData() {
        if (skillData == null) {
            skillData = new PlayerSkillData(playerUUID, playerName);
        }
        return skillData;
    }

    /**
     * Set PlayerSkillData (cho load)
     */
    public void setSkillData(PlayerSkillData skillData) {
        this.skillData = skillData;
    }

    // ==================== TRIBULATION ====================

    public boolean isWaitingForTribulation() { return waitingForTribulation; }
    public void setWaitingForTribulation(boolean waiting) { this.waitingForTribulation = waiting; }

    public boolean isTribulationInProgress() { return tribulationInProgress; }
    public void setTribulationInProgress(boolean inProgress) { this.tribulationInProgress = inProgress; }

    /**
     * Kiểm tra level có phải threshold cần độ kiếp không (9, 19, 29, 39, ...)
     * Đây là cấp cuối của đại cảnh giới (viên mãn), cần độ kiếp để đột phá
     */
    public static boolean isTribulationLevel(int level) {
        return level > 0 && level % 10 == 9;
    }

    /**
     * Kiểm tra xem level này có phải đã qua độ kiếp thành công không
     * (tức là level 10, 20, 30... - cấp đầu tiên của đại cảnh giới tiếp theo)
     */
    public static boolean isPostTribulationLevel(int level) {
        return level > 0 && level % 10 == 0;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Thêm exp, kiểm tra level up
     * @return true nếu level up
     */
    public boolean addExperience(double amount) {
        this.experience += amount;
        if (this.experience >= this.maxExperience) {
            this.experience -= this.maxExperience;
            this.level++;
            // Tính exp cần cho level tiếp theo: level * 100
            this.maxExperience = this.level * 100.0;
            // Tăng linh lực tối đa
            this.maxMana = 100 + (this.level * 10);
            // Hồi đầy linh lực khi thăng cấp
            this.mana = this.maxMana;
            return true; // Level up!
        }
        return false;
    }

    /**
     * Tiêu hao exp (ví dụ để học skill)
     * @return true nếu đủ exp
     */
    public boolean consumeExperience(double amount) {
        if (this.experience >= amount) {
            this.experience -= amount;
            return true;
        }
        return false;
    }

    /**
     * Tiêu hao linh lực
     * @return true nếu đủ linh lực
     */
    public boolean consumeMana(int amount) {
        if (this.mana >= amount) {
            this.mana -= amount;
            return true;
        }
        return false;
    }

    /**
     * Hồi linh lực
     */
    public void regenMana(int amount) {
        this.mana = Math.min(this.mana + amount, this.maxMana);
    }

    /**
     * Kiểm tra đã học skill chưa
     */
    public boolean hasLearnedSkill(String skillId) {
        return learnedSkills.getOrDefault(skillId, false);
    }

    /**
     * Học skill
     */
    public void learnSkill(String skillId) {
        learnedSkills.put(skillId, true);
    }

    /**
     * Kiểm tra passive skill có active không
     */
    public boolean isPassiveActive(String skillId) {
        return activePassiveSkills.getOrDefault(skillId, false);
    }

    /**
     * Toggle passive skill
     */
    public void togglePassive(String skillId) {
        activePassiveSkills.put(skillId, !isPassiveActive(skillId));
    }

    /**
     * Lấy tên cảnh giới dựa vào level
     */
    public String getRealmName() {
        return CultivationManager.getRealmName(this.level);
    }

    /**
     * Lấy prefix cảnh giới (đã tô màu)
     */
    public String getRealmPrefix() {
        return CultivationManager.getRealmPrefix(this.level);
    }

    /**
     * Lấy phần trăm exp
     */
    public double getExpPercent() {
        if (maxExperience <= 0) return 0;
        return Math.min(100.0, (experience / maxExperience) * 100.0);
    }

    /**
     * Lấy tỉ lệ linh lực
     */
    public double getManaPercent() {
        if (maxMana <= 0) return 0;
        return Math.min(100.0, (double) mana / (double) maxMana * 100.0);
    }

    /**
     * Thêm thống kê
     */
    public void incrementMobsKilled() { this.mobsKilled++; }
    public void incrementElitesKilled() { this.elitesKilled++; }
    public void incrementBossesKilled() { this.bossesKilled++; }
    public void incrementPillsCrafted() { this.pillsCrafted++; }
    public void incrementHerbsHarvested() { this.herbsHarvested++; }
}