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
    private static final double[] ARTIFACT_BONUS = {
        0.0, 0.1, 0.2, 0.35, 0.5, 0.65, 0.8, 1.0, 1.2, 1.5, 1.8, 2.2
    };

    private static final List<ArtifactRecipe> RECIPES = new ArrayList<>();

    static {
        RECIPES.add(new ArtifactRecipe("FLYING_SWORD", "&b&lKiem Phi Hanh", Material.DIAMOND_SWORD,
                "&7Click phai de ngu kien phi hanh, tieu hao linh luc",
                new LinkedHashMap<Material, Integer>() {{ put(Material.DIAMOND_SWORD, 1); put(Material.DIAMOND, 8); put(Material.FEATHER, 4); }},
                15, 30, 50.0, "FORGE_MASTERY", 3));
        RECIPES.add(new ArtifactRecipe("SPIRIT_BELL", "&6&lLinh Chung", Material.BELL,
                "&7Lam choang quai trong ban kinh, tieu hao linh luc",
                new LinkedHashMap<Material, Integer>() {{ put(Material.BELL, 1); put(Material.GOLD_INGOT, 4); put(Material.DIAMOND, 2); }},
                10, 20, 60.0, "FORGE_MASTERY", 2));
        RECIPES.add(new ArtifactRecipe("BAGUA_MIRROR", "&5&lBat Quai Kinh", Material.SHIELD,
                "&7Cam tren tay: Giam 30% sat thuong nhan vao",
                new LinkedHashMap<Material, Integer>() {{ put(Material.SHIELD, 1); put(Material.OBSIDIAN, 4); put(Material.EMERALD, 4); }},
                20, 40, 45.0, "FORGE_MASTERY", 4));
        RECIPES.add(new ArtifactRecipe("SOUL_JADE", "&a&lHon Ngoc", Material.EMERALD,
                "&7Tu dong: Hoi 50% mau khi HP<20%, CD 5 phut",
                new LinkedHashMap<Material, Integer>() {{ put(Material.EMERALD, 1); put(Material.GOLD_INGOT, 4); put(Material.ENDER_PEARL, 2); }},
                25, 45, 40.0, "FORGE_MASTERY", 5));
        RECIPES.add(new ArtifactRecipe("HEAVEN_SHIELD", "&4&lThien Linh Thuong", Material.NETHERITE_CHESTPLATE,
                "&7Kich hoat: Bat tu 5 giay, CD 3 phut",
                new LinkedHashMap<Material, Integer>() {{ put(Material.NETHERITE_CHESTPLATE, 1); put(Material.ENDER_EYE, 8); }},
                40, 60, 25.0, "FORGE_MASTERY", 6));
        RECIPES.add(new ArtifactRecipe("THUNDER_SEAL", "&e&lLoi An", Material.TRIDENT,
                "&7Click vao quai: Goi set danh, tieu hao linh luc",
                new LinkedHashMap<Material, Integer>() {{ put(Material.TRIDENT, 1); put(Material.DIAMOND, 4); put(Material.DRAGON_BREATH, 2); }},
                30, 35, 35.0, "FORGE_MASTERY", 5));
        RECIPES.add(new ArtifactRecipe("PHOENIX_REBIRTH", "&6&lPhuong Hoang Lenh", Material.FEATHER,
                "&7Tu dong: Hoi sinh 1 lan sau khi chet, CD 1 ngay",
                new LinkedHashMap<Material, Integer>() {{ put(Material.FEATHER, 1); put(Material.GOLD_BLOCK, 8); put(Material.NETHERITE_INGOT, 4); put(Material.DRAGON_EGG, 1); }},
                60, 120, 10.0, "FORGE_MASTERY", 8));
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

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ColorUtils.colorize("&8✦ Luyen Che Phap Bao ✦"));
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) gui.setItem(slot, null);
        gui.setItem(SLOT_CRAFT, new ItemBuilder(Material.ANVIL).setGlow(true).setName("&6&l⚒ Che Tao Phap Bao")
                .setLore("", "&7Dat vat lieu vao o ben trai", "&7Bam nut nay de luyen che", "", "&cYeu cau: Ky nang Luyen Ki Thuat").build());
        gui.setItem(SLOT_RESULT, null);
        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.PAPER).setName("&e&lTrang Thai").setLore("", "&7San sang che tao!").build());
        gui.setItem(SLOT_GUIDE, new ItemBuilder(Material.KNOWLEDGE_BOOK).setName("&6&lCong Thuc Phap Bao")
                .setLore("", "&bKiem Phi Hanh: &71 Kiem DC + 8 DC + 4 Long",
                        "&6Linh Chung: &71 Chuong + 4 Vang + 2 DC",
                        "&5Bat Quai Kinh: &71 Khien + 4 Obsidian + 4 Ngoc",
                        "&aHon Ngoc: &71 Ngoc + 4 Vang + 2 Mat End",
                        "&4Thien Linh Thuong: &71 Nguc Netherite + 8 Mat End",
                        "&eLoi An: &71 Dinh ba + 4 DC + 2 Nuoc Rong",
                        "&6Phuong Hoang Lenh: &71 Long + 8 Vang Khoi + 4 Netherite + 1 Trung Rong").build());
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&r").build();
        for (int i = 0; i < 54; i++) if (gui.getItem(i) == null && !isInputSlot(i) && i != SLOT_RESULT) gui.setItem(i, border);
        gui.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW).setName("&e&l← Quay Lai").build());
        activeSessions.put(player.getUniqueId(), new ArtifactSession(player.getUniqueId()));
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyen Che Phap Bao")) return;
        for (Integer slot : event.getRawSlots()) { if (slot < 54 && !isInputSlot(slot)) { event.setCancelled(true); return; } }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyen Che Phap Bao")) return;
        int slot = event.getRawSlot();
        if (slot >= 54) return;
        if (slot < 0) { event.setCancelled(true); return; }
        if (isInputSlot(slot) || slot == SLOT_RESULT) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ArtifactSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        ClickType clickType = event.getClick();
        if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) {
            switch (slot) {
                case SLOT_CRAFT: attemptCraft(player, session); break;
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
            if (level >= 71) bonus += 3; else if (level >= 51) bonus += 2; else if (level >= 31) bonus += 1;
        }
        return Math.max(0, Math.min(bonus, 11));
    }

    private void attemptCraft(Player player, ArtifactSession session) {
        Inventory gui = player.getOpenInventory().getTopInventory();
        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        PlayerSkillData skillData = plugin.getCultivationManager().getPlayerSkillData(player.getUniqueId());

        // KIEM TRA SKILL LUYEN KI THUAT
        if (data == null || !data.hasLearnedSkill("FORGE_MASTERY")) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.BARRIER)
                    .setName("&c&lChua Hoc Luyen Ki Thuat!")
                    .setLore("", "&7Ban can hoc ky nang &6Luyen Ki Thuat",
                            "&7de co the che tao phap bao.", "", "&eYeu cau: Cap 15+")
                    .build());
            MessageUtils.send(player, "&c✦ Ban chua hoc Luyen Ki Thuat! Can co ky nang nay de che tao phap bao!");
            MessageUtils.send(player, "&7Hoc tai menu Cong Phap hoac dung sach cong phap.");
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        Map<Material, Integer> ingredients = new HashMap<>();
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                ingredients.put(item.getType(), ingredients.getOrDefault(item.getType(), 0) + item.getAmount());
            }
        }

        if (ingredients.isEmpty()) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE).setName("&c&lKhong Co Vat Lieu").setLore("", "&7Hay dat vat lieu vao o ben trai!").build());
            return;
        }

        ArtifactRecipe matchedRecipe = null;
        for (ArtifactRecipe recipe : RECIPES) { if (matchesRecipe(recipe, ingredients)) { matchedRecipe = recipe; break; } }

        if (matchedRecipe == null) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE).setName("&c&lKhong Co Cong Thuc Phu Hop").setLore("", "&7Vat lieu khong khop voi bat ky cong thuc nao!").build());
            return;
        }

        if (data != null && data.getLevel() < matchedRecipe.requiredLevel) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lKhong Du Tu Vi")
                    .setLore("", "&7Yeu cau: &cCap " + matchedRecipe.requiredLevel, "&7Hien tai: &eCap " + data.getLevel()).build());
            return;
        }

        // TINH PHAM CAP PHAP BAO
        int artifactGrade = calculateArtifactGrade(data, skillData);
        String gradeDisplay = GRADE_DISPLAY[artifactGrade];

        // Luyen Ki Thuat info
        String profName = (skillData != null) ? ColorUtils.colorize(skillData.getForgeMasteryProficiencyName()) : "&7N/A";
        int profBonus = (skillData != null) ? skillData.getForgeGradeBonus() : 0;

        final ArtifactRecipe finalRecipe = matchedRecipe;
        final PlayerCultivationData finalData = data;
        final PlayerSkillData finalSkillData = skillData;
        final Inventory finalGui = gui;
        final UUID playerUUID = player.getUniqueId();
        final int finalGradeIndex = artifactGrade;

        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.FURNACE).setName("&6&lDang Luyen Che...")
                .setLore("", "&7Phap bao: " + finalRecipe.displayName,
                        "&7Pham cap du kien: " + gradeDisplay,
                        "&7Luyen Ki Thuat: " + profName + " (+" + profBonus + " pham)",
                        "&7Thoi gian: &e" + finalRecipe.craftingTime + " giay",
                        "&7Ti le: &a" + finalRecipe.successChance + "%").build());

        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
            ItemStack item = finalGui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                int needed = finalRecipe.ingredients.getOrDefault(item.getType(), 0);
                if (needed > 0) {
                    int surplus = item.getAmount() - needed;
                    if (surplus > 0) { ItemStack returnItem = item.clone(); returnItem.setAmount(surplus); player.getInventory().addItem(returnItem); }
                    finalGui.setItem(slot, null);
                }
            }
        }

        MessageUtils.playSound(player, Sound.BLOCK_ANVIL_USE);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = Bukkit.getPlayer(playerUUID);
            if (p == null || !p.isOnline()) return;
            Inventory inv = p.getOpenInventory().getTopInventory();
            if (!inv.equals(finalGui)) return;

            double chance = finalRecipe.successChance;
            int levelBonus = (finalData != null) ? finalData.getLevel() : 0;
            chance += levelBonus * 1.0;
            chance = Math.min(chance, 90.0);

            Random random = new Random();
            boolean success = random.nextDouble() * 100 < chance;

            if (success) {
                double bonus = ARTIFACT_BONUS[finalGradeIndex];
                int manaReduction = (int)(bonus * 100);

                ItemStack result = new ItemBuilder(finalRecipe.resultMaterial)
                        .setName(finalRecipe.displayName)
                        .setGlow(true)
                        .setLore("", finalRecipe.lore, "",
                                GRADE_DISPLAY[finalGradeIndex],
                                "&6✦ Phap bao thanh cong ✦",
                                (manaReduction > 0 ? "&bGiam tieu hao linh luc: " + manaReduction + "%" : "&7Phap bao co ban"),
                                "&7Luyen che boi: &e" + p.getName())
                        .build();

                inv.setItem(SLOT_RESULT, result);
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.EMERALD).setName("&a&l✦ Che Tao Thanh Cong ✦")
                        .setLore("", "&7Phap bao: " + finalRecipe.displayName, "&7Pham cap: " + gradeDisplay).build());

                // TANG PROFICIENCY LUYEN KI THUAT
                if (finalSkillData != null) {
                    finalSkillData.incrementSkillUsage("FORGE_MASTERY");
                    int usageCount = finalSkillData.getSkillUsageCount("FORGE_MASTERY");
                    PlayerSkillData.ProficiencyLevel newProf = finalSkillData.getProficiencyLevel("FORGE_MASTERY");
                    MessageUtils.send(p, "&6✦ Luyen Ki Thuat: &e" + usageCount + " lan su dung → " + ColorUtils.colorize(newProf.getDisplayName()));
                }

                if (finalData != null) {
                    double expReward = finalRecipe.craftingTime * 3;
                    plugin.getCultivationManager().addExperience(p, expReward);
                    MessageUtils.send(p, "&6✦ Che tao phap bao thanh cong! Nhan &e" + (int)expReward + " &6tu vi!");
                }
                MessageUtils.playSound(p, Sound.BLOCK_ANVIL_USE);
            } else {
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK).setName("&c&lChe Tao That Bai").setLore("", "&7Mat het vat lieu! Hay thu lai!").build());
                MessageUtils.send(p, "&cChe tao phap bao that bai! Mat het vat lieu.");
                MessageUtils.playSound(p, Sound.ENTITY_ITEM_BREAK);
            }
        }, finalRecipe.craftingTime * 20L);
    }

    private boolean matchesRecipe(ArtifactRecipe recipe, Map<Material, Integer> ingredients) {
        if (recipe.ingredients.size() != ingredients.size()) return false;
        for (Map.Entry<Material, Integer> entry : recipe.ingredients.entrySet()) {
            Integer count = ingredients.get(entry.getKey());
            if (count == null || count < entry.getValue()) return false;
        }
        return true;
    }

    private void showRecipe(Player player) {
        MessageUtils.send(player, "&6&l= Cong Thuc Luyen Che Phap Bao =");
        MessageUtils.send(player, "&bKiem Phi Hanh: &71 Kiem Kim Cuong + 8 Kim Cuong + 4 Long Vu");
        MessageUtils.send(player, "&6Linh Chung: &71 Chuong + 4 Vang + 2 Kim Cuong");
        MessageUtils.send(player, "&5Bat Quai Kinh: &71 Khien + 4 Obsidian + 4 Ngoc Luc Bao");
        MessageUtils.send(player, "&aHon Ngoc: &71 Ngoc Luc Bao + 4 Vang + 2 Mat End");
        MessageUtils.send(player, "&4Thien Linh Thuong: &71 Nguc Netherite + 8 Mat End");
        MessageUtils.send(player, "&eLoi An: &71 Dinh ba + 4 Kim Cuong + 2 Hoi Rong");
        MessageUtils.send(player, "&6Phuong Hoang Lenh: &71 Long + 8 Khoi Vang + 4 Netherite + 1 Trung Rong");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        ArtifactSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        Inventory gui = event.getInventory();
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3, SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6, SLOT_RESULT}) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) player.getInventory().addItem(item);
        }
        activeSessions.remove(player.getUniqueId());
    }

    private static class ArtifactRecipe {
        final String id;
        final String displayName;
        final Material resultMaterial;
        final String lore;
        final Map<Material, Integer> ingredients;
        final int requiredLevel;
        final int craftingTime;
        final double successChance;
        final String requiredSkill;
        final int skillLevel;

        ArtifactRecipe(String id, String displayName, Material resultMaterial, String lore,
                      Map<Material, Integer> ingredients, int requiredLevel, int craftingTime, double successChance,
                      String requiredSkill, int skillLevel) {
            this.id = id; this.displayName = displayName; this.resultMaterial = resultMaterial;
            this.lore = lore; this.ingredients = ingredients; this.requiredLevel = requiredLevel;
            this.craftingTime = craftingTime; this.successChance = successChance;
            this.requiredSkill = requiredSkill; this.skillLevel = skillLevel;
        }
    }

    private static class ArtifactSession {
        final UUID playerUUID;
        ArtifactSession(UUID playerUUID) { this.playerUUID = playerUUID; }
    }
}