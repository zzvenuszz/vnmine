package com.vnmine.item.artifacts.abilities;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * MountItemListener - Xử lý click phải vào Mount Key để unlock mount
 */
public class MountItemListener implements Listener {

    private final VNMinePlugin plugin;

    public MountItemListener(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;
        if (!item.hasItemMeta()) return;

        // Check NBT persistent data for mount key
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        // Kiểm tra key "vnmine_mount_key"
        String mountId = container.get(
                new org.bukkit.NamespacedKey(plugin, "vnmine_mount_key"),
                PersistentDataType.STRING);
        
        if (mountId == null || mountId.isEmpty()) return;

        event.setCancelled(true);

        // Kiểm tra đã unlock chưa
        if (plugin.getMountManager().hasUnlockedMount(player, mountId)) {
            MessageUtils.send(player, "&cBạn đã mở khóa tọa kỵ này rồi!");
            return;
        }

        // Unlock mount
        plugin.getMountManager().unlockMount(player, mountId);
        MessageUtils.send(player, "&6✦ Bạn đã học được cách triệu hồi tọa kỵ!");

        // Remove 1 item
        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() <= 0) {
            player.getInventory().setItemInMainHand(null);
        }

        // Chơi âm thanh
        player.getWorld().playSound(player.getLocation(), 
                org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }
}