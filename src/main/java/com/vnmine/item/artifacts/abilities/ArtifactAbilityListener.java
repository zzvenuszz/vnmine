package com.vnmine.item.artifacts.abilities;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ArtifactAbilityListener - Xử lý kích hoạt pháp bảo
 * Click phải để thi triển, kèm niệm khẩu quyết
 */
public class ArtifactAbilityListener implements Listener {

    private final VNMinePlugin plugin;

    // Cooldown cho từng player (giây)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // Flying Sword sessions đang active
    private final Map<UUID, FlyingSwordSession> activeSessions = new ConcurrentHashMap<>();

    // ID pháp bảo
    private static final String FLYING_SWORD = "Kiếm Phi Hành";
    private static final String SPIRIT_BELL = "Linh Chung";
    private static final String BAGUA_MIRROR = "Bát Quái Kính";
    private static final String SOUL_JADE = "Hồn Ngọc";
    private static final String HEAVEN_SHIELD = "Thiên Linh Thuẫn";
    private static final String THUNDER_SEAL = "Lôi Ấn";
    private static final String PHOENIX_REBIRTH = "Phượng Hoàng Lệnh";

    public ArtifactAbilityListener(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Kiểm tra item có phải pháp bảo không dựa vào tên
     */
    private String getArtifactId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return null;
        String name = meta.getDisplayName();

        // Bỏ qua màu sắc để so khớp
        String stripped = stripColor(name);

        if (stripped.contains(FLYING_SWORD)) return FLYING_SWORD;
        if (stripped.contains(SPIRIT_BELL)) return SPIRIT_BELL;
        if (stripped.contains(BAGUA_MIRROR)) return BAGUA_MIRROR;
        if (stripped.contains(SOUL_JADE)) return SOUL_JADE;
        if (stripped.contains(HEAVEN_SHIELD)) return HEAVEN_SHIELD;
        if (stripped.contains(THUNDER_SEAL)) return THUNDER_SEAL;
        if (stripped.contains(PHOENIX_REBIRTH)) return PHOENIX_REBIRTH;

        return null;
    }

