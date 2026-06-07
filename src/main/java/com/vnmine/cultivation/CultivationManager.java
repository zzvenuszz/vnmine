package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
    private double tribulationRadiusPerLevel;
    private double tribulationExpForOthers;
    private int tribulationLevelDropOnFail;
    private List<String> tribulationSuccessMessage;
    private List<String> tribulationFailMessage;
    private List<String> tribulationBroadcast;

    // Theo dõi các phiên độ kiếp đang diễn ra
    private final Map<UUID, TribulationSession> activeTribulations = new ConcurrentHashMap<>();

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
            tribulationRadiusPerLevel = tribSection.getDouble("radius-per-level", 1.5);
            tribulationExpForOthers = tribSection.getDouble("exp-for-others", 50.0);
            tribulationLevelDropOnFail = tribSection.getInt("level-drop-on-fail", 1);
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
            tribulationRadiusPerLevel = 1.5;
            tribulationExpForOthers = 50.0;
            tribulationLevelDropOnFail = 1;
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
                data.setWaitingForTribulation(section.getBoolean("waiting-tribulation", false));

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
            dataConfig.set(path + ".waiting-tribulation", data.isWaitingForTribulation());

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

        // Kiểm tra nếu đang chờ độ kiếp thì không nhận exp
        if (data.isWaitingForTribulation()) {
            MessageUtils.sendActionBar(player,
                    "&c⚡ Bạn đang bị chặn ở cảnh giới hiện tại! Hãy độ kiếp để tiếp tục tu luyện.");
            return;
        }

        if (data.addExperience(amount)) {
            // Level up! Kiểm tra xem có cần độ kiếp không
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

        // Kiểm tra lôi kiếp: level 10, 20, 30, ...
        if (tribulationEnabled && PlayerCultivationData.isTribulationLevel(level)) {
            // Chặn player ở threshold, không cho nhận thêm exp
            data.setWaitingForTribulation(true);
            data.setExperience(0); // Reset exp về 0

            // Thông báo yêu cầu độ kiếp
            MessageUtils.send(player, "&6⚡ &lBẠN CẦN ĐỘ KIẾP! ⚡");
            MessageUtils.send(player, "&eCấp " + level + " là ngưỡng đột phá đại cảnh giới!");
            MessageUtils.send(player, "&eHãy dùng &b/vn &emở menu, chọn &bThông Tin &evà bấm &cĐộ Kiếp&e!");
            MessageUtils.send(player, "&cLưu ý: Phải ở nơi có thể thấy bầu trời mới độ kiếp được!");
        }
    }

    // ==================== TRIBULATION SYSTEM ====================

    /**
     * Kiểm tra player có thể độ kiếp không
     */
    public String canStartTribulation(Player player) {
        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        if (data == null) return "&cBạn chưa bắt đầu tu luyện! Dùng /vn start";

        if (!tribulationEnabled) return "&cHệ thống độ kiếp đang tắt!";

        if (!data.isWaitingForTribulation()) {
            if (PlayerCultivationData.isTribulationLevel(data.getLevel())) {
                // Phục hồi trạng thái nếu bị lỗi
                data.setWaitingForTribulation(true);
            } else {
                return "&cBạn chưa cần độ kiếp!";
            }
        }

        if (data.isTribulationInProgress()) {
            return "&cBạn đang trong quá trình độ kiếp!";
        }

        // Kiểm tra có thể thấy bầu trời không
        if (!canSeeSky(player)) {
            return "&cBạn phải ở nơi có thể thấy bầu trời mới độ kiếp được!";
        }

        return null; // null = ok
    }

    /**
     * Kiểm tra player có thể thấy bầu trời không
     */
    private boolean canSeeSky(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return false;

        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int highestY = world.getHighestBlockYAt(x, z);
        int playerY = loc.getBlockY();

        // Player phải ở trên hoặc ngang mức block cao nhất (tức là thấy trời)
        return playerY >= highestY;
    }

    /**
     * Bắt đầu độ kiếp (gọi từ GUI hoặc lệnh)
     */
    public void startTribulation(Player player) {
        String error = canStartTribulation(player);
        if (error != null) {
            MessageUtils.send(player, error);
            return;
        }

        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        if (data == null) return;

        int level = data.getLevel();
        int strikes = level / 10; // level 10 → 1 strike, level 20 → 2 strikes, ...

        data.setTribulationInProgress(true);

        // Lưu session
        TribulationSession session = new TribulationSession(player.getUniqueId(), level, strikes);
        activeTribulations.put(player.getUniqueId(), session);

        // Broadcast thông báo
        String realmName = data.getRealmName();
        for (String msg : tribulationBroadcast) {
            MessageUtils.broadcast(msg
                    .replace("{player}", player.getName())
                    .replace("{strikes}", String.valueOf(strikes))
                    .replace("{realm}", realmName));
        }

        MessageUtils.send(player, "&6⚡ Chuẩn bị độ kiếp! Bạn có " + tribulationCountdown + " giây để chuẩn bị!");

        // Đếm ngược rồi bắt đầu đánh sét
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                cancelTribulation(player);
                return;
            }
            // Bắt đầu các đòn sét
            performTribulationStrikes(player, session, 0);
        }, tribulationCountdown * 20L);
    }

    /**
     * Thực hiện một đòn lôi kiếp
     */
    private void performTribulationStrikes(Player player, TribulationSession session, int strikeIndex) {
        if (!player.isOnline() || player.isDead()) {
            failTribulation(player);
            return;
        }

        // Kiểm tra session hợp lệ
        if (!activeTribulations.containsKey(player.getUniqueId())) return;

        int level = session.level;
        int totalStrikes = session.totalStrikes;
        int strikeNum = strikeIndex + 1;

        // Tính bán kính mở rộng dần theo cấp độ
        double radius = level * tribulationRadiusPerLevel;
        Location center = player.getLocation();

        // Tính sát thương: base * multiplier^(strikeNum-1)
        double damage = tribulationBaseDamage * Math.pow(tribulationDamageMultiplier, strikeNum - 1);

        // Hiệu ứng sét trực tiếp vào người chơi
        center.getWorld().strikeLightningEffect(center);

        // Sát thương bản thân người chơi
        player.damage(damage);

        // Tìm các thực thể trong bán kính
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (entity.equals(player)) continue;

            if (entity instanceof Player) {
                Player other = (Player) entity;
                // Gây sát thương cho người chơi khác
                other.damage(damage * 0.5, player);
                // Người chơi khác nhận exp
                double expReward = tribulationExpForOthers * strikeNum;
                addExperience(other, expReward);
                MessageUtils.send(other, "&e⚡ Bị lôi kiếp của &f" + player.getName() + " &eđánh trúng! Nhận &a+" + (int) expReward + " EXP");
            } else if (entity instanceof Monster) {
                // Gây sát thương cho quái vật
                ((LivingEntity) entity).damage(damage * 1.5, player);
            }
        }

        // Hiệu ứng nổ nhẹ (visual only)
        center.getWorld().createExplosion(center, 1.0f, false, false);

        // Thông báo
        MessageUtils.send(player, "&e⚡ Lôi kiếp &f" + strikeNum + "/" + totalStrikes + 
                " &e| Sát thương: &c" + String.format("%.1f", damage) +
                " &e| Bán kính: &f" + String.format("%.1f", radius) + "m");

        // Đánh dấu đã thực hiện đòn này
        session.strikesDone++;

        if (strikeNum < totalStrikes) {
            // Đòn tiếp theo sau interval
            int nextIndex = strikeIndex + 1;
            Bukkit.getScheduler().runTaskLater(plugin, () -> 
                performTribulationStrikes(player, session, nextIndex),
                tribulationStrikeInterval
            );
        } else {
            // Đã thực hiện xong tất cả đòn, kiểm tra kết quả
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!activeTribulations.containsKey(player.getUniqueId())) return;
                if (player.isOnline() && !player.isDead()) {
                    completeTribulation(player);
                } else {
                    failTribulation(player);
                }
            }, 5L);
        }
    }

    /**
     * Lôi kiếp thành công
     */
    private void completeTribulation(Player player) {
        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        if (data == null) return;

        String realmName = data.getRealmName();
        activeTribulations.remove(player.getUniqueId());

        // Gỡ trạng thái chờ
        data.setWaitingForTribulation(false);
        data.setTribulationInProgress(false);

        // Thông báo thành công
        for (String msg : tribulationSuccessMessage) {
            MessageUtils.broadcast(msg
                    .replace("{player}", player.getName())
                    .replace("{realm}", realmName));
        }

        MessageUtils.sendTitle(player,
                "&d&l✦ ĐỘ KIẾP THÀNH CÔNG ✦",
                "&fĐã vượt qua " + (data.getLevel() / 10) + " lần lôi kiếp!",
                10, 80, 10);

        // Thiết lập miễn dịch
        player.setNoDamageTicks(tribulationImmunityDuration * 20);

        // Thưởng exp
        addExperience(player, tribulationBaseDamage * 20);

        MessageUtils.playSound(player, Sound.BLOCK_BELL_USE);
        MessageUtils.send(player, "&a✦ Bạn có thể tiếp tục tu luyện lên cấp tiếp theo!");
    }

    /**
     * Lôi kiếp thất bại (public để CultivationListener gọi khi player chết)
     */
    public void failTribulation(Player player) {
        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        if (data == null) return;

        activeTribulations.remove(player.getUniqueId());
        data.setTribulationInProgress(false);

        // Trừ exp khiến tụt 1-2 level
        int levelsToDrop = tribulationLevelDropOnFail + (Math.random() < 0.5 ? 0 : 1);
        int oldLevel = data.getLevel();

        // Tính exp hiện tại và trừ tương ứng levels
        double expLost = 0;
        for (int i = 0; i < levelsToDrop; i++) {
            int lvl = data.getLevel() - i;
            if (lvl <= 1) break;
            expLost += lvl * expPerLevelMultiplier;
        }

        // Set exp về 0 và giảm level
        data.setExperience(0);
        int newLevel = Math.max(1, data.getLevel() - levelsToDrop);
        data.setLevel(newLevel);
        data.setMaxMana(calculateMaxMana(newLevel));

        // Nếu tụt xuống dưới threshold thì gỡ waiting
        data.setWaitingForTribulation(PlayerCultivationData.isTribulationLevel(newLevel));

        String realmName = data.getRealmName();

        // Thông báo thất bại
        for (String msg : tribulationFailMessage) {
            MessageUtils.broadcast(msg
                    .replace("{player}", player.getName())
                    .replace("{realm}", realmName));
        }

        MessageUtils.sendTitle(player,
                "&4&l✦ ĐỘ KIẾP THẤT BẠI ✦",
                "&cTu vi giảm từ cấp " + oldLevel + " xuống cấp " + newLevel,
                10, 80, 10);

        // Hồi máu để không chết thêm lần nữa
        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 10));
    }

    /**
     * Hủy độ kiếp (khi player disconnect)
     */
    private void cancelTribulation(Player player) {
        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        if (data != null) {
            data.setTribulationInProgress(false);
        }
        activeTribulations.remove(player.getUniqueId());
    }

    /**
     * Kiểm tra player có đang trong quá trình độ kiếp không
     */
    public boolean isInTribulation(Player player) {
        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        return data != null && data.isTribulationInProgress();
    }

    /**
     * Lấy tổng số đòn lôi kiếp cho level hiện tại
     */
    public int getTribulationStrikes(int level) {
        return level / 10;
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

    // ==================== INNER CLASSES ====================

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

    /**
     * Lưu trạng thái phiên độ kiếp
     */
    private static class TribulationSession {
        final UUID playerId;
        final int level;
        final int totalStrikes;
        int strikesDone;

        TribulationSession(UUID playerId, int level, int totalStrikes) {
            this.playerId = playerId;
            this.level = level;
            this.totalStrikes = totalStrikes;
            this.strikesDone = 0;
        }
    }

    public void reload() {
        loadConfig();
        loadData();
    }
}