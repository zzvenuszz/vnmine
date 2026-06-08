package com.vnmine.skill;

/**
 * SkillGrade - Định nghĩa phẩm cấp kỹ năng
 * Thiên / Địa / Huyền / Hoàng
 */
public enum SkillGrade {
    HOANG("&7Hoàng Cấp", "[Hoàng]", "&7", 1.0, 1.0, 1.0, 1.0, 1.0, "&7"),
    HUYEN("&bHuyền Cấp", "[Huyền]", "&b", 1.5, 1.3, 1.3, 1.2, 0.9, "&b"),
    DIA("&eĐịa Cấp", "[Địa]", "&e", 2.0, 1.6, 1.6, 1.5, 0.8, "&e"),
    THIEN("&cThiên Cấp", "[Thiên]", "&c", 3.0, 2.0, 2.0, 2.0, 0.7, "&c");

    private final String name;
    private final String prefix;
    private final String color;  // Màu sắc
    private final double damageMultiplier;
    private final double defenseMultiplier;
    private final double healMultiplier;
    private final double manaCostMultiplier;
    private final double cooldownMultiplier;
    private final String masteryBarColor;

    SkillGrade(String name, String prefix, String color, double damageMultiplier, double defenseMultiplier,
               double healMultiplier, double manaCostMultiplier, double cooldownMultiplier,
               String masteryBarColor) {
        this.name = name;
        this.prefix = prefix;
        this.color = color;
        this.damageMultiplier = damageMultiplier;
        this.defenseMultiplier = defenseMultiplier;
        this.healMultiplier = healMultiplier;
        this.manaCostMultiplier = manaCostMultiplier;
        this.cooldownMultiplier = cooldownMultiplier;
        this.masteryBarColor = masteryBarColor;
    }

    public String getName() { return name; }
    public String getPrefix() { return prefix; }
    public String getColor() { return color; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getDefenseMultiplier() { return defenseMultiplier; }
    public double getHealMultiplier() { return healMultiplier; }
    public double getManaCostMultiplier() { return manaCostMultiplier; }
    public double getCooldownMultiplier() { return cooldownMultiplier; }
    public String getMasteryBarColor() { return masteryBarColor; }

    public static SkillGrade fromString(String str) {
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return HOANG;
        }
    }
}