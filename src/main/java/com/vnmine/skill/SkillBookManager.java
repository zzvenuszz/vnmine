package com.vnmine.skill;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * SkillBookManager - Quản lý sách công pháp (skill books)
 * Xử lý các phẩm cấp: Thiên/Địa/Huyền/Hoàng × Thượng/Trung/Hạ
 * Quản lý tỉ lệ học thành công, cơ chế học thất bại
 */
public class SkillBookManager {

    private final VNMinePlugin plugin;

    // Tỉ lệ học thành công theo phẩm cấp (đọc từ config)
    private double rateTHUONG = 0.90;
    private double rateTRUNG = 0.60;
    private double rateHA = 0.20;

    // Tỉ lệ mất sách khi thất bại (đọc từ config)
    private double failLoseBookChance = 0.50;

    // Cấu hình drop sách từ quái
    private boolean bookDropEnabled;
    private Map<String, DropEntry> mobDropConfig;

    public SkillBookManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        this.mobDropConfig = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection bookSection = config.getConfigurationSection("skill-books");
        if (bookSection == null) return;

        bookDropEnabled = bookSection.getBoolean("drop-from-mobs.enabled", true);
        failLoseBookChance = bookSection.getDouble("fail-lose-book-chance", 0.50);

        // Load success rates from config
        ConfigurationSection rateSection = bookSection.getConfigurationSection("success-rate");
        if (rateSection != null) {
            rateTHUONG = rateSection.getDouble("THUONG", 0.90);
            rateTRUNG = rateSection.getDouble("TRUNG", 0.60);
            rateHA = rateSection.getDouble("HA", 0.20);
        }

