package com.vnmine.skill;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.gui.MainMenuGUI;
import com.vnmine.item.ItemBuilder;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * SkillBarGUI - Quản lý Skill Bar và Menu gán skill
 * 
 * Chức năng:
 * 1. Skill Bar 1x9: hiển thị khi bấm phím Q
 *    - Cancel mọi tương tác với bottom inventory để không mất đồ
 *    - Chỉ cast skill qua click skill hoặc bấm số 1-9
 * 2. Menu quản lý: xem danh sách skill đã học, gán skill vào bar
 * 3. Bấm số 1-9 khi skill bar mở → cast skill
 */
public class SkillBarGUI implements Listener {

    private final VNMinePlugin plugin;

    // Map lưu trạng thái player đang mở skill bar
    private final Set<UUID> skillBarOpen;

    // Map lưu trạng thái player đang ở chế độ gán skill
    private final Map<UUID, Boolean> assignMode;

    // Map lưu skill bar tạm thời khi đang gán (để lưu khi đóng)
    private final Map<UUID, String[]> tempSkillBar;

    public SkillBarGUI(VNMinePlugin plugin) {
        this.plugin = plugin;
        this.skillBarOpen = new HashSet<>();
        this.assignMode = new HashMap<>();
        this.tempSkillBar = new HashMap<>();
    }

    // ==================== SKILL BAR (1x9) ====================

    /**
     * Mở Skill Bar 1x9
     * Chặn mọi click vào bottom inventory để không mất đồ
     */
    public void openSkillBar(Player player) {
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());
        if (skillData == null) return;

        Inventory bar = Bukkit.createInventory(null, 9,
                ColorUtils.colorize("&8✦ Skill Bar ✦"));

        // Gán các skill đã setup
        for (int i = 0; i < 9; i++) {
            String skillId = skillData.getSkillBarSlot(i);
            if (skillId == null) continue;

            SkillManager.SkillConfig skill = getSkillById(skillId);
            if (skill == null) continue;

            Material iconMat = Material.getMaterial(skill.icon.toUpperCase());
            if (iconMat == null) iconMat = Material.STONE;

            List<String> lore = new ArrayList<>();
            for (String desc : skill.description) {
                lore.add(desc);
            }

            // Thông tin cooldown
            boolean onCooldown = skillData.isOnCooldown(skillId);
            if (onCooldown) {
                long remaining = skillData.getCooldownRemaining(skillId);
                lore.add("");
                lore.add("&c⏳ Hồi chiêu: &e" + remaining + "s");
            }

            // Thông tin thành thục
            PlayerSkillData.ProficiencyLevel profLevel = skillData.getProficiencyLevel(skillId);
            lore.add("");
            lore.add("&7Độ thành thục: " + profLevel.getDisplayName());
            lore.add("&7Hệ số: &6x" + String.format("%.1f", skillData.getProficiencyMultiplier(skillId)));

            // Mana cost
            lore.add("");
            lore.add("&b✦ Linh lực: " + skill.manaCost);

            // Số thứ tự
            lore.add("");
            lore.add("&eBấm &f" + (i + 1) + " &eđể thi triển");

            ItemStack iconItem = new ItemBuilder(iconMat)
                    .setName((onCooldown ? "&7" : "&a") + "◈ " + ColorUtils.stripColor(skill.name)
                            + (onCooldown ? " &7[Cooldown]" : ""))
                    .setLore(lore)
                    .setGlow(!onCooldown)
                    .build();

            bar.setItem(i, iconItem);
        }

