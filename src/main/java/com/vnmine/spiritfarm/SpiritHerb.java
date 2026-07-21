package com.vnmine.spiritfarm;

import com.vnmine.VNMinePlugin;
import com.vnmine.item.ItemBuilder;
import com.vnmine.item.ItemDataLoader;
import com.vnmine.item.ItemDefinition;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * SpiritHerb - Định nghĩa linh thảo
 * 
 * Giữ enum HerbQuality, HerbRarity cho logic game (trồng trọt, tuổi)
 * Dữ liệu item đọc từ ItemDataLoader (herbs.yml)
 * 
 * Backward compatible: getHerb(id) vẫn hoạt động như cũ
 */
public class SpiritHerb {

    private static VNMinePlugin pluginInstance = null;

    /**
     * Set plugin instance (gọi từ VNMinePlugin.onEnable)
     */
    public static void setPlugin(VNMinePlugin plugin) {
        pluginInstance = plugin;
    }

    /**
     * HerbQuality - Chất lượng (tuổi) của linh thảo
     */
    public enum HerbQuality {
        MAM_NON(0, "&fMầm Non", 1.0),
        TRUONG_THANH(1, "&aTrưởng Thành", 1.2),
        MOT_NAM(2, "&e1 Năm", 1.5),
        MUOI_NAM(3, "&6&l10 Năm", 2.0),
        TRAM_NAM(4, "&c&l100 Năm", 3.0),
        NGAN_NAM(5, "&5&l1000 Năm", 4.5),
        VAN_NAM(6, "&4&l1 Vạn Năm", 7.0);

        private final int level;
        private final String display;
        private final double multiplier;

        HerbQuality(int level, String display, double multiplier) {
            this.level = level;
            this.display = display;
            this.multiplier = multiplier;
        }

        public int getLevel() { return level; }
        public String getDisplay() { return display; }
        public double getMultiplier() { return multiplier; }

        public static HerbQuality fromAgeCode(int ageCode) {
            for (HerbQuality q : values()) {
                if (q.ordinal() == ageCode) return q;
            }
            return TRUONG_THANH;
        }

        public static HerbQuality fromLevel(int level) {
            for (HerbQuality q : values()) {
                if (q.level == level) return q;
            }
            return MAM_NON;
        }

        public static HerbQuality maxGrowable() {
            return NGAN_NAM;
        }
    }

    /**
     * HerbRarity - Phẩm cấp linh thảo
     */
    public enum HerbRarity {
        HA("&fHạ Phẩm", 1.0),
        TRUNG("&eTrung Phẩm", 1.5),
        THUONG("&aThượng Phẩm", 2.5),
        TIEN("&dTiên Phẩm", 4.0);

        private final String display;
        private final double multiplier;

        HerbRarity(String display, double multiplier) {
            this.display = display;
            this.multiplier = multiplier;
        }

        public String getDisplay() { return display; }
        public double getMultiplier() { return multiplier; }
    }

    // Cache cũ (backward compatibility)
    private static final Map<String, SpiritHerb> ALL_HERBS = new HashMap<>();

    // Static data: đảm bảo ALL_HERBS có dữ liệu ngay cả khi ItemDataLoader chưa load
    // Dữ liệu này sẽ được ghi đè bởi YML khi plugin khởi tạo đầy đủ
    static {
        register("LINH_THAO", "Linh Thảo", Material.SHORT_GRASS, Material.BEETROOT_SEEDS, HerbRarity.HA, 5);
        register("NGUYET_QUANG_THAO", "Nguyệt Quang Thảo", Material.FERN, Material.BEETROOT_SEEDS, HerbRarity.HA, 5);
        register("BINH_LINH_THAO", "Bình Linh Thảo", Material.AZURE_BLUET, Material.BEETROOT_SEEDS, HerbRarity.HA, 5);
        register("LAM_LINH_THAO", "Lam Linh Thảo", Material.CORNFLOWER, Material.BEETROOT_SEEDS, HerbRarity.HA, 5);
        register("LOI_LINH_THAO", "Lôi Linh Thảo", Material.DANDELION, Material.BEETROOT_SEEDS, HerbRarity.HA, 5);
        register("HUYEN_BINH_THAO", "Huyền Băng Thảo", Material.BLUE_ORCHID, Material.BEETROOT_SEEDS, HerbRarity.TRUNG, 5);
        register("HUYET_LINH_THAO", "Huyết Linh Thảo", Material.POPPY, Material.BEETROOT_SEEDS, HerbRarity.TRUNG, 5);
        register("HOA_LINH_THAO", "Hoa Linh Thảo", Material.ALLIUM, Material.BEETROOT_SEEDS, HerbRarity.TRUNG, 5);
        register("HAC_LINH_THAO", "Hạc Linh Thảo", Material.OXEYE_DAISY, Material.BEETROOT_SEEDS, HerbRarity.TRUNG, 5);
        register("KIM_LINH_THAO", "Kim Linh Thảo", Material.SUNFLOWER, Material.BEETROOT_SEEDS, HerbRarity.TRUNG, 5);
        register("LONG_HUYET_THAO", "Long Huyết Thảo", Material.RED_TULIP, Material.MELON_SEEDS, HerbRarity.THUONG, 5);
        register("THIEN_LINH_THAO", "Thiên Linh Thảo", Material.LILAC, Material.MELON_SEEDS, HerbRarity.THUONG, 5);
        register("PHUNG_LINH_THAO", "Phụng Linh Thảo", Material.PEONY, Material.MELON_SEEDS, HerbRarity.THUONG, 5);
        register("VAN_NIEN_LINH_CHI", "Vạn Niên Linh Chi", Material.ROSE_BUSH, Material.MELON_SEEDS, HerbRarity.THUONG, 6);
        register("LUYEN_THAN_THAO", "Luyện Thần Thảo", Material.WITHER_ROSE, Material.MELON_SEEDS, HerbRarity.THUONG, 5);
        register("TIEN_THAO", "Tiên Thảo", Material.TORCHFLOWER, Material.PUMPKIN_SEEDS, HerbRarity.TIEN, 5);
        register("LONG_LINH_THAO", "Long Linh Thảo", Material.PINK_TULIP, Material.PUMPKIN_SEEDS, HerbRarity.TIEN, 5);
        register("THANH_LONG_THAO", "Thanh Long Thảo", Material.PITCHER_PLANT, Material.PUMPKIN_SEEDS, HerbRarity.TIEN, 5);
        register("HONG_MONG_THAO", "Hồng Mộng Thảo", Material.ORANGE_TULIP, Material.PUMPKIN_SEEDS, HerbRarity.TIEN, 5);
        register("THIEN_HA_THAO", "Thiên Hà Thảo", Material.WHITE_TULIP, Material.PUMPKIN_SEEDS, HerbRarity.TIEN, 5);
    }

