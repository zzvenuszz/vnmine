package com.vnmine.spiritfarm;

import com.vnmine.VNMinePlugin;
import com.vnmine.item.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * SpiritFarmListener - Xử lý tương tác với block linh điền
 * - Click phải bằng block linh điền -> đặt linh điền
 * - Click phải bằng hạt giống -> gieo hạt
 * - Click phải vào cây trưởng thành -> thu hoạch
 * - Phá block linh điền -> chặn
 */
public class SpiritFarmListener implements Listener {

    private final VNMinePlugin plugin;
    private SpiritFarmManager farmManager;

    public SpiritFarmListener(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    public void setFarmManager(SpiritFarmManager farmManager) {
        this.farmManager = farmManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (farmManager == null) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        Location loc = clickedBlock.getLocation();

        // 1. Đặt block linh điền
        String farmBlockData = ItemBuilder.getPersistentData(item, "vnmine_farm_block");
        if (farmBlockData != null) {
            event.setCancelled(true);
            try {
                int grade = Integer.parseInt(farmBlockData);
                Block target = clickedBlock.getRelative(event.getBlockFace());
                if (farmManager.placeFarmBlock(player, target.getLocation(), grade)) {
                    item.setAmount(item.getAmount() - 1);
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cLỗi dữ liệu block linh điền!");
            }
            return;
        }

        // 2. Gieo hạt giống
        String seedData = ItemBuilder.getPersistentData(item, "vnmine_seed");
        if (seedData != null) {
            event.setCancelled(true);
            if (farmManager.plantSeed(player, loc, seedData)) {
                item.setAmount(item.getAmount() - 1);
            }
            return;
        }

        // 3. Thu hoạch
        if (farmManager.getFarmBlockAt(loc) != null) {
            event.setCancelled(true);
            farmManager.harvest(player, loc);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (farmManager == null) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();

        SpiritFarmBlock farmBlock = farmManager.getFarmBlockAt(loc);
        if (farmBlock != null) {
            event.setCancelled(true);
            player.sendMessage("§cHãy dùng click phải để thu hoạch trước khi phá linh điền!");
        }
    }
}