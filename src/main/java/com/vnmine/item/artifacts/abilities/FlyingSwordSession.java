package com.vnmine.item.artifacts.abilities;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * FlyingSwordSession - Quản lý phiên ngự kiếm phi hành.
 * 
 * Cơ chế bay:
 *   1. Kích hoạt: push player lên cao với velocity
 *   2. Sau 2 ticks (khi đã airborne), setAllowFlight(true) + setFlying(true) 
 *   3. Task duy trì flight mỗi tick (re-set flying nếu bị mất)
 *   4. Kiểm tra sneak + gần đất để kết thúc
 *   5. Miễn nhiễm sát thương rơi khi đang bay
 * 
 * Điều khiển: Space = lên, Ctrl/Sneak = xuống, Shift gần đất = hạ cánh
 * Tiêu hao: 10 linh lực/giây
 */
public class FlyingSwordSession {

    private final VNMinePlugin plugin;
    private final Player player;
    private final UUID playerUUID;
    private ItemDisplay swordDisplay;

    // Tasks
    private BukkitRunnable followTask;
    private BukkitRunnable manaDrainTask;
    private BukkitRunnable sneakCheckTask;
    private BukkitRunnable hoverTask;

    // Trạng thái
    private boolean ended = false;
    private boolean flightEstablished = false; // Đã set flying thành công trên không chưa
    private long startTick;

    // Constants
    private static final double DISPLAY_Y_OFFSET = -1.0;
    private static final int MANA_DRAIN_INTERVAL = 20;    // 1 giây
    private static final int MANA_COST_PER_SECOND = 10;
    private static final int SNEAK_CHECK_INTERVAL = 3;
    private static final double DISMOUNT_HEIGHT = 1.8;
    private static final int START_DELAY_TICKS = 40;      // 2 giây delay trước khi cho phép hạ cánh
    private static final float SWORD_SCALE = 2.5f;
    private static final int COOLDOWN_SECONDS = 30;
    private static final double LAUNCH_VELOCITY = 2.0;    // Đẩy lên cao hơn
    private static final int FLIGHT_ESTABLISH_DELAY = 2;  // Delay 2 ticks trước khi set flying

    public FlyingSwordSession(VNMinePlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerUUID = player.getUniqueId();
        this.startTick = 0;
        this.flightEstablished = false;
    }

