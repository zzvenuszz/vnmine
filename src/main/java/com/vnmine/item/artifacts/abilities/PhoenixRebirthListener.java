package com.vnmine.item.artifacts.abilities;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PhoenixRebirthListener - Xử lý hồi sinh từ Phượng Hoàng Lệnh
 * - Khi player chết, nếu có Phượng Hoàng Lệnh trong inventory
 *   → cancel death, hồi đầy HP, thông báo, bắt đầu cooldown
 * - Cooldown cấu hình trong config.yml (mặc định 3600 giây)
 */
public class PhoenixRebirthListener implements Listener {

    private final VNMinePlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final String PHOENIX_REBIRTH = "Phượng Hoàng Lệnh";

    public PhoenixRebirthListener(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Kiểm tra có bật trong config không
        if (!plugin.getConfig().getBoolean("items.artifacts.phoenix_rebirth.enabled", true)) return;

        UUID uuid = player.getUniqueId();

        // Kiểm tra cooldown
        int cooldownSeconds = plugin.getConfig().getInt("items.artifacts.phoenix_rebirth.cooldown-seconds", 3600);
        long now = System.currentTimeMillis();
        long lastUse = cooldowns.getOrDefault(uuid, 0L);
        long remaining = (lastUse + (cooldownSeconds * 1000L)) - now;
        if (remaining > 0) {
            String cooldownMsg = plugin.getConfig().getString("items.artifacts.phoenix_rebirth.cooldown-message",
                    "&cPhượng Hoàng Lệnh đang hồi phục! Còn &e{remaining} &cgiây!");
            cooldownMsg = cooldownMsg.replace("{remaining}", String.valueOf(remaining / 1000));
            MessageUtils.send(player, cooldownMsg);
            return;
        }

        // Kiểm tra có Phượng Hoàng Lệnh trong inventory không
        ItemStack phoenixItem = findPhoenixItem(player);
        if (phoenixItem == null) return;

        // Cancel death event - hồi sinh
        event.setCancelled(true);

        // Hồi đầy HP
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(10);
        player.setFireTicks(0);

        // Xóa 1 Phượng Hoàng Lệnh khỏi inventory
        phoenixItem.setAmount(phoenixItem.getAmount() - 1);
        if (phoenixItem.getAmount() <= 0) {
            player.getInventory().remove(phoenixItem);
        }

        // Bắt đầu cooldown
        cooldowns.put(uuid, now);

        // Thông báo
        boolean notify = plugin.getConfig().getBoolean("items.artifacts.phoenix_rebirth.notify-player", true);
        if (notify) {
            String msg = plugin.getConfig().getString("items.artifacts.phoenix_rebirth.notify-message",
                    "&6✦ Phượng Hoàng Lệnh! Hồi sinh thành công!");
            MessageUtils.send(player, msg);
        }

        // Broadcast
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.getWorld().playSound(player.getLocation(),
                org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.0f);
    }

    /**
     * Tìm Phượng Hoàng Lệnh trong inventory
     */
    private ItemStack findPhoenixItem(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (!item.hasItemMeta()) continue;

            ItemMeta meta = item.getItemMeta();
            if (!meta.hasDisplayName()) continue;

            String name = meta.getDisplayName().replaceAll("§[0-9a-fk-or]", "").trim();
            if (name.contains(PHOENIX_REBIRTH)) {
                return item;
            }
        }
        return null;
    }
}