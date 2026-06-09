package com.vnmine.gui;

import com.vnmine.VNMinePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * QuickMenuListener - Bắt phím F (swap hand) để mở menu chính VNMine
 * Cancel hành động đổi tay, thay bằng mở menu /vn
 */
public class QuickMenuListener implements Listener {

    private final VNMinePlugin plugin;

    public QuickMenuListener(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        // Cancel hành động đổi tay (phím F)
        event.setCancelled(true);

        Player player = event.getPlayer();

        // Mở menu chính VNMine
        MainMenuGUI mainMenu = plugin.getMainMenuGUI();
        if (mainMenu != null) {
            mainMenu.openMainMenu(player);
        }
    }
}