    private String stripColor(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "").trim();
    }

    /**
     * Kiểm tra cooldown
     */
    private boolean checkCooldown(Player player, String artifactId, int cooldownSeconds) {
        if (cooldownSeconds <= 0) return true;
        UUID uuid = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(uuid, k -> new HashMap<>());
        long now = System.currentTimeMillis();
        long lastUse = playerCooldowns.getOrDefault(artifactId, 0L);
        long remaining = (lastUse + (cooldownSeconds * 1000L)) - now;
        if (remaining > 0) {
            MessageUtils.send(player, "&cPháp bảo đang hồi chiêu! Còn &e" + (remaining / 1000) + "&c giây.");
            return false;
        }
        playerCooldowns.put(artifactId, now);
        return true;
    }

    /**
     * Tiêu hao linh lực, trả về false nếu không đủ
     */
    private boolean consumeMana(Player player, int amount) {
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data == null || data.getMana() < amount) {
            MessageUtils.send(player, "&cKhông đủ linh lực! Cần &b" + amount + " &clinh lực.");
            return false;
        }
        data.consumeMana(amount);
        return true;
    }

    /**
     * Niệm khẩu quyết - broadcast tên kỹ năng lên chat (dùng ColorUtils, không gọi player.chat)
     */
    private void chant(Player player, String incantation) {
        Bukkit.broadcastMessage(ColorUtils.colorize("&7[&e" + player.getName() + "&7] &f" + incantation));
    }

    // ==================== SỰ KIỆN ====================

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        String artifactId = getArtifactId(item);
        if (artifactId == null) return;

        event.setCancelled(true);

        switch (artifactId) {
            case FLYING_SWORD:
                activateFlyingSword(player, item);
                break;
            case SPIRIT_BELL:
                activateSpiritBell(player);
                break;
            case HEAVEN_SHIELD:
                activateHeavenShield(player);
                break;
            case THUNDER_SEAL:
                activateThunderSeal(player);
                break;
            case BAGUA_MIRROR:
                MessageUtils.send(player, "&5&l◆ Bát Quái Kính ◆ &7- Giảm 30% sát thương khi cầm trên tay.");
                break;
            case SOUL_JADE:
                MessageUtils.send(player, "&a&l◆ Hồn Ngọc ◆ &7- Tự động hồi 50% máu khi HP < 20%.");
                break;
            case PHOENIX_REBIRTH:
                MessageUtils.send(player, "&6&l◆ Phượng Hoàng Lệnh ◆ &7- Tự động hồi sinh 1 lần sau khi chết.");
                break;
        }
    }

    // ==================== EVENT HANDLERS CHO FLYING SWORD ====================

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();

        FlyingSwordSession session = activeSessions.get(uuid);
        if (session != null && !session.isEnded()) {
            // Hủy sát thương rơi khi đang ngự kiếm (fall distance đã được reset mỗi tick)
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
                return;
            }
            // Các loại sát thương khác → kết thúc ngự kiếm
            session.end();
            activeSessions.remove(uuid);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        FlyingSwordSession session = activeSessions.get(uuid);
        if (session != null && !session.isEnded()) {
            session.end();
            activeSessions.remove(uuid);
        }
    }

    // ==================== KỸ NĂNG PHÁP BẢO ====================

    /**
     * Kiếm Phi Hành - Ngự kiếm phi hành với cơ chế bay hoàn chỉnh
     * Space = lên, Ctrl = xuống, Shift gần đất = nhảy xuống kết thúc
     * Tiêu hao 10 linh lực/giây, hết linh lực = tự kết thúc + hồi chiêu CD 30s
     * "Thiên Ngự Kiếm!"
     */
    private void activateFlyingSword(Player player, ItemStack item) {
        // Kiểm tra nếu đang có session active thì kết thúc session cũ
        UUID uuid = player.getUniqueId();
        FlyingSwordSession existingSession = activeSessions.get(uuid);
        if (existingSession != null && !existingSession.isEnded()) {
            existingSession.end();
            activeSessions.remove(uuid);
            return;
        }

        // Kiểm tra cooldown
        if (!checkCooldown(player, FLYING_SWORD, FlyingSwordSession.getCooldownSeconds())) return;

        // Tiêu hao mana ngay lúc kích hoạt (1 lần đầu)
        int initialManaCost = FlyingSwordSession.getManaCostPerSecond();
        if (!consumeMana(player, initialManaCost)) return;

        chant(player, "§b✦ Thiên Ngự Kiếm! ✦");

        // Tạo và khởi động session
        FlyingSwordSession session = new FlyingSwordSession(plugin, player);
        activeSessions.put(uuid, session);
        session.start();
    }

    /**
     * Linh Chung - Làm choáng quái trong bán kính
     * "Chung Âm Chấn!"
     */
    private void activateSpiritBell(Player player) {
        if (!checkCooldown(player, SPIRIT_BELL, 15)) return;
        if (!consumeMana(player, 15)) return;

        chant(player, "§6✦ Chung Âm Chấn! ✦");

        // Làm choáng quái trong bán kính 5 block
        int radius = 5;
        int stunned = 0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Monster && entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 10, false, false, false));
                living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 10, false, false, false));
                stunned++;
            }
        }

        MessageUtils.send(player, "&6✦ Linh Chung vang lên, làm choáng &e" + stunned + " &6quái vật!");
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BELL_USE, 1.0f, 1.0f);
    }

    /**
     * Thiên Linh Thuẫn - Bất tử 5 giây, CD 3 phút
     * "Thiên Linh Thuẫn Hộ Thể!"
     */
    private void activateHeavenShield(Player player) {
        if (!checkCooldown(player, HEAVEN_SHIELD, 180)) return; // CD 3 phút
        if (!consumeMana(player, 40)) return;

        chant(player, "§4✦ Thiên Linh Thuẫn Hộ Thể! ✦");

        // Bất tử 5 giây - sử dụng damage resistance max
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 10, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 4, true, true, true));
        player.setFireTicks(0);

        MessageUtils.send(player, "&4✦ Thiên Linh Thuẫn kích hoạt! Bất tử 5 giây.");
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

        // Hiệu ứng visual
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= 5 || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.0f);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * Lôi Ấn - Gọi sét đánh mục tiêu, CD 10s
     * "Lôi Ấn! Thiên Lôi Dẫn!"
     */
    private void activateThunderSeal(Player player) {
        if (!checkCooldown(player, THUNDER_SEAL, 10)) return;
        if (!consumeMana(player, 25)) return;

        // Tìm mục tiêu gần nhất
        Entity target = null;
        double closest = 20.0;
        for (Entity entity : player.getNearbyEntities(20, 10, 20)) {
            if (entity instanceof Monster || entity instanceof Player) {
                if (entity.equals(player)) continue;
                double dist = player.getLocation().distance(entity.getLocation());
                if (dist < closest) {
                    closest = dist;
                    target = entity;
                }
            }
        }

        if (target == null) {
            MessageUtils.send(player, "&cKhông có mục tiêu nào trong phạm vi!");
            return;
        }

        chant(player, "§e✦ Lôi Ấn! Thiên Lôi Dẫn! ✦");

        // Gọi sét
        target.getWorld().strikeLightning(target.getLocation());
        MessageUtils.send(player, "&e✦ Lôi Ấn giáng sét xuống &f" + target.getName() + "&e!");
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
    }
}