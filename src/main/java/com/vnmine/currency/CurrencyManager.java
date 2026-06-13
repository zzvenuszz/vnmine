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

public class CurrencyManager {

    private final VNMinePlugin plugin;
    private boolean enabled;

    // 3 tiers of currency
    private CurrencyTier tierHa;
    private CurrencyTier tierTrung;
    private CurrencyTier tierThuong;

    // Exchange rate
    private int exchangeDowngradeLossPercent;

    // Drop config
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
        if (currency == null) { enabled = false; return; }

        enabled = currency.getBoolean("enabled", true);
        exchangeDowngradeLossPercent = currency.getInt("exchange-downgrade-loss-percent", 5);

        // Load tiers
        ConfigurationSection tiers = currency.getConfigurationSection("tiers");
        if (tiers != null) {
            tierHa = loadTier(tiers.getConfigurationSection("HA"), "PRISMARINE_SHARD", "&bLinh Thach Ha Pham", 1);
            tierTrung = loadTier(tiers.getConfigurationSection("TRUNG"), "DARK_PRISMARINE", "&dLinh Thach Trung Pham", 100);
            tierThuong = loadTier(tiers.getConfigurationSection("THUONG"), "HEART_OF_THE_SEA", "&6&lLinh Thach Thuong Pham", 10000);
        } else {
            tierHa = new CurrencyTier(Material.PRISMARINE_SHARD, "&bLinh Thach", Arrays.asList("&7Tiền tệ tu tiên"), 1);
            tierTrung = new CurrencyTier(Material.DARK_PRISMARINE, "&dLinh Thach Trung Pham", Arrays.asList("&7Linh thach trung cap"), 100);
            tierThuong = new CurrencyTier(Material.HEART_OF_THE_SEA, "&6&lLinh Thach Thuong Pham", Arrays.asList("&7Linh thach quy hiem"), 10000);
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

    private CurrencyTier loadTier(ConfigurationSection section, String defaultMat, String defaultName, int defaultValue) {
        if (section == null) return new CurrencyTier(Material.getMaterial(defaultMat), defaultName, Arrays.asList("&7Currency"), defaultValue);
        Material mat = Material.getMaterial(section.getString("material", defaultMat));
        if (mat == null) mat = Material.getMaterial(defaultMat);
        String name = section.getString("name", defaultName);
        List<String> lore = section.getStringList("lore");
        int value = section.getInt("value", defaultValue);
        return new CurrencyTier(mat, name, lore, value);
    }

    // ========== CREATE CURRENCY ITEMS ==========

    public ItemStack createCurrencyHa(int amount) {
        return createTierItem(tierHa, amount);
    }

    public ItemStack createCurrencyTrung(int amount) {
        return createTierItem(tierTrung, amount);
    }

    public ItemStack createCurrencyThuong(int amount) {
        return createTierItem(tierThuong, amount);
    }

    private ItemStack createTierItem(CurrencyTier tier, int amount) {
        return new ItemBuilder(tier.material)
                .setName(tier.name)
                .setLore(tier.lore)
                .setAmount(Math.min(amount, 64))
                .build();
    }

    // ========== DETECT TIER ==========

    public CurrencyTier detectTier(ItemStack item) {
        if (item == null) return null;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = item.getItemMeta().getDisplayName();
            if (isSameTier(name, tierThuong)) return tierThuong;
            if (isSameTier(name, tierTrung)) return tierTrung;
            if (isSameTier(name, tierHa)) return tierHa;
        }
        return null;
    }

    private boolean isSameTier(String itemName, CurrencyTier tier) {
        String expected = ColorUtils.stripColor(ColorUtils.colorize(tier.name));
        return ColorUtils.stripColor(itemName).equals(expected);
    }

    // ========== COUNT BY TIER ==========

    public int countCurrencyHa(Player player) { return countTier(player, tierHa); }
    public int countCurrencyTrung(Player player) { return countTier(player, tierTrung); }
    public int countCurrencyThuong(Player player) { return countTier(player, tierThuong); }

