package com.vnmine.gui;

import com.vnmine.VNMinePlugin;
import com.vnmine.item.ItemBuilder;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AdminMenuGUI implements Listener {

    private final VNMinePlugin plugin;

    private static final String TITLE_ADMIN_MAIN = "✦ VNMine Admin Menu ✦";
    private static final String TITLE_PILLS = "✦ Admin - Dan Duoc";
    private static final String TITLE_ARTIFACTS = "✦ Admin - Phap Bao";
    private static final String TITLE_SKILLS = "✦ Admin - Cong Phap ✦";
    private static final String TITLE_HERBS = "✦ Admin - Linh Thao ✦";
    private static final String TITLE_MATERIALS = "✦ Admin - Nguyen Lieu ✦";
    private static final String TITLE_MOUNTS = "✦ Admin - Toa Ky ✦";
    private static final String TITLE_CURRENCY = "✦ Admin - Linh Thach ✦";

    private final Map<UUID, int[]> pageState = new HashMap<>();

    private static final List<String> ADMIN_TITLES = Arrays.asList(
        TITLE_ADMIN_MAIN, TITLE_PILLS, TITLE_ARTIFACTS, TITLE_SKILLS,
        TITLE_HERBS, TITLE_MATERIALS, TITLE_MOUNTS, TITLE_CURRENCY
    );

    // ==================== SKILL GRADES (12: Hoàng/Huyền/Địa/Thiên × Hạ/Trung/Thượng) ====================
    private static final String[] SKILL_GRADES = {"Hoàng", "Huyền", "Địa", "Thiên"};
    private static final String[] SKILL_SUBS = {"Hạ", "Trung", "Thượng"};
    private static final String[] SKILL_GRADE_COLORS = {"&7", "&b", "&5", "&6"};
    private static final String[] SKILL_SUB_COLORS = {"&f", "&e", "&a"};

    // ==================== ARTIFACT GRADES (21 tiers) ====================
    private static final String[][] ARTIFACT_GRADES = {
        // {giaiName, capName, giaiColor, capColor}
        {"Phàm Giai", "Phàm Cấp", "&7", "&7"},
        {"Phàm Giai", "Hậu Thiên", "&7", "&8"},
        {"Phàm Giai", "Tiên Thiên", "&7", "&f"},
        {"Linh Giai", "Linh Cấp", "&a", "&a"},
        {"Linh Giai", "Địa Cấp", "&a", "&2"},
        {"Linh Giai", "Thiên Cấp", "&a", "&b"},
        {"Vương Giai", "Vương Cấp", "&6", "&6"},
        {"Vương Giai", "Hoàng Cấp", "&6", "&e"},
        {"Vương Giai", "Tôn Cấp", "&6", "&c"},
        {"Thánh Giai", "Nhập Thánh", "&f", "&f&l"},
        {"Thánh Giai", "Đại Thánh", "&f", "&d"},
        {"Đế Giai", "Đế Cấp", "&5", "&5"},
        {"Đế Giai", "Đại Đế", "&5", "&5&l"},
        {"Tiên Giai", "Chân Tiên", "&b", "&b&l"},
        {"Tiên Giai", "Kim Tiên", "&b", "&3"},
        {"Tiên Giai", "Tiên Đế", "&b", "&3&l"},
        {"Thần Giai", "Chân Thần", "&c", "&c&l"},
        {"Thần Giai", "Thiên Thần", "&c", "&4"},
        {"Thần Giai", "Chủ Thần", "&c", "&4&l"},
        {"Chí Tôn Giai", "Chí Tôn", "&4", "&6&l"},
        {"Chí Tôn Giai", "Chúa Tể", "&4", "&4&k"}
    };

    private static final double[] ARTIFACT_MULTIPLIERS = {
        0.0, 0.1, 0.2, 0.35, 0.5, 0.65,
        0.8, 1.0, 1.2, 1.5, 1.8,
        2.2, 2.5, 3.0, 3.5, 4.0,
        4.5, 5.0, 6.0, 7.0, 8.0
    };

    // Helper: build artifact display name
    private static String artifactName(String baseName, int tierIndex) {
        String[] g = ARTIFACT_GRADES[tierIndex];
        return g[2] + "[" + g[0] + "]-" + g[3] + "[" + g[1] + "] &f&l" + baseName;
    }

    // Helper: build skill display name
    private static String skillName(String baseName, int gradeIndex, int subIndex) {
        String gc = SKILL_GRADE_COLORS[gradeIndex];
        String sc = SKILL_SUB_COLORS[subIndex];
        return gc + "[" + SKILL_GRADES[gradeIndex] + " Cấp]-" + sc + "[" + SKILL_SUBS[subIndex] + " Phẩm] &f&l" + baseName;
    }

    public AdminMenuGUI(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isOwnInventory(InventoryClickEvent event) {
        String title = ColorUtils.stripColor(event.getView().getTitle());
        for (String t : ADMIN_TITLES) { if (title.contains(t)) return true; }
        return false;
    }

    private static class AdminItemDef {
        final String displayName;
        final Material material;
        final String lore;
        final boolean stack64;
        AdminItemDef(String displayName, Material material, String lore, boolean stack64) {
            this.displayName = displayName; this.material = material; this.lore = lore; this.stack64 = stack64;
        }
    }

    // ==================== PILL DEFINITIONS (12 grades) - dùng POTION + charge system ====================
    private static final List<AdminItemDef> ALL_PILLS = new ArrayList<>();
    static {
        String[][] pillTypes = {
            {"Hồi Linh Đan", "POTION", "Hồi phục linh lực"},
            {"Đại Hồi Linh Đan", "POTION", "Hồi phục lớn linh lực + hồi phục"},
            {"Cương Thể Đan", "POTION", "Tăng sát thương"},
            {"Thanh Tâm Đan", "POTION", "Giải trừ trạng thái xấu"},
            {"Tốc Thánh Đan", "POTION", "Tăng tốc độ di chuyển"},
            {"Tu Luyện Đan", "POTION", "Tăng EXP tu luyện"},
            {"Phi Thăng Đan", "POTION", "EXP lớn (1 lần/đại cảnh giới)"},
            {"Bách Độc Đan", "POTION", "Miễn nhiễm độc"},
            {"Thiên Hồi Đan", "POTION", "Hồi HP + Linh lực lớn"},
            {"Phê Ma Đan", "POTION", "Tăng sát thương vs quái"},
            {"Trường Thọ Đan", "POTION", "Hồi sinh sau khi chết"}
        };
        double[] manaMultipliers = {1.0, 1.2, 1.4, 1.6, 1.8, 2.0, 2.3, 2.6, 3.0, 3.5, 4.0, 5.0};
        int[] cooldowns = {60, 55, 50, 45, 40, 35, 30, 25, 20, 15, 10, 5};
        // Màu sắc theo phẩm cấp (đồng bộ với AlchemyCraftGUI)
        org.bukkit.Color[] colors = {
            org.bukkit.Color.WHITE, org.bukkit.Color.YELLOW, org.bukkit.Color.LIME,
            org.bukkit.Color.AQUA, org.bukkit.Color.ORANGE, org.bukkit.Color.GREEN,
            org.bukkit.Color.PURPLE, org.bukkit.Color.fromRGB(0xFF00FF), org.bukkit.Color.RED,
            org.bukkit.Color.fromRGB(0xFFD700), org.bukkit.Color.fromRGB(0x00FFFF), org.bukkit.Color.fromRGB(0x8B00FF)
        };

        for (String[] pill : pillTypes) {
            Material mat = Material.POTION;
            for (int g = 0; g < 4; g++) {
                for (int s = 0; s < 3; s++) {
                    int idx = g * 3 + s;
                    String dn = skillName(pill[0], g, s);
                    String lore = "&7" + pill[2] + "\n&7Hiệu quả: x" + String.format("%.1f", manaMultipliers[idx])
                        + "\n&7CD: " + cooldowns[idx] + "s\n&7Lượng dùng: 10 lần";
                    ALL_PILLS.add(new AdminItemDef(dn, mat, lore, false));
                }
            }
        }
    }

    // ==================== ARTIFACT DEFINITIONS (21 tiers) ====================
    private static final List<AdminItemDef> ALL_ARTIFACTS = new ArrayList<>();
    static {
        String[][] artifactTypes = {
            {"Kiếm Phi Hành", "DIAMOND_SWORD", "Ngự kiếm phi hành"},
            {"Linh Chung", "BELL", "Làm choáng quái AOE"},
            {"Bát Quái Kính", "SHIELD", "Giảm sát thương nhận vào"},
            {"Hồn Ngọc", "EMERALD", "Tự hồi máu khi HP thấp"},
            {"Thiên Linh Thuẫn", "NETHERITE_CHESTPLATE", "Bất tử tạm thời"},
            {"Lôi Ấn", "TRIDENT", "Gọi sét đánh quái"},
            {"Phượng Hoàng Lệnh", "FEATHER", "Hồi sinh sau khi chết"}
        };

        for (String[] art : artifactTypes) {
            Material mat = Material.valueOf(art[1]);
            for (int t = 0; t < ARTIFACT_GRADES.length; t++) {
                String dn = artifactName(art[0], t);
                String gradeLabel = ARTIFACT_GRADES[t][0] + " " + ARTIFACT_GRADES[t][1];
                double mult = ARTIFACT_MULTIPLIERS[t];
                String lore = "&7" + art[2]
                    + "\n&7Phẩm cấp: " + gradeLabel
                    + (mult > 0 ? "\n&7Giảm tiêu hao LL: " + (int)(mult * 100) + "%" : "\n&7Pháp bảo cơ bản");
                ALL_ARTIFACTS.add(new AdminItemDef(dn, mat, lore, false));
            }
        }
    }

    // ==================== HERBS (tiered) ====================
    private static final List<AdminItemDef> HERBS = new ArrayList<>();
    static {
        // Hạ Phẩm
        addHerb("&f[Hạ Phẩm] &f&lLinh Thảo", "GREEN_DYE", "Nguyên liệu cơ bản, quái yếu掉落");
        addHerb("&f[Hạ Phẩm] &f&lBình Linh Thảo", "LIGHT_BLUE_DYE", "Tinh hoa giải độc, quái vùng tuyết掉落");
        addHerb("&f[Hạ Phẩm] &f&lLôi Linh Thảo", "YELLOW_DYE", "Hấp thu lôi khí, quái lightning掉落");
        // Trung Phẩm
        addHerb("&e[Trung Phẩm] &f&lHuyết Linh Thảo", "RED_DYE", "Nguyên liệu trung cấp, quái trung cấp掉落");
        addHerb("&e[Trung Phẩm] &f&lHoa Linh Thảo", "MAGENTA_DYE", "Duy hỏa, Blaze掉落");
        // Thượng Phẩm
        addHerb("&a[Thượng Phẩm] &f&lLong Huyết Thảo", "ORANGE_DYE", "Nguyên liệu cao cấp, Elite掉落");
        addHerb("&a[Thượng Phẩm] &f&lThiên Linh Thảo", "CYAN_DYE", "Linh thảo thiên thượng, Boss掉落");
        addHerb("&a[Thượng Phẩm] &f&lVạn Niên Linh Chi", "PURPLE_DYE", "Tăng 100 năm tu vi, Boss hiếm掉落");
    }
    private static void addHerb(String dn, String mat, String desc) {
        HERBS.add(new AdminItemDef(dn, Material.valueOf(mat), "&7" + desc, true));
    }

    // ==================== MATERIALS (tiered) ====================
    private static final List<AdminItemDef> MATERIALS = new ArrayList<>();
    static {
        // Hạ Phẩm
        addMat("&f[Hạ Phẩm] &f&lNước Tinh Khiết", "POTION", "Nước dùng luyện đan", false);
        addMat("&f[Hạ Phẩm] &f&lNgân Sa", "IRON_NUGGET", "Giảm thời gian nấu chảy", true);
        addMat("&f[Hạ Phẩm] &f&lLông Vũ", "FEATHER", "Lông vũ phượng hoàng", true);
        addMat("&f[Hạ Phẩm] &f&lBột Blaze", "BLAZE_POWDER", "Tinh hoa lửa", true);
        // Trung Phẩm
        addMat("&e[Trung Phẩm] &f&lVàng Thanh", "GOLD_INGOT", "Kim loại quý", true);
        addMat("&e[Trung Phẩm] &f&lKim Cương", "DIAMOND", "Kim cương thô", true);
        addMat("&e[Trung Phẩm] &f&lNgọc Lục Bảo", "EMERALD", "Ngọc quý", true);
        addMat("&e[Trung Phẩm] &f&lHuyết Thạch", "REDSTONE", "Tăng phẩm đan dược", true);
        addMat("&e[Trung Phẩm] &f&lLong Lân", "PRISMARINE_SHARD", "Vật liệu pháp khí", true);
        addMat("&e[Trung Phẩm] &f&lThiên Thạch", "END_STONE", "Nguyên liệu thiên ngoại", true);
        addMat("&e[Trung Phẩm] &f&lHuyền Kim", "IRON_INGOT", "Kim loại huyền bí", true);
        // Thượng Phẩm
        addMat("&a[Thượng Phẩm] &f&lĐá Obsidian", "OBSIDIAN", "Vật liệu phong hỏa", true);
        addMat("&a[Thượng Phẩm] &f&lMắt End", "ENDER_PEARL", "Vật liệu không gian", true);
        addMat("&a[Thượng Phẩm] &f&lMắt Ender", "ENDER_EYE", "Mắt ender kích hoạt", true);
        addMat("&a[Thượng Phẩm] &f&lNetherite Thanh", "NETHERITE_INGOT", "Vật liệu tối thượng", false);
        addMat("&a[Thượng Phẩm] &f&lHơi Rồng", "DRAGON_BREATH", "Hơi thở của rồng", false);
        addMat("&a[Thượng Phẩm] &f&lKhối Vàng", "GOLD_BLOCK", "Khối vàng nguyên chất", true);
    }
    private static void addMat(String dn, String mat, String desc, boolean stack64) {
        MATERIALS.add(new AdminItemDef(dn, Material.valueOf(mat), "&7" + desc, stack64));
    }

    // ==================== MOUNT KEYS ====================
    private static final List<AdminItemDef> MOUNT_KEYS = Arrays.asList(
        new AdminItemDef("&6Phượng Hoàng Lệnh Bài", Material.FEATHER, "&7Key triệu hồi Phượng Hoàng", false),
        new AdminItemDef("&fBạch Hổ Lệnh Bài", Material.BONE, "&7Key triệu hồi Bạch Hổ", false),
        new AdminItemDef("&aThanh Long Lệnh Bài", Material.DRAGON_BREATH, "&7Key triệu hồi Thanh Long", false)
    );

    // ==================== SKILL BOOKS (12 grades) ====================
    private static final List<AdminItemDef> SKILL_BOOKS = new ArrayList<>();
    private static final Map<String, String> SKILL_ID_MAP = new HashMap<>();
    static {
        String[][] combatSkills = {
            {"BASIC_HEAL", "Trúc Cơ Liệu Thương", "Hồi phục HP"},
            {"QI_SHIELD", "Linh Khí Hộ Thể", "Hộ thể linh khí"},
            {"FIRE_BALL", "Hỏa Cầu Thuật", "Hỏa cầu thuật"},
            {"LIGHTNING_STRIKE", "Thiên Lôi Đạn", "Thiên lôi đạn"},
            {"SPEED_STEP", "Phi Vân Bộ", "Phi vân bộ"},
            {"TELEPORT", "Thuấn Di", "Thuấn di"},
            {"WIND_BLADE", "Phong Nhận", "Phong nhận"},
            {"METEOR_STORM", "Tinh Thần Bào", "Tinh thần bào"}
        };
        for (String[] skill : combatSkills) {
            for (int g = 0; g < 4; g++) {
                for (int s = 0; s < 3; s++) {
                    String dn = skillName(skill[1], g, s);
                    SKILL_BOOKS.add(new AdminItemDef(dn, Material.ENCHANTED_BOOK,
                        "&7Skill: " + skill[0] + " - " + skill[2], false));
                    SKILL_ID_MAP.put(stripColorRaw(dn), skill[0]);
                }
            }
        }
        String[][] craftSkills = {
            {"FIRE_CONTROL", "Khống Hỏa Thuật", "Kiểm soát hỏa hậu luyện đan"},
            {"FORGE_MASTERY", "Luyện Khí Thuật", "Thuật luyện khí đỉnh cao"}
        };
        for (String[] skill : craftSkills) {
            for (int g = 0; g < 4; g++) {
                for (int s = 0; s < 3; s++) {
                    String dn = skillName(skill[1], g, s);
                    SKILL_BOOKS.add(new AdminItemDef(dn, Material.ENCHANTED_BOOK,
                        "&7Skill: " + skill[0] + " - " + skill[2], false));
                    SKILL_ID_MAP.put(stripColorRaw(dn), skill[0]);
                }
            }
        }
    }

    // ==================== CURRENCY ====================
    private static final List<AdminItemDef> CURRENCY_ITEMS = Arrays.asList(
        new AdminItemDef("&bLinh Thạch Hạ Phẩm", Material.PRISMARINE_SHARD, "&71 LT Hạ = 1 linh thạch", true),
        new AdminItemDef("&dLinh Thạch Trung Phẩm", Material.DARK_PRISMARINE, "&71 LT Trung = 100 LT Hạ", false),
        new AdminItemDef("&6Linh Thạch Thượng Phẩm", Material.HEART_OF_THE_SEA, "&71 LT Thượng = 10000 LT Hạ", false)
    );

    // ==================== OPEN MENU ====================
    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ColorUtils.colorize("&8" + TITLE_ADMIN_MAIN));
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&r").build();
        for (int i = 0; i < 27; i++) gui.setItem(i, border);
        gui.setItem(10, new ItemBuilder(Material.NETHER_STAR).setGlow(true).setName("&aĐan Dược (12 phẩm cấp)")
                .setLore("", "&7" + ALL_PILLS.size() + " loại đan dược").build());
        gui.setItem(11, new ItemBuilder(Material.DIAMOND_SWORD).setGlow(true).setName("&6Pháp Bảo (21 phẩm cấp)")
                .setLore("", "&7" + ALL_ARTIFACTS.size() + " loại pháp bảo").build());
        gui.setItem(12, new ItemBuilder(Material.ENCHANTED_BOOK).setGlow(true).setName("&dCông Pháp (12 phẩm cấp)")
                .setLore("", "&7" + SKILL_BOOKS.size() + " sách kỹ năng").build());
        gui.setItem(13, new ItemBuilder(Material.GREEN_DYE).setGlow(true).setName("&2Linh Thảo")
                .setLore("", "&7" + HERBS.size() + " loại linh thảo").build());
        gui.setItem(14, new ItemBuilder(Material.GOLD_INGOT).setGlow(true).setName("&eNguyên Liệu")
                .setLore("", "&7" + MATERIALS.size() + " loại nguyên liệu").build());
        gui.setItem(15, new ItemBuilder(Material.SADDLE).setGlow(true).setName("&6Tọa Kỵ")
                .setLore("", "&7Key triệu hồi tọa kỵ").build());
        gui.setItem(16, new ItemBuilder(Material.PRISMARINE_SHARD).setGlow(true).setName("&bLinh Thạch")
                .setLore("", "&7Tiền tệ tu tiên").build());
        gui.setItem(26, new ItemBuilder(Material.BARRIER).setName("&cĐóng").build());
        pageState.remove(player.getUniqueId());
        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    // ==================== PAGINATION ====================
    private void openPaginatedMenu(Player player, String titleSuffix, List<AdminItemDef> items) {
        int page = 0;
        int[] state = pageState.get(player.getUniqueId());
        if (state != null) page = state[0];
        int itemsPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));
        page = Math.max(0, Math.min(page, totalPages - 1));
        Inventory gui = Bukkit.createInventory(null, 54,
            ColorUtils.colorize("&8" + titleSuffix + " &7[Trang " + (page + 1) + "/" + totalPages + "]"));
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());
        for (int i = start; i < end; i++) gui.setItem(i - start, createMenuItem(items.get(i)));
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&r").build();
        for (int i = 45; i < 54; i++) gui.setItem(i, border);
        gui.setItem(45, new ItemBuilder(Material.ARROW).setName("&e← Quay Lai").build());
        gui.setItem(48, page > 0 ? new ItemBuilder(Material.ARROW).setGlow(true).setName("&a◀ Trang Trước").build()
            : new ItemBuilder(Material.BARRIER).setName("&7Trang Trước").build());
        gui.setItem(49, new ItemBuilder(Material.PAPER).setName("&eTrang " + (page + 1) + "/" + totalPages)
            .setLore("", "&7Tổng: &e" + items.size() + " &7mặt hàng").build());
        gui.setItem(50, page < totalPages - 1 ? new ItemBuilder(Material.ARROW).setGlow(true).setName("&aTrang Tiếp ▶").build()
            : new ItemBuilder(Material.BARRIER).setName("&7Trang Tiếp").build());
        gui.setItem(53, new ItemBuilder(Material.BARRIER).setName("&cĐóng").build());
        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    private ItemStack createMenuItem(AdminItemDef def) {
        String amountStr = def.stack64 ? "&8[x64]" : "&8[x1]";
        ItemBuilder builder = new ItemBuilder(def.material).setName(def.displayName)
                .setLore("", def.lore, "", "&eClick để thêm vào kho đồ!", amountStr);
        if (def.stack64) builder.setGlow(true);
        return builder.build();
    }

    // ==================== CLICK HANDLERS ====================
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!isOwnInventory(event)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        if (event.getRawSlot() < 0) { event.setCancelled(true); event.setResult(Event.Result.DENY); return; }
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        int slot = event.getRawSlot();
        if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (slot >= 45 && !title.contains(TITLE_ADMIN_MAIN)) { handleNavigation(player, title, slot, clicked); return; }
        if (title.contains(TITLE_ADMIN_MAIN)) handleAdminMainClick(player, slot);
        else if (title.contains(TITLE_PILLS)) handleSubItemClick(player, ALL_PILLS, clicked);
        else if (title.contains(TITLE_ARTIFACTS)) handleSubItemClick(player, ALL_ARTIFACTS, clicked);
        else if (title.contains(TITLE_SKILLS)) handleSkillClick(player, clicked, slot);
        else if (title.contains(TITLE_HERBS)) handleSubItemClick(player, HERBS, clicked);
        else if (title.contains(TITLE_MATERIALS)) handleSubItemClick(player, MATERIALS, clicked);
        else if (title.contains(TITLE_MOUNTS)) handleSubItemClick(player, MOUNT_KEYS, clicked);
        else if (title.contains(TITLE_CURRENCY)) handleSubItemClick(player, CURRENCY_ITEMS, clicked);
    }

    private void handleNavigation(Player player, String title, int slot, ItemStack clicked) {
        String stripped = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");
        if (stripped.contains("Quay Lai")) { open(player); return; }
        if (stripped.contains("Đóng")) { player.closeInventory(); return; }
        int[] state = pageState.get(player.getUniqueId());
        if (state == null) return;
        // "Trang Trước" and "Trang Tiếp" are unique patterns - paper only has "Trang X/Y"
        if (stripped.contains("Trang Trước") && state[0] > 0) { state[0]--; reopenCurrentPage(player, title); return; }
        if (stripped.contains("Trang Tiếp")) { state[0]++; reopenCurrentPage(player, title); return; }
    }

    private void reopenCurrentPage(Player player, String title) {
        if (title.contains(TITLE_PILLS)) openPaginatedMenu(player, TITLE_PILLS, ALL_PILLS);
        else if (title.contains(TITLE_ARTIFACTS)) openPaginatedMenu(player, TITLE_ARTIFACTS, ALL_ARTIFACTS);
        else if (title.contains(TITLE_SKILLS)) openPaginatedMenu(player, TITLE_SKILLS, SKILL_BOOKS);
        else if (title.contains(TITLE_HERBS)) openPaginatedMenu(player, TITLE_HERBS, HERBS);
        else if (title.contains(TITLE_MATERIALS)) openPaginatedMenu(player, TITLE_MATERIALS, MATERIALS);
        else if (title.contains(TITLE_MOUNTS)) openPaginatedMenu(player, TITLE_MOUNTS, MOUNT_KEYS);
        else if (title.contains(TITLE_CURRENCY)) openPaginatedMenu(player, TITLE_CURRENCY, CURRENCY_ITEMS);
    }

    private void handleAdminMainClick(Player player, int slot) {
        switch (slot) {
            case 10: pageState.put(player.getUniqueId(), new int[]{0}); openPaginatedMenu(player, TITLE_PILLS, ALL_PILLS); break;
            case 11: pageState.put(player.getUniqueId(), new int[]{0}); openPaginatedMenu(player, TITLE_ARTIFACTS, ALL_ARTIFACTS); break;
            case 12: pageState.put(player.getUniqueId(), new int[]{0}); openPaginatedMenu(player, TITLE_SKILLS, SKILL_BOOKS); break;
            case 13: pageState.put(player.getUniqueId(), new int[]{0}); openPaginatedMenu(player, TITLE_HERBS, HERBS); break;
            case 14: pageState.put(player.getUniqueId(), new int[]{0}); openPaginatedMenu(player, TITLE_MATERIALS, MATERIALS); break;
            case 15: pageState.put(player.getUniqueId(), new int[]{0}); openPaginatedMenu(player, TITLE_MOUNTS, MOUNT_KEYS); break;
            case 16: pageState.put(player.getUniqueId(), new int[]{0}); openPaginatedMenu(player, TITLE_CURRENCY, CURRENCY_ITEMS); break;
            case 26: player.closeInventory(); break;
        }
    }

    private void handleSubItemClick(Player player, List<AdminItemDef> items, ItemStack clicked) {
        String stripped = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");
        if (stripped.contains("Quay Lai")) { open(player); return; }
        AdminItemDef matched = findItemInList(items, clicked);
        if (matched == null) return;
        giveItemToPlayer(player, matched);
    }

    private void handleSkillClick(Player player, ItemStack clicked, int slot) {
        if (slot >= 45) { open(player); return; }
        String stripped = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");
        if (stripped.contains("Quay Lai")) { open(player); return; }
        AdminItemDef matched = findItemInList(SKILL_BOOKS, clicked);
        if (matched == null) return;
        String skillId = SKILL_ID_MAP.get(stripped);
        if (skillId == null) { MessageUtils.send(player, "&cKhông tìm thấy skill ID!"); return; }
        if (plugin.getSkillBookManager() != null) {
            String grade = "HOANG"; String subGrade = "HA";
            if (stripped.contains("Huyền")) grade = "HUYEN";
            else if (stripped.contains("Địa")) grade = "DIA";
            else if (stripped.contains("Thiên")) grade = "THIEN";
            if (stripped.contains("Trung")) subGrade = "TRUNG";
            else if (stripped.contains("Thượng")) subGrade = "THUONG";
            ItemStack book = plugin.getSkillBookManager().createSkillBook(skillId, grade, subGrade);
            if (book != null) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(book);
                for (ItemStack drop : leftover.values()) player.getWorld().dropItemNaturally(player.getLocation(), drop);
                MessageUtils.send(player, "&aĐã nhận sách công pháp: &f" + matched.displayName);
                MessageUtils.playSound(player, Sound.ENTITY_ITEM_PICKUP);
            } else { MessageUtils.send(player, "&cKhông thể tạo sách công pháp!"); }
        }
    }

    private void giveItemToPlayer(Player player, AdminItemDef matched) {
        int amount = matched.stack64 ? 64 : 1;
        ItemBuilder giveBuilder = new ItemBuilder(matched.material).setName(matched.displayName)
                .setLore("", matched.lore).setGlow(true).setAmount(amount);
        giveBuilder.setPersistentData("vnmine_item", "true");
        ItemStack giveItem = giveBuilder.build();
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(giveItem);
        for (ItemStack drop : leftover.values()) player.getWorld().dropItemNaturally(player.getLocation(), drop);
        MessageUtils.send(player, "&aĐã nhận &f" + matched.displayName + " &r&a(" + (matched.stack64 ? "x64" : "x1") + ")!");
        MessageUtils.playSound(player, Sound.ENTITY_ITEM_PICKUP);
    }

    private AdminItemDef findItemInList(List<AdminItemDef> items, ItemStack clicked) {
        Material type = clicked.getType();
        String stripped = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");
        for (AdminItemDef def : items) {
            if (def.material == type && stripColor(def.displayName).equals(stripped)) return def;
        }
        return null;
    }

    private static String stripColor(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }

    private static String stripColorRaw(String input) {
        if (input == null) return "";
        return input.replaceAll("&[0-9a-fk-or]", "").trim();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) { }
}