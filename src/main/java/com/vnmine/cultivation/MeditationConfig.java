package com.vnmine.cultivation;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import java.util.LinkedHashMap;
import java.util.Map;

public class MeditationConfig {

    private final VNMinePlugin plugin;

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

    private final Map<Integer, ParticleConfig> particleByLevelStart = new LinkedHashMap<>();

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
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("cultivation.meditation");
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
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getPassiveExp() {
        return passiveExp;
    }

    public int getExpIntervalTicks() {
        return expIntervalTicks;
    }

    public int getSneakDurationTicks() {
        return sneakDurationTicks;
    }

    public boolean isCancelOnDamage() {
        return cancelOnDamage;
    }

    public boolean isCancelOnMove() {
        return cancelOnMove;
    }

    public boolean isCancelOnInteract() {
        return cancelOnInteract;
    }

    public String getActivationGuiTitle() {
        return activationGuiTitle;
    }

    public String getActivationGuiConfirm() {
        return activationGuiConfirm;
    }

    public String getActivationGuiCancel() {
        return activationGuiCancel;
    }

    public String getMessageStart() {
        return messageStart;
    }

    public String getMessageStop() {
        return messageStop;
    }

    public String getMessageAlreadyMeditating() {
        return messageAlreadyMeditating;
    }

    public String getMessagePermission() {
        return messagePermission;
    }

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
}
