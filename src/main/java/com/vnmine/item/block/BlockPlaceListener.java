package com.vnmine.item.block;

import com.vnmine.item.ItemBuilder;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 * BlockPlaceListener - Chặn đặt block đặc biệt (item từ admin menu, /vngive, v.v.)
 * Các item đặc biệt có persistent data "vnmine_item" = "true" sẽ không được đặt ra world
 */
public class BlockPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        if (item == null || item.getType() == Material.AIR) return;

        // Kiểm tra nếu item có tag "vnmine_item" (item đặc biệt từ plugin)
        if (ItemBuilder.hasPersistentData(item, "vnmine_item")) {
            event.setCancelled(true);
            MessageUtils.send(player, "&c⚠ Bạn không thể đặt item đặc biệt này ra ngoài!");
            player.updateInventory();
        }
    }
}