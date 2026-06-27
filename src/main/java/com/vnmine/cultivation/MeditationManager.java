package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;

public class MeditationManager {
    private final VNMinePlugin plugin;
    private final Map<UUID, ActiveMeditationSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> sneakHoldTicks = new ConcurrentHashMap<>();
    private final Map<UUID, ArmorStand> chairStands = new ConcurrentHashMap<>();

    public MeditationManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        startSneakMonitor();
        startExpTask();
        startParticleTask();
        startBiomeMonitorTask();
    }

    private CultivationManager getCultivationManager() {
        return plugin.getCultivationManager();
    }
    
    public MeditationConfig getMeditationConfig() {
        return plugin.getMeditationConfig();
    }

    private void startSneakMonitor() {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    if (player.isInsideVehicle() && isMeditating(player)) continue;
                    UUID uuid = player.getUniqueId();
                    if (player.isSneaking()) {
                        int prev = sneakHoldTicks.getOrDefault(uuid, 0);
                        sneakHoldTicks.merge(uuid, 1, Integer::sum);
                        int newTicks = sneakHoldTicks.get(uuid);
                        if (prev == 0 && newTicks == 1) {
                            plugin.getLogger().info("[DEBUG Meditation] Player " + player.getName() + " started holding sneak.");
                            player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, player.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.01);
                        }
                        int required = getConfigSneakDurationTicks();
                        if (newTicks >= required && !isMeditating(player)) {
                            plugin.getLogger().info("[DEBUG Meditation] Player " + player.getName() + " triggered meditation menu after holding sneak for " + newTicks + " ticks.");
                            openConfirmGUI(player);
                            sneakHoldTicks.put(uuid, 0);
                        }
                    } else {
                        sneakHoldTicks.remove(uuid);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void startExpTask() {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (ActiveMeditationSession session : new ArrayList<>(activeSessions.values())) {
                    Player player = org.bukkit.Bukkit.getPlayer(session.getPlayerId());
                    if (player == null || !player.isOnline() || !isMeditating(player)) {
                        stopMeditation(session.getPlayerId(), true);
                        continue;
                    }
                    session.incrementTicksSinceLastExp();
                    if (session.getTicksSinceLastExp() >= getConfigExpIntervalTicks()) {
                        int exp = getConfigPassiveExp();
                        getCultivationManager().addExperience(player, exp);
                        session.setTicksSinceLastExp(0);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void startParticleTask() {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                for (ActiveMeditationSession session : new ArrayList<>(activeSessions.values())) {
                    Player player = org.bukkit.Bukkit.getPlayer(session.getPlayerId());
                    if (player == null || !player.isOnline() || !isMeditating(player)) {
                        stopMeditation(session.getPlayerId(), true);
                        continue;
                    }
                    Location loc = session.getSitLocation().add(0, 0.2, 0);
                    player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, loc, 10, 0.5, 0.5, 0.5, 0.02);
                }
            }
        }.runTaskTimer(plugin, 1L, 2L);
    }

    private void startBiomeMonitorTask() {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                var biomeManager = plugin.getBiomeQiManager();
                if (biomeManager == null) return;
                for (ActiveMeditationSession session : new ArrayList<>(activeSessions.values())) {
                    Player player = org.bukkit.Bukkit.getPlayer(session.getPlayerId());
                    if (player == null || !player.isOnline() || !isMeditating(player)) continue;
                    org.bukkit.block.Biome biome = player.getLocation().getBlock().getBiome();
                    double penalty = biomeManager.getDrainPenalty(biome);
                    if (penalty > 1.0) {
                        var data = getCultivationManager().getPlayerData(player.getUniqueId());
                        if (data != null) {
                            int drain = Math.max(1, (int) (plugin.getConfig().getInt("cultivation.mana.regen.base-amount", 5) * (penalty - 1.0)));
                            data.setMana(Math.max(0, data.getMana() - drain));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, getConfigBiomeCheckTicks());
    }

    private int getConfigExpIntervalTicks() {
        return plugin.getConfig().getConfigurationSection("cultivation.meditation") != null
                ? plugin.getConfig().getConfigurationSection("cultivation.meditation").getInt("exp-interval-ticks", 100)
                : 100;
    }

    private int getConfigPassiveExp() {
        return plugin.getConfig().getConfigurationSection("cultivation.meditation") != null
                ? plugin.getConfig().getConfigurationSection("cultivation.meditation").getInt("passive-exp", 8)
                : 8;
    }

    private int getConfigBiomeCheckTicks() {
        return plugin.getConfig().getConfigurationSection("cultivation.biome-qi") != null
                ? plugin.getConfig().getConfigurationSection("cultivation.biome-qi").getInt("check-interval-ticks", 100)
                : 100;
    }

    public void handleSneakChange(Player player, boolean isSneaking) {
        UUID uuid = player.getUniqueId();
        if (isSneaking) {
            startMeditationFromSneak(player);
        } else {
            stopMeditation(uuid, false);
            sneakHoldTicks.remove(uuid);
        }
    }

    private void startMeditationFromSneak(Player player) {
        if (!player.isOnline()) return;
        UUID uuid = player.getUniqueId();
        if (isMeditating(player)) return;
        if (player.isInsideVehicle()) return;
        int hold = sneakHoldTicks.getOrDefault(uuid, 0);
        if (hold < getConfigSneakDurationTicks()) return;
        openConfirmGUI(player);
        sneakHoldTicks.put(uuid, 0);
    }

    private int getConfigSneakDurationTicks() {
        return plugin.getConfig().getConfigurationSection("cultivation.meditation") != null
                ? plugin.getConfig().getConfigurationSection("cultivation.meditation").getInt("sneak-duration-ticks", 200)
                : 200;
    }

    private void openConfirmGUI(Player player) {
        var cfgSection = plugin.getConfig().getConfigurationSection("cultivation.meditation");
        String title = "&6&l✧ Tọa Thiền ✧";
        String confirm = "&a[XÁC NHẬN] Bước vào trạng thái Tọa Thiền";
        String cancel = "&c[HỦY BỎ] Không muốn ngồi thiền";
        if (cfgSection != null) {
            title = cfgSection.getString("activation-gui-title", title);
            confirm = cfgSection.getString("activation-gui-confirm", confirm);
            cancel = cfgSection.getString("activation-gui-cancel", cancel);
        }
        var inv = org.bukkit.Bukkit.createInventory(player, 9, title);
        var confirmItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.EMERALD_BLOCK);
        var confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(Component.text(title));
            confirmMeta.setCustomModelData(1);
            confirmItem.setItemMeta(confirmMeta);
        }
        var cancelItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.RED_STAINED_GLASS_PANE);
        var cancelMeta = cancelItem.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.displayName(Component.text(cancel));
            cancelMeta.setCustomModelData(2);
            cancelItem.setItemMeta(cancelMeta);
        }
        inv.setItem(2, confirmItem);
        inv.setItem(6, cancelItem);
        player.openInventory(inv);
    }

    public void confirmMeditation(Player player) {
        if (!player.isOnline()) return;
        if (isMeditating(player)) return;
        startMeditation(player);
    }

    public void cancelMeditation(Player player) {
        if (!player.isOnline()) return;
        MessageUtils.send(player, "&7Bạn đã hủy tọa thiền.");
    }

    private void startMeditation(Player player) {
        UUID uuid = player.getUniqueId();
        Location loc = player.getLocation().clone();
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(loc.add(0, -1.35, 0), EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setInvulnerable(true);
        stand.setCollidable(false);
        stand.addPassenger(player);
        chairStands.put(uuid, stand);
        ActiveMeditationSession session = new ActiveMeditationSession(uuid, stand.getUniqueId(), loc);
        activeSessions.put(uuid, session);
        MessageUtils.send(player, getConfigMessageStart());
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.6f, 1.2f);
    }

    public boolean isMeditating(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public void stopMeditation(UUID uuid, boolean silent) {
        ActiveMeditationSession session = activeSessions.remove(uuid);
        if (session == null) return;
        ArmorStand stand = chairStands.remove(uuid);
        if (stand != null && !stand.isDead()) {
            for (org.bukkit.entity.Entity passenger : stand.getPassengers()) {
                passenger.leaveVehicle();
            }
            stand.remove();
        }
        if (!silent) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                MessageUtils.send(player, getConfigMessageStop());
            }
        }
    }

    private String getConfigMessageStart() {
        var section = plugin.getConfig().getConfigurationSection("cultivation.meditation");
        if (section != null && section.contains("message.start"))
            return section.getString("message.start", "&d✧ Bạn bắt đầu tọa thiền...");
        return "&d✧ Bạn bắt đầu tọa thiền...";
    }

    private String getConfigMessageStop() {
        var section = plugin.getConfig().getConfigurationSection("cultivation.meditation");
        if (section != null && section.contains("message.stop"))
            return section.getString("message.stop", "&7Bạn đã dừng tọa thiền.");
        return "&7Bạn đã dừng tọa thiền.";
    }
}
