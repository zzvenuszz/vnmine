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
    private int biomeCheckIntervalTicks;
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
    private final Map<Integer, FireRingConfig> fireRingByLevel = new LinkedHashMap<>();

    private boolean flyingItemsEnabled;
    private final Map<Integer, FlyingItemConfig> flyingItemsByLevel = new LinkedHashMap<>();

    // ==================== INNER CLASSES ====================

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

    private static class FireRingConfig {
        final Particle particle;
        final double radius;
        final int count;
        final double speed;
        final double yOffset;

        FireRingConfig(Particle particle, double radius, int count, double speed, double yOffset) {
            this.particle = particle;
            this.radius = radius;
            this.count = count;
            this.speed = speed;
            this.yOffset = yOffset;
        }
    }

    private static class FlyingItemConfig {
        final Material material;
        final int count;
        final double radius;
        final double yOffset;

        FlyingItemConfig(Material material, int count, double radius, double yOffset) {
            this.material = material;
            this.count = count;
            this.radius = radius;
            this.yOffset = yOffset;
        }
    }

    // ==================== CONSTRUCTOR ====================

    public MeditationConfig(VNMinePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    // ==================== LOAD ====================

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

        // Load particles by level
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

        // Load biome qi config
        ConfigurationSection biomeSection = cultivationConfig.getConfigurationSection("biome-qi");
        biomeCheckIntervalTicks = (biomeSection != null) ? biomeSection.getInt("check-interval-ticks", 100) : 100;

        // Load visuals
        ConfigurationSection visuals = section.getConfigurationSection("visuals");
        if (visuals != null) {
            loadFireRings(visuals);
            loadFlyingItems(visuals);
        } else {
            fireRingEnabled = false;
            setFireRingDefaults();
            flyingItemsEnabled = false;
            setFlyingItemDefaults();
        }
    }

    private void loadFireRings(ConfigurationSection visuals) {
        fireRingByLevel.clear();
        ConfigurationSection fr = visuals.getConfigurationSection("fire-rings");
        if (fr != null) {
            fireRingEnabled = fr.getBoolean("enabled", true);
            ConfigurationSection byLevel = fr.getConfigurationSection("by-level");
            if (byLevel != null) {
                for (String key : byLevel.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(key);
                        ConfigurationSection cfg = byLevel.getConfigurationSection(key);
                        if (cfg == null) continue;
                        String typeName = cfg.getString("particle", "FLAME");
                        Particle particle;
                        try {
                            particle = Particle.valueOf(typeName);
                        } catch (IllegalArgumentException ex) {
                            particle = Particle.FLAME;
                        }
                        double radius = cfg.getDouble("radius", 0.8);
                        int count = cfg.getInt("count", 8);
                        double speed = cfg.getDouble("speed", 0.02);
                        double yOffset = cfg.getDouble("y-offset", 0.2);
                        fireRingByLevel.put(level, new FireRingConfig(particle, radius, count, speed, yOffset));
                    } catch (NumberFormatException ignored) {}
                }
            }
            if (fireRingByLevel.isEmpty()) {
                setFireRingDefaults();
            }
        } else {
            fireRingEnabled = false;
            setFireRingDefaults();
        }
    }

    private void loadFlyingItems(ConfigurationSection visuals) {
        flyingItemsByLevel.clear();
        ConfigurationSection fi = visuals.getConfigurationSection("flying-items");
        if (fi != null) {
            flyingItemsEnabled = fi.getBoolean("enabled", true);
            ConfigurationSection byLevel = fi.getConfigurationSection("by-level");
            if (byLevel != null) {
                for (String key : byLevel.getKeys(false)) {
                    try {
                        int level = Integer.parseInt(key);
                        ConfigurationSection cfg = byLevel.getConfigurationSection(key);
                        if (cfg == null) continue;
                        String matName = cfg.getString("material", "BLAZE_ROD");
                        Material mat = Material.getMaterial(matName);
                        if (mat == null) mat = Material.BLAZE_ROD;
                        int count = cfg.getInt("count", 3);
                        double radius = cfg.getDouble("radius", 1.2);
                        double yOffset = cfg.getDouble("y-offset", 1.7);
                        flyingItemsByLevel.put(level, new FlyingItemConfig(mat, count, radius, yOffset));
                    } catch (NumberFormatException ignored) {}
                }
            }
            if (flyingItemsByLevel.isEmpty()) {
                setFlyingItemDefaults();
            }
        } else {
            flyingItemsEnabled = false;
            setFlyingItemDefaults();
        }
    }

    private void setFireRingDefaults() {
        fireRingByLevel.clear();
        fireRingByLevel.put(1, new FireRingConfig(Particle.FLAME, 0.8, 8, 0.02, 0.2));
    }

    private void setFlyingItemDefaults() {
        flyingItemsByLevel.clear();
        flyingItemsByLevel.put(1, new FlyingItemConfig(Material.BLAZE_ROD, 3, 1.2, 1.7));
    }

    // ==================== GETTERS (GENERAL) ====================

    public boolean isEnabled() { return enabled; }
    public int getPassiveExp() { return passiveExp; }
    public int getExpIntervalTicks() { return expIntervalTicks; }
    public int getSneakDurationTicks() { return sneakDurationTicks; }
    public int getBiomeCheckIntervalTicks() { return biomeCheckIntervalTicks; }
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

    // ==================== GETTERS (PARTICLES) ====================

    private ParticleConfig getParticleConfig(int level) {
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
        ParticleConfig cfg = getParticleConfig(level);
        return cfg != null ? cfg.type : Particle.WITCH;
    }

    public int getParticleCount(int level) {
        ParticleConfig cfg = getParticleConfig(level);
        return cfg != null ? cfg.count : 5;
    }

    public double getParticleOffset(int level) {
        ParticleConfig cfg = getParticleConfig(level);
        return cfg != null ? cfg.offset : 0.3;
    }

    public double getParticleSpeed(int level) {
        ParticleConfig cfg = getParticleConfig(level);
        return cfg != null ? cfg.speed : 0.02;
    }

    // ==================== GETTERS (FIRE RINGS) ====================

    public boolean isFireRingEnabled() { return fireRingEnabled; }

    private FireRingConfig getFireRingConfig(int level) {
        int best = Integer.MAX_VALUE;
        FireRingConfig cfg = null;
        for (Map.Entry<Integer, FireRingConfig> entry : fireRingByLevel.entrySet()) {
            if (entry.getKey() <= level && entry.getKey() < best) {
                best = entry.getKey();
                cfg = entry.getValue();
            }
        }
        return cfg;
    }

    public Particle getFireRingParticle(int level) {
        FireRingConfig cfg = getFireRingConfig(level);
        return cfg != null ? cfg.particle : Particle.FLAME;
    }

    public double getFireRingRadius(int level) {
        FireRingConfig cfg = getFireRingConfig(level);
        return cfg != null ? cfg.radius : 0.8;
    }

    public int getFireRingCount(int level) {
        FireRingConfig cfg = getFireRingConfig(level);
        return cfg != null ? cfg.count : 8;
    }

    public double getFireRingSpeed(int level) {
        FireRingConfig cfg = getFireRingConfig(level);
        return cfg != null ? cfg.speed : 0.02;
    }

    public double getFireRingYOffset(int level) {
        FireRingConfig cfg = getFireRingConfig(level);
        return cfg != null ? cfg.yOffset : 0.2;
    }

    // ==================== GETTERS (FLYING ITEMS) ====================

    public boolean isFlyingItemsEnabled() { return flyingItemsEnabled; }

    private FlyingItemConfig getFlyingItemConfig(int level) {
        int best = Integer.MAX_VALUE;
        FlyingItemConfig cfg = null;
        for (Map.Entry<Integer, FlyingItemConfig> entry : flyingItemsByLevel.entrySet()) {
            if (entry.getKey() <= level && entry.getKey() < best) {
                best = entry.getKey();
                cfg = entry.getValue();
            }
        }
        return cfg;
    }

    public Material getFlyingItemMaterial(int level) {
        FlyingItemConfig cfg = getFlyingItemConfig(level);
        return cfg != null ? cfg.material : Material.BLAZE_ROD;
    }

    public int getFlyingItemCount(int level) {
        FlyingItemConfig cfg = getFlyingItemConfig(level);
        return cfg != null ? cfg.count : 3;
    }

    public double getFlyingItemRadius(int level) {
        FlyingItemConfig cfg = getFlyingItemConfig(level);
        return cfg != null ? cfg.radius : 1.2;
    }

    public double getFlyingItemYOffset(int level) {
        FlyingItemConfig cfg = getFlyingItemConfig(level);
        return cfg != null ? cfg.yOffset : 1.7;
    }
}