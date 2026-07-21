package com.vnmine.gui.framework;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * CraftingMenuGUI - Menu dạng luyện chế vật phẩm.
 * 
 * Đặc điểm:
 * - Input slots: Người chơi đặt nguyên liệu vào
 * - Output slots: Thành phẩm hiển thị ở đây
 * - Craft button: Nút để bắt đầu quá trình luyện chế
 * - Status slot: Hiển thị trạng thái hiện tại
 * - Timer + BossBar: Quá trình luyện chế có thời gian
 * - Auto-return items khi đóng inventory
 * 
 * Texture support:
 * - Tất cả border items dùng MenuSlot.fromConfig()
 * - Input/Output slots có thể có texture riêng
 * 
 * Cách dùng:
 * 1. extends CraftingMenuGUI
 * 2. Implement các abstract methods:
 *    - checkRequirements(): Kiểm tra điều kiện (skill, level, tu vi...)
 *    - calculateGrade(): Tính phẩm cấp thành phẩm
 *    - onCraftStart(): Logic khi bắt đầu craft
 *    - onCraftComplete(): Logic khi craft hoàn tất
 *    - getInputSlots(): Mảng slot input
 *    - getOutputSlots(): Mảng slot output
 *    - getCraftSlot(): Slot nút craft
 *    - getStatusSlot(): Slot hiển thị trạng thái
 *    - getBackSlot(): Slot nút back
 */
public abstract class CraftingMenuGUI extends MenuGUI {

    protected static final int DEFAULT_CHARGES = 10;

    // Session quản lý trạng thái craft của từng player
    protected static class CraftSession {
        final UUID playerUUID;
        boolean isCrafting;
        BossBar activeBossBar;

        CraftSession(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.isCrafting = false;
            this.activeBossBar = null;
        }
    }

    protected final Map<UUID, CraftSession> activeSessions = new HashMap<>();

    public CraftingMenuGUI(VNMinePlugin plugin, String mainTitle) {
        super(plugin, mainTitle);
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Kiểm tra điều kiện trước khi craft
     * @return true nếu đủ điều kiện, false nếu không
     */
    protected abstract boolean checkRequirements(Player player);

    /**
     * Tính phẩm cấp thành phẩm
     * @return Chỉ số phẩm cấp (0 = thấp nhất)
     */
    protected abstract int calculateGrade(Player player);

    /**
     * Logic khi bắt đầu craft
     * @return Thời gian craft (giây)
     */
    protected abstract long onCraftStart(Player player, Inventory gui, CraftSession session);

    /**
     * Logic khi craft hoàn tất
     */
    protected abstract void onCraftComplete(Player player, Inventory gui, CraftSession session);

    /**
     * Lấy mảng slot input
     */
    protected abstract int[] getInputSlots();

    /**
     * Lấy mảng slot output
     */
    protected abstract int[] getOutputSlots();

    /**
     * Lấy slot nút craft
     */
    protected abstract int getCraftSlot();

    /**
     * Lấy slot hiển thị trạng thái
     */
    protected abstract int getStatusSlot();

    /**
     * Lấy slot nút back (có thể trả về -1 nếu không có)
     */
    protected abstract int getBackSlot();

    // ==================== SLOT HELPERS ====================

    /**
     * Kiểm tra slot có phải input không
     */
    protected boolean isInputSlot(int slot) {
        for (int s : getInputSlots()) {
            if (s == slot) return true;
        }
        return false;
    }

    /**
     * Kiểm tra slot có phải output không
     */
    protected boolean isOutputSlot(int slot) {
        for (int s : getOutputSlots()) {
            if (s == slot) return true;
        }
        return false;
    }

    // ==================== EVENT HANDLING ====================

    @Override
    protected void handleClick(Player player, int slot, ItemStack clicked, InventoryClickEvent event) {
        CraftSession session = getOrCreateSession(player);

        if (session.isCrafting) {
            MessageUtils.send(player, "&cĐang trong quá trình luyện chế, vui lòng chờ!");
            return;
        }

        // Input slots: cho phép tương tác mặc định
        if (isInputSlot(slot)) {
            return;
        }

        // Output slots: chỉ cho phép lấy item ra, không cho đặt vào
        if (isOutputSlot(slot)) {
            handleOutputSlotClick(player, slot, event);
            return;
        }

        // Craft button
        if (slot == getCraftSlot()) {
            attemptCraft(player, session);
            return;
        }

        // Back button
        if (slot == getBackSlot()) {
            handleBack(player);
            return;
        }
    }

    /**
     * Xử lý click vào output slot - chỉ cho phép lấy ra
     */
    protected void handleOutputSlotClick(Player player, int slot, InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            event.setCancelled(true);
            return;
        }
        // Cho phép lấy item ra
    }

    /**
     * Xử lý nút back - mặc định đóng inventory
     */
    protected void handleBack(Player player) {
        returnItemsToPlayer(player);
        player.closeInventory();
    }

    @Override
    protected void onClose(Player player, InventoryCloseEvent event) {
        CraftSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        // Cleanup BossBar
        if (session.activeBossBar != null) {
            session.activeBossBar.removeAll();
            session.activeBossBar = null;
        }

        // Trả items từ input và output slots
        Inventory gui = event.getInventory();
        returnItemsToPlayer(player, gui);

        activeSessions.remove(player.getUniqueId());
    }

    // ==================== CRAFT LOGIC ====================

