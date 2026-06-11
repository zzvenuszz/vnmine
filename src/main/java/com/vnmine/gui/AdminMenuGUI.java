package com.vnmine.gui;

import com.vnmine.VNMinePlugin;
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
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * AdminMenuGUI - Menu admin phân nhóm
 * Menu chính → chọn nhóm → sub-menu với các item
 */
public class AdminMenuGUI implements Listener {

    private final VNMinePlugin plugin;
    private final Map<UUID, String> openMenus = new HashMap<>();

    private static final String ADMIN_MAIN = "admin_main";
    private static final String SUB_PILLS = "admin_pills";
    private static final String SUB_ARTIFACTS = "admin_artifacts";
    private static final String SUB_SKILLS = "admin_skills";
    private static final String SUB_HERBS = "admin_herbs";
    private static final String SUB_MATERIALS = "admin_materials";
    private static final String SUB_MOUNTS = "admin_mounts";

    public AdminMenuGUI(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    // ==================== ĐỊNH NGHĨA ITEM ====================

    private static class AdminItemDef {
        final String displayName;
        final Material material;
        final String lore;
        final boolean stack64;

        AdminItemDef(String displayName, Material material, String lore, boolean stack64) {
            this.displayName = displayName;
            this.material = material;
            this.lore = lore;
            this.stack64 = stack64;
        }
    }

    private static final List<AdminItemDef> PILLS = Arrays.asList(
        new AdminItemDef("&aHồi Linh Đan", Material.GLOWSTONE_DUST,
                "&7Hồi phục &b30 &7linh lực", true),
        new AdminItemDef("&bĐại Hồi Linh Đan", Material.GLOWSTONE,
                "&7Hồi phục &b100 &7linh lực + hồi phục 30s", true),
        new AdminItemDef("&cCương Thể Đan", Material.REDSTONE_BLOCK,
                "&7Tăng &c20% sát thương &760s", true),
        new AdminItemDef("&aThanh Tâm Đan", Material.SUGAR,
                "&7Giải trừ mọi trạng thái xấu", false),
        new AdminItemDef("&bTốc Thánh Đan", Material.FEATHER,
                "&7Tăng &b50% tốc độ &730s", true),
        new AdminItemDef("&5Tu Luyện Đan", Material.PURPLE_DYE,
                "&7+50 EXP tu luyện", true),
        new AdminItemDef("&6&l◆ Phi Thăng Đan ◆", Material.NETHER_STAR,
                "&7+500 EXP (1 lần/đại cảnh giới)", true)
    );

    private static final List<AdminItemDef> ARTIFACTS = Arrays.asList(
        new AdminItemDef("&b&l◆ Kiếm Phi Hành ◆", Material.DIAMOND_SWORD,
                "&7Click phải ngự kiếm phi hành", false),
        new AdminItemDef("&6&l◆ Linh Chung ◆", Material.BELL,
                "&7Làm choáng quái trong bán kính", false),
        new AdminItemDef("&5&l◆ Bát Quái Kính ◆", Material.SHIELD,
                "&7Giảm 30% sát thương nhận vào", false),
        new AdminItemDef("&a&l◆ Hồn Ngọc ◆", Material.EMERALD,
                "&7Tự hồi 50% máu khi HP<30%", false),
        new AdminItemDef("&4&l◆ Thiên Linh Thuẫn ◆", Material.NETHERITE_CHESTPLATE,
                "&7Bất tử 5s, CD 3 phút", false),
        new AdminItemDef("&e&l◆ Lôi Ấn ◆", Material.TRIDENT,
                "&7Gọi sét đánh quái", false),
        new AdminItemDef("&6&l◆ Phượng Hoàng Lệnh ◆", Material.FEATHER,
                "&7Hồi sinh 1 lần sau khi chết (CD 60p)", false)
    );

    private static final List<AdminItemDef> HERBS = Arrays.asList(
        new AdminItemDef("&aLinh Thảo", Material.GREEN_DYE,
                "&7Nguyên liệu cơ bản", true),
        new AdminItemDef("&cHuyết Linh Thảo", Material.RED_DYE,
                "&7Nguyên liệu trung cấp", true),
        new AdminItemDef("&6Long Huyết Thảo", Material.ORANGE_DYE,
                "&7Nguyên liệu cao cấp", true)
    );

    private static final List<AdminItemDef> MATERIALS = Arrays.asList(
        new AdminItemDef("&fNước", Material.POTION,
                "&7Nước tinh khiết", false),
        new AdminItemDef("&cBột Blaze", Material.BLAZE_POWDER,
                "&7Nguyên liệu đặc biệt", true),
        new AdminItemDef("&6Vàng", Material.GOLD_INGOT,
                "&7Kim loại quý", true),
        new AdminItemDef("&bKim Cương", Material.DIAMOND,
                "&7Kim cương thô", true),
        new AdminItemDef("&5Ngọc Lục Bảo", Material.EMERALD,
                "&7Ngọc quý", true),
        new AdminItemDef("&8Obsidian", Material.OBSIDIAN,
                "&7Vật liệu phòng hộ", true),
        new AdminItemDef("&aMắt End", Material.ENDER_PEARL,
                "&7Vật liệu không gian", true),
        new AdminItemDef("&5Netherite", Material.NETHERITE_INGOT,
                "&7Vật liệu tối thượng", false),
        new AdminItemDef("&dHơi Rồng", Material.DRAGON_BREATH,
                "&7Hơi thở của rồng", false),
        new AdminItemDef("&6Vàng Khối", Material.GOLD_BLOCK,
                "&7Khối vàng nguyên chất", true),
        new AdminItemDef("&bPha Lê", Material.DIAMOND,
                "&7Pha lê năng lượng", true),
        new AdminItemDef("&cMắt End (Ác)", Material.ENDER_EYE,
                "&7Mắt ender đã kích hoạt", true),
        new AdminItemDef("&7Lông Vũ", Material.FEATHER,
                "&7Lông vũ phượng hoàng", true)
    );

    // Mount keys
    private static final List<AdminItemDef> MOUNT_KEYS = Arrays.asList(
        new AdminItemDef("&6&l◆ Phượng Hoàng Lệnh Bài ◆", Material.FEATHER,
                "&7Key triệu hồi Phượng Hoàng tọa kỵ", false),
        new AdminItemDef("&f&l◆ Bạch Hổ Lệnh Bài ◆", Material.BONE,
                "&7Key triệu hồi Bạch Hổ tọa kỵ", false),
        new AdminItemDef("&a&l◆ Thanh Long Lệnh Bài ◆", Material.DRAGON_BREATH,
                "&7Key triệu hồi Thanh Long tọa kỵ", false)
    );

    // Skill books (grade mặc định HOANG/HA)
    private static final List<AdminItemDef> SKILL_BOOKS = Arrays.asList(
        new AdminItemDef("&aTrúc Cơ Liệu Thương (Hoàng Hạ)", Material.ENCHANTED_BOOK,
                "&7Skill: BASIC_HEAL - Hồi 10 HP", false),
        new AdminItemDef("&bLinh Khí Hộ Thể (Hoàng Trung)", Material.ENCHANTED_BOOK,
                "&7Skill: QI_SHIELD - Hấp thụ 20 sát thương", false),
        new AdminItemDef("&cHỏa Cầu Thuật (Hoàng Hạ)", Material.ENCHANTED_BOOK,
                "&7Skill: FIRE_BALL - Gây 15 sát thương", false),
        new AdminItemDef("&eThiên Lôi Dẫn (Huyền Hạ)", Material.ENCHANTED_BOOK,
                "&7Skill: LIGHTNING_STRIKE - Gây 25 sát thương", false),
        new AdminItemDef("&bPhi Vân Bộ (Hoàng Hạ)", Material.ENCHANTED_BOOK,
                "&7Skill: SPEED_STEP - Tăng 40% tốc độ 15s", false),
        new AdminItemDef("&dThuấn Di (Huyền Hạ)", Material.ENCHANTED_BOOK,
                "&7Skill: TELEPORT - Dịch chuyển tầm nhìn", false),
        new AdminItemDef("&fPhong Nhẫn (Hoàng Trung)", Material.ENCHANTED_BOOK,
                "&7Skill: WIND_BLADE - Gây 12 sát thương xuyên thấu", false),
        new AdminItemDef("&6Tinh Thần Bạo (Thiên Hạ)", Material.ENCHANTED_BOOK,
                "&7Skill: METEOR_STORM - Gây 35 sát thương AOE", false)
    );

    // Map skill stripped names to IDs for give
    private static final Map<String, String> SKILL_ID_MAP = new HashMap<>();
    static {
        SKILL_ID_MAP.put("Trúc Cơ Liệu Thương (Hoàng Hạ)", "BASIC_HEAL");
        SKILL_ID_MAP.put("Linh Khí Hộ Thể (Hoàng Trung)", "QI_SHIELD");
        SKILL_ID_MAP.put("Hỏa Cầu Thuật (Hoàng Hạ)", "FIRE_BALL");
        SKILL_ID_MAP.put("Thiên Lôi Dẫn (Huyền Hạ)", "LIGHTNING_STRIKE");
        SKILL_ID_MAP.put("Phi Vân Bộ (Hoàng Hạ)", "SPEED_STEP");
        SKILL_ID_MAP.put("Thuấn Di (Huyền Hạ)", "TELEPORT");
        SKILL_ID_MAP.put("Phong Nhẫn (Hoàng Trung)", "WIND_BLADE");
        SKILL_ID_MAP.put("Tinh Thần Bạo (Thiên Hạ)", "METEOR_STORM");
    }

    // ==================== MỞ MENU ====================

    /**
     * Mở menu chính admin
     */
    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ColorUtils.colorize("&8✦ VNMine Admin Menu ✦"));

