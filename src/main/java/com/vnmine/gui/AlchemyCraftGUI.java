package com.vnmine.gui;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PillConfig;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.skill.PlayerSkillData;
import com.vnmine.spiritfarm.SpiritHerb;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AlchemyCraftGUI implements Listener {

    private final VNMinePlugin plugin;
    private final MainMenuGUI mainMenu;

    // === SLOT LAYOUT ===
    private static final int[] INPUT_SLOTS = {19, 20, 21, 28, 29, 30};
    private static final int[] RESULT_SLOTS = {23, 24, 25, 32, 33, 34};
    private static final int SLOT_CRAFT = 22;
    private static final int SLOT_STATUS = 40;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_GUIDE = 46;

    // Phẩm cấp hiển thị
    private static final String[] GRADE_DISPLAY = {
        "&7&oHoàng cấp &fHạ phẩm", "&7&oHoàng cấp &eTrung phẩm", "&7&oHoàng cấp &aThượng phẩm",
        "&b&oHuyền cấp &fHạ phẩm", "&b&oHuyền cấp &eTrung phẩm", "&b&oHuyền cấp &aThượng phẩm",
        "&5&oĐịa cấp &fHạ phẩm", "&5&oĐịa cấp &eTrung phẩm", "&5&oĐịa cấp &aThượng phẩm",
        "&6&oThiên cấp &fHạ phẩm", "&6&oThiên cấp &eTrung phẩm", "&6&oThiên cấp &aThượng phẩm"
    };

    private static final Color[] GRADE_COLORS = {
        Color.WHITE, Color.YELLOW, Color.LIME,
        Color.AQUA, Color.ORANGE, Color.GREEN,
        Color.PURPLE, Color.fromRGB(0xFF00FF), Color.RED,
        Color.fromRGB(0xFFD700), Color.fromRGB(0x00FFFF), Color.fromRGB(0x8B00FF)
    };

    private static final double[] GRADE_MULTIPLIERS = {
        1.0, 1.3, 1.6, 2.0, 2.5, 3.0, 3.5, 4.0, 5.0, 6.0, 7.5, 10.0
    };

    // Số lượng thành phẩm theo phẩm cấp
    private static final int[][] GRADE_YIELD_RANGE = {
        {1, 1}, {1, 1}, {1, 2}, {1, 2}, {1, 3}, {2, 3},
        {2, 4}, {2, 4}, {3, 5}, {3, 5}, {4, 5}, {5, 5}
    };

    // Material đa dạng cho từng loại đan dược
    private static final Map<String, Material> PILL_MATERIALS = new HashMap<>();
    static {
        PILL_MATERIALS.put("HOI_LINH_DAN", Material.POTION);
        PILL_MATERIALS.put("DAI_HOI_LINH_DAN", Material.LINGERING_POTION);
        PILL_MATERIALS.put("CUONG_THE_DAN", Material.SPLASH_POTION);
        PILL_MATERIALS.put("THANH_TAM_DAN", Material.HONEY_BOTTLE);
        PILL_MATERIALS.put("TOC_THANH_DAN", Material.POTION);
        PILL_MATERIALS.put("TU_LUYEN_DAN", Material.EXPERIENCE_BOTTLE);
        PILL_MATERIALS.put("PHI_THANG_DAN", Material.DRAGON_BREATH);
        PILL_MATERIALS.put("BACH_DOC_DAN", Material.POTION);
        PILL_MATERIALS.put("THIEN_HOI_DAN", Material.LINGERING_POTION);
        PILL_MATERIALS.put("PHE_MA_DAN", Material.SPLASH_POTION);
        PILL_MATERIALS.put("TRUONG_THO_DAN", Material.HONEY_BOTTLE);
        PILL_MATERIALS.put("KIM_CUONG_DAN", Material.SPLASH_POTION);
        PILL_MATERIALS.put("LINH_NHIEN_DAN", Material.POTION);
        PILL_MATERIALS.put("TIEM_HANH_DAN", Material.POTION);
        PILL_MATERIALS.put("PHAP_TUONG_DAN", Material.LINGERING_POTION);
        PILL_MATERIALS.put("THAN_LONG_DAN", Material.DRAGON_BREATH);
        PILL_MATERIALS.put("CUONG_LUC_DAN", Material.SPLASH_POTION);
        PILL_MATERIALS.put("HAN_BANG_DAN", Material.LINGERING_POTION);
        PILL_MATERIALS.put("LINH_PHONG_DAN", Material.POTION);
        PILL_MATERIALS.put("HOA_LONG_DAN", Material.DRAGON_BREATH);
        PILL_MATERIALS.put("THIEN_LINH_DAN", Material.EXPERIENCE_BOTTLE);
        PILL_MATERIALS.put("DAC_COC_DAN", Material.HONEY_BOTTLE);
        PILL_MATERIALS.put("VO_THUONG_DAN", Material.DRAGON_BREATH);
    }

    private static final int DEFAULT_CHARGES = 10;

    // ==================== INGREDIENT DEFINITION ====================
    private static class IngredientDef {
        final String herbId;      // null nếu không phải linh thảo
        final Material material;  // Material để kiểm tra fallback
        final int count;
        final boolean isHerb;

        IngredientDef(String herbId, Material material, int count, boolean isHerb) {
            this.herbId = herbId;
            this.material = material;
            this.count = count;
            this.isHerb = isHerb;
        }
    }

    // Helper để tạo IngredientDef cho linh thảo
    private static IngredientDef herb(String herbId, int count) {
        SpiritHerb h = SpiritHerb.getHerb(herbId);
        Material mat = (h != null) ? h.getMaterial() : Material.SHORT_GRASS;
        return new IngredientDef(herbId, mat, count, true);
    }

    // Helper để tạo IngredientDef cho nguyên liệu thường
    private static IngredientDef mat(String materialName, int count) {
        Material mat = Material.getMaterial(materialName);
        return new IngredientDef(null, (mat != null) ? mat : Material.STONE, count, false);
    }

    // ==================== RECIPES ====================
    private static final List<AlchemyRecipe> RECIPES = new ArrayList<>();
    static {
        RECIPES.add(new AlchemyRecipe("HOI_LINH_DAN", "&aHồi Linh Đan", Material.POTION,
                "&7Hồi phục &b{recover} &7linh lực",
                Arrays.asList(herb("LINH_THAO", 3), mat("POTION", 1)),
                3, 10, 80.0));

        RECIPES.add(new AlchemyRecipe("DAI_HOI_LINH_DAN", "&bĐại Hồi Linh Đan", Material.LINGERING_POTION,
                "&7Hồi &b{recover} &7linh lực + &a{regen} &7trong &a{duration}s",
                Arrays.asList(herb("HUYET_LINH_THAO", 2), herb("LINH_THAO", 5)),
                10, 30, 60.0));

        RECIPES.add(new AlchemyRecipe("THANH_TAM_DAN", "&aThanh Tâm Đan", Material.HONEY_BOTTLE,
                "&7Giải trừ mọi trạng thái xấu",
                Arrays.asList(herb("BINH_LINH_THAO", 3), mat("POTION", 1)),
                5, 15, 85.0));

        RECIPES.add(new AlchemyRecipe("TOC_THANH_DAN", "&bTốc Thánh Đan", Material.POTION,
                "&7Tăng &b{regen}% tốc độ &7trong &a{duration}s",
                Arrays.asList(herb("LOI_LINH_THAO", 3), mat("SUGAR", 2), mat("FEATHER", 1)),
                8, 15, 70.0));

        RECIPES.add(new AlchemyRecipe("CUONG_THE_DAN", "&cCương Thể Đan", Material.SPLASH_POTION,
                "&7Tăng &c{dmg}% sát thương &7trong &c{duration}s",
                Arrays.asList(herb("HUYET_LINH_THAO", 3), herb("LINH_THAO", 5), mat("BLAZE_POWDER", 1)),
                15, 20, 55.0));

        RECIPES.add(new AlchemyRecipe("BACH_DOC_DAN", "&9Bách Độc Đan", Material.POTION,
                "&7Miễn nhiễm độc &7trong &b{duration}s",
                Arrays.asList(herb("BINH_LINH_THAO", 3), mat("REDSTONE", 2), mat("BLAZE_POWDER", 1)),
                25, 25, 50.0));

        RECIPES.add(new AlchemyRecipe("TU_LUYEN_DAN", "&5Tu Luyện Đan", Material.EXPERIENCE_BOTTLE,
                "&7Tăng &5+{exp} EXP &7tu luyện",
                Arrays.asList(herb("LINH_THAO", 10), herb("HUYET_LINH_THAO", 5), herb("LONG_HUYET_THAO", 2), mat("GOLD_INGOT", 1)),
                20, 45, 40.0));

        RECIPES.add(new AlchemyRecipe("THIEN_HOI_DAN", "&6Thiên Hồi Đan", Material.LINGERING_POTION,
                "&7Hồi &a{heal}% HP &7+ &b{recover}% &7linh lực",
                Arrays.asList(herb("THIEN_LINH_THAO", 5), herb("HOA_LINH_THAO", 3), mat("PRISMARINE_SHARD", 1)),
                35, 40, 45.0));

        RECIPES.add(new AlchemyRecipe("PHE_MA_DAN", "&8Phê Ma Đan", Material.SPLASH_POTION,
                "&7Tăng &c{dmg}% sát thương &7vs quái &7trong &c{duration}s",
                Arrays.asList(herb("LOI_LINH_THAO", 3), mat("IRON_INGOT", 2), mat("END_STONE", 1)),
                40, 30, 40.0));

        RECIPES.add(new AlchemyRecipe("TRUONG_THO_DAN", "&6Trường Thọ Đan", Material.HONEY_BOTTLE,
                "&7Tự động hồi sinh 1 lần",
                Arrays.asList(herb("VAN_NIEN_LINH_CHI", 5), mat("IRON_NUGGET", 3), mat("EMERALD", 2)),
                45, 60, 25.0));

        RECIPES.add(new AlchemyRecipe("PHI_THANG_DAN", "&6&l◆ Phi Thăng Đan ◆", Material.DRAGON_BREATH,
                "&7+{exp} EXP &7(1 lần/đại cảnh giới)",
                Arrays.asList(herb("TIEN_THAO", 10), mat("DRAGON_BREATH", 1), mat("NETHERITE_INGOT", 2)),
                50, 120, 15.0));

        // 12 công thức mới
        RECIPES.add(new AlchemyRecipe("KIM_CUONG_DAN", "&bKim Cương Đan", Material.SPLASH_POTION,
                "&7Giảm &b{dmg}% &7sát thương nhận vào &7trong &b{duration}s",
                Arrays.asList(herb("KIM_LINH_THAO", 5), mat("DIAMOND", 2), mat("OBSIDIAN", 1)),
                25, 35, 50.0));

        RECIPES.add(new AlchemyRecipe("LINH_NHIEN_DAN", "&aLinh Nhiên Đan", Material.POTION,
                "&7Tăng &a{regen}% &7tốc độ đánh + &c{dmg}% &7crit &7trong &a{duration}s",
                Arrays.asList(herb("HAC_LINH_THAO", 5), mat("SUGAR", 3), mat("FEATHER", 2)),
                28, 30, 55.0));

        RECIPES.add(new AlchemyRecipe("TIEM_HANH_DAN", "&8Tiềm Hành Đan", Material.POTION,
                "&7Tàng hình &a{duration}s &7+ tăng &c{dmg}% &7sát thương đòn đầu",
                Arrays.asList(herb("NGUYET_QUANG_THAO", 5), mat("ENDER_PEARL", 2), mat("FEATHER", 1)),
                30, 40, 45.0));

        RECIPES.add(new AlchemyRecipe("PHAP_TUONG_DAN", "&5Pháp Tướng Đan", Material.LINGERING_POTION,
                "&7Tăng &b{recover}% &7max mana &7trong &b{duration}s",
                Arrays.asList(herb("LONG_HUYET_THAO", 5), mat("GOLD_INGOT", 2), mat("DIAMOND", 1)),
                35, 50, 40.0));

        RECIPES.add(new AlchemyRecipe("THAN_LONG_DAN", "&6&l◆ Thần Long Đan ◆", Material.DRAGON_BREATH,
                "&7Hồi &a{heal}% HP &7+ &b{recover}% &7linh lực + miễn dịch &b{duration}s",
                Arrays.asList(herb("TIEN_THAO", 8), mat("DRAGON_BREATH", 2), mat("NETHERITE_INGOT", 1), mat("GOLDEN_APPLE", 1)),
                40, 60, 30.0));

        RECIPES.add(new AlchemyRecipe("CUONG_LUC_DAN", "&cCường Lực Đan", Material.SPLASH_POTION,
                "&7Tăng &c{dmg}% &7sát thương cận chiến &7trong &c{duration}s",
                Arrays.asList(herb("HUYET_LINH_THAO", 5), mat("BLAZE_POWDER", 3), mat("IRON_INGOT", 2)),
                32, 35, 50.0));

        RECIPES.add(new AlchemyRecipe("HAN_BANG_DAN", "&bHàn Băng Đan", Material.LINGERING_POTION,
                "&7Làm chậm kẻ địch, tăng giáp băng &7trong &a{duration}s",
                Arrays.asList(herb("HUYEN_BINH_THAO", 5), mat("BLUE_ICE", 2), mat("PRISMARINE_SHARD", 2)),
                33, 40, 45.0));

        RECIPES.add(new AlchemyRecipe("LINH_PHONG_DAN", "&aLinh Phong Đan", Material.POTION,
                "&7Tăng &a{regen}% &7tốc độ + nhảy cao &7trong &a{duration}s",
                Arrays.asList(herb("LAM_LINH_THAO", 3), mat("FEATHER", 3), mat("SUGAR", 2)),
                27, 25, 60.0));

        RECIPES.add(new AlchemyRecipe("HOA_LONG_DAN", "&6&l◆ Hóa Long Đan ◆", Material.DRAGON_BREATH,
                "&7Biến rồng &a{duration}s &7tăng &c{dmg}% &7sát thương + &b{recover}% &7phòng thủ",
                Arrays.asList(herb("TIEN_THAO", 8), mat("DRAGON_BREATH", 3), mat("NETHERITE_INGOT", 2), mat("GOLD_BLOCK", 1)),
                55, 90, 20.0));

        RECIPES.add(new AlchemyRecipe("THIEN_LINH_DAN", "&d&l◆ Thiên Linh Đan ◆", Material.EXPERIENCE_BOTTLE,
                "&7+{exp} EXP &7(1 lần/đại cảnh giới)",
                Arrays.asList(herb("THIEN_LINH_THAO", 8), mat("DIAMOND", 3), mat("NETHERITE_INGOT", 2), mat("DRAGON_BREATH", 1)),
                45, 80, 25.0));

        RECIPES.add(new AlchemyRecipe("DAC_COC_DAN", "&5Đặc Cốc Đan", Material.HONEY_BOTTLE,
                "&7Reset cooldown tất cả skill &7(1 lần/giờ)",
                Arrays.asList(herb("PHUNG_LINH_THAO", 5), mat("EMERALD", 3), mat("GOLD_INGOT", 2), mat("BLAZE_POWDER", 1)),
                38, 50, 35.0));

        RECIPES.add(new AlchemyRecipe("VO_THUONG_DAN", "&4&l◆ Vô Thượng Đan ◆", Material.DRAGON_BREATH,
                "&7Tăng &c{dmg}% &7sát thương + &a{regen}% &7tốc độ + &b{recover}% &7phòng thủ &7trong &c{duration}s &7(1 lần/ngày)",
                Arrays.asList(herb("LUYEN_THAN_THAO", 10), mat("NETHERITE_INGOT", 3), mat("DRAGON_BREATH", 2), mat("END_CRYSTAL", 1)),
                70, 120, 10.0));
    }

    private static final Map<UUID, AlchemySession> activeSessions = new HashMap<>();

    public AlchemyCraftGUI(VNMinePlugin plugin, MainMenuGUI mainMenu) {
        this.plugin = plugin;
        this.mainMenu = mainMenu;
    }

    public void open(Player player) {
        plugin.getLogger().info("[AlchemyDebug] open() called for " + player.getName());
        Inventory gui = Bukkit.createInventory(null, 54, ColorUtils.colorize("&8✦ Luyện Đan - Khống Hỏa Thuật ✦"));

        for (int slot : INPUT_SLOTS) gui.setItem(slot, null);

        gui.setItem(SLOT_CRAFT, new ItemBuilder(Material.FIRE_CHARGE).setGlow(true).setName("&c&l🔥 Luyện Đan")
                .setLore("", "&7Đặt nguyên liệu vào ô bên trái", "&7Bấm nút này để luyện đan",
                        "", "&cYêu cầu: Kỹ năng Khống Hỏa Thuật", "&7Level 21+ (Trúc Cơ)").build());

        for (int slot : RESULT_SLOTS) gui.setItem(slot, null);

        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.PAPER).setName("&e&lTrạng Thái")
                .setLore("", "&7Sẵn sàng luyện đan!").build());

        // Guide
        gui.setItem(SLOT_GUIDE, new ItemBuilder(Material.KNOWLEDGE_BOOK).setName("&6&lCông Thức Luyện Đan")
                .setLore("", "&a◆ Hồi Linh Đan ◆: &73 Linh Thảo + 1 Nước Tinh Khiết",
                        "&b◆ Đại Hồi Linh Đan ◆: &72 Huyết Linh Thảo + 5 Linh Thảo",
                        "&c◆ Cương Thể Đan ◆: &73 Huyết Linh Thảo + 5 Linh Thảo + 1 Bột Blaze",
                        "&a◆ Thanh Tâm Đan ◆: &73 Bình Linh Thảo + 1 Nước Tinh Khiết",
                        "&b◆ Tốc Thánh Đan ◆: &73 Lôi Linh Thảo + 2 Đường + 1 Lông Vũ",
                        "&5◆ Tu Luyện Đan ◆: &710 Linh Thảo + 5 Huyết Linh Thảo + 2 Long Huyết Thảo + 1 Vàng Thanh",
                        "&6◆ Phi Thăng Đan ◆: &710 Tiên Thảo + 1 Hơi Rồng + 2 Netherite Thanh",
                        "&9◆ Bách Độc Đan ◆: &73 Bình Linh Thảo + 2 Huyết Thạch + 1 Bột Blaze",
                        "&6◆ Thiên Hồi Đan ◆: &75 Thiên Linh Thảo + 3 Hoa Linh Thảo + 1 Long Lân",
                        "&8◆ Phê Ma Đan ◆: &73 Lôi Linh Thảo + 2 Huyền Kim + 1 Thiên Thạch",
                        "&6◆ Trường Thọ Đan ◆: &75 Vạn Niên Linh Chi + 3 Ngân Sa + 2 Ngọc Lục Bảo").build());

        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&r").build();
        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null && !isInputSlot(i) && !isResultSlot(i)) {
                gui.setItem(i, border);
            }
        }

        gui.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW).setName("&e&l← Quay Lai").build());

        player.openInventory(gui);
        activeSessions.put(player.getUniqueId(), new AlchemySession(player.getUniqueId()));
    }

    private boolean isInputSlot(int slot) {
        for (int s : INPUT_SLOTS) if (s == slot) return true;
        return false;
    }

    private boolean isResultSlot(int slot) {
        for (int s : RESULT_SLOTS) if (s == slot) return true;
        return false;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Đan")) return;
        for (Integer slot : event.getRawSlots()) {
            if (slot < 54 && !isInputSlot(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String rawTitle = event.getView().getTitle();
        String title = ColorUtils.stripColor(rawTitle);
        if (!title.contains("Luyện Đan")) return;

        int slot = event.getRawSlot();
        if (slot >= 54) return;
        if (slot < 0) { event.setCancelled(true); return; }

        if (isInputSlot(slot)) return;

        if (isResultSlot(slot)) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                event.setCancelled(true);
                return;
            }
            return;
        }

        event.setCancelled(true);

        AlchemySession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            activeSessions.put(player.getUniqueId(), new AlchemySession(player.getUniqueId()));
            session = activeSessions.get(player.getUniqueId());
        }

        if (session.isCrafting) {
            MessageUtils.send(player, "&cĐang trong quá trình luyện đan, vui lòng chờ!");
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (slot) {
            case SLOT_CRAFT:
                attemptCraft(player, session);
                break;
            case SLOT_BACK:
                returnItemsToPlayer(player);
                mainMenu.openMainMenu(player);
                break;
        }
    }

    private void returnItemsToPlayer(Player player) {
        Inventory gui = player.getOpenInventory().getTopInventory();
        for (int slot : RESULT_SLOTS) {
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

    // ==================== TÍNH TOÁN PHẨM CẤP ====================

    /**
     * Trích xuất số năm từ persistent data vnmine_herb_age
     */
    private int extractHerbAge(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        String ageStr = ItemBuilder.getPersistentData(item, "vnmine_herb_age");
        if (ageStr != null) {
            try {
                int level = Integer.parseInt(ageStr);
                // Map level: 0=Mầm Non, 3=10 Năm, 4=100 Năm, 5=1000 Năm, 6=1 Vạn Năm
                if (level >= 3) return level - 2; // 10 Năm=1, 100 Năm=2, 1000 Năm=3, 1 Vạn=4
                if (level >= 1) return 0; // Trưởng Thành, 1 Năm -> chất lượng thấp
                return 0;
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private int calculateIngredientQuality(Inventory gui) {
        int totalQuality = 0;
        int herbCount = 0;
        for (int slot : INPUT_SLOTS) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                int age = extractHerbAge(item);
                if (age > 0) {
                    totalQuality += age;
                    herbCount++;
                }
            }
        }
        if (herbCount == 0) return 0;
        return (totalQuality * 2) / Math.max(1, herbCount);
    }

    private int calculatePillGrade(PlayerCultivationData playerData, PlayerSkillData skillData, int ingredientQuality) {
        int qualityBonus = ingredientQuality / 2;
        int skillBonus = (skillData != null) ? skillData.getAlchemyGradeBonus() : 0;
        int levelBonus = 0;
        if (playerData != null) {
            int level = playerData.getLevel();
            if (level >= 71) levelBonus = 3;
            else if (level >= 51) levelBonus = 2;
            else if (level >= 31) levelBonus = 1;
        }
        int luck = new Random().nextInt(3) - 1;

        int grade = qualityBonus + skillBonus + levelBonus + luck;
        return Math.max(0, Math.min(grade, 11));
    }

    private double calculateSuccessChance(AlchemyRecipe recipe, PlayerCultivationData playerData, int extraItemCount) {
        double chance = recipe.successChance;
        if (playerData != null) {
            chance += playerData.getLevel() * 0.5;
        }
        chance -= extraItemCount * 10.0;
        chance += (new Random().nextDouble() * 10) - 5;
        return Math.max(5.0, Math.min(chance, 95.0));
    }

    private int getGradeYield(int gradeIndex) {
        if (gradeIndex < 0 || gradeIndex >= GRADE_YIELD_RANGE.length) return 1;
        int min = GRADE_YIELD_RANGE[gradeIndex][0];
        int max = GRADE_YIELD_RANGE[gradeIndex][1];
        return min + new Random().nextInt(max - min + 1);
    }

    // ==================== TẠO ITEM ĐAN DƯỢC ====================

    private ItemStack createPotionItem(AlchemyRecipe recipe, int gradeIndex, int amount) {
        Color potionColor = GRADE_COLORS[gradeIndex];
        double multiplier = GRADE_MULTIPLIERS[gradeIndex];
        Material pillMaterial = PILL_MATERIALS.getOrDefault(recipe.id, Material.POTION);

        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect(recipe.id);
        String effectLore = (effect != null) ? effect.getLore(multiplier) : "&7Đan dược quý giá";
        String flavorLore = plugin.getPillConfig().getRandomFlavor(recipe.id);
        String gradeDisplay = GRADE_DISPLAY[gradeIndex];

        ItemBuilder builder = new ItemBuilder(pillMaterial)
                .setName(recipe.displayName)
                .setAmount(Math.min(amount, 64))
                .setGlow(true)
                .setPersistentData("vnmine_pill_type", recipe.id)
                .setPersistentData("vnmine_pill_charges", String.valueOf(DEFAULT_CHARGES))
                .setPersistentData("vnmine_pill_grade", String.valueOf(gradeIndex))
                .setLore("",
                        gradeDisplay,
                        effectLore,
                        flavorLore,
                        "",
                        "&7Lượng dùng: &e" + DEFAULT_CHARGES + "/" + DEFAULT_CHARGES + " &7lần",
                        "&a✦ Click phải để sử dụng!");

        if (pillMaterial == Material.POTION || pillMaterial == Material.LINGERING_POTION || pillMaterial == Material.SPLASH_POTION) {
            builder.setPotionColor(potionColor);
        }

        builder.hideAll();
        if (pillMaterial == Material.POTION || pillMaterial == Material.LINGERING_POTION || pillMaterial == Material.SPLASH_POTION) {
            PotionMeta meta = (PotionMeta) builder.build().getItemMeta();
            if (meta != null) {
                meta.addCustomEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 0, 0, true, false, false), true);
                builder.build().setItemMeta(meta);
            }
        }

        return builder.build();
    }

    // ==================== KIỂM TRA NGUYÊN LIỆU ====================

    private static class IngredientCheckResult {
        final AlchemyRecipe recipe;
        final Map<String, Integer> recipeHerbs;     // herbId -> count
        final Map<String, Integer> recipeMaterials; // material name -> count
        final int extraItemCount;

        IngredientCheckResult(AlchemyRecipe recipe, Map<String, Integer> recipeHerbs,
                             Map<String, Integer> recipeMaterials, int extraItemCount) {
            this.recipe = recipe;
            this.recipeHerbs = recipeHerbs;
            this.recipeMaterials = recipeMaterials;
            this.extraItemCount = extraItemCount;
        }
    }

    /**
     * Kiểm tra nguyên liệu dựa trên herb ID (vnmine_herb) và Material
     */
    private IngredientCheckResult checkIngredients(Inventory gui) {
        // Thu thập input: herbId -> count và material name -> count
        Map<String, Integer> inputHerbs = new HashMap<>();    // herbId -> count
        Map<String, Integer> inputMaterials = new HashMap<>(); // material name -> count
        int totalSlots = 0;

        for (int slot : INPUT_SLOTS) {
            ItemStack item = gui.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            totalSlots++;

            String herbId = ItemBuilder.getPersistentData(item, "vnmine_herb");
            if (herbId != null) {
                // Đây là linh thảo chính hãng
                inputHerbs.put(herbId, inputHerbs.getOrDefault(herbId, 0) + item.getAmount());
            } else {
                // Nguyên liệu thường (Nước Tinh Khiết, Bột Blaze, v.v.)
                String matName = item.getType().name();
                inputMaterials.put(matName, inputMaterials.getOrDefault(matName, 0) + item.getAmount());
            }
        }

        if (inputHerbs.isEmpty() && inputMaterials.isEmpty()) return null;

        // Tìm công thức phù hợp
        for (AlchemyRecipe recipe : RECIPES) {
            // Tách ingredients thành herb và material requirements
            Map<String, Integer> requiredHerbs = new HashMap<>();
            Map<String, Integer> requiredMaterials = new HashMap<>();
            for (IngredientDef ing : recipe.ingredients) {
                if (ing.isHerb) {
                    requiredHerbs.put(ing.herbId, requiredHerbs.getOrDefault(ing.herbId, 0) + ing.count);
                } else {
                    requiredMaterials.put(ing.material.name(), requiredMaterials.getOrDefault(ing.material.name(), 0) + ing.count);
                }
            }

            // Kiểm tra herbs
            boolean herbsMatch = true;
            for (Map.Entry<String, Integer> req : requiredHerbs.entrySet()) {
                int available = inputHerbs.getOrDefault(req.getKey(), 0);
                if (available < req.getValue()) {
                    herbsMatch = false;
                    break;
                }
            }
            if (!herbsMatch) continue;

            // Kiểm tra materials
            boolean materialsMatch = true;
            for (Map.Entry<String, Integer> req : requiredMaterials.entrySet()) {
                int available = inputMaterials.getOrDefault(req.getKey(), 0);
                if (available < req.getValue()) {
                    materialsMatch = false;
                    break;
                }
            }
            if (!materialsMatch) continue;

            // Kiểm tra không có dư thừa
            boolean hasExcess = false;

            // Dư herb
            for (Map.Entry<String, Integer> entry : inputHerbs.entrySet()) {
                int required = requiredHerbs.getOrDefault(entry.getKey(), 0);
                if (entry.getValue() > required) {
                    hasExcess = true;
                    break;
                }
            }
            if (hasExcess) continue;

            // Dư material
            for (Map.Entry<String, Integer> entry : inputMaterials.entrySet()) {
                int required = requiredMaterials.getOrDefault(entry.getKey(), 0);
                if (entry.getValue() > required) {
                    hasExcess = true;
                    break;
                }
            }
            if (hasExcess) continue;

            // Tính item lạ (herb không có trong công thức + material không có trong công thức)
            int extraItems = 0;
            for (Map.Entry<String, Integer> entry : inputHerbs.entrySet()) {
                if (!requiredHerbs.containsKey(entry.getKey())) {
                    extraItems += entry.getValue();
                }
            }
            for (Map.Entry<String, Integer> entry : inputMaterials.entrySet()) {
                if (!requiredMaterials.containsKey(entry.getKey())) {
                    extraItems += entry.getValue();
                }
            }

            return new IngredientCheckResult(recipe, requiredHerbs, requiredMaterials, extraItems);
        }

        return null;
    }

    // ==================== QUÁ TRÌNH LUYỆN ĐAN ====================

    private void attemptCraft(Player player, AlchemySession session) {
        long startTime = System.currentTimeMillis();
        Inventory gui = player.getOpenInventory().getTopInventory();
        PlayerCultivationData playerData = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());

        if (playerData == null || !playerData.hasLearnedSkill("FIRE_CONTROL")) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.BARRIER)
                    .setName("&c&lChưa Học Khống Hỏa Thuật!")
                    .setLore("", "&7Bạn cần học kỹ năng &cKhống Hỏa Thuật",
                            "&7để có thể luyện đan.", "", "&eYêu cầu: Level 21+ (Trúc Cơ)").build());
            MessageUtils.send(player, "&c✦ Bạn chưa học Khống Hỏa Thuật!");
            return;
        }

        IngredientCheckResult checkResult = checkIngredients(gui);
        if (checkResult == null) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE)
                    .setName("&c&lSai Công Thức!")
                    .setLore("", "&7Nguyên liệu không khớp với bất kỳ công thức nào!",
                            "&7Chỉ sử dụng linh thảo chính hãng từ linh điền,",
                            "&7không dùng cây cỏ dại ngoài tự nhiên!").build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        AlchemyRecipe matchedRecipe = checkResult.recipe;
        int extraItemCount = checkResult.extraItemCount;

        if (playerData.getLevel() < matchedRecipe.requiredLevel) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lKhông Đủ Tu Vi")
                    .setLore("", "&7Yêu cầu: &cLevel " + matchedRecipe.requiredLevel,
                            "&7Hiện tại: &eLevel " + playerData.getLevel()).build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        int ingredientQuality = calculateIngredientQuality(gui);
        int pillGradeIndex = calculatePillGrade(playerData, skillData, ingredientQuality);
        String pillGradeDisplay = GRADE_DISPLAY[pillGradeIndex];
        long adjustedTime = calculateCookingTime(matchedRecipe.cookingTime, skillData);
        double successChance = calculateSuccessChance(matchedRecipe, playerData, extraItemCount);

        String profName = (skillData != null) ? ColorUtils.colorize(skillData.getFireControlProficiencyName()) : "&7N/A";
        int profBonus = (skillData != null) ? skillData.getAlchemyGradeBonus() : 0;
        double timeReduction = (skillData != null) ? skillData.getAlchemyTimeReduction() : 0.0;

        List<String> statusLore = new ArrayList<>();
        statusLore.add("");
        statusLore.add(ColorUtils.colorize("&7Đan dược: " + matchedRecipe.displayName));
        statusLore.add(ColorUtils.colorize("&7Phẩm cấp dự kiến: " + pillGradeDisplay));
        statusLore.add(ColorUtils.colorize("&7Khống Hỏa Thuật: " + profName + " &7(+" + profBonus + " phẩm)"));
        statusLore.add(ColorUtils.colorize("&7Giảm thời gian: &c" + (int)(timeReduction * 100) + "%"));
        statusLore.add(ColorUtils.colorize("&7Thời gian: &e" + adjustedTime + " giây"));
        statusLore.add(ColorUtils.colorize("&7Tỷ lệ: &a" + String.format("%.1f", successChance) + "%"));
        if (extraItemCount > 0) {
            statusLore.add(ColorUtils.colorize("&c⚠ Có " + extraItemCount + " nguyên liệu lạ! Giảm tỉ lệ."));
        }

        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.FURNACE).setName("&6&lĐang Luyện Đan...")
                .setLore(statusLore.toArray(new String[0])).build());
        player.updateInventory();

        // Tiêu hao nguyên liệu
        consumeIngredients(gui, matchedRecipe, checkResult);

        session.isCrafting = true;
        MessageUtils.playSound(player, Sound.BLOCK_FIRE_AMBIENT);

        final UUID playerUUID = player.getUniqueId();
        final AlchemyRecipe finalRecipe = matchedRecipe;
        final PlayerCultivationData finalData = playerData;
        final PlayerSkillData finalSkillData = skillData;
        final Inventory finalGui = gui;
        final int finalGradeIndex = pillGradeIndex;
        final double finalChance = successChance;
        final int finalExtraItems = extraItemCount;

        BossBar bossBar = Bukkit.createBossBar(
                ColorUtils.colorize("&a🔥 Đang luyện " + stripColor(matchedRecipe.displayName) + "..."),
                BarColor.GREEN, BarStyle.SEGMENTED_10);
        bossBar.addPlayer(player);
        session.activeBossBar = bossBar;

        final long totalTicks = adjustedTime * 20L;
        final long intervalTicks = 10L;
        final long totalSteps = totalTicks / intervalTicks;

        new BukkitRunnable() {
            long currentStep = 0;

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(playerUUID);
                if (p == null || !p.isOnline()) {
                    bossBar.removeAll();
                    session.isCrafting = false;
                    cancel();
                    return;
                }

                currentStep++;
                double progress = Math.min(1.0, (double) currentStep / totalSteps);
                bossBar.setProgress(progress);
                int percent = (int) (progress * 100);
                bossBar.setTitle(String.format("§a🔥 [%d%%] Đang luyện %s...", percent, stripColor(finalRecipe.displayName)));

                if (currentStep >= totalSteps) {
                    cancel();
                    bossBar.removeAll();
                    session.isCrafting = false;
                    session.activeBossBar = null;
                    finishCraft(p, finalGui, finalRecipe, finalData, finalSkillData,
                            finalGradeIndex, finalChance, finalExtraItems);
                }
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    /**
     * Tiêu hao nguyên liệu từ input slots
     */
    private void consumeIngredients(Inventory gui, AlchemyRecipe recipe, IngredientCheckResult checkResult) {
        // Gom tất cả yêu cầu vào một map: key = "herb:herbId" hoặc "mat:MaterialName"
        Map<String, Integer> remainingNeeded = new HashMap<>();
        for (Map.Entry<String, Integer> entry : checkResult.recipeHerbs.entrySet()) {
            remainingNeeded.put("herb:" + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : checkResult.recipeMaterials.entrySet()) {
            remainingNeeded.put("mat:" + entry.getKey(), entry.getValue());
        }

        for (int slot : INPUT_SLOTS) {
            ItemStack item = gui.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;

            String key;
            String herbId = ItemBuilder.getPersistentData(item, "vnmine_herb");
            if (herbId != null) {
                key = "herb:" + herbId;
            } else {
                key = "mat:" + item.getType().name();
            }

            Integer needed = remainingNeeded.get(key);
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
                remainingNeeded.put(key, needed - consume);
            } else {
                // Nguyên liệu lạ
                gui.setItem(slot, null);
            }
        }
    }

    private long calculateCookingTime(int baseTime, PlayerSkillData skillData) {
        double timeReduction = (skillData != null) ? skillData.getAlchemyTimeReduction() : 0.0;
        return Math.max(10, (long)(baseTime * (1.0 - timeReduction)));
    }

    private void finishCraft(Player p, Inventory inv, AlchemyRecipe recipe, PlayerCultivationData data,
                             PlayerSkillData skillData, int gradeIndex, double chance, int extraItems) {
        if (p == null || !p.isOnline()) return;

        Random random = new Random();
        boolean success = random.nextDouble() * 100 < chance;

        if (skillData != null) {
            if (success) {
                skillData.incrementSkillUsage("FIRE_CONTROL");
                skillData.incrementSkillUsage("FIRE_CONTROL");
            } else {
                skillData.incrementSkillUsage("FIRE_CONTROL");
            }
            PlayerSkillData.ProficiencyLevel newProf = skillData.getProficiencyLevel("FIRE_CONTROL");
            int usage = skillData.getSkillUsageCount("FIRE_CONTROL");
            MessageUtils.send(p, "&c✦ Khống Hỏa Thuật: &e" + usage + " lần → " + ColorUtils.colorize(newProf.getDisplayName()));
        }

        if (success) {
            int yield = getGradeYield(gradeIndex);
            ItemStack result = createPotionItem(recipe, gradeIndex, 1);

            int placed = 0;
            for (int slot : RESULT_SLOTS) {
                if (placed >= yield) break;
                ItemStack clone = result.clone();
                inv.setItem(slot, clone);
                placed++;
            }

            inv.setItem(SLOT_STATUS, new ItemBuilder(Material.EMERALD).setName("&a&l✦ Luyện Đan Thành Công ✦")
                    .setLore("", "&7Đan dược: " + recipe.displayName,
                            "&7Phẩm cấp: " + GRADE_DISPLAY[gradeIndex],
                            "&7Số lượng: &e" + yield + " lọ",
                            "&7Tỷ lệ: &a" + String.format("%.1f", chance) + "%").build());
            p.updateInventory();

            if (data != null) {
                double expReward = recipe.cookingTime * 2;
                plugin.getCultivationManager().addExperience(p, expReward);
                data.incrementPillsCrafted();
                MessageUtils.send(p, "&a✦ Luyện đan thành công! Nhận &e" + (int)expReward + " &atu vi!");
            }
            MessageUtils.playSound(p, Sound.BLOCK_BREWING_STAND_BREW);
        } else {
            double failRand = random.nextDouble() * 100;
            if (failRand < 60) {
                inv.setItem(RESULT_SLOTS[0], new ItemBuilder(Material.GUNPOWDER)
                        .setName("&7Phế Liệu Luyện Đan")
                        .setLore("", "&7Có thể dùng làm phân bón").setAmount(1).build());
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lLuyện Đan Thất Bại")
                        .setLore("", "&7Thu được phế liệu.").build());
                MessageUtils.send(p, "&eLuyện đan thất bại! Thu được phế liệu.");
            } else {
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lLuyện Đan Thất Bại")
                        .setLore("", "&7Mất hết nguyên liệu!").build());
                MessageUtils.send(p, "&cLuyện đan thất bại! Mất hết nguyên liệu!");
            }
            p.updateInventory();
            MessageUtils.playSound(p, Sound.ENTITY_ITEM_BREAK);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Đan")) return;
        Player player = (Player) event.getPlayer();
        AlchemySession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        if (session.activeBossBar != null) {
            session.activeBossBar.removeAll();
            session.activeBossBar = null;
        }

        Inventory gui = event.getInventory();
        for (int slot : INPUT_SLOTS) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item);
            }
        }

        activeSessions.remove(player.getUniqueId());
    }

    private String stripColor(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }

    // ==================== DATA CLASSES ====================

    public static class AlchemyRecipe {
        final String id;
        final String displayName;
        final Material resultMaterial;
        final String lore;
        final List<IngredientDef> ingredients;
        final int requiredLevel;
        final int cookingTime;
        final double successChance;

        public AlchemyRecipe(String id, String displayName, Material resultMaterial, String lore,
                            List<IngredientDef> ingredients, int requiredLevel,
                            int cookingTime, double successChance) {
            this.id = id;
            this.displayName = displayName;
            this.resultMaterial = resultMaterial;
            this.lore = lore;
            this.ingredients = ingredients;
            this.requiredLevel = requiredLevel;
            this.cookingTime = cookingTime;
            this.successChance = successChance;
        }
    }

    private static class AlchemySession {
        final UUID playerUUID;
        boolean isCrafting;
        BossBar activeBossBar;
        AlchemySession(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.isCrafting = false;
            this.activeBossBar = null;
        }
    }
}