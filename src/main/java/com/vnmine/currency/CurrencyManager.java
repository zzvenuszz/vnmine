package com.vnmine.currency;

import com.vnmine.VNMinePlugin;
import com.vnmine.item.ItemBuilder;
import com.vnmine.util.ColorUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * CurrencyManager - Quản lý Linh Thạch (tiền tệ tu tiên)
 * Linh Thạch là item PRISMARINE_SHARD có thể trao đổi
 */
public class CurrencyManager {

    private final VNMinePlugin plugin;
    private boolean enabled;

    private String itemName;
    private List<String> itemLore;
    private Material itemMaterial;

    // Cấu hình drop
    private double mobDropChance;
    private String mobDropAmount;
    private double eliteDropChance;
    private String eliteDropAmount;
    private double bossDropChance;
    private String bossDropAmount;
    private double oreDropChance;
    private int oreDropAmount;

    public CurrencyManager(VNMinePlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection currency = config.getConfigurationSection("currency");
        if (currency == null) {
            enabled = false;
            return;
        }

        enabled = currency.getBoolean("enabled", true);

        ConfigurationSection itemSection = currency.getConfigurationSection("item");
        if (itemSection != null) {
            itemMaterial = Material.getMaterial(itemSection.getString("material", "PRISMARINE_SHARD"));
            if (itemMaterial == null) itemMaterial = Material.PRISMARINE_SHARD;
            itemName = itemSection.getString("name", "&bLinh Thạch");
            itemLore = itemSection.getStringList("lore");
            if (itemLore.isEmpty()) {
                itemLore = Arrays.asList("&7Tiền tệ tu tiên", "&7Dùng để mua vật phẩm từ NPC");
            }
        }

        ConfigurationSection drops = currency.getConfigurationSection("drops");
        if (drops != null) {
            mobDropChance = drops.getDouble("kill-mob.chance", 10.0);
            mobDropAmount = drops.getString("kill-mob.amount", "1-3");
            eliteDropChance = drops.getDouble("kill-elite.chance", 100.0);
            eliteDropAmount = drops.getString("kill-elite.amount", "5-10");
            bossDropChance = drops.getDouble("kill-boss.chance", 100.0);
            bossDropAmount = drops.getString("kill-boss.amount", "20-50");
            oreDropChance = drops.getDouble("break-ore.chance", 5.0);
            oreDropAmount = drops.getInt("break-ore.amount", 1);
        }
    }

    /**
     * Tạo ItemStack Linh Thạch với số lượng
     */
    public ItemStack createCurrency(int amount) {
        return new ItemBuilder(itemMaterial == null ? Material.PRISMARINE_SHARD : itemMaterial)
                .setName(itemName)
                .setLore(itemLore)
                .setAmount(Math.min(amount, 64))
                .build();
    }

    /**
     * Tính số lượng random từ string "1-3"
     */
    public int parseAmount(String amountStr) {
        try {
            if (amountStr.contains("-")) {
                String[] parts = amountStr.split("-");
                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
                return min + new Random().nextInt(max - min + 1);
            }
            return Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * Kiểm tra player có đủ linh thạch không
     */
    public boolean hasEnough(Player player, int amount) {
        int count = countCurrency(player);
        return count >= amount;
    }

    /**
     * Đếm số linh thạch trong inventory
     */
    public int countCurrency(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isCurrencyItem(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Kiểm tra item có phải linh thạch không
     */
    public boolean isCurrencyItem(ItemStack item) {
        if (item == null || item.getType() != itemMaterial) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String displayName = ColorUtils.stripColor(item.getItemMeta().getDisplayName());
        String expected = ColorUtils.stripColor(ColorUtils.colorize(itemName));
        return displayName.equals(expected);
    }

    /**
     * Trừ linh thạch khỏi inventory
     */
    public boolean withdraw(Player player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && isCurrencyItem(item)) {
                int stackAmount = item.getAmount();
                if (stackAmount <= remaining) {
                    remaining -= stackAmount;
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(stackAmount - remaining);
                    remaining = 0;
                    break;
                }
                if (remaining <= 0) break;
            }
        }
        return remaining <= 0;
    }

    /**
     * Thêm linh thạch vào inventory
     */
    public void deposit(Player player, int amount) {
        ItemStack currency = createCurrency(amount);
        player.getInventory().addItem(currency);
    }

    public double getMobDropChance() { return mobDropChance; }
    public String getMobDropAmount() { return mobDropAmount; }
    public double getEliteDropChance() { return eliteDropChance; }
    public String getEliteDropAmount() { return eliteDropAmount; }
    public double getBossDropChance() { return bossDropChance; }
    public String getBossDropAmount() { return bossDropAmount; }
    public double getOreDropChance() { return oreDropChance; }
    public int getOreDropAmount() { return oreDropAmount; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}