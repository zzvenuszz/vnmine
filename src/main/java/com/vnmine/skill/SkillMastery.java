package com.vnmine.skill;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SkillMastery - Định nghĩa các cấp độ thành thục kỹ năng
 * Nhập Môn → Tiểu Thành → Đại Thành → Viên Mãn → Xuất Thần Nhập Hóa → Đăng Phong Tạo Cực
 */
public enum SkillMastery {
    NHAP_MON("&7Nhập Môn", 0, 1.0, 1.0, 1.0, 0.0, 0.0),
    TIEU_THANH("&aTiểu Thành", 100, 1.3, 1.2, 1.2, 0.05, 0.05),
    DAI_THANH("&bĐại Thành", 300, 1.6, 1.5, 1.5, 0.1, 0.1),
    VIEN_MAN("&eViên Mãn", 600, 2.0, 1.8, 1.8, 0.15, 0.15),
    XUAT_THAN_NHAP_HOA("&5Xuất Thần Nhập Hóa", 1000, 2.8, 2.5, 2.5, 0.25, 0.2),
    DANG_PHONG_TAO_CUC("&6&lĐăng Phong Tạo Cực", 2000, 4.0, 3.5, 3.5, 0.35, 0.3);

    private final String name;
    private final int expRequired;
    private final double damageMultiplier;
    private final double defenseMultiplier;
    private final double healMultiplier;
    private final double cooldownReduction;
    private final double manaCostReduction;

    SkillMastery(String name, int expRequired, double damageMultiplier, double defenseMultiplier,
                 double healMultiplier, double cooldownReduction, double manaCostReduction) {
        this.name = name;
        this.expRequired = expRequired;
        this.damageMultiplier = damageMultiplier;
        this.defenseMultiplier = defenseMultiplier;
        this.healMultiplier = healMultiplier;
        this.cooldownReduction = cooldownReduction;
        this.manaCostReduction = manaCostReduction;
    }

    public String getName() { return name; }
    public int getExpRequired() { return expRequired; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getDefenseMultiplier() { return defenseMultiplier; }
    public double getHealMultiplier() { return healMultiplier; }
    public double getCooldownReduction() { return cooldownReduction; }
    public double getManaCostReduction() { return manaCostReduction; }

    /**
     * Tìm cấp mastery dựa vào số EXP
     * @return Cấp mastery cao nhất đạt được
     */
    public static SkillMastery getByExp(int exp) {
        SkillMastery result = NHAP_MON;
        for (SkillMastery m : values()) {
            if (exp >= m.expRequired) {
                result = m;
            }
        }
        return result;
    }

    /**
     * Lấy mastery tiếp theo (để hiển thị tiến trình)
     */
    public SkillMastery getNext() {
        SkillMastery[] values = values();
        int ordinal = this.ordinal();
        if (ordinal < values.length - 1) {
            return values[ordinal + 1];
        }
        return null; // Đã max
    }

    /**
     * Lấy % hoàn thành để lên cấp mastery tiếp theo
     */
    public static double getProgressPercent(int currentExp, SkillMastery currentMastery) {
        SkillMastery next = currentMastery.getNext();
        if (next == null) return 100.0; // Đã max

        int currentRequired = currentMastery.expRequired;
        int nextRequired = next.expRequired;
        int progress = currentExp - currentRequired;
        int needed = nextRequired - currentRequired;

        if (needed <= 0) return 100.0;
        return Math.min(100.0, (double) progress / needed * 100.0);
    }
}