        // Viền
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName("&r")
                .build();
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        // Nút Tu Luyện (Đan dược)
        gui.setItem(11, new ItemBuilder(Material.NETHER_STAR)
                .setGlow(true)
                .setName("&a&l◆ Tu Luyện (Đan Dược) ◆")
                .setLore(
                        "",
                        "&7Các loại đan dược tu luyện",
                        "&7Click để mở danh sách",
                        "",
                        "&eClick để mở!"
                ).build());

        // Nút Pháp Bảo
        gui.setItem(12, new ItemBuilder(Material.DIAMOND_SWORD)
                .setGlow(true)
                .setName("&6&l◆ Pháp Bảo ◆")
                .setLore(
                        "",
                        "&7Các loại pháp bảo thần kỳ",
                        "&7Click để mở danh sách",
                        "",
                        "&eClick để mở!"
                ).build());

        // Nút Công Pháp
        gui.setItem(13, new ItemBuilder(Material.ENCHANTED_BOOK)
                .setGlow(true)
                .setName("&d&l◆ Công Pháp (Skill Books) ◆")
                .setLore(
                        "",
                        "&7Sách kỹ năng các loại",
                        "&7Click để mở danh sách",
                        "",
                        "&eClick để mở!"
                ).build());

        // Nút Linh Thảo
        gui.setItem(14, new ItemBuilder(Material.GREEN_DYE)
                .setGlow(true)
                .setName("&2&l◆ Linh Thảo ◆")
                .setLore(
                        "",
                        "&7Nguyên liệu luyện đan",
                        "&7Click để mở danh sách",
                        "",
                        "&eClick để mở!"
                ).build());

