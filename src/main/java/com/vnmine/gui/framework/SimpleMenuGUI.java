package com.vnmine.gui.framework;

import com.vnmine.VNMinePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * SimpleMenuGUI - Menu dạng click để mở menu phụ hoặc gọi hành động.
 * 
 * Đặc điểm:
 * - Không có input/output của player
 * - Click vào item → mở menu khác / chạy command / gọi action
 * - Có back button tự động quay về parent menu
 * 
 * Cách dùng:
 * 1. extends SimpleMenuGUI
 * 2. Implement handleMenuClick() để xử lý click
 * 3. Dùng openInventory() để mở menu
 * 
 * Ví dụ: MainMenuGUI, AdminMenuGUI
 */
public abstract class SimpleMenuGUI extends MenuGUI {

    /**
     * @param plugin Plugin instance
     * @param mainTitle Title chính của GUI
     */
    public SimpleMenuGUI(VNMinePlugin plugin, String mainTitle) {
        super(plugin, mainTitle);
    }

    /**
     * Xử lý click vào slot trong menu.
     * Subclass implement method này.
     * 
     * @param player Người chơi click
     * @param slot Slot number
     * @param clicked Item đã click
     */
    protected abstract void handleMenuClick(Player player, int slot, ItemStack clicked);

    /**
     * Implementation của handleClick từ MenuGUI.
     * Chuyển tiếp sang handleMenuClick đơn giản hơn.
     */
    @Override
    protected final void handleClick(Player player, int slot, ItemStack clicked, InventoryClickEvent event) {
        handleMenuClick(player, slot, clicked);
    }
}