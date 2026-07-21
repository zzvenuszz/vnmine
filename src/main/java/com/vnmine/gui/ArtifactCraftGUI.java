package com.vnmine.gui;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ArtifactRecipe;
import com.vnmine.item.ItemBuilder;
import com.vnmine.item.ItemDataLoader;
import com.vnmine.skill.PlayerSkillData;
import com.vnmine.util.ColorUtils;
import com.vnmine.util.MessageUtils;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArtifactCraftGUI implements Listener {

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

    // ==================== 21-TIER ARTIFACT GRADE SYSTEM ====================
    // {giaiName, capName, giaiColor, capColor}
    private static final String[][] ARTIFACT_GRADES = {
        {"Phàm Giai", "Phàm Cấp", "&7", "&7"},
        {"Phàm Giai", "Hậu Thiên", "&7", "&8"},
        {"Phàm Giai", "Tiên Thiên", "&7", "&f"},
        {"Linh Giai", "Linh Cấp", "&a", "&a"},
        {"Linh Giai", "Địa Cấp", "&a", "&2"},
        {"Linh Giai", "Thiên Cấp", "&a", "&b"},
        {"Vương Giai", "Vương Cấp", "&6", "&6"},
        {"Vương Giai", "Hoàng Cấp", "&6", "&e"},
        {"Vương Giai", "Tôn Cấp", "&6", "&c"},
        {"Thánh Giai", "Nhập Thánh", "&f", "&f&l"},
        {"Thánh Giai", "Đại Thánh", "&f", "&d"},
        {"Đế Giai", "Đế Cấp", "&5", "&5"},
        {"Đế Giai", "Đại Đế", "&5", "&5&l"},
        {"Tiên Giai", "Chân Tiên", "&b", "&b&l"},
        {"Tiên Giai", "Kim Tiên", "&b", "&3"},
        {"Tiên Giai", "Tiên Đế", "&b", "&3&l"},
        {"Thần Giai", "Chân Thần", "&c", "&c&l"},
        {"Thần Giai", "Thiên Thần", "&c", "&4"},
        {"Thần Giai", "Chủ Thần", "&c", "&4&l"},
        {"Chí Tôn Giai", "Chí Tôn", "&4", "&6&l"},
        {"Chí Tôn Giai", "Chúa Tể", "&4", "&4&k"}
    };

    private static final double[] ARTIFACT_BONUS = {
        0.0, 0.1, 0.2, 0.35, 0.5, 0.65,
        0.8, 1.0, 1.2, 1.5, 1.8,
        2.2, 2.5, 3.0, 3.5, 4.0,
        4.5, 5.0, 6.0, 7.0, 8.0
    };

    // Material mapping theo grade cho từng loại pháp bảo
    private static final Material[][] ARTIFACT_MATERIALS = {
        // Kiếm Phi Hành: từ gỗ -> đá -> sắt -> vàng -> kim cương -> netherite
        {Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD,
         Material.IRON_SWORD, Material.IRON_SWORD, Material.IRON_SWORD,
         Material.DIAMOND_SWORD, Material.DIAMOND_SWORD, Material.DIAMOND_SWORD,
         Material.DIAMOND_SWORD, Material.DIAMOND_SWORD, Material.DIAMOND_SWORD,
         Material.DIAMOND_SWORD, Material.DIAMOND_SWORD, Material.DIAMOND_SWORD,
         Material.NETHERITE_SWORD, Material.NETHERITE_SWORD, Material.NETHERITE_SWORD,
         Material.NETHERITE_SWORD, Material.NETHERITE_SWORD, Material.NETHERITE_SWORD},
        // Linh Chung
        {Material.BELL, Material.BELL, Material.BELL,
         Material.BELL, Material.BELL, Material.BELL,
         Material.BELL, Material.BELL, Material.BELL,
         Material.BELL, Material.BELL, Material.BELL,
         Material.BELL, Material.BELL, Material.BELL,
         Material.BELL, Material.BELL, Material.BELL,
         Material.BELL, Material.BELL, Material.BELL},
        // Bát Quái Kính
        {Material.SHIELD, Material.SHIELD, Material.SHIELD,
         Material.SHIELD, Material.SHIELD, Material.SHIELD,
         Material.SHIELD, Material.SHIELD, Material.SHIELD,
         Material.SHIELD, Material.SHIELD, Material.SHIELD,
         Material.SHIELD, Material.SHIELD, Material.SHIELD,
         Material.SHIELD, Material.SHIELD, Material.SHIELD,
         Material.SHIELD, Material.SHIELD, Material.SHIELD},
        // Hồn Ngọc
        {Material.EMERALD, Material.EMERALD, Material.EMERALD,
         Material.EMERALD, Material.EMERALD, Material.EMERALD,
         Material.DIAMOND, Material.DIAMOND, Material.DIAMOND,
         Material.DIAMOND, Material.DIAMOND, Material.DIAMOND,
         Material.DIAMOND, Material.NETHERITE_SCRAP, Material.NETHERITE_SCRAP,
         Material.NETHERITE_SCRAP, Material.NETHERITE_SCRAP, Material.NETHERITE_SCRAP,
         Material.NETHERITE_SCRAP, Material.NETHERITE_SCRAP, Material.NETHERITE_SCRAP},
        // Thiên Linh Thuẫn
        {Material.IRON_CHESTPLATE, Material.IRON_CHESTPLATE, Material.IRON_CHESTPLATE,
         Material.IRON_CHESTPLATE, Material.IRON_CHESTPLATE, Material.IRON_CHESTPLATE,
         Material.DIAMOND_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_CHESTPLATE,
         Material.DIAMOND_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_CHESTPLATE,
         Material.DIAMOND_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_CHESTPLATE,
         Material.NETHERITE_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_CHESTPLATE,
         Material.NETHERITE_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_CHESTPLATE},
        // Lôi Ấn
        {Material.TRIDENT, Material.TRIDENT, Material.TRIDENT,
         Material.TRIDENT, Material.TRIDENT, Material.TRIDENT,
         Material.TRIDENT, Material.TRIDENT, Material.TRIDENT,
         Material.TRIDENT, Material.TRIDENT, Material.TRIDENT,
         Material.TRIDENT, Material.TRIDENT, Material.TRIDENT,
         Material.TRIDENT, Material.TRIDENT, Material.TRIDENT,
         Material.TRIDENT, Material.TRIDENT, Material.TRIDENT},
        // Phượng Hoàng Lệnh
        {Material.FEATHER, Material.FEATHER, Material.FEATHER,
         Material.FEATHER, Material.FEATHER, Material.FEATHER,
         Material.FEATHER, Material.FEATHER, Material.FEATHER,
         Material.FEATHER, Material.FEATHER, Material.FEATHER,
         Material.FEATHER, Material.FEATHER, Material.FEATHER,
         Material.FEATHER, Material.FEATHER, Material.FEATHER,
         Material.FEATHER, Material.FEATHER, Material.FEATHER}
    };

    /** Lấy prefix màu cho grade */
    private static String gradeColorPrefix(int tierIndex) {
        String[] g = ARTIFACT_GRADES[tierIndex];
        return g[2] + "[" + g[0] + "]-" + g[3] + "[" + g[1] + "] &f&l";
    }

    /** Lấy tên grade thuần (không màu) dùng cho so sánh */
    private static String gradePlainName(int tierIndex) {
        String[] g = ARTIFACT_GRADES[tierIndex];
        return "[" + g[0] + "]-[" + g[1] + "]";
    }

    /** Lấy tên grade hiển thị đầy đủ (có màu) */
    private static String gradeDisplayName(int tierIndex) {
        return gradeColorPrefix(tierIndex);
    }

    private static String gradeLabelPlain(int tierIndex) {
        return ARTIFACT_GRADES[tierIndex][0] + " " + ARTIFACT_GRADES[tierIndex][1];
    }

    private boolean isInputSlot(int slot) {
        return slot == SLOT_INPUT_1 || slot == SLOT_INPUT_2 || slot == SLOT_INPUT_3 ||
               slot == SLOT_INPUT_4 || slot == SLOT_INPUT_5 || slot == SLOT_INPUT_6;
    }

    private static final Map<UUID, ArtifactSession> activeSessions = new HashMap<>();

    public ArtifactCraftGUI(VNMinePlugin plugin, MainMenuGUI mainMenu) {
        this.plugin = plugin;
        this.mainMenu = mainMenu;
    }

    /**
     * Lấy danh sách artifact recipes từ ItemDataLoader (đã load từ YML)
     */
    private List<ArtifactRecipe> getRecipes() {
        ItemDataLoader loader = plugin.getItemDataLoader();
        if (loader == null) return new ArrayList<>();
        Map<String, ArtifactRecipe> recipeMap = loader.getArtifactRecipes();
        return new ArrayList<>(recipeMap.values());
    }

    /**
     * Lấy recipe index trong danh sách recipes (để map material)
     */
    private int getRecipeIndex(String recipeId) {
        List<ArtifactRecipe> recipes = getRecipes();
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).getId().equals(recipeId)) return i;
        }
        return -1;
    }

    public void open(Player player) {
        plugin.getLogger().info("[ArtifactDebug] open() called for " + player.getName());
        Inventory gui = Bukkit.createInventory(null, 54, ColorUtils.colorize("&8✦ Luyện Chế Pháp Bảo ✦"));
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) gui.setItem(slot, null);
        gui.setItem(SLOT_CRAFT, new ItemBuilder(Material.ANVIL).setGlow(true).setName("&6&l⚒ Chế Tạo Pháp Bảo")
                .setLore("", "&7Đặt vật liệu vào ô bên trái", "&7Bấm nút này để luyện chế", "", "&cYêu cầu: Luyện Khí Thuật").build());
        gui.setItem(SLOT_RESULT, null);
        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.PAPER).setName("&e&lTrạng Thái").setLore("", "&7Sẵn sàng chế tạo!").build());

        // Guide sách - build từ recipes
        List<ArtifactRecipe> recipes = getRecipes();
        List<String> guideLore = new ArrayList<>();
        guideLore.add("");
        for (ArtifactRecipe recipe : recipes) {
            StringBuilder sb = new StringBuilder();
            sb.append("&b◆ ").append(ColorUtils.stripColor(recipe.getDisplayName())).append(" ◆: &7");
            List<String> partNames = new ArrayList<>();
            for (ArtifactRecipe.IngredientDef ing : recipe.getIngredients()) {
                if (ing.isHerb()) {
                    partNames.add(ing.getHerbId() + " x" + ing.getCount());
                } else {
                    String matName = formatMaterialName(ing.getMaterial().name());
                    partNames.add(matName + " x" + ing.getCount());
                }
            }
            sb.append(String.join(" + ", partNames));
            guideLore.add(ColorUtils.colorize(sb.toString()));
        }

        gui.setItem(SLOT_GUIDE, new ItemBuilder(Material.KNOWLEDGE_BOOK).setName("&6&lCông Thức Pháp Bảo")
                .setLore(guideLore.toArray(new String[0])).build());

        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&r").build();
        for (int i = 0; i < 54; i++) if (gui.getItem(i) == null && !isInputSlot(i) && i != SLOT_RESULT) gui.setItem(i, border);
        gui.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW).setName("&e&l← Quay Lai").build());
        player.openInventory(gui);
        activeSessions.put(player.getUniqueId(), new ArtifactSession(player.getUniqueId()));
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Chế Pháp Bảo")) return;
        for (Integer slot : event.getRawSlots()) { if (slot < 54 && !isInputSlot(slot)) { event.setCancelled(true); return; } }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player eventPlayer = (Player) event.getWhoClicked();
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Chế Pháp Bảo")) return;
        int slot = event.getRawSlot();
        if (slot < 0) { event.setCancelled(true); return; }
        plugin.getLogger().info("[ArtifactDebug] Click slot=" + slot + " player=" + eventPlayer.getName());

        // === Player inventory clicks (slot >= 54) ===
        if (slot >= 54) {
            // Handle shift-click from player inventory: place into input slots ONLY (not result)
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                event.setCancelled(true);
                Player player = eventPlayer;
                Inventory gui = player.getOpenInventory().getTopInventory();
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

                for (int inputSlot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
                    ItemStack inputItem = gui.getItem(inputSlot);
                    if (inputItem == null || inputItem.getType() == Material.AIR) {
                        gui.setItem(inputSlot, clickedItem.clone());
                        player.getInventory().setItem(event.getSlot(), null);
                        player.updateInventory();
                        return;
                    } else if (inputItem.isSimilar(clickedItem) && inputItem.getAmount() < inputItem.getMaxStackSize()) {
                        int canAdd = inputItem.getMaxStackSize() - inputItem.getAmount();
                        int toAdd = Math.min(canAdd, clickedItem.getAmount());
                        inputItem.setAmount(inputItem.getAmount() + toAdd);
                        clickedItem.setAmount(clickedItem.getAmount() - toAdd);
                        if (clickedItem.getAmount() <= 0) {
                            player.getInventory().setItem(event.getSlot(), null);
                        }
                        player.updateInventory();
                        return;
                    }
                }
            }
            return;
        }

        // === Input slots: allow default placement/removal ===
        if (isInputSlot(slot)) return;

        // === Result slot (OUTPUT): allow picking up, block placing ===
        if (slot == SLOT_RESULT) {
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            boolean cursorHasItem = cursor != null && cursor.getType() != Material.AIR;
            boolean slotHasItem = currentItem != null && currentItem.getType() != Material.AIR;

            if (cursorHasItem) {
                event.setCancelled(true);
                return;
            }

            if (slotHasItem) {
                Player resultPlayer = eventPlayer;
                if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                    event.setCancelled(true);
                    Map<Integer, ItemStack> leftover = resultPlayer.getInventory().addItem(currentItem.clone());
                    if (leftover.isEmpty()) {
                        event.getView().getTopInventory().setItem(SLOT_RESULT, null);
                    }
                    resultPlayer.updateInventory();
                    return;
                }
                return;
            }

            event.setCancelled(true);
            return;
        }

        // === All other GUI slots (borders, etc): block interaction ===
        event.setCancelled(true);
        Player player = eventPlayer;
        ArtifactSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        ClickType clickType = event.getClick();
        if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) {
            switch (slot) {
                case SLOT_CRAFT:
                    MessageUtils.send(player, "&6✦ Đang bắt đầu quá trình luyện khí...");
                    attemptCraft(player, session);
                    break;
                case SLOT_BACK: mainMenu.openMainMenu(player); break;
                case SLOT_GUIDE: showRecipe(player); break;
            }
        }
    }

    private int calculateArtifactGrade(PlayerCultivationData playerData, PlayerSkillData skillData) {
        int bonus = 0;
        if (skillData != null) bonus += skillData.getForgeGradeBonus();
        if (playerData != null) {
            int level = playerData.getLevel();
            if (level >= 81) bonus += 7; else if (level >= 71) bonus += 5;
            else if (level >= 61) bonus += 4; else if (level >= 51) bonus += 3;
            else if (level >= 41) bonus += 2; else if (level >= 31) bonus += 1;
        }
        return Math.max(0, Math.min(bonus, ARTIFACT_GRADES.length - 1));
    }

    private void attemptCraft(Player player, ArtifactSession session) {
        long startTime = System.currentTimeMillis();
        plugin.getLogger().info("[ArtifactDebug] attemptCraft START for " + player.getName());
        Inventory gui = player.getOpenInventory().getTopInventory();
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());

        plugin.getLogger().info("[ArtifactDebug] playerData=" + (data != null ? "OK" : "NULL")
            + " skillData=" + (skillData != null ? "OK" : "NULL"));

        if (data == null || !data.hasLearnedSkill("FORGE_MASTERY")) {
            plugin.getLogger().info("[ArtifactDebug] FAIL: FORGE_MASTERY not learned!");
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.BARRIER)
                    .setName("&c&lChưa Học Luyện Khí Thuật!")
                    .setLore("", "&7Bạn cần học kỹ năng &6Luyện Khí Thuật",
                            "&7để có thể chế tạo pháp bảo.", "", "&eYêu cầu: Level 15+").build());
            MessageUtils.send(player, "&c✦ Bạn chưa học Luyện Khí Thuật!");
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // Thu thập input: herbId -> count và material -> count
        Map<String, Integer> inputHerbs = new HashMap<>();
        Map<Material, Integer> inputMaterials = new HashMap<>();
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                String herbId = ItemBuilder.getPersistentData(item, "vnmine_herb");
                if (herbId != null) {
                    inputHerbs.put(herbId, inputHerbs.getOrDefault(herbId, 0) + item.getAmount());
                } else {
                    inputMaterials.put(item.getType(), inputMaterials.getOrDefault(item.getType(), 0) + item.getAmount());
                }
            }
        }

        plugin.getLogger().info("[ArtifactDebug] Herbs found: " + inputHerbs + " Materials: " + inputMaterials);

        if (inputHerbs.isEmpty() && inputMaterials.isEmpty()) {
            plugin.getLogger().info("[ArtifactDebug] FAIL: No ingredients!");
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE).setName("&c&lKhông Có Vật Liệu").setLore("", "&7Hãy đặt vật liệu vào ô bên trái!").build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // Tìm công thức phù hợp từ YML
        ArtifactRecipe matchedRecipe = null;
        List<ArtifactRecipe> recipes = getRecipes();
        for (ArtifactRecipe recipe : recipes) {
            if (matchesRecipe(recipe, inputHerbs, inputMaterials)) {
                matchedRecipe = recipe;
                break;
            }
        }

        if (matchedRecipe == null) {
            plugin.getLogger().info("[ArtifactDebug] FAIL: No matching recipe!");
        } else {
            plugin.getLogger().info("[ArtifactDebug] Matched recipe: " + matchedRecipe.getId());
        }

        if (matchedRecipe == null) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE).setName("&c&lKhông Có Công Thức Phù Hợp").setLore("", "&7Vật liệu không khớp!").build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        if (data != null && data.getLevel() < matchedRecipe.getRequiredLevel()) {
            plugin.getLogger().info("[ArtifactDebug] FAIL: Level too low. Required=" + matchedRecipe.getRequiredLevel() + " Current=" + data.getLevel());
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lKhông Đủ Tu Vi")
                    .setLore("", "&7Yêu cầu: &cLevel " + matchedRecipe.getRequiredLevel(), "&7Hiện tại: &eLevel " + data.getLevel()).build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        int artifactGrade = calculateArtifactGrade(data, skillData);
        long adjustedTime = matchedRecipe.getCookingTime();
        plugin.getLogger().info("[ArtifactDebug] Crafting: grade=" + artifactGrade + " time=" + adjustedTime + "s");
        String gradeDisplay = gradeDisplayName(artifactGrade);
        String profName = (skillData != null) ? ColorUtils.colorize(skillData.getForgeMasteryProficiencyName()) : "&7N/A";
        int profBonus = (skillData != null) ? skillData.getForgeGradeBonus() : 0;

        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.FURNACE).setName("&6&lĐang Luyện Khí...")
                .setLore("", "&7Pháp bảo: &f&l" + matchedRecipe.getDisplayName(),
                        gradeDisplay + " &7dự kiến",
                        "&7Luyện Khí Thuật: " + profName + " (+" + profBonus + " cấp)",
                        "&7Thời gian: &e" + adjustedTime + " giây",
                        "&7Tỷ lệ: &a" + matchedRecipe.getSuccessChance() + "%").build());
        MessageUtils.send(player, "&6✦ Đang bắt đầu luyện khí &f&l" + matchedRecipe.getDisplayName() + "&r&6...");
        player.updateInventory();

        // Tiêu hao nguyên liệu
        consumeIngredients(gui, matchedRecipe, inputHerbs, inputMaterials, player);

        player.updateInventory();
        session.isCrafting = true;
        MessageUtils.playSound(player, Sound.BLOCK_ANVIL_USE);

        final UUID playerUUID = player.getUniqueId();
        final ArtifactRecipe finalRecipe = matchedRecipe;
        final PlayerCultivationData finalData = data;
        final PlayerSkillData finalSkillData = skillData;
        final Inventory finalGui = gui;
        final int finalGradeIndex = artifactGrade;
        final String finalGradeDisplay = gradeDisplay;

        BossBar bossBar = Bukkit.createBossBar(
                ColorUtils.colorize("&6🔥 Đang luyện khí " + matchedRecipe.getDisplayName() + "..."),
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
                if (p == null || !p.isOnline()) { bossBar.removeAll(); session.isCrafting = false; cancel(); return; }
                Inventory inv = p.getOpenInventory().getTopInventory();
                if (!inv.equals(finalGui)) { bossBar.removeAll(); session.isCrafting = false; cancel(); return; }

                currentStep++;
                double progress = Math.min(1.0, (double) currentStep / totalSteps);
                bossBar.setProgress(progress);
                int percent = (int) (progress * 100);
                bossBar.setTitle(String.format("§6🔥 [%d%%] Đang luyện khí %s...", percent, finalRecipe.getDisplayName()));

                if (currentStep % 10 == 0) {
                    plugin.getLogger().info("[ArtifactDebug] Progress: " + percent + "% for " + p.getName());
                }

                String progressText = "§6§l" + "█".repeat(Math.max(0, percent / 5)) + "§7" + "░".repeat(Math.max(0, 20 - percent / 5));
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.FURNACE).setName("&6&lĐang Luyện Khí... " + percent + "%")
                        .setLore("", "&7Pháp bảo: &f&l" + finalRecipe.getDisplayName(),
                                finalGradeDisplay + " &7dự kiến",
                                "", progressText,
                                "&7Thời gian còn lại: &e" + Math.max(0, (int)((totalTicks - currentStep * intervalTicks) / 20)) + "s").build());
                p.updateInventory();

                if (currentStep >= totalSteps) {
                    plugin.getLogger().info("[ArtifactDebug] Craft completed for " + p.getName() + " time=" + (System.currentTimeMillis() - startTime) + "ms");
                    cancel(); bossBar.removeAll(); session.isCrafting = false;
                    finishCraft(p, finalGui, finalRecipe, finalData, finalSkillData, finalGradeIndex, finalGradeDisplay);
                }
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    /**
     * Tiêu hao nguyên liệu từ input slots
     */
    private void consumeIngredients(Inventory gui, ArtifactRecipe recipe,
                                     Map<String, Integer> inputHerbs,
                                     Map<Material, Integer> inputMaterials,
                                     Player player) {
        // Gom yêu cầu
        Map<String, Integer> remainingHerbs = new HashMap<>();
        Map<Material, Integer> remainingMats = new HashMap<>();
        for (ArtifactRecipe.IngredientDef ing : recipe.getIngredients()) {
            if (ing.isHerb()) {
                remainingHerbs.put(ing.getHerbId(), remainingHerbs.getOrDefault(ing.getHerbId(), 0) + ing.getCount());
            } else {
                remainingMats.put(ing.getMaterial(), remainingMats.getOrDefault(ing.getMaterial(), 0) + ing.getCount());
            }
        }

        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
            ItemStack item = gui.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;

            String herbId = ItemBuilder.getPersistentData(item, "vnmine_herb");
            if (herbId != null) {
                Integer needed = remainingHerbs.get(herbId);
                if (needed != null && needed > 0) {
                    int consume = Math.min(needed, item.getAmount());
                    int surplus = item.getAmount() - consume;
                    if (surplus > 0) {
                        ItemStack returnItem = item.clone();
                        returnItem.setAmount(surplus);
                        player.getInventory().addItem(returnItem);
                    }
                    remainingHerbs.put(herbId, needed - consume);
                    gui.setItem(slot, null);
                } else {
                    // Trả lại nguyên liệu thừa
                    player.getInventory().addItem(item);
                    gui.setItem(slot, null);
                }
            } else {
                Integer needed = remainingMats.get(item.getType());
                if (needed != null && needed > 0) {
                    int consume = Math.min(needed, item.getAmount());
                    int surplus = item.getAmount() - consume;
                    if (surplus > 0) {
                        ItemStack returnItem = item.clone();
                        returnItem.setAmount(surplus);
                        player.getInventory().addItem(returnItem);
                    }
                    remainingMats.put(item.getType(), needed - consume);
                    gui.setItem(slot, null);
                } else {
                    player.getInventory().addItem(item);
                    gui.setItem(slot, null);
                }
            }
        }
    }

    private void finishCraft(Player p, Inventory inv, ArtifactRecipe recipe, PlayerCultivationData data,
                             PlayerSkillData skillData, int gradeIndex, String gradeDisplay) {
        if (p == null || !p.isOnline()) {
            plugin.getLogger().info("[ArtifactDebug] finishCraft: Player offline, skipping.");
            return;
        }
        if (!inv.equals(p.getOpenInventory().getTopInventory())) {
            plugin.getLogger().info("[ArtifactDebug] finishCraft: Inventory mismatch, skipping.");
            return;
        }
        plugin.getLogger().info("[ArtifactDebug] finishCraft for " + p.getName() + " recipe=" + recipe.getId() + " grade=" + gradeIndex);

        double chance = recipe.getSuccessChance();
        int levelBonus = (data != null) ? data.getLevel() : 0;
        chance += levelBonus * 1.0;
        chance = Math.min(chance, 90.0);
        boolean success = new Random().nextDouble() * 100 < chance;
        plugin.getLogger().info("[ArtifactDebug] Success chance=" + chance + " result=" + (success ? "SUCCESS" : "FAIL"));

        if (success) {
            double bonus = ARTIFACT_BONUS[gradeIndex];
            int manaReduction = (int)(bonus * 100);

            // Lấy recipe index để ánh xạ material theo grade
            int recipeIndex = getRecipeIndex(recipe.getId());
            Material finalMaterial = recipe.getResultMaterial();
            if (recipeIndex >= 0 && recipeIndex < ARTIFACT_MATERIALS.length
                    && gradeIndex < ARTIFACT_MATERIALS[recipeIndex].length) {
                finalMaterial = ARTIFACT_MATERIALS[recipeIndex][gradeIndex];
            }

            // Use baseName + grade format - crafted by grade-based Material
            String craftedName = gradeDisplayName(gradeIndex) + " &f&l" + recipe.getDisplayName();

            ItemStack result = new ItemBuilder(finalMaterial)
                    .setName(craftedName)
                    .setGlow(true)
                    .setLore("", recipe.getLore(), "",
                            gradeDisplay,
                            "&6✦ Pháp bảo luyện khí thành công ✦",
                            (manaReduction > 0 ? "&bGiảm tiêu hao LL: " + manaReduction + "%" : "&7Pháp bảo cơ bản"),
                            "&7Luyện khí bởi: &e" + p.getName())
                    .build();

            inv.setItem(SLOT_RESULT, result);
            inv.setItem(SLOT_STATUS, new ItemBuilder(Material.EMERALD).setName("&a&l✦ Luyện Khí Thành Công ✦")
                    .setLore("", "&7Pháp bảo: &f&l" + recipe.getDisplayName(), gradeDisplay).build());
            p.updateInventory();

            MessageUtils.send(p, "&6✦ Luyện khí thành công! Nhận " + craftedName);
            MessageUtils.playSound(p, Sound.BLOCK_ANVIL_USE);

            if (skillData != null) {
                skillData.incrementSkillUsage("FORGE_MASTERY");
                int usageCount = skillData.getSkillUsageCount("FORGE_MASTERY");
                PlayerSkillData.ProficiencyLevel newProf = skillData.getProficiencyLevel("FORGE_MASTERY");
                MessageUtils.send(p, "&6✦ Luyện Khí Thuật: &e" + usageCount + " lần → " + ColorUtils.colorize(newProf.getDisplayName()));
            }
            if (data != null) {
                double expReward = recipe.getCookingTime() * 3;
                plugin.getCultivationManager().addExperience(p, expReward);
                MessageUtils.send(p, "&6✦ Nhận &e" + (int)expReward + " &6tu vi!");
            }
        } else {
            inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lLuyện Khí Thất Bại").setLore("", "&7Mất hết vật liệu!").build());
            p.updateInventory();
            MessageUtils.send(p, "&cLuyện khí thất bại! Mất hết vật liệu.");
            MessageUtils.playSound(p, Sound.ENTITY_ITEM_BREAK);
        }
    }

    /**
     * Kiểm tra nguyên liệu có khớp với công thức không
     * Kiểm tra cả herbId (vnmine_herb) và Material
     */
    private boolean matchesRecipe(ArtifactRecipe recipe,
                                   Map<String, Integer> inputHerbs,
                                   Map<Material, Integer> inputMaterials) {
        // Gom yêu cầu từ recipe
        Map<String, Integer> requiredHerbs = new HashMap<>();
        Map<Material, Integer> requiredMats = new HashMap<>();
        for (ArtifactRecipe.IngredientDef ing : recipe.getIngredients()) {
            if (ing.isHerb()) {
                requiredHerbs.put(ing.getHerbId(), requiredHerbs.getOrDefault(ing.getHerbId(), 0) + ing.getCount());
            } else {
                requiredMats.put(ing.getMaterial(), requiredMats.getOrDefault(ing.getMaterial(), 0) + ing.getCount());
            }
        }

        // Kiểm tra số lượng loại nguyên liệu
        if (requiredHerbs.size() != inputHerbs.size() || requiredMats.size() != inputMaterials.size()) {
            return false;
        }

        // Kiểm tra herbs
        for (Map.Entry<String, Integer> req : requiredHerbs.entrySet()) {
            Integer count = inputHerbs.get(req.getKey());
            if (count == null || count < req.getValue()) return false;
        }

        // Kiểm tra materials
        for (Map.Entry<Material, Integer> req : requiredMats.entrySet()) {
            Integer count = inputMaterials.get(req.getKey());
            if (count == null || count < req.getValue()) return false;
        }

        return true;
    }

    private void showRecipe(Player player) {
        MessageUtils.send(player, "&6&l══════ Công Thức Luyện Khí ══════");
        List<ArtifactRecipe> recipes = getRecipes();
        for (ArtifactRecipe recipe : recipes) {
            StringBuilder sb = new StringBuilder();
            sb.append("&b◆ ").append(recipe.getDisplayName()).append(" ◆: &7");
            List<String> parts = new ArrayList<>();
            for (ArtifactRecipe.IngredientDef ing : recipe.getIngredients()) {
                if (ing.isHerb()) {
                    parts.add(ing.getHerbId() + " x" + ing.getCount());
                } else {
                    parts.add(formatMaterialName(ing.getMaterial().name()) + " x" + ing.getCount());
                }
            }
            sb.append(String.join(" + ", parts));
            MessageUtils.send(player, ColorUtils.colorize(sb.toString()));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Chế Pháp Bảo")) return;
        Player player = (Player) event.getPlayer();
        ArtifactSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        if (session.activeBossBar != null) session.activeBossBar.removeAll();
        Inventory gui = event.getInventory();
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6, SLOT_RESULT}) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) player.getInventory().addItem(item);
        }
        activeSessions.remove(player.getUniqueId());
    }

    /**
     * Format Material name sang tên tiếng Việt dễ đọc
     */
    private String formatMaterialName(String materialName) {
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

    private static class ArtifactSession {
        final UUID playerUUID; boolean isCrafting; BossBar activeBossBar;
        ArtifactSession(UUID playerUUID) { this.playerUUID = playerUUID; this.isCrafting = false; this.activeBossBar = null; }
    }
}