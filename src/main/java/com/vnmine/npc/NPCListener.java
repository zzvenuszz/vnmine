package com.vnmine.npc;

import com.vnmine.VNMinePlugin;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * NPCListener - Click NPC → mở shop
 */
public class NPCListener implements Listener {

    private final VNMinePlugin plugin;
    private final NPCManager npcManager;
    private final NPCShopGUI npcShopGUI;

    public NPCListener(VNMinePlugin plugin, NPCManager npcManager, NPCShopGUI npcShopGUI) {
        this.plugin = plugin;
        this.npcManager = npcManager;
        this.npcShopGUI = npcShopGUI;
    }

    @EventHandler
    public void onEntityClick(PlayerInteractEntityEvent event) {
        if (!npcManager.isEnabled()) return;

        // Kiểm tra có phải NPC của plugin không
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            
            // Kiểm tra metadata
            if (villager.hasMetadata("vnmine_npc_id")) {
                event.setCancelled(true); // Không mở giao dịch mặc định
                
                // Lấy instance ID từ metadata
                String instanceId = villager.getMetadata("vnmine_npc_id").get(0).asString();
                
                npcShopGUI.openShop(event.getPlayer(), instanceId);
            }
        }
    }
}