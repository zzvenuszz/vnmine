package com.vnmine.item;

import com.vnmine.VNMinePlugin;
import com.vnmine.util.ColorUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * ItemDataLoader - Load định nghĩa item từ các file YML trong thư mục items/
 * 
 * Tự động load tất cả file .yml trong resources/items/ khi plugin start.
 * Hỗ trợ reload lệnh.
 */
public class ItemDataLoader {

    private final VNMinePlugin plugin;
    private final Map<String, ItemDefinition> allItems = new LinkedHashMap<>();
    private final GradeDefinitions grades = new GradeDefinitions();
    
    // Danh sách các file YML cần load
    private static final String[] ITEM_FILES = {
        "grades.yml",
        "herbs.yml",
        "pills.yml", 
        "artifacts.yml",
        "skills.yml",
        "materials.yml",
        "mounts.yml",
        "currency.yml"
    };
    
    // Phân loại item theo category
    private final Map<String, List<String>> itemsByCategory = new HashMap<>();

    public ItemDataLoader(VNMinePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load tất cả item definitions từ file YML
     */
    public void loadAll() {
        allItems.clear();
        itemsByCategory.clear();
        
        // Đảm bảo thư mục items/ tồn tại
        File itemsDir = new File(plugin.getDataFolder(), "items");
        if (!itemsDir.exists()) {
            itemsDir.mkdirs();
        }
        
        int totalLoaded = 0;
        
        for (String fileName : ITEM_FILES) {
            int count = loadFile(fileName);
            if (count > 0) {
                totalLoaded += count;
                plugin.getLogger().info("[ItemLoader] Đã load " + count + " item từ " + fileName);
            }
        }
        
        plugin.getLogger().info("[ItemLoader] Tổng cộng: " + totalLoaded + " items đã load từ " + ITEM_FILES.length + " files");
        
        // Thống kê theo category
        for (Map.Entry<String, List<String>> entry : itemsByCategory.entrySet()) {
            plugin.getLogger().info("[ItemLoader]  - " + entry.getKey() + ": " + entry.getValue().size() + " items");
        }
    }

    /**
     * Load một file YML cụ thể
     */
    private int loadFile(String fileName) {
        File file = new File(plugin.getDataFolder(), "items/" + fileName);
        
        // Nếu file chưa tồn tại, copy từ resources
        if (!file.exists()) {
            plugin.saveResource("items/" + fileName, false);
        }
        
        // Special handling for grades.yml - not an item file
        if (fileName.equals("grades.yml")) {
            grades.load(file);
            plugin.getLogger().info("[ItemLoader] Đã load grades: " + grades.getArtifactGradeCount() + " artifact tiers, "
                + grades.getSkillGradeCount() + " skill grades");
            return 0;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Đọc từng item trong file
        int count = 0;
        for (String key : config.getKeys(false)) {
            try {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null) continue;
                
                ItemDefinition def = parseItem(key, section);
                if (def != null) {
                    allItems.put(key.toUpperCase(), def);
                    
                    // Phân loại theo category
                    String cat = def.getCategory() != null ? def.getCategory() : determineCategory(def);
                    itemsByCategory.computeIfAbsent(cat, k -> new ArrayList<>()).add(key.toUpperCase());
                    
                    count++;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[ItemLoader] Lỗi load item '" + key + "' trong " + fileName + ": " + e.getMessage());
            }
        }
        
        return count;
    }

    /**
     * Xác định category dựa vào type nếu không có category trong config
     */
    private String determineCategory(ItemDefinition def) {
        if (def.isPill()) return "pill";
        if (def.isHerb()) return "herb";
        if (def.isArtifact()) return "artifact";
        if (def.isSkill()) return "skill";
        if (def.isMaterial()) return "material";
        if (def.isMount()) return "mount";
        if (def.isCurrency()) return "currency";
        return "other";
    }

    /**
     * Parse một section YML thành ItemDefinition
     */
    private ItemDefinition parseItem(String id, ConfigurationSection section) {
        ItemDefinition def = new ItemDefinition();
        def.setId(id);
        
        // Thông tin cơ bản
        def.setName(ColorUtils.colorize(section.getString("name", "&f" + id)));
        def.setType(section.getString("type", ""));
        def.setRank(ColorUtils.colorize(section.getString("rank", "")));
        def.setElement(section.getString("element", ""));
        def.setAge(section.getString("age", ""));
        def.setRarity(section.getString("rarity", ""));
        def.setMaterial(section.getString("material", "STONE"));
        def.setCustomModelData(section.getInt("custom-model-data", 0));
        def.setCategory(section.getString("category", ""));
        
        // Thông số đan dược
        def.setBaseRecover(section.getInt("base-recover", 0));
        def.setBaseHeal(section.getInt("base-heal", 0));
        def.setBaseRegen(section.getInt("base-regen", 0));
        def.setBaseDuration(section.getInt("base-duration", 0));
        def.setBaseDmg(section.getInt("base-dmg", 0));
        def.setBaseExp(section.getInt("base-exp", 0));
        def.setScaleWithGrade(section.getBoolean("scale-with-grade", false));
        def.setScaleDuration(section.getBoolean("scale-duration", false));
        
        // Thông số chế tạo
        def.setCookingTime(section.getInt("cooking-time", 0));
        def.setSuccessChance(section.getDouble("success-chance", 0));
        def.setRequiredLevel(section.getInt("required-level", 0));
        def.setRequiredSkills(section.getStringList("required-skills"));
        def.setIngredients(section.getStringList("ingredients"));
        
        // Danh sách (hỗ trợ mã màu &)
        def.setEffects(colorizeList(section.getStringList("effects")));
        def.setSideEffects(colorizeList(section.getStringList("side-effects")));
        def.setGrowEnvironment(colorizeList(section.getStringList("grow-environment")));
        def.setCompatibleAlchemy(colorizeList(section.getStringList("compatible-alchemy")));
        def.setLore(colorizeList(section.getStringList("lore")));
        def.setSkillIds(section.getStringList("skill-ids"));
        
        // Thông tin bổ sung
        ConfigurationSection addSection = section.getConfigurationSection("additional");
        if (addSection != null) {
            Map<String, String> additional = new HashMap<>();
            for (String key : addSection.getKeys(false)) {
                additional.put(key, addSection.getString(key, ""));
            }
            def.setAdditional(additional);
        }
        
        return def;
    }
    
    /**
     * Colorize tất cả string trong list
     */
    private List<String> colorizeList(List<String> list) {
        if (list == null) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (String s : list) {
            result.add(ColorUtils.colorize(s));
        }
        return result;
    }

    /**
     * Lấy ItemDefinition theo ID
     */
    public ItemDefinition getItem(String id) {
        if (id == null) return null;
        return allItems.get(id.toUpperCase());
    }

    /**
     * Lấy tất cả item IDs
     */
    public Set<String> getAllItemIds() {
        return allItems.keySet();
    }

    /**
     * Lấy tất cả items
     */
    public Map<String, ItemDefinition> getAllItems() {
        return Collections.unmodifiableMap(allItems);
    }

    /**
     * Lấy item IDs theo category
     */
    public List<String> getItemIdsByCategory(String category) {
        return itemsByCategory.getOrDefault(category.toLowerCase(), Collections.emptyList());
    }

    /**
     * Lấy tất cả category
     */
    public Set<String> getAllCategories() {
        return itemsByCategory.keySet();
    }

    /**
     * Lấy danh sách items theo danh sách IDs
     */
    public List<ItemDefinition> getItems(List<String> ids) {
        List<ItemDefinition> result = new ArrayList<>();
        for (String id : ids) {
            ItemDefinition def = getItem(id);
            if (def != null) result.add(def);
        }
        return result;
    }

    /**
     * Lấy danh sách items theo category
     */
    public List<ItemDefinition> getItemsByCategory(String category) {
        List<String> ids = getItemIdsByCategory(category);
        return getItems(ids);
    }

    /**
     * Lấy danh sách items có công thức chế tạo
     */
    public List<ItemDefinition> getCraftableItems() {
        List<ItemDefinition> result = new ArrayList<>();
        for (ItemDefinition def : allItems.values()) {
            if (def.hasRecipe()) result.add(def);
        }
        return result;
    }

    /**
     * Xây dựng map display name → item ID để match chính xác
     */
    public Map<String, String> buildDisplayNameToIdMap() {
        Map<String, String> map = new HashMap<>();
        for (ItemDefinition def : allItems.values()) {
            String stripped = stripColorRaw(def.getName());
            map.put(stripped, def.getId());
        }
        return map;
    }
    
    private String stripColorRaw(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }

    /**
     * Lấy GradeDefinitions
     */
    public GradeDefinitions getGrades() {
        return grades;
    }

    /**
     * Reload tất cả dữ liệu
     */
    public void reload() {
        loadAll();
        plugin.getLogger().info("[ItemLoader] Đã reload " + allItems.size() + " items");
    }
}