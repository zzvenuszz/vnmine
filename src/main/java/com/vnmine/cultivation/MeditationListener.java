package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class MeditationListener implements Listener {
    private final VNMinePlugin plugin;
    private final MeditationManager meditationManager;

    public MeditationListener(VNMinePlugin plugin, MeditationManager meditationManager) {
        this.plugin = plugin;
        this.meditationManager = meditationManager;
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking()) return;
        var section = plugin.getConfig().getConfigurationSection("cultivation.meditation");
        if (section != null && !section.getBoolean("enabled", true)) return;
        meditationManager.handleSneakChange(player, true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String guiTitle = meditationManager.getMeditationConfig().getActivationGuiTitle();
        String actualTitle = event.getView().getTitle();
        
        if (guiTitle == null || actualTitle == null) return;
        
        if (!actualTitle.equals(guiTitle)) {
            return;
        }
        
        event.setCancelled(true);
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            plugin.getLogger().info("[MenuDebug] Click in meditation menu at slot " + event.getRawSlot() + " but item is null/AIR");
            return;
        }
        
        int slot = event.getRawSlot();
        plugin.getLogger().info("[MenuDebug] Meditation menu click: slot=" + slot + " item=" + event.getCurrentItem().getType() + " title='" + actualTitle + "'");
        
        if (slot == 2) {
            plugin.getLogger().info("[DEBUG Meditation] CONFIRM clicked by " + player.getName());
            meditationManager.confirmMeditation(player);
            player.closeInventory();
        } else if (slot == 6) {
            plugin.getLogger().info("[DEBUG Meditation] CANCEL clicked by " + player.getName());
            meditationManager.cancelMeditation(player);
            player.closeInventory();
        }
    }
}
