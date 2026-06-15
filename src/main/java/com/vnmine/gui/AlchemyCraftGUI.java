package com.vnmine.gui;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.skill.PlayerSkillData;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AlchemyCraftGUI implements Listener {

    private final VNMinePlugin plugin;
    private final MainMenuGUI mainMenu;

    private static final int SLOT_INPUT_1 = 19;
    private static final int SLOT_INPUT_2 = 20;
    private static final int SLOT_INPUT_3 = 21;
    private static final int SLOT_INPUT_4 = 28;
    private static final int SLOT_INPUT_5 = 29;
    private static final int SLOT_INPUT_6 = 30;
    private static final int SLOT_RESULT = 24;
    private static final int SLOT_CRAFT = 22;
    private static final int SLOT_STATUS = 40;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_GUIDE = 46;

    // Phẩm cấp hiển thị (có dấu)
    private static final String[] GRADE_DISPLAY = {
        "&7&oHoàng cấp &fHạ phẩm", "&7&oHoàng cấp &eTrung phẩm", "&7&oHoàng cấp &aThượng phẩm",
        "&b&oHuyền cấp &fHạ phẩm", "&b&oHuyền cấp &eTrung phẩm", "&b&oHuyền cấp &aThượng phẩm",
        "&5&oĐịa cấp &fHạ phẩm", "&5&oĐịa cấp &eTrung phẩm", "&5&oĐịa cấp &aThượng phẩm",
        "&6&oThiên cấp &fHạ phẩm", "&6&oThiên cấp &eTrung phẩm", "&6&oThiên cấp &aThượng phẩm"
    };
    private static final double[] GRADE_MULTIPLIERS = {
        1.0, 1.3, 1.6, 2.0, 2.5, 3.0, 3.5, 4.0, 5.0, 6.0, 7.5, 10.0
    };
    private static final int[] GRADE_YIELDS = { 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5 };

    // Danh sách công thức đan dược (tất cả tên phải có dấu, đồng bộ với PillUseListener)
    private static final List<AlchemyRecipe> RECIPES = new ArrayList<>();

    static {
        // === Đan dược cơ bản ===
        RECIPES.add(new AlchemyRecipe("HOI_LINH_DAN", "&aHồi Linh Đan", Material.GLOWSTONE_DUST,
                "&7Hồi phục &b30 &7linh lực",
                new LinkedHashMap<Material, Integer>() {{ put(Material.GREEN_DYE, 3); put(Material.POTION, 1); }},
                3, 10, 80.0, 2));

        RECIPES.add(new AlchemyRecipe("DAI_HOI_LINH_DAN", "&bĐại Hồi Linh Đan", Material.GLOWSTONE,
                "&7Hồi phục &b100 &7linh lực &7+ hồi phục 30s",
                new LinkedHashMap<Material, Integer>() {{ put(Material.GLOWSTONE_DUST, 2); put(Material.RED_DYE, 2); put(Material.GREEN_DYE, 5); }},
                10, 30, 60.0, 1));

        RECIPES.add(new AlchemyRecipe("THANH_TAM_DAN", "&aThanh Tâm Đan", Material.SUGAR,
                "&7Giải trừ mọi trạng thái xấu",
                new LinkedHashMap<Material, Integer>() {{ put(Material.GREEN_DYE, 5); put(Material.POTION, 1); }},
                5, 15, 85.0, 2));

        RECIPES.add(new AlchemyRecipe("TOC_THANH_DAN", "&bTốc Thánh Đan", Material.FEATHER,
                "&7Tăng &b50% tốc độ &7trong 30 giây",
                new LinkedHashMap<Material, Integer>() {{ put(Material.GREEN_DYE, 3); put(Material.SUGAR, 2); put(Material.FEATHER, 1); }},
                8, 15, 70.0, 2));

        RECIPES.add(new AlchemyRecipe("CUONG_THE_DAN", "&cCương Thể Đan", Material.REDSTONE_BLOCK,
                "&7Tăng &c20% sát thương &7trong 60 giây",
                new LinkedHashMap<Material, Integer>() {{ put(Material.RED_DYE, 3); put(Material.GREEN_DYE, 5); put(Material.BLAZE_POWDER, 1); }},
                15, 20, 55.0, 1));

        RECIPES.add(new AlchemyRecipe("BACH_DOC_DAN", "&9Bách Độc Đan", Material.CYAN_DYE,
                "&7Miễn nhiễm độc &95 &7phút",
                new LinkedHashMap<Material, Integer>() {{ put(Material.LIGHT_BLUE_DYE, 3); put(Material.REDSTONE, 2); put(Material.BLAZE_POWDER, 1); }},
                25, 25, 50.0, 1));

        RECIPES.add(new AlchemyRecipe("TU_LUYEN_DAN", "&5Tu Luyện Đan", Material.PURPLE_DYE,
                "&7Tăng &5+50 EXP &7tu luyện khi sử dụng",
                new LinkedHashMap<Material, Integer>() {{ put(Material.GREEN_DYE, 10); put(Material.RED_DYE, 5); put(Material.ORANGE_DYE, 2); put(Material.GOLD_INGOT, 1); }},
                20, 45, 40.0, 1));

        RECIPES.add(new AlchemyRecipe("THIEN_HOI_DAN", "&6Thiên Hồi Đan", Material.GOLDEN_APPLE,
                "&7Hồi &a50% HP &7+ &b50% Linh lực",
                new LinkedHashMap<Material, Integer>() {{ put(Material.CYAN_DYE, 5); put(Material.MAGENTA_DYE, 3); put(Material.PRISMARINE_SHARD, 1); }},
                35, 40, 45.0, 1));

        RECIPES.add(new AlchemyRecipe("PHE_MA_DAN", "&8Phê Ma Đan", Material.REDSTONE,
                "&7Tăng &c30% sát thương &7vs quái 2 phút",
                new LinkedHashMap<Material, Integer>() {{ put(Material.YELLOW_DYE, 3); put(Material.IRON_INGOT, 2); put(Material.END_STONE, 1); }},
                40, 30, 40.0, 1));

        RECIPES.add(new AlchemyRecipe("TRUONG_THO_DAN", "&6Trường Thọ Đan", Material.NETHER_STAR,
                "&7Tự động hồi sinh 1 lần &7(CD 1h)",
                new LinkedHashMap<Material, Integer>() {{ put(Material.PURPLE_DYE, 5); put(Material.IRON_NUGGET, 3); put(Material.EMERALD, 2); }},
                45, 60, 25.0, 1));

        RECIPES.add(new AlchemyRecipe("PHI_THANG_DAN", "&6&l◆ Phi Thăng Đan ◆", Material.NETHER_STAR,
                "&7+500 EXP &7(1 lần/đại cảnh giới)",
                new LinkedHashMap<Material, Integer>() {{ put(Material.PURPLE_DYE, 3); put(Material.ORANGE_DYE, 10); put(Material.DRAGON_BREATH, 1); put(Material.NETHERITE_INGOT, 2); }},
                50, 120, 15.0, 1));
    }

    private static final Map<UUID, AlchemySession> activeSessions = new HashMap<>();

    public AlchemyCraftGUI(VNMinePlugin plugin, MainMenuGUI mainMenu) {
        this.plugin = plugin;
        this.mainMenu = mainMenu;
    }

    public void open(Player player) {
        plugin.getLogger().info("[AlchemyDebug] open() called for " + player.getName());
        Inventory gui = Bukkit.createInventory(null, 54, ColorUtils.colorize("&8✦ Luyện Đan - Khống Hỏa Thuật ✦"));
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&r").build();
        for (int slot : new int[]{19, 20, 21, 28, 29, 30}) gui.setItem(slot, null);
        gui.setItem(SLOT_CRAFT, new ItemBuilder(Material.FIRE_CHARGE).setGlow(true).setName("&c&l🔥 Luyện Đan")
                .setLore("", "&7Đặt nguyên liệu vào ô bên trái", "&7Bấm nút này để luyện đan", "", "&cYêu cầu: Kỹ năng Khống Hỏa Thuật", "&7Level 21+ (Trúc Cơ)").build());
        gui.setItem(SLOT_RESULT, null);
        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.PAPER).setName("&e&lTrạng Thái").setLore("", "&7Sẵn sàng luyện đan!").build());
        gui.setItem(SLOT_GUIDE, new ItemBuilder(Material.KNOWLEDGE_BOOK).setName("&6&lCông Thức Luyện Đan")
                .setLore("", "&aHồi Linh Đan: &73 Linh Thảo + 1 Nước Tinh Khiết",
                        "&bĐại Hồi Linh Đan: &72 HLD + 2 HLT + 5 LT",
                        "&cCương Thể Đan: &73 HLT + 5 LT + 1 Blaze Powder",
                        "&aThanh Tâm Đan: &75 LT + 1 Nước Tinh Khiết",
                        "&bTốc Thánh Đan: &73 LT + 2 Đường + 1 Lông vũ",
                        "&5Tu Luyện Đan: &710 LT + 5 HLT + 2 LHT + 1 Vàng",
                        "&6Phi Thăng Đan: &73 TLD + 10 LHT + 1 Hơi Rồng + 2 Thạch Anh Hắc",
                        "&9Bách Độc Đan: &73 Binh LT + 2 Huyết Thạch + 1 Blaze Powder",
                        "&6Thiên Hồi Đan: &75 Thùy LT + 3 Hoa LT + 1 Long Lân",
                        "&8Phê Ma Đan: &73 Lôi LT + 2 Huyền Kim + 1 Thiên Thạch",
                        "&6Trường Thọ Đan: &75 VNLCT + 3 Ngân Sa + 2 Ngọc").build());
        for (int i = 0; i < 54; i++) if (gui.getItem(i) == null && !isInputSlot(i) && i != SLOT_RESULT) gui.setItem(i, border);
        gui.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW).setName("&e&l← Quay Lai").build());
        player.openInventory(gui);
        activeSessions.put(player.getUniqueId(), new AlchemySession(player.getUniqueId()));
    }

    private boolean isInputSlot(int slot) {
        return slot == SLOT_INPUT_1 || slot == SLOT_INPUT_2 || slot == SLOT_INPUT_3 || slot == SLOT_INPUT_4 || slot == SLOT_INPUT_5 || slot == SLOT_INPUT_6;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Đan")) return;
        for (Integer slot : event.getRawSlots()) { if (slot < 54 && !isInputSlot(slot)) { event.setCancelled(true); return; } }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String rawTitle = event.getView().getTitle();
        String title = ColorUtils.stripColor(rawTitle);
        plugin.getLogger().info("[AlchemyDebug] Click detected! title='" + title + "' rawSlot=" + event.getRawSlot() + " click=" + event.getClick() + " clickedItem=" + event.getCurrentItem());
        if (!title.contains("Luyện Đan")) return;
        int slot = event.getRawSlot();
        if (slot >= 54) return;
        if (slot < 0) { event.setCancelled(true); return; }
        if (isInputSlot(slot) || slot == SLOT_RESULT) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        AlchemySession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            plugin.getLogger().info("[AlchemyDebug] Session is NULL for " + player.getName());
            return;
        }
        plugin.getLogger().info("[AlchemyDebug] Slot=" + slot + " CRAFT_SLOT=" + SLOT_CRAFT + " " + (slot == SLOT_CRAFT ? "CRAFT CLICKED" : "other"));
        if (session.isCrafting) {
            MessageUtils.send(player, "&cĐang trong quá trình luyện đan, vui lòng chờ!");
            return;
        }
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            plugin.getLogger().info("[AlchemyDebug] Clicked item is NULL or AIR! slot=" + slot);
            return;
        }
        ClickType clickType = event.getClick();
        if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) {
            switch (slot) {
                case SLOT_CRAFT:
                    plugin.getLogger().info("[AlchemyDebug] CRAFT BUTTON PRESSED by " + player.getName() + " - calling attemptCraft");
                    MessageUtils.send(player, "&a✦ Đang bắt đầu quá trình luyện đan...");
                    attemptCraft(player, session);
                    break;
                case SLOT_BACK: mainMenu.openMainMenu(player); break;
                case SLOT_GUIDE:
                    MessageUtils.send(player, "&6&l=== Công Thức Luyện Đan ===");
                    MessageUtils.send(player, "&aHồi Linh Đan: &73 LT + 1 Nước → Hồi 30 linh lực");
                    MessageUtils.send(player, "&bĐại Hồi Linh Đan: &72 HLD + 2 HLT + 5 LT → Hồi 100 linh lực");
                    MessageUtils.send(player, "&cCương Thể Đan: &73 HLT + 5 LT + 1 Blaze → +20% DMG 60s");
                    MessageUtils.send(player, "&aThanh Tâm Đan: &75 LT + 1 Nước → Giải trừ debuff");
                    MessageUtils.send(player, "&bTốc Thánh Đan: &73 LT + 2 Đường + 1 Lông → +50% tốc độ 30s");
                    MessageUtils.send(player, "&5Tu Luyện Đan: &710 LT + 5 HLT + 2 LHT + 1 Vàng → +50 EXP");
                    MessageUtils.send(player, "&6Phi Thăng Đan: &73 TLD + 10 LHT + 1 HR + 2 Thạch Anh → +500 EXP");
                    MessageUtils.send(player, "&9Bách Độc Đan: &73 Binh LT + 2 Huyết Thạch + 1 Blaze → Miễn độc 5p");
                    MessageUtils.send(player, "&6Thiên Hồi Đan: &75 Thùy LT + 3 Hoa LT + 1 Long Lân → Hồi 50% HP+Mana");
                    MessageUtils.send(player, "&8Phê Ma Đan: &73 Lôi LT + 2 Huyền Kim + 1 Thiên Thạch → +30% DMG quái");
                    MessageUtils.send(player, "&6Trường Thọ Đan: &75 VNLCT + 3 Ngân Sa + 2 Ngọc → Hồi sinh 1 lần");
                    break;
            }
        }
    }

    /**
     * Tính phẩm cấp đan dược dựa trên tu vi + độ thuần thục Khống Hỏa Thuật
     */
    private int calculatePillGrade(PlayerCultivationData playerData, PlayerSkillData skillData) {
        int bonus = 0;
        if (skillData != null) bonus += skillData.getAlchemyGradeBonus();
        if (playerData != null) {
            int level = playerData.getLevel();
            if (level >= 71) bonus += 3; else if (level >= 51) bonus += 2; else if (level >= 31) bonus += 1;
        }
        return Math.max(0, Math.min(bonus, 11));
    }

    /**
     * Tính thời gian luyện đan: giảm theo độ thuần thục Khống Hỏa Thuật
     * Tối thiểu 10 giây
     */
    private long calculateCookingTime(int baseTime, PlayerSkillData skillData) {
        double timeReduction = (skillData != null) ? skillData.getAlchemyTimeReduction() : 0.0;
        long adjusted = Math.max(10, (long)(baseTime * (1.0 - timeReduction)));
        return adjusted;
    }

    private int getGradeYield(int gradeIndex) {
        if (gradeIndex < 0 || gradeIndex >= GRADE_YIELDS.length) return 1;
        return GRADE_YIELDS[gradeIndex];
    }

    private double getGradeMultiplier(int gradeIndex) {
        if (gradeIndex < 0 || gradeIndex >= GRADE_MULTIPLIERS.length) return 1.0;
        return GRADE_MULTIPLIERS[gradeIndex];
    }

    /**
     * Bắt đầu quá trình luyện đan với BossBar progress
     */
    private void attemptCraft(Player player, AlchemySession session) {
        Inventory gui = player.getOpenInventory().getTopInventory();
        PlayerCultivationData playerData = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());

        // KIỂM TRA SKILL KHỐNG HỎA THUẬT
        if (playerData == null || !playerData.hasLearnedSkill("FIRE_CONTROL")) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.BARRIER)
                    .setName("&c&lChưa Học Khống Hỏa Thuật!")
                    .setLore("", "&7Bạn cần học kỹ năng &cKhống Hỏa Thuật",
                            "&7để có thể luyện đan.", "", "&eYêu cầu: Level 21+ (Trúc Cơ)")
                    .build());
            MessageUtils.send(player, "&c✦ Bạn chưa học Khống Hỏa Thuật! Cần có kỹ năng này để luyện đan!");
            MessageUtils.send(player, "&7Học tại menu Công Pháp hoặc dùng sách công pháp.");
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // THU THẬP NGUYÊN LIỆU
        Map<Material, Integer> ingredients = new HashMap<>();
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                ingredients.put(item.getType(), ingredients.getOrDefault(item.getType(), 0) + item.getAmount());
            }
        }

        if (ingredients.isEmpty()) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE).setName("&c&lKhông Có Nguyên Liệu").setLore("", "&7Hãy đặt nguyên liệu vào ô bên trái!").build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // TÌM CÔNG THỨC PHÙ HỢP
        AlchemyRecipe matchedRecipe = null;
        for (AlchemyRecipe recipe : RECIPES) {
            if (matchesRecipe(recipe, ingredients)) { matchedRecipe = recipe; break; }
        }

        if (matchedRecipe == null) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE).setName("&c&lKhông Có Công Thức Phù Hợp").setLore("", "&7Nguyên liệu không khớp với bất kỳ công thức nào!").build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // KIỂM TRA LEVEL
        if (playerData != null && playerData.getLevel() < matchedRecipe.requiredLevel) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lKhông Đủ Tu Vi")
                    .setLore("", "&7Yêu cầu: &cLevel " + matchedRecipe.requiredLevel, "&7Hiện tại: &eLevel " + playerData.getLevel()).build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // TÍNH PHẨM CẤP DỰ KIẾN
        int pillGradeIndex = calculatePillGrade(playerData, skillData);
        String pillGradeDisplay = GRADE_DISPLAY[pillGradeIndex];

        // TÍNH THỜI GIAN LUYỆN ĐAN
        long adjustedTime = calculateCookingTime(matchedRecipe.cookingTime, skillData);

        // TÍNH PROFICIENCY INFO
        String profName = (skillData != null) ? ColorUtils.colorize(skillData.getFireControlProficiencyName()) : "&7N/A";
        int profBonus = (skillData != null) ? skillData.getAlchemyGradeBonus() : 0;
        double timeReduction = (skillData != null) ? skillData.getAlchemyTimeReduction() : 0.0;

        // HIỂN THỊ THÔNG TRƯỚC KHI LUYỆN
        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.FURNACE).setName("&6&lĐang Luyện Đan...")
                .setLore("", "&7Đan dược: " + matchedRecipe.displayName,
                        "&7Phẩm cấp dự kiến: " + pillGradeDisplay,
                        "&7Khống Hỏa Thuật: " + profName + " &7(+" + profBonus + " phẩm)",
                        "&7Giảm thời gian: &c" + (int)(timeReduction * 100) + "%",
                        "&7Thời gian: &e" + adjustedTime + " giây",
                        "&7Tỷ lệ: &a" + matchedRecipe.successChance + "%").build());
        player.updateInventory();

        // TIÊU HAO NGUYÊN LIỆU
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                int needed = matchedRecipe.ingredients.getOrDefault(item.getType(), 0);
                if (needed > 0) {
                    int surplus = item.getAmount() - needed;
                    if (surplus > 0) { ItemStack returnItem = item.clone(); returnItem.setAmount(surplus); player.getInventory().addItem(returnItem); }
                    gui.setItem(slot, null);
                }
            }
        }

        // ĐỒNG BỘ INVENTORY SAU KHI TIÊU HAO NGUYÊN LIỆU
        player.updateInventory();

        // ĐẶT TRẠNG THÁI ĐANG LUYỆN
        session.isCrafting = true;
        MessageUtils.playSound(player, Sound.BLOCK_FIRE_AMBIENT);

        // TẠO BOSSBAR PROGRESS
        final UUID playerUUID = player.getUniqueId();
        final AlchemyRecipe finalRecipe = matchedRecipe;
        final PlayerCultivationData finalData = playerData;
        final PlayerSkillData finalSkillData = skillData;
        final Inventory finalGui = gui;
        final int finalGradeIndex = pillGradeIndex;
        final String finalGradeDisplay = pillGradeDisplay;

        BossBar bossBar = Bukkit.createBossBar(
                ColorUtils.colorize("&c🔥 Đang luyện " + stripColor(matchedRecipe.displayName) + "..."),
                BarColor.RED, BarStyle.SEGMENTED_10, BarFlag.CREATE_FOG);
        bossBar.addPlayer(player);
        session.activeBossBar = bossBar;

        // Chạy BossBar progress
        final long totalTicks = adjustedTime * 20L;
        final long intervalTicks = 10L; // 0.5 giây mỗi lần cập nhật
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
                Inventory inv = p.getOpenInventory().getTopInventory();
                if (!inv.equals(finalGui)) {
                    bossBar.removeAll();
                    session.isCrafting = false;
                    cancel();
                    return;
                }

                currentStep++;
                double progress = Math.min(1.0, (double) currentStep / totalSteps);
                bossBar.setProgress(progress);

                // Cập nhật title trên BossBar
                int percent = (int) (progress * 100);
                String barText = String.format("§c🔥 [%d%%] Đang luyện %s...", percent, stripColor(finalRecipe.displayName));
                bossBar.setTitle(barText);

                // Cập nhật status slot
                String progressText = "§6§l" + "█".repeat(Math.max(0, percent / 5)) + "§7" + "░".repeat(Math.max(0, 20 - percent / 5));
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.FURNACE).setName("&6&lĐang Luyện Đan... " + percent + "%")
                        .setLore("", "&7Đan dược: " + finalRecipe.displayName,
                                "&7Phẩm cấp dự kiến: " + finalGradeDisplay,
                                "", progressText,
                                "&7Thời gian còn lại: &e" + Math.max(0, (int)((totalTicks - currentStep * intervalTicks) / 20)) + "s").build());
                p.updateInventory();

                if (currentStep >= totalSteps) {
                    cancel();
                    bossBar.removeAll();
                    session.isCrafting = false;
                    finishCraft(p, finalGui, finalRecipe, finalData, finalSkillData, finalGradeIndex, finalGradeDisplay);
                }
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    /**
     * Hoàn thành luyện đan - nhận kết quả
     */
    private void finishCraft(Player p, Inventory inv, AlchemyRecipe recipe, PlayerCultivationData data,
                             PlayerSkillData skillData, int gradeIndex, String gradeDisplay) {
        if (p == null || !p.isOnline()) return;
        if (!inv.equals(p.getOpenInventory().getTopInventory())) return;

        // Tính tỷ lệ thành công
        double chance = recipe.successChance;
        int levelBonus = (data != null) ? data.getLevel() : 0;
        chance += levelBonus * 1.5;
        chance = Math.min(chance, 95.0);

        Random random = new Random();
        boolean success = random.nextDouble() * 100 < chance;

        if (success) {
            int baseYield = getGradeYield(gradeIndex);
            double gradeMultiplier = getGradeMultiplier(gradeIndex);

            // Tạo item kết quả
            ItemStack result = new ItemBuilder(recipe.resultMaterial)
                    .setName(recipe.displayName)
                    .setAmount(baseYield)
                    .setGlow(true)
                    .setLore("", recipe.lore, "",
                            gradeDisplay,
                            "&a✦ Luyện đan thành công! x" + baseYield,
                            "&7Tăng hiệu quả: &6x" + String.format("%.1f", gradeMultiplier))
                    .build();

            inv.setItem(SLOT_RESULT, result);
            inv.setItem(SLOT_STATUS, new ItemBuilder(Material.EMERALD).setName("&a&l✦ Luyện Đan Thành Công ✦")
                    .setLore("", "&7Đan dược: " + recipe.displayName,
                            "&7Phẩm cấp: " + gradeDisplay,
                            "&7Số lượng: &e" + baseYield,
                            "&7Tỷ lệ: &a" + String.format("%.1f", chance) + "%").build());
            p.updateInventory();

            // TĂNG PROFICIENCY KHỐNG HỎA THUẬT
            if (skillData != null) {
                skillData.incrementSkillUsage("FIRE_CONTROL");
                int usageCount = skillData.getSkillUsageCount("FIRE_CONTROL");
                PlayerSkillData.ProficiencyLevel newProf = skillData.getProficiencyLevel("FIRE_CONTROL");
                MessageUtils.send(p, "&c✦ Khống Hỏa Thuật: &e" + usageCount + " lần sử dụng → " + ColorUtils.colorize(newProf.getDisplayName()));
            }

            // TĂNG EXP
            if (data != null) {
                double expReward = recipe.cookingTime * 2;
                plugin.getCultivationManager().addExperience(p, expReward);
                data.incrementPillsCrafted();
                MessageUtils.send(p, "&a✦ Luyện đan thành công! Nhận &e" + (int) expReward + " &atu vi!");
            }
            MessageUtils.playSound(p, Sound.BLOCK_BREWING_STAND_BREW);
        } else {
            double failRand = random.nextDouble() * 100;
            if (failRand < 60) {
                inv.setItem(SLOT_RESULT, new ItemBuilder(Material.GUNPOWDER).setName("&7Phế Liệu Luyện Đan").setLore("", "&7Có thể dùng làm phân bón").setAmount(1).build());
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lLuyện Đan Thất Bại").setLore("", "&7Thu được phế liệu.").build());
                MessageUtils.send(p, "&eLuyện đan thất bại! Thu được phế liệu.");
            } else {
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lLuyện Đan Thất Bại").setLore("", "&7Mất hết nguyên liệu!").build());
                MessageUtils.send(p, "&cLuyện đan thất bại! Mất hết nguyên liệu!");
            }
            p.updateInventory();
            MessageUtils.playSound(p, Sound.ENTITY_ITEM_BREAK);
        }
    }

    /**
     * Loại bỏ dấu từ tên (để so sánh)
     */
    private String stripColor(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "").trim();
    }

    private boolean matchesRecipe(AlchemyRecipe recipe, Map<Material, Integer> ingredients) {
        if (recipe.ingredients.size() != ingredients.size()) return false;
        for (Map.Entry<Material, Integer> entry : recipe.ingredients.entrySet()) {
            Integer count = ingredients.get(entry.getKey());
            if (count == null || count < entry.getValue()) return false;
        }
        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Đan")) return;
        Player player = (Player) event.getPlayer();
        AlchemySession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        Inventory gui = event.getInventory();

        // Trả lại BossBar nếu đang炼
        if (session.activeBossBar != null) {
            session.activeBossBar.removeAll();
        }

        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6, SLOT_RESULT}) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) player.getInventory().addItem(item);
        }
        activeSessions.remove(player.getUniqueId());
    }

    public static class AlchemyRecipe {
        final String id;
        final String displayName;
        final Material resultMaterial;
        final String lore;
        final Map<Material, Integer> ingredients;
        final int requiredLevel;
        final int cookingTime;
        final double successChance;
        final int yield;

        public AlchemyRecipe(String id, String displayName, Material resultMaterial, String lore,
                            Map<Material, Integer> ingredients, int requiredLevel, int cookingTime, double successChance, int yield) {
            this.id = id; this.displayName = displayName; this.resultMaterial = resultMaterial;
            this.lore = lore; this.ingredients = ingredients; this.requiredLevel = requiredLevel;
            this.cookingTime = cookingTime; this.successChance = successChance; this.yield = yield;
        }
    }

    private static class AlchemySession {
        final UUID playerUUID;
        boolean isCrafting;
        BossBar activeBossBar;
        AlchemySession(UUID playerUUID) { this.playerUUID = playerUUID; this.isCrafting = false; this.activeBossBar = null; }
    }
}