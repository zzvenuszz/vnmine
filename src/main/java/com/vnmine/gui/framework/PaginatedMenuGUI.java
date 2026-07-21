package com.vnmine.gui.framework;

import com.vnmine.VNMinePlugin;
import com.vnmine.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * PaginatedMenuGUI - Menu dạng phân trang (pagination).
 * 
 * Đặc điểm:
 * - Tự động chia items thành nhiều trang
 * - Navigation buttons (Trang trước / Trang tiếp)
 * - Item click handler
 * - Back button
 * 
 * Texture support:
 * - Navigation buttons dùng MenuSlot.fromConfig()
 * - Các item có thể có CustomModelData riêng
 * 
 * Cách dùng:
 * 1. extends PaginatedMenuGUI
 * 2. Implement createMenuItems() để cung cấp danh sách item
 * 3. Implement onItemClick() để xử lý khi click vào item
 */
public abstract class PaginatedMenuGUI extends MenuGUI {

    protected final Map<UUID, int[]> pageState = new HashMap<>();
    protected static final int ITEMS_PER_PAGE = 45;

    /**
     * Định nghĩa một item trong menu phân trang
     */
    public static class PaginatedMenuItem {
        public final String displayName;
        public final Material material;
        public final String lore;
        public final boolean stack64;
        public final int customModelData;

        public PaginatedMenuItem(String displayName, Material material, String lore, boolean stack64) {
            this(displayName, material, lore, stack64, 0);
        }

        public PaginatedMenuItem(String displayName, Material material, String lore, boolean stack64, int customModelData) {
            this.displayName = displayName;
            this.material = material;
            this.lore = lore;
            this.stack64 = stack64;
            this.customModelData = customModelData;
        }
    }

    public PaginatedMenuGUI(VNMinePlugin plugin, String mainTitle) {
        super(plugin, mainTitle);
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Tạo danh sách items hiển thị trong menu
     */
    protected abstract List<PaginatedMenuItem> createMenuItems();

    /**
     * Xử lý khi click vào một item trong menu
     */
    protected abstract void onItemClick(Player player, PaginatedMenuItem item, InventoryClickEvent event);

    /**
     * Xử lý khi click vào nút back
     */
    protected abstract void onBackClick(Player player);

    /**
     * Lấy title suffix cho menu (ví dụ: "Admin - Dan Duoc")
     */
    protected abstract String getMenuTitleSuffix();

    // ==================== MENU OPENING ====================

    /**
     * Mở menu phân trang
     */
    public void openPaginated(Player player) {
        int page = 0;
        int[] state = pageState.get(player.getUniqueId());
        if (state != null) page = state[0];

        List<PaginatedMenuItem> items = createMenuItems();
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory gui = createInventory(54, "&8" + getMenuTitleSuffix() + " &7[Trang " + (page + 1) + "/" + totalPages + "]");

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, items.size());
        for (int i = start; i < end; i++) {
            gui.setItem(i - start, createMenuItem(items.get(i)));
        }

        // Navigation bar (bottom row)
        ItemStack border = MenuSlot.createBorderItem(plugin);
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, border);
        }

        gui.setItem(45, createBackButton());
        gui.setItem(48, page > 0
                ? new ItemBuilder(Material.ARROW).setGlow(true).setName("&a◀ Trang Trước").build()
                : new ItemBuilder(Material.BARRIER).setName("&7Trang Trước").build());
        gui.setItem(49, new ItemBuilder(Material.PAPER)
                .setName("&eTrang " + (page + 1) + "/" + totalPages)
                .setLore("", "&7Tổng: &e" + items.size() + " &7mặt hàng")
                .build());
        gui.setItem(50, page < totalPages - 1
                ? new ItemBuilder(Material.ARROW).setGlow(true).setName("&aTrang Tiếp ▶").build()
                : new ItemBuilder(Material.BARRIER).setName("&7Trang Tiếp").build());
        gui.setItem(53, createCloseButton());

        player.openInventory(gui);
    }

    /**
     * Tạo ItemStack từ PaginatedMenuItem
     */
    protected ItemStack createMenuItem(PaginatedMenuItem def) {
        ItemBuilder builder = new ItemBuilder(def.material)
                .setName(def.displayName);

        if (def.customModelData > 0) {
            builder.setCustomModelData(def.customModelData);
        }

        if (def.lore != null && !def.lore.isEmpty()) {
            builder.setLore("", def.lore, "", "&aClick để thêm vào kho đồ!",
                    def.stack64 ? "&8[x64]" : "&8[x1]");
        }

        if (def.stack64) {
            builder.setGlow(true);
        }

        return builder.build();
    }

    /**
     * Tìm PaginatedMenuItem từ ItemStack
     */
    protected PaginatedMenuItem findItem(List<PaginatedMenuItem> items, ItemStack clicked) {
        Material type = clicked.getType();
        String stripped = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");
        for (PaginatedMenuItem def : items) {
            if (def.material == type && stripColor(def.displayName).equals(stripped)) {
                return def;
            }
        }
        return null;
    }

    // ==================== EVENT HANDLING ====================

    @Override
    protected void handleClick(Player player, int slot, ItemStack clicked, InventoryClickEvent event) {
        // Bottom navigation row (slot 45-53)
        if (slot >= 45 && slot < 54) {
            handleNavigationClick(player, slot, clicked);
            return;
        }

        // Item click
        List<PaginatedMenuItem> items = createMenuItems();
        PaginatedMenuItem item = findItem(items, clicked);
        if (item != null) {
            onItemClick(player, item, event);
        }
    }

    /**
     * Xử lý click vào navigation bar
     */
    protected void handleNavigationClick(Player player, int slot, ItemStack clicked) {
        String stripped = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");

        if (stripped.contains("Quay Lại") || stripped.contains("Quay Lai")) {
            int[] state = pageState.get(player.getUniqueId());
            if (state != null) {
                state[0] = 0; // Reset page
            }
            onBackClick(player);
            return;
        }

        if (stripped.contains("Đóng")) {
            player.closeInventory();
            return;
        }

        int[] state = pageState.get(player.getUniqueId());
        if (state == null) {
            state = new int[]{0};
            pageState.put(player.getUniqueId(), state);
        }

        if (stripped.contains("Trang Trước") && state[0] > 0) {
            state[0]--;
            openPaginated(player);
        } else if (stripped.contains("Trang Tiếp")) {
            state[0]++;
            openPaginated(player);
        }
    }

    // ==================== UTILITY ====================

    /**
     * Strip color từ string
     */
    protected String stripColor(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }

}