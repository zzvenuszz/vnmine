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
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AdminMenuGUI implements Listener {

    private final VNMinePlugin plugin;

    private static final String TITLE_ADMIN_MAIN = "✦ VNMine Admin Menu ✦";
    private static final String TITLE_PILLS = "✦ Admin - Dan Duoc ✦";
    private static final String TITLE_ARTIFACTS = "✦ Admin - Phap Bao ✦";
    private static final String TITLE_SKILLS = "✦ Admin - Cong Phap ✦";
    private static final String TITLE_HERBS = "✦ Admin - Linh Thao ✦";
    private static final String TITLE_MATERIALS = "✦ Admin - Nguyen Lieu ✦";
    private static final String TITLE_MOUNTS = "✦ Admin - Toa Ky ✦";
    private static final String TITLE_CURRENCY = "✦ Admin - Linh Thach ✦";

    private static final List<String> ADMIN_TITLES = Arrays.asList(
        TITLE_ADMIN_MAIN, TITLE_PILLS, TITLE_ARTIFACTS, TITLE_SKILLS,
        TITLE_HERBS, TITLE_MATERIALS, TITLE_MOUNTS, TITLE_CURRENCY
    );

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

    private static final List<AdminItemDef> PILLS = Arrays.asList(
        new AdminItemDef("&aHồi Linh Đan", Material.GLOWSTONE_DUST, "&7Hồi phục &b30 &7linh lực", true),
        new AdminItemDef("&bĐại Hồi Linh Đan", Material.GLOWSTONE, "&7Hồi phục &b100 &7linh lực + hồi phục 30s", true),
        new AdminItemDef("&cCương Thể Đan", Material.REDSTONE_BLOCK, "&7Tăng &c20% sát thương &7trong 60 giây", true),
        new AdminItemDef("&aThanh Tâm Đan", Material.SUGAR, "&7Giải trừ mọi trạng thái xấu", false),
        new AdminItemDef("&bTốc Thánh Đan", Material.FEATHER, "&7Tăng &b50% tốc độ &7trong 30 giây", true),
        new AdminItemDef("&5Tu Luyện Đan", Material.PURPLE_DYE, "&7Tăng &5+50 EXP &7tu luyện", true),
        new AdminItemDef("&6◆ Phi Thăng Đan ◆", Material.NETHER_STAR, "&7+500 EXP (1 lần/đại cảnh giới)", true),
        new AdminItemDef("&9Bách Độc Đan", Material.CYAN_DYE, "&7Miễn nhiễm độc 5 phút", false),
        new AdminItemDef("&6Thiên Hồi Đan", Material.GOLDEN_APPLE, "&7Hồi 50% HP + 50% linh lực", false),
        new AdminItemDef("&8Phê Ma Đan", Material.REDSTONE, "&7Tăng 30% sát thương vs quái 2 phút", false),
        new AdminItemDef("&6Trường Thọ Đan", Material.NETHER_STAR, "&7Hồi sinh 1 lần (CD 1h)", false)
    );

    private static final List<AdminItemDef> ARTIFACTS = Arrays.asList(
        new AdminItemDef("&b◆ Kiếm Phi Hành ◆", Material.DIAMOND_SWORD, "&7Click phải để ngự kiếm phi hành", false),
        new AdminItemDef("&6◆ Linh Chung ◆", Material.BELL, "&7Làm choáng quái AOE", false),
        new AdminItemDef("&5◆ Bát Quái Kính ◆", Material.SHIELD, "&7Giảm 30% sát thương nhận vào", false),
        new AdminItemDef("&a◆ Hồn Ngọc ◆", Material.EMERALD, "&7Tự hồi 50% máu khi HP<20%", false),
        new AdminItemDef("&4◆ Thiên Linh Thuẫn ◆", Material.NETHERITE_CHESTPLATE, "&7Bất tử 5s, CD 3 phút", false),
        new AdminItemDef("&e◆ Lôi Ấn ◆", Material.TRIDENT, "&7Gọi sét đánh quái", false),
        new AdminItemDef("&6◆ Phượng Hoàng Lệnh ◆", Material.FEATHER, "&7Hồi sinh 1 lần (CD 60p)", false)
    );

    private static final List<AdminItemDef> HERBS = Arrays.asList(
        new AdminItemDef("&aLinh Thảo", Material.GREEN_DYE, "&7Nguyên liệu cơ bản - Kiếm từ quái yếu, Linh Điền", true),
        new AdminItemDef("&cHuyết Linh Thảo", Material.RED_DYE, "&7Nguyên liệu trung cấp - Kiếm từ quái trung cấp, NPC", true),
        new AdminItemDef("&6Long Huyết Thảo", Material.ORANGE_DYE, "&7Nguyên liệu cao cấp - Kiếm từ Elite Mob", true),
        new AdminItemDef("&bThiên Linh Thảo", Material.CYAN_DYE, "&7Linh thảo thiên thượng - Kiếm từ Boss, rương cổ đại", true),
        new AdminItemDef("&dBình Linh Thảo", Material.LIGHT_BLUE_DYE, "&7Tinh hoa giải độc - Kiếm từ quái vùng tuyết", true),
        new AdminItemDef("&eLôi Linh Thảo", Material.YELLOW_DYE, "&7Hấp thu lôi khí - Kiếm từ quái lightning", true),
        new AdminItemDef("&dHoa Linh Thảo", Material.MAGENTA_DYE, "&7Duy hỏa - Kiếm từ Blaze, Nether", true),
        new AdminItemDef("&5Vạn Niên Linh Chi", Material.PURPLE_DYE, "&7Tăng 100 năm tu vi - Boss hiếm, rương cổ đại", false)
    );

    private static final List<AdminItemDef> MATERIALS = Arrays.asList(
        new AdminItemDef("&fNước Tinh Khiết", Material.POTION, "&7Nước tinh khiết dùng luyện đan", false),
        new AdminItemDef("&cBột Blaze", Material.BLAZE_POWDER, "&7Tinh hoa lửa - Blaze, Nether", true),
        new AdminItemDef("&6Vàng", Material.GOLD_INGOT, "&7Kim loại quý", true),
        new AdminItemDef("&bKim Cương", Material.DIAMOND, "&7Kim cương thô", true),
        new AdminItemDef("&5Ngọc Lục Bảo", Material.EMERALD, "&7Ngọc quý", true),
        new AdminItemDef("&8Obsidian", Material.OBSIDIAN, "&7Vật liệu phong hỏa", true),
        new AdminItemDef("&7Mắt End", Material.ENDER_PEARL, "&7Vật liệu không gian", true),
        new AdminItemDef("&5Netherite", Material.NETHERITE_INGOT, "&7Vật liệu tối thượng", false),
        new AdminItemDef("&dHơi Rồng", Material.DRAGON_BREATH, "&7Hơi thở của rồng", false),
        new AdminItemDef("&6Khối Vàng", Material.GOLD_BLOCK, "&7Khối vàng nguyên chất", true),
        new AdminItemDef("&bMắt Ender", Material.ENDER_EYE, "&7Mắt ender đã kích hoạt", true),
        new AdminItemDef("&7Lông Vũ", Material.FEATHER, "&7Lông vũ phượng hoàng", true),
        new AdminItemDef("&fNgân Sa", Material.IRON_NUGGET, "&7Giảm thời gian nấu chảy - Mỏ sâu, quái ngầm", true),
        new AdminItemDef("&cHuyết Thạch", Material.REDSTONE, "&7Tăng phẩm đan dược - Quái quý, Nether", true),
        new AdminItemDef("&bLong Lân", Material.PRISMARINE_SHARD, "&7Vật liệu pháp khí - Rồng nước, Ocean Monument", true),
        new AdminItemDef("&fThiên Thạch", Material.END_STONE, "&7Nguyên liệu thiên ngoại - End City, Enderman hiếm", true),
        new AdminItemDef("&8Huyền Kim", Material.IRON_INGOT, "&7Kim loại huyền bí - Ancient Debris, Deep Dark", true)
    );

    private static final List<AdminItemDef> MOUNT_KEYS = Arrays.asList(
        new AdminItemDef("&6Phượng Hoàng Lệnh Bài", Material.FEATHER, "&7Key triệu hồi Phượng Hoàng tọa kỵ", false),
        new AdminItemDef("&fBạch Hổ Lệnh Bài", Material.BONE, "&7Key triệu hồi Bạch Hổ tọa kỵ", false),
        new AdminItemDef("&aThanh Long Lệnh Bài", Material.DRAGON_BREATH, "&7Key triệu hồi Thanh Long tọa kỵ", false)
    );

    // ALL skill books: existing + FIRE_CONTROL (12 grades) + FORGE_MASTERY (12 grades)
    private static final List<AdminItemDef> SKILL_BOOKS = new ArrayList<>();
    private static final Map<String, String> SKILL_ID_MAP = new HashMap<>();
    static {
        // Existing skill books
        SKILL_BOOKS.add(new AdminItemDef("&aTruc Co Lieu Thuong (Hoang Ha)", Material.ENCHANTED_BOOK, "&7Skill: BASIC_HEAL - Hoi 10 HP", false));
        SKILL_BOOKS.add(new AdminItemDef("&bLinh Khi Ho The (Hoang Trung)", Material.ENCHANTED_BOOK, "&7Skill: QI_SHIELD - Hop thu 20 sat thuong", false));
        SKILL_BOOKS.add(new AdminItemDef("&cHoa Cau Thuat (Hoang Ha)", Material.ENCHANTED_BOOK, "&7Skill: FIRE_BALL - Gay 15 sat thuong", false));
        SKILL_BOOKS.add(new AdminItemDef("&eThien Loi Dan (Huyen Ha)", Material.ENCHANTED_BOOK, "&7Skill: LIGHTNING_STRIKE - Gay 25 sat thuong", false));
        SKILL_BOOKS.add(new AdminItemDef("&bPhi Van Bo (Hoang Ha)", Material.ENCHANTED_BOOK, "&7Skill: SPEED_STEP - Tang 40% toc do 15s", false));
        SKILL_BOOKS.add(new AdminItemDef("&dThuan Di (Huyen Ha)", Material.ENCHANTED_BOOK, "&7Skill: TELEPORT - Dich chuyen tam nhin", false));
        SKILL_BOOKS.add(new AdminItemDef("&fPhong Nhan (Hoang Trung)", Material.ENCHANTED_BOOK, "&7Skill: WIND_BLADE - Gay 12 sat thuong xuyen thau", false));
        SKILL_BOOKS.add(new AdminItemDef("&6Tinh Than Bao (Thien Ha)", Material.ENCHANTED_BOOK, "&7Skill: METEOR_STORM - Gay 35 sat thuong AOE", false));
        SKILL_ID_MAP.put("Truc Co Lieu Thuong (Hoang Ha)", "BASIC_HEAL");
        SKILL_ID_MAP.put("Linh Khi Ho The (Hoang Trung)", "QI_SHIELD");
        SKILL_ID_MAP.put("Hoa Cau Thuat (Hoang Ha)", "FIRE_BALL");
        SKILL_ID_MAP.put("Thien Loi Dan (Huyen Ha)", "LIGHTNING_STRIKE");
        SKILL_ID_MAP.put("Phi Van Bo (Hoang Ha)", "SPEED_STEP");
        SKILL_ID_MAP.put("Thuan Di (Huyen Ha)", "TELEPORT");
        SKILL_ID_MAP.put("Phong Nhan (Hoang Trung)", "WIND_BLADE");
        SKILL_ID_MAP.put("Tinh Than Bao (Thien Ha)", "METEOR_STORM");

        // FIRE_CONTROL - 12 books (4 grades x 3 sub-grades)
        String[] grades = {"Hoang", "Huyen", "Dia", "Thien"};
        String[] gradeIds = {"HOANG", "HUYEN", "DIA", "THIEN"};
        String[] subGrades = {"Ha", "Trung", "Thuong"};
        String[] subGradeIds = {"HA", "TRUNG", "THUONG"};

        for (int g = 0; g < grades.length; g++) {
            for (int s = 0; s < subGrades.length; s++) {
                String name = "Khong Hoa Thuat (" + grades[g] + " " + subGrades[s] + ")";
                SKILL_BOOKS.add(new AdminItemDef("&c" + name, Material.ENCHANTED_BOOK,
                    "&7Skill: FIRE_CONTROL - Kiem soat hoa hau luyen dan", false));
                SKILL_ID_MAP.put(name, "FIRE_CONTROL");
            }
        }

        // FORGE_MASTERY - 12 books
        for (int g = 0; g < grades.length; g++) {
            for (int s = 0; s < subGrades.length; s++) {
                String name = "Luyen Ki Thuat (" + grades[g] + " " + subGrades[s] + ")";
                SKILL_BOOKS.add(new AdminItemDef("&6" + name, Material.ENCHANTED_BOOK,
                    "&7Skill: FORGE_MASTERY - Thuat luyen khi dinh cao", false));
                SKILL_ID_MAP.put(name, "FORGE_MASTERY");
            }
        }
    }

    // Currency items for admin
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

        gui.setItem(10, new ItemBuilder(Material.NETHER_STAR).setGlow(true).setName("&aTu Luyen (Dan Duoc)")
                .setLore("", "&7Cac loai dan duoc tu luyen", "&7Click de mo danh sach").build());
        gui.setItem(11, new ItemBuilder(Material.DIAMOND_SWORD).setGlow(true).setName("&6Phap Bao")
                .setLore("", "&7Cac loai phap bao than ky", "&7Click de mo danh sach").build());
        gui.setItem(12, new ItemBuilder(Material.ENCHANTED_BOOK).setGlow(true).setName("&dCong Phap (Skill Books)")
                .setLore("", "&7Sach ky nang cac loai", "&7Click de mo danh sach").build());
        gui.setItem(13, new ItemBuilder(Material.GREEN_DYE).setGlow(true).setName("&2Linh Thao")
                .setLore("", "&7Nguyen lieu luyen dan", "&7Click de mo danh sach").build());
        gui.setItem(14, new ItemBuilder(Material.GOLD_INGOT).setGlow(true).setName("&eNguyen Lieu")
                .setLore("", "&7Nguyen lieu che tao phap bao", "&7Click de mo danh sach").build());
        gui.setItem(15, new ItemBuilder(Material.SADDLE).setGlow(true).setName("&6Toa Ky (Mount Keys)")
                .setLore("", "&7Key trieu hoi toa ky", "&7Click de mo danh sach").build());
        gui.setItem(16, new ItemBuilder(Material.PRISMARINE_SHARD).setGlow(true).setName("&bLinh Thach")
                .setLore("", "&7Tien te tu tien", "&7Click de mo danh sach").build());
        gui.setItem(26, new ItemBuilder(Material.BARRIER).setName("&cDong").build());

        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    private void openSubMenu(Player player, String titleSuffix, List<AdminItemDef> items) {
        int size = Math.max(54, ((items.size() / 9) + 1) * 9);
        if (size > 54) size = 54;
        Inventory gui = Bukkit.createInventory(null, size, ColorUtils.colorize("&8" + titleSuffix));
        int slot = 0;
        for (AdminItemDef item : items) { if (slot < size - 1) gui.setItem(slot++, createMenuItem(item)); }
        gui.setItem(size - 1, new ItemBuilder(Material.ARROW).setName("&e← Quay Lai").build());
        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    private ItemStack createMenuItem(AdminItemDef def) {
        String amountStr = def.stack64 ? "&8[x64]" : "&8[x1]";
        ItemBuilder builder = new ItemBuilder(def.material).setName(def.displayName)
                .setLore("", def.lore, "", "&eClick de them vao kho do!", amountStr);
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

        if (title.contains(TITLE_ADMIN_MAIN)) handleAdminMainClick(player, slot);
        else if (title.contains(TITLE_PILLS)) handleSubItemClick(player, PILLS, clicked);
        else if (title.contains(TITLE_ARTIFACTS)) handleSubItemClick(player, ARTIFACTS, clicked);
        else if (title.contains(TITLE_SKILLS)) handleSkillClick(player, clicked, slot);
        else if (title.contains(TITLE_HERBS)) handleSubItemClick(player, HERBS, clicked);
        else if (title.contains(TITLE_MATERIALS)) handleSubItemClick(player, MATERIALS, clicked);
        else if (title.contains(TITLE_MOUNTS)) handleMountClick(player, clicked, slot);
        else if (title.contains(TITLE_CURRENCY)) handleCurrencyClick(player, clicked);
    }

    private void handleAdminMainClick(Player player, int slot) {
        switch (slot) {
            case 10: openSubMenu(player, TITLE_PILLS, PILLS); break;
            case 11: openSubMenu(player, TITLE_ARTIFACTS, ARTIFACTS); break;
            case 12: openSubMenu(player, TITLE_SKILLS, SKILL_BOOKS); break;
            case 13: openSubMenu(player, TITLE_HERBS, HERBS); break;
            case 14: openSubMenu(player, TITLE_MATERIALS, MATERIALS); break;
            case 15: openSubMenu(player, TITLE_MOUNTS, MOUNT_KEYS); break;
            case 16: openSubMenu(player, TITLE_CURRENCY, CURRENCY_ITEMS); break;
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
        String stripped = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");
        if (stripped.contains("Quay Lai")) { open(player); return; }
        AdminItemDef matched = findItemInList(SKILL_BOOKS, clicked);
        if (matched == null) return;
        String skillId = SKILL_ID_MAP.get(stripped);
        if (skillId == null) { MessageUtils.send(player, "&cKhong tim thay skill ID!"); return; }

        if (plugin.getSkillBookManager() != null) {
            String grade = "HOANG"; String subGrade = "HA";
            if (stripped.contains("Huyen")) grade = "HUYEN";
            else if (stripped.contains("Dia")) grade = "DIA";
            else if (stripped.contains("Thien")) grade = "THIEN";
            if (stripped.contains("Trung")) subGrade = "TRUNG";
            else if (stripped.contains("Thuong")) subGrade = "THUONG";

            ItemStack book = plugin.getSkillBookManager().createSkillBook(skillId, grade, subGrade);
            if (book != null) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(book);
                for (ItemStack drop : leftover.values()) player.getWorld().dropItemNaturally(player.getLocation(), drop);
                MessageUtils.send(player, "&aDa nhan sach cong phap: &f" + matched.displayName);
                MessageUtils.playSound(player, Sound.ENTITY_ITEM_PICKUP);
            } else {
                MessageUtils.send(player, "&cKhong the tao sach cong phap!");
            }
        }
    }

    private void handleMountClick(Player player, ItemStack clicked, int slot) {
        String stripped = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");
        if (stripped.contains("Quay Lai")) { open(player); return; }
        AdminItemDef matched = findItemInList(MOUNT_KEYS, clicked);
        if (matched == null) return;
        String mountId = null;
        if (stripped.contains("Phuong Hoang")) mountId = "PHUONG_HOANG";
        else if (stripped.contains("Bach Ho")) mountId = "BACH_HO";
        else if (stripped.contains("Thanh Long")) mountId = "THANH_LONG";
        if (mountId != null && plugin.getMountManager() != null) {
            plugin.getMountManager().unlockMount(player, mountId);
            giveItemToPlayer(player, matched);
            MessageUtils.send(player, "&aDa mo khoa toa ky: " + matched.displayName);
        } else { giveItemToPlayer(player, matched); }
    }

    private void handleCurrencyClick(Player player, ItemStack clicked) {
        String stripped = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");
        if (stripped.contains("Quay Lai")) { open(player); return; }
        AdminItemDef matched = findItemInList(CURRENCY_ITEMS, clicked);
        if (matched == null) return;
        giveItemToPlayer(player, matched);
    }

    private void giveItemToPlayer(Player player, AdminItemDef matched) {
        int amount = matched.stack64 ? 64 : 1;
        ItemBuilder giveBuilder = new ItemBuilder(matched.material).setName(matched.displayName)
                .setLore("", matched.lore).setGlow(true).setAmount(amount);
        giveBuilder.setPersistentData("vnmine_item", "true");
        ItemStack giveItem = giveBuilder.build();
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(giveItem);
        for (ItemStack drop : leftover.values()) player.getWorld().dropItemNaturally(player.getLocation(), drop);
        String amountStr = matched.stack64 ? "x64" : "x1";
        MessageUtils.send(player, "&aDa nhan &f" + matched.displayName + " &r&a(" + amountStr + ")!");
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

    private String stripColor(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) { }
}