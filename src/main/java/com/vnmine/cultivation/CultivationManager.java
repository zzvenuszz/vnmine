package com.vnmine.cultivation;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import com.vnmine.util.NameTagManager;
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
    private final NameTagManager nameTagManager;
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

    // Cấu hình damage mới
    private double tribulationDamagePerLevel;      // Sát thương cơ bản mỗi level
    private double tribulationDamageArmorReduction; // Giảm sát thương từ giáp (0.0-1.0)

    // Hằng số thời gian lôi kiếp
    private static final int TOTAL_WAVES = 3;              // LUÔN 3 đợt thiên kiếp
    private static final int WAVE_DURATION_SECONDS = 3;     // Mỗi đợt kéo dài 3 giây
    private static final int REST_DURATION_SECONDS = 5;     // Nghỉ 5 giây giữa các đợt (có thể dùng thuốc/skill hồi)
    private static final int TICKS_PER_SECOND = 20;

    // Theo dõi các phiên độ kiếp đang diễn ra
    private final Map<UUID, TribulationSession> activeTribulations = new ConcurrentHashMap<>();

    // File lưu trữ
    private File dataFile;
    private FileConfiguration dataConfig;

    public CultivationManager(VNMinePlugin plugin, NameTagManager nameTagManager) {
        this.plugin = plugin;
        this.nameTagManager = nameTagManager;
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
            tribulationDamagePerLevel = tribSection.getDouble("damage.damage-per-level", 1.0);
            tribulationDamageArmorReduction = tribSection.getDouble("damage.armor-reduction", 0.6);
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
            tribulationDamagePerLevel = 1.0;
            tribulationDamageArmorReduction = 0.6;
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

        int currentLevel = data.getLevel();

        // Nếu level hiện tại là ngưỡng độ kiếp (9, 19, 29...) và sắp đầy exp
        if (PlayerCultivationData.isTribulationLevel(currentLevel)) {
            double newExp = data.getExperience() + amount;
            double maxExp = data.getMaxExperience();
            if (newExp >= maxExp) {
                // Chặn ở ngưỡng, không cho lên cấp
                data.setExperience(maxExp);
                data.setWaitingForTribulation(true);

                // Thông báo yêu cầu độ kiếp
                MessageUtils.send(player, "&6⚡ &lBẠN CẦN ĐỘ KIẾP! ⚡");
                MessageUtils.send(player, "&eCấp " + currentLevel + " là ngưỡng đột phá đại cảnh giới!");
                MessageUtils.send(player, "&eHãy dùng &b/vn &emở menu, chọn &bThông Tin &evà bấm &cĐộ Kiếp&e!");
                MessageUtils.send(player, "&cLưu ý: Phải ở nơi có thể thấy bầu trời mới độ kiếp được!");

                // Cập nhật action bar
                MessageUtils.sendActionBar(player,
                        "&c⚡ ĐÃ ĐẠT NGƯỠNG! HÃY ĐỘ KIẾP ĐỂ ĐỘT PHÁ!");
                return;
            }
        }

        if (data.addExperience(amount)) {
            // Level up! Kiểm tra xem có cần độ kiếp không
            handleLevelUp(player, data);
        }
    }

    /**
     * Xử lý khi lên cấp (chỉ gọi khi level tăng tự nhiên, không phải ngưỡng độ kiếp)
     */
    private void handleLevelUp(Player player, PlayerCultivationData data) {
        int level = data.getLevel();
        String realmPrefix = data.getRealmPrefix();

        MessageUtils.sendTitle(player,
                "&a&l✦ THĂNG CẤP ✦",
                "&fCấp " + level + " - " + realmPrefix + "&f]",
                10, 60, 10);

        MessageUtils.broadcast(
                "&d✦ &l" + player.getName() + " &r&dđã đột phá lên &e" + realmPrefix + "&r&e] &d✦",
                Sound.ENTITY_PLAYER_LEVELUP
        );

        // Cập nhật name tag với prefix mới
        nameTagManager.updateNameTag(player);
    }

    // ==================== TRIBULATION SYSTEM ====================

    /**
     * Kiểm tra player có thể độ kiếp không
     */
    public String canStartTribulation(Player player) {
        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        if (data == null) return "&cBạn chưa bắt đầu tu luyện! Dùng /vn start";

        if (!tribulationEnabled) return "&cHệ thống độ kiếp đang tắt!";

        // Kiểm tra: player đang ở level 9, 19, 29... và exp đầy
        int level = data.getLevel();
        if (!PlayerCultivationData.isTribulationLevel(level)) {
            return "&cBạn chưa cần độ kiếp! (Cần ở cấp 9, 19, 29, ...)";
        }

        if (!data.isWaitingForTribulation()) {
            // Nếu đã đạt level ngưỡng nhưng chưa set waiting (do data cũ), tự động set
            if (data.getExperience() >= data.getMaxExperience()) {
                data.setWaitingForTribulation(true);
            } else {
                return "&cBạn chưa đạt đủ tu vi để độ kiếp! (Cần đầy exp)";
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

        data.setTribulationInProgress(true);

        // Lưu session - LUÔN 3 đợt
        TribulationSession session = new TribulationSession(player.getUniqueId(), level, TOTAL_WAVES);
        activeTribulations.put(player.getUniqueId(), session);

        // Broadcast thông báo
        String realmName = data.getRealmName();
        for (String msg : tribulationBroadcast) {
            MessageUtils.broadcast(msg
                    .replace("{player}", player.getName())
                    .replace("{strikes}", String.valueOf(TOTAL_WAVES))
                    .replace("{realm}", realmName));
        }

        MessageUtils.send(player, "&6⚡ Chuẩn bị độ kiếp! Bạn có " + tribulationCountdown + " giây để chuẩn bị!");

        // Đếm ngược rồi bắt đầu đợt sét đầu tiên
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                cancelTribulation(player);
                return;
            }
            // Bắt đầu đợt sét đầu tiên
            startTribulationWave(player, session, 0);
        }, tribulationCountdown * 20L);
    }

    /**
     * Bắt đầu một đợt lôi kiếp
     */
    private void startTribulationWave(Player player, TribulationSession session, int waveIndex) {
        if (!player.isOnline() || player.isDead()) {
            failTribulation(player);
            return;
        }

        if (!activeTribulations.containsKey(player.getUniqueId())) return;

        session.currentWave = waveIndex;
        session.currentStrikeInWave = 0;

        MessageUtils.send(player, "&4⚡ &lĐỢT LÔI KIẾP " + (waveIndex + 1) + "/" + session.totalWaves + " &4⚡");
        MessageUtils.send(player, "&cChuẩn bị chịu đòn! (3 giây)");

        // Bắt đầu từng giây trong đợt
        executeWaveSecond(player, session, 0);
    }

    /**
     * Tính sát thương lôi kiếp (cân bằng để full giáp sắt sống được)
     * Công thức: baseDamage + (level * damagePerLevel) + (waveIndex * waveBonus)
     * với armorReduction giảm trừ dựa trên chất liệu giáp
     */
    private double calculateTribulationDamage(Player player, int level, int waveIndex, int secondIndex) {
        // Sát thương cơ bản
        double baseDamage = tribulationBaseDamage;
        
        // Sát thương tăng theo level
        double levelDamage = level * tribulationDamagePerLevel;
        
        // Sát thương tăng theo đợt (đợt sau mạnh hơn)
        double waveBonus = waveIndex * 3.0;
        
        // Sát thương tích lũy theo giây trong đợt
        double secondMultiplier = 1.0 + (secondIndex * 0.3);
        
        double rawDamage = (baseDamage + levelDamage + waveBonus) * secondMultiplier;
        
        // Giảm sát thương dựa trên giáp của player (simulate armor protection)
        double armorReduction = getArmorReduction(player);
        
        return rawDamage * (1.0 - armorReduction);
    }

    /**
     * Tính % giảm sát thương dựa trên giáp đang mặc
     * Tay trần: 0%, Giáp sắt full: 60%, Giáp kim cương full: 80%
     */
    private double getArmorReduction(Player player) {
        org.bukkit.inventory.ItemStack helmet = player.getInventory().getHelmet();
        org.bukkit.inventory.ItemStack chestplate = player.getInventory().getChestplate();
        org.bukkit.inventory.ItemStack leggings = player.getInventory().getLeggings();
        org.bukkit.inventory.ItemStack boots = player.getInventory().getBoots();

        int pieces = 0;
        int armorLevel = 0; // 0=none, 1=leather, 2=iron, 3=diamond, 4=netherite

        for (org.bukkit.inventory.ItemStack piece : new org.bukkit.inventory.ItemStack[]{helmet, chestplate, leggings, boots}) {
            if (piece != null && piece.getType() != org.bukkit.Material.AIR) {
                pieces++;
                String matName = piece.getType().name();
                if (matName.contains("NETHERITE")) {
                    armorLevel = Math.max(armorLevel, 4);
                } else if (matName.contains("DIAMOND")) {
                    armorLevel = Math.max(armorLevel, 3);
                } else if (matName.contains("IRON") || matName.contains("CHAINMAIL")) {
                    armorLevel = Math.max(armorLevel, 2);
                } else if (matName.contains("LEATHER") || matName.contains("GOLD")) {
                    armorLevel = Math.max(armorLevel, 1);
                }
            }
        }

        // Không full set → giảm hiệu quả
        double setBonus = (pieces / 4.0);

        // Không mặc giáp
        if (pieces == 0) return 0.0;

        // Giáp thấp nhất (leather/gold): 20% * setBonus
        // Giáp sắt: 40% * setBonus
        // Giáp kim cương: 60% * setBonus
        // Giáp netherite: 70% * setBonus
        double baseReduction;
        switch (armorLevel) {
            case 1: baseReduction = 0.20; break; // leather/gold
            case 2: baseReduction = 0.45; break; // iron/chainmail
            case 3: baseReduction = 0.65; break; // diamond
            case 4: baseReduction = 0.75; break; // netherite
            default: baseReduction = 0.0;
        }

        return baseReduction * setBonus;
    }

    /**
     * Thực thi một giây trong đợt lôi kiếp
     */
    private void executeWaveSecond(Player player, TribulationSession session, int secondIndex) {
        if (!player.isOnline() || player.isDead()) {
            failTribulation(player);
            return;
        }

        if (!activeTribulations.containsKey(player.getUniqueId())) return;

        int level = session.level;
        int waveIndex = session.currentWave;

        // Tính sát thương với công thức cân bằng
        double damage = calculateTribulationDamage(player, level, waveIndex, secondIndex);

        // 1. Tia sét CHÍNH đánh thẳng vào player
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.damage(damage);

        // 2. Tia sét PHỤ đánh xung quanh
        double radius = level * tribulationRadiusPerLevel;
        int secondaryStrikes = (level / 5) + 1 + waveIndex; // Càng cao level càng nhiều tia phụ

        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

        // Lọc các entity ưu tiên: Player > Monster > Animals
        List<Entity> nearbyPlayers = new ArrayList<>();
        List<Entity> nearbyMonsters = new ArrayList<>();
        List<Entity> nearbyAnimals = new ArrayList<>();

        for (Entity entity : nearbyEntities) {
            if (entity.equals(player)) continue;
            if (entity instanceof Player) {
                nearbyPlayers.add(entity);
            } else if (entity instanceof Monster) {
                nearbyMonsters.add(entity);
            } else if (entity instanceof LivingEntity) {
                nearbyAnimals.add(entity);
            }
        }

        // Gộp theo thứ tự ưu tiên: player > monster > animal
        List<Entity> priorityEntities = new ArrayList<>();
        priorityEntities.addAll(nearbyPlayers);
        priorityEntities.addAll(nearbyMonsters);
        priorityEntities.addAll(nearbyAnimals);

        // Chọn ngẫu nhiên secondaryStrikes entity để đánh
        Random random = new Random();
        for (int i = 0; i < secondaryStrikes && !priorityEntities.isEmpty(); i++) {
            int idx = random.nextInt(priorityEntities.size());
            Entity target = priorityEntities.remove(idx);

            if (target.isDead()) continue;

            // Đánh sét vào entity
            target.getWorld().strikeLightningEffect(target.getLocation());

            if (target instanceof Player) {
                Player other = (Player) target;
                // Sát thương phụ cho player khác
                other.damage(damage * 0.5, player);
                // Thưởng exp cho player khác
                double expReward = tribulationExpForOthers * (waveIndex + 1);
                addExperience(other, expReward);
                MessageUtils.send(other, "&e⚡ Bị lôi kiếp của &f" + player.getName() + " &eđánh trúng! Nhận &a+" + (int) expReward + " EXP");
            } else if (target instanceof LivingEntity) {
                // Sát thương cho quái/thú
                ((LivingEntity) target).damage(damage * 1.5, player);
            }
        }

        // Hiệu ứng nổ nhẹ tại vị trí player
        player.getWorld().createExplosion(player.getLocation(), 0.5f, false, false);

        // Thông báo
        MessageUtils.send(player, "&e⚡ Lôi kiếp đợt " + (waveIndex + 1) + "/" + session.totalWaves +
                " | Giây " + (secondIndex + 1) + "/3" +
                " | Sát thương: &c" + String.format("%.1f", damage) +
                " | &f" + secondaryStrikes + " tia phụ");

        session.currentStrikeInWave = secondIndex + 1;

        if (secondIndex < WAVE_DURATION_SECONDS - 1) {
            // Còn giây tiếp theo trong đợt
            int nextSecond = secondIndex + 1;
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                            executeWaveSecond(player, session, nextSecond),
                    TICKS_PER_SECOND
            );
        } else {
            // Đã kết thúc đợt này
            int nextWave = waveIndex + 1;
            if (nextWave < session.totalWaves) {
                // Còn đợt tiếp theo → nghỉ giữa giờ
                startRestPeriod(player, session, nextWave);
            } else {
                // Đã xong tất cả đợt → thành công
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
    }

    /**
     * Thời gian nghỉ giữa các đợt (5 giây - có thể dùng thuốc/skill)
     */
    private void startRestPeriod(Player player, TribulationSession session, int nextWave) {
        if (!player.isOnline()) {
            cancelTribulation(player);
            return;
        }

        MessageUtils.send(player, "&a✦ Đợt " + (session.currentWave + 1) + "/" + session.totalWaves + " đã qua!");
        MessageUtils.send(player, "&a⏳ Đợt tiếp theo sau " + REST_DURATION_SECONDS + " giây...");
        MessageUtils.send(player, "&e💊 Hãy dùng thuốc hoặc skill hồi máu ngay!");

        // Đếm ngược trên action bar
        final int[] countdown = {REST_DURATION_SECONDS};
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!player.isOnline() || !activeTribulations.containsKey(player.getUniqueId())) {
                return;
            }
            int remaining = countdown[0];
            if (remaining > 0) {
                MessageUtils.sendActionBar(player,
                        "&e⏳ Đợt " + (nextWave + 1) + "/" + session.totalWaves + " sau " + remaining + " giây... &cHãy hồi máu gấp!");
                countdown[0]--;
            }
        }, 0L, 20L);

        // Sau REST_DURATION_SECONDS giây, bắt đầu đợt tiếp theo
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getScheduler().cancelTask(taskId);
            if (!player.isOnline()) {
                cancelTribulation(player);
                return;
            }
            if (!activeTribulations.containsKey(player.getUniqueId())) return;
            startTribulationWave(player, session, nextWave);
        }, REST_DURATION_SECONDS * 20L);
    }

    /**
     * Lôi kiếp thành công
     */
    private void completeTribulation(Player player) {
        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        if (data == null) return;

        // Kiểm tra session hợp lệ
        if (!activeTribulations.containsKey(player.getUniqueId())) return;
        TribulationSession session = activeTribulations.get(player.getUniqueId());

        int oldLevel = data.getLevel();
        String oldRealmName = data.getRealmName();

        // Tăng level lên 1 (từ 9 → 10, 19 → 20, ...)
        int newLevel = oldLevel + 1;
        data.setLevel(newLevel);
        data.setExperience(0);
        data.setMaxExperience(newLevel * 100.0);
        data.setMaxMana(calculateMaxMana(newLevel));
        data.setMana(data.getMaxMana());

        // Lấy tên cảnh giới mới
        String newRealmName = data.getRealmName();
        String newRealmPrefix = data.getRealmPrefix();

        // Xóa session
        activeTribulations.remove(player.getUniqueId());

        // Gỡ trạng thái chờ
        data.setWaitingForTribulation(false);
        data.setTribulationInProgress(false);

        // Cập nhật name tag với cảnh giới mới
        nameTagManager.updateNameTag(player);

        // Thông báo thành công
        for (String msg : tribulationSuccessMessage) {
            MessageUtils.broadcast(msg
                    .replace("{player}", player.getName())
                    .replace("{realm}", newRealmName));
        }

        MessageUtils.sendTitle(player,
                "&d&l✦ ĐỘ KIẾP THÀNH CÔNG ✦",
                "&f" + oldRealmName + " → " + newRealmPrefix + "&r&f]",
                10, 80, 10);

        // Thiết lập miễn dịch
        player.setNoDamageTicks(tribulationImmunityDuration * 20);

        MessageUtils.playSound(player, Sound.BLOCK_BELL_USE);
        MessageUtils.send(player, "&a✦ Bạn đã đột phá lên &e" + newRealmPrefix + "&r&e] &a(Cấp " + newLevel + ")");
        MessageUtils.send(player, "&a✦ Hãy tiếp tục tu luyện lên cấp tiếp theo!");
    }

    /**
     * Lôi kiếp thất bại (public để CultivationListener gọi khi player chết)
     */
    public void failTribulation(Player player) {
        // Chống gọi 2 lần
        if (!activeTribulations.containsKey(player.getUniqueId())) return;

        PlayerCultivationData data = getPlayerData(player.getUniqueId());
        if (data == null) return;

        activeTribulations.remove(player.getUniqueId());
        data.setTribulationInProgress(false);

        int level = data.getLevel();

        // Tính lượng exp tương đương 2 level: (level * expPerLevel) + ((level-1) * expPerLevel)
        double expLevel1 = level * expPerLevelMultiplier;
        double expLevel2 = (level - 1) * expPerLevelMultiplier;
        if (level <= 1) expLevel2 = 0;
        double expLost = expLevel1 + expLevel2;

        // Trừ exp, nhưng không giảm level
        double currentExp = data.getExperience();
        data.setExperience(Math.max(0, currentExp - expLost));

        // Nếu exp xuống dưới ngưỡng max, cho phép tu luyện lại (gỡ waiting)
        if (data.getExperience() < data.getMaxExperience()) {
            data.setWaitingForTribulation(false);
        }

        String realmName = data.getRealmName();

        // Thông báo thất bại
        for (String msg : tribulationFailMessage) {
            MessageUtils.broadcast(msg
                    .replace("{player}", player.getName())
                    .replace("{realm}", realmName));
        }

        MessageUtils.sendTitle(player,
                "&4&l✦ ĐỘ KIẾP THẤT BẠI ✦",
                "&cMất " + (int) expLost + " EXP! Hãy tu luyện thêm!",
                10, 80, 10);

        // Cập nhật name tag
        nameTagManager.updateNameTag(player);
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
     * Lấy tổng số đợt lôi kiếp cho level hiện tại
     */
    public int getTribulationWaves(int level) {
        return TOTAL_WAVES; // Luôn 3 đợt
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
        if (levelInRealm >= 10) {
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
        if (levelInRealm >= 10) {
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
    public NameTagManager getNameTagManager() { return nameTagManager; }

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
    public static class TribulationSession {
        final UUID playerId;
        final int level;
        final int totalWaves;
        int currentWave;
        int currentStrikeInWave;

        TribulationSession(UUID playerId, int level, int totalWaves) {
            this.playerId = playerId;
            this.level = level;
            this.totalWaves = totalWaves;
            this.currentWave = 0;
            this.currentStrikeInWave = 0;
        }
    }

    public void reload() {
        loadConfig();
        loadData();
    }
}