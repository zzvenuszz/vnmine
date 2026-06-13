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

    private static final String[] GRADE_NAMES = {
        "Hoang cap Ha pham", "Hoang cap Trung pham", "Hoang cap Thuong pham",
        "Huyen cap Ha pham", "Huyen cap Trung pham", "Huyen cap Thuong pham",
        "Dia cap Ha pham", "Dia cap Trung pham", "Dia cap Thuong pham",
        "Thien cap Ha pham", "Thien cap Trung pham", "Thien cap Thuong pham"
    };
    private static final String[] GRADE_DISPLAY = {
        "&7&oHoang cap &fHa pham", "&7&oHoang cap &eTrung pham", "&7&oHoang cap &aThuong pham",
        "&b&oHuyen cap &fHa pham", "&b&oHuyen cap &eTrung pham", "&b&oHuyen cap &aThuong pham",
        "&5&oDia cap &fHa pham", "&5&oDia cap &eTrung pham", "&5&oDia cap &aThuong pham",
        "&6&oThien cap &fHa pham", "&6&oThien cap &eTrung pham", "&6&oThien cap &aThuong pham"
    };
    private static final double[] GRADE_MULTIPLIERS = {
        1.0, 1.3, 1.6, 2.0, 2.5, 3.0, 3.5, 4.0, 5.0, 6.0, 7.5, 10.0
    };
    private static final int[] GRADE_YIELDS = { 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 5, 5 };

    private static final List<AlchemyRecipe> RECIPES = new ArrayList<>();

    static {
        RECIPES.add(new AlchemyRecipe("HOI_LINH_DAN", "&aHoi Linh Dan", Material.GLOWSTONE_DUST,
                "&7Hoi phuc &b30 &7linh luc", new LinkedHashMap<Material, Integer>() {{ put(Material.GREEN_DYE, 3); put(Material.POTION, 1); }}, 3, 10, 80.0, 2));
        RECIPES.add(new AlchemyRecipe("DAI_HOI_LINH_DAN", "&bDai Hoi Linh Dan", Material.GLOWSTONE,
                "&7Hoi phuc &b100 &7linh luc", new LinkedHashMap<Material, Integer>() {{ put(Material.GLOWSTONE_DUST, 2); put(Material.RED_DYE, 2); put(Material.GREEN_DYE, 5); }}, 10, 30, 60.0, 1));
        RECIPES.add(new AlchemyRecipe("CUONG_THE_DAN", "&cCuong The Dan", Material.REDSTONE_BLOCK,
                "&7Tang &c20% sat thuong &7trong 60s", new LinkedHashMap<Material, Integer>() {{ put(Material.RED_DYE, 3); put(Material.GREEN_DYE, 5); put(Material.BLAZE_POWDER, 1); }}, 15, 20, 55.0, 1));
        RECIPES.add(new AlchemyRecipe("THANH_TAM_DAN", "&aThanh Tam Dan", Material.SUGAR,
                "&7Giai tru moi trang thai xau", new LinkedHashMap<Material, Integer>() {{ put(Material.GREEN_DYE, 5); put(Material.POTION, 1); }}, 5, 15, 85.0, 2));
        RECIPES.add(new AlchemyRecipe("TOC_THANH_DAN", "&bToc Thanh Dan", Material.FEATHER,
                "&7Tang &b50% toc do &7trong 30s", new LinkedHashMap<Material, Integer>() {{ put(Material.GREEN_DYE, 3); put(Material.SUGAR, 2); put(Material.FEATHER, 1); }}, 8, 15, 70.0, 2));
        RECIPES.add(new AlchemyRecipe("TU_LUYEN_DAN", "&5Tu Luyen Dan", Material.PURPLE_DYE,
                "&7Tang &5+50 EXP &7khi su dung", new LinkedHashMap<Material, Integer>() {{ put(Material.GREEN_DYE, 10); put(Material.RED_DYE, 5); put(Material.ORANGE_DYE, 2); put(Material.GOLD_INGOT, 1); }}, 20, 45, 40.0, 1));
        RECIPES.add(new AlchemyRecipe("PHI_THANG_DAN", "&6&lPhi Thang Dan", Material.NETHER_STAR,
                "&7+500 EXP", new LinkedHashMap<Material, Integer>() {{ put(Material.PURPLE_DYE, 3); put(Material.ORANGE_DYE, 10); put(Material.DRAGON_BREATH, 1); put(Material.NETHERITE_INGOT, 2); }}, 50, 120, 15.0, 1));
        RECIPES.add(new AlchemyRecipe("BACH_DOC_DAN", "&9Bach Doc Dan", Material.CYAN_DYE,
                "&7Mien doc 5 phut", new LinkedHashMap<Material, Integer>() {{ put(Material.LIGHT_BLUE_DYE, 3); put(Material.REDSTONE, 2); put(Material.BLAZE_POWDER, 1); }}, 25, 25, 50.0, 1));
        RECIPES.add(new AlchemyRecipe("THIEN_HOI_DAN", "&6Thien Hoi Dan", Material.GOLDEN_APPLE,
                "&7Hoi 50% HP + 50% Mana", new LinkedHashMap<Material, Integer>() {{ put(Material.CYAN_DYE, 5); put(Material.MAGENTA_DYE, 3); put(Material.PRISMARINE_SHARD, 1); }}, 35, 40, 45.0, 1));
        RECIPES.add(new AlchemyRecipe("PHE_MA_DAN", "&8Phe Ma Dan", Material.REDSTONE,
                "&7Tang &c30% sat thuong &7vs quai 2 phut", new LinkedHashMap<Material, Integer>() {{ put(Material.YELLOW_DYE, 3); put(Material.IRON_INGOT, 2); put(Material.END_STONE, 1); }}, 40, 30, 40.0, 1));
        RECIPES.add(new AlchemyRecipe("TRUONG_THO_DAN", "&6Truong Tho Dan", Material.NETHER_STAR,
                "&7Hoi sinh 1 lan (CD 1h)", new LinkedHashMap<Material, Integer>() {{ put(Material.PURPLE_DYE, 5); put(Material.IRON_NUGGET, 3); put(Material.EMERALD, 2); }}, 45, 60, 25.0, 1));
    }

    private static final Map<UUID, AlchemySession> activeSessions = new HashMap<>();

    public AlchemyCraftGUI(VNMinePlugin plugin, MainMenuGUI mainMenu) {
        this.plugin = plugin;
        this.mainMenu = mainMenu;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ColorUtils.colorize("&8✦ Luyen Dan - Khong Hoa Thuat ✦"));
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&r").build();
        for (int slot : new int[]{19, 20, 21, 28, 29, 30}) gui.setItem(slot, null);
        gui.setItem(SLOT_CRAFT, new ItemBuilder(Material.FIRE_CHARGE).setGlow(true).setName("&c&l🔥 Luyen Dan")
                .setLore("", "&7Dat nguyen lieu vao o ben trai", "&7Bam nut nay de luyen dan", "", "&cYeu cau: Ky nang Khong Hoa Thuat", "&7Cap 21+ (Truc Co)").build());
        gui.setItem(SLOT_RESULT, null);
        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.PAPER).setName("&e&lTrang Thai").setLore("", "&7San sang luyen dan!").build());
        gui.setItem(SLOT_GUIDE, new ItemBuilder(Material.KNOWLEDGE_BOOK).setName("&6&lCong Thuc Luyen Dan")
                .setLore("", "&aHoi Linh Dan: &73 Linh Thao + 1 Nuoc", "&bDai Hoi Linh Dan: &72 HLD + 2 HLT + 5 LT",
                        "&cCuong The Dan: &73 HLT + 5 LT + 1 Blaze", "&aThanh Tam Dan: &75 LT + 1 Nuoc",
                        "&bToc Thanh Dan: &73 LT + 2 Duong + 1 Long", "&5Tu Luyen Dan: &710 LT + 5 HLT + 2 LHT + 1 Vang",
                        "&6Phi Thang Dan: &73 TLD + 10 LHT + 1 HR + 2 Netherite", "",
                        "&9Bach Doc Dan: &73 Binh LT + 2 Huyet Thach + 1 Tinh Hoa",
                        "&6Thien Hoi Dan: &75 Thien LT + 3 Hoa LT + 1 Long Lan",
                        "&8Phe Ma Dan: &73 Loi LT + 2 Huyen Kim + 1 Thien Thach",
                        "&6Truong Tho Dan: &75 VNLCT + 3 Ngan Sa + 2 Ngoc").build());
        for (int i = 0; i < 54; i++) if (gui.getItem(i) == null && !isInputSlot(i) && i != SLOT_RESULT) gui.setItem(i, border);
        gui.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW).setName("&e&l← Quay Lai").build());
        activeSessions.put(player.getUniqueId(), new AlchemySession(player.getUniqueId()));
        player.openInventory(gui);
    }

    private boolean isInputSlot(int slot) {
        return slot == SLOT_INPUT_1 || slot == SLOT_INPUT_2 || slot == SLOT_INPUT_3 || slot == SLOT_INPUT_4 || slot == SLOT_INPUT_5 || slot == SLOT_INPUT_6;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyen Dan")) return;
        for (Integer slot : event.getRawSlots()) { if (slot < 54 && !isInputSlot(slot)) { event.setCancelled(true); return; } }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyen Dan")) return;
        int slot = event.getRawSlot();
        if (slot >= 54) return;
        if (slot < 0) { event.setCancelled(true); return; }
        if (isInputSlot(slot) || slot == SLOT_RESULT) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        AlchemySession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        ClickType clickType = event.getClick();
        if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) {
            switch (slot) {
                case SLOT_CRAFT: attemptCraft(player, session); break;
                case SLOT_BACK: mainMenu.openMainMenu(player); break;
                case SLOT_GUIDE:
                    MessageUtils.send(player, "&6&lCong Thuc Luyen Dan:");
                    MessageUtils.send(player, "&aHoi Linh Dan: &73 LT + 1 Nuoc → Hoi 30 mana");
                    MessageUtils.send(player, "&bDai Hoi Linh Dan: &72 HLD + 2 HLT + 5 LT → Hoi 100 mana");
                    MessageUtils.send(player, "&cCuong The Dan: &73 HLT + 5 LT + 1 Blaze → +20% DMG 60s");
                    MessageUtils.send(player, "&9Bach Doc Dan: &73 Binh LT + 2 Huyet Thach + 1 Tinh Hoa → Mien doc 5p");
                    MessageUtils.send(player, "&6Thien Hoi Dan: &75 Thien LT + 3 Hoa LT + 1 Long Lan → Hoi 50% HP+Mana");
                    MessageUtils.send(player, "&8Phe Ma Dan: &73 Loi LT + 2 Huyen Kim + 1 Thien Thach → +30% DMG quai");
                    MessageUtils.send(player, "&6Truong Tho Dan: &75 VNLCT + 3 Ngan Sa + 2 Ngoc → Hoi sinh 1 lan");
                    break;
            }
        }
    }

    private int calculatePillGrade(PlayerCultivationData playerData, PlayerSkillData skillData) {
        int bonus = 0;
        if (skillData != null) bonus += skillData.getAlchemyGradeBonus();
        if (playerData != null) {
            int level = playerData.getLevel();
            if (level >= 71) bonus += 3; else if (level >= 51) bonus += 2; else if (level >= 31) bonus += 1;
        }
        return Math.max(0, Math.min(bonus, 11));
    }

    private long calculateCookingTime(int baseTime, PlayerSkillData skillData) {
        double timeReduction = (skillData != null) ? skillData.getAlchemyTimeReduction() : 0.0;
        return Math.max(1, (long)(baseTime * (1.0 - timeReduction)));
    }

    private int getGradeYield(int gradeIndex) {
        if (gradeIndex < 0 || gradeIndex >= GRADE_YIELDS.length) return 1;
        return GRADE_YIELDS[gradeIndex];
    }

    private double getGradeMultiplier(int gradeIndex) {
        if (gradeIndex < 0 || gradeIndex >= GRADE_MULTIPLIERS.length) return 1.0;
        return GRADE_MULTIPLIERS[gradeIndex];
    }

    private void attemptCraft(Player player, AlchemySession session) {
        Inventory gui = player.getOpenInventory().getTopInventory();
        PlayerCultivationData playerData = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());

        // KIEM TRA SKILL KHONG HOA THUAT
        if (playerData == null || !playerData.hasLearnedSkill("FIRE_CONTROL")) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.BARRIER)
                    .setName("&c&lChua Hoc Khong Hoa Thuat!")
                    .setLore("", "&7Ban can hoc ky nang &cKhong Hoa Thuat",
                            "&7de co the luyen dan.", "", "&eYeu cau: Cap 21+ (Truc Co)")
                    .build());
            MessageUtils.send(player, "&c✦ Ban chua hoc Khong Hoa Thuat! Can co ky nang nay de luyen dan!");
            MessageUtils.send(player, "&7Hoc tai menu Cong Phap hoac dung sach cong phap.");
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // THU THAP NGUYEN LIEU
        Map<Material, Integer> ingredients = new HashMap<>();
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                ingredients.put(item.getType(), ingredients.getOrDefault(item.getType(), 0) + item.getAmount());
            }
        }

        if (ingredients.isEmpty()) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE).setName("&c&lKhong Co Nguyen Lieu").setLore("", "&7Hay dat nguyen lieu vao o ben trai!").build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // TIM CONG THUC PHU HOP
        AlchemyRecipe matchedRecipe = null;
        for (AlchemyRecipe recipe : RECIPES) {
            if (matchesRecipe(recipe, ingredients)) { matchedRecipe = recipe; break; }
        }

        if (matchedRecipe == null) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE).setName("&c&lKhong Co Cong Thuc Phu Hop").setLore("", "&7Nguyen lieu khong khop voi bat ky cong thuc nao!").build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // KIEM TRA LEVEL
        if (playerData != null && playerData.getLevel() < matchedRecipe.requiredLevel) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lKhong Du Tu Vi")
                    .setLore("", "&7Yeu cau: &cCap " + matchedRecipe.requiredLevel, "&7Hien tai: &eCap " + playerData.getLevel()).build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // TINH PHAM CAP DUST DUST
        int pillGradeIndex = calculatePillGrade(playerData, skillData);
        String pillGradeDisplay = GRADE_DISPLAY[pillGradeIndex];

        // TINH THOI GIAN LUYEN DAN
        long adjustedTime = calculateCookingTime(matchedRecipe.cookingTime, skillData);

        // TINH proficiency info
        String profName = (skillData != null) ? ColorUtils.colorize(skillData.getFireControlProficiencyName()) : "&7N/A";
        int profBonus = (skillData != null) ? skillData.getAlchemyGradeBonus() : 0;
        double timeReduction = (skillData != null) ? skillData.getAlchemyTimeReduction() : 0.0;

        // HIEN THI THONG TIN TRUOC KHI LUYEN
        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.FURNACE).setName("&6&lDang Luyen Dan...")
                .setLore("", "&7Dan duoc: " + matchedRecipe.displayName,
                        "&7Pham cap du kien: " + pillGradeDisplay,
                        "&7Khong Hoa Thuat: " + profName + " &7(+" + profBonus + " pham)",
                        "&7Giam thoi gian: &c" + (int)(timeReduction * 100) + "%",
                        "&7Thoi gian: &e" + adjustedTime + " giay",
                        "&7Ti le: &a" + matchedRecipe.successChance + "%").build());

        // CHI TIEU HOA DUNG SO NGUYEN LIEU CAN, TRA SURPLUS
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

        MessageUtils.playSound(player, Sound.BLOCK_FIRE_AMBIENT);

        final AlchemyRecipe finalRecipe = matchedRecipe;
        final PlayerCultivationData finalData = playerData;
        final PlayerSkillData finalSkillData = skillData;
        final Inventory finalGui = gui;
        final UUID playerUUID = player.getUniqueId();
        final int finalGradeIndex = pillGradeIndex;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = Bukkit.getPlayer(playerUUID);
            if (p == null || !p.isOnline()) return;
            Inventory inv = p.getOpenInventory().getTopInventory();
            if (!inv.equals(finalGui)) return;

            // CHIEM NGUOI CHOI PHAI GIU NGUYEN UI
            // (da kiem tra inv.equals tren)

            double chance = finalRecipe.successChance;
            int levelBonus = (finalData != null) ? finalData.getLevel() : 0;
            chance += levelBonus * 1.5;
            chance = Math.min(chance, 95.0);

            Random random = new Random();
            boolean success = random.nextDouble() * 100 < chance;

            if (success) {
                int baseYield = getGradeYield(finalGradeIndex);
                double gradeMultiplier = getGradeMultiplier(finalGradeIndex);

                ItemStack result = new ItemBuilder(finalRecipe.resultMaterial)
                        .setName(finalRecipe.displayName)
                        .setAmount(baseYield)
                        .setGlow(true)
                        .setLore("", finalRecipe.lore, "",
                                GRADE_DISPLAY[finalGradeIndex],
                                "&a✦ Luyen dan thanh cong! x" + baseYield,
                                "&7Tang hieu qua: &6x" + String.format("%.1f", gradeMultiplier))
                        .build();

                inv.setItem(SLOT_RESULT, result);
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.EMERALD).setName("&a&l✦ Luyen Dan Thanh Cong ✦")
                        .setLore("", "&7Dan duoc: " + finalRecipe.displayName,
                                "&7Pham cap: " + GRADE_DISPLAY[finalGradeIndex],
                                "&7So luong: &e" + baseYield,
                                "&7Ti le: &a" + String.format("%.1f", chance) + "%").build());

                // TANG PROFICIENCY KHONG HOA THUAT
                if (finalSkillData != null) {
                    finalSkillData.incrementSkillUsage("FIRE_CONTROL");
                    int usageCount = finalSkillData.getSkillUsageCount("FIRE_CONTROL");
                    PlayerSkillData.ProficiencyLevel newProf = finalSkillData.getProficiencyLevel("FIRE_CONTROL");
                    MessageUtils.send(p, "&c✦ Khong Hoa Thuat: &e" + usageCount + " lan su dung → " + ColorUtils.colorize(newProf.getDisplayName()));
                }

                if (finalData != null) {
                    double expReward = finalRecipe.cookingTime * 2;
                    plugin.getCultivationManager().addExperience(p, expReward);
                    finalData.incrementPillsCrafted();
                    MessageUtils.send(p, "&a✦ Luyen dan thanh cong! Nhan &e" + (int)expReward + " &atu vi!");
                }
                MessageUtils.playSound(p, Sound.BLOCK_BREWING_STAND_BREW);
            } else {
                double failRand = random.nextDouble() * 100;
                if (failRand < 60) {
                    inv.setItem(SLOT_RESULT, new ItemBuilder(Material.GUNPOWDER).setName("&7Phe Lieu Luyen Dan").setLore("", "&7Co the dung lam phan bon").setAmount(1).build());
                    inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lLuyen Dan That Bai").setLore("", "&7Thu duoc phe lieu.").build());
                    MessageUtils.send(p, "&eLuyen dan that bai! Thu duoc phe lieu.");
                } else {
                    inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lLuyen Dan That Bai").setLore("", "&7Mat het nguyen lieu!").build());
                    MessageUtils.send(p, "&cLuyen dan that bai! Mat het nguyen lieu!");
                }
                MessageUtils.playSound(p, Sound.ENTITY_ITEM_BREAK);
            }
        }, adjustedTime * 20L);
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
        Player player = (Player) event.getPlayer();
        AlchemySession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        Inventory gui = event.getInventory();
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
        AlchemySession(UUID playerUUID) { this.playerUUID = playerUUID; this.isCrafting = false; }
    }
}