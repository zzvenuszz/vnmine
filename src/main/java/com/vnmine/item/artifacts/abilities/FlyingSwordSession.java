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
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * FlyingSwordSession - Quản lý phiên ngự kiếm phi hành.
 * ============================================================
 * ⚠️ CẢNH BÁO: Class này ĐÃ HOẠT ĐỘNG ỔN ĐỊNH. KHÔNG SỬA ĐỔI
 * nếu không có lệnh trực tiếp từ người dùng.
 * ============================================================
 * 
 * Cơ chế bay:
 *   1. Kích hoạt: push player lên cao với velocity
 *   2. Sau 2 ticks: setAllowFlight(true) + setFlying(true) 
 *   3. Follow task mỗi tick: setAllowFlight(true) + reset fall distance
 *      KHÔNG force setFlying mỗi tick (tránh lỗi inventory)
 *   4. Periodic check mỗi 10 ticks: re-set flying nếu bị mất
 *   5. Space = lên (vanilla flight), Shift = xuống
 *   6. Ctrl (Sprint) = tăng tốc về phía trước
 *   7. Sneak gần đất = hạ cánh
 *   8. Reset fall distance mỗi tick để tránh sát thương rơi
 * 
 * Điều khiển: Space = lên, Shift/Sneak = xuống, Ctrl = tăng tốc
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
    private BukkitRunnable flightCheckTask;

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
    private static final double AUTO_LAND_HEIGHT = 0.5;
    private static final int START_DELAY_TICKS = 40;      // 2 giây delay trước khi cho phép hạ cánh
    private static final float SWORD_SCALE = 2.5f;
    private static final int COOLDOWN_SECONDS = 30;
    private static final double LAUNCH_VELOCITY = 0.6;    // Đẩy lên cao ~2 blocks
    private static final int FLIGHT_ESTABLISH_DELAY = 10;  // Delay 10 ticks (~0.5s) trước khi set flying
    private static final int FLIGHT_CHECK_INTERVAL = 10;  // Check flying mỗi 10 ticks (0.5s)
    private static final double SPRINT_SPEED_BOOST = 0.4; // Tốc độ tăng thêm khi sprint

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
        startFlightCheckTask();

        // 6. Schedule set flying sau 15 ticks (khi đã lên cao ~2 blocks)
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
                MessageUtils.send(player, "&b✦ Ngự Kiếm Phi Hành! &7Space lên, Shift xuống, Ctrl tăng tốc.");
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
        if (flightCheckTask != null) flightCheckTask.cancel();

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

        flightEstablished = false;

        // Reset fall distance để tránh sát thương rơi khi kết thúc
        if (player.isOnline()) {
            player.setFallDistance(0);
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
     * Chỉ setAllowFlight + reset fall distance. KHÔNG force setFlying mỗi tick
     * (để tránh lỗi không kéo được items trong inventory).
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

                // === Duy trì allow flight + reset fall distance ===
                // KHÔNG force setFlying(true) ở đây để tránh lỗi inventory
                if (flightEstablished) {
                    if (!player.getAllowFlight()) {
                        player.setAllowFlight(true);
                    }
                    // Paper anti-fly: re-set flying nếu bị reset
                    if (!player.isFlying()) {
                        player.setFlying(true);
                    }
                    // Reset fall distance để không bị sát thương rơi
                    player.setFallDistance(0);
                }

                // Xử lý sprint (Ctrl) - tăng tốc về phía trước
                if (flightEstablished && player.isSprinting()) {
                    // Lấy hướng nhìn của player, chỉ lấy horizontal direction
                    Vector direction = player.getLocation().getDirection();
                    direction.setY(0).normalize();
                    // Boost vận tốc theo hướng nhìn
                    Vector vel = player.getVelocity();
                    double currentHorizontal = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ());
                    if (currentHorizontal < SPRINT_SPEED_BOOST) {
                        vel.add(direction.multiply(SPRINT_SPEED_BOOST - currentHorizontal));
                        player.setVelocity(vel);
                    }
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
     * Task kiểm tra flight định kỳ - mỗi 10 ticks (0.5 giây).
     * Chỉ re-set flying nếu bị mất, KHÔNG chạy mỗi tick để tránh lỗi inventory.
     */
    private void startFlightCheckTask() {
        flightCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (ended || !player.isOnline()) {
                    end();
                    this.cancel();
                    return;
                }

                if (flightEstablished) {
                    // Chỉ re-set flying nếu thực sự bị mất
                    if (!player.isFlying()) {
                        player.setFlying(true);
                    }
                }
            }
        };
        flightCheckTask.runTaskTimer(plugin, 20L, FLIGHT_CHECK_INTERVAL);
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
     * Task duy trì độ cao - hỗ trợ trong giai đoạn flight chưa established.
     * Giữ player ở độ cao ~2 blocks so với mặt đất để tránh rơi,
     * cho đến khi setFlying(true) được gọi thành công.
     */
    private void startHoverTask() {
        hoverTask = new BukkitRunnable() {
            private boolean hasPassedPeak = false;

            @Override
            public void run() {
                if (ended || !player.isOnline()) {
                    end();
                    this.cancel();
                    return;
                }

                // Reset fall distance
                player.setFallDistance(0);

                if (!flightEstablished) {
                    // Duy trì allowFlight trong lúc chờ
                    player.setAllowFlight(true);

                    Vector vel = player.getVelocity();

                    // Phát hiện đã qua đỉnh (velocity Y chuyển từ dương sang âm)
                    if (vel.getY() <= 0) {
                        hasPassedPeak = true;
                    }

                    if (hasPassedPeak) {
                        // Đang rơi - giữ player lơ lửng tại chỗ, không để rơi xuống đất
                        player.setVelocity(new Vector(vel.getX() * 0.5, 0.1, vel.getZ() * 0.5));
                    } else {
                        // Đang đi lên - giữ nguyên quán tính
                        if (vel.getY() < 0.3) {
                            player.setVelocity(vel.setY(vel.getY() + 0.05));
                        }
                    }
                }
            }
        };
        hoverTask.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * Task kiểm tra sneak để hạ cánh.
     * - 2 giây đầu: không check (tránh lỗi rơi ngay)
     * - Sneak + gần đất: hạ cánh
     * - Áp sát mặt đất: tự động hạ cánh
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

                // Tự động hạ cánh khi quá gần mặt đất (< 0.5 block)
                if (distanceToGround >= 0 && distanceToGround <= AUTO_LAND_HEIGHT) {
                    MessageUtils.send(player, "&7Hạ cánh.");
                    end();
                    this.cancel();
                    return;
                }

                // Sneak + gần mặt đất: hạ cánh
                if (player.isSneaking() && distanceToGround >= 0 && distanceToGround <= DISMOUNT_HEIGHT) {
                    MessageUtils.send(player, "&7Hạ cánh.");
                    player.setVelocity(player.getVelocity().setY(0.2));
                    end();
                    this.cancel();
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