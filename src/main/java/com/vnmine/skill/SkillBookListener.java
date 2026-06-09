package com.vnmine.skill;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * SkillBookListener - Bắt sự kiện click phải với sách công pháp
 * Xử lý việc học skill từ sách
 */
public class SkillBookListener implements Listener {

    private final VNMinePlugin plugin;

    public SkillBookListener(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Chỉ xử lý click phải (RIGHT_CLICK_AIR hoặc RIGHT_CLICK_BLOCK)
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Kiểm tra có phải sách công pháp không
        if (item == null || item.getType() == Material.AIR) return;
        if (!SkillBookManager.isSkillBook(item)) return;

        // Cancel event để không spawn entity items
        event.setCancelled(true);

        // Xử lý học skill
        SkillBookManager bookManager = plugin.getSkillBookManager();
        if (bookManager == null) {
            MessageUtils.send(player, "&cHệ thống sách công pháp chưa được kích hoạt!");
            return;
        }

        bookManager.learnFromBook(player, item);
    }
}