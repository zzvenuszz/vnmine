package com.vnmine.skill;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * QuickbarGUI - Giao diện gán skill vào thanh dùng nhanh (hotbar)
 * - Hàng trên (0-8): Hotbar slots của player
 * - Hàng dưới (9-17): Các skill đã học
 * - Click skill ở dưới → click slot hotbar trên để gán
 * - Nút hoàn tất / ESC tự động lưu
 */
public class QuickbarGUI implements Listener {

    private final VNMinePlugin plugin;
    private final SkillManager skillManager;
    // Player đang trong chế độ gán skill
    private final Set<UUID> assignModePlayers;

    public QuickbarGUI(VNMinePlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
        this.assignModePlayers = new HashSet<>();
    }

    /**
     * Mở menu gán skill
     */
    public void openAssignMenu(Player player) {
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            MessageUtils.send(player, "&cHãy bắt đầu tu tiên trước! (/vn start)");
            return;
        }

        Map<String, Boolean> learnedSkills = data.getLearnedSkills();
        if (learnedSkills.isEmpty()) {
            MessageUtils.send(player, "&cBạn chưa học kỹ năng nào!");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 18,
                ColorUtils.colorize("&8✦ Gán Kỹ Năng ✦"));

        // Hàng trên (0-8): Hotbar slots
        for (int i = 0; i < 9; i++) {
            String assignedSkill = data.getSkillData().getQuickbarSkill(i);
            ItemStack displayItem;

            if (assignedSkill != null) {
                SkillManager.SkillConfig sc = skillManager.getSkill(assignedSkill);
                if (sc != null) {
                    Material iconMat = Material.getMaterial(sc.icon.toUpperCase());
                    if (iconMat == null) iconMat = Material.STONE;

                    SkillGrade grade = sc.grade;
                    String gradeColor = grade.getColor();

                    displayItem = new ItemBuilder(iconMat)
                            .setName(gradeColor + "◈ " + sc.name)
                            .setLore(
                                    "",
                                    "&8[Số " + (i + 1) + "]",
                                    "&7Phẩm cấp: " + grade.getName(),
                                    "&eClick chuột phải để cast!",
                                    "",
                                    "&7Shift+Click để bỏ gán"
                            ).build();
                } else {
                    displayItem = new ItemBuilder(Material.BARRIER)
                            .setName("&cLỗi: Skill không tồn tại")
                            .build();
                }
            } else {
                displayItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                        .setName("&7[Số " + (i + 1) + "] &8Trống")
                        .setLore("&7Click vào skill bên dưới rồi click vào đây để gán")
                        .build();
            }

            gui.setItem(i, displayItem);
        }

        // Hàng dưới (9-17): Skills đã học
        int slot = 9;
        List<String> learnedIds = new ArrayList<>(learnedSkills.keySet());
        for (String skillId : learnedIds) {
            SkillManager.SkillConfig sc = skillManager.getSkill(skillId);
            if (sc == null) continue;
            if (slot >= 18) break;

            Material iconMat = Material.getMaterial(sc.icon.toUpperCase());
            if (iconMat == null) iconMat = Material.STONE;

            SkillGrade grade = sc.grade;
            int assignedSlot = data.getSkillData().findSkillSlot(skillId);

            List<String> lore = new ArrayList<>(sc.description);
            lore.add("");
            lore.add("&8Phẩm cấp: " + grade.getName());
            lore.add("&8Linh lực: &b" + sc.manaCost);
            lore.add("&8Hồi chiêu: &e" + sc.cooldownSeconds + "s");

            // Mastery info
            SkillMastery mastery = data.getSkillData().getMasteryLevel(skillId);
            lore.add("&8Thành thục: " + mastery.getName());

            if (assignedSlot >= 0) {
                lore.add("&a✦ Đã gán: Số " + (assignedSlot + 1));
            } else {
                lore.add("&7Click để chọn, rồi click vào slot trên để gán");
            }

            ItemStack skillItem = new ItemBuilder(iconMat)
                    .setName(grade.getColor() + "◈ " + sc.name)
                    .setLore(lore)
                    .build();

            gui.setItem(slot, skillItem);
            slot++;
        }

        // Nếu còn slot trống, fill bằng glass pane
        ItemStack empty = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName("&r")
                .build();
        for (int i = slot; i < 18; i++) {
            gui.setItem(i, empty);
        }

        assignModePlayers.add(player.getUniqueId());
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.contains("Gán Kỹ Năng")) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 18) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        // Click vào slot hotbar (hàng trên: 0-8)
        if (slot < 9) {
            // Shift+Click: bỏ gán skill
            if (event.isShiftClick()) {
                data.getSkillData().clearQuickbarSlot(slot);
                MessageUtils.send(player, "&cĐã bỏ gán skill ở slot " + (slot + 1));
                MessageUtils.playSound(player, Sound.BLOCK_LEVER_CLICK);
                openAssignMenu(player);
                return;
            }

            // Click bình thường: đã có skill ở dưới được chọn không?
            // Logic này xử lý ở phần click skill ở hàng dưới
            return;
        }

        // Click vào skill ở hàng dưới (9-17)
        int skillIndex = slot - 9;
        List<String> learnedIds = new ArrayList<>(data.getLearnedSkills().keySet());
        if (skillIndex >= learnedIds.size()) return;

        String skillId = learnedIds.get(skillIndex);
        SkillManager.SkillConfig sc = skillManager.getSkill(skillId);
        if (sc == null) return;

        // Kiểm tra skill đã gán chưa
        int assignedSlot = data.getSkillData().findSkillSlot(skillId);
        if (assignedSlot >= 0) {
            // Đã gán → bỏ gán nếu click lại
            data.getSkillData().clearQuickbarSlot(assignedSlot);
            MessageUtils.send(player, "&cĐã bỏ gán: " + sc.name);
            MessageUtils.playSound(player, Sound.BLOCK_LEVER_CLICK);
            openAssignMenu(player);
            return;
        }

        // Chưa gán → tìm slot trống và gán
        int emptySlot = -1;
        for (int i = 0; i < 9; i++) {
            if (data.getSkillData().getQuickbarSkill(i) == null) {
                emptySlot = i;
                break;
            }
        }

        if (emptySlot < 0) {
            MessageUtils.send(player, "&cThanh dùng nhanh đã đầy! Hãy bỏ gán skill khác trước.");
            return;
        }

        data.getSkillData().setQuickbarSkill(emptySlot, skillId);
        MessageUtils.send(player, "&a✦ Đã gán " + sc.name + " &avào slot số " + (emptySlot + 1));
        MessageUtils.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        openAssignMenu(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        if (!title.contains("Gán Kỹ Năng")) return;

        assignModePlayers.remove(player.getUniqueId());
        MessageUtils.send(player, "&a✦ Đã lưu cấu hình thanh kỹ năng!");
        MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING);
    }

    public boolean isInAssignMode(Player player) {
        return assignModePlayers.contains(player.getUniqueId());
    }
}