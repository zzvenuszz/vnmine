package com.vnmine.gui;

import com.vnmine.VNMinePlugin;
import com.vnmine.cultivation.PlayerCultivationData;
import com.vnmine.item.ItemBuilder;
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

/**
 * ArtifactCraftGUI - Giao diện luyện chế pháp bảo
 * Người chơi đặt vật liệu vào UI, bấm nút để chế tạo
 * Yêu cầu kỹ năng: Luyện Khí Thuật
 */
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

    // Định nghĩa công thức chế tạo pháp bảo
    private static final List<ArtifactRecipe> RECIPES = new ArrayList<>();

    static {
        // Kiếm Phi Hành: 1 Kiếm Kim Cương + 8 Pha Lê + 4 Cánh Phượng
        RECIPES.add(new ArtifactRecipe(
                "FLYING_SWORD",
                "&b&l◆ Kiếm Phi Hành ◆",
                Material.DIAMOND_SWORD,
                "&7Click phải để ngự kiếm phi hành, tiêu hao linh lực",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.DIAMOND_SWORD, 1);
                    put(Material.DIAMOND, 8);
                    put(Material.FEATHER, 4);
                }},
                15, 30, 50.0,
                "Luyện Khí Thuật", 3
        ));

        // Linh Chung: 1 Chuông + 4 Vàng + 2 Pha Lê
        RECIPES.add(new ArtifactRecipe(
                "SPIRIT_BELL",
                "&6&l◆ Linh Chung ◆",
                Material.BELL,
                "&7Click phải: Làm choáng quái trong bán kính, tiêu hao linh lực",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.BELL, 1);
                    put(Material.GOLD_INGOT, 4);
                    put(Material.DIAMOND, 2);
                }},
                10, 20, 60.0,
                "Luyện Khí Thuật", 2
        ));

        // Bát Quái Kính: 1 Khiên + 4 Obsidian + 4 Ngọc Lục Bảo
        RECIPES.add(new ArtifactRecipe(
                "BAGUA_MIRROR",
                "&5&l◆ Bát Quái Kính ◆",
                Material.SHIELD,
                "&7Cầm trên tay: Giảm 30% sát thương nhận vào",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.SHIELD, 1);
                    put(Material.OBSIDIAN, 4);
                    put(Material.EMERALD, 4);
                }},
                20, 40, 45.0,
                "Luyện Khí Thuật", 4
        ));

        // Hồn Ngọc: 1 Ngọc Lục Bảo + 4 Vàng + 2 Mắt End
        RECIPES.add(new ArtifactRecipe(
                "SOUL_JADE",
                "&a&l◆ Hồn Ngọc ◆",
                Material.EMERALD,
                "&7Tự động: Hồi 50% máu khi HP<20%, CD 5 phút",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.EMERALD, 1);
                    put(Material.GOLD_INGOT, 4);
                    put(Material.ENDER_PEARL, 2);
                }},
                25, 45, 40.0,
                "Luyện Khí Thuật", 5
        ));

        // Thiên Linh Thuẫn: 1 Ngực Netherite + 8 Mắt End
        RECIPES.add(new ArtifactRecipe(
                "HEAVEN_SHIELD",
                "&4&l◆ Thiên Linh Thuẫn ◆",
                Material.NETHERITE_CHESTPLATE,
                "&7Kích hoạt: Bất tử 5 giây, CD 3 phút",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.NETHERITE_CHESTPLATE, 1);
                    put(Material.ENDER_EYE, 8);
                }},
                40, 60, 25.0,
                "Luyện Khí Thuật", 6
        ));

        // Lôi Ấn: 1 Đinh ba + 4 Pha Lê + 2 Nước Rồng
        RECIPES.add(new ArtifactRecipe(
                "THUNDER_SEAL",
                "&e&l◆ Lôi Ấn ◆",
                Material.TRIDENT,
                "&7Click vào quái: Gọi sét đánh, tiêu hao linh lực",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.TRIDENT, 1);
                    put(Material.DIAMOND, 4);
                    put(Material.DRAGON_BREATH, 2);
                }},
                30, 35, 35.0,
                "Luyện Khí Thuật", 5
        ));

        // Phượng Hoàng Lệnh: 1 Lông + 8 Vàng + 4 Netherite + 1 Trứng Rồng
        RECIPES.add(new ArtifactRecipe(
                "PHOENIX_REBIRTH",
                "&6&l◆ Phượng Hoàng Lệnh ◆",
                Material.FEATHER,
                "&7Tự động: Hồi sinh 1 lần sau khi chết, CD 1 ngày",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.FEATHER, 1);
                    put(Material.GOLD_BLOCK, 8);
                    put(Material.NETHERITE_INGOT, 4);
                    put(Material.DRAGON_EGG, 1);
                }},
                60, 120, 10.0,
                "Luyện Khí Thuật", 8
        ));
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
        Inventory gui = Bukkit.createInventory(null, 54,
                ColorUtils.colorize("&8✦ Luyện Chế Pháp Bảo ✦"));

        // Khu vực nguyên liệu - để trống, không đặt glass
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3,
                                   SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
            gui.setItem(slot, null);
        }

        // Nút chế tạo
        gui.setItem(SLOT_CRAFT, new ItemBuilder(Material.ANVIL)
                .setGlow(true)
                .setName("&6&l⚒ Chế Tạo Pháp Bảo")
                .setLore(
                        "",
                        "&7Đặt vật liệu vào ô bên trái",
                        "&7Bấm nút này để luyện chế",
                        "",
                        "&cYêu cầu: Kỹ năng Luyện Khí Thuật"
                ).build());

        // Khu vực kết quả - để trống, không đặt BARRIER
        gui.setItem(SLOT_RESULT, null);

        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.PAPER)
                .setName("&e&lTrạng Thái")
                .setLore("", "&7Sẵn sàng chế tạo!")
                .build());

        gui.setItem(SLOT_GUIDE, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .setName("&6&lCông Thức Pháp Bảo")
                .setLore(
                        "",
                        "&bKiếm Phi Hành: &71 Kiếm DC + 8 DC + 4 Lông",
                        "&6Linh Chung: &71 Chuông + 4 Vàng + 2 DC",
                        "&5Bát Quái Kính: &71 Khiên + 4 Obsidian + 4 Ngọc",
                        "&aHồn Ngọc: &71 Ngọc + 4 Vàng + 2 Mắt End",
                        "&4Thiên Linh Thuẫn: &71 Ngực Netherite + 8 Mắt End",
                        "&eLôi Ấn: &71 Đinh ba + 4 DC + 2 Nước Rồng",
                        "&6Phượng Hoàng Lệnh: &71 Lông + 8 Vàng Khối + 4 Netherite + 1 Trứng Rồng"
                ).build());

        // Viền - chỉ đặt glass vào các slot trống còn lại (không phải input, result)
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName("&r")
                .build();
        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null && !isInputSlot(i) && i != SLOT_RESULT) {
                gui.setItem(i, border);
            }
        }

        gui.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW)
                .setName("&e&l← Quay Lại")
                .build());

        activeSessions.put(player.getUniqueId(), new ArtifactSession(player.getUniqueId()));
        player.openInventory(gui);
    }

    /**
     * Xử lý drag trong GUI chế tạo pháp bảo - chặn drag item vào các slot không được phép
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Chế Pháp Bảo")) return;

        // Chỉ chặn nếu có slot nào trong top inventory (0-53) KHÔNG phải input slot
        // Cho phép drag từ bottom inventory (>=54) vào input slots
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

        // Title-based detection
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Chế Pháp Bảo")) return;

        Player player = (Player) event.getWhoClicked();
        ArtifactSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            event.setCancelled(true);
            return;
        }

        int slot = event.getRawSlot();
        Inventory gui = event.getInventory();

        // Bottom inventory: cho phép tương tác tự do
        if (slot >= 54) {
            return;
        }

        // Slot âm: chặn
        if (slot < 0) {
            event.setCancelled(true);
            return;
        }

        // Input slots: xử lý THỦ CÔNG để đảm bảo hoạt động
        if (isInputSlot(slot)) {
            event.setCancelled(true);
            handleInputSlotClick(event, player, gui, slot);
            return;
        }

        // Result slot: cho phép lấy thành phẩm bằng cursor
        if (slot == SLOT_RESULT) {
            ItemStack current = event.getCurrentItem();
            if (current == null || current.getType() == Material.AIR) return;
            // Để Bukkit xử lý tự nhiên: đặt item lên cursor
            return;
        }

        // Chặn tất cả các slot khác trong top inventory
        event.setCancelled(true);

        // Các nút chức năng
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ClickType clickType = event.getClick();
        if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) {
            switch (slot) {
                case SLOT_CRAFT:
                    attemptCraft(player, session);
                    break;
                case SLOT_BACK:
                    mainMenu.openMainMenu(player);
                    break;
                case SLOT_GUIDE:
                    showRecipe(player);
                    break;
            }
        }
    }

    /**
     * Xử lý THỦ CÔNG click trên input slot - đảm bảo hoạt động bất kể event system
     */
    private void handleInputSlotClick(InventoryClickEvent event, Player player, Inventory gui, int slot) {
        ItemStack cursor = event.getCursor();
        ItemStack current = gui.getItem(slot);
        ClickType click = event.getClick();

        if (click == ClickType.LEFT || click == ClickType.RIGHT) {
            // Hoán đổi cursor và item trong slot
            gui.setItem(slot, cursor);
            player.setItemOnCursor(current);
        } else if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
            if (current != null && current.getType() != Material.AIR) {
                // Chuyển item từ GUI về player inventory
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(current.clone());
                if (leftover.isEmpty()) {
                    gui.setItem(slot, null);
                } else {
                    gui.setItem(slot, leftover.get(0));
                }
            } else if (cursor != null && cursor.getType() != Material.AIR) {
                // Đặt cursor item vào slot
                gui.setItem(slot, cursor.clone());
                player.setItemOnCursor(null);
            }
        } else if (click == ClickType.NUMBER_KEY) {
            int hotbarSlot = event.getHotbarButton();
            ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
            gui.setItem(slot, hotbarItem);
            player.getInventory().setItem(hotbarSlot, current);
        }
        // Các click type khác: bỏ qua
    }

    private void attemptCraft(Player player, ArtifactSession session) {
        Inventory gui = player.getOpenInventory().getTopInventory();

        Map<Material, Integer> ingredients = new HashMap<>();
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3,
                                   SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                ingredients.put(item.getType(),
                        ingredients.getOrDefault(item.getType(), 0) + item.getAmount());
            }
        }

        if (ingredients.isEmpty()) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE)
                    .setName("&c&lKhông Có Vật Liệu")
                    .setLore("", "&7Hãy đặt vật liệu vào ô bên trái!")
                    .build());
            return;
        }

        ArtifactRecipe matchedRecipe = null;
        for (ArtifactRecipe recipe : RECIPES) {
            if (matchesRecipe(recipe, ingredients)) {
                matchedRecipe = recipe;
                break;
            }
        }

        if (matchedRecipe == null) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE)
                    .setName("&c&lKhông Có Công Thức Phù Hợp")
                    .setLore("", "&7Vật liệu không khớp với bất kỳ công thức nào!")
                    .build());
            return;
        }

        PlayerCultivationData data = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (data != null && data.getLevel() < matchedRecipe.requiredLevel) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK)
                    .setName("&c&lKhông Đủ Tu Vi")
                    .setLore(
                            "",
                            "&7Yêu cầu: &cCấp " + matchedRecipe.requiredLevel,
                            "&7Hiện tại: &eCấp " + data.getLevel()
                    ).build());
            return;
        }

        // Copy to effectively final variables for lambda
        final ArtifactRecipe finalRecipe = matchedRecipe;
        final PlayerCultivationData finalData = data;
        final Inventory finalGui = gui;
        final UUID playerUUID = player.getUniqueId();

        // Bắt đầu chế tạo
        finalGui.setItem(SLOT_STATUS, new ItemBuilder(Material.FURNACE)
                .setName("&6&lĐang Luyện Chế...")
                .setLore(
                        "",
                        "&7Pháp bảo: " + finalRecipe.displayName,
                        "&7Thời gian: &e" + finalRecipe.craftingTime + " giây",
                        "&7Tỉ lệ: &a" + finalRecipe.successChance + "%"
                ).build());

        // Chỉ tiêu hao đúng số lượng vật liệu cần, trả surplus vào túi
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3,
                                   SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6}) {
            ItemStack item = finalGui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                int needed = finalRecipe.ingredients.getOrDefault(item.getType(), 0);
                if (needed > 0) {
                    int surplus = item.getAmount() - needed;
                    if (surplus > 0) {
                        ItemStack returnItem = item.clone();
                        returnItem.setAmount(surplus);
                        player.getInventory().addItem(returnItem);
                    }
                    finalGui.setItem(slot, null);
                }
                // Nếu là item không phải nguyên liệu của công thức này, giữ lại
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
                ItemStack result = new ItemBuilder(finalRecipe.resultMaterial)
                        .setName(finalRecipe.displayName)
                        .setGlow(true)
                        .setLore(
                                "",
                                finalRecipe.lore,
                                "",
                                "&6&l✦ Pháp bảo thượng phẩm ✦",
                                "&7Luyện chế bởi: &e" + p.getName()
                        ).build();

                inv.setItem(SLOT_RESULT, result);
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.EMERALD)
                        .setName("&a&l✦ Chế Tạo Thành Công ✦")
                        .setLore("", "&7Pháp bảo: " + finalRecipe.displayName)
                        .build());

                if (finalData != null) {
                    double expReward = finalRecipe.craftingTime * 3;
                    plugin.getCultivationManager().addExperience(p, expReward);
                    MessageUtils.send(p, "&6✦ Chế tạo pháp bảo thành công! Nhận &e" + (int)expReward + " &6tu vi!");
                }

                MessageUtils.playSound(p, Sound.BLOCK_ANVIL_USE);
            } else {
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK)
                        .setName("&c&lChế Tạo Thất Bại")
                        .setLore("", "&7Mất hết vật liệu! Hãy thử lại!")
                        .build());
                MessageUtils.send(p, "&cChế tạo pháp bảo thất bại! Mất hết vật liệu.");
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
        MessageUtils.send(player, "&6&l= Công Thức Luyện Chế Pháp Bảo =");
        MessageUtils.send(player, "&bKiếm Phi Hành: &71 Kiếm Kim Cương + 8 Kim Cương + 4 Lông Vũ");
        MessageUtils.send(player, "&6Linh Chung: &71 Chuông + 4 Vàng + 2 Kim Cương");
        MessageUtils.send(player, "&5Bát Quái Kính: &71 Khiên + 4 Obsidian + 4 Ngọc Lục Bảo");
        MessageUtils.send(player, "&aHồn Ngọc: &71 Ngọc Lục Bảo + 4 Vàng + 2 Mắt End");
        MessageUtils.send(player, "&4Thiên Linh Thuẫn: &71 Ngực Netherite + 8 Mắt End");
        MessageUtils.send(player, "&eLôi Ấn: &71 Đinh ba + 4 Kim Cương + 2 Hơi Rồng");
        MessageUtils.send(player, "&6Phượng Hoàng Lệnh: &71 Lông + 8 Khối Vàng + 4 Netherite + 1 Trứng Rồng");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        ArtifactSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        Inventory gui = event.getInventory();
        for (int slot : new int[]{SLOT_INPUT_1, SLOT_INPUT_2, SLOT_INPUT_3,
                                   SLOT_INPUT_4, SLOT_INPUT_5, SLOT_INPUT_6, SLOT_RESULT}) {
            ItemStack item = gui.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                player.getInventory().addItem(item);
            }
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

        ArtifactRecipe(String id, String displayName, Material resultMaterial,
                      String lore, Map<Material, Integer> ingredients,
                      int requiredLevel, int craftingTime, double successChance,
                      String requiredSkill, int skillLevel) {
            this.id = id;
            this.displayName = displayName;
            this.resultMaterial = resultMaterial;
            this.lore = lore;
            this.ingredients = ingredients;
            this.requiredLevel = requiredLevel;
            this.craftingTime = craftingTime;
            this.successChance = successChance;
            this.requiredSkill = requiredSkill;
            this.skillLevel = skillLevel;
        }
    }

    private static class ArtifactSession {
        final UUID playerUUID;
        ArtifactSession(UUID playerUUID) { this.playerUUID = playerUUID; }
    }
}