    /**
     * Bắt đầu phiên ngự kiếm phi hành
     */
    public void start() {
        if (ended) return;

        // 1. Reset fall distance để tránh sát thương rơi
        player.setFallDistance(0);

        // 2. Cho phép bay + đẩy lên cao
        player.setAllowFlight(true);
        player.setVelocity(player.getVelocity().setY(LAUNCH_VELOCITY));

        // 3. Ghi nhận tick bắt đầu
        startTick = player.getWorld().getGameTime();

        // 4. Spawn ItemDisplay sword
        spawnSwordDisplay();

        // 5. Các task
        startFollowTask();
        startManaDrainTask();
        startSneakCheckTask();
        startHoverTask();

        // 6. Schedule set flying sau 2 ticks (khi đã rời mặt đất)
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ended || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                // Lúc này player đã ở trên không, set flying sẽ thành công
                player.setAllowFlight(true);
                player.setFlying(true);
                flightEstablished = true;
                MessageUtils.send(player, "&b✦ Ngự Kiếm Phi Hành! &7Space lên, Ctrl xuống, Shift gần đất để hạ cánh.");
                MessageUtils.playSound(player, org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LAUNCH);
            }
        }.runTaskLater(plugin, FLIGHT_ESTABLISH_DELAY);
    }

    /**
     * Kết thúc phiên ngự kiếm
     */
    public void end() {
        if (ended) return;
        ended = true;

        // Cancel tasks
        if (followTask != null) followTask.cancel();
        if (manaDrainTask != null) manaDrainTask.cancel();
        if (sneakCheckTask != null) sneakCheckTask.cancel();
        if (hoverTask != null) hoverTask.cancel();

        // Remove ItemDisplay
        if (swordDisplay != null) {
            swordDisplay.remove();
            swordDisplay = null;
        }

        // Tắt flight
        if (player.isOnline()) {
            if (!plugin.getMountManager().hasActiveMount(player)) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }

        // Hiệu ứng
        if (player.isOnline()) {
            MessageUtils.send(player, "&7Ngự kiếm kết thúc. Hồi chiêu &e" + COOLDOWN_SECONDS + "&7 giây.");
            MessageUtils.playSound(player, org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP);
        }
    }

    public boolean isEnded() { return ended; }
    public UUID getPlayerUUID() { return playerUUID; }
    public Player getPlayer() { return player; }
    public static int getCooldownSeconds() { return COOLDOWN_SECONDS; }
    public static int getManaCostPerSecond() { return MANA_COST_PER_SECOND; }

    // ==================== PRIVATE METHODS ====================

    /**
     * Spawn ItemDisplay chứa kiếm phóng to, nằm ngang, hướng về trước
     */
    private void spawnSwordDisplay() {
        Location loc = player.getLocation().clone();
        loc.setY(loc.getY() + DISPLAY_Y_OFFSET);

        swordDisplay = (ItemDisplay) player.getWorld().spawnEntity(loc, EntityType.ITEM_DISPLAY);
        swordDisplay.setItemStack(createDisplaySword());
        swordDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
        swordDisplay.setBillboard(Display.Billboard.FIXED);
        swordDisplay.setInvulnerable(true);
        swordDisplay.setGravity(false);
        swordDisplay.setVisibleByDefault(true);
        swordDisplay.addScoreboardTag("vnmine_flying_sword");

        // Transformation: scale + xoay kiếm nằm ngang, mũi kiếm hướng về trước
        Transformation transformation = new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf().rotationX((float) Math.toRadians(90)),
                new Vector3f(SWORD_SCALE, SWORD_SCALE, SWORD_SCALE),
                new Quaternionf()
        );
        swordDisplay.setTransformation(transformation);

        // Đồng bộ yaw với hướng player
        swordDisplay.setRotation(player.getLocation().getYaw(), 0);
    }

    /**
     * Tạo kiếm với glow, unbreakable, attribute ẩn
     */
    private ItemStack createDisplaySword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(org.bukkit.enchantments.Enchantment.SHARPNESS, 5, true);
            sword.setItemMeta(meta);
        }
        return sword;
    }

    /**
     * Task cập nhật vị trí + hướng ItemDisplay theo player mỗi tick.
     * Đồng thời duy trì trạng thái flying và reset fall distance.
     */
    private void startFollowTask() {
        followTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (ended || !player.isOnline() || swordDisplay == null || swordDisplay.isDead()) {
                    end();
                    this.cancel();
                    return;
                }

                // === Duy trì trạng thái flying ===
                // Nếu flight đã established nhưng player bị mất flying, re-set ngay
                if (flightEstablished) {
                    if (!player.getAllowFlight()) {
                        player.setAllowFlight(true);
                    }
                    if (!player.isFlying()) {
                        player.setFlying(true);
                    }
                    // Reset fall distance để không bị sát thương rơi
                    player.setFallDistance(0);
                }

                // Cập nhật vị trí dưới chân player
                Location playerLoc = player.getLocation();
                Location displayLoc = playerLoc.clone();
                displayLoc.setY(playerLoc.getY() + DISPLAY_Y_OFFSET);
                displayLoc.setPitch(0);
                swordDisplay.teleport(displayLoc);

                // Cập nhật hướng (yaw) đồng bộ với player
                swordDisplay.setRotation(playerLoc.getYaw(), 0);
            }
        };
        followTask.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Task tiêu hao linh lực mỗi giây
     */
    private void startManaDrainTask() {
        manaDrainTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (ended || !player.isOnline()) {
                    end();
                    this.cancel();
                    return;
                }

                if (!plugin.getCultivationManager().consumeMana(player, MANA_COST_PER_SECOND)) {
                    MessageUtils.send(player, "&cHết linh lực! Ngự kiếm kết thúc.");
                    end();
                    this.cancel();
                }
            }
        };
        manaDrainTask.runTaskTimer(plugin, 20L, MANA_DRAIN_INTERVAL);
    }

    /**
     * Task duy trì độ cao - nhẹ nhàng giữ player không bị rơi.
     * Chỉ can thiệp khi player đang falling và flight chưa established.
     * Khi flight đã established, player tự điều khiển bằng Space/Ctrl.
     */
    private void startHoverTask() {
        hoverTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (ended || !player.isOnline()) {
                    end();
                    this.cancel();
                    return;
                }

                // Trong 2 tick đầu, flight chưa established, giúp player không rơi
                if (!flightEstablished) {
                    org.bukkit.util.Vector vel = player.getVelocity();
                    if (vel.getY() < 0.2) {
                        // Giảm tốc độ rơi để player có thời gian lên cao
                        player.setVelocity(vel.setY(vel.getY() * 0.8 + 0.15));
                    }
                }
            }
        };
        hoverTask.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Task kiểm tra sneak để hạ cánh.
     * - 2 giây đầu: không check (tránh lỗi rơi ngay)
     * - Sneak + gần đất: hạ cánh (chứ không auto-hạ cánh như cũ)
     * - Sneak trên cao: bay xuống nhanh hơn
     */
    private void startSneakCheckTask() {
        sneakCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (ended || !player.isOnline()) {
                    end();
                    this.cancel();
                    return;
                }

                // Kiểm tra delay khởi động
                long currentTick = player.getWorld().getGameTime();
                if (currentTick - startTick < START_DELAY_TICKS) {
                    return;
                }

                Location loc = player.getLocation();
                double distanceToGround = getDistanceToGround(loc);

                // Chỉ hạ cánh khi player đang sneak VÀ gần mặt đất
                if (player.isSneaking() && distanceToGround >= 0 && distanceToGround <= DISMOUNT_HEIGHT) {
                    MessageUtils.send(player, "&7Hạ cánh.");
                    player.setVelocity(player.getVelocity().setY(0.2));
                    end();
                    this.cancel();
                    return;
                }

                // Trên cao + đang sneak → hạ nhanh hơn (override flight)
                if (player.isSneaking() && distanceToGround > DISMOUNT_HEIGHT) {
                    // Tạm thời tắt flying để rơi xuống
                    if (flightEstablished) {
                        player.setFlying(false);
                    }
                    org.bukkit.util.Vector vel = player.getVelocity();
                    if (vel.getY() > -0.8) {
                        player.setVelocity(vel.setY(vel.getY() - 0.15));
                    }
                } else if (flightEstablished && !player.isFlying()) {
                    // Nếu không sneak mà flying bị tắt, bật lại
                    player.setFlying(true);
                }
            }

            private double getDistanceToGround(Location loc) {
                Location check = loc.clone();
                for (int i = 0; i < 30; i++) {
                    check.setY(check.getY() - 1);
                    if (check.getBlock().getType().isSolid()) {
                        return loc.getY() - check.getY();
                    }
                }
                return -1;
            }
        };
        sneakCheckTask.runTaskTimer(plugin, 0L, SNEAK_CHECK_INTERVAL);
    }
}