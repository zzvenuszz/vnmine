package com.vnmine.cultivation;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class MeditationConfig {

    private final VNMinePlugin plugin;
    private File cultivationFile;
    private FileConfiguration cultivationConfig;

    private boolean enabled;
    private int passiveExp;
    private int expIntervalTicks;
    private int sneakDurationTicks;
    private boolean cancelOnDamage;
    private boolean cancelOnMove;
    private boolean cancelOnInteract;
    private String activationGuiTitle;
    private String activationGuiConfirm;
    private String activationGuiCancel;
    private String messageStart;
    private String messageStop;
    private String messageAlreadyMeditating;
    private String messagePermission;
    private double sitOffset;

    private final Map<Integer, ParticleConfig> particleByLevelStart = new LinkedHashMap<>();

    private boolean fireRingEnabled;
    private Particle fireRingParticle;
    private double fireRingRadius;
    private int fireRingCount;
    private double fireRingSpeed;
    private double fireRingYOffset;

    private boolean flyingItemsEnabled;
    private double flyingItemsYOffset;
    private double flyingItemsRadius;
    private final Map<Integer, Material> flyingItemsByLevel = new LinkedHashMap<>();

    private static class ParticleConfig {
        final Particle type;
        final int count;
        final double offset;
        final double speed;

        ParticleConfig(Particle type, int count, double offset, double speed) {
            this.type = type;
            this.count = count;
            this.offset = offset;
            this.speed = speed;
        }
    }

    public MeditationConfig(VNMinePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        cultivationFile = new File(plugin.getDataFolder(), "cultivation.yml");
        if (!cultivationFile.exists()) {
            plugin.saveResource("cultivation.yml", false);
        }
        cultivationConfig = YamlConfiguration.loadConfiguration(cultivationFile);

        ConfigurationSection section = cultivationConfig.getConfigurationSection("meditation");
        if (section == null) {
            enabled = false;
            return;
        }

        enabled = section.getBoolean("enabled", true);
        passiveExp = section.getInt("passive-exp", 8);
        expIntervalTicks = section.getInt("exp-interval-ticks", 100);
        sneakDurationTicks = section.getInt("sneak-duration-ticks", 200);
        cancelOnDamage = section.getBoolean("cancel-on-damage", true);
        cancelOnMove = section.getBoolean("cancel-on-move", true);
        cancelOnInteract = section.getBoolean("cancel-on-interact", true);
        sitOffset = section.getDouble("sit-offset", 0.4);
        activationGuiTitle = ColorUtils.colorize(section.getString("activation-gui-title", "&6&l✧ Tọa Thiền ✧"));
        activationGuiConfirm = ColorUtils.colorize(section.getString("activation-gui-confirm", "&a[XÁC NHẬN] Bước vào trạng thái Tọa Thiền"));
        activationGuiCancel = ColorUtils.colorize(section.getString("activation-gui-cancel", "&c[HỦY BỎ] Không muốn ngồi thiền"));

        ConfigurationSection msg = section.getConfigurationSection("message");
        if (msg != null) {
            messageStart = ColorUtils.colorize(msg.getString("start", "&d✧ Bạn bắt đầu tọa thiền..."));
            messageStop = ColorUtils.colorize(msg.getString("stop", "&7Bạn đã dừng tọa thiền."));
            messageAlreadyMeditating = ColorUtils.colorize(msg.getString("already-meditating", "&cBạn đang trong trạng thái tọa thiền rồi!"));
            messagePermission = ColorUtils.colorize(msg.getString("permission", "&cBạn chưa đủ tu vi để tọa thiền."));
        } else {
            messageStart = ColorUtils.colorize("&d✧ Bạn bắt đầu tọa thiền...");
            messageStop = ColorUtils.colorize("&7Bạn đã dừng tọa thiền.");
            messageAlreadyMeditating = ColorUtils.colorize("&cBạn đang trong trạng thái tọa thiền rồi!");
            messagePermission = ColorUtils.colorize("&cBạn chưa đủ tu vi để tọa thiền.");
        }

        particleByLevelStart.clear();
        ConfigurationSection particles = section.getConfigurationSection("particles");
        if (particles != null) {
            for (String key : particles.getKeys(false)) {
                try {
                    int level = Integer.parseInt(key);
                    ConfigurationSection p = particles.getConfigurationSection(key);
                    if (p == null) continue;
                    String typeName = p.getString("type", "WITCH");
                    Particle type;
                    try {
                        type = Particle.valueOf(typeName);
                    } catch (IllegalArgumentException ex) {
                        type = Particle.WITCH;
                    }
                    int count = p.getInt("count", 5);
                    double offset = p.getDouble("offset", 0.3);
                    double speed = p.getDouble("speed", 0.02);
                    particleByLevelStart.put(level, new ParticleConfig(type, count, offset, speed));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        ConfigurationSection visuals = section.getConfigurationSection("visuals");
        if (visuals != null) {
            ConfigurationSection fr = visuals.getConfigurationSection("fire-rings");
            if (fr != null) {
                fireRingEnabled = fr.getBoolean("enabled", true);
                String frTypeName = fr.getString("particle", "FLAME");
                try {
                    fireRingParticle = Particle.valueOf(frTypeName);
                } catch (IllegalArgumentException ex) {
                    fireRingParticle = Particle.FLAME;
                }
                fireRingRadius = fr.getDouble("radius", 0.8);
                fireRingCount = fr.getInt("count", 8);
                fireRingSpeed = fr.getDouble("speed", 0.02);
                fireRingYOffset = fr.getDouble("y-offset", 0.2);
            } else {
                setFireRingDefaults();
            }

            ConfigurationSection fi = visuals.getConfigurationSection("flying-items");
            if (fi != null) {
                flyingItemsEnabled = fi.getBoolean("enabled", true);
                flyingItemsYOffset = fi.getDouble("y-offset", 1.7);
                flyingItemsRadius = fi.getDouble("radius", 1.2);
                flyingItemsByLevel.clear();
                ConfigurationSection byLevel = fi.getConfigurationSection("by-level");
                if (byLevel != null) {
                    for (String key : byLevel.getKeys(false)) {
                        try {
                            int level = Integer.parseInt(key);
                            ConfigurationSection itemCfg = byLevel.getConfigurationSection(key);
                            if (itemCfg == null) continue;
                            String matName = itemCfg.getString("material", "BLAZE_ROD");
                            Material mat = Material.getMaterial(matName);
                            if (mat != null) flyingItemsByLevel.put(level, mat);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (flyingItemsByLevel.isEmpty()) setFlyingItemDefaults();
            } else { flyingItemsEnabled = false; setFlyingItemDefaults(); }
        } else {
            fireRingEnabled = false; setFireRingDefaults();
            flyingItemsEnabled = false; setFlyingItemDefaults();
        }
    }

    private void setFireRingDefaults() {
        fireRingEnabled = true;
        fireRingParticle = Particle.FLAME;
        fireRingRadius = 0.8;
        fireRingCount = 8;
        fireRingSpeed = 0.02;
        fireRingYOffset = 0.2;
    }

    private void setFlyingItemDefaults() {
        flyingItemsByLevel.clear();
        flyingItemsByLevel.put(1, Material.BLAZE_ROD);
        flyingItemsByLevel.put(41, Material.NETHER_STAR);
        flyingItemsByLevel.put(71, Material.END_CRYSTAL);
        flyingItemsByLevel.put(91, Material.DIAMOND_SWORD);
    }

    // ==================== GETTERS ====================

    public boolean isEnabled() { return enabled; }
    public int getPassiveExp() { return passiveExp; }
    public int getExpIntervalTicks() { return expIntervalTicks; }
    public int getSneakDurationTicks() { return sneakDurationTicks; }
    public boolean isCancelOnDamage() { return cancelOnDamage; }
    public boolean isCancelOnMove() { return cancelOnMove; }
    public boolean isCancelOnInteract() { return cancelOnInteract; }
    public double getSitOffset() { return sitOffset; }
    public String getActivationGuiTitle() { return activationGuiTitle; }
    public String getActivationGuiConfirm() { return activationGuiConfirm; }
    public String getActivationGuiCancel() { return activationGuiCancel; }
    public String getMessageStart() { return messageStart; }
    public String getMessageStop() { return messageStop; }
    public String getMessageAlreadyMeditating() { return messageAlreadyMeditating; }
    public String getMessagePermission() { return messagePermission; }

    private ParticleConfig getConfig(int level) {
        int best = Integer.MAX_VALUE;
        ParticleConfig cfg = null;
        for (Map.Entry<Integer, ParticleConfig> entry : particleByLevelStart.entrySet()) {
            if (entry.getKey() <= level && entry.getKey() < best) {
                best = entry.getKey();
                cfg = entry.getValue();
            }
        }
        return cfg;
    }

    public Particle getParticleType(int level) {
        ParticleConfig cfg = getConfig(level);
        return cfg != null ? cfg.type : Particle.WITCH;
    }

    public int getParticleCount(int level) {
        ParticleConfig cfg = getConfig(level);
        return cfg != null ? cfg.count : 5;
    }

    public double getParticleOffset(int level) {
        ParticleConfig cfg = getConfig(level);
        return cfg != null ? cfg.offset : 0.3;
    }

    public double getParticleSpeed(int level) {
        ParticleConfig cfg = getConfig(level);
        return cfg != null ? cfg.speed : 0.02;
    }

    public boolean isFireRingEnabled() { return fireRingEnabled; }
    public Particle getFireRingParticle() { return fireRingParticle; }
    public double getFireRingRadius() { return fireRingRadius; }
    public int getFireRingCount() { return fireRingCount; }
    public double getFireRingSpeed() { return fireRingSpeed; }
    public double getFireRingYOffset() { return fireRingYOffset; }

    public boolean isFlyingItemsEnabled() { return flyingItemsEnabled; }
    public double getFlyingItemsYOffset() { return flyingItemsYOffset; }
    public double getFlyingItemsRadius() { return flyingItemsRadius; }

    public Material getFlyingItemMaterial(int level) {
        int best = Integer.MAX_VALUE;
        Material mat = Material.BLAZE_ROD;
        for (Map.Entry<Integer, Material> entry : flyingItemsByLevel.entrySet()) {
            if (entry.getKey() <= level && entry.getKey() < best) {
                best = entry.getKey();
                mat = entry.getValue();
            }
        }
        return mat;
    }
}
