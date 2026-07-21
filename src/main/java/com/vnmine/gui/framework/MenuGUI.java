package com.vnmine.gui.framework;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * MenuGUI - Abstract base class cho tất cả các GUI (Chest Menu) trong plugin.
 * 
 * Cung cấp sẵn:
 * - Event handling chuẩn hóa (click, drag, close)
 * - Title-based detection tự động
 * - Border fill với texture support (CustomModelData)
 * - Back button pattern
 * - Click type filtering (chặn shift-click, double-click, drop, number key...)
 * 
 * Texture support:
 * - Tất cả border items đều dùng MenuSlot.fromConfig() để load từ config.yml
 * - Khi có resource pack, chỉ cần sửa config là texture thay đổi
 * 
 * Cách dùng:
 * 1. extends MenuGUI
 * 2. Gọi super(plugin, title) trong constructor
 * 3. Implement handleClick()
 * 4. Gọi registerSlots() để đăng ký các title
 */
public abstract class MenuGUI implements Listener {

    protected final VNMinePlugin plugin;
    protected final List<String> ownedTitles;
    protected final String mainTitle;

    /**
     * @param plugin Plugin instance
     * @param mainTitle Title chính của GUI này (dùng để detect inventory)
     */
    public MenuGUI(VNMinePlugin plugin, String mainTitle) {
        this.plugin = plugin;
        this.mainTitle = mainTitle;
        this.ownedTitles = new ArrayList<>();
        registerTitles();
    }

    /**
     * Đăng ký các title mà GUI này quản lý.
     * Subclass ghi đè method này để thêm title phụ.
     */
    protected void registerTitles() {
        ownedTitles.add(mainTitle);
    }

    /**
     * Kiểm tra inventory hiện tại có thuộc GUI này không
     */
    protected boolean isOwnInventory(InventoryClickEvent event) {
        String title = ColorUtils.stripColor(event.getView().getTitle());
        for (String t : ownedTitles) {
            if (title.contains(t)) return true;
        }
        return false;
    }

    /**
     * Kiểm tra title có thuộc GUI này không (dùng cho drag/close)
     */
    protected boolean isOwnTitle(String title) {
        String stripped = ColorUtils.stripColor(title);
        for (String t : ownedTitles) {
            if (stripped.contains(t)) return true;
        }
        return false;
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Xử lý drag - chặn mọi drag trong GUI
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!isOwnTitle(event.getView().getTitle())) return;
        event.setCancelled(true);
    }

    /**
     * Xử lý click chuẩn hóa
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!isOwnInventory(event)) return;

        Player player = (Player) event.getWhoClicked();

        // Chặn mọi click type đặc biệt
        ClickType click = event.getClick();
        if (isSpecialClick(click)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Gọi handler của subclass
        handleClick(player, slot, clicked, event);
    }

    /**
     * Xử lý close - cleanup mặc định
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!isOwnTitle(event.getView().getTitle())) return;
        onClose((Player) event.getPlayer(), event);
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Xử lý click vào slot trong GUI.
     * Subclass implement method này để xử lý logic riêng.
     * 
     * @param player Người chơi click
     * @param slot Slot number (raw slot)
     * @param clicked ItemStack đã click
     * @param event Event gốc (có thể dùng để get cursor, v.v.)
     */
    protected abstract void handleClick(Player player, int slot, ItemStack clicked, InventoryClickEvent event);

    /**
     * Xử lý khi đóng inventory.
     * Subclass có thể ghi đè để cleanup session, trả item, v.v.
     */
    protected void onClose(Player player, InventoryCloseEvent event) {
        // Mặc định không làm gì
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Kiểm tra có phải click đặc biệt cần chặn không
     */
    protected boolean isSpecialClick(ClickType click) {
        return click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT ||
               click == ClickType.DOUBLE_CLICK || click == ClickType.DROP ||
               click == ClickType.CONTROL_DROP ||
               click == ClickType.NUMBER_KEY || click == ClickType.WINDOW_BORDER_LEFT ||
               click == ClickType.WINDOW_BORDER_RIGHT;
    }

    /**
     * Fill các slot trống với border item (có texture support)
     */
    protected void fillEmptySlots(Inventory inv) {
        ItemStack border = MenuSlot.createBorderItem(plugin);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, border);
            }
        }
    }

    /**
     * Fill các slot trống với border item, bỏ qua các slot được chỉ định
     */
    protected void fillEmptySlots(Inventory inv, Set<Integer> excludeSlots) {
        ItemStack border = MenuSlot.createBorderItem(plugin);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null && !excludeSlots.contains(i)) {
                inv.setItem(i, border);
            }
        }
    }

    /**
     * Fill các slot trống với border item, bỏ qua các slot trong mảng
     */
    protected void fillEmptySlots(Inventory inv, int[] excludeSlots) {
        Set<Integer> exclude = new HashSet<>();
        for (int s : excludeSlots) exclude.add(s);
        fillEmptySlots(inv, exclude);
    }

    /**
     * Tạo nút back (quay lại)
     */
    protected ItemStack createBackButton() {
        return new com.vnmine.item.ItemBuilder(Material.ARROW)
                .setName("&e&l← Quay Lại")
                .build();
    }

    /**
     * Tạo nút đóng
     */
    protected ItemStack createCloseButton() {
        return new com.vnmine.item.ItemBuilder(Material.BARRIER)
                .setName("&c&lĐóng")
                .build();
    }

    /**
     * Tạo nút với texture support
     */
    protected ItemStack createButton(Material material, String name, String... lore) {
        return new com.vnmine.item.ItemBuilder(material)
                .setName(name)
                .setLore(lore)
                .build();
    }

    /**
     * Tạo nút với glow và texture support
     */
    protected ItemStack createGlowButton(Material material, String name, String... lore) {
        return new com.vnmine.item.ItemBuilder(material)
                .setName(name)
                .setLore(lore)
                .setGlow(true)
                .build();
    }

    /**
     * Tạo nút với CustomModelData (cho resource pack)
     */
    protected ItemStack createTexturedButton(String configPath, Material defaultMaterial, String name, String... lore) {
        MenuSlot slot = MenuSlot.fromConfig(plugin, configPath, defaultMaterial, name);
        return slot.createItem(lore);
    }

    /**
     * Mở inventory cho player
     */
    protected void openInventory(Player player, Inventory inv) {
        player.openInventory(inv);
    }

    /**
     * Tạo inventory với kích thước và title
     */
    protected Inventory createInventory(int size, String title) {
        return Bukkit.createInventory(null, size, ColorUtils.colorize(title));
    }

    /**
     * Lấy plugin instance
     */
    public VNMinePlugin getPlugin() {
        return plugin;
    }

    /**
     * Lấy main title
     */
    public String getMainTitle() {
        return mainTitle;
    }
}