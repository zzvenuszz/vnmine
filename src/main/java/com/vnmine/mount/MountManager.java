package com.vnmine.mount;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * MountManager - Quản lý tọa kỵ phi hành
 */
public class MountManager {

    private final VNMinePlugin plugin;
    private boolean enabled;
    private int requiredLevel;
    private int manaPerSecond;

    private final Map<String, MountConfig> mountConfigs;
    private final Map<UUID, org.bukkit.entity.Entity> activeMounts;

    public MountManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        this.mountConfigs = new LinkedHashMap<>();
        this.activeMounts = new HashMap<>();
        loadConfig();
        startManaDrainTask();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection mountSection = config.getConfigurationSection("mount");
        if (mountSection == null) {
            enabled = false;
            return;
        }

        enabled = mountSection.getBoolean("enabled", true);
        requiredLevel = mountSection.getInt("required-level", 30);
        manaPerSecond = mountSection.getInt("mana-per-second", 3);
        mountConfigs.clear();

        ConfigurationSection mounts = mountSection.getConfigurationSection("mounts");
        if (mounts == null) return;

        for (String mountId : mounts.getKeys(false)) {
            ConfigurationSection m = mounts.getConfigurationSection(mountId);
            if (m == null || !m.getBoolean("enabled", true)) continue;

            MountConfig mc = new MountConfig(
                    mountId,
                    m.getString("name", mountId),
                    m.getInt("required-level", requiredLevel),
                    m.getDouble("speed", 0.5),
                    m.getDouble("flight-speed", 0.8),
                    m.getString("color", "WHITE")
            );
            mountConfigs.put(mountId, mc);
        }
    }

    private void startManaDrainTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!enabled) return;
            for (Map.Entry<UUID, org.bukkit.entity.Entity> entry : activeMounts.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline() || !player.isInsideVehicle()) {
                    dismissMount(player);
                    continue;
                }

                // Tiêu hao linh lực
                PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(entry.getKey());
                if (data == null || data.getMana() < manaPerSecond) {
                    MessageUtils.send(player, "&cHết linh lực! Tọa kỵ hạ cánh.");
                    dismissMount(player);
                    continue;
                }
                data.consumeMana(manaPerSecond);
            }
        }, 20L, 20L);
    }

    /**
     * Triệu hồi tọa kỵ
     */
    public void summonMount(Player player, String mountId) {
        MountConfig config = mountConfigs.get(mountId);
        if (config == null) {
            MessageUtils.send(player, "&cTọa kỵ '" + mountId + "' không tồn tại!");
            return;
        }

        // Kiểm tra đã mở khóa chưa
        if (!player.getScoreboardTags().contains("vnmine_mount_" + mountId.toLowerCase())) {
            MessageUtils.send(player, "&cBạn chưa mở khóa tọa kỵ này!");
            return;
        }

        // Kiểm tra level
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getLevel() < config.requiredLevel) {
            MessageUtils.send(player, "&cBạn cần đạt cấp " + config.requiredLevel + " để sử dụng tọa kỵ này!");
            return;
        }

        // Hủy tọa kỵ cũ nếu có
        dismissMount(player);

        // Spawn ngựa
        Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
        horse.setCustomName(ColorUtils.colorize(config.name));
        horse.setCustomNameVisible(true);
        horse.setTamed(true);
        horse.setOwner(player);
        horse.setAI(false);
        horse.setCollidable(false);
        horse.setInvulnerable(true);
        horse.setAdult();
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        horse.setColor(Horse.Color.valueOf(config.color.toUpperCase()));

        // Style
        try {
            horse.setStyle(Horse.Style.valueOf("BLACK_DOTS"));
        } catch (IllegalArgumentException ignored) {}

        // Cho player lên ngựa
        horse.addPassenger(player);
        horse.setMetadata("vnmine_mount", new org.bukkit.metadata.FixedMetadataValue(plugin, mountId));

        activeMounts.put(player.getUniqueId(), horse);

        MessageUtils.send(player, "&6✦ Triệu hồi tọa kỵ: " + config.name);
        MessageUtils.send(player, "&7Linh lực tiêu hao: &b" + manaPerSecond + "/giây");
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    /**
     * Hủy tọa kỵ
     */
    public void dismissMount(Player player) {
        org.bukkit.entity.Entity mount = activeMounts.remove(player.getUniqueId());
        if (mount != null) {
            mount.eject();
            mount.remove();
        }
        if (player.isOnline()) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    /**
     * Kiểm tra player đang cưỡi tọa kỵ
     */
    public boolean hasActiveMount(Player player) {
        return activeMounts.containsKey(player.getUniqueId());
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getRequiredLevel() { return requiredLevel; }
    public Collection<MountConfig> getMountConfigs() { return mountConfigs.values(); }

    /**
     * Mở khóa tọa kỵ cho player (thêm scoreboard tag)
     */
    public void unlockMount(Player player, String mountId) {
        String tag = "vnmine_mount_" + mountId.toLowerCase();
        if (!player.getScoreboardTags().contains(tag)) {
            player.addScoreboardTag(tag);
        }
    }

    /**
     * Kiểm tra player đã mở khóa tọa kỵ chưa
     */
    public boolean hasUnlockedMount(Player player, String mountId) {
        return player.getScoreboardTags().contains("vnmine_mount_" + mountId.toLowerCase());
    }

    /**
     * Tạo item key triệu hồi tọa kỵ (cho admin give)
     */
    public ItemStack createMountKey(String mountId) {
        MountConfig config = mountConfigs.get(mountId);
        if (config == null) return null;

        Material mat;
        switch (mountId) {
            case "PHUONG_HOANG": mat = Material.FEATHER; break;
            case "BACH_HO": mat = Material.BONE; break;
            case "THANH_LONG": mat = Material.DRAGON_BREATH; break;
            default: mat = Material.SADDLE;
        }

        ItemStack key = new ItemBuilder(mat)
                .setName(config.name + " &7(Lệnh Bài)")
                .setLore(
                        "",
                        "&7Click phải để học cách triệu hồi",
                        "&7" + config.name,
                        "",
                        "&eYêu cầu cấp: &b" + config.requiredLevel
                )
                .setPersistentData("vnmine_item", "true")
                .setPersistentData("vnmine_mount_key", mountId)
                .build();

        return key;
    }

    public void reload() {
        for (org.bukkit.entity.Entity mount : activeMounts.values()) {
            mount.remove();
        }
        activeMounts.clear();
        loadConfig();
    }

    public static class MountConfig {
        public final String id;
        public final String name;
        public final int requiredLevel;
        public final double speed;
        public final double flightSpeed;
        public final String color;

        MountConfig(String id, String name, int requiredLevel, double speed, double flightSpeed, String color) {
            this.id = id;
            this.name = name;
            this.requiredLevel = requiredLevel;
            this.speed = speed;
            this.flightSpeed = flightSpeed;
            this.color = color;
        }
    }
}