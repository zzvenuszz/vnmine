package com.vnmine.skill;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.World;

/**
 * SkillEffects - Xử lý hiệu ứng particle và sound cho kỹ năng
 */
public class SkillEffects {

    /**
     * Phát hiệu ứng particle
     */
    public static void playParticle(Player player, String particleName, Location location, int count) {
        if (particleName == null || particleName.isEmpty()) return;
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            World world = location.getWorld();
            if (world != null) {
                world.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * Phát hiệu ứng particle tại vị trí cụ thể với offset
     */
    public static void playParticle(Player player, String particleName, Location location, 
                                     double offsetX, double offsetY, double offsetZ, 
                                     int count, double speed) {
        if (particleName == null || particleName.isEmpty()) return;
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            World world = location.getWorld();
            if (world != null) {
                world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * Phát âm thanh
     */
    public static void playSound(Player player, String soundName) {
        if (soundName == null || soundName.isEmpty()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * Phát âm thanh tại vị trí
     */
    public static void playSound(Location location, String soundName, float volume, float pitch) {
        if (soundName == null || soundName.isEmpty()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            World world = location.getWorld();
            if (world != null) {
                world.playSound(location, sound, volume, pitch);
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * Hiệu ứng theo grade của skill
     */
    public static void playGradeEffect(Player player, SkillGrade grade) {
        switch (grade) {
            case HOANG:
                playParticle(player, "SPELL", player.getLocation(), 10);
                playSound(player, "BLOCK_NOTE_BLOCK_PLING");
                break;
            case HUYEN:
                playParticle(player, "SPELL_MOB", player.getEyeLocation(), 15);
                playSound(player, "BLOCK_NOTE_BLOCK_CHIME");
                break;
            case DIA:
                playParticle(player, "ENCHANTMENT_TABLE", player.getEyeLocation(), 20);
                playSound(player, "BLOCK_BEACON_POWER_SELECT");
                break;
            case THIEN:
                playParticle(player, "END_ROD", player.getEyeLocation(), 30);
                playParticle(player, "FIREWORKS_SPARK", player.getLocation().add(0, 2, 0), 20);
                playSound(player, "ENTITY_ENDER_DRAGON_GROWL");
                break;
        }
    }

    /**
     * Tạo hình tròn particle quanh người chơi
     */
    public static void playCircleEffect(Player player, String particleName, double radius, int count) {
        if (particleName == null || particleName.isEmpty()) return;
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            Location center = player.getLocation();
            World world = center.getWorld();
            if (world == null) return;

            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                Location loc = center.clone().add(x, 0.5, z);
                world.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
            }
        } catch (IllegalArgumentException ignored) {
        }
    }
}