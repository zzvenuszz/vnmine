package com.vnmine.spiritfarm;

import com.vnmine.VNMinePlugin;
import com.vnmine.currency.CurrencyManager;
import com.vnmine.item.ItemBuilder;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * SpiritNPCListener - Xử lý tương tác với NPC Linh Điền Sư và Linh Thảo Thương
 * - Linh Điền Sư: Bán block linh điền, hạt giống
 * - Linh Thảo Thương: Mua linh thảo từ người chơi với giá linh thạch
 */
public class SpiritNPCListener implements Listener {

    private final VNMinePlugin plugin;
    private final SpiritFarmManager farmManager;
    private static final String FARMING_MASTER_NAME = "Linh Điền Sư";
    private static final String HERB_MERCHANT_NAME = "Linh Thảo Thương";

    // Lưu session mua bán linh thảo
    private final Map<UUID, HerbSellSession> sellSessions = new HashMap<>();

    public SpiritNPCListener(VNMinePlugin plugin, SpiritFarmManager farmManager) {
        this.plugin = plugin;
        this.farmManager = farmManager;
    }

    @EventHandler
    public void onPlayerInteractNPC(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        String entityName = event.getRightClicked().getCustomName();

        if (entityName == null) return;
        String stripped = ChatColor.stripColor(entityName);

        if (stripped.contains(HERB_MERCHANT_NAME)) {
            event.setCancelled(true);
            openHerbSellShop(player);
        }
    }

    /**
     * Mở shop bán linh thảo (Linh Thảo Thương)
     * Player đặt linh thảo vào ô, hệ thống tính giá và bán
     */
    public void openHerbSellShop(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27,
                ChatColor.translateAlternateColorCodes('&', "&8✦ Linh Thảo Thương ✦"));

        // Ô đặt linh thảo (slot 11)
        ItemBuilder inputSlot = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .setName("&a&lĐặt Linh Thảo Vào Đây")
                .setLore("", "&7Đặt linh thảo vào ô này", "&7Hệ thống sẽ tự động tính giá", "",
                        "&cLưu ý: Chỉ chấp nhận linh thảo", "&ctrồng từ hệ thống Linh Điền");
        gui.setItem(11, inputSlot.build());

        // Ô hiển thị giá (slot 13)
        ItemBuilder priceSlot = new ItemBuilder(Material.GOLD_NUGGET)
                .setName("&6&lGiá Mua")
                .setLore("", "&7Đặt linh thảo vào ô xanh", "&7để xem giá mua");
        gui.setItem(13, priceSlot.build());

        // Nút bán (slot 15)
        ItemBuilder sellBtn = new ItemBuilder(Material.EMERALD)
                .setName("&a&lBÁN")
                .setLore("", "&7Click để bán linh thảo", "&7Ô xanh bên trái");
        gui.setItem(15, sellBtn.build());

