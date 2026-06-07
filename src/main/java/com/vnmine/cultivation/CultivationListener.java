package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import com.vnmine.util.NameTagManager;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 * CultivationListener - Lắng nghe sự kiện để thêm exp, quản lý linh lực
 */
public class CultivationListener implements Listener {

    private final VNMinePlugin plugin;
    private final CultivationManager cultivationManager;
    private final NameTagManager nameTagManager;

    public CultivationListener(VNMinePlugin plugin, CultivationManager cultivationManager, NameTagManager nameTagManager) {
        this.plugin = plugin;
        this.cultivationManager = cultivationManager;
        this.nameTagManager = nameTagManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Đảm bảo có dữ liệu tu luyện
        PlayerCultivationData data = cultivationManager.getOrCreatePlayerData(player.getUniqueId(), player.getName());
        // Đồng bộ level tu tiên với level Minecraft vanilla
        int mcLevel = player.getLevel();
        if (data.getLevel() != mcLevel) {
            data.setLevel(mcLevel);
            data.setMaxMana(cultivationManager.calculateMaxMana(mcLevel));
            if (data.getMana() > data.getMaxMana()) {
                data.setMana(data.getMaxMana());
            }
        }
        // Cập nhật name tag với prefix tu tiên
        nameTagManager.updateNameTag(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        PlayerCultivationData data = cultivationManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        int newLevel = player.getLevel();
        data.setLevel(newLevel);
        data.setMaxMana(cultivationManager.calculateMaxMana(newLevel));
        if (data.getMana() > data.getMaxMana()) {
            data.setMana(data.getMaxMana());
        }

        // Cập nhật name tag
        nameTagManager.updateNameTag(player);

        // Hiển thị action bar thông tin
        String realmPrefix = data.getRealmPrefix();
        MessageUtils.sendActionBar(player,
                "&a✦ Cấp " + newLevel + " - " + realmPrefix + "&r&a] ✦");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerCultivationData data = cultivationManager.getPlayerData(player.getUniqueId());
        String prefix = (data != null) ? data.getRealmPrefix() : "&7[Phàm Nhân";

        // Format chat: Display name (đã có prefix + ] + tên) : nội dung
        // %1$s là DisplayName, %2$s là nội dung chat
        String format = ColorUtils.colorize("%1$s&r&f: %2$s");
        event.setFormat(format);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Dọn dẹp team scoreboard
        nameTagManager.removeTeam(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerCultivationData data = cultivationManager.getPlayerData(player.getUniqueId());
        if (data != null && data.isTribulationInProgress()) {
            // Player chết trong lúc độ kiếp → fail tribulation
            cultivationManager.failTribulation(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        // Player giết mob
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Entity entity = event.getEntity();

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