        // Load mob drop config
        ConfigurationSection dropsSection = bookSection.getConfigurationSection("drop-from-mobs.drops");
        if (dropsSection != null) {
            for (String mobType : dropsSection.getKeys(false)) {
                ConfigurationSection dropConfig = dropsSection.getConfigurationSection(mobType);
                if (dropConfig == null) continue;

                String skillId = dropConfig.getString("skill", "");
                String grade = dropConfig.getString("grade", "HOANG");
                String subGrade = dropConfig.getString("sub-grade", "HA");
                double chance = dropConfig.getDouble("chance", 5.0);

                mobDropConfig.put(mobType.toUpperCase(),
                        new DropEntry(skillId, grade, subGrade, chance));
            }
        }
    }

    /**
     * Tạo item sách công pháp
     */
    public ItemStack createSkillBook(String skillId, String grade, String subGrade) {
        SkillManager skillManager = plugin.getSkillManager();
        SkillManager.SkillConfig skill = null;
        for (SkillManager.SkillConfig sc : skillManager.getSkills()) {
            if (sc.id.equals(skillId)) {
                skill = sc;
                break;
            }
        }
        if (skill == null) return null;

        String gradeColor = getGradeColor(grade);
        String gradeName = getGradeName(grade);
        String subGradeName = getSubGradeName(subGrade);
        String skillName = ColorUtils.stripColor(skill.name);

        Material icon = Material.ENCHANTED_BOOK;

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&7Công pháp: &f" + skillName);
        lore.add("&7Phẩm cấp: " + gradeColor + gradeName + " " + subGradeName);
        lore.add("");
        lore.add("&7Loại: &e" + (skill.type.equals("ACTIVE") ? "Chủ động" : "Thụ động"));
        lore.add("&7Linh lực: &b" + skill.manaCost);
        lore.add("");
        lore.add("&a✦ Click phải để học công pháp này!");
        lore.add("&7Tỉ lệ thành công: &e" + getSuccessRatePercent(subGrade) + "%");
        lore.add("&cThất bại: " + (int)(failLoseBookChance * 100) + "% mất sách");
        lore.add("&c         " + (int)((1 - failLoseBookChance) * 100) + "% giảm phẩm cấp");

        return new ItemBuilder(icon)
                .setName(gradeColor + "◆ Sách " + skillName + " ◆")
                .setLore(lore)
                .setGlow(true)
                .setPersistentData("vnmine_skill_book", skillId)
                .setPersistentData("vnmine_skill_grade", grade)
                .setPersistentData("vnmine_skill_subgrade", subGrade)
                .build();
    }

    /**
     * Xử lý học skill từ sách
     * @return true nếu học thành công
     */
    public boolean learnFromBook(Player player, ItemStack book) {
        // Lấy data từ NBT
        String skillId = ItemBuilder.getPersistentData(book, "vnmine_skill_book");
        String grade = ItemBuilder.getPersistentData(book, "vnmine_skill_grade");
        String subGrade = ItemBuilder.getPersistentData(book, "vnmine_skill_subgrade");

        if (skillId == null || grade == null || subGrade == null) {
            MessageUtils.send(player, "&cSách công pháp không hợp lệ!");
            return false;
        }

        // Kiểm tra đã học skill chưa
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            MessageUtils.send(player, "&cBạn chưa bắt đầu tu luyện! Dùng /vn start");
            return false;
        }

        // Lấy thông tin skill
        SkillManager skillManager = plugin.getSkillManager();
        SkillManager.SkillConfig skill = null;
        for (SkillManager.SkillConfig sc : skillManager.getSkills()) {
            if (sc.id.equals(skillId)) {
                skill = sc;
                break;
            }
        }
        if (skill == null) {
            MessageUtils.send(player, "&cCông pháp này không tồn tại!");
            return false;
        }

        // Kiểm tra level requirement
        if (data.getLevel() < skill.requiredLevel) {
            MessageUtils.send(player, "&cBạn chưa đủ cấp để học công pháp này! (Cần cấp " + skill.requiredLevel + ")");
            return false;
        }

        // Nếu đã học skill này: cho phép học lại để nâng cấp phẩm
        if (data.hasLearnedSkill(skillId)) {
            String currentGrade = data.getSkillGrade(skillId);
            String currentSubGrade = data.getSkillSubGrade(skillId);

            // So sánh phẩm cấp: sách mới phải cao hơn hoặc bằng
            int currentGradeIndex = getGradeIndex(currentGrade, currentSubGrade);
            int newGradeIndex = getGradeIndex(grade, subGrade);

            if (newGradeIndex <= currentGradeIndex) {
                MessageUtils.send(player, "&cBạn đã học công pháp này rồi! Phẩm cấp hiện tại: " + getGradeColor(currentGrade) + getGradeName(currentGrade) + " " + getSubGradeName(currentSubGrade));
                MessageUtils.send(player, "&eDùng sách phẩm cấp cao hơn để nâng cấp!");
                return false;
            }

            // Nâng cấp skill: thay thế grade mới, reset proficiency
            data.setSkillGrade(skillId, grade, subGrade);
            data.getSkillData().setSkillUsage(skillId, 0); // Reset proficiency

            MessageUtils.sendTitle(player,
                    "&6&l✦ NÂNG CẤP CÔNG PHÁP ✦",
                    grade + "Công pháp " + ColorUtils.stripColor(skill.name) + " đã nâng cấp!",
                    10, 60, 10);
            MessageUtils.send(player, "&6✦ Nâng cấp công pháp: &e" + skill.name);
            MessageUtils.send(player, "&a✦ Phẩm cấp mới: " + getGradeColor(grade) + getGradeName(grade) + " " + getSubGradeName(subGrade));
            MessageUtils.send(player, "&7Độ thành thục đã reset về 0.");
            MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);

            // Xóa sách
            consumeBook(player, book);
            return true;
        }

        // Chưa học: thử học
        double successRate = getSuccessRate(subGrade);
        Random random = new Random();
        boolean success = random.nextDouble() < successRate;

        // Xóa sách khỏi tay player
        consumeBook(player, book);

        String gradeColor = getGradeColor(grade);
        String gradeName = getGradeName(grade);
        String subGradeName = getSubGradeName(subGrade);

        if (success) {
            // Thành công: lưu grade
            data.learnSkill(skillId, grade, subGrade);
            MessageUtils.sendTitle(player,
                    "&a&l✦ HỌC THÀNH CÔNG ✦",
                    gradeColor + "Công pháp " + ColorUtils.stripColor(skill.name) + " đã được khắc vào thần thức!",
                    10, 60, 10);
            MessageUtils.send(player, "&a✦ Bạn đã học được công pháp: &e" + skill.name);
            MessageUtils.send(player, "&a✦ Phẩm cấp: " + gradeColor + gradeName + " " + subGradeName);
            MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
            return true;
        } else {
            // Thất bại: 50% mất sách, 50% giảm phẩm sách
            boolean loseBook = random.nextDouble() < failLoseBookChance;

            if (loseBook) {
                // Mất sách hoàn toàn
                MessageUtils.sendTitle(player,
                        "&4&l✦ HỌC THẤT BẠI ✦",
                        "&cSách công pháp đã tan biến!",
                        10, 60, 10);
                MessageUtils.send(player, "&c✦ Học công pháp thất bại! Sách đã mất!");
                MessageUtils.send(player, "&7Phẩm cấp: " + gradeColor + gradeName + " " + subGradeName);
                MessageUtils.playSound(player, Sound.ENTITY_ITEM_BREAK);
            } else {
                // Giảm phẩm cấp sách
                String newSubGrade = downgradeSubGrade(subGrade);

                if (newSubGrade == null) {
                    // Hạ phẩm giảm nữa → mất sách
                    MessageUtils.sendTitle(player,
                            "&4&l✦ HỌC THẤT BẠI ✦",
                            "&cSách công pháp đã tan biến sau khi giảm phẩm!",
                            10, 60, 10);
                    MessageUtils.send(player, "&c✦ Sách đã giảm xuống phẩm thấp nhất và biến mất!");
                    MessageUtils.playSound(player, Sound.ENTITY_ITEM_BREAK);
                } else {
                    // Tạo sách mới với phẩm cấp giảm
                    ItemStack downgradedBook = createSkillBook(skillId, grade, newSubGrade);
                    if (downgradedBook != null) {
                        player.getInventory().addItem(downgradedBook);
                    }

                    String newGradeColor = getGradeColor(grade);
                    String newGradeName = getGradeName(grade);
                    String newSubGradeName = getSubGradeName(newSubGrade);

                    MessageUtils.sendTitle(player,
                            "&c✦ HỌC THẤT BẠI ✦",
                            "&7Phẩm cấp sách bị giảm!",
                            10, 60, 10);
                    MessageUtils.send(player, "&c✦ Học công pháp thất bại!");
                    MessageUtils.send(player, "&7Phẩm cấp giảm: " + gradeColor + gradeName + " " + subGradeName +
                            " → " + newGradeColor + newGradeName + " " + newSubGradeName);
                    MessageUtils.playSound(player, Sound.BLOCK_ANVIL_USE);
                }
            }

            // NOTE: Không ảnh hưởng skill đã học (theo yêu cầu)
            return false;
        }
    }

    /**
     * Xóa/giảm sách khỏi inventory
     */
    private void consumeBook(Player player, ItemStack book) {
        int heldSlot = player.getInventory().getHeldItemSlot();
        ItemStack heldItem = player.getInventory().getItem(heldSlot);
        if (heldItem != null && heldItem.equals(book)) {
            heldItem.setAmount(heldItem.getAmount() - 1);
            if (heldItem.getAmount() <= 0) {
                player.getInventory().setItem(heldSlot, null);
            }
        }
    }

    /**
     * Lấy tỉ lệ thành công dựa vào sub grade
     */
    public double getSuccessRate(String subGrade) {
        switch (subGrade.toUpperCase()) {
            case "THUONG": return rateTHUONG;
            case "TRUNG": return rateTRUNG;
            case "HA": return rateHA;
            default: return rateHA;
        }
    }

    /**
     * Lấy tỉ lệ thành công dưới dạng phần trăm
     */
    public int getSuccessRatePercent(String subGrade) {
        return (int)(getSuccessRate(subGrade) * 100);
    }

    /**
     * Lấy grade index để so sánh (càng cao càng mạnh)
     * THIEN_THUONG=11, DIA_THUONG=10, HUYEN_THUONG=9, HOANG_THUONG=8
     * THIEN_TRUNG=7, DIA_TRUNG=6, HUYEN_TRUNG=5, HOANG_TRUNG=4
     * THIEN_HA=3, DIA_HA=2, HUYEN_HA=1, HOANG_HA=0
     */
    private int getGradeIndex(String grade, String subGrade) {
        String g = grade.toUpperCase();
        String s = subGrade.toUpperCase();

        int gradeBase = 0;
        switch (g) {
            case "THIEN": gradeBase = 3; break;
            case "DIA": gradeBase = 2; break;
            case "HUYEN": gradeBase = 1; break;
            case "HOANG": gradeBase = 0; break;
        }

        int subBonus = 0;
        switch (s) {
            case "THUONG": subBonus = 3; break;
            case "TRUNG": subBonus = 2; break;
            case "HA": subBonus = 1; break;
        }

        return gradeBase * 4 + subBonus;
    }

    /**
     * Lấy màu hiển thị cho grade
     */
    private String getGradeColor(String grade) {
        switch (grade.toUpperCase()) {
            case "THIEN": return "&6";
            case "DIA": return "&5";
            case "HUYEN": return "&b";
            case "HOANG": return "&7";
            default: return "&f";
        }
    }

    /**
     * Giảm phẩm cấp: Thượng → Trung → Hạ → null (mất)
     */
    private String downgradeSubGrade(String subGrade) {
        switch (subGrade.toUpperCase()) {
            case "THUONG": return "TRUNG";
            case "TRUNG": return "HA";
            case "HA": return null;
            default: return null;
        }
    }

    /**
     * Lấy tên grade
     */
    public static String getGradeName(String grade) {
        switch (grade.toUpperCase()) {
            case "THIEN": return "Thiên Cấp";
            case "DIA": return "Địa Cấp";
            case "HUYEN": return "Huyền Cấp";
            case "HOANG": return "Hoàng Cấp";
            default: return "Không rõ";
        }
    }

    /**
     * Lấy tên sub grade
     */
    public static String getSubGradeName(String subGrade) {
        switch (subGrade.toUpperCase()) {
            case "THUONG": return "Thượng Phẩm";
            case "TRUNG": return "Trung Phẩm";
            case "HA": return "Hạ Phẩm";
            default: return "Không rõ";
        }
    }

    /**
     * Kiểm tra item có phải sách công pháp không
     */
    public static boolean isSkillBook(ItemStack item) {
        return ItemBuilder.hasPersistentData(item, "vnmine_skill_book");
    }

    /**
     * Lấy cấu hình drop cho mob
     */
    public DropEntry getDropForMob(String mobType) {
        return mobDropConfig.get(mobType.toUpperCase());
    }

    /**
     * Kiểm tra drop sách từ mob có enabled không
     */
    public boolean isBookDropEnabled() {
        return bookDropEnabled;
    }

    /**
     * Reload config
     */
    public void reload() {
        loadConfig();
    }

    // ==================== INNER CLASSES ====================

    /**
     * Cấu hình drop sách từ quái
     */
    public static class DropEntry {
        public final String skillId;
        public final String grade;
        public final String subGrade;
        public final double chance; // 0-100

        public DropEntry(String skillId, String grade, String subGrade, double chance) {
            this.skillId = skillId;
            this.grade = grade;
            this.subGrade = subGrade;
            this.chance = chance;
        }
    }
}