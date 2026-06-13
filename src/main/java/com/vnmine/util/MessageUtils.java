package com.vnmine.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;

/**
 * MessageUtils - Gửi thông báo toàn server, hiệu ứng, âm thanh
 */
public class MessageUtils {

    // ==================== BROADCAST ====================

    /**
     * Gửi thông báo toàn server
     */
    public static void broadcast(String message) {
        if (message == null || message.isEmpty()) return;
        Bukkit.broadcast(ColorUtils.toComponent(message));
    }

    /**
     * Gửi nhiều dòng thông báo
     */
    public static void broadcast(List<String> messages) {
        if (messages == null) return;
        for (String msg : messages) {
            broadcast(msg);
        }
    }

    /**
     * Gửi thông báo với sound
     */
    public static void broadcast(String message, Sound sound) {
        broadcast(message);
        if (sound != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }

    /**
     * Gửi thông báo kèm title
     */
    public static void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component titleComponent = ColorUtils.toComponent(title);
        Component subtitleComponent = ColorUtils.toComponent(subtitle);
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );
        Title t = Title.title(titleComponent, subtitleComponent, times);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(t);
        }
    }

    // ==================== PLAYER-SPECIFIC ====================

    /**
     * Gửi tin nhắn cho player
     */
    public static void send(Player player, String message) {
        if (player == null || !player.isOnline() || message == null) return;
        player.sendMessage(ColorUtils.toComponent(message));
    }

    /**
     * Gửi tin nhắn cho CommandSender (Player hoặc Console)
     */
    public static void send(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        if (sender instanceof Player) {
            send((Player) sender, message);
        } else {
            sender.sendMessage(ColorUtils.stripColor(message));
        }
    }

    /**
     * Gửi nhiều dòng cho player
     */
    public static void send(Player player, List<String> messages) {
        if (player == null || !player.isOnline() || messages == null) return;
        for (String msg : messages) {
            send(player, msg);
        }
    }

    /**
     * Gửi title cho player
     */
    public static void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 40, 10);
    }

    /**
     * Gửi title với thời gian tùy chỉnh
     */
    public static void sendTitle(Player player, String title, String subtitle,
                                  int fadeIn, int stay, int fadeOut) {
        if (player == null || !player.isOnline()) return;
        Component t = ColorUtils.toComponent(title);
        Component s = ColorUtils.toComponent(subtitle);
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );
        player.showTitle(Title.title(t, s, times));
    }

    /**
     * Gửi action bar
     */
    public static void sendActionBar(Player player, String message) {
        if (player == null || !player.isOnline() || message == null) return;
        player.sendActionBar(ColorUtils.toComponent(message));
    }

    // ==================== ÂM THANH ====================

    /**
     * Phát âm thanh tại vị trí
     */
    public static void playSound(Location location, Sound sound, float volume, float pitch) {
        if (location == null || location.getWorld() == null) return;
        location.getWorld().playSound(location, sound, volume, pitch);
    }

    /**
     * Phát âm thanh cho player
     */
    public static void playSound(Player player, Sound sound) {
        if (player == null || !player.isOnline()) return;
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    // ==================== HIỆU ỨNG ====================

    /**
     * Gửi thông báo khu vực (trong bán kính)
     */
    public static void broadcastRadius(Location location, double radius, String message) {
        if (location == null || location.getWorld() == null) return;
        Component component = ColorUtils.toComponent(message);
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= radius) {
                player.sendMessage(component);
            }
        }
    }
}