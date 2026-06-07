package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 * CultivationListener - Lắng nghe sự kiện để thêm exp, quản lý linh lực
 */
public class CultivationListener implements Listener {

    private final VNMinePlugin plugin;
    private final CultivationManager cultivationManager;

    public CultivationListener(VNMinePlugin plugin, CultivationManager cultivationManager) {
        this.plugin = plugin;
        this.cultivationManager = cultivationManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Đảm bảo có dữ liệu tu luyện
        cultivationManager.getOrCreatePlayerData(player.getUniqueId(), player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        // Player giết mob
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Entity entity = event.getEntity();
        String entityType = entity.getType().name();

        // Xác định loại quái để thêm exp
        double expAmount = 0;
        boolean isElite = false;
        boolean isBoss = false;

        // Kiểm tra metadata để biết elite/boss
        if (entity.hasMetadata("vnmine_elite")) {
            isElite = true;
            expAmount = cultivationManager.getExpKillElite();
            PlayerCultivationData data = cultivationManager.getPlayerData(killer.getUniqueId());
            if (data != null) data.incrementElitesKilled();
        } else if (entity.hasMetadata("vnmine_boss")) {
            isBoss = true;
            expAmount = cultivationManager.getExpKillBoss();
            PlayerCultivationData data = cultivationManager.getPlayerData(killer.getUniqueId());
            if (data != null) data.incrementBossesKilled();
        } else if (entity instanceof Monster) {
            expAmount = cultivationManager.getExpKillMob();
            PlayerCultivationData data = cultivationManager.getPlayerData(killer.getUniqueId());
            if (data != null) data.incrementMobsKilled();
        }

        if (expAmount > 0) {
            cultivationManager.addExperience(killer, expAmount);
            // Hiển thị ActionBar khi nhận exp
            PlayerCultivationData data = cultivationManager.getPlayerData(killer.getUniqueId());
            if (data != null) {
                String expBar = createExpBar(data.getExpPercent(), 15);
                MessageUtils.sendActionBar(killer,
                        "&a✦ +" + (int) expAmount + " EXP &7| " + expBar + " &7" + 
                        (int) data.getExperience() + "/" + (int) data.getMaxExperience());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        // Thêm exp khi đào quặng
        if (isOre(blockType)) {
            cultivationManager.addExperience(player, 5);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            cultivationManager.addExperience(event.getPlayer(), 15);
        }
    }

    /**
     * Kiểm tra block có phải quặng không
     */
    private boolean isOre(Material material) {
        return material.name().endsWith("_ORE");
    }

    /**
     * Tạo exp bar
     */
    private String createExpBar(double percent, int totalBars) {
        int filled = (int) (percent / 100.0 * totalBars);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < totalBars; i++) {
            if (i < filled) {
                sb.append("&a█");
            } else {
                sb.append("&7░");
            }
        }
        return ColorUtils.colorize(sb.toString());
    }
}