        // Nút Nguyên Liệu
        gui.setItem(15, new ItemBuilder(Material.GOLD_INGOT)
                .setGlow(true)
                .setName("&e&l◆ Nguyên Liệu ◆")
                .setLore(
                        "",
                        "&7Nguyên liệu chế tạo pháp bảo",
                        "&7Click để mở danh sách",
                        "",
                        "&eClick để mở!"
                ).build());

        // Nút Tọa Kỵ
        gui.setItem(22, new ItemBuilder(Material.SADDLE)
                .setGlow(true)
                .setName("&6&l◆ Tọa Kỵ (Mount Keys) ◆")
                .setLore(
                        "",
                        "&7Key triệu hồi tọa kỵ",
                        "&7Phượng Hoàng, Bạch Hổ, Thanh Long",
                        "&7Click để mở danh sách",
                        "",
                        "&eClick để mở!"
                ).build());

        // Nút Đóng
        gui.setItem(26, new ItemBuilder(Material.BARRIER)
                .setName("&c&lĐóng")
                .build());

        openMenus.put(player.getUniqueId(), ADMIN_MAIN);
        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    /**
     * Mở sub-menu với danh sách item
     */
    private void openSubMenu(Player player, String menuKey, String title, List<AdminItemDef> items) {
        int size = ((items.size() / 9) + 1) * 9;
        if (size < 18) size = 18;

        Inventory gui = Bukkit.createInventory(null, Math.min(size, 54),
                ColorUtils.colorize(title));

        // Đổ item vào
        int slot = 0;
        for (AdminItemDef item : items) {
            gui.setItem(slot++, createMenuItem(item));
        }

        // Nút quay lại
        int backSlot = Math.min(size, 53);
        gui.setItem(backSlot, new ItemBuilder(Material.ARROW)
                .setName("&e&l← Quay Lại")
                .build());

        openMenus.put(player.getUniqueId(), menuKey);
        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    private ItemStack createMenuItem(AdminItemDef def) {
        String amountStr = def.stack64 ? "&8[x64]" : "&8[x1]";
        ItemBuilder builder = new ItemBuilder(def.material)
                .setName(def.displayName)
                .setLore(
                        "",
                        def.lore,
                        "",
                        "&eClick để thêm vào kho đồ!",
                        amountStr
                );
        if (def.stack64) {
            builder.setGlow(true);
        }
        return builder.build();
    }

    // ==================== XỬ LÝ CLICK ====================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String currentMenu = openMenus.get(player.getUniqueId());
        if (currentMenu == null) return;

        // Cancel tất cả mọi click (cả top và bottom inventory)
        event.setCancelled(true);

        // Chỉ xử lý click vào top inventory
        if (event.getClickedInventory() == null) return;
        InventoryView view = event.getView();
        if (event.getClickedInventory() != view.getTopInventory()) {
            // Bottom inventory click: đã cancel ở trên, không xử lý gì thêm
            return;
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= view.getTopInventory().getSize()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (currentMenu) {
            case ADMIN_MAIN:
                handleAdminMainClick(player, slot);
                break;
            case SUB_PILLS:
                handleSubItemClick(player, PILLS, clicked);
                break;
            case SUB_ARTIFACTS:
                handleSubItemClick(player, ARTIFACTS, clicked);
                break;
            case SUB_SKILLS:
                handleSkillClick(player, clicked, slot);
                break;
            case SUB_HERBS:
                handleSubItemClick(player, HERBS, clicked);
                break;
            case SUB_MATERIALS:
                handleSubItemClick(player, MATERIALS, clicked);
                break;
            case SUB_MOUNTS:
                handleMountClick(player, clicked, slot);
                break;
        }
    }

    private void handleAdminMainClick(Player player, int slot) {
        switch (slot) {
            case 11: // Tu Luyện
                openSubMenu(player, SUB_PILLS, "&8✦ Admin - Đan Dược ✦", PILLS);
                break;
            case 12: // Pháp Bảo
                openSubMenu(player, SUB_ARTIFACTS, "&8✦ Admin - Pháp Bảo ✦", ARTIFACTS);
                break;
            case 13: // Công Pháp
                openSubMenu(player, SUB_SKILLS, "&8✦ Admin - Công Pháp ✦", SKILL_BOOKS);
                break;
            case 14: // Linh Thảo
                openSubMenu(player, SUB_HERBS, "&8✦ Admin - Linh Thảo ✦", HERBS);
                break;
            case 15: // Nguyên Liệu
                openSubMenu(player, SUB_MATERIALS, "&8✦ Admin - Nguyên Liệu ✦", MATERIALS);
                break;
            case 22: // Tọa Kỵ
                openSubMenu(player, SUB_MOUNTS, "&8✦ Admin - Tọa Kỵ ✦", MOUNT_KEYS);
                break;
            case 26: // Đóng
                player.closeInventory();
                break;
        }
    }

    private void handleSubItemClick(Player player, List<AdminItemDef> items, ItemStack clicked) {
        // Kiểm tra nếu là nút quay lại (back arrow ở slot cuối)
        String stripped = stripColor(clicked.getItemMeta() != null
                ? clicked.getItemMeta().getDisplayName() : "");
        if (stripped.contains("Quay Lại") || stripped.contains("Back") || stripped.contains("←")) {
            open(player);
            return;
        }

        AdminItemDef matched = findItemInList(items, clicked);
        if (matched == null) return;

        giveItemToPlayer(player, matched);
    }

    private void handleSkillClick(Player player, ItemStack clicked, int slot) {
        // Kiểm tra nếu là nút quay lại
        String stripped = stripColor(clicked.getItemMeta() != null
                ? clicked.getItemMeta().getDisplayName() : "");
        if (stripped.contains("Quay Lại") || stripped.contains("←")) {
            open(player);
            return;
        }

        // Tìm skill match
        AdminItemDef matched = findItemInList(SKILL_BOOKS, clicked);
        if (matched == null) return;

        // Lấy skill ID từ tên
        String skillId = SKILL_ID_MAP.get(stripColor(matched.displayName));
        if (skillId == null) {
            MessageUtils.send(player, "&cKhông tìm thấy skill ID!");
            return;
        }

        // Give skill book
        if (plugin.getSkillBookManager() != null) {
            // Mặc định grade Hoàng Hạ
            String grade = "HOANG";
            String subGrade = "HA";
            String strippedName = stripColor(matched.displayName);
            if (strippedName.contains("Huyền")) grade = "HUYEN";
            else if (strippedName.contains("Thiên")) grade = "THIEN";
            if (strippedName.contains("Trung")) subGrade = "TRUNG";
            else if (strippedName.contains("Thượng")) subGrade = "THUONG";

            ItemStack book = plugin.getSkillBookManager().createSkillBook(skillId, grade, subGrade);
            if (book != null) {
                // Gắn NBT tag
                ItemBuilder builder = new ItemBuilder(book.getType())
                        .setAmount(1);
                if (book.hasItemMeta() && book.getItemMeta().hasDisplayName()) {
                    builder.setName(book.getItemMeta().getDisplayName());
                }
                builder.setPersistentData("vnmine_item", "true");
                ItemStack giveItem = builder.build();

                Map<Integer, ItemStack> leftover = player.getInventory().addItem(giveItem);
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
                MessageUtils.send(player, "&a✦ Đã nhận sách công pháp: &f" + matched.displayName);
                MessageUtils.playSound(player, Sound.ENTITY_ITEM_PICKUP);
            } else {
                MessageUtils.send(player, "&cKhông thể tạo sách công pháp!");
            }
        }
    }

    private void handleMountClick(Player player, ItemStack clicked, int slot) {
        // Kiểm tra nếu là nút quay lại
        String stripped = stripColor(clicked.getItemMeta() != null
                ? clicked.getItemMeta().getDisplayName() : "");
        if (stripped.contains("Quay Lại") || stripped.contains("←")) {
            open(player);
            return;
        }

        AdminItemDef matched = findItemInList(MOUNT_KEYS, clicked);
        if (matched == null) return;

        // Give mount key item và unlock mount
        String mountId = null;
        String strippedName = stripColor(matched.displayName);
        if (strippedName.contains("Phượng Hoàng")) mountId = "PHUONG_HOANG";
        else if (strippedName.contains("Bạch Hổ")) mountId = "BACH_HO";
        else if (strippedName.contains("Thanh Long")) mountId = "THANH_LONG";

        if (mountId != null && plugin.getMountManager() != null) {
            // Unlock mount
            plugin.getMountManager().unlockMount(player, mountId);
            // Give item
            giveItemToPlayer(player, matched);
            MessageUtils.send(player, "&a✦ Đã mở khóa tọa kỵ: " + matched.displayName + " &a(click phải item để học)");
        } else {
            giveItemToPlayer(player, matched);
        }
    }

    private void giveItemToPlayer(Player player, AdminItemDef matched) {
        int amount = matched.stack64 ? 64 : 1;

        ItemBuilder giveBuilder = new ItemBuilder(matched.material)
                .setName(matched.displayName)
                .setLore("", matched.lore)
                .setGlow(true)
                .setAmount(amount);

        // Gắn persistent data "vnmine_item" để BlockPlaceListener chặn đặt
        giveBuilder.setPersistentData("vnmine_item", "true");

        ItemStack giveItem = giveBuilder.build();

        // Thêm vào kho, nếu đầy thì drop
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(giveItem);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }

        String amountStr = matched.stack64 ? "&ex64" : "&ex1";
        MessageUtils.send(player, "&a✦ Đã nhận &f" + matched.displayName + " &r&a(" + amountStr + "&a)!");
        MessageUtils.playSound(player, Sound.ENTITY_ITEM_PICKUP);
    }

    private AdminItemDef findItemInList(List<AdminItemDef> items, ItemStack clicked) {
        Material type = clicked.getType();
        String stripped = stripColor(clicked.getItemMeta() != null
                ? clicked.getItemMeta().getDisplayName() : "");

        for (AdminItemDef def : items) {
            if (def.material == type && stripColor(def.displayName).equals(stripped)) {
                return def;
            }
        }
        return null;
    }

    private String stripColor(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        cleanupPlayer(event.getPlayer().getUniqueId());
    }

    public void cleanupPlayer(UUID uuid) {
        openMenus.remove(uuid);
    }
}