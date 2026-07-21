package com.vnmine.gui;

import com.vnmine.VNMinePlugin;
import com.vnmine.item.ItemBuilder;
import com.vnmine.item.ItemDataLoader;
import com.vnmine.item.ItemDefinition;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * AdminMenuGUI - Menu admin lấy item test
 * Dữ liệu được load từ ItemDataLoader (file YML trong items/)
 */
public class AdminMenuGUI implements Listener {

    private final VNMinePlugin plugin;
    private final Map<String, String> displayToIdMap = new HashMap<>();

    private static final String TITLE_ADMIN_MAIN = "✦ VNMine Admin Menu ✦";
    private static final Map<String, String> CATEGORY_TITLES = new LinkedHashMap<>();
    static {
        CATEGORY_TITLES.put("pill", "✦ Admin - Dan Duoc");
        CATEGORY_TITLES.put("artifact", "✦ Admin - Phap Bao");
        CATEGORY_TITLES.put("skill", "✦ Admin - Cong Phap ✦");
        CATEGORY_TITLES.put("herb", "✦ Admin - Linh Thao ✦");
        CATEGORY_TITLES.put("material", "✦ Admin - Nguyen Lieu ✦");
        CATEGORY_TITLES.put("mount", "✦ Admin - Toa Ky ✦");
        CATEGORY_TITLES.put("currency", "✦ Admin - Linh Thach ✦");
    }

    private static final List<String> ADMIN_TITLES = new ArrayList<>();
    static {
        ADMIN_TITLES.add(TITLE_ADMIN_MAIN);
        ADMIN_TITLES.addAll(CATEGORY_TITLES.values());
    }

    private final Map<UUID, int[]> pageState = new HashMap<>();

