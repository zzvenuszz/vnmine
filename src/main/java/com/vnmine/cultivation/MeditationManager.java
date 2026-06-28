package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

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
                        plugin.getLogger().info("[MeditationExp] +" + exp + " exp for " + player.getName());
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
                    PlayerCultivationData data = getCultivationManager().getPlayerData(player.getUniqueId());
                    int level = (data != null) ? data.getLevel() : 1;
                    MeditationConfig config = plugin.getMeditationConfig();
                    Particle particle = config.getParticleType(level);
                    int count = config.getParticleCount(level);
                    double offset = config.getParticleOffset(level);
                    double speed = config.getParticleSpeed(level);
                    Location loc = player.getLocation().add(0, 0.2, 0);
                    player.getWorld().spawnParticle(particle, loc, count, offset, offset, offset, speed);

                    // Fire rings effect - 2 vong lua doi xung qua nguoi choi
                    if (config.isFireRingEnabled()) {
                        Location origin = session.getOriginalLocation().clone().add(0, config.getFireRingYOffset(level), 0);
                        float angle = session.getRotationAngle();
                        double r = config.getFireRingRadius(level);
                        int pCount = config.getFireRingCount(level);
                        Particle fParticle = config.getFireRingParticle(level);
                        double pSpeed = config.getFireRingSpeed(level);
                        // Ring 1
                        for (int i = 0; i < pCount; i++) {
                            double a = (Math.PI * 2 / pCount) * i;
                            double x = r * Math.cos(angle + a);
                            double z = r * Math.sin(angle + a);
                            origin.getWorld().spawnParticle(fParticle, origin.clone().add(x, 0, z), 1, 0, 0, 0, pSpeed);
                        }
                        // Ring 2 (doi xung - lech pha 180 do)
                        for (int i = 0; i < pCount; i++) {
                            double a = (Math.PI * 2 / pCount) * i + Math.PI;
                            double x = r * Math.cos(angle + a);
                            double z = r * Math.sin(angle + a);
                            origin.getWorld().spawnParticle(fParticle, origin.clone().add(x, 0, z), 1, 0, 0, 0, pSpeed);
                        }
                    }

                    // Update flying items rotation
                    if (config.isFlyingItemsEnabled()) {
                        Location origin = session.getOriginalLocation().clone().add(0, config.getFlyingItemYOffset(level), 0);
                        float angle = session.getRotationAngle();
                        double fr = config.getFlyingItemRadius(level);
                        int idx = 0;
                        for (UUID displayId : session.getDisplayItemIds()) {
                            org.bukkit.entity.Entity ent = player.getWorld().getEntity(displayId);
                            if (ent instanceof ItemDisplay) {
                                double itemAngle = angle + (Math.PI * 2 / 3) * idx;
                                double ix = fr * Math.cos(itemAngle);
                                double iz = fr * Math.sin(itemAngle);
                                ent.teleport(origin.clone().add(ix, 0, iz));
                            }
                            idx++;
                        }
                    }

                    session.incrementRotationAngle(0.15f);
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
        return getMeditationConfig().getExpIntervalTicks();
    }

    private int getConfigPassiveExp() {
        return getMeditationConfig().getPassiveExp();
    }

    private int getConfigBiomeCheckTicks() {
        return getMeditationConfig().getBiomeCheckIntervalTicks();
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
        return getMeditationConfig().getSneakDurationTicks();
    }

    private void openConfirmGUI(Player player) {
        MeditationConfig config = getMeditationConfig();
        String title = ColorUtils.colorize(config.getActivationGuiTitle());
        String confirm = ColorUtils.colorize(config.getActivationGuiConfirm());
        String cancel = ColorUtils.colorize(config.getActivationGuiCancel());
        var inv = org.bukkit.Bukkit.createInventory(null, 9, title);
        var confirmItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.LIME_STAINED_GLASS_PANE);
        var confirmMeta = confirmItem.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(Component.text(confirm));
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
        var border = new org.bukkit.inventory.ItemStack(org.bukkit.Material.GRAY_STAINED_GLASS_PANE);
        var borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.displayName(Component.text(""));
            border.setItemMeta(borderMeta);
        }
        for (int i = 0; i < 9; i++) {
            if (i != 2 && i != 6) {
                inv.setItem(i, border);
            }
        }
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
        Location sitLocation = loc.clone().add(0, getMeditationConfig().getSitOffset(), 0);
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(sitLocation, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setInvulnerable(true);
        stand.setCollidable(false);
        stand.addPassenger(player);
        chairStands.put(uuid, stand);
        ActiveMeditationSession session = new ActiveMeditationSession(uuid, stand.getUniqueId(), sitLocation, loc);
        activeSessions.put(uuid, session);
        // Tạo ItemDisplay cho item bay
        spawnFlyingItems(player, session);
        MessageUtils.send(player, getConfigMessageStart());
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.6f, 1.2f);
    }

    public boolean isMeditating(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public void stopMeditation(UUID uuid, boolean silent) {
        ActiveMeditationSession session = activeSessions.remove(uuid);
        if (session == null) return;
        // Xóa ItemDisplay của item bay
        removeFlyingItems(session);
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
        return getMeditationConfig().getMessageStart();
    }

    private String getConfigMessageStop() {
        return getMeditationConfig().getMessageStop();
    }

    // ==================== VISUAL EFFECTS ====================

    // ==================== VISUAL EFFECTS ====================

    private void spawnFlyingItems(Player player, ActiveMeditationSession session) {
        if (!getMeditationConfig().isFlyingItemsEnabled()) return;
        PlayerCultivationData data = getCultivationManager().getPlayerData(player.getUniqueId());
        int level = (data != null) ? data.getLevel() : 1;
        org.bukkit.Material mat = getMeditationConfig().getFlyingItemMaterial(level);
        ItemStack itemStack = new ItemStack(mat);

        Location origin = session.getOriginalLocation().clone().add(0, getMeditationConfig().getFlyingItemYOffset(level), 0);
        double fr = getMeditationConfig().getFlyingItemRadius(level);
        int itemCount = getMeditationConfig().getFlyingItemCount(level);

        for (int i = 0; i < itemCount; i++) {
            double angle = (Math.PI * 2 / itemCount) * i;
            double ix = fr * Math.cos(angle);
            double iz = fr * Math.sin(angle);
            Location spawnLoc = origin.clone().add(ix, 0, iz);

            ItemDisplay display = (ItemDisplay) player.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.ITEM_DISPLAY);
            display.setItemStack(itemStack);
            display.setDisplayWidth(0.5f);
            display.setDisplayHeight(0.5f);
            display.setGravity(false);
            display.setInvulnerable(true);

            session.addDisplayItemId(display.getUniqueId());
        }
    }

    private void removeFlyingItems(ActiveMeditationSession session) {
        for (UUID displayId : session.getDisplayItemIds()) {
            org.bukkit.entity.Entity ent = org.bukkit.Bukkit.getEntity(displayId);
            if (ent != null && !ent.isDead()) {
                ent.remove();
            }
        }
        session.getDisplayItemIds().clear();
    }
}