    private static void register(String id, String name, Material material, Material seedMaterial, HerbRarity rarity, int totalStages) {
        ALL_HERBS.put(id, new SpiritHerb(id, name, material, seedMaterial, rarity, totalStages));
    }

    private final String id;
    private final String name;
    private final Material material;
    private final Material seedMaterial;
    private final HerbRarity rarity;
    private final int totalStages;

    // Constructor private - chỉ tạo từ static register hoặc từ ItemDataLoader
    private SpiritHerb(String id, String name, Material material, Material seedMaterial, HerbRarity rarity, int totalStages) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.seedMaterial = seedMaterial;
        this.rarity = rarity;
        this.totalStages = totalStages;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Material getMaterial() { return material; }
    public Material getSeedMaterial() { return seedMaterial; }
    public HerbRarity getRarity() { return rarity; }
    public int getTotalStages() { return totalStages; }

    /**
     * Lấy tên hiển thị theo chất lượng (tuổi)
     */
    public String getQualityName(HerbQuality quality) {
        return quality.getDisplay() + " " + rarity.getDisplay() + " &f" + name;
    }

    /**
     * Tạo item linh thảo với chất lượng (tuổi) cụ thể
     * Dùng ItemBuilder.buildFromDefinition() nếu có ItemDataLoader
     */
    public ItemStack createHerbItem(HerbQuality quality, int amount) {
        // Nếu có ItemDataLoader, dùng dữ liệu từ YML
        if (pluginInstance != null) {
            ItemDataLoader loader = pluginInstance.getItemDataLoader();
            if (loader != null) {
                ItemDefinition def = loader.getItem(id);
                if (def != null) {
                    ItemStack item = ItemBuilder.buildFromDefinition(def);
                    item.setAmount(Math.min(amount, 64));
                    // Thêm persistent data cho herb age
                    ItemBuilder builder = new ItemBuilder(item);
                    builder.setPersistentData("vnmine_herb", id);
                    builder.setPersistentData("vnmine_herb_age", String.valueOf(quality.getLevel()));
                    return builder.build();
                }
            }
        }

        // Fallback: tạo item kiểu cũ
        String displayName = getQualityName(quality);
        String ageDesc;
        switch (quality) {
            case MAM_NON: ageDesc = "&7Mầm non, chưa thể dùng"; break;
            case TRUONG_THANH: ageDesc = "&7Linh thảo trưởng thành, chất lượng thấp"; break;
            case MOT_NAM: ageDesc = "&7Linh thảo 1 năm tuổi, chất lượng trung bình"; break;
            case MUOI_NAM: ageDesc = "&7Linh thảo 10 năm tuổi, chất lượng khá"; break;
            case TRAM_NAM: ageDesc = "&7Linh thảo 100 năm tuổi, chất lượng cao"; break;
            case NGAN_NAM: ageDesc = "&7Linh thảo 1000 năm tuổi, chất lượng siêu phàm"; break;
            case VAN_NAM: ageDesc = "&7Linh thảo 1 vạn năm tuổi, chất lượng tuyệt đỉnh"; break;
            default: ageDesc = "&7Linh thảo"; break;
        }

        return new ItemBuilder(material)
                .setName(displayName)
                .setLore("",
                        rarity.getDisplay() + " &f" + name,
                        quality.getDisplay(),
                        ageDesc,
                        "",
                        "&7Dùng để luyện đan hoặc bán cho NPC")
                .setGlow(true)
                .setAmount(Math.min(amount, 64))
                .setPersistentData("vnmine_herb", id)
                .setPersistentData("vnmine_herb_age", String.valueOf(quality.getLevel()))
                .build();
    }