    private int countTier(Player player, CurrencyTier tier) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            CurrencyTier detected = detectTier(item);
            if (detected != null && detected.material == tier.material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    // ========== COUNT TOTAL (in Ha value) ==========

    public int countTotalValue(Player player) {
        return countCurrencyHa(player) * tierHa.value
             + countCurrencyTrung(player) * tierTrung.value
             + countCurrencyThuong(player) * tierThuong.value;
    }

    // ========== WITHDRAW ==========

    public boolean withdrawHa(Player player, int amount) { return withdrawTier(player, tierHa, amount); }
    public boolean withdrawTrung(Player player, int amount) { return withdrawTier(player, tierTrung, amount); }
    public boolean withdrawThuong(Player player, int amount) { return withdrawTier(player, tierThuong, amount); }

    private boolean withdrawTier(Player player, CurrencyTier tier, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            CurrencyTier detected = detectTier(item);
            if (detected != null && detected.material == tier.material) {
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

    // ========== DEPOSIT ==========

    public void depositHa(Player player, int amount) { depositTier(player, tierHa, amount); }
    public void depositTrung(Player player, int amount) { depositTier(player, tierTrung, amount); }
    public void depositThuong(Player player, int amount) { depositTier(player, tierThuong, amount); }

    private void depositTier(Player player, CurrencyTier tier, int amount) {
        ItemStack currency = createTierItem(tier, amount);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(currency);
        for (ItemStack drop : leftover.values()) player.getWorld().dropItemNaturally(player.getLocation(), drop);
    }

    // ========== EXCHANGE ==========

    /**
     * Exchange currency: upgrade or downgrade
     * @param player the player
     * @param fromTier "HA", "TRUNG", "THUONG"
     * @param toTier "HA", "TRUNG", "THUONG"
     * @param amount amount of fromTier to convert
     * @return true if successful
     */
    public boolean exchange(Player player, String fromTier, String toTier, int amount) {
        CurrencyTier from = getTier(fromTier);
        CurrencyTier to = getTier(toTier);
        if (from == null || to == null) return false;
        if (from.value == to.value) return false;

        // Check if player has enough
        int fromCount = countTier(player, from);
        if (fromCount < amount) return false;

        // Calculate output
        double ratio = (double) from.value / (double) to.value;
        int output = (int) Math.floor(amount * ratio);

        // Apply loss when going to smaller denomination
        if (from.value > to.value) {
            double loss = 1.0 - (exchangeDowngradeLossPercent / 100.0);
            output = (int) Math.floor(output * loss);
        }

        if (output <= 0) return false;

        // Withdraw and deposit
        if (!withdrawTier(player, from, amount)) return false;
        depositTier(player, to, output);

        return true;
    }

    // Backward-compatible methods for existing code
    public boolean withdraw(Player player, int amount) { return withdrawHa(player, amount); }
    public void deposit(Player player, int amount) { depositHa(player, amount); }
    public int countCurrency(Player player) { return countCurrencyHa(player); }
    public boolean isCurrencyItem(ItemStack item) { return detectTier(item) != null; }
    public int parseAmount(String amountStr) {
        try {
            if (amountStr.contains("-")) {
                String[] parts = amountStr.split("-");
                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);
                return min + new Random().nextInt(max - min + 1);
            }
            return Integer.parseInt(amountStr);
        } catch (NumberFormatException e) { return 1; }
    }
    public ItemStack createCurrency(int amount) { return createCurrencyHa(amount); }

    public CurrencyTier getTier(String tierId) {
        switch (tierId.toUpperCase()) {
            case "HA": return tierHa;
            case "TRUNG": return tierTrung;
            case "THUONG": return tierThuong;
            default: return null;
        }
    }

    // ========== GETTERS ==========

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public CurrencyTier getTierHa() { return tierHa; }
    public CurrencyTier getTierTrung() { return tierTrung; }
    public CurrencyTier getTierThuong() { return tierThuong; }
    public int getExchangeDowngradeLossPercent() { return exchangeDowngradeLossPercent; }

    public double getMobDropChance() { return mobDropChance; }
    public String getMobDropAmount() { return mobDropAmount; }
    public double getEliteDropChance() { return eliteDropChance; }
    public String getEliteDropAmount() { return eliteDropAmount; }
    public double getBossDropChance() { return bossDropChance; }
    public String getBossDropAmount() { return bossDropAmount; }
    public double getOreDropChance() { return oreDropChance; }
    public int getOreDropAmount() { return oreDropAmount; }

    // ========== TIER CLASS ==========

    public static class CurrencyTier {
        public final Material material;
        public final String name;
        public final List<String> lore;
        public final int value;

        public CurrencyTier(Material material, String name, List<String> lore, int value) {
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.value = value;
        }
    }
}