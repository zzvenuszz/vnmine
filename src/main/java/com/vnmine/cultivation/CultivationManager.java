package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CultivationManager - Quản lý hệ thống tu luyện
 * Quản lý level, exp, cảnh giới, linh lực, lôi kiếp
 */
public class CultivationManager {

    private final VNMinePlugin plugin;
    private boolean enabled;

    // Dữ liệu người chơi
    private final Map<UUID, PlayerCultivationData> playerDataMap;

    // Cấu hình cảnh giới
    private static final Map<Integer, RealmConfig> REALMS = new LinkedHashMap<>();
    private static final String[] TIER_NAMES = {
            "nhất trọng", "nhị trọng", "tam trọng", "tứ trọng",
            "ngũ trọng", "lục trọng", "thất trọng", "bát trọng", "cửu trọng"
    };
    private static final String[] TIER_COLORS = {
            "&7", "&7", "&7", "&7", "&7", "&7", "&7", "&7", "&7"
    };

    // Cấu hình exp
    private double expPerLevelMultiplier;
    private double expKillMob;
    private double expKillElite;
    private double expKillBoss;
    private double expBreakOre;
    private double expFishing;

    // Cấu hình linh lực
    private int baseMaxMana;
    private int manaPerLevel;
    private int manaRegenAmount;
    private int manaRegenInterval;
    private int combatDelayTicks;
    private boolean manaBossBarEnabled;

    // Cấu hình lôi kiếp
    private boolean tribulationEnabled;
    private double tribulationBaseDamage;
    private double tribulationDamageMultiplier;
    private int tribulationCountdown;
    private int tribulationStrikeInterval;
    private int tribulationImmunityDuration;
    private List<String> tribulationSuccessMessage;
    private List<String> tribulationFailMessage;
    private List<String> tribulationBroadcast;

    // File lưu trữ
    private File dataFile;
    private FileConfiguration dataConfig;

    public CultivationManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
        loadConfig();
        initDataFile();
        loadData();
        startManaRegenTask();
        startAutoSaveTask();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        if (!config.contains("cultivation")) {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            config = plugin.getConfig();
        }

        ConfigurationSection cultivation = config.getConfigurationSection("cultivation");
        if (cultivation == null) {
            enabled = false;
            return;
        }

        enabled = cultivation.getBoolean("enabled", true);
        expPerLevelMultiplier = cultivation.getDouble("exp-per-level-multiplier", 100);
        expKillMob = cultivation.getDouble("exp-sources.kill-mob.amount", 10);
        expKillElite = cultivation.getDouble("exp-sources.kill-elite.amount", 50);
        expKillBoss = cultivation.getDouble("exp-sources.kill-boss.amount", 200);
        expBreakOre = cultivation.getDouble("exp-sources.break-ore.amount", 5);
        expFishing = cultivation.getDouble("exp-sources.fishing.amount", 15);

        // Mana config
        ConfigurationSection manaSection = cultivation.getConfigurationSection("mana");
        if (manaSection != null) {
            baseMaxMana = manaSection.getInt("base-max-mana", 100);
            manaPerLevel = manaSection.getInt("mana-per-level", 10);
            manaRegenAmount = manaSection.getInt("regen.base-amount", 2);
            manaRegenInterval = manaSection.getInt("regen.regen-interval-ticks", 40);
            combatDelayTicks = manaSection.getInt("regen.combat-delay-ticks", 100);
            manaBossBarEnabled = manaSection.getBoolean("display.bossbar-enabled", true);
        } else {
            baseMaxMana = 100;
            manaPerLevel = 10;
            manaRegenAmount = 2;
            manaRegenInterval = 40;
            combatDelayTicks = 100;
            manaBossBarEnabled = true;
        }

        // Tribulation config
        ConfigurationSection tribSection = cultivation.getConfigurationSection("tribulation");
        if (tribSection != null) {
            tribulationEnabled = tribSection.getBoolean("enabled", true);
            tribulationBaseDamage = tribSection.getDouble("damage.base", 5.0);
            tribulationDamageMultiplier = tribSection.getDouble("damage.per-strike-multiplier", 1.5);
            tribulationCountdown = tribSection.getInt("countdown-seconds", 5);
            tribulationStrikeInterval = tribSection.getInt("strike-interval-ticks", 20);
            tribulationImmunityDuration = tribSection.getInt("immunity-duration-seconds", 60);
            tribulationSuccessMessage = tribSection.getStringList("success.message");
            tribulationFailMessage = tribSection.getStringList("fail.message");
            tribulationBroadcast = tribSection.getStringList("broadcast");
        } else {
            tribulationEnabled = true;
            tribulationBaseDamage = 5.0;
            tribulationDamageMultiplier = 1.5;
            tribulationCountdown = 5;
            tribulationStrikeInterval = 20;
            tribulationImmunityDuration = 60;
        }