    /**
     * Tạo item hạt giống
     */
    public ItemStack createSeedItem(int amount) {
        return new ItemBuilder(seedMaterial)
                .setName("&a&lHạt Giống " + name)
                .setLore("",
                        rarity.getDisplay() + " &f" + name,
                        "&7Gieo lên linh điền để trồng!",
                        "",
                        "&7Thời gian: " + totalStages + " giai đoạn",
                        "&7Phẩm cấp: " + rarity.getDisplay())
                .setGlow(true)
                .setAmount(Math.min(amount, 64))
                .setPersistentData("vnmine_seed", id)
                .build();
    }

    /**
     * Tính giá bán linh thảo (dùng cho NPC mua)
     */
    public int calculateSellPrice(HerbQuality quality) {
        double basePrice = 5;
        double rarityMultiplier = rarity.getMultiplier();
        double qualityMultiplier = quality.getMultiplier();
        return (int) Math.max(1, basePrice * rarityMultiplier * qualityMultiplier);
    }

    // ==================== STATIC HELPERS ====================

    /**
     * Lấy SpiritHerb theo ID
     * Kiểm tra cache cũ trước, sau đó fallback sang ItemDataLoader
     */
    public static SpiritHerb getHerb(String id) {
        if (id == null) return null;
        // Check old cache first
        SpiritHerb herb = ALL_HERBS.get(id.toUpperCase());
        if (herb != null) return herb;

        // Fallback to ItemDataLoader
        if (pluginInstance != null) {
            ItemDataLoader loader = pluginInstance.getItemDataLoader();
            if (loader != null) {
                ItemDefinition def = loader.getItem(id);
                if (def != null && def.isHerb()) {
                    // Create SpiritHerb from ItemDefinition
                    HerbRarity rarity = parseRarity(def.getRank());
                    Material seedMat = getSeedMaterial(rarity);
                    SpiritHerb newHerb = new SpiritHerb(def.getId(), 
                        stripColorRaw(def.getName()), 
                        def.getMaterial(), 
                        seedMat, 
                        rarity, 
                        5);
                    ALL_HERBS.put(def.getId(), newHerb);
                    return newHerb;
                }
            }
        }
        return null;
    }

    public static Map<String, SpiritHerb> getAllHerbs() {
        // Merge old cache with ItemDataLoader herbs
        if (pluginInstance != null) {
            ItemDataLoader loader = pluginInstance.getItemDataLoader();
            if (loader != null) {
                for (ItemDefinition def : loader.getItemsByCategory("herb")) {
                    if (!ALL_HERBS.containsKey(def.getId())) {
                        HerbRarity rarity = parseRarity(def.getRank());
                        Material seedMat = getSeedMaterial(rarity);
                        ALL_HERBS.put(def.getId(), new SpiritHerb(def.getId(),
                            stripColorRaw(def.getName()),
                            def.getMaterial(),
                            seedMat,
                            rarity,
                            5));
                    }
                }
            }
        }
        return ALL_HERBS;
    }

    public static String[] getHerbIds() {
        return getAllHerbs().keySet().toArray(new String[0]);
    }

    private static HerbRarity parseRarity(String rank) {
        if (rank == null) return HerbRarity.HA;
        if (rank.contains("Tiên")) return HerbRarity.TIEN;
        if (rank.contains("Thượng")) return HerbRarity.THUONG;
        if (rank.contains("Trung")) return HerbRarity.TRUNG;
        return HerbRarity.HA;
    }

    private static Material getSeedMaterial(HerbRarity rarity) {
        switch (rarity) {
            case TIEN: return Material.PUMPKIN_SEEDS;
            case THUONG: return Material.MELON_SEEDS;
            case TRUNG: return Material.BEETROOT_SEEDS;
            default: return Material.WHEAT_SEEDS;
        }
    }

    private static String stripColorRaw(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }

    /**
     * Lấy chất lượng từ persistent data của item
     */
    public static HerbQuality getQualityFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return HerbQuality.MAM_NON;
        String ageStr = ItemBuilder.getPersistentData(item, "vnmine_herb_age");
        if (ageStr != null) {
            try {
                int level = Integer.parseInt(ageStr);
                return HerbQuality.fromLevel(level);
            } catch (NumberFormatException ignored) {}
        }
        return HerbQuality.MAM_NON;
    }

    /**
     * Lấy herb ID từ persistent data của item
     */
    public static String getHerbIdFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return ItemBuilder.getPersistentData(item, "vnmine_herb");
    }

    /**
     * Kiểm tra item có phải linh thảo không
     */
    public static boolean isHerbItem(ItemStack item) {
        return getHerbIdFromItem(item) != null;
    }
}