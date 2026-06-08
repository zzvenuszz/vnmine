package com.vnmine.skill;

/**
 * SkillQuality - Định nghĩa phẩm chất sách kỹ năng
 * Thượng (90%) / Trung (60%) / Hạ (20%)
 */
public enum SkillQuality {
    THUONG("&6Thượng Phẩm", 90),
    TRUNG("&eTrung Phẩm", 60),
    HA("&7Hạ Phẩm", 20);

    private final String name;
    private final int learnChance; // Tỉ lệ học thành công (%)

    SkillQuality(String name, int learnChance) {
        this.name = name;
        this.learnChance = learnChance;
    }

    public String getName() { return name; }
    public int getLearnChance() { return learnChance; }

    public static SkillQuality fromString(String str) {
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return HA;
        }
    }

    /**
     * Kiểm tra học thành công dựa vào tỉ lệ
     */
    public boolean tryLearn() {
        return Math.random() * 100 < learnChance;
    }
}