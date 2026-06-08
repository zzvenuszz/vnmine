package com.vnmine.skill;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * SkillBookListener - Xử lý khi người chơi click chuột phải vào sách kỹ năng để học
 */
public class SkillBookListener implements Listener {

    private final VNMinePlugin plugin;
    private final SkillManager skillManager;
    private final NamespacedKey skillIdKey;
    private final NamespacedKey skillGradeKey;
    private final NamespacedKey skillQualityKey;

    public SkillBookListener(VNMinePlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
        this.skillIdKey = new NamespacedKey(plugin, "skill-id");
        this.skillGradeKey = new NamespacedKey(plugin, "skill-grade");
        this.skillQualityKey = new NamespacedKey(plugin, "skill-quality");
    }

    public NamespacedKey getSkillIdKey() { return skillIdKey; }
    public NamespacedKey getSkillGradeKey() { return skillGradeKey; }
    public NamespacedKey getSkillQualityKey() { return skillQualityKey; }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        // Kiểm tra có phải sách kỹ năng không
        String skillId = getSkillId(item);
        if (skillId == null) return;

        event.setCancelled(true);

        // Kiểm tra skill có tồn tại không
        SkillManager.SkillConfig skillConfig = skillManager.getSkill(skillId);
        if (skillConfig == null) {
            MessageUtils.send(player, "&cKỹ năng không tồn tại!");
            return;
        }

        // Kiểm tra đã học chưa
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            MessageUtils.send(player, "&cHãy bắt đầu tu tiên trước! (/vn start)");
            return;
        }

        if (data.hasLearnedSkill(skillId)) {
            MessageUtils.send(player, "&cBạn đã học kỹ năng này rồi!");
            return;
        }

        // Lấy grade và quality từ item
        SkillGrade grade = getSkillGrade(item);
        SkillQuality quality = getSkillQuality(item);

        // Thử học
        boolean success = quality.tryLearn();
        int keepChance = skillManager.getBookKeepOnFailChance();

        if (success) {
            // Học thành công
            data.learnSkill(skillId);
            MessageUtils.send(player, "&a✦ Học thành công: &e" + skillConfig.name + " &a(" + grade.getName() + " &a- " + quality.getName() + "&a)");
            MessageUtils.playSound(player, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP);
            SkillEffects.playGradeEffect(player, grade);

            // Xóa sách
            item.setAmount(item.getAmount() - 1);
        } else {
            // Học thất bại
            MessageUtils.send(player, "&c✦ Học thất bại: " + skillConfig.name);
            MessageUtils.playSound(player, org.bukkit.Sound.BLOCK_FIRE_EXTINGUISH);

            // Random giữ lại sách
            if (Math.random() * 100 < keepChance) {
                MessageUtils.send(player, "&a✦ May mắn! Sách vẫn còn nguyên!");
            } else {
                item.setAmount(item.getAmount() - 1);
                MessageUtils.send(player, "&cSách đã bị tiêu hủy!");
            }
        }

        player.updateInventory();
    }

    /**
     * Tạo item sách kỹ năng
     */
    public ItemStack createSkillBook(String skillId, SkillGrade grade, SkillQuality quality) {
        SkillManager.SkillConfig skill = skillManager.getSkill(skillId);
        if (skill == null) return null;

        String gradeName = ColorUtils.colorize(grade.getName());
        String qualityName = ColorUtils.colorize(quality.getName());
        String skillName = ColorUtils.colorize(skill.name);

        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return null;

        meta.setDisplayName(ColorUtils.colorize("&6◆ " + skillName + " ◆"));
        meta.setLore(java.util.Arrays.asList(
                ColorUtils.colorize(""),
                ColorUtils.colorize("&8Phẩm cấp: " + gradeName + " &8- " + qualityName),
                ColorUtils.colorize("&8Tỉ lệ thành công: &e" + quality.getLearnChance() + "%"),
                ColorUtils.colorize(""),
                ColorUtils.colorize("&7Click chuột phải để học kỹ năng này!"),
                ColorUtils.colorize("&7Yêu cầu cấp: &e" + skill.requiredLevel)
        ));

        // Lưu NBT data
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(skillIdKey, PersistentDataType.STRING, skillId);
        pdc.set(skillGradeKey, PersistentDataType.STRING, grade.name());
        pdc.set(skillQualityKey, PersistentDataType.STRING, quality.name());

        book.setItemMeta(meta);
        return book;
    }

    /**
     * Lấy skill_id từ item
     */
    public String getSkillId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(skillIdKey, PersistentDataType.STRING);
    }

    /**
     * Lấy grade từ item
     */
    public SkillGrade getSkillGrade(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return SkillGrade.HOANG;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String gradeStr = pdc.get(skillGradeKey, PersistentDataType.STRING);
        return SkillGrade.fromString(gradeStr);
    }

    /**
     * Lấy quality từ item
     */
    public SkillQuality getSkillQuality(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return SkillQuality.HA;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String qualityStr = pdc.get(skillQualityKey, PersistentDataType.STRING);
        return SkillQuality.fromString(qualityStr);
    }
}