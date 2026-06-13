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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.*;

/**
 * AlchemyCraftGUI - Giao diện luyện đan
 * Người chơi đặt nguyên liệu vào UI, bấm nút để luyện
 * Yêu cầu kỹ năng: Khống Hỏa Thuật
 */
public class AlchemyCraftGUI implements Listener {

    private final VNMinePlugin plugin;
    private final MainMenuGUI mainMenu;

    // Các slot cố định
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

    // Định nghĩa công thức luyện đan
    private static final List<AlchemyRecipe> RECIPES = new ArrayList<>();

    static {
        // Hồi Linh Đan: 3 Linh Thảo + 1 Nước
        RECIPES.add(new AlchemyRecipe(
                "HOI_LINH_DAN",
                "&aHồi Linh Đan",
                Material.GLOWSTONE_DUST,
                "&7Hồi phục &b30 &7linh lực ngay lập tức",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.GREEN_DYE, 3);     // Linh Thảo
                    put(Material.POTION, 1);         // Nước (potion water)
                }},
                3, 10, 80.0, 2
        ));

        // Đại Hồi Linh Đan: 2 Hồi Linh Đan + 2 Huyết Linh Thảo + 5 Linh Thảo
        RECIPES.add(new AlchemyRecipe(
                "DAI_HOI_LINH_DAN",
                "&bĐại Hồi Linh Đan",
                Material.GLOWSTONE,
                "&7Hồi phục &b100 &7linh lực + 20% hồi phục 30s",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.GLOWSTONE_DUST, 2); // Hồi Linh Đan
                    put(Material.RED_DYE, 2);         // Huyết Linh Thảo
                    put(Material.GREEN_DYE, 5);       // Linh Thảo
                }},
                10, 30, 60.0, 1
        ));

        // Cương Thể Đan: 3 Huyết Linh Thảo + 5 Linh Thảo + 1 Blaze Powder
        RECIPES.add(new AlchemyRecipe(
                "CUONG_THE_DAN",
                "&cCương Thể Đan",
                Material.REDSTONE_BLOCK,
                "&7Tăng &c20% sát thương &7trong 60 giây",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.RED_DYE, 3);          // Huyết Linh Thảo
                    put(Material.GREEN_DYE, 5);        // Linh Thảo
                    put(Material.BLAZE_POWDER, 1);     // Blaze Powder
                }},
                15, 20, 55.0, 1
        ));

        // Thanh Tâm Đan: 5 Linh Thảo + 1 Nước
        RECIPES.add(new AlchemyRecipe(
                "THANH_TAM_DAN",
                "&aThanh Tâm Đan",
                Material.SUGAR,
                "&7Giải trừ mọi trạng thái xấu (độc, mù, chậm...)",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.GREEN_DYE, 5);
                    put(Material.POTION, 1);
                }},
                5, 15, 85.0, 2
        ));

        // Tốc Thánh Đan: 3 Linh Thảo + 2 Đường + 1 Lông
        RECIPES.add(new AlchemyRecipe(
                "TOC_THANH_DAN",
                "&bTốc Thánh Đan",
                Material.FEATHER,
                "&7Tăng &b50% tốc độ &7trong 30 giây",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.GREEN_DYE, 3);
                    put(Material.SUGAR, 2);
                    put(Material.FEATHER, 1);
                }},
                8, 15, 70.0, 2
        ));

        // Tu Luyện Đan: 10 Linh Thảo + 5 Huyết Linh Thảo + 2 Long Huyết Thảo + 1 Vàng
        RECIPES.add(new AlchemyRecipe(
                "TU_LUYEN_DAN",
                "&5Tu Luyện Đan",
                Material.PURPLE_DYE,
                "&7Tăng &5+50 EXP &7khi sử dụng",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.GREEN_DYE, 10);
                    put(Material.RED_DYE, 5);
                    put(Material.ORANGE_DYE, 2);      // Long Huyết Thảo
                    put(Material.GOLD_INGOT, 1);
                }},
                20, 45, 40.0, 1
        ));

        // Phi Thăng Đan: 3 Tu Luyện Đan + 10 Long Huyết Thảo + 1 Hơi Rồng + 2 Netherite
        RECIPES.add(new AlchemyRecipe(
                "PHI_THANG_DAN",
                "&6&l◆ Phi Thăng Đan ◆",
                Material.NETHER_STAR,
                "&7+500 EXP ngay lập tức (1 lần/đại cảnh giới)",
                new LinkedHashMap<Material, Integer>() {{
                    put(Material.PURPLE_DYE, 3);       // Tu Luyện Đan
                    put(Material.ORANGE_DYE, 10);      // Long Huyết Thảo
                    put(Material.DRAGON_BREATH, 1);
                    put(Material.NETHERITE_INGOT, 2);
                }},
                50, 120, 15.0, 1
        ));
    }

    // Các GUI đang mở
    private static final Map<UUID, AlchemySession> activeSessions = new HashMap<>();

    public AlchemyCraftGUI(VNMinePlugin plugin, MainMenuGUI mainMenu) {
        this.plugin = plugin;
        this.mainMenu = mainMenu;
    }

    /**
     * Mở GUI luyện đan cho player
     */
    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54,
                ColorUtils.colorize("&8✦ Luyện Đan - Khống Hỏa Thuật ✦"));

        // Viền trang trí
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName("&r")
                .build();

        // Khu vực nguyên liệu (3x2) - để trống, không đặt glass
        for (int slot : new int[]{19, 20, 21, 28, 29, 30}) {
            gui.setItem(slot, null);
        }

        // Nút luyện đan
        gui.setItem(SLOT_CRAFT, new ItemBuilder(Material.FIRE_CHARGE)
                .setGlow(true)
                .setName("&c&l🔥 Luyện Đan")
                .setLore(
                        "",
                        "&7Đặt nguyên liệu vào ô bên trái",
                        "&7Bấm nút này để luyện đan",
                        "",
                        "&cYêu cầu: Kỹ năng Khống Hỏa Thuật",
                        "&7Cấp 3+ để sử dụng"
                ).build());

        // Khu vực kết quả - để trống, không đặt BARRIER
        gui.setItem(SLOT_RESULT, null);

        // Trạng thái
        gui.setItem(SLOT_STATUS, new ItemBuilder(Material.PAPER)
                .setName("&e&lTrạng Thái")
                .setLore(
                        "",
                        "&7Sẵn sàng luyện đan!",
                        "&7Chọn nguyên liệu và bấm Luyện Đan"
                ).build());

        // Danh sách công thức
        gui.setItem(SLOT_GUIDE, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .setName("&6&lCông Thức Luyện Đan")
                .setLore(
                        "",
                        "&aHồi Linh Đan: &73 Linh Thảo + 1 Nước",
                        "&bĐại Hồi Linh Đan: &72 Hồi Linh Đan + 2 Huyết LT + 5 LT",
                        "&cCương Thể Đan: &73 Huyết LT + 5 LT + 1 Blaze",
                        "&aThanh Tâm Đan: &75 LT + 1 Nước",
                        "&bTốc Thánh Đan: &73 LT + 2 Đường + 1 Lông",
                        "&5Tu Luyện Đan: &710 LT + 5 Huyết LT + 2 Long Huyết + 1 Vàng",
                        "&6Phi Thăng Đan: &73 Tu Luyện Đan + 10 Long Huyết + 1 Hơi Rồng + 2 Netherite"
                ).build());

        // Viền - chỉ đặt glass vào các slot trống còn lại (không phải input, result)
        for (int i = 0; i < 54; i++) {
            if (gui.getItem(i) == null && !isInputSlot(i) && i != SLOT_RESULT) {
                gui.setItem(i, border);
            }
        }

        // Nút quay lại
        gui.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW)
                .setName("&e&l← Quay Lại")
                .build());

        // Tạo session
        activeSessions.put(player.getUniqueId(), new AlchemySession(player.getUniqueId()));

        player.openInventory(gui);
    }

    private boolean isInputSlot(int slot) {
        return slot == SLOT_INPUT_1 || slot == SLOT_INPUT_2 || slot == SLOT_INPUT_3 ||
               slot == SLOT_INPUT_4 || slot == SLOT_INPUT_5 || slot == SLOT_INPUT_6;
    }

    /**
     * Xử lý drag trong GUI luyện đan - chặn drag item vào các slot không được phép
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Đan")) return;

        // Chỉ chặn nếu có slot nào trong top inventory (0-53) KHÔNG phải input slot
        // Cho phép drag từ bottom inventory (>=54) vào input slots
        for (Integer slot : event.getRawSlots()) {
            if (slot < 54 && !isInputSlot(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Xử lý click trong GUI luyện đan
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Title-based detection
        String title = ColorUtils.stripColor(event.getView().getTitle());
        if (!title.contains("Luyện Đan")) return;

        Player player = (Player) event.getWhoClicked();
        AlchemySession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            event.setCancelled(true);
            return;
        }

        int slot = event.getRawSlot();
        Inventory gui = event.getInventory();

        // Bottom inventory (slot >= 54): cho phép tương tác tự do
        if (slot >= 54) {
            return;
        }

        // Slot âm: chặn
        if (slot < 0) {
            event.setCancelled(true);
            return;
        }

        // Input slots (19-21, 28-30): xử lý THỦ CÔNG để đảm bảo hoạt động
        if (isInputSlot(slot)) {
            event.setCancelled(true);
            handleInputSlotClick(event, player, gui, slot);
            return;
        }

        // Result slot (24): cho phép lấy thành phẩm bằng cursor
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
                    MessageUtils.send(player, "&6&lCông Thức Luyện Đan:");
                    MessageUtils.send(player, "&aHồi Linh Đan: &73 Linh Thảo + 1 Nước → Hồi 30 mana");
                    MessageUtils.send(player, "&bĐại Hồi Linh Đan: &72 Hồi Linh Đan + 2 Huyết LT + 5 LT → Hồi 100 mana");
                    MessageUtils.send(player, "&cCương Thể Đan: &73 Huyết LT + 5 LT + 1 Blaze → +20% DMG 60s");
                    MessageUtils.send(player, "&aThanh Tâm Đan: &75 LT + 1 Nước → Giải trừ trạng thái");
                    MessageUtils.send(player, "&bTốc Thánh Đan: &73 LT + 2 Đường + 1 Lông → +50% Speed 30s");
                    MessageUtils.send(player, "&5Tu Luyện Đan: &710 LT + 5 Huyết LT + 2 Long Huyết + 1 Vàng → +50 EXP");
                    MessageUtils.send(player, "&6Phi Thăng Đan: &73 Tu Luyện Đan + 10 Long Huyết + 1 Hơi Rồng + 2 Netherite → +500 EXP");
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

    /**
     * Thực hiện luyện đan
     */
    private void attemptCraft(Player player, AlchemySession session) {
        Inventory gui = player.getOpenInventory().getTopInventory();

        // Thu thập nguyên liệu từ các slot
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
                    .setName("&c&lKhông Có Nguyên Liệu")
                    .setLore("", "&7Hãy đặt nguyên liệu vào ô bên trái!")
                    .build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // Tìm công thức phù hợp
        AlchemyRecipe matchedRecipe = null;
        for (AlchemyRecipe recipe : RECIPES) {
            if (matchesRecipe(recipe, ingredients)) {
                matchedRecipe = recipe;
                break;
            }
        }

        if (matchedRecipe == null) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE)
                    .setName("&c&lKhông Có Công Thức Phù Hợp")
                    .setLore("", "&7Nguyên liệu không khớp với bất kỳ công thức nào!")
                    .build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // Kiểm tra level yêu cầu
        PlayerCultivationData playerData = plugin.getCultivationManager().getPlayerData(player.getUniqueId());
        if (playerData != null && playerData.getLevel() < matchedRecipe.requiredLevel) {
            gui.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK)
                    .setName("&c&lKhông Đủ Tu Vi")
                    .setLore(
                            "",
                            "&7Yêu cầu: &cCấp " + matchedRecipe.requiredLevel,
                            "&7Hiện tại: &eCấp " + playerData.getLevel()
                    ).build());
            MessageUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }

        // Copy to effectively final variables for lambda
        final AlchemyRecipe finalRecipe = matchedRecipe;
        final PlayerCultivationData finalData = playerData;
        final Inventory finalGui = gui;
        final UUID playerUUID = player.getUniqueId();

        // Tiến hành luyện đan (có delay)
        finalGui.setItem(SLOT_STATUS, new ItemBuilder(Material.FURNACE)
                .setName("&6&lĐang Luyện Đan...")
                .setLore(
                        "",
                        "&7Đan dược: " + finalRecipe.displayName,
                        "&7Thời gian: &e" + finalRecipe.cookingTime + " giây",
                        "&7Tỉ lệ: &a" + finalRecipe.successChance + "%"
                ).build());

        // Chỉ tiêu hao đúng số lượng nguyên liệu cần, trả surplus vào túi
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

        MessageUtils.playSound(player, Sound.BLOCK_FIRE_AMBIENT);

        // Tạo task luyện đan
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player p = Bukkit.getPlayer(playerUUID);
            if (p == null || !p.isOnline()) return;

            Inventory inv = p.getOpenInventory().getTopInventory();
            if (!inv.equals(finalGui)) return;

            // Tính toán tỉ lệ thành công
            double chance = finalRecipe.successChance;
            int levelBonus = (finalData != null) ? finalData.getLevel() : 0;
            chance += levelBonus * 1.5;
            chance = Math.min(chance, 95.0);

            Random random = new Random();
            boolean success = random.nextDouble() * 100 < chance;

            if (success) {
                // Thành công
                ItemStack result = new ItemBuilder(finalRecipe.resultMaterial)
                        .setName(finalRecipe.displayName)
                        .setAmount(finalRecipe.yield)
                        .setGlow(true)
                        .setLore(
                                "",
                                finalRecipe.lore,
                                "",
                                "&a&l✦ Luyện đan thành công ✦",
                                "&7Cấp độ: &a" + (int)(chance / 10) + " sao"
                        ).build();

                inv.setItem(SLOT_RESULT, result);
                inv.setItem(SLOT_STATUS, new ItemBuilder(Material.EMERALD)
                        .setName("&a&l✦ Luyện Đan Thành Công ✦")
                        .setLore(
                                "",
                                "&7Đan dược: " + finalRecipe.displayName,
                                "&7Số lượng: &e" + finalRecipe.yield,
                                "&7Tỉ lệ thành công: &a" + String.format("%.1f", chance) + "%"
                        ).build());

                // Thêm exp và thống kê
                if (finalData != null) {
                    double expReward = finalRecipe.cookingTime * 2;
                    plugin.getCultivationManager().addExperience(p, expReward);
                    finalData.incrementPillsCrafted();
                    MessageUtils.send(p, "&a✦ Luyện đan thành công! Nhận &e" + (int)expReward + " &atu vi!");
                }

                MessageUtils.playSound(p, Sound.BLOCK_BREWING_STAND_BREW);

            } else {
                // Thất bại
                double failRand = random.nextDouble() * 100;

                if (failRand < 60) {
                    inv.setItem(SLOT_RESULT, new ItemBuilder(Material.GUNPOWDER)
                            .setName("&7Phế Liệu Luyện Đan")
                            .setLore("", "&7Có thể dùng làm phân bón")
                            .setAmount(1)
                            .build());
                    inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK)
                            .setName("&c&lLuyện Đan Thất Bại")
                            .setLore("", "&7Luyện đan thất bại! Thu được phế liệu.")
                            .build());
                    MessageUtils.send(p, "&eLuyện đan thất bại! Thu được phế liệu.");
                } else if (failRand < 90) {
                    inv.setItem(SLOT_RESULT, new ItemBuilder(Material.BARRIER)
                            .setName("&c&lThất Bại")
                            .setLore("", "&7Mất hết nguyên liệu")
                            .build());
                    inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK)
                            .setName("&c&lLuyện Đan Thất Bại")
                            .setLore("", "&7Luyện đan thất bại! Mất hết nguyên liệu!")
                            .build());
                    MessageUtils.send(p, "&cLuyện đan thất bại! Mất hết nguyên liệu!");
                } else {
                    p.getWorld().createExplosion(p.getLocation(), 1.5f, false, false);
                    inv.setItem(SLOT_RESULT, new ItemBuilder(Material.BARRIER)
                            .setName("&c&lNỔ LÒ!")
                            .setLore("", "&7Lò luyện đan phát nổ!")
                            .build());
                    inv.setItem(SLOT_STATUS, new ItemBuilder(Material.REDSTONE_BLOCK)
                            .setName("&c&lNỔ LÒ!")
                            .setLore("", "&4LÒ LUYỆN ĐAN PHÁT NỔ! Tránh xa!")
                            .build());
                    MessageUtils.send(p, "&4&lLÒ LUYỆN ĐAN PHÁT NỔ! Tránh xa!");
                }

                MessageUtils.playSound(p, Sound.ENTITY_GENERIC_EXPLODE);
            }
        }, finalRecipe.cookingTime * 20L);
    }

    /**
     * Kiểm tra nguyên liệu có khớp công thức không
     */
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

        // Trả nguyên liệu và thành phẩm về túi nếu còn trong GUI
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

    // ==================== INNER CLASSES ====================

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

        public AlchemyRecipe(String id, String displayName, Material resultMaterial,
                            String lore, Map<Material, Integer> ingredients,
                            int requiredLevel, int cookingTime, double successChance, int yield) {
            this.id = id;
            this.displayName = displayName;
            this.resultMaterial = resultMaterial;
            this.lore = lore;
            this.ingredients = ingredients;
            this.requiredLevel = requiredLevel;
            this.cookingTime = cookingTime;
            this.successChance = successChance;
            this.yield = yield;
        }
    }

    private static class AlchemySession {
        final UUID playerUUID;
        boolean isCrafting;

        AlchemySession(UUID playerUUID) {
            this.playerUUID = playerUUID;
            this.isCrafting = false;
        }
    }
}