    /**
     * Bắt đầu quá trình craft
     */
    protected void attemptCraft(Player player, CraftSession session) {
        Inventory gui = player.getOpenInventory().getTopInventory();

        // Kiểm tra điều kiện
        if (!checkRequirements(player)) {
            return;
        }

        // Gọi hook onCraftStart để lấy thời gian craft
        long craftTime = onCraftStart(player, gui, session);
        if (craftTime <= 0) {
            return; // onCraftStart đã xử lý lỗi
        }

        session.isCrafting = true;

        // Tạo BossBar
        BossBar bossBar = Bukkit.createBossBar(
                ColorUtils.colorize("&a🔥 Đang luyện chế..."),
                BarColor.GREEN, BarStyle.SEGMENTED_10);
        bossBar.addPlayer(player);
        session.activeBossBar = bossBar;

        final UUID playerUUID = player.getUniqueId();
        final long totalTicks = craftTime * 20L;
        final long intervalTicks = 10L;
        final long totalSteps = totalTicks / intervalTicks;

        new BukkitRunnable() {
            long currentStep = 0;

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(playerUUID);
                if (p == null || !p.isOnline()) {
                    cleanupBossBar(session);
                    session.isCrafting = false;
                    cancel();
                    return;
                }

                currentStep++;
                double progress = Math.min(1.0, (double) currentStep / totalSteps);
                bossBar.setProgress(progress);
                int percent = (int) (progress * 100);
                bossBar.setTitle(String.format("§a🔥 [%d%%] Đang luyện chế...", percent));

                if (currentStep >= totalSteps) {
                    cancel();
                    cleanupBossBar(session);
                    session.isCrafting = false;

                    // Gọi hook onCraftComplete
                    Inventory inv = p.getOpenInventory().getTopInventory();
                    onCraftComplete(p, inv, session);
                }
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    /**
     * Cleanup BossBar
     */
    protected void cleanupBossBar(CraftSession session) {
        if (session.activeBossBar != null) {
            session.activeBossBar.removeAll();
            session.activeBossBar = null;
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Lấy hoặc tạo session cho player
     */
    protected CraftSession getOrCreateSession(Player player) {
        return activeSessions.computeIfAbsent(player.getUniqueId(), CraftSession::new);
    }

    /**
     * Trả items từ input và output slots về inventory player
     */
    protected void returnItemsToPlayer(Player player) {
        Inventory gui = player.getOpenInventory().getTopInventory();
        returnItemsToPlayer(player, gui);
    }

    /**
     * Trả items từ input và output slots về inventory player
     */
    protected void returnItemsToPlayer(Player player, Inventory gui) {
        Set<Integer> allSlots = new HashSet<>();
        for (int s : getInputSlots()) allSlots.add(s);
        for (int s : getOutputSlots()) allSlots.add(s);

        for (int slot : allSlots) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
                gui.setItem(slot, null);
            }
        }
    }

    /**
     * Tiêu hao nguyên liệu từ input slots
     */
    protected void consumeIngredients(Inventory gui, Map<String, Integer> required) {
        for (int slot : getInputSlots()) {
            ItemStack item = gui.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;

            // Tìm key phù hợp
            String key = findIngredientKey(item, required);
            if (key == null) {
                // Nguyên liệu lạ - xóa
                gui.setItem(slot, null);
                continue;
            }

            Integer needed = required.get(key);
            if (needed != null && needed > 0) {
                int consume = Math.min(needed, item.getAmount());
                int surplus = item.getAmount() - consume;
                if (surplus > 0) {
                    ItemStack returnItem = item.clone();
                    returnItem.setAmount(surplus);
                    gui.setItem(slot, returnItem);
                } else {
                    gui.setItem(slot, null);
                }
                required.put(key, needed - consume);
            } else {
                gui.setItem(slot, null);
            }
        }
    }

    /**
     * Tìm key trong required map phù hợp với item
     */
    protected String findIngredientKey(ItemStack item, Map<String, Integer> required) {
        // Mặc định: dùng material name làm key
        String matKey = "mat:" + item.getType().name();
        if (required.containsKey(matKey)) return matKey;

        // Kiểm tra persistent data (herb)
        String herbId = com.vnmine.item.ItemBuilder.getPersistentData(item, "vnmine_herb");
        if (herbId != null) {
            String herbKey = "herb:" + herbId;
            if (required.containsKey(herbKey)) return herbKey;
        }

        return null;
    }

    /**
     * Tạo item trạng thái
     */
    protected void setStatusItem(Inventory gui, Material material, String name, String... lore) {
        gui.setItem(getStatusSlot(), new com.vnmine.item.ItemBuilder(material)
                .setName(name)
                .setLore(lore)
                .build());
    }

    /**
     * Tạo item trạng thái với glow
     */
    protected void setStatusGlowItem(Inventory gui, Material material, String name, String... lore) {
        gui.setItem(getStatusSlot(), new com.vnmine.item.ItemBuilder(material)
                .setName(name)
                .setLore(lore)
                .setGlow(true)
                .build());
    }

    /**
     * Setup border cho crafting GUI
     */
    protected void setupCraftingBorder(Inventory gui) {
        Set<Integer> excludeSlots = new HashSet<>();
        for (int s : getInputSlots()) excludeSlots.add(s);
        for (int s : getOutputSlots()) excludeSlots.add(s);
        excludeSlots.add(getCraftSlot());
        excludeSlots.add(getStatusSlot());
        if (getBackSlot() >= 0) excludeSlots.add(getBackSlot());
        fillEmptySlots(gui, excludeSlots);
    }

    /**
     * Strip color từ string
     */
    protected String stripColor(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }
}