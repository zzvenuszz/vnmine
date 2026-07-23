package com.vnmine.item.artifacts.abilities;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ArtifactAbilityListener - Xử lý kích hoạt pháp bảo
 * Click phải để thi triển, kèm niệm khẩu quyết
 * 
 * Tất cả thời gian trong config đều tính bằng giây
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

    // Soul Jade cooldown per player
    private final Map<UUID, Long> soulJadeCooldowns = new HashMap<>();

    // Anti double-fire cooldown (Spigot fires RIGHT_CLICK_BLOCK + RIGHT_CLICK_AIR)
    private final Map<UUID, Long> lastInteractTime = new HashMap<>();
    private static final long INTERACT_COOLDOWN_MS = 300; // 300ms anti double-fire

    public ArtifactAbilityListener(VNMinePlugin plugin) {
        this.plugin = plugin;
        // Start Soul Jade check task
        startSoulJadeCheckTask();
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
     * Đọc config seconds và convert ra milliseconds cho cooldown
     */
    private int getConfigCooldown(String artifactPath, int defaultSeconds) {
        return plugin.getConfig().getInt("items.artifacts." + artifactPath, defaultSeconds);
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
     * Niệm khẩu quyết - broadcast tên kỹ năng lên chat
     */
    private void chant(Player player, String incantation) {
        Bukkit.broadcastMessage(ColorUtils.colorize("&7[&e" + player.getName() + "&7] &f" + incantation));
    }

    // ==================== SỰ KIỆN ====================

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK &&
            action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        String artifactId = getArtifactId(item);
        if (artifactId == null) return;

        // Anti double-fire: Spigot fires both RIGHT_CLICK_BLOCK + RIGHT_CLICK_AIR
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastInteract = lastInteractTime.get(uuid);
        if (lastInteract != null && (now - lastInteract) < INTERACT_COOLDOWN_MS) {
            return; // Đã xử lý trong 300ms qua
        }
        lastInteractTime.put(uuid, now);

        event.setCancelled(true);

        // Determine click type
        String clickType = (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) ? "left-click" : "right-click";

        // Try to get artifact skills from item definition (new system)
        com.vnmine.item.ItemDefinition def = plugin.getItemDataLoader().getItem(artifactId.toUpperCase());
        if (def != null && def.getClickBehavior() != null && !def.getClickBehavior().isEmpty()) {
            String skillId = def.getClickBehavior().get(clickType);
            if (skillId != null && !skillId.equalsIgnoreCase("none") && !skillId.isEmpty()) {
                // Cast artifact skill via ArtifactSkillManager
                plugin.getArtifactSkillManager().castArtifactSkill(player, skillId, plugin.getCultivationManager().getPlayerData(uuid));
                return;
            }
        }

        // Fallback to legacy hardcoded behavior (for backward compatibility)
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
                MessageUtils.send(player, "&a&l◆ Hồn Ngọc ◆ &7- Tự động hồi 50% máu khi HP < 30%.");
                break;
            case PHOENIX_REBIRTH:
                MessageUtils.send(player, "&6&l◆ Phượng Hoàng Lệnh ◆ &7- Tự động hồi sinh 1 lần sau khi chết.");
                break;
        }
    }

    // ==================== BÁT QUÁI KÍNH - GIẢM SÁT THƯƠNG ====================
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        // Kiểm tra player có đang cầm Bát Quái Kính không (main hand hoặc off hand)
        if (!isHoldingBaguaMirror(player)) return;

        // Đọc config
        double reduction = plugin.getConfig().getDouble("items.artifacts.bagua_mirror.damage-reduction", 0.3);
        boolean notify = plugin.getConfig().getBoolean("items.artifacts.bagua_mirror.notify-player", true);

        // Giảm sát thương
        double original = event.getDamage();
        double reduced = original * reduction;
        event.setDamage(original - reduced);

        // Thông báo
        if (notify && reduced > 0) {
            String msg = plugin.getConfig().getString("items.artifacts.bagua_mirror.notify-message",
                    "&5◆ Bát Quái Kính ◆ &7đã chặn &c{reduced} &7sát thương!");
            msg = msg.replace("{reduced}", String.format("%.1f", reduced));
            msg = msg.replace("{original}", String.format("%.1f", original));
            MessageUtils.send(player, msg);
        }
    }

    private boolean isHoldingBaguaMirror(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && mainHand.getType() != Material.AIR) {
            String id = getArtifactId(mainHand);
            if (BAGUA_MIRROR.equals(id)) return true;
        }
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null && offHand.getType() != Material.AIR) {
            String id = getArtifactId(offHand);
            if (BAGUA_MIRROR.equals(id)) return true;
        }
        return false;
    }

    // ==================== HỒN NGỌC - TỰ HỒI MÁU ====================

    /**
     * Kiểm tra định kỳ HP của player có Hồn Ngọc
     */
    private void startSoulJadeCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfig().getBoolean("items.artifacts.soul_jade.enabled", true)) return;
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Kiểm tra có Hồn Ngọc trong inventory
                    if (!hasSoulJade(player)) continue;

                    double hpPercent = player.getHealth() / player.getMaxHealth();
                    double threshold = plugin.getConfig().getDouble("items.artifacts.soul_jade.hp-threshold", 0.3);
                    
                    if (hpPercent > threshold) continue;

                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    int cooldownSeconds = plugin.getConfig().getInt("items.artifacts.soul_jade.cooldown-seconds", 120);
                    long lastUse = soulJadeCooldowns.getOrDefault(uuid, 0L);
                    long remaining = (lastUse + (cooldownSeconds * 1000L)) - now;
                    if (remaining > 0) continue;

                    // Hồi máu
                    double healPercent = plugin.getConfig().getDouble("items.artifacts.soul_jade.heal-percent", 0.5);
                    double healAmount = player.getMaxHealth() * healPercent;
                    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healAmount));
                    soulJadeCooldowns.put(uuid, now);

                    // Thông báo
                    boolean notify = plugin.getConfig().getBoolean("items.artifacts.soul_jade.notify-player", true);
                    if (notify) {
                        String msg = plugin.getConfig().getString("items.artifacts.soul_jade.notify-message",
                                "&a✦ Hồn Ngọc tỏa sáng! Hồi phục &f{healed} &amáu!");
                        msg = msg.replace("{healed}", String.format("%.0f", healAmount));
                        MessageUtils.send(player, msg);
                    }

                    // Hiệu ứng
                    player.getWorld().playSound(player.getLocation(),
                            org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check mỗi giây
    }

    private boolean hasSoulJade(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (!item.hasItemMeta()) continue;
            ItemMeta meta = item.getItemMeta();
            if (!meta.hasDisplayName()) continue;
            String name = meta.getDisplayName().replaceAll("§[0-9a-fk-or]", "").trim();
            if (name.contains(SOUL_JADE)) return true;
        }
        return false;
    }

    // ==================== EVENT HANDLERS CHO FLYING SWORD ====================

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();

        FlyingSwordSession session = activeSessions.get(uuid);
        if (session != null && !session.isEnded()) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
                return;
            }
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
     * Kiếm Phi Hành - Ngự kiếm phi hành
     */
    private void activateFlyingSword(Player player, ItemStack item) {
        UUID uuid = player.getUniqueId();
        FlyingSwordSession existingSession = activeSessions.get(uuid);
        if (existingSession != null && !existingSession.isEnded()) {
            existingSession.end();
            activeSessions.remove(uuid);
            return;
        }

        int cd = getConfigCooldown("flying_sword.cooldown-seconds", 30);
        if (!checkCooldown(player, FLYING_SWORD, cd)) return;

        int initialManaCost = FlyingSwordSession.getManaCostPerSecond();
        if (!consumeMana(player, initialManaCost)) return;

        chant(player, "§b✦ Thiên Ngự Kiếm! ✦");

        FlyingSwordSession session = new FlyingSwordSession(plugin, player);
        activeSessions.put(uuid, session);
        session.start();
    }

    /**
     * Linh Chung - Làm choáng quái (cấu hình được)
     */
    private void activateSpiritBell(Player player) {
        int cd = getConfigCooldown("spirit_bell.cooldown-seconds", 15);
        if (!checkCooldown(player, SPIRIT_BELL, cd)) return;
        
        int manaCost = plugin.getConfig().getInt("items.artifacts.spirit_bell.mana-cost", 15);
        if (!consumeMana(player, manaCost)) return;

        chant(player, "§6✦ Chung Âm Chấn! ✦");

        // Đọc config: stun-duration-seconds (giây) → ticks
        int stunSeconds = plugin.getConfig().getInt("items.artifacts.spirit_bell.stun-duration-seconds", 5);
        int stunTicks = stunSeconds * 20;
        int radius = plugin.getConfig().getInt("items.artifacts.spirit_bell.stun-radius", 5);

        int stunned = 0;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Monster && entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stunTicks, 10, false, false, false));
                living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, stunTicks, 10, false, false, false));
                stunned++;
            }
        }

        MessageUtils.send(player, "&6✦ Linh Chung vang lên, làm choáng &e" + stunned + " &6quái vật trong &e" + stunSeconds + "&6 giây!");
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BELL_USE, 1.0f, 1.0f);
    }

    /**
     * Thiên Linh Thuẫn - Bất tử (cấu hình được)
     */
    private void activateHeavenShield(Player player) {
        int cd = getConfigCooldown("heaven_shield.cooldown-seconds", 180);
        if (!checkCooldown(player, HEAVEN_SHIELD, cd)) return;
        
        int manaCost = plugin.getConfig().getInt("items.artifacts.heaven_shield.mana-cost", 40);
        if (!consumeMana(player, manaCost)) return;

        chant(player, "§4✦ Thiên Linh Thuẫn Hộ Thể! ✦");

        // Đọc invulnerability-seconds → ticks
        int invulSeconds = plugin.getConfig().getInt("items.artifacts.heaven_shield.invulnerability-seconds", 5);
        int invulTicks = invulSeconds * 20;

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, invulTicks, 10, true, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, invulTicks, 4, true, true, true));
        player.setFireTicks(0);

        MessageUtils.send(player, "&4✦ Thiên Linh Thuẫn kích hoạt! Bất tử &e" + invulSeconds + " &4giây.");
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= invulSeconds || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.0f);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    /**
     * Lôi Ấn - Gọi sét (cấu hình được)
     */
    private void activateThunderSeal(Player player) {
        int manaCost = plugin.getConfig().getInt("items.artifacts.thunder_seal.mana-cost", 25);
        if (!consumeMana(player, manaCost)) return;

        int range = plugin.getConfig().getInt("items.artifacts.thunder_seal.range", 20);

        Entity target = null;
        try {
            Entity targetEntity = player.getTargetEntity(range);
            if (targetEntity != null && targetEntity != player && !isOwnPet(player, targetEntity)) {
                target = targetEntity;
            }
        } catch (Exception e) {
            // Fallback
        }

        if (target == null) {
            double closest = (double) range;
            for (Entity entity : player.getNearbyEntities(range, 10, range)) {
                if (entity.equals(player)) continue;
                if (isOwnPet(player, entity)) continue;
                if (entity instanceof LivingEntity) {
                    double dist = player.getLocation().distance(entity.getLocation());
                    if (dist < closest) {
                        closest = dist;
                        target = entity;
                    }
                }
            }
        }

        if (target == null) {
            MessageUtils.send(player, "&cKhông có mục tiêu nào trong phạm vi!");
            PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
            if (data != null) data.regenMana(manaCost);
            return;
        }

        int cd = getConfigCooldown("thunder_seal.cooldown-seconds", 10);
        if (!checkCooldown(player, THUNDER_SEAL, cd)) return;

        chant(player, "§e✦ Lôi Ấn! Thiên Lôi Dẫn! ✦");

        target.getWorld().strikeLightning(target.getLocation());
        String targetName = (target instanceof Player) ? target.getName() : target.getType().name();
        MessageUtils.send(player, "&e✦ Lôi Ấn giáng sét xuống &f" + targetName + "&e!");
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
    }

    private boolean isOwnPet(Player player, Entity entity) {
        if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            AnimalTamer owner = tameable.getOwner();
            return owner != null && owner.getUniqueId().equals(player.getUniqueId());
        }
        return false;
    }
}