package com.vnmine.skill;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.gui.MainMenuGUI;
import com.vnmine.item.ItemBuilder;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.Sound;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * SkillBarGUI - Quản lý Skill Bar và Menu gán skill
 * 
 * Chức năng:
 * 1. Skill Bar Mode: bấm Q → vào chế độ skill bar (Action Bar hiển thị skill)
 *    - Bấm số 1-9 để cast skill tương ứng
 *    - Tự động thoát sau 7 giây không tương tác
 *    - Bấm Q lần nữa để thoát thủ công
 * 2. Menu quản lý: xem danh sách skill đã học, gán skill vào bar (giữ nguyên)
 */
public class SkillBarGUI implements Listener {

    private final VNMinePlugin plugin;

    // Trạng thái player đang ở chế độ skill bar (Action Bar)
    private final Set<UUID> skillBarMode;

    // Map lưu trạng thái player đang ở chế độ gán skill
    private final Map<UUID, Boolean> assignMode;

    // Map lưu skill bar tạm thời khi đang gán (để lưu khi đóng)
    private final Map<UUID, String[]> tempSkillBar;

    // Task ID cho auto-exit timer (mỗi player)
    private final Map<UUID, Integer> exitTaskIds;

    // Task ID cho refresh action bar (mỗi player)
    private final Map<UUID, Integer> refreshTaskIds;

    // Thời gian tối đa ở trong skill bar mode (giây)
    private static final int SKILL_BAR_TIMEOUT = 7;

    public SkillBarGUI(VNMinePlugin plugin) {
        this.plugin = plugin;
        this.skillBarMode = new HashSet<>();
        this.assignMode = new HashMap<>();
        this.tempSkillBar = new HashMap<>();
        this.exitTaskIds = new HashMap<>();
        this.refreshTaskIds = new HashMap<>();
    }

    // ==================== SKILL BAR MODE (ACTION BAR) ====================

    /**
     * Vào chế độ Skill Bar Mode
     * Hiển thị Action Bar với danh sách skill đã gán
     */
    public void enterSkillBarMode(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(uuid);
        if (skillData == null) {
            MessageUtils.send(player, "&c✦ Bạn chưa có dữ liệu công pháp! Dùng &e/vnskill &cđể xem.");
            return;
        }

        // Tạm dừng mana bar action bar để tránh xung đột action bar
        plugin.getCultivationManager().pauseManaBar(uuid);

        // Chuyển sang slot cuối (phím 9) để đảm bảo PlayerItemHeldEvent
        // luôn fire khi bấm phím số (vì previousSlot=8 != newSlot=0-8)
        player.getInventory().setHeldItemSlot(8);

        // Thêm vào skill bar mode
        skillBarMode.add(uuid);

        // Gửi Action Bar ngay lập tức
        sendSkillBarActionBar(player);

        // Schedule tự động thoát sau SKILL_BAR_TIMEOUT giây
        int exitTaskId = new BukkitRunnable() {
            int countdown = SKILL_BAR_TIMEOUT;
            @Override
            public void run() {
                if (!skillBarMode.contains(uuid)) {
                    cancel();
                    return;
                }
                countdown--;
                if (countdown <= 0) {
                    exitSkillBarMode(player, false);
                    MessageUtils.send(player, "&7✦ Đã thoát Skill Bar (hết thời gian)");
                }
            }
        }.runTaskTimer(plugin, 20L, 20L).getTaskId();

        // Cancel task cũ nếu có
        cancelExitTask(uuid);
        exitTaskIds.put(uuid, exitTaskId);

        // Schedule refresh action bar mỗi 2 giây
        int refreshTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (skillBarMode.contains(uuid)) {
                    sendSkillBarActionBar(player);
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 40L, 40L).getTaskId();

        cancelRefreshTask(uuid);
        refreshTaskIds.put(uuid, refreshTaskId);
    }

