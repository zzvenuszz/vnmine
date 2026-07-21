package com.vnmine.gui.framework;

import com.vnmine.VNMinePlugin;
import com.vnmine.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * MenuSlot - Định nghĩa một slot trong GUI với texture support từ resource pack.
 * 
 * Mỗi slot có:
 * - Material: Material fallback khi không có resource pack
 * - customModelData: Custom model data cho resource pack (ItemModel trong 1.21+)
 * - textureKey: Key để tra cứu trong config
 * 
 * Texture config trong config.yml:
 *   gui.textures.border:
 *     material: BLACK_STAINED_GLASS_PANE
 *     custom-model-data: 1001
 */
public class MenuSlot {

    private final Material material;
    private final int customModelData;
    private final String textureKey;
    private final String displayName;

    /**
     * Tạo MenuSlot với material và custom model data
     */
    public MenuSlot(Material material, int customModelData, String textureKey, String displayName) {
        this.material = material;
        this.customModelData = customModelData;
        this.textureKey = textureKey;
        this.displayName = displayName;
    }

    /**
     * Tạo MenuSlot chỉ với material (không texture)
     */
    public MenuSlot(Material material, String displayName) {
        this(material, 0, null, displayName);
    }

    /**
     * Tạo ItemStack từ MenuSlot này
     */
    public ItemStack createItem() {
        ItemBuilder builder = new ItemBuilder(material)
                .setName(displayName);
        
        if (customModelData > 0) {
            builder.setCustomModelData(customModelData);
        }
        
        return builder.build();
    }

    /**
     * Tạo ItemStack từ MenuSlot này với lore
     */
    public ItemStack createItem(String... lore) {
        ItemBuilder builder = new ItemBuilder(material)
                .setName(displayName)
                .setLore(lore);
        
        if (customModelData > 0) {
            builder.setCustomModelData(customModelData);
        }
        
        return builder.build();
    }

    /**
     * Load MenuSlot từ config dựa trên key
     * Ví dụ: gui.textures.border
     */
    public static MenuSlot fromConfig(VNMinePlugin plugin, String configPath, Material defaultMaterial, String displayName) {
        FileConfiguration config = plugin.getConfig();
        String matName = config.getString(configPath + ".material", defaultMaterial.name());
        int cmd = config.getInt(configPath + ".custom-model-data", 0);
        
        Material material;
        try {
            material = Material.valueOf(matName);
        } catch (IllegalArgumentException e) {
            material = defaultMaterial;
        }
        
        return new MenuSlot(material, cmd, configPath, displayName);
    }

    /**
     * Tạo border item mặc định từ config hoặc fallback
     */
    public static ItemStack createBorderItem(VNMinePlugin plugin) {
        MenuSlot slot = fromConfig(plugin, "gui.textures.border", 
                Material.BLACK_STAINED_GLASS_PANE, "&r");
        return slot.createItem();
    }

    // Getters
    public Material getMaterial() { return material; }
    public int getCustomModelData() { return customModelData; }
    public String getTextureKey() { return textureKey; }
    public String getDisplayName() { return displayName; }
}