        // Load realms
        REALMS.clear();
        ConfigurationSection realmsSection = cultivation.getConfigurationSection("realms");
        if (realmsSection != null) {
            for (String key : realmsSection.getKeys(false)) {
                ConfigurationSection realmSection = realmsSection.getConfigurationSection(key);
                if (realmSection == null) continue;
                try {
                    int startLevel = Integer.parseInt(key);
                    String name = realmSection.getString("name", "Unknown");
                    String prefix = realmSection.getString("prefix", "&7[" + name + "");
                    REALMS.put(startLevel, new RealmConfig(startLevel, name, prefix));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Đảm bảo có ít nhất cảnh giới Khí Động
        if (REALMS.isEmpty()) {
            REALMS.put(1, new RealmConfig(1, "Khí Động", "&7[Khí Động"));
            REALMS.put(11, new RealmConfig(11, "Luyện Khí", "&a[Luyện Khí"));
            REALMS.put(21, new RealmConfig(21, "Trúc Cơ", "&b[Trúc Cơ"));
        }
    }

    private void initDataFile() {
        dataFile = new File(plugin.getDataFolder(), "cultivation_data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create cultivation_data.yml!");
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void loadData() {
        playerDataMap.clear();
        if (dataConfig == null) return;

        ConfigurationSection playersSection = dataConfig.getConfigurationSection("players");
        if (playersSection == null) return;

        for (String uuidStr : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection section = playersSection.getConfigurationSection(uuidStr);
                if (section == null) continue;

                String name = section.getString("name", "Unknown");
                PlayerCultivationData data = new PlayerCultivationData(uuid, name);
                data.setLevel(section.getInt("level", 1));
                data.setExperience(section.getDouble("exp", 0));
                data.setMana(section.getInt("mana", baseMaxMana));
                data.setMaxMana(section.getInt("max-mana", baseMaxMana));

                // Load skills
                List<String> skills = section.getStringList("learned-skills");
                for (String skill : skills) {
                    data.learnSkill(skill);
                }

                // Load active passives
                List<String> passives = section.getStringList("active-passives");
                for (String passive : passives) {
                    data.getActivePassiveSkills().put(passive, true);
                }

                data.setMobsKilled(section.getInt("stats.mobs-killed", 0));
                data.setElitesKilled(section.getInt("stats.elites-killed", 0));
                data.setBossesKilled(section.getInt("stats.bosses-killed", 0));
                data.setPillsCrafted(section.getInt("stats.pills-crafted", 0));
                data.setHerbsHarvested(section.getInt("stats.herbs-harvested", 0));

                playerDataMap.put(uuid, data);
            } catch (IllegalArgumentException ignored) {
            }
        }
        plugin.getLogger().info("Loaded " + playerDataMap.size() + " player cultivation data.");
    }

    public void saveData() {
        if (dataConfig == null) return;

        dataConfig.set("players", null);
        for (Map.Entry<UUID, PlayerCultivationData> entry : playerDataMap.entrySet()) {
            String path = "players." + entry.getKey().toString();
            PlayerCultivationData data = entry.getValue();
            dataConfig.set(path + ".name", data.getPlayerName());
            dataConfig.set(path + ".level", data.getLevel());
            dataConfig.set(path + ".exp", data.getExperience());
            dataConfig.set(path + ".mana", data.getMana());
            dataConfig.set(path + ".max-mana", data.getMaxMana());

            List<String> skills = new ArrayList<>(data.getLearnedSkills().keySet());
            dataConfig.set(path + ".learned-skills", skills);

            List<String> passives = new ArrayList<>();
            for (Map.Entry<String, Boolean> p : data.getActivePassiveSkills().entrySet()) {
                if (p.getValue()) passives.add(p.getKey());
            }
            dataConfig.set(path + ".active-passives", passives);

            dataConfig.set(path + ".stats.mobs-killed", data.getMobsKilled());
            dataConfig.set(path + ".stats.elites-killed", data.getElitesKilled());
            dataConfig.set(path + ".stats.bosses-killed", data.getBossesKilled());
            dataConfig.set(path + ".stats.pills-crafted", data.getPillsCrafted());
            dataConfig.set(path + ".stats.herbs-harvested", data.getHerbsHarvested());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save cultivation_data.yml!");
        }
    }

    private void startManaRegenTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!enabled) return;
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerCultivationData data = getPlayerData(player.getUniqueId());
                if (data == null) continue;

                // Kiểm tra combat delay
                long timeSinceCombat = System.currentTimeMillis() - data.getLastCombatTime();
                if (timeSinceCombat < combatDelayTicks * 50L) continue;

                long timeSinceRegen = System.currentTimeMillis() - data.getLastManaRegenTime();
                if (timeSinceRegen >= manaRegenInterval * 50L) {
                    data.regenMana(manaRegenAmount);
                    data.setLastManaRegenTime(System.currentTimeMillis());

                    // Cập nhật BossBar
                    if (manaBossBarEnabled) {
                        updateManaBossBar(player, data);
                    }
                }
            }
        }, 20L, 20L);
    }

    private void startAutoSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveData, 
                6000L, 6000L); // Lưu mỗi 5 phút
    }

    /**
     * Cập nhật BossBar linh lực
     */
    private void updateManaBossBar(Player player, PlayerCultivationData data) {
        if (data == null) return;
        double percent = data.getManaPercent();
        String bar = createProgressBar(percent, 20);
        String msg = ColorUtils.colorize("&b◆ Linh Lực ◆ &7[" + bar + "&7] &b" + 
                data.getMana() + "/" + data.getMaxMana());
        player.sendActionBar(ColorUtils.toComponent(msg));
    }

    /**
     * Tạo progress bar
     */
    private String createProgressBar(double percent, int totalBars) {
        int filled = (int) (percent / 100.0 * totalBars);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < totalBars; i++) {
            if (i < filled) {
                sb.append("&b█");
            } else {
                sb.append("&7░");
            }
        }
        return ColorUtils.colorize(sb.toString());
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Lấy dữ liệu tu luyện của player
     */
    public PlayerCultivationData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    /**
     * Lấy hoặc tạo dữ liệu tu luyện
     */
    public PlayerCultivationData getOrCreatePlayerData(UUID uuid, String playerName) {
        return playerDataMap.computeIfAbsent(uuid, k -> {
            PlayerCultivationData data = new PlayerCultivationData(uuid, playerName);
            data.setMaxMana(calculateMaxMana(1));
            data.setMana(data.getMaxMana());
            return data;
        });
    }

    /**
     * Tính linh lực tối đa dựa vào level
     */
    public int calculateMaxMana(int level) {
        return baseMaxMana + (level * manaPerLevel);
    }

    /**
     * Thêm exp cho người chơi
     */
    public void addExperience(Player player, double amount) {
        if (!enabled || player == null) return;
        PlayerCultivationData data = getOrCreatePlayerData(player.getUniqueId(), player.getName());
        
        // Kiểm tra max level
        if (data.getLevel() >= 100) return;

        if (data.addExperience(amount)) {
            // Level up! Kiểm tra lôi kiếp
            handleLevelUp(player, data);
        }
    }

    /**
     * Xử lý khi lên cấp
     */
    private void handleLevelUp(Player player, PlayerCultivationData data) {
        int level = data.getLevel();
        String realmName = data.getRealmName();
        String realmPrefix = data.getRealmPrefix();

        MessageUtils.sendTitle(player, 
                "&a&l✦ THĂNG CẤP ✦", 
                "&fCấp " + level + " - " + realmPrefix + "&f]",
                10, 60, 10);

        MessageUtils.broadcast(
                "&d✦ &l" + player.getName() + " &r&dđã đột phá lên &e" + realmPrefix + "&r&e] &d✦",
                Sound.ENTITY_PLAYER_LEVELUP
        );

        // Kiểm tra lôi kiếp: level chẵn (10, 20, 30, ...)
        if (tribulationEnabled && level % 10 == 0) {
            int strikes = level / 10;
            startTribulation(player, data, strikes);
        }
    }

    /**
     * Bắt đầu lôi kiếp
     */
    private void startTribulation(Player player, PlayerCultivationData data, int strikes) {
        String realmName = data.getRealmName();

        // Broadcast thông báo
        for (String msg : tribulationBroadcast) {
            MessageUtils.broadcast(msg
                    .replace("{player}", player.getName())
                    .replace("{strikes}", String.valueOf(strikes))
                    .replace("{realm}", realmName));
        }

        // Đếm ngược
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            
            // Bắt đầu đánh sét
            performTribulationStrikes(player, data, strikes, 0);
            
        }, tribulationCountdown * 20L);
    }

    /**
     * Thực hiện các đòn sét đánh
     */
    private void performTribulationStrikes(Player player, PlayerCultivationData data, 
                                            int remaining, int currentStrike) {
        if (!player.isOnline()) return;

        Location loc = player.getLocation();
        int strikeNum = currentStrike + 1;

        // Tính sát thương: base * multiplier^(strikeNum-1)
        double damage = tribulationBaseDamage * Math.pow(tribulationDamageMultiplier, strikeNum - 1);

        // Hiệu ứng sét
        loc.getWorld().strikeLightning(loc);
        
        // Sát thương
        player.damage(damage);

        // Hiệu ứng nổ
        loc.getWorld().createExplosion(loc, 1.0f, false, false);

        // Thông báo
        MessageUtils.send(player, "&e⚡ Lôi kiếp &f" + strikeNum + "/" + (remaining + currentStrike) + 
                " &e| Sát thương: &c" + String.format("%.1f", damage));

        if (remaining > 1 && player.isOnline()) {
            // Đòn tiếp theo
            int nextStrike = currentStrike + 1;
            int nextRemaining = remaining - 1;
            Bukkit.getScheduler().runTaskLater(plugin, () -> 
                performTribulationStrikes(player, data, nextRemaining, nextStrike),
                tribulationStrikeInterval
            );
        } else {
            // Hoàn thành lôi kiếp
            if (player.isOnline() && !player.isDead()) {
                completeTribulation(player, data);
            } else {
                failTribulation(player, data);
            }
        }
    }

    /**
     * Lôi kiếp thành công
     */
    private void completeTribulation(Player player, PlayerCultivationData data) {
        String realmName = data.getRealmName();

        // Thông báo thành công
        for (String msg : tribulationSuccessMessage) {
            MessageUtils.broadcast(msg
                    .replace("{player}", player.getName())
                    .replace("{realm}", realmName));
        }

        MessageUtils.sendTitle(player,
                "&d&l✦ ĐỘ KIẾP THÀNH CÔNG ✦",
                "&fBước vào " + data.getRealmPrefix() + "&r&f]",
                10, 80, 10);

        // Thiết lập miễn dịch
        player.setNoDamageTicks(tribulationImmunityDuration * 20);

        // Thưởng exp
        addExperience(player, tribulationBaseDamage * 20);

        MessageUtils.playSound(player, Sound.BLOCK_BELL_USE);
    }

    /**
     * Lôi kiếp thất bại
     */
    private void failTribulation(Player player, PlayerCultivationData data) {
        for (String msg : tribulationFailMessage) {
            MessageUtils.broadcast(msg
                    .replace("{player}", player.getName())
                    .replace("{realm}", data.getRealmName()));
        }

        // Mất 50% exp hiện tại
        double currentExp = data.getExperience();
        data.setExperience(currentExp * 0.5);
    }

    /**
     * Tiêu hao linh lực
     */
    public boolean consumeMana(Player player, int amount) {
        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        if (data == null || data.getMana() < amount) return false;
        data.consumeMana(amount);
        data.updateCombatTime();
        updateManaBossBar(player, data);
        return true;
    }

    /**
     * Kiểm tra có đủ linh lực không
     */
    public boolean hasEnoughMana(Player player, int amount) {
        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        return data != null && data.getMana() >= amount;
    }

    /**
     * Lấy tên cảnh giới dựa vào level (static)
     */
    public static String getRealmName(int level) {
        RealmConfig realm = getRealmForLevel(level);
        if (realm == null) return "&7Khí Động";

        int levelInRealm = level - realm.startLevel + 1;
        if (levelInRealm == 10) {
            return realm.name + " Đại Viên Mãn";
        } else {
            int tierIndex = levelInRealm - 1;
            String tierName = (tierIndex >= 0 && tierIndex < TIER_NAMES.length) 
                    ? TIER_NAMES[tierIndex] : "?";
            return realm.name + " " + tierName;
        }
    }

    /**
     * Lấy prefix cảnh giới (static)
     */
    public static String getRealmPrefix(int level) {
        RealmConfig realm = getRealmForLevel(level);
        if (realm == null) return "&7[Khí Động";

        int levelInRealm = level - realm.startLevel + 1;
        if (levelInRealm == 10) {
            return realm.prefix + " Đại Viên Mãn";
        } else {
            int tierIndex = levelInRealm - 1;
            String tierName = (tierIndex >= 0 && tierIndex < TIER_NAMES.length) 
                    ? TIER_NAMES[tierIndex] : "?";
            return realm.prefix + " " + tierName;
        }
    }

    /**
     * Tìm cảnh giới phù hợp với level
     */
    private static RealmConfig getRealmForLevel(int level) {
        RealmConfig best = null;
        for (RealmConfig realm : REALMS.values()) {
            if (realm.startLevel <= level) {
                if (best == null || realm.startLevel > best.startLevel) {
                    best = realm;
                }
            }
        }
        return best;
    }

    /**
     * Lấy exp cần cho level
     */
    public double getExpForLevel(int level) {
        return level * expPerLevelMultiplier;
    }

    /**
     * Kiểm tra hệ thống có bật không
     */
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public double getExpKillMob() { return expKillMob; }
    public double getExpKillElite() { return expKillElite; }
    public double getExpKillBoss() { return expKillBoss; }

    // ==================== INNER CLASS ====================

    private static class RealmConfig {
        final int startLevel;
        final String name;
        final String prefix;

        RealmConfig(int startLevel, String name, String prefix) {
            this.startLevel = startLevel;
            this.name = name;
            this.prefix = prefix;
        }
    }

    public void reload() {
        loadConfig();
        loadData();
    }
}