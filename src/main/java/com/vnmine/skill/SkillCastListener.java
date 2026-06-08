package com.vnmine.skill;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * SkillCastListener - Lắng nghe sự kiện chuột phải để cast skill từ thanh dùng nhanh
 * Khi player click chuột phải và đang cầm item đã được gán skill thì cast skill đó
 */
public class SkillCastListener implements Listener {

    private final VNMinePlugin plugin;
    private final SkillManager skillManager;

    public SkillCastListener(VNMinePlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Chỉ xử lý click chuột phải
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Nếu đang cầm item không phải skill quickbar, bỏ qua
        if (item == null || item.getType() == Material.AIR) return;

        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        // Kiểm tra xem slot hiện tại có skill không
        int hotbarSlot = player.getInventory().getHeldItemSlot();
        String skillId = data.getSkillData().getQuickbarSkill(hotbarSlot);

        if (skillId == null) return; // Không phải skill slot

        SkillManager.SkillConfig skill = skillManager.getSkill(skillId);
        if (skill == null) return;

        // Nếu là ACTIVE skill thì cast, không block interaction với block
        if (!skill.type.equals("ACTIVE")) return;

        event.setCancelled(true);
        skillManager.castSkill(player, skill, data);
    }
}