    public AdminMenuGUI(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isOwnInventory(InventoryClickEvent event) {
        String title = ColorUtils.stripColor(event.getView().getTitle());
        for (String t : ADMIN_TITLES) { if (title.contains(t)) return true; }
        return false;
    }

    private void rebuildDisplayToIdMap() {
        displayToIdMap.clear();
        ItemDataLoader loader = plugin.getItemDataLoader();
        if (loader != null) {
            displayToIdMap.putAll(loader.buildDisplayNameToIdMap());
        }
    }

    // ==================== OPEN MENU ====================
    public void open(Player player) {
        rebuildDisplayToIdMap();
        Inventory gui = Bukkit.createInventory(null, 27, ColorUtils.colorize("&8" + TITLE_ADMIN_MAIN));
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&r").build();
        for (int i = 0; i < 27; i++) gui.setItem(i, border);
        gui.setItem(10, new ItemBuilder(Material.NETHER_STAR).setGlow(true).setName("&aĐan Dược")
                .setLore("", "&7Đan dược các phẩm cấp").build());
        gui.setItem(11, new ItemBuilder(Material.DIAMOND_SWORD).setGlow(true).setName("&6Pháp Bảo")
                .setLore("", "&7Pháp bảo các phẩm cấp").build());
        gui.setItem(12, new ItemBuilder(Material.ENCHANTED_BOOK).setGlow(true).setName("&dCông Pháp")
                .setLore("", "&7Sách kỹ năng").build());
        gui.setItem(13, new ItemBuilder(Material.GREEN_DYE).setGlow(true).setName("&2Linh Thảo")
                .setLore("", "&7Các loại linh thảo").build());
        gui.setItem(14, new ItemBuilder(Material.GOLD_INGOT).setGlow(true).setName("&eNguyên Liệu")
                .setLore("", "&7Nguyên liệu luyện đan/chế tạo").build());
        gui.setItem(15, new ItemBuilder(Material.SADDLE).setGlow(true).setName("&6Tọa Kỵ")
                .setLore("", "&7Key triệu hồi tọa kỵ").build());
        gui.setItem(16, new ItemBuilder(Material.PRISMARINE_SHARD).setGlow(true).setName("&bLinh Thạch")
                .setLore("", "&7Tiền tệ tu tiên").build());
        gui.setItem(26, new ItemBuilder(Material.BARRIER).setName("&cĐóng").build());
        pageState.remove(player.getUniqueId());
        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    // ==================== PAGINATION ====================
    private void openPaginatedMenu(Player player, String category) {
        String titleSuffix = CATEGORY_TITLES.getOrDefault(category, "✦ Admin ✦");
        int page = 0;
        int[] state = pageState.get(player.getUniqueId());
        if (state != null) page = state[0];

        List<ItemDefinition> items = plugin.getItemDataLoader().getItemsByCategory(category);
        int itemsPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / itemsPerPage));
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory gui = Bukkit.createInventory(null, 54,
            ColorUtils.colorize("&8" + titleSuffix + " &7[Trang " + (page + 1) + "/" + totalPages + "]"));
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());
        for (int i = start; i < end; i++) {
            gui.setItem(i - start, createMenuItem(items.get(i)));
        }
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&r").build();
        for (int i = 45; i < 54; i++) gui.setItem(i, border);
        gui.setItem(45, new ItemBuilder(Material.ARROW).setName("&e← Quay Lai").build());
        gui.setItem(48, page > 0 ? new ItemBuilder(Material.ARROW).setGlow(true).setName("&a◀ Trang Trước").build()
            : new ItemBuilder(Material.BARRIER).setName("&7Trang Trước").build());
        gui.setItem(49, new ItemBuilder(Material.PAPER).setName("&eTrang " + (page + 1) + "/" + totalPages)
            .setLore("", "&7Tổng: &e" + items.size() + " &7mặt hàng").build());
        gui.setItem(50, page < totalPages - 1 ? new ItemBuilder(Material.ARROW).setGlow(true).setName("&aTrang Tiếp ▶").build()
            : new ItemBuilder(Material.BARRIER).setName("&7Trang Tiếp").build());
        gui.setItem(53, new ItemBuilder(Material.BARRIER).setName("&cĐóng").build());
        player.openInventory(gui);
        MessageUtils.playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN);
    }

    private ItemStack createMenuItem(ItemDefinition def) {
        // Dùng ItemBuilder.buildFromDefinition() để tạo item với lore đầy đủ
        ItemStack item = ItemBuilder.buildFromDefinition(def);
        // Thêm click instruction
        return item;
    }

    // ==================== CLICK HANDLERS ====================
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!isOwnInventory(event)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        if (event.getRawSlot() < 0) { event.setCancelled(true); event.setResult(Event.Result.DENY); return; }
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        int slot = event.getRawSlot();
        if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (slot >= 45 && !title.contains(TITLE_ADMIN_MAIN)) { handleNavigation(player, title, slot); return; }
        if (title.contains(TITLE_ADMIN_MAIN)) handleAdminMainClick(player, slot);
        else handleCategoryClick(player, title, clicked);
    }

    private void handleNavigation(Player player, String title, int slot) {
        ItemStack clicked = player.getOpenInventory().getTopInventory().getItem(slot);
        if (clicked == null) return;
        String display = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");
        if (display.contains("Quay Lai")) { open(player); return; }
        if (display.contains("Đóng")) { player.closeInventory(); return; }
        int[] state = pageState.get(player.getUniqueId());
        if (state == null) return;
        if (display.contains("Trang Trước") && state[0] > 0) { state[0]--; reopenCurrentPage(player, title); return; }
        if (display.contains("Trang Tiếp")) { state[0]++; reopenCurrentPage(player, title); return; }
    }

    private void reopenCurrentPage(Player player, String title) {
        for (Map.Entry<String, String> entry : CATEGORY_TITLES.entrySet()) {
            if (title.contains(entry.getValue())) {
                pageState.put(player.getUniqueId(), new int[]{pageState.getOrDefault(player.getUniqueId(), new int[]{0})[0]});
                openPaginatedMenu(player, entry.getKey());
                return;
            }
        }
        open(player);
    }

    private void handleAdminMainClick(Player player, int slot) {
        String[] categories = {"pill", "artifact", "skill", "herb", "material", "mount", "currency"};
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i]) {
                pageState.put(player.getUniqueId(), new int[]{0});
                openPaginatedMenu(player, categories[i]);
                return;
            }
        }
        if (slot == 26) player.closeInventory();
    }

    /**
     * Xử lý click vào item trong menu category
     * Match chính xác bằng displayName → ID map
     */
    private void handleCategoryClick(Player player, String title, ItemStack clicked) {
        String stripped = stripColor(clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "");
        if (stripped.contains("Quay Lai")) { open(player); return; }

        // Match chính xác từ map
        String itemId = displayToIdMap.get(stripped);
        if (itemId == null) {
            MessageUtils.send(player, "&cKhông tìm thấy ID item!");
            return;
        }

        ItemDefinition def = plugin.getItemDataLoader().getItem(itemId);
        if (def == null) return;

        giveItemToPlayer(player, def);
    }

    private void giveItemToPlayer(Player player, ItemDefinition def) {
        int amount = "material".equals(def.getCategory()) ? 64 : 1;

        // Dùng ItemBuilder.buildFromDefinition() để tạo item đồng nhất
        ItemStack giveItem = ItemBuilder.buildFromDefinition(def);
        giveItem.setAmount(Math.min(amount, 64));

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(giveItem);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
        MessageUtils.send(player, "&aĐã nhận &f" + def.getName() + " &r&a(" + (amount == 64 ? "x64" : "x1") + ")!");
        MessageUtils.playSound(player, Sound.ENTITY_ITEM_PICKUP);
    }

    private static String stripColor(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) { }
}