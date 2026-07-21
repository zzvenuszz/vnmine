package com.vnmine.gui;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PillConfig;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
import com.vnmine.item.ItemDataLoader;
import com.vnmine.item.PillRecipe;
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

    private static final int DEFAULT_CHARGES = 10;

    private static final Map<UUID, AlchemySession> activeSessions = new HashMap<>();

    public AlchemyCraftGUI(VNMinePlugin plugin, MainMenuGUI mainMenu) {
        this.plugin = plugin;
        this.mainMenu = mainMenu;
    }

    /**
     * Lấy danh sách recipes từ ItemDataLoader (đã load từ YML)
     */
    private List<PillRecipe> getRecipes() {
        ItemDataLoader loader = plugin.getItemDataLoader();
        if (loader == null) return new ArrayList<>();
        Map<String, PillRecipe> recipeMap = loader.getPillRecipes();
        return new ArrayList<>(recipeMap.values());
    }

    /**
     * Lấy recipe theo ID
     */
    private PillRecipe getRecipeById(String id) {
        ItemDataLoader loader = plugin.getItemDataLoader();
        if (loader == null) return null;
        return loader.getPillRecipe(id);
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

        // Guide sách - build từ recipes
        List<PillRecipe> recipes = getRecipes();
        List<String> guideLore = new ArrayList<>();
        guideLore.add("");
        for (PillRecipe recipe : recipes) {
            StringBuilder sb = new StringBuilder();
            sb.append("&a◆ ").append(ColorUtils.stripColor(recipe.getDisplayName())).append(" ◆: &7");
            List<String> partNames = new ArrayList<>();
            for (PillRecipe.IngredientDef ing : recipe.getIngredients()) {
                if (ing.isHerb()) {
                    SpiritHerb h = SpiritHerb.getHerb(ing.getHerbId());
                    String herbName = (h != null) ? ColorUtils.stripColor(h.getName()) : ing.getHerbId();
                    partNames.add(herbName + " x" + ing.getCount());
                } else {
                    String matName = formatMaterialName(ing.getMaterial().name());
                    partNames.add(matName + " x" + ing.getCount());
                }
            }
            sb.append(String.join(" + ", partNames));
            guideLore.add(ColorUtils.colorize(sb.toString()));
        }

        gui.setItem(SLOT_GUIDE, new ItemBuilder(Material.KNOWLEDGE_BOOK).setName("&6&lCông Thức Luyện Đan")
                .setLore(guideLore.toArray(new String[0])).build());

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

    private double calculateSuccessChance(PillRecipe recipe, PlayerCultivationData playerData, int extraItemCount) {
        double chance = recipe.getSuccessChance();
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

    /**
     * Lấy Material cho Pill dựa vào ItemDefinition trong data loader
     */
    private Material getPillMaterial(String pillId) {
        ItemDataLoader loader = plugin.getItemDataLoader();
        if (loader != null) {
            com.vnmine.item.ItemDefinition def = loader.getItem(pillId);
            if (def != null) {
                return def.getMaterial();
            }
        }
        return Material.POTION;
    }

    private ItemStack createPotionItem(PillRecipe recipe, int gradeIndex, int amount) {
        Color potionColor = GRADE_COLORS[gradeIndex];
        double multiplier = GRADE_MULTIPLIERS[gradeIndex];
        Material pillMaterial = getPillMaterial(recipe.getId());

        PillConfig.PillEffect effect = plugin.getPillConfig().getEffect(recipe.getId());
        String effectLore = (effect != null) ? effect.getLore(multiplier) : "&7Đan dược quý giá";
        String flavorLore = plugin.getPillConfig().getRandomFlavor(recipe.getId());
        String gradeDisplay = GRADE_DISPLAY[gradeIndex];

        ItemBuilder builder = new ItemBuilder(pillMaterial)
                .setName(recipe.getDisplayName())
                .setAmount(Math.min(amount, 64))
                .setGlow(true)
                .setPersistentData("vnmine_pill_type", recipe.getId())
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
        final PillRecipe recipe;
        final Map<String, Integer> recipeHerbs;     // herbId -> count
        final Map<String, Integer> recipeMaterials; // material name -> count
        final int extraItemCount;

        IngredientCheckResult(PillRecipe recipe, Map<String, Integer> recipeHerbs,
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

        // Tìm công thức phù hợp từ YML
        List<PillRecipe> recipes = getRecipes();
        for (PillRecipe recipe : recipes) {
            // Tách ingredients thành herb và material requirements
            Map<String, Integer> requiredHerbs = new HashMap<>();
            Map<String, Integer> requiredMaterials = new HashMap<>();
            for (PillRecipe.IngredientDef ing : recipe.getIngredients()) {
                if (ing.isHerb()) {
                    requiredHerbs.put(ing.getHerbId(), requiredHerbs.getOrDefault(ing.getHerbId(), 0) + ing.getCount());
                } else {
                    requiredMaterials.put(ing.getMaterial().name(), requiredMaterials.getOrDefault(ing.getMaterial().name(), 0) + ing.getCount());
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

        PillRecipe matchedRecipe = checkResult.recipe;
        int extraItemCount = checkResult.extraItemCount;

        if (playerData.getLevel() < matchedRecipe.getRequiredLevel()) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lKhông Đủ Tu Vi")
                    .setLore("", "&7Yêu cầu: &cLevel " + matchedRecipe.getRequiredLevel(),
                            "&7Hiện tại: &eLevel " + playerData.getLevel()).build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        int ingredientQuality = calculateIngredientQuality(gui);
        int pillGradeIndex = calculatePillGrade(playerData, skillData, ingredientQuality);
        String pillGradeDisplay = GRADE_DISPLAY[pillGradeIndex];
        long adjustedTime = calculateCookingTime(matchedRecipe.getCookingTime(), skillData);
        double successChance = calculateSuccessChance(matchedRecipe, playerData, extraItemCount);

        String profName = (skillData != null) ? ColorUtils.colorize(skillData.getFireControlProficiencyName()) : "&7N/A";
        int profBonus = (skillData != null) ? skillData.getAlchemyGradeBonus() : 0;
        double timeReduction = (skillData != null) ? skillData.getAlchemyTimeReduction() : 0.0;

        List<String> statusLore = new ArrayList<>();
        statusLore.add("");
        statusLore.add(ColorUtils.colorize("&7Đan dược: " + matchedRecipe.getDisplayName()));
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
        final PillRecipe finalRecipe = matchedRecipe;
        final PlayerCultivationData finalData = playerData;
        final PlayerSkillData finalSkillData = skillData;
        final Inventory finalGui = gui;
        final int finalGradeIndex = pillGradeIndex;
        final double finalChance = successChance;
        final int finalExtraItems = extraItemCount;

        BossBar bossBar = Bukkit.createBossBar(
                ColorUtils.colorize("&a🔥 Đang luyện " + stripColor(matchedRecipe.getDisplayName()) + "..."),
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
                bossBar.setTitle(String.format("§a🔥 [%d%%] Đang luyện %s...", percent, stripColor(finalRecipe.getDisplayName())));

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
    private void consumeIngredients(Inventory gui, PillRecipe recipe, IngredientCheckResult checkResult) {
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

    private void finishCraft(Player p, Inventory inv, PillRecipe recipe, PlayerCultivationData data,
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
                    .setLore("", "&7Đan dược: " + recipe.getDisplayName(),
                            "&7Phẩm cấp: " + GRADE_DISPLAY[gradeIndex],
                            "&7Số lượng: &e" + yield + " lọ",
                            "&7Tỷ lệ: &a" + String.format("%.1f", chance) + "%").build());
            p.updateInventory();

            if (data != null) {
                double expReward = recipe.getCookingTime() * 2;
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

    /**
     * Format Material name sang tên tiếng Việt dễ đọc
     */
    private String formatMaterialName(String materialName) {
        // Một số từ đặc biệt
        switch (materialName) {
            case "POTION": return "Nước Tinh Khiết";
            case "BLAZE_POWDER": return "Bột Blaze";
            case "SUGAR": return "Đường";
            case "FEATHER": return "Lông Vũ";
            case "REDSTONE": return "Huyết Thạch";
            case "GOLD_INGOT": return "Vàng Thanh";
            case "IRON_INGOT": return "Sắt";
            case "BLUE_ICE": return "Băng Lam";
            case "PRISMARINE_SHARD": return "Long Lân";
            case "DRAGON_BREATH": return "Hơi Rồng";
            case "NETHERITE_INGOT": return "Netherite Thanh";
            case "DIAMOND": return "Kim Cương";
            case "EMERALD": return "Ngọc Lục Bảo";
            case "OBSIDIAN": return "Đá Obsidian";
            case "ENDER_PEARL": return "Mắt End";
            case "IRON_NUGGET": return "Ngân Sa";
            case "END_STONE": return "Thiên Thạch";
            case "GOLDEN_APPLE": return "Táo Vàng";
            case "GOLD_BLOCK": return "Khối Vàng";
            case "END_CRYSTAL": return "Pha Lê End";
            default: {
                // Format chung: thay _ bằng khoảng trắng, viết hoa chữ cái đầu
                String[] words = materialName.toLowerCase().split("_");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                    if (w.length() > 0) {
                        sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
                    }
                }
                return sb.toString().trim();
            }
        }
    }

    // ==================== DATA CLASSES ====================

    /**
     * AlchemyRecipe - wrapper giữ backward compatibility
     * Dữ liệu thực tế lấy từ PillRecipe trong ItemDataLoader
     */
    @Deprecated
    public static class AlchemyRecipe {
        final String id;
        final String displayName;
        final Material resultMaterial;
        final String lore;
        final List<com.vnmine.item.PillRecipe.IngredientDef> ingredients;
        final int requiredLevel;
        final int cookingTime;
        final double successChance;

        public AlchemyRecipe(String id, String displayName, Material resultMaterial, String lore,
                            List<com.vnmine.item.PillRecipe.IngredientDef> ingredients, int requiredLevel,
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