    /**
     * Thoát khỏi chế độ Skill Bar Mode
     */
    public void exitSkillBarMode(Player player, boolean sendMessage) {
        UUID uuid = player.getUniqueId();
        skillBarMode.remove(uuid);
        cancelExitTask(uuid);
        cancelRefreshTask(uuid);

        // Clear action bar
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));

        // Tiếp tục mana bar action bar
        plugin.getCultivationManager().resumeManaBar(uuid);
    }

    /**
     * Gửi Action Bar hiển thị các skill đã gán
     */
    private void sendSkillBarActionBar(Player player) {
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());
        if (skillData == null) {
            exitSkillBarMode(player, false);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("&8✦ &aSkill Bar &8✦");

        for (int i = 0; i < 9; i++) {
            String skillId = skillData.getSkillBarSlot(i);
            if (skillId == null) {
                sb.append(" &7[").append(i + 1).append("]&8-");
                continue;
            }

            SkillManager.SkillConfig skill = getSkillById(skillId);
            if (skill == null) {
                sb.append(" &7[").append(i + 1).append("]&8-");
                continue;
            }

            boolean onCooldown = skillData.isOnCooldown(skillId);

            if (onCooldown) {
                sb.append(" &7[").append(i + 1).append("]&8").append(ColorUtils.stripColor(skill.name));
            } else {
                sb.append(" &a[").append(i + 1).append("]&f").append(ColorUtils.stripColor(skill.name));
            }
        }

        sb.append(" &8✦");

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ColorUtils.colorize(sb.toString())));
    }

    /**
     * Kiểm tra player đang ở skill bar mode không
     */
    public boolean isInSkillBarMode(Player player) {
        return skillBarMode.contains(player.getUniqueId());
    }

    /**
     * Hủy task auto-exit
     */
    private void cancelExitTask(UUID uuid) {
        Integer taskId = exitTaskIds.remove(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    /**
     * Hủy task refresh action bar
     */
    private void cancelRefreshTask(UUID uuid) {
        Integer taskId = refreshTaskIds.remove(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
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
            MessageUtils.send(player, "&cSlot " + (barSlot + 1) + " trống!");
            exitSkillBarMode(player, false);
            return;
        }

        SkillManager.SkillConfig skill = getSkillById(skillId);
        if (skill == null) {
            MessageUtils.send(player, "&cKỹ năng không tồn tại!");
            exitSkillBarMode(player, false);
            return;
        }

        PlayerCultivationData cultData = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (cultData == null) return;

        // Kiểm tra loại skill - chỉ ACTIVE mới cast được từ bar
        if (!skill.type.equals("ACTIVE")) {
            MessageUtils.send(player, "&cKỹ năng thụ động không thể cast từ Skill Bar!");
            exitSkillBarMode(player, false);
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
            exitSkillBarMode(player, false);
            return;
        }

        // Kiểm tra mana
        if (skill.manaCost > 0) {
            if (!plugin.getCultivationManager().consumeMana(player, skill.manaCost)) {
                MessageUtils.send(player, "&cKhông đủ linh lực! (Cần &b" + skill.manaCost + " &clinh lực)");
                exitSkillBarMode(player, false);
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

        // Tăng proficiency (usage count + points)
        skillData.incrementSkillUsage(skillId);
        skillData.addProficiencyPoints(skillId, 1); // +1 điểm mỗi lần thi triển

        // Kiểm tra lên bậc mới
        PlayerSkillData.ProficiencyLevel oldLevel = skillData.getProficiencyLevel(skillId);
        PlayerSkillData.ProficiencyLevel newLevel = skillData.getProficiencyLevel(skillId);
        if (oldLevel != newLevel) {
            MessageUtils.send(player, "&d&l✦ CÔNG PHÁP THĂNG CẤP! ✦");
            MessageUtils.send(player, "&e" + ColorUtils.stripColor(skill.name) + " &7đạt &e" + newLevel.getDisplayName() + "&7!");
            MessageUtils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
        }

        // Set cooldown (nếu không bypass)
        if (!bypassCooldown) {
            skillData.setCooldown(skillId, skill.cooldownSeconds);
        }

        // Thoát skill bar mode
        exitSkillBarMode(player, false);
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
        skillBarMode.remove(uuid);
        assignMode.remove(uuid);
        tempSkillBar.remove(uuid);
        cancelExitTask(uuid);
        cancelRefreshTask(uuid);
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Xử lý phím số 1-9 khi đang ở Skill Bar Mode
     * PlayerItemHeldEvent luôn fire khi bấm số (không phụ thuộc GUI)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Chỉ xử lý nếu player đang ở skill bar mode
        if (!skillBarMode.contains(uuid)) return;

        // Cancel để không đổi slot hotbar thật
        event.setCancelled(true);

        // event.getNewSlot() trả về 0-8 tương ứng phím 1-9
        int barSlot = event.getNewSlot();

        // Cast skill tương ứng
        castSkillFromBar(player, barSlot);
    }

    /**
     * Xử lý phím Q - vào/thoát Skill Bar Mode
     * Chỉ active khi không mở inventory khác
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Kiểm tra nếu đang mở inventory khác (không phải crafting/player)
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

        event.setCancelled(true);

        // Nếu đang ở skill bar mode → thoát
        if (skillBarMode.contains(uuid)) {
            exitSkillBarMode(player, true);
            MessageUtils.send(player, "&7✦ Đã thoát Skill Bar");
            return;
        }

        // Kiểm tra player đã có dữ liệu skill chưa
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(uuid);
        if (skillData == null) {
            MessageUtils.send(player, "&c✦ Bạn chưa có dữ liệu công pháp! Dùng &e/vnskill &cđể xem.");
            return;
        }

        // Vào skill bar mode
        MessageUtils.send(player, "&a✦ Skill Bar &7- Bấm số &e1-9 &7để thi triển, bấm &eQ &7để thoát");
        enterSkillBarMode(player);
    }

    /**
     * Thoát skill bar mode khi player rời game
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cleanupPlayer(uuid);
    }

    /**
     * Xử lý click trong Menu quản lý (giữ nguyên)
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Xử lý click trong Menu quản lý
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
    }

    /**
     * Xử lý drag trong Menu quản lý (giữ nguyên)
     */
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

    /**
     * Xử lý đóng Menu quản lý (giữ nguyên)
     */
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
            skillBarMode.remove(uuid);
            tempSkillBar.remove(uuid);
            return;
        }
    }
}