        // Viền
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&r").build();
        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) gui.setItem(i, border);
        }

        // Nút đóng
        gui.setItem(22, new ItemBuilder(Material.BARRIER).setName("&c&lĐóng").build());

        player.openInventory(gui);
        sellSessions.put(player.getUniqueId(), new HerbSellSession());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = ChatColor.stripColor(event.getView().getTitle());

        if (!title.contains("Linh Thảo Thương")) return;

        int slot = event.getRawSlot();
        UUID uuid = player.getUniqueId();
        HerbSellSession session = sellSessions.get(uuid);

        if (slot >= 27) return; // Player inventory clicks are allowed
        event.setCancelled(true);

        if (slot == 11) {
            // Cho phép đặt linh thảo vào - chặn nếu không phải linh thảo
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                if (!SpiritHerb.isHerbItem(cursor)) {
                    MessageUtils.send(player, "&cVật phẩm này không phải linh thảo!");
                    return;
                }
                // Cho phép đặt
                event.setCancelled(false);
                // Sau khi đặt xong, cập nhật giá
                Bukkit.getScheduler().runTask(plugin, () -> {
                    updateSellPrice(player, event.getInventory());
                });
            } else {
                // Lấy ra: bỏ qua
                event.setCancelled(false);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    updateSellPrice(player, event.getInventory());
                });
            }
            return;
        }

        if (slot == 15) {
            // Nút bán
            ItemStack herbItem = event.getInventory().getItem(11);
            if (herbItem == null || herbItem.getType() == Material.AIR) {
                MessageUtils.send(player, "&cVui lòng đặt linh thảo vào ô xanh!");
                return;
            }
            if (!SpiritHerb.isHerbItem(herbItem)) {
                MessageUtils.send(player, "&cVật phẩm này không phải linh thảo!");
                return;
            }
            sellHerb(player, herbItem, event.getInventory());
            return;
        }

        if (slot == 22) {
            // Đóng - trả lại linh thảo nếu còn
            ItemStack herbItem = event.getInventory().getItem(11);
            if (herbItem != null && herbItem.getType() != Material.AIR) {
                player.getInventory().addItem(herbItem);
            }
            player.closeInventory();
            sellSessions.remove(uuid);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.contains("Linh Thảo Thương")) return;

        UUID uuid = player.getUniqueId();
        HerbSellSession session = sellSessions.remove(uuid);
        if (session == null) return;

        // Trả lại linh thảo nếu chưa bán
        ItemStack herbItem = event.getInventory().getItem(11);
        if (herbItem != null && herbItem.getType() != Material.AIR) {
            player.getInventory().addItem(herbItem);
        }
    }

    /**
     * Cập nhật giá bán hiển thị
     */
    private void updateSellPrice(Player player, Inventory gui) {
        ItemStack herbItem = gui.getItem(11);
        if (herbItem == null || herbItem.getType() == Material.AIR || !SpiritHerb.isHerbItem(herbItem)) {
            gui.setItem(13, new ItemBuilder(Material.GOLD_NUGGET)
                    .setName("&6&lGiá Mua")
                    .setLore("", "&7Đặt linh thảo vào ô xanh", "&7để xem giá mua").build());
            return;
        }

        String herbId = SpiritHerb.getHerbIdFromItem(herbItem);
        SpiritHerb.HerbQuality quality = SpiritHerb.getQualityFromItem(herbItem);
        SpiritHerb herb = SpiritHerb.getHerb(herbId);

        if (herb == null) {
            gui.setItem(13, new ItemBuilder(Material.GOLD_NUGGET)
                    .setName("&c&lKhông xác định").build());
            return;
        }

        int price = herb.calculateSellPrice(quality);
        int amount = herbItem.getAmount();
        int totalPrice = price * amount;

        // Lưu giá vào session
        UUID uuid = player.getUniqueId();
        HerbSellSession session = sellSessions.get(uuid);
        if (session != null) {
            session.currentPrice = totalPrice;
        }

        gui.setItem(13, new ItemBuilder(Material.GOLD_NUGGET)
                .setName("&6&lGiá Mua")
                .setLore("",
                        "&7Linh thảo: &f" + herb.getName(),
                        "&7Chất lượng: " + quality.getDisplay(),
                        "&7Số lượng: &e" + amount,
                        "&7Đơn giá: &b" + price + " &7Linh Thạch",
                        "&7Tổng: &6&l" + totalPrice + " &7Linh Thạch").build());
    }

    /**
     * Bán linh thảo
     */
    private void sellHerb(Player player, ItemStack herbItem, Inventory gui) {
        String herbId = SpiritHerb.getHerbIdFromItem(herbItem);
        SpiritHerb.HerbQuality quality = SpiritHerb.getQualityFromItem(herbItem);
        SpiritHerb herb = SpiritHerb.getHerb(herbId);

        if (herb == null) {
            MessageUtils.send(player, "&cLỗi: Không tìm thấy loại linh thảo!");
            return;
        }

        int price = herb.calculateSellPrice(quality);
        int amount = herbItem.getAmount();
        int totalPrice = price * amount;

        // Trừ linh thảo
        gui.setItem(11, null);

        // Cộng linh thạch
        if (plugin.getCurrencyManager() != null) {
            plugin.getCurrencyManager().deposit(player, totalPrice);
        }

        // Cập nhật lại giá
        updateSellPrice(player, gui);

        MessageUtils.send(player, "&a✦ Đã bán &e" + amount + "x &f" + herb.getName()
                + " &7(" + quality.getDisplay() + "&7) với giá &6" + totalPrice + " Linh Thạch!");
    }

    /**
     * Mở shop Linh Điền cho người chơi (Linh Điền Sư)
     */
    public void openFarmShop(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54,
                ChatColor.translateAlternateColorCodes('&', "&8✦ Linh Điền Sư ✦"));

        int slot = 0;

        // Bán block linh điền (12 loại)
        String[] gradeNames = {
            "Hoàng Hạ", "Hoàng Trung", "Hoàng Thượng",
            "Huyền Hạ", "Huyền Trung", "Huyền Thượng",
            "Địa Hạ", "Địa Trung", "Địa Thượng",
            "Thiên Hạ", "Thiên Trung", "Thiên Thượng"
        };
        int[] prices = {10, 20, 30, 50, 75, 100, 150, 200, 300, 500, 750, 1000};

        for (int g = 0; g < 12; g++) {
            ItemStack blockItem = farmManager.createFarmBlockItem(g);
            ItemBuilder builder = new ItemBuilder(blockItem.clone());
            builder.addLore("");
            builder.addLore("&7Giá: &b" + prices[g] + " Linh Thạch");
            builder.addLore("&eClick để mua!");
            builder.setPersistentData("vnmine_shop_farm_block", String.valueOf(g));
            builder.setPersistentData("vnmine_shop_price", String.valueOf(prices[g]));
            gui.setItem(slot, builder.build());
            slot++;
        }

        // Bán hạt giống (20 loại - tất cả các loại linh thảo)
        int[] seedPrices = {3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 8, 8, 8, 10, 8, 15, 15, 15, 15, 15};

        for (int i = 0; i < SpiritHerb.getHerbIds().length && slot < 54 && i < seedPrices.length; i++) {
            ItemStack seedItem = farmManager.createSeedItem(SpiritHerb.getHerbIds()[i], 1);
            if (seedItem != null) {
                ItemBuilder builder = new ItemBuilder(seedItem.clone());
                builder.addLore("");
                builder.addLore("&7Giá: &b" + seedPrices[i] + " Linh Thạch");
                builder.addLore("&eClick để mua!");
                builder.setPersistentData("vnmine_shop_seed", SpiritHerb.getHerbIds()[i]);
                builder.setPersistentData("vnmine_shop_price", String.valueOf(seedPrices[i]));
                gui.setItem(slot, builder.build());
                slot++;
            }
        }

        // Nút đóng
        gui.setItem(49, new ItemBuilder(Material.BARRIER)
                .setName("&c&lĐóng").build());

        player.openInventory(gui);
    }

    /**
     * Session lưu thông tin bán linh thảo
     */
    private static class HerbSellSession {
        int currentPrice = 0;
    }
}