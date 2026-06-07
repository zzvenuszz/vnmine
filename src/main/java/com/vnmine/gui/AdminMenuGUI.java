package com.vnmine.gui;

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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * AdminMenuGUI - Menu admin dành cho OP, click vào item để nhận
 * Đan dược (consumable) → 64 cái
 * Pháp bảo / nguyên liệu → 1 cái
 */
public class AdminMenuGUI implements Listener {

    private final Map<UUID, String> openMenus = new HashMap<>();

    // ==================== ĐỊNH NGHĨA ITEM ====================

    // Đan dược (click nhận 64)
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

    // Pháp bảo (click nhận 1)
    private static final List<AdminItemDef> ARTIFACTS = Arrays.asList(
        new AdminItemDef("&b&l◆ Kiếm Phi Hành ◆", Material.DIAMOND_SWORD,
                "&7Click phải ngự kiếm phi hành", false),
        new AdminItemDef("&6&l◆ Linh Chung ◆", Material.BELL,
                "&7Làm choáng quái trong bán kính", false),
        new AdminItemDef("&5&l◆ Bát Quái Kính ◆", Material.SHIELD,
                "&7Giảm 30% sát thương nhận vào", false),
        new AdminItemDef("&a&l◆ Hồn Ngọc ◆", Material.EMERALD,
                "&7Tự hồi 50% máu khi HP<20%", false),
        new AdminItemDef("&4&l◆ Thiên Linh Thuẫn ◆", Material.NETHERITE_CHESTPLATE,
                "&7Bất tử 5s, CD 3 phút", false),
        new AdminItemDef("&e&l◆ Lôi Ấn ◆", Material.TRIDENT,
                "&7Gọi sét đánh quái", false),
        new AdminItemDef("&6&l◆ Phượng Hoàng Lệnh ◆", Material.FEATHER,
                "&7Hồi sinh 1 lần sau khi chết", false)
    );

    // Nguyên liệu (click nhận 64)
    private static final List<AdminItemDef> MATERIALS = Arrays.asList(
        new AdminItemDef("&aLinh Thảo", Material.GREEN_DYE,
                "&7Nguyên liệu cơ bản", true),
        new AdminItemDef("&cHuyết Linh Thảo", Material.RED_DYE,
                "&7Nguyên liệu trung cấp", true),
        new AdminItemDef("&6Long Huyết Thảo", Material.ORANGE_DYE,
                "&7Nguyên liệu cao cấp", true),
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

    /**
     * Mở menu admin cho player (chỉ OP)
     */
    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54,
                ColorUtils.colorize("&8✦ VNMine Admin - Lấy Item ✦"));

        // === Hàng 0: Đan Dược (slot 0-8) ===
        gui.setItem(9, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE)
                .setName("&9&l✦ ĐAN DƯỢC ✦")
                .setLore("&7Click để lấy 64 cái")
                .build());

        int slot = 10;
        for (AdminItemDef pill : PILLS) {
            if (slot > 16) break;
            gui.setItem(slot++, createMenuItem(pill));
        }

        // === Hàng 2: Pháp Bảo (slot 18-26) ===
        gui.setItem(18, new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE)
                .setName("&6&l✦ PHÁP BẢO ✦")
                .setLore("&7Click để lấy 1 cái")
                .build());

        slot = 19;
        for (AdminItemDef arti : ARTIFACTS) {
            if (slot > 25) break;
            gui.setItem(slot++, createMenuItem(arti));
        }

        // === Hàng 3-5: Nguyên Liệu (slot 27-53) ===
        gui.setItem(27, new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE)
                .setName("&e&l✦ NGUYÊN LIỆU ✦")
                .setLore("&7Click để lấy 64 cái")
                .build());

        slot = 28;
        for (AdminItemDef mat : MATERIALS) {
            if (slot > 52) break;
            gui.setItem(slot++, createMenuItem(mat));
        }

        openMenus.put(player.getUniqueId(), "admin");
        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    /**
     * Tạo ItemStack cho một AdminItemDef
     */
    private ItemStack createMenuItem(AdminItemDef def) {
        ItemBuilder builder = new ItemBuilder(def.material)
                .setName(def.displayName + " &8[" + (def.stack64 ? "x64" : "x1") + "]")
                .setLore(
                        "",
                        def.lore,
                        "",
                        "&eClick để thêm vào kho đồ!"
                );
        if (def.stack64) {
            builder.setGlow(true);
        }
        return builder.build();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String currentMenu = openMenus.get(player.getUniqueId());
        if (currentMenu == null) return;

        int slot = event.getRawSlot();
        if (slot >= 0 && slot < 54) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            // Tìm item trong danh sách
            AdminItemDef matched = findItem(clicked);
            if (matched == null) return;

            // Thêm item vào inventory
            int amount = matched.stack64 ? 64 : 1;
            ItemStack giveItem = new ItemBuilder(matched.material)
                    .setName(matched.displayName)
                    .setLore("", matched.lore)
                    .setGlow(true)
                    .setAmount(amount)
                    .build();

            // Thêm vào kho, nếu đầy thì drop
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(giveItem);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }

            String amountStr = matched.stack64 ? "&ex64" : "&ex1";
            MessageUtils.send(player, "&a✦ Đã nhận &f" + matched.displayName + " &r&a(" + amountStr + "&a)!");
            MessageUtils.playSound(player, Sound.ENTITY_ITEM_PICKUP);
        }
    }

    /**
     * Tìm AdminItemDef tương ứng với ItemStack đã click
     */
    private AdminItemDef findItem(ItemStack clicked) {
        Material type = clicked.getType();
        // Xóa màu để so sánh tên
        String stripped = stripColor(clicked.getItemMeta() != null
                ? clicked.getItemMeta().getDisplayName() : "");

        // Tìm trong pills
        for (AdminItemDef def : PILLS) {
            if (def.material == type && stripColor(def.displayName).equals(stripped)) {
                return def;
            }
        }
        // Tìm trong artifacts
        for (AdminItemDef def : ARTIFACTS) {
            if (def.material == type && stripColor(def.displayName).equals(stripped)) {
                return def;
            }
        }
        // Tìm trong materials
        for (AdminItemDef def : MATERIALS) {
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

    /**
     * Lưu định nghĩa một item trong menu admin
     */
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

    public void cleanupPlayer(UUID uuid) {
        openMenus.remove(uuid);
    }
}