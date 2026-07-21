package com.vnmine.item;

import com.vnmine.util.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.potion.PotionType;
import org.bukkit.inventory.meta.PotionMeta;

/**
 * ItemBuilder - Xây dựng item với &color, lore, enchants
 * Hỗ trợ đầy đủ & màu sắc, tự động chuyển đổi
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    /**
     * Tạo ItemBuilder từ Material
     */
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    /**
     * Tạo ItemBuilder từ ItemStack có sẵn
     */
    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = item.getItemMeta();
    }

    // ==================== TÊN ====================

    /**
     * Set tên item với &color
     */
    public ItemBuilder setName(String name) {
        if (meta != null && name != null) {
            meta.setDisplayName(ColorUtils.colorize(name));
        }
        return this;
    }

    // ==================== LORE ====================

    /**
     * Set lore từ List với &color
     */
    public ItemBuilder setLore(List<String> lore) {
        if (meta != null && lore != null) {
            meta.setLore(ColorUtils.colorize(lore));
        }
        return this;
    }

    /**
     * Set lore từ mảng String với &color
     */
    public ItemBuilder setLore(String... lore) {
        if (meta != null && lore != null) {
            meta.setLore(ColorUtils.colorize(Arrays.asList(lore)));
        }
        return this;
    }

    /**
     * Thêm dòng lore với &color
     */
    public ItemBuilder addLore(String line) {
        if (meta != null && line != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(ColorUtils.colorize(line));
            meta.setLore(lore);
        }
        return this;
    }

    // ==================== SỐ LƯỢNG ====================

    /**
     * Set số lượng
     */
    public ItemBuilder setAmount(int amount) {
        item.setAmount(Math.max(1, Math.min(amount, 64)));
        return this;
    }

    // ==================== ENCHANT ====================

    /**
     * Thêm enchant
     */
    public ItemBuilder addEnchant(Enchantment enchant, int level) {
        if (meta != null && enchant != null) {
            meta.addEnchant(enchant, level, true);
        }
        return this;
    }

    /**
     * Thêm nhiều enchant
     */
    public ItemBuilder addEnchants(Map<Enchantment, Integer> enchants) {
        if (meta != null && enchants != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }
        return this;
    }

    // ==================== ITEM FLAGS ====================

    /**
     * Thêm ItemFlag (ẩn enchants, attributes...)
     */
    public ItemBuilder addItemFlags(ItemFlag... flags) {
        if (meta != null && flags != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    /**
     * Ẩn tất cả enchants
     */
    public ItemBuilder hideEnchants() {
        return addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    /**
     * Ẩn tất cả attributes
     */
    public ItemBuilder hideAttributes() {
        return addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    }

    /**
     * Ẩn tất cả
     */
    public ItemBuilder hideAll() {
        return addItemFlags(
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_UNBREAKABLE,
                    ItemFlag.HIDE_DYE,
                    ItemFlag.HIDE_ARMOR_TRIM
        );
    }

    // ==================== UNBREAKABLE ====================

    /**
     * Set unbreakable
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
        }
        return this;
    }

    // ==================== CUSTOM MODEL DATA ====================

    /**
     * Set custom model data (cho resource pack)
     */
    public ItemBuilder setCustomModelData(int data) {
        if (meta != null) {
            meta.setCustomModelData(data);
        }
        return this;
    }

    // ==================== POTION COLOR ====================

    /**
     * Set màu cho lọ thuốc (Potion)
     */
    public ItemBuilder setPotionColor(org.bukkit.Color color) {
        if (meta instanceof PotionMeta && color != null) {
            ((PotionMeta) meta).setColor(color);
        }
        return this;
    }

    // ==================== LEATHER ARMOR COLOR ====================

    /**
     * Set màu cho leather armor
     */
    public ItemBuilder setLeatherColor(org.bukkit.Color color) {
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
        }
        return this;
    }

    // ==================== SKULL ====================

    /**
     * Set skull owner
     */
    public ItemBuilder setSkullOwner(String owner) {
        if (meta instanceof SkullMeta && owner != null) {
            ((SkullMeta) meta).setOwner(owner);
        }
        return this;
    }

    // ==================== GLOW ====================

    /**
     * Thêm hiệu ứng glow (ảo)
     */
    public ItemBuilder setGlow(boolean glow) {
        if (glow && meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    // ==================== PERSISTENT DATA (NBT) ====================

    private static Plugin pluginInstance = null;

    /**
     * Set plugin instance để dùng NamespacedKey
     */
    public static void setPlugin(Plugin plugin) {
        pluginInstance = plugin;
    }

    /**
     * Set persistent data vào item (NBT tag)
     */
    public ItemBuilder setPersistentData(String key, String value) {
        if (meta != null && pluginInstance != null) {
            NamespacedKey namespacedKey = new NamespacedKey(pluginInstance, key);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(namespacedKey, PersistentDataType.STRING, value);
        }
        return this;
    }

    /**
     * Kiểm tra item có persistent data không (static)
     */
    public static boolean hasPersistentData(ItemStack item, String key) {
        if (item == null || pluginInstance == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey namespacedKey = new NamespacedKey(pluginInstance, key);
        return meta.getPersistentDataContainer().has(namespacedKey, PersistentDataType.STRING);
    }

    /**
     * Lấy persistent data từ item (static)
     */
    public static String getPersistentData(ItemStack item, String key) {
        if (item == null || pluginInstance == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey namespacedKey = new NamespacedKey(pluginInstance, key);
        return meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
    }

    // ==================== BUILD ====================

    /**
     * Build ItemStack hoàn chỉnh
     */
    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }

    // ==================== BUILD FROM DEFINITION ====================

    /**
     * Xây dựng ItemStack từ ItemDefinition
     * Tự động sinh lore từ tất cả thông tin có sẵn
     */
    public static ItemStack buildFromDefinition(ItemDefinition def) {
        if (def == null) return new ItemStack(Material.STONE);
        
        ItemBuilder builder = new ItemBuilder(def.getMaterial())
                .setName(def.getName())
                .setGlow(true)
                .setCustomModelData(def.getCustomModelData());
        
        List<String> lore = new ArrayList<>();
        
        // Thông tin cơ bản
        if (def.getType() != null && !def.getType().isEmpty()) {
            lore.add("&8✦ " + def.getType());
        }
        if (def.getRank() != null && !def.getRank().isEmpty()) {
            lore.add("&7Phẩm cấp: " + def.getRank());
        }
        if (def.getElement() != null && !def.getElement().isEmpty()) {
            lore.add("&7Thuộc tính: " + def.getElement());
        }
        if (def.getRarity() != null && !def.getRarity().isEmpty()) {
            lore.add("&7Độ hiếm: " + def.getRarity());
        }
        
        // Effects
        if (!def.getEffects().isEmpty()) {
            lore.add("");
            lore.add("&6&l✦ Hiệu ứng:");
            lore.addAll(def.getEffects());
        }
        
        // Grow environment (cho linh thảo)
        if (!def.getGrowEnvironment().isEmpty()) {
            lore.add("");
            lore.add("&a&l✦ Môi trường sinh trưởng:");
            lore.addAll(def.getGrowEnvironment());
        }
        
        // Compatible alchemy (cho linh thảo)
        if (!def.getCompatibleAlchemy().isEmpty()) {
            lore.add("");
            lore.add("&b&l✦ Có thể luyện:");
            lore.addAll(def.getCompatibleAlchemy());
        }
        
        // Ingredients (cho recipe)
        if (!def.getIngredients().isEmpty()) {
            lore.add("");
            lore.add("&e&l✦ Nguyên liệu:");
            for (String ing : def.getIngredients()) {
                lore.add(" &7- " + ing);
            }
        }
        
        // Required skills
        if (!def.getRequiredSkills().isEmpty()) {
            lore.add("");
            lore.add("&c&l✦ Yêu cầu kỹ năng:");
            for (String req : def.getRequiredSkills()) {
                String[] parts = req.split(":");
                String skillId = parts.length > 0 ? parts[0] : req;
                String level = parts.length > 1 ? " cấp " + parts[1] : "";
                lore.add(" &7- " + skillId + level);
            }
        }
        
        // Required level
        if (def.getRequiredLevel() > 0) {
            lore.add("&7Yêu cầu cấp độ: &e" + def.getRequiredLevel());
        }
        
        // Cooking time
        if (def.getCookingTime() > 0) {
            lore.add("&7Thời gian chế tạo: &e" + def.getCookingTime() + "s");
        }
        if (def.getSuccessChance() > 0) {
            lore.add("&7Tỷ lệ thành công: &e" + def.getSuccessChance() + "%");
        }
        
        // Side effects
        if (!def.getSideEffects().isEmpty()) {
            lore.add("");
            lore.add("&c&l✦ Tác dụng phụ:");
            lore.addAll(def.getSideEffects());
        }
        
        // Custom lore
        if (!def.getLore().isEmpty()) {
            lore.add("");
            lore.addAll(def.getLore());
        }
        
        // Hướng dẫn sử dụng
        if (def.isPill()) {
            lore.add("");
            lore.add("&a✦ Click phải để sử dụng!");
        } else if (def.isHerb()) {
            lore.add("");
            lore.add("&7Dùng để luyện đan hoặc bán cho NPC");
        }
        
        builder.setLore(lore);
        
        // Persistent data
        builder.setPersistentData("vnmine_item", "true");
        builder.setPersistentData("vnmine_item_id", def.getId());
        
        return builder.build();
    }

    // ==================== STATIC HELPERS ====================

    /**
     * Tạo item nhanh
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        return new ItemBuilder(material)
                .setName(name)
                .setLore(lore)
                .build();
    }

    /**
     * Tạo item với glow
     */
    public static ItemStack createGlowItem(Material material, String name, String... lore) {
        return new ItemBuilder(material)
                .setName(name)
                .setLore(lore)
                .setGlow(true)
                .build();
    }

    /**
     * Tạo item head người chơi
     */
    public static ItemStack createPlayerHead(String playerName, String displayName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwner(playerName);
            skullMeta.setDisplayName(ColorUtils.colorize(displayName));
            skull.setItemMeta(skullMeta);
        }
        return skull;
    }
}