        skillBarOpen.add(player.getUniqueId());
        player.openInventory(bar);
    }

    /**
     * Đóng Skill Bar
     */
    public void closeSkillBar(Player player) {
        skillBarOpen.remove(player.getUniqueId());
        player.closeInventory();
    }

    /**
     * Kiểm tra player đang mở skill bar không
     */
    public boolean isSkillBarOpen(Player player) {
        return skillBarOpen.contains(player.getUniqueId());
    }

    // ==================== MENU QUẢN LÝ SKILL BAR ====================

    /**
     * Mở menu quản lý gán skill - LUÔN bật chế độ gán mặc định
     */
    public void openSkillManagement(Player player) {
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());
        PlayerCultivationData cultData = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (skillData == null || cultData == null) return;

        // Luôn bật chế độ gán mặc định
        assignMode.put(player.getUniqueId(), true);

        Inventory gui = Bukkit.createInventory(null, 54,
                ColorUtils.colorize("&8✦ Quản Lý Skill Bar ✦"));

        // Header - thông tin
        gui.setItem(4, new ItemBuilder(Material.BOOK)
                .setName("&d&l✦ Quản Lý Công Pháp ✦")
                .setLore(
                        "",
                        "&7Kéo thả skill vào Skill Bar bên dưới",
                        "&7Click vào skill đã học để gán nhanh",
                        "&7Click vào skill trong bar để tháo",
                        "",
                        "&7Chế độ gán luôn &aBẬT &7khi mở menu"
                ).build());

        // Danh sách skill đã học (slot 9-35) - chỉ hiển thị skill đã học
        int slot = 9;
        for (SkillManager.SkillConfig skill : plugin.getSkillManager().getSkills()) {
            if (!cultData.hasLearnedSkill(skill.id)) continue;
            if (slot >= 36) break;

            Material iconMat = Material.getMaterial(skill.icon.toUpperCase());
            if (iconMat == null) iconMat = Material.STONE;

            String skillId = skill.id;

            // Kiểm tra skill đã được gán trong bar chưa
            boolean alreadyInBar = false;
            for (int i = 0; i < 9; i++) {
                if (skillId.equals(skillData.getSkillBarSlot(i))) {
                    alreadyInBar = true;
                    break;
                }
            }

            PlayerSkillData.ProficiencyLevel profLevel = skillData.getProficiencyLevel(skillId);
            int usageCount = skillData.getSkillUsageCount(skillId);

            List<String> lore = new ArrayList<>(skill.description);
            lore.add("");
            lore.add("&fĐộ thành thục: " + profLevel.getDisplayName());
            lore.add("&fĐã dùng: &e" + usageCount + " &7lần");
            lore.add("&fHệ số: &6x" + String.format("%.1f", skillData.getProficiencyMultiplier(skillId)));
            lore.add("&fLinh lực: &b" + skill.manaCost);

            if (alreadyInBar) {
                lore.add("");
                lore.add("&a✓ Đã gán vào Skill Bar");
            } else {
                lore.add("");
                lore.add("&eClick để gán vào Skill Bar");
            }

            gui.setItem(slot, new ItemBuilder(iconMat)
                    .setName("&a◈ " + ColorUtils.stripColor(skill.name))
                    .setLore(lore)
                    .setGlow(true)
                    .build());
            slot++;
        }

        // Nếu không có skill nào
        if (slot == 9) {
            gui.setItem(22, new ItemBuilder(Material.BARRIER)
                    .setName("&cBạn chưa học công pháp nào!")
                    .setLore(
                            "",
                            "&7Sử dụng sách công pháp để học skill",
                            "&7 hoặc mua từ NPC Đại Sư Công Pháp"
                    ).build());
        }

        // Skill Bar preview ở hàng cuối (slot 36-44)
        for (int i = 0; i < 9; i++) {
            String sid = skillData.getSkillBarSlot(i);
            ItemStack barItem;

            if (sid != null) {
                SkillManager.SkillConfig sc = getSkillById(sid);
                if (sc != null) {
                    Material iconMat = Material.getMaterial(sc.icon.toUpperCase());
                    if (iconMat == null) iconMat = Material.STONE;
                    barItem = new ItemBuilder(iconMat)
                            .setName("&a◈ " + ColorUtils.stripColor(sc.name))
                            .setLore("", "&7Slot " + (i+1), "", "&cClick để tháo")
                            .setGlow(true)
                            .build();
                } else {
                    barItem = createEmptyBarSlot(i);
                }
            } else {
                barItem = createEmptyBarSlot(i);
            }
            gui.setItem(36 + i, barItem);
        }

        // Nút xóa toàn bộ Skill Bar
        gui.setItem(49, new ItemBuilder(Material.BARRIER)
                .setName("&c&l✦ Xóa Skill Bar")
                .setLore("", "&7Xóa tất cả skill khỏi Skill Bar", "", "&eClick để xóa")
                .build());

        // Nút hoàn tất
        gui.setItem(50, new ItemBuilder(Material.LIME_DYE)
                .setName("&a&l✦ Hoàn Tất")
                .setLore("", "&7Lưu cấu hình Skill Bar", "", "&eClick để lưu")
                .build());

        // Nút quay lại menu chính
        gui.setItem(48, new ItemBuilder(Material.ARROW)
                .setName("&e&l← Quay Lại Menu Chính")
                .setLore("", "&eClick để quay lại menu chính")
                .build());

        // Viền
        ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName("&r")
                .build();
        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, border);
            }
        }

        // Lưu temp skill bar để lưu khi đóng (ESC)
        tempSkillBar.put(player.getUniqueId(), skillData.getSkillBarSlots());

        player.openInventory(gui);
    }

    /**
     * Xử lý click trong menu quản lý skill bar
     * Slot 48: nút quay lại menu chính
     * Slot 49: nút xóa bar
     * Slot 50: nút hoàn tất
     * Slot 9-35: skill đã học → gán vào ô trống đầu tiên
     * Slot 36-44: skill bar → click để tháo
     */
    public void handleManagementClick(Player player, int slot) {
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());
        PlayerCultivationData cultData = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (skillData == null || cultData == null) return;

        // Nút quay lại menu chính (slot 48)
        if (slot == 48) {
            MainMenuGUI mainMenu = plugin.getMainMenuGUI();
            if (mainMenu != null) {
                mainMenu.openMainMenu(player);
            } else {
                player.closeInventory();
            }
            return;
        }

        // Nút xóa skill bar (slot 49)
        if (slot == 49) {
            skillData.clearSkillBar();
            openSkillManagement(player);
            return;
        }

        // Nút hoàn tất (slot 50)
        if (slot == 50) {
            tempSkillBar.remove(player.getUniqueId());
            assignMode.put(player.getUniqueId(), false);
            player.closeInventory();
            return;
        }

        // Click vào skill đã học (slot 9-35) → gán vào ô trống đầu tiên
        if (slot >= 9 && slot < 36) {
            int index = slot - 9;
            List<SkillManager.SkillConfig> learnedSkills = new ArrayList<>();
            for (SkillManager.SkillConfig s : plugin.getSkillManager().getSkills()) {
                if (cultData.hasLearnedSkill(s.id)) {
                    learnedSkills.add(s);
                }
            }
            if (index >= learnedSkills.size()) return;

            SkillManager.SkillConfig skill = learnedSkills.get(index);
            assignSkillToBar(skillData, player, skill.id);
            openSkillManagement(player);
            return;
        }

        // Click vào Skill Bar (slot 36-44) → tháo skill
        if (slot >= 36 && slot < 45) {
            int barSlot = slot - 36;
            String sid = skillData.getSkillBarSlot(barSlot);

            if (sid != null) {
                skillData.clearSkillBarSlot(barSlot);
                SkillManager.SkillConfig sc = getSkillById(sid);
                if (sc != null) {
                    MessageUtils.send(player, "&cĐã tháo &e" + ColorUtils.stripColor(sc.name) + " &ckhỏi slot " + (barSlot + 1));
                }
                openSkillManagement(player);
            }
            return;
        }
    }

    /**
     * Gán skill vào Skill Bar, tự động:
     * - Tìm ô trống đầu tiên
     * - Nếu skill đã tồn tại ở ô khác, xóa ô cũ trước
     * - Không cho phép trùng skill
     */
    private void assignSkillToBar(PlayerSkillData skillData, Player player, String skillId) {
        // Kiểm tra skill đã tồn tại trong bar chưa
        int existingSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (skillId.equals(skillData.getSkillBarSlot(i))) {
                existingSlot = i;
                break;
            }
        }

        // Nếu đã tồn tại, xóa khỏi vị trí cũ (không cho trùng)
        if (existingSlot >= 0) {
            skillData.clearSkillBarSlot(existingSlot);
        }

        // Tìm ô trống đầu tiên
        int emptySlot = -1;
        for (int i = 0; i < 9; i++) {
            if (skillData.getSkillBarSlot(i) == null) {
                emptySlot = i;
                break;
            }
        }

        if (emptySlot == -1) {
            MessageUtils.send(player, "&cSkill Bar đã đầy! Hãy xóa bớt skill trước.");
            return;
        }

        // Gán skill vào ô trống
        skillData.setSkillBarSlot(emptySlot, skillId);
        SkillManager.SkillConfig sc = getSkillById(skillId);
        if (sc != null) {
            if (existingSlot >= 0) {
                MessageUtils.send(player, "&aĐã di chuyển &e" + ColorUtils.stripColor(sc.name)
                        + " &atừ slot " + (existingSlot + 1) + " → " + (emptySlot + 1));
            } else {
                MessageUtils.send(player, "&aĐã gán &e" + ColorUtils.stripColor(sc.name)
                        + " &avào slot " + (emptySlot + 1));
            }
        }
    }

    /**
     * Cast skill từ skill bar
     */
    public void castSkillFromBar(Player player, int barSlot) {
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());
        if (skillData == null) return;

        String skillId = skillData.getSkillBarSlot(barSlot);
        if (skillId == null) {
            MessageUtils.send(player, "&cSlot này trống!");
            closeSkillBar(player);
            return;
        }

        SkillManager.SkillConfig skill = getSkillById(skillId);
        if (skill == null) {
            MessageUtils.send(player, "&cKỹ năng không tồn tại!");
            closeSkillBar(player);
            return;
        }

        PlayerCultivationData cultData = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (cultData == null) return;

        // Kiểm tra loại skill - chỉ ACTIVE mới cast được từ bar
        if (!skill.type.equals("ACTIVE")) {
            MessageUtils.send(player, "&cKỹ năng thụ động không thể cast từ Skill Bar!");
            closeSkillBar(player);
            return;
        }

        // Kiểm tra cooldown (nếu không có bypass)
        boolean bypassCooldown = false;
        if (skillData.isCooldownBypass() && 
            plugin.getPermissionManager() != null &&
            plugin.getPermissionManager().hasPermission(player, "vnmine.skill.cooldown.bypass")) {
            bypassCooldown = true;
        }
        if (!bypassCooldown && skillData.isOnCooldown(skillId)) {
            long remaining = skillData.getCooldownRemaining(skillId);
            MessageUtils.send(player, "&cKỹ năng &e" + ColorUtils.stripColor(skill.name) + " &cđang hồi chiêu! (&e" + remaining + "s&c)");
            closeSkillBar(player);
            return;
        }

        // Kiểm tra mana
        if (skill.manaCost > 0) {
            if (!plugin.getCultivationManager().consumeMana(player, skill.manaCost)) {
                MessageUtils.send(player, "&cKhông đủ linh lực! (Cần &b" + skill.manaCost + " &clinh lực)");
                closeSkillBar(player);
                return;
            }
        }

        // Cast skill với proficiency bonus
        double multiplier = skillData.getProficiencyMultiplier(skillId);
        int proficiencyBonus = (int) ((multiplier - 1.0) * 100);
        MessageUtils.send(player, "&d✦ Thi triển: &e" + ColorUtils.stripColor(skill.name)
                + " &7(Thành thục +" + proficiencyBonus + "%)");

        // Cast skill với multiplier
        plugin.getSkillManager().castSkill(player, skill, cultData, multiplier);

        // Tăng proficiency
        skillData.incrementSkillUsage(skillId);

        // Set cooldown (nếu không bypass)
        if (!bypassCooldown) {
            skillData.setCooldown(skillId, skill.cooldownSeconds);
        }

        // Đóng skill bar
        closeSkillBar(player);
    }

    /**
     * Lấy SkillConfig theo ID
     */
    private SkillManager.SkillConfig getSkillById(String skillId) {
        for (SkillManager.SkillConfig sc : plugin.getSkillManager().getSkills()) {
            if (sc.id.equals(skillId)) return sc;
        }
        return null;
    }

    /**
     * Tạo item cho ô trống trong skill bar
     */
    private ItemStack createEmptyBarSlot(int slotIndex) {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName("&7Slot " + (slotIndex + 1) + " (Trống)")
                .setLore("", "&7Kéo skill từ danh sách vào đây")
                .build();
    }

    // ==================== GETTERS ====================

    public boolean isAssignMode(Player player) {
        return assignMode.getOrDefault(player.getUniqueId(), false);
    }

    public void cleanupPlayer(UUID uuid) {
        skillBarOpen.remove(uuid);
        assignMode.remove(uuid);
        tempSkillBar.remove(uuid);
    }

    // ==================== EVENT HANDLERS ====================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Xử lý click trong Menu quản lý TRƯỚC (vì "Quản Lý Skill Bar" cũng chứa "Skill Bar")
        if (title.contains("Quản Lý Skill Bar")) {
            int slot = event.getRawSlot();
            // Cancel tất cả click vào top inventory
            if (slot >= 0 && slot < 54) {
                event.setCancelled(true);

                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() == Material.AIR) return;

                handleManagementClick(player, slot);
            }
            return;
        }

        // Xử lý click trong Skill Bar (1x9)
        if (title.contains("Skill Bar")) {
            // Cancel MỌI click để không mất đồ từ bottom inventory
            event.setCancelled(true);

            // Xử lý bấm phím số 1-9
            int hotbarSlot = event.getHotbarButton();
            if (hotbarSlot >= 0 && hotbarSlot < 9) {
                castSkillFromBar(player, hotbarSlot);
                return;
            }

            int rawSlot = event.getRawSlot();

            // Nếu click vào skill bar bằng chuột (rawSlot 0-8)
            if (rawSlot >= 0 && rawSlot < 9) {
                castSkillFromBar(player, rawSlot);
            }
            // Click vào bottom inventory (rawSlot >= 9) bị cancel, không làm gì cả
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.contains("Quản Lý Skill Bar")) return;

        // Luôn cho phép kéo thả (chế độ gán mặc định BẬT)
        Set<Integer> rawSlots = event.getRawSlots();
        boolean dragToBar = false;
        boolean dragFromBar = false;

        for (Integer slot : rawSlots) {
            if (slot >= 36 && slot < 45) {
                dragToBar = true;
            } else if (slot >= 0 && slot < 36) {
                dragFromBar = true;
            }
        }

        if (dragFromBar) {
            event.setCancelled(true);
            return;
        }

        if (dragToBar) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();
        String title = event.getView().getTitle();

        // Khi đóng Menu quản lý
        if (title.contains("Quản Lý Skill Bar")) {
            if (tempSkillBar.containsKey(uuid)) {
                tempSkillBar.remove(uuid);
            }
            skillBarOpen.remove(uuid);
            tempSkillBar.remove(uuid);
            return;
        }

        // Khi đóng Skill Bar (1x9)
        if (title.contains("Skill Bar")) {
            // Xóa trạng thái skill bar sau 1 tick
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                skillBarOpen.remove(uuid);
            }, 1L);
            return;
        }
    }

    /**
     * Xử lý phím Q - mở Skill Bar khi không mở inventory
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        try {
            InventoryView openInv = player.getOpenInventory();
            if (openInv != null) {
                org.bukkit.inventory.Inventory topInv = openInv.getTopInventory();
                if (topInv != null) {
                    InventoryType invType = topInv.getType();
                    if (invType != InventoryType.CRAFTING && invType != InventoryType.PLAYER) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            // Bỏ qua lỗi
        }

        // Kiểm tra player đã có dữ liệu skill chưa
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());
        if (skillData == null) {
            MessageUtils.send(player, "&c✦ Bạn chưa có dữ liệu công pháp! Dùng &e/vnskill &cđể xem.");
            return;
        }

        // Mở skill bar
        event.setCancelled(true);
        openSkillBar(player);
    }
}